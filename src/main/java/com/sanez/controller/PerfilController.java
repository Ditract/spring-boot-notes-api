package com.sanez.controller;

import com.sanez.dto.perfil.PerfilRequestDTO;
import com.sanez.dto.perfil.PerfilResponseDTO;
import com.sanez.service.PerfilService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Perfil", description = "Gestión del perfil del usuario autenticado")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/perfiles")
public class PerfilController {

    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @Operation(summary = "Obtener mi perfil", description = "Retorna el perfil del usuario autenticado con sus notas favoritas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil obtenido exitosamente",
                    content = @Content(schema = @Schema(implementation = PerfilResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/mi-perfil")
    public ResponseEntity<PerfilResponseDTO> obtenerMiPerfil() {
        return ResponseEntity.ok(perfilService.obtenerMiPerfil());
    }

    @Operation(summary = "Actualizar mi perfil", description = "Actualiza el nombre del perfil del usuario autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = PerfilResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PutMapping("/mi-perfil")
    public ResponseEntity<PerfilResponseDTO> actualizarMiPerfil(
            @Valid @RequestBody PerfilRequestDTO perfilRequestDTO) {
        return ResponseEntity.ok(perfilService.actualizarMiPerfil(perfilRequestDTO));
    }

    @Operation(summary = "Agregar nota a favoritos", description = "Marca una nota propia como favorita")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Nota agregada a favoritos"),
            @ApiResponse(responseCode = "404", description = "Nota no encontrada"),
            @ApiResponse(responseCode = "403", description = "No eres el propietario de esta nota"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PostMapping("/favoritas/{notaId}")
    public ResponseEntity<Void> agregarNotaFavorita(@PathVariable Long notaId) {
        perfilService.agregarNotaFavorita(notaId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Remover nota de favoritos", description = "Quita una nota de la lista de favoritos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Nota removida de favoritos"),
            @ApiResponse(responseCode = "404", description = "Nota no encontrada"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @DeleteMapping("/favoritas/{notaId}")
    public ResponseEntity<Void> removerNotaFavorita(@PathVariable Long notaId) {
        perfilService.removerNotaFavorita(notaId);
        return ResponseEntity.noContent().build();
    }
}