package cl.comunigest.backend.service;

import cl.comunigest.backend.entity.Residente;
import cl.comunigest.backend.repository.ResidenteRepository;
import org.springframework.stereotype.Service;

@Service
public class ResidenteService extends CrudService<Residente> {

    public ResidenteService(ResidenteRepository repository) {
        super(repository);
    }

    @Override
    protected void setId(Residente entity, Long id) {
        entity.setId(id);
    }
}
