package cl.comunigest.backend.repository;

import cl.comunigest.backend.entity.Encomienda;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EncomiendaRepository extends JpaRepository<Encomienda, Long> {
    long countByEstado(Encomienda.EstadoEncomienda estado);
}
