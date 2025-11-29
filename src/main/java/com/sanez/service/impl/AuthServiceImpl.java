package com.sanez.service.impl;

import com.sanez.dto.usuario.UsuarioRequestDTO;
import com.sanez.dto.usuario.UsuarioResponseDTO;
import com.sanez.exception.EmailYaRegistradoException;
import com.sanez.exception.RecursoNoEncontradoException;
import com.sanez.mapper.UsuarioMapper;
import com.sanez.model.Perfil;
import com.sanez.model.Rol;
import com.sanez.model.Usuario;
import com.sanez.repository.RoleRepository;
import com.sanez.repository.UsuarioRepository;
import com.sanez.service.AuthService;
import com.sanez.service.EmailService;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final EmailService emailService;

    public AuthServiceImpl(UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder,
                           RoleRepository roleRepository,
                           EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.emailService = emailService;
    }

    @Override
    public UsuarioResponseDTO registrarUsuario(UsuarioRequestDTO usuarioRequestDTO) {

        if (usuarioRepository.findByEmail(usuarioRequestDTO.getEmail()).isPresent()) {
            throw new EmailYaRegistradoException("El email ya está en uso");
        }

        Usuario usuario = UsuarioMapper.toEntity(usuarioRequestDTO);
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        // Asignar automáticamente el rol "USER" para registro público
        Rol rolUsuario = roleRepository.findByNombre("USER")
                .orElseThrow(() -> new RecursoNoEncontradoException("Error: Rol 'USER' no encontrado"));
        usuario.setRoles(Set.of(rolUsuario));

        // Usuario inactivo hasta verificar email
        usuario.setEnabled(false);

        // Generar token de verificación
        String token = UUID.randomUUID().toString();
        usuario.setVerificationToken(token);
        usuario.setTokenExpiration(LocalDateTime.now().plusHours(24)); // Token válido por 24 horas

        // Crear perfil automáticamente
        Perfil perfil = new Perfil();
        perfil.setUsuario(usuario);
        perfil.setNombre("");
        usuario.setPerfil(perfil);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // Enviar email de verificación
        emailService.enviarEmailVerificacion(usuarioGuardado.getEmail(), token);

        return UsuarioMapper.toResponseDTO(usuarioGuardado);
    }

    @Override
    public void verificarCuenta(String token) {
        Usuario usuario = usuarioRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RecursoNoEncontradoException("Token de verificación inválido"));

        // Verificar si el token ha expirado
        if (usuario.getTokenExpiration().isBefore(LocalDateTime.now())) {
            throw new RecursoNoEncontradoException("El token de verificación ha expirado");
        }

        // Activar usuario
        usuario.setEnabled(true);
        usuario.setVerificationToken(null); // Eliminar token después de usar
        usuario.setTokenExpiration(null);

        usuarioRepository.save(usuario);
    }
}