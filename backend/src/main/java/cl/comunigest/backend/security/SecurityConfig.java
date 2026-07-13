package cl.comunigest.backend.security;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    @Order(2)
    SecurityFilterChain apiSecurity(HttpSecurity http, JwtAuthenticationFilter jwtFilter,
                                    JsonAuthenticationEntryPoint authenticationEntryPoint,
                                    JsonAccessDeniedHandler accessDeniedHandler) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/h2-console/**").denyAll()
                        .requestMatchers("/api/perfiles/**", "/api/usuarios/**", "/api/reportes/**")
                            .hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/residentes/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/departamentos/**")
                            .hasAnyRole("ADMINISTRADOR", "CONSERJE")
                        .requestMatchers("/api/departamentos/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/turnos/**")
                            .hasAnyRole("ADMINISTRADOR", "CONSERJE")
                        .requestMatchers(HttpMethod.POST, "/api/turnos/**").hasRole("CONSERJE")
                        .requestMatchers(HttpMethod.PATCH, "/api/turnos/**").hasRole("CONSERJE")
                        .requestMatchers(HttpMethod.GET, "/api/visitas", "/api/visitas/activas")
                            .hasAnyRole("ADMINISTRADOR", "CONSERJE")
                        .requestMatchers(HttpMethod.GET, "/api/visitas/*").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/visitas/**").hasRole("CONSERJE")
                        .requestMatchers(HttpMethod.PATCH, "/api/visitas/**").hasRole("CONSERJE")
                        .requestMatchers(HttpMethod.GET, "/api/encomiendas/**")
                            .hasAnyRole("ADMINISTRADOR", "CONSERJE")
                        .requestMatchers(HttpMethod.POST, "/api/encomiendas/**").hasRole("CONSERJE")
                        .requestMatchers(HttpMethod.PATCH, "/api/encomiendas/**").hasRole("CONSERJE")
                        .requestMatchers(HttpMethod.GET, "/api/incidencias/**")
                            .hasAnyRole("ADMINISTRADOR", "CONSERJE")
                        .requestMatchers(HttpMethod.POST, "/api/incidencias/registro").hasRole("CONSERJE")
                        .requestMatchers(HttpMethod.PATCH, "/api/incidencias/*/iniciar-gestion",
                                "/api/incidencias/*/resolver").hasRole("ADMINISTRADOR")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Configuration
    @Profile("h2")
    @ConditionalOnProperty(name = "spring.h2.console.enabled", havingValue = "true")
    static class LocalH2ConsoleSecurity {
        @Bean
        @Order(1)
        SecurityFilterChain h2ConsoleSecurity(HttpSecurity http) throws Exception {
            http
                    .securityMatcher(PathRequest.toH2Console())
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
            return http.build();
        }
    }
}
