package cl.comunigest.backend.security;

import cl.comunigest.backend.entity.Usuario;
import cl.comunigest.backend.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final JsonAuthenticationEntryPoint authenticationEntryPoint;

    public JwtAuthenticationFilter(JwtService jwtService, UsuarioRepository usuarioRepository,
                                   JsonAuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authorization.substring(7).trim();
            Claims claims = jwtService.parseAndValidate(token);
            Number userIdClaim = claims.get("userId", Number.class);
            if (userIdClaim == null) {
                throw new JwtException("El token no contiene un identificador de usuario");
            }
            Long userId = userIdClaim.longValue();
            Usuario usuario = usuarioRepository.findById(userId)
                    .filter(value -> Boolean.TRUE.equals(value.getActivo()))
                    .orElseThrow(() -> new JwtException("Usuario inexistente o inactivo"));
            if (!usuario.getEmail().equalsIgnoreCase(claims.getSubject())) {
                throw new JwtException("El sujeto del token no coincide");
            }

            AuthenticatedUser principal = new AuthenticatedUser(usuario.getId(), usuario.getEmail(),
                    usuario.getNombre(), usuario.getPerfil().getNombre());
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal, null, List.of(new SimpleGrantedAuthority(
                    "ROLE_" + usuario.getPerfil().getNombre().toUpperCase())));
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException exception) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response,
                    new org.springframework.security.authentication.BadCredentialsException(
                            "Token JWT inválido", exception));
        }
    }
}
