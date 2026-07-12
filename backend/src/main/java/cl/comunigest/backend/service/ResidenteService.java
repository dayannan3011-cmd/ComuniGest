package cl.comunigest.backend.service;

import cl.comunigest.backend.entity.Residente;
import cl.comunigest.backend.repository.DepartamentoRepository;
import cl.comunigest.backend.repository.ResidenteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ResidenteService extends CrudService<Residente> {

    private final DepartamentoRepository departamentoRepository;

    public ResidenteService(ResidenteRepository repository, DepartamentoRepository departamentoRepository) {
        super(repository);
        this.departamentoRepository = departamentoRepository;
    }

    @Override
    public Residente save(Residente entity) {
        validateDepartamento(entity);
        return super.save(entity);
    }

    @Override
    public Residente update(Long id, Residente entity) {
        validateDepartamento(entity);
        return super.update(id, entity);
    }

    @Override
    protected void setId(Residente entity, Long id) {
        entity.setId(id);
    }

    private void validateDepartamento(Residente residente) {
        Long departamentoId = residente.getDepartamento() == null
                ? null
                : residente.getDepartamento().getId();
        if (departamentoId == null || !departamentoRepository.existsById(departamentoId)) {
            throw new EntityNotFoundException("El departamento seleccionado no existe");
        }
    }
}
