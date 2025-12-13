package com.sanez.security.config;

import com.sanez.security.jwt.AuthEntryPointJwt;
import com.sanez.security.jwt.AuthTokenFilter;
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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthEntryPointJwt unauthorizedHandler;
    private final AuthTokenFilter authTokenFilter;
    private final UserDetailsService userDetailsService;

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
                .cors(cors -> {})
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Permitir acceso público a Swagger
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html",
                                "/api-docs/**").permitAll()
                        // Permite acceso público a endpoints de autenticación
                        .requestMatchers("/api/auth/**").permitAll()
                        // Requiere rol USER o ADMIN para endpoints de notas
                        .requestMatchers("/api/notas/**").hasAnyRole("USER", "ADMIN")
                        // Requiere rol USER O ADMIN para endpoints de perfil
                        .requestMatchers("/api/perfiles/**").hasAnyRole("USER", "ADMIN")
                        // Requiere rol ADMIN para gestión de usuarios
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        // Requiere rol ADMIN para endpoints administrativos
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // Cualquier otro endpoint requiere autenticación
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // Define el codificador de contraseñas (BCrypt).
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}