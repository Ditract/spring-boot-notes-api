package com.sanez.controller;

import com.sanez.dto.nota.NotaRequestDTO;
import com.sanez.dto.nota.NotaResponseDTO;
import com.sanez.dto.nota.NotaUpdateDTO;
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
    public ResponseEntity<NotaResponseDTO> crearNota(@Valid @RequestBody NotaRequestDTO notaRequestDTO) {
        NotaResponseDTO notaCreada = notaService.crearNota(notaRequestDTO);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(notaCreada.getId())
                .toUri();
        return ResponseEntity.created(location).body(notaCreada);
    }

    @GetMapping
    public ResponseEntity<List<NotaResponseDTO>> obtenerNotasPorUsuario() {
        return ResponseEntity.ok(notaService.obtenerNotasPorUsuario());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<NotaResponseDTO> editarNota(@PathVariable Long id,
                                                      @Valid @RequestBody NotaUpdateDTO notaUpdateDTO) {
        NotaResponseDTO notaActualizada = notaService.editarNota(id, notaUpdateDTO);
        return ResponseEntity.ok(notaActualizada);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarNota(@PathVariable Long id) {
        notaService.eliminarNota(id);
        return ResponseEntity.noContent().build();
    }
}