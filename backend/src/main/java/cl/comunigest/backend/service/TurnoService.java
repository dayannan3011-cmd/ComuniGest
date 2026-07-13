package cl.comunigest.backend.service;

import cl.comunigest.backend.entity.Turno;
import cl.comunigest.backend.entity.Usuario;
import cl.comunigest.backend.repository.TurnoRepository;
import cl.comunigest.backend.repository.UsuarioRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TurnoService {

    private final TurnoRepository repository;
    private final UsuarioRepository usuarioRepository;

    public TurnoService(TurnoRepository repository, UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Turno> findAll() {
        return repository.findAllByOrderByFechaInicioDesc();
    }

    public Turno findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Turno no encontrado."));
    }

    public List<Turno> listarPorUsuario(Long usuarioId) {
        validateUsuarioExists(usuarioId);
        return repository.findByUsuarioIdOrderByFechaInicioDesc(usuarioId);
    }

    public Optional<Turno> obtenerAbierto(Long usuarioId) {
        validateUsuarioExists(usuarioId);
        return repository.findFirstByUsuarioIdAndEstadoOrderByFechaInicioDesc(
                usuarioId, Turno.EstadoTurno.ABIERTO);
    }

    @Transactional
    public Turno iniciar(Long usuarioId, String observacionesInicio) {
        Usuario usuario = validateConserje(usuarioId);
        if (repository.existsByUsuarioIdAndEstado(usuarioId, Turno.EstadoTurno.ABIERTO)) {
            throw new EntityExistsException("Ya existe un turno abierto para este conserje.");
        }

        Turno turno = new Turno();
        turno.setUsuario(usuario);
        turno.setFechaInicio(LocalDateTime.now());
        turno.setEstado(Turno.EstadoTurno.ABIERTO);
        turno.setObservacionesInicio(normalizeOptional(observacionesInicio));
        return repository.save(turno);
    }

    @Transactional
    public Turno cerrar(Long id, Long usuarioId, String observacionesCierre) {
        validateConserje(usuarioId);
        Turno turno = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No existe un turno abierto para cerrar."));
        if (!turno.getUsuario().getId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para cerrar este turno.");
        }
        if (turno.getEstado() != Turno.EstadoTurno.ABIERTO) {
            throw new EntityNotFoundException("No existe un turno abierto para cerrar.");
        }
        turno.setEstado(Turno.EstadoTurno.CERRADO);
        turno.setFechaCierre(LocalDateTime.now());
        turno.setObservacionesCierre(normalizeOptional(observacionesCierre));
        return repository.save(turno);
    }

    private Usuario validateConserje(Long usuarioId) {
        Usuario usuario = validateUsuarioExists(usuarioId);
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario se encuentra inactivo.");
        }
        if (!"CONSERJE".equalsIgnoreCase(usuario.getPerfil().getNombre())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo un usuario con perfil CONSERJE puede iniciar un turno.");
        }
        return usuario;
    }

    private Usuario validateUsuarioExists(Long usuarioId) {
        if (usuarioId == null) {
            throw new EntityNotFoundException("El usuario no existe.");
        }
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("El usuario no existe."));
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
