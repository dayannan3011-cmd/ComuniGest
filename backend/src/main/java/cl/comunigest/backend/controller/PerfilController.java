package cl.comunigest.backend.controller;

import cl.comunigest.backend.entity.Perfil;
import cl.comunigest.backend.service.PerfilService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/perfiles")
public class PerfilController {
    private final PerfilService service;

    public PerfilController(PerfilService service) {
        this.service = service;
    }

    @GetMapping
    public List<Perfil> findAll() { return service.findCatalogo(); }

    @GetMapping("/{id}")
    public Perfil findById(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    public void rejectCreate() { throw readOnly(); }

    @PutMapping("/{id}")
    public void rejectUpdate(@PathVariable Long id) { throw readOnly(); }

    @DeleteMapping("/{id}")
    public void rejectDelete(@PathVariable Long id) { throw readOnly(); }

    private ResponseStatusException readOnly() {
        return new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                "Los perfiles son un catálogo de solo lectura.");
    }
}
