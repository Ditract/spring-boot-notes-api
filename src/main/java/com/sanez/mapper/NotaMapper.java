package com.sanez.mapper;


import com.sanez.dto.NotaDTO;
import com.sanez.model.Nota;

public class NotaMapper {

    public static NotaDTO toDTO(Nota nota) {
        if (nota == null) {
            return null;
        }
        NotaDTO dto = new NotaDTO();
        dto.setId(nota.getId());
        dto.setTitulo(nota.getTitulo());
        dto.setContenido(nota.getContenido());
        dto.setUsuarioId(nota.getUsuario().getId());
        return dto;
    }

    public static Nota toEntity(NotaDTO dto) {
        if (dto == null) {
            return null;
        }
        Nota nota = new Nota();
        nota.setId(dto.getId());
        nota.setTitulo(dto.getTitulo());
        nota.setContenido(dto.getContenido());
        return nota;  // No asignamos usuario aquí para evitar referencias cíclicas
    }
}
