package com.sanez.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UsuarioResponseDTO {

    private Long id;
    private String email;
    private Set<String> roles;
}
