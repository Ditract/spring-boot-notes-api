package com.sanez.service;

import com.sanez.dto.nota.NotaRequestDTO;
import com.sanez.dto.nota.NotaResponseDTO;
import com.sanez.dto.nota.NotaUpdateDTO;

import java.util.List;

public interface NotaService {
    NotaResponseDTO crearNota(NotaRequestDTO notaRequestDTO);
    List<NotaResponseDTO> obtenerNotasPorUsuario();
    NotaResponseDTO editarNota(Long notaId, NotaUpdateDTO notaUpdateDTO);
    void eliminarNota(Long notaId);
}