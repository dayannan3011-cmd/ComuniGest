package cl.comunigest.backend.controller;

import cl.comunigest.backend.dto.IngresoVisitaRequest;
import cl.comunigest.backend.dto.SalidaVisitaRequest;
import cl.comunigest.backend.entity.Visita;
import cl.comunigest.backend.service.VisitaService;
import cl.comunigest.backend.security.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public List<Visita> findAll(@AuthenticationPrincipal AuthenticatedUser user) {
        return "ADMINISTRADOR".equalsIgnoreCase(user.perfil()) ? service.findAll() : service.findActivas();
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
    public Visita registrarIngreso(@Valid @RequestBody IngresoVisitaRequest request,
                                   @AuthenticationPrincipal AuthenticatedUser user) {
        return service.registrarIngreso(user.id(), request.getDepartamentoId(),
                request.getNombreVisitante(), request.getDocumento(), request.getPatente());
    }

    @PatchMapping("/{id}/salida")
    public Visita registrarSalida(@PathVariable Long id, @Valid @RequestBody SalidaVisitaRequest request,
                                  @AuthenticationPrincipal AuthenticatedUser user) {
        return service.registrarSalida(id, user.id());
    }
}
