package cl.comunigest.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
@ConditionalOnProperty(name = "comunigest.mail.provider", havingValue = "resend")
public class ResendCorreoRecuperacionService implements CorreoRecuperacionService {

    private static final String RESEND_API_URL = "https://api.resend.com";
    private static final String SUBJECT = "Restablecimiento de contraseña de ComuniGest";

    private final RestClient restClient;
    private final String remitente;
    private final String frontendUrl;

    public ResendCorreoRecuperacionService(
            RestClient.Builder restClientBuilder,
            @Value("${RESEND_API_KEY}") String apiKey,
            @Value("${COMUNIGEST_RESEND_FROM:onboarding@resend.dev}") String remitente,
            @Value("${comunigest.frontend.url}") String frontendUrl) {
        this.restClient = restClientBuilder
                .baseUrl(RESEND_API_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
        this.remitente = remitente;
        this.frontendUrl = frontendUrl.replaceAll("/+$", "");
    }

    @Override
    public void enviarEnlaceRecuperacion(String destinatario, String nombre, String token) {
        String enlace = frontendUrl + "/restablecer-clave?token=" + token;
        String html = construirHtml(nombre, enlace);
        ResendEmailRequest request = new ResendEmailRequest(
                remitente, List.of(destinatario), SUBJECT, html);

        restClient.post()
                .uri("/emails")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    private String construirHtml(String nombre, String enlace) {
        String nombreSeguro = HtmlUtils.htmlEscape(nombre);
        String enlaceSeguro = HtmlUtils.htmlEscape(enlace);
        return "<p>Hola <strong>" + nombreSeguro + "</strong>,</p>" +
                "<p>Recibimos una solicitud para restablecer tu contraseña de ComuniGest.</p>" +
                "<p>Este enlace es válido durante 30 minutos y solo puede utilizarse una vez.</p>" +
                "<p><a href=\"" + enlaceSeguro + "\">Restablecer contraseña</a></p>" +
                "<p>Si no realizaste esta solicitud, puedes ignorar este mensaje.</p>";
    }

    private record ResendEmailRequest(String from, List<String> to, String subject, String html) {
    }
}
