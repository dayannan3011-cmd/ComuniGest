package cl.comunigest.backend.controller;

import cl.comunigest.backend.dto.EntregaEncomiendaRequest;
import cl.comunigest.backend.entity.Encomienda;
import cl.comunigest.backend.service.EncomiendaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/encomiendas")
public class EncomiendaController extends BaseCrudController<Encomienda> {

    private final EncomiendaService service;

    public EncomiendaController(EncomiendaService service) {
        super(service);
        this.service = service;
    }

    @PatchMapping("/{id}/entregar")
    public Encomienda entregar(@PathVariable Long id, @Valid @RequestBody EntregaEncomiendaRequest request) {
        return service.entregar(id, request.getEntregadoA());
    }
}
