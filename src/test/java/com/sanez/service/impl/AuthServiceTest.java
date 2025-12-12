package com.sanez.service.impl;

import com.sanez.dto.auth.ResetPasswordRequest;
import com.sanez.dto.usuario.UsuarioRequestDTO;
import com.sanez.dto.usuario.UsuarioResponseDTO;
import com.sanez.exception.EmailYaRegistradoException;
import com.sanez.exception.RecursoNoEncontradoException;
import com.sanez.model.Rol;
import com.sanez.model.Usuario;
import com.sanez.repository.RoleRepository;
import com.sanez.repository.UsuarioRepository;
import com.sanez.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Tests Unitarios")
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthServiceImpl authService;

    private UsuarioRequestDTO usuarioRequestDTO;
    private Usuario usuario;
    private Rol rolUser;

    @BeforeEach
    void setUp() {
        // Datos de prueba
        usuarioRequestDTO = new UsuarioRequestDTO();
        usuarioRequestDTO.setEmail("test@example.com");
        usuarioRequestDTO.setPassword("Password123");

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("test@example.com");
        usuario.setPassword("encodedPassword");
        usuario.setEnabled(false);

        rolUser = new Rol();
        rolUser.setId(1L);
        rolUser.setNombre("USER");
    }

    // ==================== TESTS DE REGISTRO ====================

    @Test
    @DisplayName("Registrar usuario - Exitoso")
    void registrarUsuario_exitoso() {
        // Arrange
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findByNombre("USER")).thenReturn(Optional.of(rolUser));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        doNothing().when(emailService).enviarEmailVerificacion(anyString(), anyString());

        // Act
        UsuarioResponseDTO resultado = authService.registrarUsuario(usuarioRequestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals("test@example.com", resultado.getEmail());
        verify(usuarioRepository, times(1)).findByEmail("test@example.com");
        verify(passwordEncoder, times(1)).encode("Password123");
        verify(roleRepository, times(1)).findByNombre("USER");
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
        verify(emailService, times(1)).enviarEmailVerificacion(anyString(), anyString());
    }

    @Test
    @DisplayName("Registrar usuario - Email duplicado lanza excepción")
    void registrarUsuario_emailDuplicado_lanzaExcepcion() {
        // Arrange
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(usuario));

        // Act & Assert
        assertThrows(EmailYaRegistradoException.class, () -> {
            authService.registrarUsuario(usuarioRequestDTO);
        });

        verify(usuarioRepository, times(1)).findByEmail("test@example.com");
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(emailService, never()).enviarEmailVerificacion(anyString(), anyString());
    }

    @Test
    @DisplayName("Registrar usuario - Rol USER no encontrado lanza excepción")
    void registrarUsuario_rolNoEncontrado_lanzaExcepcion() {
        // Arrange
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findByNombre("USER")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNoEncontradoException.class, () -> {
            authService.registrarUsuario(usuarioRequestDTO);
        });

        verify(roleRepository, times(1)).findByNombre("USER");
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // ==================== TESTS DE VERIFICACIÓN ====================

    @Test
    @DisplayName("Verificar cuenta - Token válido activa usuario")
    void verificarCuenta_tokenValido_activaUsuario() {
        // Arrange
        String token = "valid-token";
        usuario.setVerificationToken(token);
        usuario.setTokenExpiration(LocalDateTime.now().plusHours(1));
        usuario.setEnabled(false);

        when(usuarioRepository.findByVerificationToken(token)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Act
        authService.verificarCuenta(token);

        // Assert
        verify(usuarioRepository, times(1)).findByVerificationToken(token);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
        assertTrue(usuario.isEnabled());
        assertNull(usuario.getVerificationToken());
        assertNull(usuario.getTokenExpiration());
    }

    @Test
    @DisplayName("Verificar cuenta - Token inválido lanza excepción")
    void verificarCuenta_tokenInvalido_lanzaExcepcion() {
        // Arrange
        String token = "invalid-token";
        when(usuarioRepository.findByVerificationToken(token)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNoEncontradoException.class, () -> {
            authService.verificarCuenta(token);
        });

        verify(usuarioRepository, times(1)).findByVerificationToken(token);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Verificar cuenta - Token expirado lanza excepción")
    void verificarCuenta_tokenExpirado_lanzaExcepcion() {
        // Arrange
        String token = "expired-token";
        usuario.setVerificationToken(token);
        usuario.setTokenExpiration(LocalDateTime.now().minusHours(1)); // Token expirado

        when(usuarioRepository.findByVerificationToken(token)).thenReturn(Optional.of(usuario));

        // Act & Assert
        assertThrows(RecursoNoEncontradoException.class, () -> {
            authService.verificarCuenta(token);
        });

        verify(usuarioRepository, times(1)).findByVerificationToken(token);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // ==================== TESTS DE REENVÍO ====================

    @Test
    @DisplayName("Reenviar verificación - Usuario no verificado exitoso")
    void reenviarEmailVerificacion_usuarioNoVerificado_exitoso() {
        // Arrange
        usuario.setEnabled(false);
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        doNothing().when(emailService).enviarEmailVerificacion(anyString(), anyString());

        // Act
        authService.reenviarEmailVerificacion("test@example.com");

        // Assert
        verify(usuarioRepository, times(1)).findByEmail("test@example.com");
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
        verify(emailService, times(1)).enviarEmailVerificacion(anyString(), anyString());
        assertNotNull(usuario.getVerificationToken());
    }

    @Test
    @DisplayName("Reenviar verificación - Usuario ya verificado lanza excepción")
    void reenviarEmailVerificacion_usuarioVerificado_lanzaExcepcion() {
        // Arrange
        usuario.setEnabled(true);
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(usuario));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            authService.reenviarEmailVerificacion("test@example.com");
        });

        verify(usuarioRepository, times(1)).findByEmail("test@example.com");
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(emailService, never()).enviarEmailVerificacion(anyString(), anyString());
    }

    // ==================== TESTS DE RECUPERACIÓN DE PASSWORD ====================

    @Test
    @DisplayName("Solicitar recuperación - Cuenta verificada exitoso")
    void solicitarRecuperacionPassword_cuentaVerificada_exitoso() {
        // Arrange
        usuario.setEnabled(true);
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        doNothing().when(emailService).enviarEmailRecuperacionPassword(anyString(), anyString());

        // Act
        authService.solicitarRecuperacionPassword("test@example.com");

        // Assert
        verify(usuarioRepository, times(1)).findByEmail("test@example.com");
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
        verify(emailService, times(1)).enviarEmailRecuperacionPassword(anyString(), anyString());
        assertNotNull(usuario.getPasswordResetToken());
        assertNotNull(usuario.getPasswordResetTokenExpiration());
    }

    @Test
    @DisplayName("Solicitar recuperación - Cuenta no verificada lanza excepción")
    void solicitarRecuperacionPassword_cuentaNoVerificada_lanzaExcepcion() {
        // Arrange
        usuario.setEnabled(false);
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(usuario));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            authService.solicitarRecuperacionPassword("test@example.com");
        });

        verify(usuarioRepository, times(1)).findByEmail("test@example.com");
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(emailService, never()).enviarEmailRecuperacionPassword(anyString(), anyString());
    }

    @Test
    @DisplayName("Solicitar recuperación - Usuario no encontrado lanza excepción")
    void solicitarRecuperacionPassword_usuarioNoEncontrado_lanzaExcepcion() {
        // Arrange
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNoEncontradoException.class, () -> {
            authService.solicitarRecuperacionPassword("noexiste@example.com");
        });

        verify(usuarioRepository, times(1)).findByEmail("noexiste@example.com");
        verify(emailService, never()).enviarEmailRecuperacionPassword(anyString(), anyString());
    }

    // ==================== TESTS DE RESET PASSWORD ====================

    @Test
    @DisplayName("Resetear password - Token válido actualiza password")
    void resetearPassword_tokenValido_actualizaPassword() {
        // Arrange
        String token = "reset-token";
        String nuevaPassword = "NewPassword456";
        String oldPasswordEncoded = "oldPasswordEncoded";

        usuario.setPasswordResetToken(token);
        usuario.setPasswordResetTokenExpiration(LocalDateTime.now().plusMinutes(30));
        usuario.setPassword(oldPasswordEncoded);

        ResetPasswordRequest request = new ResetPasswordRequest(token, nuevaPassword);

        when(usuarioRepository.findByPasswordResetToken(token)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(nuevaPassword, oldPasswordEncoded)).thenReturn(false);
        when(passwordEncoder.encode(nuevaPassword)).thenReturn("newPasswordEncoded");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Act
        authService.resetearPassword(request);

        // Assert
        verify(usuarioRepository, times(1)).findByPasswordResetToken(token);
        verify(passwordEncoder, times(1)).matches(nuevaPassword, oldPasswordEncoded);
        verify(passwordEncoder, times(1)).encode(nuevaPassword);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
        assertNull(usuario.getPasswordResetToken());
        assertNull(usuario.getPasswordResetTokenExpiration());
    }

    @Test
    @DisplayName("Resetear password - Token expirado lanza excepción")
    void resetearPassword_tokenExpirado_lanzaExcepcion() {
        // Arrange
        String token = "expired-reset-token";
        usuario.setPasswordResetToken(token);
        usuario.setPasswordResetTokenExpiration(LocalDateTime.now().minusMinutes(30));

        ResetPasswordRequest request = new ResetPasswordRequest(token, "NewPassword456");

        when(usuarioRepository.findByPasswordResetToken(token)).thenReturn(Optional.of(usuario));

        // Act & Assert
        assertThrows(RecursoNoEncontradoException.class, () -> {
            authService.resetearPassword(request);
        });

        verify(usuarioRepository, times(1)).findByPasswordResetToken(token);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Resetear password - Misma password anterior lanza excepción")
    void resetearPassword_mismaPasswordAnterior_lanzaExcepcion() {
        // Arrange
        String token = "reset-token";
        String mismaPassword = "SamePassword123";
        String passwordEncoded = "encodedPassword";

        usuario.setPasswordResetToken(token);
        usuario.setPasswordResetTokenExpiration(LocalDateTime.now().plusMinutes(30));
        usuario.setPassword(passwordEncoded);

        ResetPasswordRequest request = new ResetPasswordRequest(token, mismaPassword);

        when(usuarioRepository.findByPasswordResetToken(token)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(mismaPassword, passwordEncoded)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authService.resetearPassword(request);
        });

        verify(usuarioRepository, times(1)).findByPasswordResetToken(token);
        verify(passwordEncoder, times(1)).matches(mismaPassword, passwordEncoded);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Resetear password - Token inválido lanza excepción")
    void resetearPassword_tokenInvalido_lanzaExcepcion() {
        // Arrange
        String token = "invalid-token";
        ResetPasswordRequest request = new ResetPasswordRequest(token, "NewPassword456");

        when(usuarioRepository.findByPasswordResetToken(token)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNoEncontradoException.class, () -> {
            authService.resetearPassword(request);
        });

        verify(usuarioRepository, times(1)).findByPasswordResetToken(token);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}