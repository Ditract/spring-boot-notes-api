package com.sanez.service;

import com.sanez.dto.usuario.UsuarioRequestDTO;
import com.sanez.dto.usuario.UsuarioResponseDTO;

public interface AuthService {

    UsuarioResponseDTO registrarUsuario(UsuarioRequestDTO usuarioRequestDTO);
    void verificarCuenta(String token);
}