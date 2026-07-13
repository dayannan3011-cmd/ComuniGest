package cl.comunigest.backend.dto;

import jakarta.validation.constraints.NotNull;

public class SalidaVisitaRequest {

    @NotNull
    private Long usuarioId;

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }
}
