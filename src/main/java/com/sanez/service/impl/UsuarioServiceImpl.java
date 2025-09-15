package com.sanez.service.impl;

import com.sanez.dto.UsuarioRequestDTO;
import com.sanez.dto.UsuarioResponseDTO;
import com.sanez.mapper.UsuarioMapper;
import com.sanez.model.Rol;
import com.sanez.model.Usuario;
import com.sanez.repository.RoleRepository;
import com.sanez.repository.UsuarioRepository;
import com.sanez.service.UsuarioService;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                              BCryptPasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }


    @Override
    public UsuarioResponseDTO crearUsuario(UsuarioRequestDTO usuarioRequestDTO) {
        Usuario usuario = UsuarioMapper.toEntity(usuarioRequestDTO);
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        // 🔹 Asignar automáticamente el rol "USER"
        Rol rolUsuario = roleRepository.findByNombre("USER")
                .orElseThrow(() -> new RuntimeException("Error: Rol 'USER' no encontrado"));

        usuario.setRoles(Set.of(rolUsuario));

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        return UsuarioMapper.toResponseDTO(usuarioGuardado);
    }



    @Override
    public UsuarioResponseDTO obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .map(UsuarioMapper::toResponseDTO)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
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
        usuarioRepository.deleteById(id);
    }
}
