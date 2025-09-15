package com.sanez.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UsuarioRequestDTO {

    @NotBlank
    private String nombre;

    @NotBlank
    private String email;

    @NotBlank
    private String password;


    private Set<String> roles;
}
