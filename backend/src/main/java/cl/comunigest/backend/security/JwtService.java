package cl.comunigest.backend.security;

import cl.comunigest.backend.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key;
    private final String issuer;
    private final Duration expiration;

    public JwtService(@Value("${comunigest.jwt.secret}") String secret,
                      @Value("${comunigest.jwt.issuer:ComuniGest}") String issuer,
                      @Value("${comunigest.jwt.expiration-hours:8}") long expirationHours) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.expiration = Duration.ofHours(expirationHours);
    }

    public String generateToken(Usuario usuario) {
        return generateToken(usuario, Instant.now(), expiration);
    }

    public String generateToken(Usuario usuario, Instant issuedAt, Duration validity) {
        Instant expiresAt = issuedAt.plus(validity);
        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("userId", usuario.getId())
                .claim("nombre", usuario.getNombre())
                .claim("perfil", usuario.getPerfil().getNombre())
                .issuer(issuer)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
    }

    public Claims parseAndValidate(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
