package cl.comunigest.backend.controller;

import cl.comunigest.backend.dto.EntregaEncomiendaRequest;
import cl.comunigest.backend.dto.RecepcionEncomiendaRequest;
import cl.comunigest.backend.entity.Encomienda;
import cl.comunigest.backend.service.EncomiendaService;
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
    public java.util.List<Encomienda> historial() { return service.findAll(); }

    @GetMapping("/pendientes")
    public java.util.List<Encomienda> pendientes() { return service.findPendientes(); }

    @GetMapping("/mes-actual")
    public java.util.List<Encomienda> mesActual() { return service.findMesActual(); }

    @PostMapping("/recepcion")
    public Encomienda registrarRecepcion(@Valid @RequestBody RecepcionEncomiendaRequest request) {
        return service.registrarRecepcion(request.getUsuarioId(), request.getDepartamentoId(),
                request.getDestinatario(), request.getDescripcion(), request.getEmpresaRepartidor());
    }

    @PatchMapping("/{id}/entregar")
    public Encomienda entregar(@PathVariable Long id, @Valid @RequestBody EntregaEncomiendaRequest request) {
        return service.entregar(id, request.getUsuarioId(), request.getEntregadoA());
    }
}
