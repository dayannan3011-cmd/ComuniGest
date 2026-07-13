package cl.comunigest.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Entity
@Table(name = "incidencias")
public class Incidencia {

    public enum EstadoIncidencia {
        ABIERTA, EN_PROCESO, RESUELTA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 140)
    private String titulo;

    @NotBlank
    @Column(nullable = false, length = 1000)
    private String descripcion;

    @Column(length = 80)
    private String categoria;

    @Column(nullable = false, length = 40)
    private String criticidad = "MEDIA";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "registrada_por_id")
    private Usuario registradaPor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "turno_registro_id")
    private Turno turnoRegistro;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    private LocalDateTime fechaResolucion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_resuelve_id")
    private Usuario usuarioResuelve;

    @Column(length = 1000)
    private String resolucion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoIncidencia estado = EstadoIncidencia.ABIERTA;

    @Column(nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(nullable = false)
    private LocalDateTime actualizadoEn;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        creadoEn = now;
        actualizadoEn = now;
    }

    @PreUpdate
    protected void onUpdate() { actualizadoEn = LocalDateTime.now(); }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getCriticidad() {
        return criticidad;
    }

    public void setCriticidad(String criticidad) {
        this.criticidad = criticidad;
    }

    public Usuario getRegistradaPor() {
        return registradaPor;
    }

    public void setRegistradaPor(Usuario registradaPor) {
        this.registradaPor = registradaPor;
    }

    public Turno getTurnoRegistro() { return turnoRegistro; }
    public void setTurnoRegistro(Turno turnoRegistro) { this.turnoRegistro = turnoRegistro; }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public LocalDateTime getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(LocalDateTime fechaResolucion) { this.fechaResolucion = fechaResolucion; }
    public Usuario getUsuarioResuelve() { return usuarioResuelve; }
    public void setUsuarioResuelve(Usuario usuarioResuelve) { this.usuarioResuelve = usuarioResuelve; }
    public String getResolucion() { return resolucion; }
    public void setResolucion(String resolucion) { this.resolucion = resolucion; }

    public EstadoIncidencia getEstado() {
        return estado;
    }

    public void setEstado(EstadoIncidencia estado) {
        this.estado = estado;
    }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
}
