package cl.comunigest.backend.dto;

public class ReporteResumenResponse {

    private long residentesActivos;
    private long turnosAbiertos;
    private long visitasDentro;
    private long encomiendasPendientes;
    private long incidenciasAbiertas;

    public ReporteResumenResponse(long residentesActivos, long turnosAbiertos, long visitasDentro,
                                  long encomiendasPendientes, long incidenciasAbiertas) {
        this.residentesActivos = residentesActivos;
        this.turnosAbiertos = turnosAbiertos;
        this.visitasDentro = visitasDentro;
        this.encomiendasPendientes = encomiendasPendientes;
        this.incidenciasAbiertas = incidenciasAbiertas;
    }

    public long getResidentesActivos() {
        return residentesActivos;
    }

    public long getTurnosAbiertos() {
        return turnosAbiertos;
    }

    public long getVisitasDentro() {
        return visitasDentro;
    }

    public long getEncomiendasPendientes() {
        return encomiendasPendientes;
    }

    public long getIncidenciasAbiertas() {
        return incidenciasAbiertas;
    }
}
