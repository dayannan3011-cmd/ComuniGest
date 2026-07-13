package cl.comunigest.backend.controller;

import cl.comunigest.backend.dto.CierreTurnoRequest;
import cl.comunigest.backend.dto.InicioTurnoRequest;
import cl.comunigest.backend.entity.Turno;
import cl.comunigest.backend.service.TurnoService;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/turnos")
public class TurnoController {

    private final TurnoService service;

    public TurnoController(TurnoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Turno> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Turno findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<Turno> listarPorUsuario(@PathVariable Long usuarioId) {
        return service.listarPorUsuario(usuarioId);
    }

    @GetMapping("/abierto/{usuarioId}")
    public Turno obtenerAbierto(@PathVariable Long usuarioId) {
        return service.obtenerAbierto(usuarioId).orElse(null);
    }

    @PostMapping("/iniciar")
    public Turno iniciar(@Valid @RequestBody InicioTurnoRequest request) {
        return service.iniciar(request.getUsuarioId(), request.getObservacionesInicio());
    }

    @PatchMapping("/{id}/cerrar")
    public Turno cerrar(@PathVariable Long id, @Valid @RequestBody CierreTurnoRequest request) {
        return service.cerrar(id, request.getUsuarioId(), request.getObservacionesCierre());
    }
}
