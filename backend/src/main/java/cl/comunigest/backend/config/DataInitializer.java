package cl.comunigest.backend.config;

import cl.comunigest.backend.entity.Perfil;
import cl.comunigest.backend.entity.Usuario;
import cl.comunigest.backend.repository.PerfilRepository;
import cl.comunigest.backend.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(PerfilRepository perfilRepository, UsuarioRepository usuarioRepository) {
        return args -> {
            Perfil admin = perfilRepository.findByNombre("ADMINISTRADOR")
                    .orElseGet(() -> {
                        Perfil perfil = new Perfil();
                        perfil.setNombre("ADMINISTRADOR");
                        perfil.setDescripcion("Administracion general del sistema");
                        return perfilRepository.save(perfil);
                    });

            perfilRepository.findByNombre("CONSERJE").orElseGet(() -> {
                Perfil perfil = new Perfil();
                perfil.setNombre("CONSERJE");
                perfil.setDescripcion("Operacion diaria de conserjeria");
                return perfilRepository.save(perfil);
            });

            usuarioRepository.findByEmail("admin@comunigest.local").orElseGet(() -> {
                Usuario usuario = new Usuario();
                usuario.setNombre("Administrador ComuniGest");
                usuario.setEmail("admin@comunigest.local");
                usuario.setPassword("admin123");
                usuario.setPerfil(admin);
                return usuarioRepository.save(usuario);
            });
        };
    }
}
