package com.sanez.controller;

import com.sanez.dto.auth.LoginRequest;
import com.sanez.dto.auth.LoginResponse;
import com.sanez.dto.usuario.UsuarioRequestDTO;
import com.sanez.dto.usuario.UsuarioResponseDTO;
import com.sanez.security.jwt.JwtUtil;
import com.sanez.exception.AccesoNoAutorizadoException;
import com.sanez.service.AuthService;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AuthService authService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                          AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.authService = authService;
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

        UsuarioResponseDTO usuarioCreado = authService.registrarUsuario(usuarioRequestDTO);

        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Usuario registrado exitosamente. Por favor, verifica tu correo electrónico.");
        response.put("email", usuarioCreado.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //Verificación de cuenta por email
    @GetMapping("/verify")
    public ResponseEntity<?> verifyAccount(@RequestParam("token") String token) {
        authService.verificarCuenta(token);

        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Cuenta verificada exitosamente. Ya puedes iniciar sesión.");

        return ResponseEntity.ok(response);
    }

    // Reenvío de email de verificación
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerificationEmail(@RequestParam("email") String email) {
        authService.reenviarEmailVerificacion(email);

        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Email de verificación reenviado. Por favor, revisa tu correo.");

        return ResponseEntity.ok(response);
    }

}
