package cl.comunigest.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RestablecerClaveRequest {

    @NotBlank
    @Size(max = 128)
    private String token;

    @NotBlank
    @Size(max = 255)
    private String nuevaClave;

    @NotBlank
    @Size(max = 255)
    private String confirmacionClave;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNuevaClave() {
        return nuevaClave;
    }

    public void setNuevaClave(String nuevaClave) {
        this.nuevaClave = nuevaClave;
    }

    public String getConfirmacionClave() {
        return confirmacionClave;
    }

    public void setConfirmacionClave(String confirmacionClave) {
        this.confirmacionClave = confirmacionClave;
    }
}
