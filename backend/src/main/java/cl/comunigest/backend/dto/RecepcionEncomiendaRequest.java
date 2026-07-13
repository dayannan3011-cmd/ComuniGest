package cl.comunigest.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RecepcionEncomiendaRequest {

    @NotNull
    private Long departamentoId;

    @NotBlank(message = "Debes indicar el destinatario de la encomienda.")
    @Size(max = 160)
    private String destinatario;

    @NotBlank(message = "Debes indicar la descripción de la encomienda.")
    @Size(max = 255)
    private String descripcion;

    @Size(max = 160)
    private String empresaRepartidor;

    public Long getDepartamentoId() { return departamentoId; }
    public void setDepartamentoId(Long departamentoId) { this.departamentoId = departamentoId; }
    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getEmpresaRepartidor() { return empresaRepartidor; }
    public void setEmpresaRepartidor(String empresaRepartidor) { this.empresaRepartidor = empresaRepartidor; }
}
