package cl.comunigest.backend.controller;

import cl.comunigest.backend.entity.Departamento;
import cl.comunigest.backend.service.DepartamentoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/departamentos")
public class DepartamentoController extends BaseCrudController<Departamento> {
    public DepartamentoController(DepartamentoService service) {
        super(service);
    }
}
