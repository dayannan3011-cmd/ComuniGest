package cl.comunigest.backend.controller;

import cl.comunigest.backend.dto.EntregaEncomiendaRequest;
import cl.comunigest.backend.dto.RecepcionEncomiendaRequest;
import cl.comunigest.backend.entity.Encomienda;
import cl.comunigest.backend.service.EncomiendaService;
import cl.comunigest.backend.security.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/encomiendas")
public class EncomiendaController {

    private final EncomiendaService service;

    public EncomiendaController(EncomiendaService service) {
        this.service = service;
    }

    @GetMapping
    public java.util.List<Encomienda> historial(@AuthenticationPrincipal AuthenticatedUser user) {
        return "ADMINISTRADOR".equalsIgnoreCase(user.perfil()) ? service.findAll() : service.findMesActual();
    }

    @GetMapping("/pendientes")
    public java.util.List<Encomienda> pendientes() { return service.findPendientes(); }

    @GetMapping("/mes-actual")
    public java.util.List<Encomienda> mesActual() { return service.findMesActual(); }

    @PostMapping("/recepcion")
    public Encomienda registrarRecepcion(@Valid @RequestBody RecepcionEncomiendaRequest request,
                                         @AuthenticationPrincipal AuthenticatedUser user) {
        return service.registrarRecepcion(user.id(), request.getDepartamentoId(),
                request.getDestinatario(), request.getDescripcion(), request.getEmpresaRepartidor());
    }

    @PatchMapping("/{id}/entregar")
    public Encomienda entregar(@PathVariable Long id, @Valid @RequestBody EntregaEncomiendaRequest request,
                               @AuthenticationPrincipal AuthenticatedUser user) {
        return service.entregar(id, user.id(), request.getEntregadoA());
    }
}
