package cl.comunigest.backend.service;

import cl.comunigest.backend.entity.Turno;
import cl.comunigest.backend.repository.TurnoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TurnoService extends CrudService<Turno> {

    private final TurnoRepository repository;

    public TurnoService(TurnoRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public Turno cerrar(Long id, String observaciones) {
        Turno turno = findById(id);
        turno.setEstado(Turno.EstadoTurno.CERRADO);
        turno.setFechaCierre(LocalDateTime.now());
        if (observaciones != null && !observaciones.isBlank()) {
            turno.setObservaciones(observaciones);
        }
        return repository.save(turno);
    }

    @Override
    protected void setId(Turno entity, Long id) {
        entity.setId(id);
    }
}
