package com.sanez.service;

import com.sanez.dto.UsuarioRequestDTO;
import com.sanez.dto.UsuarioResponseDTO;

import java.util.List;

public interface UsuarioService {
    UsuarioResponseDTO crearUsuario(UsuarioRequestDTO usuarioRequestDTO);
    UsuarioResponseDTO obtenerUsuarioPorId(Long id);
    List<UsuarioResponseDTO> listarUsuarios();
    void eliminarUsuario(Long id);
}
