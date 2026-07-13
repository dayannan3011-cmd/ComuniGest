package cl.comunigest.backend.repository;

import cl.comunigest.backend.entity.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {
    long countByEstado(Incidencia.EstadoIncidencia estado);
    List<Incidencia> findAllByOrderByFechaRegistroDesc();
    @Query("""
            select i from Incidencia i
            where i.estado in (:abierta, :enProceso)
               or (i.estado = :resuelta and i.fechaResolucion >= :desde and i.fechaResolucion < :hasta)
            order by i.fechaRegistro desc
            """)
    List<Incidencia> findParaConserje(
            @Param("abierta") Incidencia.EstadoIncidencia abierta,
            @Param("enProceso") Incidencia.EstadoIncidencia enProceso,
            @Param("resuelta") Incidencia.EstadoIncidencia resuelta,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Incidencia i where i.id = :id")
    Optional<Incidencia> findByIdForUpdate(@Param("id") Long id);
}
