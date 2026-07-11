package cl.comunigest.backend.controller;

import cl.comunigest.backend.entity.Usuario;
import cl.comunigest.backend.service.UsuarioService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController extends BaseCrudController<Usuario> {
    public UsuarioController(UsuarioService service) {
        super(service);
    }
}
