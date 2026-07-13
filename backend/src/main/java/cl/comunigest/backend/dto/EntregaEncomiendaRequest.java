package cl.comunigest.backend.dto;

import jakarta.validation.constraints.Size;

public class EntregaEncomiendaRequest {

    @jakarta.validation.constraints.NotNull
    private Long usuarioId;

    @Size(max = 140)
    private String entregadoA;

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getEntregadoA() {
        return entregadoA;
    }

    public void setEntregadoA(String entregadoA) {
        this.entregadoA = entregadoA;
    }
}
