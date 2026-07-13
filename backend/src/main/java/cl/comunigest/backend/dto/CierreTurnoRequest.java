package cl.comunigest.backend.dto;

import jakarta.validation.constraints.Size;

public class CierreTurnoRequest {

    @Size(max = 500)
    private String observacionesCierre;

    public String getObservacionesCierre() {
        return observacionesCierre;
    }

    public void setObservacionesCierre(String observacionesCierre) {
        this.observacionesCierre = observacionesCierre;
    }
}
