package cl.comunigest.backend.repository;

import cl.comunigest.backend.entity.Visita;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitaRepository extends JpaRepository<Visita, Long> {
    long countByEstado(Visita.EstadoVisita estado);
}
