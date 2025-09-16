package com.sanez.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// Componente que maneja errores de autenticación (ej. 401 Unauthorized) en la API.
@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    // Logger para registrar errores de autenticación.
    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);
    // ObjectMapper para serializar respuestas JSON.
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Método invocado cuando ocurre un error de autenticación (ej. token inválido o ausente).
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        // Loggea el intento de acceso no autorizado con el mensaje de la excepción.
        logger.error("Unauthorized access attempt: {}", authException.getMessage());

        // Configura la respuesta como JSON con status 401.
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        // Crea un objeto JSON con detalles del error.
        final Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.UNAUTHORIZED.value()); // Código HTTP 401.
        body.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase()); // "Unauthorized".
        body.put("message", authException.getMessage()); // Mensaje de la excepción.
        body.put("path", request.getRequestURI()); // Endpoint solicitado (ej. "/api/notas").
        body.put("timestamp", System.currentTimeMillis()); // Marca de tiempo del error.

        // Escribe la respuesta JSON en el cuerpo de la respuesta HTTP.
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}