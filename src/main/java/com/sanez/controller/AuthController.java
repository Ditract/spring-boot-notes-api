package com.sanez.controller;

import com.sanez.dto.auth.LoginRequest;
import com.sanez.dto.auth.LoginResponse;
import com.sanez.dto.auth.ResetPasswordRequest;
import com.sanez.dto.usuario.UsuarioRequestDTO;
import com.sanez.dto.usuario.UsuarioResponseDTO;
import com.sanez.exception.AccesoNoAutorizadoException;
import com.sanez.security.jwt.JwtUtil;
import com.sanez.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Autenticación", description = "Endpoints públicos para registro, login y recuperación de contraseña")
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

    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y retorna un token JWT válido por 1 hora")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login exitoso",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas o cuenta no verificada")
    })
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

    @Operation(summary = "Registrar nuevo usuario",
            description = "Crea una cuenta nueva y envía un email de verificación. El usuario debe verificar su email antes de poder iniciar sesión.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente. Email de verificación enviado."),
            @ApiResponse(responseCode = "409", description = "El email ya está registrado"),
            @ApiResponse(responseCode = "400", description = "Datos de registro inválidos")
    })
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del nuevo usuario. La contraseña debe tener mínimo 8 caracteres, incluir mayúscula, minúscula, número y carácter especial.",
                    content = @Content(
                            schema = @Schema(implementation = UsuarioRequestDTO.class),
                            examples = @ExampleObject(value = "{\"email\":\"usuario@example.com\",\"password\":\"Password123!\"}")
                    )
            )
            @Valid @RequestBody UsuarioRequestDTO usuarioRequestDTO) {

        UsuarioResponseDTO usuarioCreado = authService.registrarUsuario(usuarioRequestDTO);

        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Usuario registrado exitosamente. Por favor, verifica tu correo electrónico.");
        response.put("email", usuarioCreado.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Verificar cuenta",
            description = "Activa la cuenta del usuario usando el token recibido por email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cuenta verificada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Token inválido o expirado")
    })
    @GetMapping("/verify")
    public ResponseEntity<?> verifyAccount(@RequestParam("token") String token) {
        authService.verificarCuenta(token);

        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Cuenta verificada exitosamente. Ya puedes iniciar sesión.");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Reenviar email de verificación",
            description = "Genera un nuevo token y reenvía el email de verificación si el anterior expiró")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email reenviado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "400", description = "La cuenta ya está verificada")
    })
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerificationEmail(@RequestParam("email") String email) {
        authService.reenviarEmailVerificacion(email);

        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Email de verificación reenviado. Por favor, revisa tu correo.");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Solicitar recuperación de contraseña",
            description = "Envía un email con un link para resetear la contraseña. El token expira en 1 hora.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email enviado si el usuario existe")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam("email") String email) {
        authService.solicitarRecuperacionPassword(email);

        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Si el correo existe, recibirás un email con instrucciones para restablecer tu contraseña.");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Resetear contraseña",
            description = "Establece una nueva contraseña usando el token recibido por email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contraseña actualizada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Token inválido o expirado"),
            @ApiResponse(responseCode = "400", description = "Nueva contraseña inválida o igual a la anterior")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Token y nueva contraseña",
                    content = @Content(
                            schema = @Schema(implementation = ResetPasswordRequest.class),
                            examples = @ExampleObject(value = "{\"token\":\"uuid-token\",\"nuevaPassword\":\"NewPass123!\"}")
                    )
            )
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetearPassword(request);

        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Contraseña restablecida exitosamente. Ya puedes iniciar sesión con tu nueva contraseña.");

        return ResponseEntity.ok(response);
    }
}