package com.sanez.controller;

import com.sanez.dto.NotaDTO;
import com.sanez.service.NotaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/notas")
public class NotaController {

    private final NotaService notaService;

    public NotaController(NotaService notaService) {
        this.notaService = notaService;
    }

    @PostMapping
    public ResponseEntity<NotaDTO> crearNota(@Valid @RequestBody NotaDTO notaDTO) {
        NotaDTO createdNota = notaService.crearNota(notaDTO);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdNota.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdNota);
    }

    @GetMapping
    public ResponseEntity<List<NotaDTO>> obtenerNotasPorUsuario() {
        return ResponseEntity.ok(notaService.obtenerNotasPorUsuario());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarNota(@PathVariable Long id) {
        notaService.eliminarNota(id);
        return ResponseEntity.noContent().build();
    }
}