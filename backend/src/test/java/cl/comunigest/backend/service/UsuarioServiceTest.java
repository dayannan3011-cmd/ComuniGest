package cl.comunigest.backend.service;

import cl.comunigest.backend.entity.Perfil;
import cl.comunigest.backend.entity.Usuario;
import cl.comunigest.backend.repository.PerfilRepository;
import cl.comunigest.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PerfilRepository perfilRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    private UsuarioService service;
    private Perfil conserje;

    @BeforeEach
    void setUp() {
        service = new UsuarioService(usuarioRepository, perfilRepository, passwordEncoder);
        conserje = new Perfil();
        conserje.setId(2L);
        conserje.setNombre("CONSERJE");
        conserje.setActivo(true);
    }

    @Test
    void crearUsuarioGuardaBcryptYPermiteAutenticar() {
        Usuario nuevo = usuario(null, "nuevo@comunigest.local", "clave-segura");
        when(perfilRepository.findById(2L)).thenReturn(Optional.of(conserje));
        when(usuarioRepository.existsByEmailIgnoreCase(nuevo.getEmail())).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario guardado = service.save(nuevo);

        assertTrue(guardado.getPassword().startsWith("$2"));
        assertTrue(passwordEncoder.matches("clave-segura", guardado.getPassword()));
        when(usuarioRepository.findByEmailIgnoreCase(guardado.getEmail())).thenReturn(Optional.of(guardado));
        assertSame(guardado, service.autenticar(guardado.getEmail(), "clave-segura"));
    }

    @Test
    void actualizarSinClaveConservaHashActual() {
        Usuario actual = usuario(7L, "actual@comunigest.local", passwordEncoder.encode("vigente"));
        Usuario cambios = usuario(null, "actual@comunigest.local", null);
        String hashOriginal = actual.getPassword();
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(actual));
        when(perfilRepository.findById(2L)).thenReturn(Optional.of(conserje));
        when(usuarioRepository.existsByEmailIgnoreCaseAndIdNot(cambios.getEmail(), 7L)).thenReturn(false);
        when(usuarioRepository.save(actual)).thenReturn(actual);

        service.update(7L, cambios);

        assertEquals(hashOriginal, actual.getPassword());
        assertTrue(passwordEncoder.matches("vigente", actual.getPassword()));
    }

    @Test
    void actualizarClaveInvalidaAnteriorYAceptaNueva() {
        Usuario actual = usuario(8L, "cambio@comunigest.local", passwordEncoder.encode("anterior"));
        Usuario cambios = usuario(null, "cambio@comunigest.local", "nueva");
        when(usuarioRepository.findById(8L)).thenReturn(Optional.of(actual));
        when(perfilRepository.findById(2L)).thenReturn(Optional.of(conserje));
        when(usuarioRepository.existsByEmailIgnoreCaseAndIdNot(cambios.getEmail(), 8L)).thenReturn(false);
        when(usuarioRepository.save(actual)).thenReturn(actual);

        service.update(8L, cambios);

        assertFalse(passwordEncoder.matches("anterior", actual.getPassword()));
        assertTrue(passwordEncoder.matches("nueva", actual.getPassword()));
    }

    @Test
    void loginRechazaClaveIncorrectaYUsuarioInactivo() {
        Usuario activo = usuario(9L, "activo@comunigest.local", passwordEncoder.encode("correcta"));
        when(usuarioRepository.findByEmailIgnoreCase(activo.getEmail())).thenReturn(Optional.of(activo));
        ResponseStatusException incorrecta = assertThrows(ResponseStatusException.class,
                () -> service.autenticar(activo.getEmail(), "incorrecta"));
        assertEquals(HttpStatus.UNAUTHORIZED, incorrecta.getStatusCode());

        Usuario inactivo = usuario(10L, "inactivo@comunigest.local", passwordEncoder.encode("correcta"));
        inactivo.setActivo(false);
        when(usuarioRepository.findByEmailIgnoreCase(inactivo.getEmail())).thenReturn(Optional.of(inactivo));
        ResponseStatusException bloqueado = assertThrows(ResponseStatusException.class,
                () -> service.autenticar(inactivo.getEmail(), "correcta"));
        assertEquals(HttpStatus.FORBIDDEN, bloqueado.getStatusCode());
    }

    private Usuario usuario(Long id, String email, String password) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombre("Usuario Prueba");
        usuario.setEmail(email);
        usuario.setPassword(password);
        usuario.setPerfil(conserje);
        usuario.setActivo(true);
        return usuario;
    }
}
