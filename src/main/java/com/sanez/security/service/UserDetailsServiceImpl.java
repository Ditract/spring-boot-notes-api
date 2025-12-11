package com.sanez.security.service;

import com.sanez.exception.AccesoNoAutorizadoException;
import com.sanez.model.Usuario;
import com.sanez.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws AccesoNoAutorizadoException {

        //Buscamos usuario en DB
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Intento de login fallido para email: {}", email);
                    return new AccesoNoAutorizadoException("Credenciales inválidas");
                });

        // Verificar si el usuario ha confirmado su email
        if (!usuario.isEnabled()) {
            logger.warn("Intento de login con cuenta no verificada: {}", email);
            throw new AccesoNoAutorizadoException("Cuenta no verificada. Por favor, verifica tu correo electrónico.");
        }

        // Convertir roles a grantedAuthority
        Set<GrantedAuthority> authorities = usuario.getRoles().stream()
                .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.getNombre()))
                .collect(Collectors.toSet());

        return new CustomUserDetails(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getPassword(),
                authorities
        );
    }
}