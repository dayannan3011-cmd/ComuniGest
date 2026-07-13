package cl.comunigest.backend.config;

import cl.comunigest.backend.entity.Perfil;
import cl.comunigest.backend.entity.Usuario;
import cl.comunigest.backend.repository.PerfilRepository;
import cl.comunigest.backend.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(PerfilRepository perfilRepository, UsuarioRepository usuarioRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            Perfil admin = perfilRepository.findByNombre("ADMINISTRADOR")
                    .orElseGet(() -> {
                        Perfil perfil = new Perfil();
                        perfil.setNombre("ADMINISTRADOR");
                        perfil.setDescripcion("Administración general del sistema");
                        return perfilRepository.save(perfil);
                    });
            if (!"Administración general del sistema".equals(admin.getDescripcion())) {
                admin.setDescripcion("Administración general del sistema");
                perfilRepository.save(admin);
            }

            Perfil conserje = perfilRepository.findByNombre("CONSERJE").orElseGet(() -> {
                Perfil perfil = new Perfil();
                perfil.setNombre("CONSERJE");
                perfil.setDescripcion("Operación diaria de conserjería");
                return perfilRepository.save(perfil);
            });
            if (!"Operación diaria de conserjería".equals(conserje.getDescripcion())) {
                conserje.setDescripcion("Operación diaria de conserjería");
                perfilRepository.save(conserje);
            }

            usuarioRepository.findByEmailIgnoreCase("admin@comunigest.local").orElseGet(() -> {
                Usuario usuario = new Usuario();
                usuario.setNombre("Administrador ComuniGest");
                usuario.setEmail("admin@comunigest.local");
                usuario.setPassword(passwordEncoder.encode("admin123"));
                usuario.setPerfil(admin);
                return usuarioRepository.save(usuario);
            });
        };
    }
}
