package com.sanez.repository;

import com.sanez.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("UsuarioRepository - Tests de Integración")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        // Limpiar BD antes de cada test
        usuarioRepository.deleteAll();

        // Crear usuario de prueba
        usuario = new Usuario();
        usuario.setEmail("test@example.com");
        usuario.setPassword("encodedPassword");
        usuario.setEnabled(false);
        usuario.setVerificationToken("verification-token-123");
        usuario.setTokenExpiration(LocalDateTime.now().plusHours(24));
        usuario.setPasswordResetToken("reset-token-456");
        usuario.setPasswordResetTokenExpiration(LocalDateTime.now().plusHours(1));
    }

    // ==================== TESTS DE findByEmail ====================

    @Test
    @DisplayName("findByEmail - Email existente retorna usuario")
    void findByEmail_existente_retornaUsuario() {
        // Arrange
        entityManager.persistAndFlush(usuario);

        // Act
        Optional<Usuario> resultado = usuarioRepository.findByEmail("test@example.com");

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("test@example.com", resultado.get().getEmail());
        assertEquals("encodedPassword", resultado.get().getPassword());
    }

    @Test
    @DisplayName("findByEmail - Email no existente retorna vacío")
    void findByEmail_noExistente_retornaVacio() {
        // Arrange
        entityManager.persistAndFlush(usuario);

        // Act
        Optional<Usuario> resultado = usuarioRepository.findByEmail("noexiste@example.com");

        // Assert
        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("findByEmail - Email con mayúsculas/minúsculas diferentes retorna vacío")
    void findByEmail_caseSensitive_retornaVacio() {
        // Arrange
        entityManager.persistAndFlush(usuario);

        // Act
        Optional<Usuario> resultado = usuarioRepository.findByEmail("TEST@EXAMPLE.COM");

        // Assert
        assertFalse(resultado.isPresent());
    }

    // ==================== TESTS DE findByVerificationToken ====================

    @Test
    @DisplayName("findByVerificationToken - Token existente retorna usuario")
    void findByVerificationToken_existente_retornaUsuario() {
        // Arrange
        entityManager.persistAndFlush(usuario);

        // Act
        Optional<Usuario> resultado = usuarioRepository.findByVerificationToken("verification-token-123");

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("test@example.com", resultado.get().getEmail());
        assertEquals("verification-token-123", resultado.get().getVerificationToken());
    }

    @Test
    @DisplayName("findByVerificationToken - Token no existente retorna vacío")
    void findByVerificationToken_noExistente_retornaVacio() {
        // Arrange
        entityManager.persistAndFlush(usuario);

        // Act
        Optional<Usuario> resultado = usuarioRepository.findByVerificationToken("token-invalido");

        // Assert
        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("findByVerificationToken - Token null retorna vacío")
    void findByVerificationToken_null_retornaVacio() {
        // Arrange
        usuario.setVerificationToken(null);
        entityManager.persistAndFlush(usuario);

        // Act
        Optional<Usuario> resultado = usuarioRepository.findByVerificationToken("verification-token-123");

        // Assert
        assertFalse(resultado.isPresent());
    }

    // ==================== TESTS DE findByPasswordResetToken ====================

    @Test
    @DisplayName("findByPasswordResetToken - Token existente retorna usuario")
    void findByPasswordResetToken_existente_retornaUsuario() {
        // Arrange
        entityManager.persistAndFlush(usuario);

        // Act
        Optional<Usuario> resultado = usuarioRepository.findByPasswordResetToken("reset-token-456");

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("test@example.com", resultado.get().getEmail());
        assertEquals("reset-token-456", resultado.get().getPasswordResetToken());
    }

    @Test
    @DisplayName("findByPasswordResetToken - Token no existente retorna vacío")
    void findByPasswordResetToken_noExistente_retornaVacio() {
        // Arrange
        entityManager.persistAndFlush(usuario);

        // Act
        Optional<Usuario> resultado = usuarioRepository.findByPasswordResetToken("token-invalido");

        // Assert
        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("findByPasswordResetToken - Token null retorna vacío")
    void findByPasswordResetToken_null_retornaVacio() {
        // Arrange
        usuario.setPasswordResetToken(null);
        entityManager.persistAndFlush(usuario);

        // Act
        Optional<Usuario> resultado = usuarioRepository.findByPasswordResetToken("reset-token-456");

        // Assert
        assertFalse(resultado.isPresent());
    }

    // ==================== TESTS DE PERSISTENCIA ====================

    @Test
    @DisplayName("Guardar usuario - Persiste correctamente todos los campos")
    void guardarUsuario_persisteCorrectamente() {
        // Act
        Usuario guardado = usuarioRepository.save(usuario);
        entityManager.flush();
        entityManager.clear(); // Limpiar caché de primer nivel

        // Assert
        Optional<Usuario> encontrado = usuarioRepository.findById(guardado.getId());
        assertTrue(encontrado.isPresent());

        Usuario usuarioEncontrado = encontrado.get();
        assertEquals("test@example.com", usuarioEncontrado.getEmail());
        assertEquals("encodedPassword", usuarioEncontrado.getPassword());
        assertFalse(usuarioEncontrado.isEnabled());
        assertEquals("verification-token-123", usuarioEncontrado.getVerificationToken());
        assertEquals("reset-token-456", usuarioEncontrado.getPasswordResetToken());
        assertNotNull(usuarioEncontrado.getTokenExpiration());
        assertNotNull(usuarioEncontrado.getPasswordResetTokenExpiration());
    }

    @Test
    @DisplayName("Actualizar usuario - Persiste cambios correctamente")
    void actualizarUsuario_persisteCambios() {
        // Arrange
        Usuario guardado = entityManager.persistAndFlush(usuario);

        // Act
        guardado.setEnabled(true);
        guardado.setVerificationToken(null);
        guardado.setTokenExpiration(null);
        usuarioRepository.save(guardado);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<Usuario> actualizado = usuarioRepository.findById(guardado.getId());
        assertTrue(actualizado.isPresent());
        assertTrue(actualizado.get().isEnabled());
        assertNull(actualizado.get().getVerificationToken());
        assertNull(actualizado.get().getTokenExpiration());
    }

    @Test
    @DisplayName("Eliminar usuario - Remueve de la base de datos")
    void eliminarUsuario_removeDeLaBaseDeDatos() {
        // Arrange
        Usuario guardado = entityManager.persistAndFlush(usuario);
        Long id = guardado.getId();

        // Act
        usuarioRepository.delete(guardado);
        entityManager.flush();

        // Assert
        Optional<Usuario> eliminado = usuarioRepository.findById(id);
        assertFalse(eliminado.isPresent());
    }
}