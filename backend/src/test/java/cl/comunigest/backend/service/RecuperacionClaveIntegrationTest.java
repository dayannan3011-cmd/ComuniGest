package cl.comunigest.backend.service;

import cl.comunigest.backend.entity.TokenRecuperacionClave;
import cl.comunigest.backend.entity.Usuario;
import cl.comunigest.backend.repository.TokenRecuperacionClaveRepository;
import cl.comunigest.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:password-recovery;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.h2.console.enabled=false",
        "comunigest.jwt.secret=ComuniGest-password-recovery-test-secret-with-at-least-32-bytes"
})
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class RecuperacionClaveIntegrationTest {

    private static final String EMAIL = "admin@comunigest.local";
    private static final String MENSAJE_GENERICO =
            "Si el correo está registrado, recibirás instrucciones para restablecer tu contraseña";

    @Autowired MockMvc mockMvc;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired TokenRecuperacionClaveRepository tokenRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @MockBean CorreoRecuperacionService correoService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAll();
        usuario = usuarioRepository.findByEmailIgnoreCase(EMAIL).orElseThrow();
        usuario.setPassword(passwordEncoder.encode("admin123"));
        usuario.setActivo(true);
        usuario = usuarioRepository.saveAndFlush(usuario);
        reset(correoService);
    }

    @Test
    void solicitudConCorreoExistenteCreaHashYEnviaTokenSeguro() throws Exception {
        LocalDateTime antes = LocalDateTime.now();

        mockMvc.perform(post("/api/auth/recuperar-clave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + EMAIL + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(MENSAJE_GENERICO));

        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(correoService).enviarEnlaceRecuperacion(
                org.mockito.ArgumentMatchers.eq(EMAIL), anyString(), tokenCaptor.capture());
        String tokenPlano = tokenCaptor.getValue();
        assertThat(tokenPlano).matches("[A-Za-z0-9_-]{43}");

        TokenRecuperacionClave guardado = unicoToken();
        assertThat(guardado.getHashToken()).isEqualTo(RecuperacionClaveService.hash(tokenPlano));
        assertThat(guardado.getHashToken()).doesNotContain(tokenPlano);
        assertThat(guardado.getFechaCreacion()).isAfterOrEqualTo(antes);
        assertThat(Duration.between(guardado.getFechaCreacion(), guardado.getFechaExpiracion()))
                .isEqualTo(Duration.ofMinutes(30));
        assertThat(guardado.getFechaUso()).isNull();
    }

    @Test
    void solicitudConCorreoInexistenteEntregaMismoMensajeSinCrearToken() throws Exception {
        mockMvc.perform(post("/api/auth/recuperar-clave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"inexistente@comunigest.local\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(MENSAJE_GENERICO));

        assertThat(tokenRepository.count()).isZero();
        verify(correoService, never()).enviarEnlaceRecuperacion(anyString(), anyString(), anyString());
    }

    @Test
    void tokenValidoCambiaClaveConBcryptYQuedaUtilizado() throws Exception {
        String token = solicitarToken();

        mockMvc.perform(post("/api/auth/restablecer-clave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(restablecerBody(token, "nueva-clave-segura", "nueva-clave-segura")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("La contraseña fue restablecida correctamente."));

        Usuario actualizado = usuarioRepository.findById(usuario.getId()).orElseThrow();
        assertThat(actualizado.getPassword()).startsWith("$2");
        assertThat(passwordEncoder.matches("nueva-clave-segura", actualizado.getPassword())).isTrue();
        assertThat(unicoToken().getFechaUso()).isNotNull();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + EMAIL + "\",\"password\":\"nueva-clave-segura\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void tokenVencidoEsRechazado() throws Exception {
        String token = solicitarToken();
        TokenRecuperacionClave guardado = unicoToken();
        guardado.setFechaExpiracion(LocalDateTime.now().minusSeconds(1));
        tokenRepository.saveAndFlush(guardado);

        mockMvc.perform(post("/api/auth/restablecer-clave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(restablecerBody(token, "nueva-clave", "nueva-clave")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El token de recuperación ha vencido."));

        assertThat(passwordEncoder.matches("admin123",
                usuarioRepository.findById(usuario.getId()).orElseThrow().getPassword())).isTrue();
        assertThat(unicoToken().getFechaUso()).isNull();
    }

    @Test
    void tokenYaUtilizadoEsRechazado() throws Exception {
        String token = solicitarToken();
        TokenRecuperacionClave guardado = unicoToken();
        guardado.setFechaUso(LocalDateTime.now().minusSeconds(1));
        tokenRepository.saveAndFlush(guardado);

        mockMvc.perform(post("/api/auth/restablecer-clave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(restablecerBody(token, "nueva-clave", "nueva-clave")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El token de recuperación ya fue utilizado."));
    }

    @Test
    void tokenManipuladoEsRechazado() throws Exception {
        String token = solicitarToken();

        mockMvc.perform(post("/api/auth/restablecer-clave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(restablecerBody(token + "A", "nueva-clave", "nueva-clave")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El token de recuperación es inválido."));

        assertThat(unicoToken().getFechaUso()).isNull();
    }

    @Test
    void clavesQueNoCoincidenSonRechazadasSinConsumirToken() throws Exception {
        String token = solicitarToken();

        mockMvc.perform(post("/api/auth/restablecer-clave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(restablecerBody(token, "nueva-clave", "otra-clave")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Las claves no coinciden."));

        assertThat(unicoToken().getFechaUso()).isNull();
    }

    @Test
    void nuevaSolicitudEliminaTokensAnterioresSinUtilizar() throws Exception {
        solicitarSinCapturar();
        solicitarSinCapturar();
        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(correoService, times(2))
                .enviarEnlaceRecuperacion(anyString(), anyString(), tokenCaptor.capture());
        String primerToken = tokenCaptor.getAllValues().get(0);
        String segundoToken = tokenCaptor.getAllValues().get(1);

        assertThat(tokenRepository.findAll()).hasSize(1);
        assertThat(unicoToken().getHashToken()).isEqualTo(RecuperacionClaveService.hash(segundoToken));

        mockMvc.perform(post("/api/auth/restablecer-clave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(restablecerBody(primerToken, "nueva-clave", "nueva-clave")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El token de recuperación es inválido."));

        mockMvc.perform(post("/api/auth/restablecer-clave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(restablecerBody(segundoToken, "nueva-clave", "nueva-clave")))
                .andExpect(status().isOk());
    }

    @Test
    void bearerInvalidoNoBloqueaRutasPublicasDeAutenticacion() throws Exception {
        String bearerInvalido = "Bearer token-invalido";

        mockMvc.perform(post("/api/auth/login")
                        .header("Authorization", bearerInvalido)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + EMAIL + "\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        mockMvc.perform(post("/api/auth/recuperar-clave")
                        .header("Authorization", bearerInvalido)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"inexistente@comunigest.local\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(MENSAJE_GENERICO));

        mockMvc.perform(post("/api/auth/restablecer-clave")
                        .header("Authorization", bearerInvalido)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(restablecerBody("A".repeat(43), "nueva-clave", "nueva-clave")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El token de recuperación es inválido."));
    }

    private String solicitarToken() throws Exception {
        solicitarSinCapturar();
        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(correoService).enviarEnlaceRecuperacion(anyString(), anyString(), tokenCaptor.capture());
        return tokenCaptor.getValue();
    }

    private void solicitarSinCapturar() throws Exception {
        mockMvc.perform(post("/api/auth/recuperar-clave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + EMAIL + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(MENSAJE_GENERICO));
    }

    private TokenRecuperacionClave unicoToken() {
        assertThat(tokenRepository.findAll()).hasSize(1);
        return tokenRepository.findAll().get(0);
    }

    private String restablecerBody(String token, String nuevaClave, String confirmacionClave) {
        return "{\"token\":\"" + token + "\",\"nuevaClave\":\"" + nuevaClave +
                "\",\"confirmacionClave\":\"" + confirmacionClave + "\"}";
    }
}
