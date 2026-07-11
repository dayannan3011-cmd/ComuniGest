package cl.comunigest.backend.service;

import cl.comunigest.backend.entity.Perfil;
import cl.comunigest.backend.repository.PerfilRepository;
import org.springframework.stereotype.Service;

@Service
public class PerfilService extends CrudService<Perfil> {

    public PerfilService(PerfilRepository repository) {
        super(repository);
    }

    @Override
    protected void setId(Perfil entity, Long id) {
        entity.setId(id);
    }
}
