package cl.comunigest.backend.controller;

import cl.comunigest.backend.dto.IngresoVisitaRequest;
import cl.comunigest.backend.dto.SalidaVisitaRequest;
import cl.comunigest.backend.entity.Visita;
import cl.comunigest.backend.service.VisitaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visitas")
public class VisitaController {

    private final VisitaService service;

    public VisitaController(VisitaService service) {
        this.service = service;
    }

    @GetMapping
    public List<Visita> findAll() {
        return service.findAll();
    }

    @GetMapping("/activas")
    public List<Visita> findActivas() {
        return service.findActivas();
    }

    @GetMapping("/{id}")
    public Visita findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping("/ingreso")
    public Visita registrarIngreso(@Valid @RequestBody IngresoVisitaRequest request) {
        return service.registrarIngreso(request.getUsuarioId(), request.getDepartamentoId(),
                request.getNombreVisitante(), request.getDocumento(), request.getPatente());
    }

    @PatchMapping("/{id}/salida")
    public Visita registrarSalida(@PathVariable Long id, @Valid @RequestBody SalidaVisitaRequest request) {
        return service.registrarSalida(id, request.getUsuarioId());
    }
}
