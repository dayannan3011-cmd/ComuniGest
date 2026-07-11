package cl.comunigest.backend.service;

import cl.comunigest.backend.entity.Visita;
import cl.comunigest.backend.repository.VisitaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class VisitaService extends CrudService<Visita> {

    private final VisitaRepository repository;

    public VisitaService(VisitaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public Visita registrarSalida(Long id) {
        Visita visita = findById(id);
        visita.setEstado(Visita.EstadoVisita.SALIDA);
        visita.setFechaSalida(LocalDateTime.now());
        return repository.save(visita);
    }

    @Override
    protected void setId(Visita entity, Long id) {
        entity.setId(id);
    }
}
