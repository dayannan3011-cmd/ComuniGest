package cl.comunigest.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistroIncidenciaRequest {
    @NotBlank
    @Size(max = 140)
    private String titulo;
    @NotBlank
    @Size(max = 1000)
    private String descripcion;
    private String categoria;
    private String criticidad;

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getCriticidad() { return criticidad; }
    public void setCriticidad(String criticidad) { this.criticidad = criticidad; }
}
