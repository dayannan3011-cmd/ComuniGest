package cl.comunigest.backend.controller;

import cl.comunigest.backend.dto.GestionIncidenciaRequest;
import cl.comunigest.backend.dto.RegistroIncidenciaRequest;
import cl.comunigest.backend.dto.ResolucionIncidenciaRequest;
import cl.comunigest.backend.entity.Incidencia;
import cl.comunigest.backend.service.IncidenciaService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/incidencias")
public class IncidenciaController {
    private static final Logger LOGGER = LoggerFactory.getLogger(IncidenciaController.class);
    private final IncidenciaService service;

    public IncidenciaController(IncidenciaService service) { this.service = service; }

    @GetMapping
    public List<Incidencia> historial() { return service.findAll(); }

    @GetMapping("/mes-actual")
    public List<Incidencia> mesActual() { return service.findMesActual(); }

    @PostMapping("/registro")
    public Incidencia registrar(@Valid @RequestBody RegistroIncidenciaRequest request) {
        return service.registrar(request.getUsuarioId(), request.getTitulo(), request.getDescripcion(),
                request.getCategoria(), request.getCriticidad());
    }

    @PatchMapping("/{id}/iniciar-gestion")
    public Incidencia iniciarGestion(@PathVariable Long id,
                                     @Valid @RequestBody GestionIncidenciaRequest request) {
        return service.iniciarGestion(id, request.getUsuarioId());
    }

    @PatchMapping("/{id}/resolver")
    public Incidencia resolver(@PathVariable Long id,
                               @Valid @RequestBody ResolucionIncidenciaRequest request) {
        return service.resolver(id, request.getUsuarioId(), request.getResolucion());
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, Object>> databaseError(DataAccessException exception) {
        LOGGER.error("Error de base de datos al gestionar una incidencia", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "message", "No se pudo completar la operación. Intenta nuevamente."
        ));
    }
}
