package com.sanez.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotaDTO {

    private Long id;

    @NotBlank
    private String titulo;

    @NotBlank
    private String contenido;

    private Long usuarioId;
}
