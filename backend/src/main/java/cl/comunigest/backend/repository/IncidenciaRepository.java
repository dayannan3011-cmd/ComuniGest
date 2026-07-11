package cl.comunigest.backend.repository;

import cl.comunigest.backend.entity.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {
    long countByEstado(Incidencia.EstadoIncidencia estado);
}
