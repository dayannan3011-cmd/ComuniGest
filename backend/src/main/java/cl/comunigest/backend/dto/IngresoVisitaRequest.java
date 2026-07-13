package cl.comunigest.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class IngresoVisitaRequest {

    @NotNull
    private Long departamentoId;

    @NotBlank
    @Size(max = 160)
    private String nombreVisitante;

    @NotBlank
    @Size(max = 40)
    private String documento;

    @Size(max = 20)
    private String patente;

    public Long getDepartamentoId() { return departamentoId; }
    public void setDepartamentoId(Long departamentoId) { this.departamentoId = departamentoId; }
    public String getNombreVisitante() { return nombreVisitante; }
    public void setNombreVisitante(String nombreVisitante) { this.nombreVisitante = nombreVisitante; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getPatente() { return patente; }
    public void setPatente(String patente) { this.patente = patente; }
}
