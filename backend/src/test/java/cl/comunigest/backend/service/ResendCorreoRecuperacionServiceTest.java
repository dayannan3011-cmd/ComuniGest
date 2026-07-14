package cl.comunigest.backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ResendCorreoRecuperacionServiceTest {

    @Test
    void construyeYEnviaSolicitudHttpsAResend() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        ResendCorreoRecuperacionService service = new ResendCorreoRecuperacionService(
                builder,
                "re_test_api_key",
                "onboarding@resend.dev",
                "https://frontend.example/");

        server.expect(once(), requestTo("https://api.resend.com/emails"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer re_test_api_key"))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json("""
                        {
                          "from": "onboarding@resend.dev",
                          "to": ["usuario@comunigest.local"],
                          "subject": "Restablecimiento de contraseña de ComuniGest"
                        }
                        """))
                .andExpect(content().string(org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("Hola <strong>Usuario &amp; Prueba</strong>"),
                        org.hamcrest.Matchers.containsString("válido durante 30 minutos"),
                        org.hamcrest.Matchers.containsString(
                                "https://frontend.example/restablecer-clave?token=token_Base64-URL"))))
                .andRespond(withSuccess("{\"id\":\"email-id\"}", MediaType.APPLICATION_JSON));

        service.enviarEnlaceRecuperacion(
                "usuario@comunigest.local", "Usuario & Prueba", "token_Base64-URL");

        server.verify();
    }
}
