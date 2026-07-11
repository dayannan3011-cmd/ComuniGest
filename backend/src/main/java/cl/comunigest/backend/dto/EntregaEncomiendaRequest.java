package cl.comunigest.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class EntregaEncomiendaRequest {

    @NotBlank
    private String entregadoA;

    public String getEntregadoA() {
        return entregadoA;
    }

    public void setEntregadoA(String entregadoA) {
        this.entregadoA = entregadoA;
    }
}
