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
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public AuthServiceImpl(UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder,
                           RoleRepository roleRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
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

        // Crear perfil automáticamente
        Perfil perfil = new Perfil();
        perfil.setUsuario(usuario);
        perfil.setNombre("");
        usuario.setPerfil(perfil);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        return UsuarioMapper.toResponseDTO(usuarioGuardado);
    }
}
