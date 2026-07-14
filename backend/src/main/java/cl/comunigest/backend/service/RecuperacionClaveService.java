package cl.comunigest.backend.service;

import cl.comunigest.backend.entity.TokenRecuperacionClave;
import cl.comunigest.backend.entity.Usuario;
import cl.comunigest.backend.repository.TokenRecuperacionClaveRepository;
import cl.comunigest.backend.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class RecuperacionClaveService {
    public static final String MENSAJE_SOLICITUD =
            "Si el correo está registrado, recibirás instrucciones para restablecer tu contraseña";

    private static final Logger LOGGER = LoggerFactory.getLogger(RecuperacionClaveService.class);
    private static final int TOKEN_BYTES = 32;
    private static final int MINUTOS_VALIDEZ = 30;

    private final UsuarioRepository usuarioRepository;
    private final TokenRecuperacionClaveRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final CorreoRecuperacionService correoService;
    private final SecureRandom secureRandom = new SecureRandom();

    public RecuperacionClaveService(UsuarioRepository usuarioRepository,
                                    TokenRecuperacionClaveRepository tokenRepository,
                                    PasswordEncoder passwordEncoder,
                                    CorreoRecuperacionService correoService) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.correoService = correoService;
    }

    @Transactional
    public void solicitar(String email) {
        usuarioRepository.findByEmailIgnoreCase(email.trim()).ifPresent(usuario -> {
            String tokenPlano = generarToken();
            LocalDateTime now = LocalDateTime.now();
            TokenRecuperacionClave token = new TokenRecuperacionClave();
            token.setHashToken(hash(tokenPlano));
            token.setUsuario(usuario);
            token.setFechaCreacion(now);
            token.setFechaExpiracion(now.plusMinutes(MINUTOS_VALIDEZ));
            tokenRepository.deleteUnusedByUsuario(usuario);
            tokenRepository.save(token);

            try {
                correoService.enviarEnlaceRecuperacion(usuario.getEmail(), usuario.getNombre(), tokenPlano);
            } catch (RuntimeException exception) {
                LOGGER.error("No fue posible enviar el correo de recuperación solicitado. Tipo: {}",
                        exception.getClass().getSimpleName());
            }
        });
    }

    @Transactional
    public void restablecer(String tokenPlano, String nuevaClave, String confirmacionClave) {
        if (!nuevaClave.equals(confirmacionClave)) {
            throw new IllegalArgumentException("Las claves no coinciden.");
        }

        TokenRecuperacionClave token = tokenRepository.findByHashTokenForUpdate(hash(tokenPlano.trim()))
                .orElseThrow(() -> new IllegalArgumentException("El token de recuperación es inválido."));
        LocalDateTime now = LocalDateTime.now();
        if (token.getFechaUso() != null) {
            throw new IllegalArgumentException("El token de recuperación ya fue utilizado.");
        }
        if (!token.getFechaExpiracion().isAfter(now)) {
            throw new IllegalArgumentException("El token de recuperación ha vencido.");
        }

        Usuario usuario = token.getUsuario();
        usuario.setPassword(passwordEncoder.encode(nuevaClave));
        usuarioRepository.save(usuario);
        token.setFechaUso(now);
        tokenRepository.save(token);
    }

    private String generarToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 no está disponible", exception);
        }
    }
}
