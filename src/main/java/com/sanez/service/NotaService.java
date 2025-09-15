package com.sanez.service;


import com.sanez.dto.NotaDTO;

import java.util.List;

public interface NotaService {
    NotaDTO crearNota(Long usuarioId, NotaDTO notaDTO);
    List<NotaDTO> obtenerNotasPorUsuario(Long usuarioId);
    void eliminarNota(Long id);
}
