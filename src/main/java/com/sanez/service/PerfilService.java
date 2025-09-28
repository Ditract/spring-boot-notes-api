package com.sanez.service;

import com.sanez.dto.PerfilRequestDTO;
import com.sanez.dto.PerfilResponseDTO;

public interface PerfilService {
    PerfilResponseDTO obtenerPerfilPorUsuarioId(Long usuarioId);
    PerfilResponseDTO actualizarPerfil(Long usuarioId, PerfilRequestDTO perfilRequestDTO);
    void agregarFavorita(Long usuarioId, Long notaId);
    void removerFavorita(Long usuarioId, Long notaId);
}