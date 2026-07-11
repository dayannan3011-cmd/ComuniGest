package cl.comunigest.backend.service;

import cl.comunigest.backend.entity.Usuario;
import cl.comunigest.backend.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService extends CrudService<Usuario> {

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public Usuario autenticar(String email, String password) {
        Usuario usuario = repository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario o clave invalidos"));
        if (!Boolean.TRUE.equals(usuario.getActivo()) || !usuario.getPassword().equals(password)) {
            throw new EntityNotFoundException("Usuario o clave invalidos");
        }
        return usuario;
    }

    @Override
    protected void setId(Usuario entity, Long id) {
        entity.setId(id);
    }
}
