package cl.comunigest.backend.repository;

import cl.comunigest.backend.entity.Encomienda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDateTime;

public interface EncomiendaRepository extends JpaRepository<Encomienda, Long> {
    long countByEstado(Encomienda.EstadoEncomienda estado);
    List<Encomienda> findAllByOrderByFechaRecepcionDesc();
    List<Encomienda> findByEstadoOrderByFechaRecepcionDesc(Encomienda.EstadoEncomienda estado);
    List<Encomienda> findByFechaRecepcionGreaterThanEqualAndFechaRecepcionLessThanOrderByFechaRecepcionDesc(
            LocalDateTime desde, LocalDateTime hasta);
}
