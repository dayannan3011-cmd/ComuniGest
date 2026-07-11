package cl.comunigest.backend.controller;

import cl.comunigest.backend.dto.LoginRequest;
import cl.comunigest.backend.dto.LoginResponse;
import cl.comunigest.backend.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return new LoginResponse(usuarioService.autenticar(request.getEmail(), request.getPassword()));
    }
}
