package cl.comunigest.backend.service;

import cl.comunigest.backend.entity.Incidencia;
import cl.comunigest.backend.repository.IncidenciaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class IncidenciaService extends CrudService<Incidencia> {

    private final IncidenciaRepository repository;

    public IncidenciaService(IncidenciaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public Incidencia cerrar(Long id) {
        Incidencia incidencia = findById(id);
        incidencia.setEstado(Incidencia.EstadoIncidencia.CERRADA);
        incidencia.setFechaCierre(LocalDateTime.now());
        return repository.save(incidencia);
    }

    @Override
    protected void setId(Incidencia entity, Long id) {
        entity.setId(id);
    }
}
