package cl.comunigest.backend.service;

public interface CorreoRecuperacionService {
    void enviarEnlaceRecuperacion(String destinatario, String nombre, String token);
}
