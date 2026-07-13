package cl.comunigest.backend.dto;

import jakarta.validation.constraints.Size;

public class InicioTurnoRequest {

    @Size(max = 500)
    private String observacionesInicio;

    public String getObservacionesInicio() {
        return observacionesInicio;
    }

    public void setObservacionesInicio(String observacionesInicio) {
        this.observacionesInicio = observacionesInicio;
    }
}
