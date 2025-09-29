package com.sanez.service.impl;

import com.sanez.dto.UsuarioRequestDTO;
import com.sanez.dto.UsuarioResponseDTO;
import com.sanez.exception.EmailYaRegistradoException;
import com.sanez.exception.RecursoNoEncontradoException;
import com.sanez.mapper.UsuarioMapper;
import com.sanez.model.Perfil;
import com.sanez.model.Rol;
import com.sanez.model.Usuario;
import com.sanez.repository.RoleRepository;
import com.sanez.repository.UsuarioRepository;
import com.sanez.service.UsuarioService;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                              PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }


    @Override
    public UsuarioResponseDTO crearUsuario(UsuarioRequestDTO usuarioRequestDTO) {

        if (usuarioRepository.findByEmail(usuarioRequestDTO.getEmail()).isPresent()) {
            throw new EmailYaRegistradoException("El email ya está en uso");
        }

        Usuario usuario = UsuarioMapper.toEntity(usuarioRequestDTO);
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        // 🔹 Asignar automáticamente el rol "USER"
        Rol rolUsuario = roleRepository.findByNombre("USER")
                .orElseThrow(() -> new RecursoNoEncontradoException("Error: Rol 'USER' no encontrado"));
        usuario.setRoles(Set.of(rolUsuario));

        //Crear perfil automáticamente
        Perfil perfil = new Perfil();
        perfil.setUsuario(usuario);
        usuario.setPerfil(perfil);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        return UsuarioMapper.toResponseDTO(usuarioGuardado);
    }



    @Override
    public UsuarioResponseDTO obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .map(UsuarioMapper::toResponseDTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    @Override
    public List<UsuarioResponseDTO> listarUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(UsuarioMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        usuarioRepository.delete(usuario);
    }
}
