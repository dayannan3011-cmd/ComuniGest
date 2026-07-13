package cl.comunigest.backend.security;

public record AuthenticatedUser(Long id, String email, String nombre, String perfil) {
}
