package cl.comunigest.backend.service;

import cl.comunigest.backend.entity.Encomienda;
import cl.comunigest.backend.repository.EncomiendaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EncomiendaService extends CrudService<Encomienda> {

    private final EncomiendaRepository repository;

    public EncomiendaService(EncomiendaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public Encomienda entregar(Long id, String entregadoA) {
        Encomienda encomienda = findById(id);
        encomienda.setEstado(Encomienda.EstadoEncomienda.ENTREGADA);
        encomienda.setEntregadoA(entregadoA);
        encomienda.setFechaEntrega(LocalDateTime.now());
        return repository.save(encomienda);
    }

    @Override
    protected void setId(Encomienda entity, Long id) {
        entity.setId(id);
    }
}
