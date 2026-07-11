package cl.comunigest.backend.controller;

import cl.comunigest.backend.entity.Incidencia;
import cl.comunigest.backend.service.IncidenciaService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/incidencias")
public class IncidenciaController extends BaseCrudController<Incidencia> {

    private final IncidenciaService service;

    public IncidenciaController(IncidenciaService service) {
        super(service);
        this.service = service;
    }

    @PatchMapping("/{id}/cerrar")
    public Incidencia cerrar(@PathVariable Long id) {
        return service.cerrar(id);
    }
}
