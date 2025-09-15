package com.sanez.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {
    private String email;
    private List<String> roles;
    private String token;
}
