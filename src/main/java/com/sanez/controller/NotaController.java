package com.sanez.controller;

import com.sanez.dto.NotaDTO;
import com.sanez.service.NotaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notas")
public class NotaController {

    private final NotaService notaService;

    public NotaController(NotaService notaService) {
        this.notaService = notaService;
    }

    @PostMapping("/usuario/{usuarioId}")
    public ResponseEntity<NotaDTO> crearNota(@PathVariable Long usuarioId, @Valid @RequestBody NotaDTO notaDTO) {
        return ResponseEntity.ok(notaService.crearNota(usuarioId, notaDTO));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<NotaDTO>> obtenerNotasPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(notaService.obtenerNotasPorUsuario(usuarioId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarNota(@PathVariable Long id) {
        notaService.eliminarNota(id);
        return ResponseEntity.noContent().build();
    }
}
