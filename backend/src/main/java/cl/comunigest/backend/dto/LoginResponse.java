package cl.comunigest.backend.dto;

import cl.comunigest.backend.entity.Usuario;

public class LoginResponse {

    private Long id;
    private String nombre;
    private String email;
    private String perfil;
    private String token;

    public LoginResponse(Usuario usuario) {
        this.id = usuario.getId();
        this.nombre = usuario.getNombre();
        this.email = usuario.getEmail();
        this.perfil = usuario.getPerfil().getNombre();
        this.token = "local-dev-token";
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getPerfil() {
        return perfil;
    }

    public String getToken() {
        return token;
    }
}
