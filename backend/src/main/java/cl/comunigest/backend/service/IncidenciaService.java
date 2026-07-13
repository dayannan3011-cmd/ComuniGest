package cl.comunigest.backend.service;

import cl.comunigest.backend.entity.Incidencia;
import cl.comunigest.backend.entity.Turno;
import cl.comunigest.backend.entity.Usuario;
import cl.comunigest.backend.repository.IncidenciaRepository;
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
import java.util.Set;

@Service
public class IncidenciaService {
    private static final Set<String> CATEGORIAS = Set.of(
            "SEGURIDAD", "INFRAESTRUCTURA", "ACCESO", "RUIDO O CONVIVENCIA", "SERVICIOS", "OTRO");
    private static final Set<String> CRITICIDADES = Set.of("BAJA", "MEDIA", "ALTA", "CRÍTICA");

    private final IncidenciaRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final TurnoRepository turnoRepository;

    public IncidenciaService(IncidenciaRepository repository, UsuarioRepository usuarioRepository,
                             TurnoRepository turnoRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.turnoRepository = turnoRepository;
    }

    public List<Incidencia> findAll() { return repository.findAllByOrderByFechaRegistroDesc(); }

    public List<Incidencia> findMesActual() {
        LocalDateTime desde = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
        return repository.findParaConserje(
                Incidencia.EstadoIncidencia.ABIERTA,
                Incidencia.EstadoIncidencia.EN_PROCESO,
                Incidencia.EstadoIncidencia.RESUELTA,
                desde, desde.plusMonths(1));
    }

    public Incidencia findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("La incidencia no existe."));
    }

    @Transactional
    public Incidencia registrar(Long usuarioId, String titulo, String descripcion,
                                String categoria, String criticidad) {
        Usuario usuario = requireRole(usuarioId, "CONSERJE");
        Turno turno = turnoRepository.findFirstByUsuarioIdAndEstadoOrderByFechaInicioDesc(
                        usuario.getId(), Turno.EstadoTurno.ABIERTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "Debes iniciar un turno antes de registrar incidencias."));
        String categoriaNormalizada = normalize(categoria);
        String criticidadNormalizada = normalize(criticidad);
        if (!CATEGORIAS.contains(categoriaNormalizada))
            throw new IllegalArgumentException("La categoría seleccionada no es válida.");
        if (!CRITICIDADES.contains(criticidadNormalizada))
            throw new IllegalArgumentException("La criticidad seleccionada no es válida.");

        Incidencia incidencia = new Incidencia();
        incidencia.setTitulo(requireText(titulo, "Debes indicar el título de la incidencia."));
        incidencia.setDescripcion(requireText(descripcion, "Debes indicar la descripción de la incidencia."));
        incidencia.setCategoria(categoriaNormalizada);
        incidencia.setCriticidad(criticidadNormalizada);
        incidencia.setRegistradaPor(usuario);
        incidencia.setTurnoRegistro(turno);
        incidencia.setFechaRegistro(LocalDateTime.now());
        incidencia.setEstado(Incidencia.EstadoIncidencia.ABIERTA);
        return repository.save(incidencia);
    }

    @Transactional
    public Incidencia iniciarGestion(Long id, Long usuarioId) {
        requireRole(usuarioId, "ADMINISTRADOR");
        Incidencia incidencia = requireNotResolved(id);
        if (incidencia.getEstado() != Incidencia.EstadoIncidencia.ABIERTA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La incidencia ya se encuentra en proceso.");
        }
        incidencia.setEstado(Incidencia.EstadoIncidencia.EN_PROCESO);
        return repository.save(incidencia);
    }

    @Transactional
    public Incidencia resolver(Long id, Long usuarioId, String resolucion) {
        Usuario administrador = requireRole(usuarioId, "ADMINISTRADOR");
        Incidencia incidencia = requireNotResolved(id);
        incidencia.setResolucion(requireText(resolucion, "Debes indicar la resolución o medida tomada."));
        incidencia.setFechaResolucion(LocalDateTime.now());
        incidencia.setUsuarioResuelve(administrador);
        incidencia.setEstado(Incidencia.EstadoIncidencia.RESUELTA);
        return repository.save(incidencia);
    }

    private Incidencia requireNotResolved(Long id) {
        Incidencia incidencia = repository.findByIdForUpdate(id)
                .orElseThrow(() -> new EntityNotFoundException("La incidencia no existe."));
        if (incidencia.getEstado() == Incidencia.EstadoIncidencia.RESUELTA
                || incidencia.getFechaResolucion() != null) {
            throw new EntityExistsException("La incidencia ya se encuentra resuelta.");
        }
        return incidencia;
    }

    private Usuario requireRole(Long usuarioId, String role) {
        Usuario usuario = usuarioId == null ? null : usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) throw new EntityNotFoundException("El usuario no existe.");
        if (!Boolean.TRUE.equals(usuario.getActivo()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario se encuentra inactivo.");
        if (usuario.getPerfil() == null || !role.equalsIgnoreCase(usuario.getPerfil().getNombre()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para realizar esta acción.");
        return usuario;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(message);
        return value.trim();
    }
}
