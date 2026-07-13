package cl.comunigest.backend.controller;

import cl.comunigest.backend.dto.CierreTurnoRequest;
import cl.comunigest.backend.dto.InicioTurnoRequest;
import cl.comunigest.backend.entity.Turno;
import cl.comunigest.backend.service.TurnoService;
import cl.comunigest.backend.security.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
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
    public List<Turno> findAll(@AuthenticationPrincipal AuthenticatedUser user) {
        return isAdmin(user) ? service.findAll() : service.listarPorUsuario(user.id());
    }

    @GetMapping("/{id}")
    public Turno findById(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser user) {
        Turno turno = service.findById(id);
        requireOwnOrAdmin(user, turno.getUsuario().getId());
        return turno;
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<Turno> listarPorUsuario(@PathVariable Long usuarioId,
                                        @AuthenticationPrincipal AuthenticatedUser user) {
        requireOwnOrAdmin(user, usuarioId);
        return service.listarPorUsuario(usuarioId);
    }

    @GetMapping("/abierto/{usuarioId}")
    public Turno obtenerAbierto(@PathVariable Long usuarioId,
                                @AuthenticationPrincipal AuthenticatedUser user) {
        requireOwnOrAdmin(user, usuarioId);
        return service.obtenerAbierto(usuarioId).orElse(null);
    }

    @PostMapping("/iniciar")
    public Turno iniciar(@Valid @RequestBody InicioTurnoRequest request,
                         @AuthenticationPrincipal AuthenticatedUser user) {
        return service.iniciar(user.id(), request.getObservacionesInicio());
    }

    @PatchMapping("/{id}/cerrar")
    public Turno cerrar(@PathVariable Long id, @Valid @RequestBody CierreTurnoRequest request,
                        @AuthenticationPrincipal AuthenticatedUser user) {
        return service.cerrar(id, user.id(), request.getObservacionesCierre());
    }

    private boolean isAdmin(AuthenticatedUser user) {
        return "ADMINISTRADOR".equalsIgnoreCase(user.perfil());
    }

    private void requireOwnOrAdmin(AuthenticatedUser user, Long usuarioId) {
        if (!isAdmin(user) && !user.id().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tienes permisos para consultar turnos de otro usuario.");
        }
    }
}
