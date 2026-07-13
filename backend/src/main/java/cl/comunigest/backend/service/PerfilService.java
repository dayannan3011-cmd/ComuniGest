package cl.comunigest.backend.service;

import cl.comunigest.backend.entity.Perfil;
import cl.comunigest.backend.repository.PerfilRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PerfilService {

    private final PerfilRepository repository;

    public PerfilService(PerfilRepository repository) {
        this.repository = repository;
    }

    public List<Perfil> findCatalogo() {
        return List.of(
                requireByNombre("ADMINISTRADOR"),
                requireByNombre("CONSERJE")
        );
    }

    public Perfil findById(Long id) {
        Perfil perfil = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("El perfil no existe."));
        if (!"ADMINISTRADOR".equals(perfil.getNombre()) && !"CONSERJE".equals(perfil.getNombre())) {
            throw new EntityNotFoundException("El perfil no pertenece al catálogo del sistema.");
        }
        return perfil;
    }

    private Perfil requireByNombre(String nombre) {
        return repository.findByNombre(nombre)
                .orElseThrow(() -> new EntityNotFoundException("Falta el perfil requerido " + nombre + "."));
    }
}
