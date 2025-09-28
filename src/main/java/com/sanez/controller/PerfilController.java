package com.sanez.controller;

import com.sanez.dto.PerfilRequestDTO;
import com.sanez.dto.PerfilResponseDTO;
import com.sanez.service.PerfilService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/perfiles")
public class PerfilController {

    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping("/{usuarioId}")
    public ResponseEntity<PerfilResponseDTO> obtenerPerfil(@PathVariable Long usuarioId){
        PerfilResponseDTO perfilResponseDTO = perfilService.obtenerPerfilPorUsuarioId(usuarioId);
        return ResponseEntity.ok(perfilResponseDTO);
    }

    @PutMapping("/{usuarioId}")
    public ResponseEntity<PerfilResponseDTO> updatePerfil(@PathVariable Long usuarioId,
                                                          @Valid @RequestBody PerfilRequestDTO requestDTO) {
        PerfilResponseDTO actualizarPerfil = perfilService.actualizarPerfil(usuarioId, requestDTO);
        return ResponseEntity.ok(actualizarPerfil);
    }
}
