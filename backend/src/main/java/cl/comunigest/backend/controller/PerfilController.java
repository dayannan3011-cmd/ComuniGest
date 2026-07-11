package cl.comunigest.backend.controller;

import cl.comunigest.backend.entity.Perfil;
import cl.comunigest.backend.service.PerfilService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/perfiles")
public class PerfilController extends BaseCrudController<Perfil> {
    public PerfilController(PerfilService service) {
        super(service);
    }
}
