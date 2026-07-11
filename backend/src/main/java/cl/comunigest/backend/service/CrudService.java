package cl.comunigest.backend.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public abstract class CrudService<T> {

    private final JpaRepository<T, Long> repository;

    protected CrudService(JpaRepository<T, Long> repository) {
        this.repository = repository;
    }

    public List<T> findAll() {
        return repository.findAll();
    }

    public T findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Registro no encontrado: " + id));
    }

    public T save(T entity) {
        return repository.save(entity);
    }

    public T update(Long id, T entity) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Registro no encontrado: " + id);
        }
        setId(entity, id);
        return repository.save(entity);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Registro no encontrado: " + id);
        }
        repository.deleteById(id);
    }

    protected abstract void setId(T entity, Long id);
}
