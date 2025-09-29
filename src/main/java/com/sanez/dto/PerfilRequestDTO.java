package com.sanez.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class PerfilRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 40, message = "El nombre debe tener entre 3 a 40 caracteres")
    private String nombre;

    private List<Long> notasFavoritas;

}
