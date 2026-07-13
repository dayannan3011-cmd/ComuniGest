package cl.comunigest.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RegistroIncidenciaRequest {
    @NotNull
    private Long usuarioId;
    @NotBlank
    @Size(max = 140)
    private String titulo;
    @NotBlank
    @Size(max = 1000)
    private String descripcion;
    private String categoria;
    private String criticidad;

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getCriticidad() { return criticidad; }
    public void setCriticidad(String criticidad) { this.criticidad = criticidad; }
}
