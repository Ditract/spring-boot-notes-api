package com.sanez.security.jwt;

import com.sanez.security.service.CustomUserDetails;
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

        // Construye el token base con subject (username), roles, fechas de emisión y expiración.
        JwtBuilder builder = Jwts.builder()
                .subject(userDetails.getUsername()) // Establece el email/username como "subject".
                .claim("roles", roles) // Añade los roles como claim personalizado.
                .issuedAt(new Date()) // Fecha de emisión (ahora).
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs)); // Fecha de expiración.

        // Si el UserDetails es una instancia de CustomUserDetails, incluye el ID del usuario en el token.
        if (userDetails instanceof CustomUserDetails customUser) {
            builder.claim("userId", customUser.getId()); // Añade el ID del usuario como claim.
        }

        // Firma el token con la clave secreta y lo genera como string.
        return builder.signWith(getSigningKey()).compact();
    }

    // Genera un token de refresco (no usado actualmente, pero preparado para endpoints de refresh).
    public String generateRefreshToken(UserDetails userDetails) {
        String username = userDetails.getUsername();
        // Construye el token de refresco base con subject (username), fechas de emisión y expiración más larga.
        JwtBuilder builder = Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtRefreshExpirationMs));

        // Si el UserDetails es una instancia de CustomUserDetails, incluye el ID del usuario en el token de refresco.
        if (userDetails instanceof CustomUserDetails customUser) {
            builder.claim("userId", customUser.getId()); // Añade el ID del usuario como claim.
        }

        // Firma el token con la clave secreta y lo genera como string.
        return builder.signWith(getSigningKey()).compact();
    }

    // Genera un nuevo token de acceso a partir de un token de refresco válido (no usado actualmente).
    public String refreshAccessToken(String refreshToken) {
        // Valida el token de refresco.
        if (validateToken(refreshToken)) {
            // Extrae el username del token de refresco.
            String username = getUsernameFromToken(refreshToken);
            // Construye el nuevo token de acceso con expiración estándar.
            JwtBuilder builder = Jwts.builder()
                    .subject(username)
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs));

            // Si el token de refresco contiene userId, lo incluye en el nuevo token de acceso.
            Long userId = getUserIdFromToken(refreshToken);
            if (userId != null) {
                builder.claim("userId", userId);
            }

            return builder.signWith(getSigningKey()).compact();
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

    // Extrae el ID del usuario de un token JWT válido (devuelve null si no existe el claim).
    public Long getUserIdFromToken(String token) {
        try {
            // Parsea el token y obtiene los claims para extraer el userId.
            Claims claims = Jwts.parser()
                    .verifyWith((SecretKey) getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            // Obtiene el claim "userId" como Long (devuelve null si no existe).
            return claims.get("userId", Long.class);
        } catch (Exception e) {
            logger.error("Error extracting userId from token: {}", e.getMessage());
            return null; // Devuelve null si hay algún error al extraer el ID.
        }
    }

    // Extrae los roles de un token JWT válido (devuelve lista vacía si no existen).
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        try {
            // Parsea el token y obtiene los claims para extraer los roles.
            Claims claims = Jwts.parser()
                    .verifyWith((SecretKey) getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            // Obtiene el claim "roles" como lista de strings.
            return (List<String>) claims.get("roles");
        } catch (Exception e) {
            logger.error("Error extracting roles from token: {}", e.getMessage());
            return List.of(); // Devuelve lista vacía si hay algún error al extraer los roles.
        }
    }

    // Extrae la fecha de expiración de un token JWT válido.
    public Date getExpirationDateFromToken(String token) {
        try {
            // Parsea el token y obtiene la fecha de expiración.
            return Jwts.parser()
                    .verifyWith((SecretKey) getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();
        } catch (Exception e) {
            logger.error("Error extracting expiration date from token: {}", e.getMessage());
            return null; // Devuelve null si hay algún error al extraer la fecha.
        }
    }

    // Crea la clave de firma a partir de jwt.secret (decodificada de Base64).
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
}