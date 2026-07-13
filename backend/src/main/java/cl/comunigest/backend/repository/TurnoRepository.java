package cl.comunigest.backend.repository;

import cl.comunigest.backend.entity.Turno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TurnoRepository extends JpaRepository<Turno, Long> {
    long countByEstado(Turno.EstadoTurno estado);
    boolean existsByUsuarioIdAndEstado(Long usuarioId, Turno.EstadoTurno estado);
    Optional<Turno> findFirstByUsuarioIdAndEstadoOrderByFechaInicioDesc(Long usuarioId, Turno.EstadoTurno estado);
    List<Turno> findByUsuarioIdOrderByFechaInicioDesc(Long usuarioId);
    List<Turno> findAllByOrderByFechaInicioDesc();
}
