package cl.comunigest.backend.service;

import cl.comunigest.backend.entity.Encomienda;
import cl.comunigest.backend.entity.Departamento;
import cl.comunigest.backend.entity.Turno;
import cl.comunigest.backend.entity.Usuario;
import cl.comunigest.backend.repository.DepartamentoRepository;
import cl.comunigest.backend.repository.EncomiendaRepository;
import cl.comunigest.backend.repository.TurnoRepository;
import cl.comunigest.backend.repository.UsuarioRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class EncomiendaService {

    private final EncomiendaRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final TurnoRepository turnoRepository;
    private final DepartamentoRepository departamentoRepository;

    public EncomiendaService(EncomiendaRepository repository, UsuarioRepository usuarioRepository,
                             TurnoRepository turnoRepository, DepartamentoRepository departamentoRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.turnoRepository = turnoRepository;
        this.departamentoRepository = departamentoRepository;
    }

    public java.util.List<Encomienda> findAll() { return repository.findAllByOrderByFechaRecepcionDesc(); }

    public java.util.List<Encomienda> findPendientes() {
        return repository.findByEstadoOrderByFechaRecepcionDesc(Encomienda.EstadoEncomienda.PENDIENTE);
    }

    public java.util.List<Encomienda> findMesActual() {
        LocalDateTime desde = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
        return repository.findByFechaRecepcionGreaterThanEqualAndFechaRecepcionLessThanOrderByFechaRecepcionDesc(
                desde, desde.plusMonths(1));
    }

    public Encomienda findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Encomienda no encontrada."));
    }

    @Transactional
    public Encomienda registrarRecepcion(Long usuarioId, Long departamentoId, String destinatario,
                                         String descripcion, String empresaRepartidor) {
        Turno turno = requireTurnoAbierto(usuarioId, false);
        Departamento departamento = departamentoRepository.findById(departamentoId)
                .orElseThrow(() -> new EntityNotFoundException("El departamento seleccionado no existe."));
        Encomienda encomienda = new Encomienda();
        encomienda.setDestinatario(destinatario.trim());
        encomienda.setDescripcion(descripcion.trim());
        encomienda.setEmpresaRepartidor(normalizeOptional(empresaRepartidor));
        encomienda.setDepartamento(departamento);
        encomienda.setTurnoRecepcion(turno);
        encomienda.setFechaRecepcion(LocalDateTime.now());
        encomienda.setEstado(Encomienda.EstadoEncomienda.PENDIENTE);
        return repository.save(encomienda);
    }

    @Transactional
    public Encomienda entregar(Long id, Long usuarioId, String entregadoA) {
        if (entregadoA == null || entregadoA.isBlank()) {
            throw new IllegalArgumentException("Debes indicar quién retira la encomienda.");
        }
        Turno turno = requireTurnoAbierto(usuarioId, true);
        Encomienda encomienda = findById(id);
        if (encomienda.getEstado() != Encomienda.EstadoEncomienda.PENDIENTE || encomienda.getFechaEntrega() != null) {
            throw new EntityExistsException("La encomienda ya fue entregada.");
        }
        encomienda.setEstado(Encomienda.EstadoEncomienda.ENTREGADA);
        encomienda.setEntregadoA(entregadoA.trim());
        encomienda.setTurnoEntrega(turno);
        encomienda.setFechaEntrega(LocalDateTime.now());
        return repository.save(encomienda);
    }

    private Turno requireTurnoAbierto(Long usuarioId, boolean entrega) {
        Usuario usuario = validateConserje(usuarioId);
        String message = entrega
                ? "Debes iniciar un turno antes de registrar la entrega."
                : "Debes iniciar un turno antes de registrar encomiendas.";
        return turnoRepository.findFirstByUsuarioIdAndEstadoOrderByFechaInicioDesc(
                        usuario.getId(), Turno.EstadoTurno.ABIERTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, message));
    }

    private Usuario validateConserje(Long usuarioId) {
        Usuario usuario = usuarioId == null ? null : usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) throw new EntityNotFoundException("El usuario no existe.");
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario se encuentra inactivo.");
        }
        if (usuario.getPerfil() == null || !"CONSERJE".equalsIgnoreCase(usuario.getPerfil().getNombre())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo un usuario con perfil CONSERJE puede gestionar encomiendas.");
        }
        return usuario;
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
