package cl.comunigest.backend.dto;

import jakarta.validation.constraints.Size;

public class EntregaEncomiendaRequest {

    @Size(max = 140)
    private String entregadoA;

    public String getEntregadoA() {
        return entregadoA;
    }

    public void setEntregadoA(String entregadoA) {
        this.entregadoA = entregadoA;
    }
}
