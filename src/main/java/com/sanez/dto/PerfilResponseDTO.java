package com.sanez.dto;


import lombok.Data;

import java.util.List;

@Data
public class PerfilResponseDTO {

    private Long id;
    private String nombre;
    private List<Long> notasFavoritas;


}
