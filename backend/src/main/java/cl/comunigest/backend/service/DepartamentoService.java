package cl.comunigest.backend.service;

import cl.comunigest.backend.entity.Departamento;
import cl.comunigest.backend.repository.DepartamentoRepository;
import org.springframework.stereotype.Service;

@Service
public class DepartamentoService extends CrudService<Departamento> {

    public DepartamentoService(DepartamentoRepository repository) {
        super(repository);
    }

    @Override
    protected void setId(Departamento entity, Long id) {
        entity.setId(id);
    }
}
