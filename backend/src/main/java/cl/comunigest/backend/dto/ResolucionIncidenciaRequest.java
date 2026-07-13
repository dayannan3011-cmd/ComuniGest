package cl.comunigest.backend.dto;

import jakarta.validation.constraints.Size;

public class ResolucionIncidenciaRequest {
    @Size(max = 1000)
    private String resolucion;

    public String getResolucion() { return resolucion; }
    public void setResolucion(String resolucion) { this.resolucion = resolucion; }
}
