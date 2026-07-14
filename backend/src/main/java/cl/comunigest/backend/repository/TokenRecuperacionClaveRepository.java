package cl.comunigest.backend.repository;

import cl.comunigest.backend.entity.TokenRecuperacionClave;
import cl.comunigest.backend.entity.Usuario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TokenRecuperacionClaveRepository extends JpaRepository<TokenRecuperacionClave, Long> {

    @Modifying
    @Query("delete from TokenRecuperacionClave token " +
            "where token.usuario = :usuario and token.fechaUso is null")
    int deleteUnusedByUsuario(@Param("usuario") Usuario usuario);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select token from TokenRecuperacionClave token join fetch token.usuario " +
            "where token.hashToken = :hashToken")
    Optional<TokenRecuperacionClave> findByHashTokenForUpdate(@Param("hashToken") String hashToken);
}
