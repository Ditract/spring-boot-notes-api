package com.sanez.service;

import com.sanez.dto.perfil.CambiarPasswordRequest;
import com.sanez.dto.perfil.PerfilRequestDTO;
import com.sanez.dto.perfil.PerfilResponseDTO;

public interface PerfilService {
    PerfilResponseDTO obtenerMiPerfil();
    PerfilResponseDTO actualizarMiPerfil(PerfilRequestDTO perfilRequestDTO);
    void agregarNotaFavorita(Long notaId);
    void removerNotaFavorita(Long notaId);
    void cambiarPassword(CambiarPasswordRequest request);
}