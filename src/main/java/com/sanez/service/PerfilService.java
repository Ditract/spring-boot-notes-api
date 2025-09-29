package com.sanez.service;

import com.sanez.dto.PerfilRequestDTO;
import com.sanez.dto.PerfilResponseDTO;

public interface PerfilService {
    PerfilResponseDTO obtenerMiPerfil();
    PerfilResponseDTO actualizarMiPerfil(PerfilRequestDTO perfilRequestDTO);
    void agregarNotaFavorita(Long notaId);
    void removerNotaFavorita(Long notaId);
}