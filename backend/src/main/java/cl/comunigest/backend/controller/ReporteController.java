package cl.comunigest.backend.controller;

import cl.comunigest.backend.dto.ReporteResumenResponse;
import cl.comunigest.backend.entity.Encomienda;
import cl.comunigest.backend.entity.Incidencia;
import cl.comunigest.backend.entity.Turno;
import cl.comunigest.backend.entity.Visita;
import cl.comunigest.backend.repository.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ResidenteRepository residenteRepository;
    private final TurnoRepository turnoRepository;
    private final VisitaRepository visitaRepository;
    private final EncomiendaRepository encomiendaRepository;
    private final IncidenciaRepository incidenciaRepository;

    public ReporteController(ResidenteRepository residenteRepository, TurnoRepository turnoRepository,
                             VisitaRepository visitaRepository, EncomiendaRepository encomiendaRepository,
                             IncidenciaRepository incidenciaRepository) {
        this.residenteRepository = residenteRepository;
        this.turnoRepository = turnoRepository;
        this.visitaRepository = visitaRepository;
        this.encomiendaRepository = encomiendaRepository;
        this.incidenciaRepository = incidenciaRepository;
    }

    @GetMapping("/resumen")
    public ReporteResumenResponse resumen() {
        return new ReporteResumenResponse(
                residenteRepository.countByActivoTrue(),
                turnoRepository.countByEstado(Turno.EstadoTurno.ABIERTO),
                visitaRepository.countByEstado(Visita.EstadoVisita.INGRESADA),
                encomiendaRepository.countByEstado(Encomienda.EstadoEncomienda.PENDIENTE),
                incidenciaRepository.countByEstado(Incidencia.EstadoIncidencia.ABIERTA)
        );
    }
}
