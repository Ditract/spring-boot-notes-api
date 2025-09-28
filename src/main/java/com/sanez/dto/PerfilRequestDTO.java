package com.sanez.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class PerfilRequestDTO {

    @NotBlank(message = "Debe escribir su nombre completo")
    @Size(min = 5, max = 50, message = "Su nombre completo debe tener entre 5 y 50 caracteres")
    private String nombreCompleto;

    private List<Long> notasFavoritas;

}
