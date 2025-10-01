package com.sanez.controller;

import com.sanez.dto.auth.LoginRequest;
import com.sanez.dto.auth.LoginResponse;
import com.sanez.dto.usuario.UsuarioRequestDTO;
import com.sanez.dto.usuario.UsuarioResponseDTO;
import com.sanez.security.jwt.JwtUtil;
import com.sanez.exception.AccesoNoAutorizadoException;
import com.sanez.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UsuarioService usuarioService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                          UsuarioService usuarioService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.usuarioService = usuarioService;

    }

    //Inicio de sesión
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        } catch (AuthenticationException exception) {
            throw new AccesoNoAutorizadoException("Credenciales inválidos");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwtToken = jwtUtil.generateToken(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        LoginResponse response = new LoginResponse(userDetails.getUsername(), roles, jwtToken);
        return ResponseEntity.ok(response);
    }

    //Registro de usuarios públicos
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UsuarioRequestDTO usuarioRequestDTO) {

        UsuarioResponseDTO crearUsuario = usuarioService.crearUsuario(usuarioRequestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(crearUsuario);
    }

}