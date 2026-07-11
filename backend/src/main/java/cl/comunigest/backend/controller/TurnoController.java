package cl.comunigest.backend.controller;

import cl.comunigest.backend.dto.CierreTurnoRequest;
import cl.comunigest.backend.entity.Turno;
import cl.comunigest.backend.service.TurnoService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/turnos")
public class TurnoController extends BaseCrudController<Turno> {

    private final TurnoService service;

    public TurnoController(TurnoService service) {
        super(service);
        this.service = service;
    }

    @PatchMapping("/{id}/cerrar")
    public Turno cerrar(@PathVariable Long id, @RequestBody(required = false) CierreTurnoRequest request) {
        return service.cerrar(id, request == null ? null : request.getObservaciones());
    }
}
