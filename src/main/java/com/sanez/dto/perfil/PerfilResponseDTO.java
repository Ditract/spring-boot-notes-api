package com.sanez.dto.perfil;


import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PerfilResponseDTO {

    private Long id;
    private String nombre;
    private List<Long> notasFavoritas;


}
