package com.sanez.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

// Clase utilitaria para manejar operaciones relacionadas con tokens JWT (generación, validación, extracción de datos).
@Component
public class JwtUtil {
    // Logger para registrar eventos y errores relacionados con JWT.
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    // Inyecta la clave secreta para firmar tokens desde application.properties (debe ser Base64).
    @Value("${jwt.secret}")
    private String jwtSecret;

    // Inyecta el tiempo de expiración de tokens de acceso en milisegundos (ej. 3600000 = 1 hora).
    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    // Inyecta el tiempo de expiración de tokens de refresco en milisegundos (no usado actualmente).
    @Value("${jwt.refreshExpiration}")
    private long jwtRefreshExpirationMs;

    // Genera un token JWT de acceso para un usuario autenticado.
    public String generateToken(UserDetails userDetails) {
        // Extrae los roles del usuario (ej. ["ROLE_USER"]) para incluirlos en el token.
        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Crea el token con: subject (username), claim de roles, fecha de emisión, expiración, y firma.
        return Jwts.builder()
                .subject(userDetails.getUsername()) // Establece el email/username como "subject".
                .claim("roles", roles) // Añade los roles como claim personalizado.
                .issuedAt(new Date()) // Fecha de emisión (ahora).
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs)) // Fecha de expiración.
                .signWith(getSigningKey()) // Firma el token con la clave secreta.
                .compact(); // Genera el token como string.
    }

    // Genera un token de refresco (no usado actualmente, pero preparado para endpoints de refresh).
    public String generateRefreshToken(UserDetails userDetails) {
        String username = userDetails.getUsername();
        // Crea el token de refresco con: subject (username), fecha de emisión, expiración más larga, y firma.
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtRefreshExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    // Genera un nuevo token de acceso a partir de un token de refresco válido (no usado actualmente).
    public String refreshAccessToken(String refreshToken) {
        // Valida el token de refresco.
        if (validateToken(refreshToken)) {
            // Extrae el username del token de refresco.
            String username = getUsernameFromToken(refreshToken);
            // Genera un nuevo token de acceso con expiración estándar.
            return Jwts.builder()
                    .subject(username)
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                    .signWith(getSigningKey())
                    .compact();
        }
        throw new RuntimeException("Invalid refresh token");
    }

    // Extrae el token JWT del header Authorization de la solicitud HTTP.
    public String getJwtFromHeader(HttpServletRequest request) {
        // Obtiene el header Authorization (ej. "Bearer eyJ...").
        String bearerToken = request.getHeader("Authorization");
        logger.debug("Authorization Header: {}", bearerToken);
        // Verifica si el header existe y comienza con "Bearer ".
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Devuelve el token sin "Bearer ".
        }
        return null; // Devuelve null si no hay token válido.
    }

    // Valida un token JWT verificando su firma, formato, y expiración.
    public boolean validateToken(String authToken) {
        try {
            // Parsea el token usando la clave secreta para verificar su integridad.
            Jwts.parser().verifyWith((SecretKey) getSigningKey()).build().parseSignedClaims(authToken);
            return true; // Token válido.
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage()); // Token mal formado.
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage()); // Token expirado.
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage()); // Formato no soportado.
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage()); // Claims vacíos.
        }
        return false; // Token inválido.
    }

    // Extrae el username (subject) de un token JWT válido.
    public String getUsernameFromToken(String token) {
        // Parsea el token y obtiene el subject (email/username).
        return Jwts.parser()
                .verifyWith((SecretKey) getSigningKey())
                .build().parseSignedClaims(token)
                .getPayload().getSubject();
    }

    // Crea la clave de firma a partir de jwt.secret (decodificada de Base64).
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
}