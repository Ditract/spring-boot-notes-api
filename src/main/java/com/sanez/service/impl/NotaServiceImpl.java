package com.sanez.service.impl;

import com.sanez.dto.NotaDTO;
import com.sanez.exception.RecursoNoEncontradoException;
import com.sanez.mapper.NotaMapper;
import com.sanez.model.Nota;
import com.sanez.model.Usuario;
import com.sanez.repository.NotaRepository;
import com.sanez.repository.UsuarioRepository;
import com.sanez.service.NotaService;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotaServiceImpl implements NotaService {

    private final NotaRepository notaRepository;
    private final UsuarioRepository usuarioRepository;

    public NotaServiceImpl(NotaRepository notaRepository, UsuarioRepository usuarioRepository) {
        this.notaRepository = notaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public NotaDTO crearNota(NotaDTO notaDTO) {
        Long usuarioId = getUsuarioIdFromToken();
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Nota nota = NotaMapper.toEntity(notaDTO);
        nota.setUsuario(usuario);

        Nota notaGuardada = notaRepository.save(nota);
        return NotaMapper.toDTO(notaGuardada);
    }

    @Override
    public List<NotaDTO> obtenerNotasPorUsuario() {
        Long usuarioId = getUsuarioIdFromToken();
        List<Nota> notas = notaRepository.findByUsuarioId(usuarioId);

        if (!notas.isEmpty()) {
            return notas.stream()
                    .map(NotaMapper::toDTO)
                    .collect(Collectors.toList());
        }

        boolean usuarioExiste = usuarioRepository.existsById(usuarioId);
        if (!usuarioExiste) {
            throw new RecursoNoEncontradoException("Usuario con id " + usuarioId + " no existe");
        }

        return Collections.emptyList();
    }

    @Override
    public void eliminarNota(Long id) {
        Long usuarioId = getUsuarioIdFromToken();
        Nota nota = notaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Nota no encontrada"));
        if (!nota.getUsuario().getId().equals(usuarioId)) {
            throw new RuntimeException("No tienes permiso para eliminar esta nota");
        }
        notaRepository.deleteById(id);
    }

    private Long getUsuarioIdFromToken() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByEmail(username)
                .map(Usuario::getId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado en la base de datos"));
    }
}