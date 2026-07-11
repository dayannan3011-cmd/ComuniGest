package cl.comunigest.backend.repository;

import cl.comunigest.backend.entity.Residente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResidenteRepository extends JpaRepository<Residente, Long> {
    long countByActivoTrue();
}
