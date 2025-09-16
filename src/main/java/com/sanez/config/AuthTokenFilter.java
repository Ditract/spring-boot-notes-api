package com.sanez.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Filtro que procesa tokens JWT en cada solicitud HTTP para autenticar usuarios.
@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    // Logger para registrar eventos y errores de autenticación.
    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    // Inyección de dependencias: utilidades JWT y servicio de usuarios.
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    // Método principal que procesa cada solicitud HTTP.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Obtiene el path de la solicitud (ej. "/api/notas").
        String path = request.getServletPath();

        // Ignora los endpoints públicos de autenticación (login, signup).
        if (path.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extrae el token JWT del header Authorization.
            String jwt = jwtUtil.getJwtFromHeader(request);
            // Valida el token y autentica al usuario si es válido.
            if (jwt != null && jwtUtil.validateToken(jwt)) {
                // Obtiene el username (email) del token.
                String username = jwtUtil.getUsernameFromToken(jwt);

                // Verifica si no hay autenticación previa en el contexto.
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Carga los detalles del usuario desde la base de datos.
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // Crea un objeto de autenticación con los detalles del usuario y sus roles.
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    // Añade detalles de la solicitud (como IP, headers) al objeto de autenticación.
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Establece la autenticación en el contexto de seguridad.
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    // Loggea la autenticación exitosa.
                    logger.debug("User authenticated: {} with roles: {}", username, userDetails.getAuthorities());
                }
            }
        } catch (Exception e) {
            // Loggea cualquier error durante la validación o autenticación.
            logger.error("Authentication error: {}", e.getMessage());
        }

        // Continúa con la cadena de filtros.
        filterChain.doFilter(request, response);
    }
}