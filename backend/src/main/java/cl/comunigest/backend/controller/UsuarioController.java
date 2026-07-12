package cl.comunigest.backend.controller;

import cl.comunigest.backend.entity.Usuario;
import cl.comunigest.backend.service.UsuarioService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController extends BaseCrudController<Usuario> {
    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        super(service);
        this.service = service;
    }

    @PatchMapping("/{id}/desactivar")
    public Usuario desactivar(@PathVariable Long id) {
        return service.desactivar(id);
    }

    @PatchMapping("/{id}/reactivar")
    public Usuario reactivar(@PathVariable Long id) {
        return service.reactivar(id);
    }
}
