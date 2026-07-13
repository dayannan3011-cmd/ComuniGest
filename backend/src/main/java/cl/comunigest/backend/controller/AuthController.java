package cl.comunigest.backend.controller;

import cl.comunigest.backend.dto.LoginRequest;
import cl.comunigest.backend.dto.LoginResponse;
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

    public AuthController(UsuarioService usuarioService, JwtService jwtService) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Usuario usuario = usuarioService.autenticar(request.getEmail(), request.getPassword());
        return new LoginResponse(usuario, jwtService.generateToken(usuario));
    }
}
