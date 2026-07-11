package cl.comunigest.backend.repository;

import cl.comunigest.backend.entity.Turno;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TurnoRepository extends JpaRepository<Turno, Long> {
    long countByEstado(Turno.EstadoTurno estado);
}
