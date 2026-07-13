package cl.comunigest.backend.security;

import cl.comunigest.backend.entity.Perfil;
import cl.comunigest.backend.entity.Usuario;
import cl.comunigest.backend.repository.PerfilRepository;
import cl.comunigest.backend.repository.IncidenciaRepository;
import cl.comunigest.backend.repository.TurnoRepository;
import cl.comunigest.backend.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:jwt-security;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.h2.console.enabled=false",
        "comunigest.jwt.secret=ComuniGest-integration-test-secret-with-at-least-32-bytes-2026"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    private static final String CONSERJE_EMAIL = "conserje.jwt@comunigest.local";
    private static final String CONSERJE_PASSWORD = "conserje123";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired PerfilRepository perfilRepository;
    @Autowired TurnoRepository turnoRepository;
    @Autowired IncidenciaRepository incidenciaRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;

    private Usuario administrador;
    private Usuario conserje;

    @BeforeEach
    void setUp() {
        incidenciaRepository.deleteAll();
        turnoRepository.deleteAll();
        administrador = usuarioRepository.findByEmailIgnoreCase("admin@comunigest.local").orElseThrow();
        Perfil perfilConserje = perfilRepository.findByNombre("CONSERJE").orElseThrow();
        conserje = usuarioRepository.findByEmailIgnoreCase(CONSERJE_EMAIL).orElseGet(() -> {
            Usuario usuario = new Usuario();
            usuario.setNombre("Conserje JWT");
            usuario.setEmail(CONSERJE_EMAIL);
            usuario.setPassword(passwordEncoder.encode(CONSERJE_PASSWORD));
            usuario.setPerfil(perfilConserje);
            usuario.setActivo(true);
            return usuarioRepository.save(usuario);
        });
        if (!Boolean.TRUE.equals(conserje.getActivo())) {
            conserje.setActivo(true);
            conserje = usuarioRepository.save(conserje);
        }
    }

    @Test
    void loginEntregaJwtFirmadoConClaimsYVencimiento() throws Exception {
        String token = login("admin@comunigest.local", "admin123");

        Claims claims = jwtService.parseAndValidate(token);
        assertThat(claims.getIssuer()).isEqualTo("ComuniGest");
        assertThat(claims.getSubject()).isEqualTo(administrador.getEmail());
        assertThat(((Number) claims.get("userId")).longValue()).isEqualTo(administrador.getId());
        assertThat(claims.get("nombre")).isEqualTo(administrador.getNombre());
        assertThat(claims.get("perfil")).isEqualTo("ADMINISTRADOR");
        assertThat(claims.getExpiration().toInstant())
                .isAfter(claims.getIssuedAt().toInstant().plus(Duration.ofHours(7).plusMinutes(59)));
    }

    @Test
    void loginIncorrectoYRecursosSinTokenSonRechazados() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@comunigest.local\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Correo o clave incorrectos."));

        mockMvc.perform(get("/api/departamentos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void tokenManipuladoYTokenExpiradoSonRechazados() throws Exception {
        String validToken = login(CONSERJE_EMAIL, CONSERJE_PASSWORD);
        String manipulatedToken = validToken.substring(0, validToken.length() - 1)
                + (validToken.endsWith("a") ? "b" : "a");

        mockMvc.perform(get("/api/departamentos").header("Authorization", bearer(manipulatedToken)))
                .andExpect(status().isUnauthorized());

        String expiredToken = jwtService.generateToken(conserje, Instant.now().minus(Duration.ofHours(2)),
                Duration.ofHours(1));
        mockMvc.perform(get("/api/departamentos").header("Authorization", bearer(expiredToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioInactivoPierdeAccesoAunqueSuTokenSigaFirmado() throws Exception {
        String token = login(CONSERJE_EMAIL, CONSERJE_PASSWORD);
        conserje.setActivo(false);
        usuarioRepository.saveAndFlush(conserje);

        mockMvc.perform(get("/api/departamentos").header("Authorization", bearer(token)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void conserjeNoAccedeAModulosAdministrativosYAdminNoIniciaTurno() throws Exception {
        String conserjeToken = login(CONSERJE_EMAIL, CONSERJE_PASSWORD);
        String adminToken = login("admin@comunigest.local", "admin123");

        mockMvc.perform(get("/api/usuarios").header("Authorization", bearer(conserjeToken)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/perfiles").header("Authorization", bearer(conserjeToken)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/reportes/resumen").header("Authorization", bearer(conserjeToken)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/residentes").header("Authorization", bearer(conserjeToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/turnos/iniciar")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void ambosPerfilesAccedenASusConsultasPermitidas() throws Exception {
        String conserjeToken = login(CONSERJE_EMAIL, CONSERJE_PASSWORD);
        String adminToken = login("admin@comunigest.local", "admin123");

        for (String path : new String[]{"/api/departamentos", "/api/turnos", "/api/visitas",
                "/api/encomiendas", "/api/incidencias"}) {
            mockMvc.perform(get(path).header("Authorization", bearer(conserjeToken)))
                    .andExpect(status().isOk());
            mockMvc.perform(get(path).header("Authorization", bearer(adminToken)))
                    .andExpect(status().isOk());
        }

        for (String path : new String[]{"/api/perfiles", "/api/usuarios", "/api/residentes",
                "/api/reportes/resumen"}) {
            mockMvc.perform(get(path).header("Authorization", bearer(adminToken)))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void usuarioIdDelBodyNoPermiteSuplantarAlAdministrador() throws Exception {
        String token = login(CONSERJE_EMAIL, CONSERJE_PASSWORD);

        String response = mockMvc.perform(post("/api/turnos/iniciar")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":" + administrador.getId() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.id").value(conserje.getId()))
                .andReturn().getResponse().getContentAsString();
        long turnoId = objectMapper.readTree(response).path("id").asLong();

        mockMvc.perform(patch("/api/turnos/{id}/cerrar", turnoId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":" + administrador.getId() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.id").value(conserje.getId()))
                .andExpect(jsonPath("$.estado").value("CERRADO"));
    }

    @Test
    void administradorGestionaIncidenciasYConserjeNoPuedeResolverlas() throws Exception {
        String conserjeToken = login(CONSERJE_EMAIL, CONSERJE_PASSWORD);
        String adminToken = login("admin@comunigest.local", "admin123");
        mockMvc.perform(post("/api/turnos/iniciar")
                        .header("Authorization", bearer(conserjeToken))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());

        long incidenciaId = registrarIncidencia(conserjeToken, "Incidencia JWT 1");
        mockMvc.perform(patch("/api/incidencias/{id}/resolver", incidenciaId)
                        .header("Authorization", bearer(conserjeToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resolucion\":\"Intento no autorizado\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/incidencias/{id}/iniciar-gestion", incidenciaId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_PROCESO"));
        mockMvc.perform(patch("/api/incidencias/{id}/resolver", incidenciaId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resolucion\":\"Resuelta por administrador\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RESUELTA"))
                .andExpect(jsonPath("$.usuarioResuelve.id").value(administrador.getId()));
    }

    private long registrarIncidencia(String token, String titulo) throws Exception {
        String body = "{\"titulo\":\"" + titulo + "\",\"descripcion\":\"Detalle de prueba\"," +
                "\"categoria\":\"SEGURIDAD\",\"criticidad\":\"ALTA\"}";
        String response = mockMvc.perform(post("/api/incidencias/registro")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registradaPor.id").value(conserje.getId()))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("id").asLong();
    }

    private String login(String email, String password) throws Exception {
        String body = objectMapper.writeValueAsString(new LoginCredentials(email, password));
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return json.path("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private record LoginCredentials(String email, String password) {}
}
