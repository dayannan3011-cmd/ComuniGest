package cl.comunigest.backend.service;

import cl.comunigest.backend.entity.Usuario;
import cl.comunigest.backend.entity.Perfil;
import cl.comunigest.backend.repository.PerfilRepository;
import cl.comunigest.backend.repository.UsuarioRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService extends CrudService<Usuario> {

    private final UsuarioRepository repository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository, PerfilRepository perfilRepository,
                          PasswordEncoder passwordEncoder) {
        super(repository);
        this.repository = repository;
        this.perfilRepository = perfilRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario autenticar(String email, String password) {
        Usuario usuario = repository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Correo o clave incorrectos."));
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El usuario se encuentra inactivo.");
        }
        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o clave incorrectos.");
        }
        return usuario;
    }

    @Override
    public Usuario save(Usuario entity) {
        if (entity.getPassword() == null || entity.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña inicial es obligatoria.");
        }
        prepareCommonData(entity, null);
        entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        entity.setActivo(true);
        return repository.save(entity);
    }

    @Override
    public Usuario update(Long id, Usuario changes) {
        Usuario current = findById(id);
        prepareCommonData(changes, id);
        current.setNombre(changes.getNombre());
        current.setEmail(changes.getEmail());
        current.setPerfil(changes.getPerfil());
        if (changes.getPassword() != null && !changes.getPassword().isBlank()) {
            current.setPassword(passwordEncoder.encode(changes.getPassword()));
        }
        return repository.save(current);
    }

    @Transactional
    public Usuario desactivar(Long id) {
        Usuario usuario = findById(id);
        if (Boolean.TRUE.equals(usuario.getActivo())
                && "ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil().getNombre())
                && repository.countByPerfilNombreAndActivoTrue("ADMINISTRADOR") <= 1) {
            throw new EntityExistsException("No se puede desactivar al último ADMINISTRADOR activo.");
        }
        usuario.setActivo(false);
        return repository.save(usuario);
    }

    public Usuario reactivar(Long id) {
        Usuario usuario = findById(id);
        usuario.setActivo(true);
        return repository.save(usuario);
    }

    @Override
    public void delete(Long id) {
        throw new EntityExistsException("Los usuarios no se eliminan; deben desactivarse.");
    }

    @Override
    protected void setId(Usuario entity, Long id) {
        entity.setId(id);
    }

    private void prepareCommonData(Usuario entity, Long currentId) {
        Perfil perfil = validatePerfil(entity);
        String email = entity.getEmail().trim().toLowerCase();
        boolean duplicated = currentId == null
                ? repository.existsByEmailIgnoreCase(email)
                : repository.existsByEmailIgnoreCaseAndIdNot(email, currentId);
        if (duplicated) {
            throw new EntityExistsException("Ya existe un usuario registrado con ese correo.");
        }
        entity.setNombre(entity.getNombre().trim());
        entity.setEmail(email);
        entity.setPerfil(perfil);
    }

    private Perfil validatePerfil(Usuario usuario) {
        Long perfilId = usuario.getPerfil() == null ? null : usuario.getPerfil().getId();
        Perfil perfil = perfilId == null
                ? null
                : perfilRepository.findById(perfilId).orElse(null);
        if (perfil == null || !Boolean.TRUE.equals(perfil.getActivo())) {
            throw new EntityNotFoundException("El perfil seleccionado no existe.");
        }
        return perfil;
    }
}
