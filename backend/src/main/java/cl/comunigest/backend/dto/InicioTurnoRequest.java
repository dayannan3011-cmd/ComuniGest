package cl.comunigest.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class InicioTurnoRequest {

    @NotNull
    private Long usuarioId;

    @Size(max = 500)
    private String observacionesInicio;

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getObservacionesInicio() {
        return observacionesInicio;
    }

    public void setObservacionesInicio(String observacionesInicio) {
        this.observacionesInicio = observacionesInicio;
    }
}
