package cl.comunigest.backend.repository;

import cl.comunigest.backend.entity.Visita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VisitaRepository extends JpaRepository<Visita, Long> {
    long countByEstado(Visita.EstadoVisita estado);
    List<Visita> findAllByOrderByFechaIngresoDesc();
    List<Visita> findByEstadoOrderByFechaIngresoDesc(Visita.EstadoVisita estado);
}
