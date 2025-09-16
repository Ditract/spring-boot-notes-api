package com.sanez.service;

import com.sanez.dto.NotaDTO;
import java.util.List;

public interface NotaService {
    NotaDTO crearNota(NotaDTO notaDTO);
    List<NotaDTO> obtenerNotasPorUsuario();
    void eliminarNota(Long id);
}