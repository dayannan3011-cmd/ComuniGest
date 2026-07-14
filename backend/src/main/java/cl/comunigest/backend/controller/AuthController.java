package cl.comunigest.backend.controller;

import cl.comunigest.backend.dto.LoginRequest;
import cl.comunigest.backend.dto.LoginResponse;
import cl.comunigest.backend.dto.MensajeResponse;
import cl.comunigest.backend.dto.RecuperarClaveRequest;
import cl.comunigest.backend.dto.RestablecerClaveRequest;
import cl.comunigest.backend.service.RecuperacionClaveService;
import cl.comunigest.backend.service.UsuarioService;
import cl.comunigest.backend.security.JwtService;
import cl.comunigest.backend.entity.Usuario;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    private final RecuperacionClaveService recuperacionClaveService;

    public AuthController(UsuarioService usuarioService, JwtService jwtService,
                          RecuperacionClaveService recuperacionClaveService) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
        this.recuperacionClaveService = recuperacionClaveService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Usuario usuario = usuarioService.autenticar(request.getEmail(), request.getPassword());
        return new LoginResponse(usuario, jwtService.generateToken(usuario));
    }

    @PostMapping("/recuperar-clave")
    public MensajeResponse recuperarClave(@Valid @RequestBody RecuperarClaveRequest request) {
        recuperacionClaveService.solicitar(request.getEmail());
        return new MensajeResponse(RecuperacionClaveService.MENSAJE_SOLICITUD);
    }

    @PostMapping("/restablecer-clave")
    public MensajeResponse restablecerClave(@Valid @RequestBody RestablecerClaveRequest request) {
        recuperacionClaveService.restablecer(request.getToken(), request.getNuevaClave(),
                request.getConfirmacionClave());
        return new MensajeResponse("La contraseña fue restablecida correctamente.");
    }
}
