package cl.comunigest.backend.service;

import cl.comunigest.backend.entity.Departamento;
import cl.comunigest.backend.entity.Turno;
import cl.comunigest.backend.entity.Usuario;
import cl.comunigest.backend.entity.Visita;
import cl.comunigest.backend.repository.DepartamentoRepository;
import cl.comunigest.backend.repository.TurnoRepository;
import cl.comunigest.backend.repository.UsuarioRepository;
import cl.comunigest.backend.repository.VisitaRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VisitaService {

    private final VisitaRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final TurnoRepository turnoRepository;
    private final DepartamentoRepository departamentoRepository;

    public VisitaService(VisitaRepository repository, UsuarioRepository usuarioRepository,
                         TurnoRepository turnoRepository, DepartamentoRepository departamentoRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.turnoRepository = turnoRepository;
        this.departamentoRepository = departamentoRepository;
    }

    public List<Visita> findAll() {
        return repository.findAllByOrderByFechaIngresoDesc();
    }

    public List<Visita> findActivas() {
        return repository.findByEstadoOrderByFechaIngresoDesc(Visita.EstadoVisita.INGRESADA);
    }

    public Visita findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Visita no encontrada."));
    }

    @Transactional
    public Visita registrarIngreso(Long usuarioId, Long departamentoId, String nombreVisitante,
                                   String documento, String patente) {
        Turno turno = requireTurnoAbierto(usuarioId);
        Departamento departamento = departamentoRepository.findById(departamentoId)
                .orElseThrow(() -> new EntityNotFoundException("El departamento seleccionado no existe."));

        Visita visita = new Visita();
        visita.setNombreVisitante(nombreVisitante.trim());
        visita.setDocumento(documento.trim());
        visita.setPatente(normalizeOptional(patente));
        visita.setDepartamento(departamento);
        visita.setTurnoIngreso(turno);
        visita.setFechaIngreso(LocalDateTime.now());
        visita.setEstado(Visita.EstadoVisita.INGRESADA);
        return repository.save(visita);
    }

    @Transactional
    public Visita registrarSalida(Long id, Long usuarioId) {
        Turno turnoSalida = requireTurnoAbierto(usuarioId);
        Visita visita = findById(id);
        if (visita.getEstado() == Visita.EstadoVisita.SALIDA || visita.getFechaSalida() != null) {
            throw new EntityExistsException("La visita ya tiene una salida registrada.");
        }
        visita.setTurnoSalida(turnoSalida);
        visita.setFechaSalida(LocalDateTime.now());
        visita.setEstado(Visita.EstadoVisita.SALIDA);
        return repository.save(visita);
    }

    private Turno requireTurnoAbierto(Long usuarioId) {
        Usuario usuario = validateConserje(usuarioId);
        return turnoRepository.findFirstByUsuarioIdAndEstadoOrderByFechaInicioDesc(
                        usuario.getId(), Turno.EstadoTurno.ABIERTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "Debes iniciar un turno antes de registrar visitas."));
    }

    private Usuario validateConserje(Long usuarioId) {
        Usuario usuario = usuarioId == null ? null : usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) {
            throw new EntityNotFoundException("El usuario no existe.");
        }
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario se encuentra inactivo.");
        }
        if (!"CONSERJE".equalsIgnoreCase(usuario.getPerfil().getNombre())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo un usuario con perfil CONSERJE puede registrar visitas.");
        }
        return usuario;
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase();
    }
}
