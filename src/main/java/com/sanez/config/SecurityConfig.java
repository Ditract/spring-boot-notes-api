package com.sanez.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

// Configuración de Spring Security para la API.
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Inyección de dependencias: manejador de errores, filtro JWT, y servicio de usuarios.
    private final AuthEntryPointJwt unauthorizedHandler;
    private final AuthTokenFilter authTokenFilter;
    private final UserDetailsService userDetailsService;

    // Constructor para inyectar dependencias.
    public SecurityConfig(AuthEntryPointJwt unauthorizedHandler, AuthTokenFilter authTokenFilter, UserDetailsService userDetailsService) {
        this.unauthorizedHandler = unauthorizedHandler;
        this.authTokenFilter = authTokenFilter;
        this.userDetailsService = userDetailsService;
    }

    // Configura el AuthenticationManager para autenticar usuarios con UserDetailsService y PasswordEncoder.
    @Bean
    public AuthenticationManager authenticationManager() {
        // Crea un proveedor de autenticación basado en DAO (base de datos).
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // Usa UserDetailsService para cargar usuarios.
        authProvider.setPasswordEncoder(passwordEncoder()); // Usa BCrypt para validar contraseñas.
        return new ProviderManager(List.of(authProvider)); // Devuelve el AuthenticationManager.
    }

    // Configura la cadena de filtros de seguridad para HTTP.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Habilita CORS (sin configuración específica, debería ajustarse en producción).
                .cors(cors -> {})
                // Deshabilita CSRF, ya que la API es stateless y usa JWT.
                .csrf(AbstractHttpConfigurer::disable)
                // Configura el manejador de errores de autenticación (401).
                .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedHandler))
                // Usa gestión de sesiones sin estado (stateless) para APIs JWT.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Define reglas de autorización para endpoints.
                .authorizeHttpRequests(auth -> auth
                        // Permite acceso público a endpoints de autenticación (login, signup).
                        .requestMatchers("/api/auth/**").permitAll()
                        // Requiere rol USER o ADMIN para endpoints de notas.
                        .requestMatchers("/api/notas/**").hasAnyRole("USER", "ADMIN")
                        // Requiere rol ADMIN para gestión de usuarios.
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        // Requiere rol ADMIN para endpoints administrativos.
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // Cualquier otro endpoint requiere autenticación.
                        .anyRequest().authenticated()
                );

        // Añade el filtro JWT antes del filtro de autenticación estándar.
        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // Define el codificador de contraseñas (BCrypt).
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}