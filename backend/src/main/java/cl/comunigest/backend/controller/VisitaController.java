package cl.comunigest.backend.controller;

import cl.comunigest.backend.entity.Visita;
import cl.comunigest.backend.service.VisitaService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/visitas")
public class VisitaController extends BaseCrudController<Visita> {

    private final VisitaService service;

    public VisitaController(VisitaService service) {
        super(service);
        this.service = service;
    }

    @PatchMapping("/{id}/salida")
    public Visita registrarSalida(@PathVariable Long id) {
        return service.registrarSalida(id);
    }
}
