package com.sanez.controller;

import com.sanez.dto.perfil.PerfilRequestDTO;
import com.sanez.dto.perfil.PerfilResponseDTO;
import com.sanez.service.PerfilService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/perfiles")
public class PerfilController {

    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    // ============================================
    // ENDPOINTS PARA USUARIOS AUTENTICADOS
    // ============================================

    @GetMapping("/mi-perfil")
    public ResponseEntity<PerfilResponseDTO> obtenerMiPerfil() {
        return ResponseEntity.ok(perfilService.obtenerMiPerfil());
    }

    @PutMapping("/mi-perfil")
    public ResponseEntity<PerfilResponseDTO> actualizarMiPerfil(
            @Valid @RequestBody PerfilRequestDTO perfilRequestDTO) {
        return ResponseEntity.ok(perfilService.actualizarMiPerfil(perfilRequestDTO));
    }

    @PostMapping("/favoritas/{notaId}")
    public ResponseEntity<Void> agregarNotaFavorita(@PathVariable Long notaId) {
        perfilService.agregarNotaFavorita(notaId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/favoritas/{notaId}")
    public ResponseEntity<Void> removerNotaFavorita(@PathVariable Long notaId) {
        perfilService.removerNotaFavorita(notaId);
        return ResponseEntity.noContent().build();
    }
}