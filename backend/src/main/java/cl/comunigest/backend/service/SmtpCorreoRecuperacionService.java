package cl.comunigest.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class SmtpCorreoRecuperacionService implements CorreoRecuperacionService {

    private final JavaMailSender mailSender;
    private final String remitente;
    private final String frontendUrl;

    public SmtpCorreoRecuperacionService(JavaMailSender mailSender,
                                         @Value("${comunigest.mail.from}") String remitente,
                                         @Value("${comunigest.frontend.url}") String frontendUrl) {
        this.mailSender = mailSender;
        this.remitente = remitente;
        this.frontendUrl = frontendUrl.replaceAll("/+$", "");
    }

    @Override
    public void enviarEnlaceRecuperacion(String destinatario, String nombre, String token) {
        String enlace = frontendUrl + "/restablecer-clave?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(remitente);
        message.setTo(destinatario);
        message.setSubject("Restablecimiento de contraseña de ComuniGest");
        message.setText("Hola " + nombre + ",\n\n" +
                "Recibimos una solicitud para restablecer tu contraseña de ComuniGest. " +
                "El enlace es válido durante 30 minutos y solo puede utilizarse una vez:\n\n" +
                enlace + "\n\n" +
                "Si no realizaste esta solicitud, puedes ignorar este mensaje.");
        mailSender.send(message);
    }
}
