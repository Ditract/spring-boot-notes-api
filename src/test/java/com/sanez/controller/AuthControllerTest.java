package com.sanez.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanez.dto.auth.LoginRequest;
import com.sanez.dto.auth.ResetPasswordRequest;
import com.sanez.dto.usuario.UsuarioRequestDTO;
import com.sanez.dto.usuario.UsuarioResponseDTO;
import com.sanez.exception.EmailYaRegistradoException;
import com.sanez.exception.RecursoNoEncontradoException;
import com.sanez.security.jwt.JwtUtil;
import com.sanez.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@Import(TestSecurityConfig.class)
@DisplayName("AuthController - Tests de Integración")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private AuthService authService;

    // ==================== TESTS DE SIGNUP ====================

    @Test
    @DisplayName("POST /signup - Registro exitoso retorna 201")
    void signup_exitoso_retorna201() throws Exception {
        // Arrange
        UsuarioRequestDTO request = new UsuarioRequestDTO();
        request.setEmail("test@example.com");
        request.setPassword("Password123!");

        UsuarioResponseDTO response = new UsuarioResponseDTO();
        response.setId(1L);
        response.setEmail("test@example.com");

        when(authService.registrarUsuario(any(UsuarioRequestDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensaje").exists())
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(authService, times(1)).registrarUsuario(any(UsuarioRequestDTO.class));
    }

    @Test
    @DisplayName("POST /signup - Email duplicado retorna 409")
    void signup_emailDuplicado_retorna409() throws Exception {
        // Arrange
        UsuarioRequestDTO request = new UsuarioRequestDTO();
        request.setEmail("duplicado@example.com");
        request.setPassword("Password123!");

        when(authService.registrarUsuario(any(UsuarioRequestDTO.class)))
                .thenThrow(new EmailYaRegistradoException("El email ya está en uso"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        verify(authService, times(1)).registrarUsuario(any(UsuarioRequestDTO.class));
    }

    @Test
    @DisplayName("POST /signup - Validación falla retorna 400")
    void signup_validacionFalla_retorna400() throws Exception {
        // Arrange - Password muy corta
        UsuarioRequestDTO request = new UsuarioRequestDTO();
        request.setEmail("test@example.com");
        request.setPassword("Pass1");

        // Act & Assert
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).registrarUsuario(any(UsuarioRequestDTO.class));
    }

    // ==================== TESTS DE SIGNIN ====================

    @Test
    @DisplayName("POST /signin - Login exitoso retorna 200 con token")
    void signin_exitoso_retorna200ConToken() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password123");

        UserDetails userDetails = User.builder()
                .username("test@example.com")
                .password("encodedPassword")
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("jwt-token-123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.token").value("jwt-token-123"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, times(1)).generateToken(any(UserDetails.class));
    }

    @Test
    @DisplayName("POST /signin - Credenciales inválidas retorna 401")
    void signin_credencialesInvalidas_retorna401() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, never()).generateToken(any(UserDetails.class));
    }

    // ==================== TESTS DE VERIFY ====================

    @Test
    @DisplayName("GET /verify - Token válido retorna 200")
    void verify_tokenValido_retorna200() throws Exception {
        // Arrange
        String token = "valid-token-123";
        doNothing().when(authService).verificarCuenta(token);

        // Act & Assert
        mockMvc.perform(get("/api/auth/verify")
                        .param("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").exists());

        verify(authService, times(1)).verificarCuenta(token);
    }

    @Test
    @DisplayName("GET /verify - Token inválido retorna 404")
    void verify_tokenInvalido_retorna404() throws Exception {
        // Arrange
        String token = "invalid-token";
        doThrow(new RecursoNoEncontradoException("Token de verificación inválido"))
                .when(authService).verificarCuenta(token);

        // Act & Assert
        mockMvc.perform(get("/api/auth/verify")
                        .param("token", token))
                .andExpect(status().isNotFound());

        verify(authService, times(1)).verificarCuenta(token);
    }

    // ==================== TESTS DE RESEND-VERIFICATION ====================

    @Test
    @DisplayName("POST /resend-verification - Reenvío exitoso retorna 200")
    void resendVerification_exitoso_retorna200() throws Exception {
        // Arrange
        String email = "test@example.com";
        doNothing().when(authService).reenviarEmailVerificacion(email);

        // Act & Assert
        mockMvc.perform(post("/api/auth/resend-verification")
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").exists());

        verify(authService, times(1)).reenviarEmailVerificacion(email);
    }

    @Test
    @DisplayName("POST /resend-verification - Usuario no encontrado retorna 404")
    void resendVerification_usuarioNoEncontrado_retorna404() throws Exception {
        // Arrange
        String email = "noexiste@example.com";
        doThrow(new RecursoNoEncontradoException("Usuario no encontrado"))
                .when(authService).reenviarEmailVerificacion(email);

        // Act & Assert
        mockMvc.perform(post("/api/auth/resend-verification")
                        .param("email", email))
                .andExpect(status().isNotFound());

        verify(authService, times(1)).reenviarEmailVerificacion(email);
    }

    // ==================== TESTS DE FORGOT-PASSWORD ====================

    @Test
    @DisplayName("POST /forgot-password - Solicitud exitosa retorna 200")
    void forgotPassword_exitoso_retorna200() throws Exception {
        // Arrange
        String email = "test@example.com";
        doNothing().when(authService).solicitarRecuperacionPassword(email);

        // Act & Assert
        mockMvc.perform(post("/api/auth/forgot-password")
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").exists());

        verify(authService, times(1)).solicitarRecuperacionPassword(email);
    }

    // ==================== TESTS DE RESET-PASSWORD ====================

    @Test
    @DisplayName("POST /reset-password - Reset exitoso retorna 200")
    void resetPassword_exitoso_retorna200() throws Exception {
        // Arrange
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("reset-token-123");
        request.setNuevaPassword("NewPassword456!");

        doNothing().when(authService).resetearPassword(any(ResetPasswordRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").exists());

        verify(authService, times(1)).resetearPassword(any(ResetPasswordRequest.class));
    }

    @Test
    @DisplayName("POST /reset-password - Token inválido retorna 404")
    void resetPassword_tokenInvalido_retorna404() throws Exception {
        // Arrange
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("invalid-token");
        request.setNuevaPassword("NewPassword456!");

        doThrow(new RecursoNoEncontradoException("Token de recuperación inválido"))
                .when(authService).resetearPassword(any(ResetPasswordRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(authService, times(1)).resetearPassword(any(ResetPasswordRequest.class));
    }

    @Test
    @DisplayName("POST /reset-password - Contraseña débil retorna 400")
    void resetPassword_passwordDebil_retorna400() throws Exception {
        // Arrange - Contraseña sin mayúscula
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("reset-token-123");
        request.setNuevaPassword("password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).resetearPassword(any(ResetPasswordRequest.class));
    }
}