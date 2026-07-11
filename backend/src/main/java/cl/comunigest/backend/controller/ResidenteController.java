package cl.comunigest.backend.controller;

import cl.comunigest.backend.entity.Residente;
import cl.comunigest.backend.service.ResidenteService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/residentes")
public class ResidenteController extends BaseCrudController<Residente> {
    public ResidenteController(ResidenteService service) {
        super(service);
    }
}
