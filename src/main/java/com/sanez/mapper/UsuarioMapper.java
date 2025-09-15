package com.sanez.mapper;

import com.sanez.dto.UsuarioRequestDTO;
import com.sanez.dto.UsuarioResponseDTO;
import com.sanez.model.Rol;
import com.sanez.model.Usuario;

import java.util.stream.Collectors;

public class UsuarioMapper {

    public static UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setEmail(usuario.getEmail());
        dto.setRoles(usuario.getRoles().stream().map(Rol::getNombre).collect(Collectors.toSet()));
        return dto;
    }

    public static Usuario toEntity(UsuarioRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(dto.getPassword());
        return usuario;
    }
}
