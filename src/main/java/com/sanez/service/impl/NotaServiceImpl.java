package com.sanez.service.impl;

import com.sanez.dto.nota.NotaRequestDTO;
import com.sanez.dto.nota.NotaResponseDTO;
import com.sanez.dto.nota.NotaUpdateDTO;
import com.sanez.exception.AccesoNoAutorizadoException;
import com.sanez.exception.OperacionNoPermitidaException;
import com.sanez.exception.RecursoNoEncontradoException;
import com.sanez.mapper.NotaMapper;
import com.sanez.model.Nota;
import com.sanez.model.Usuario;
import com.sanez.repository.NotaRepository;
import com.sanez.repository.UsuarioRepository;
import com.sanez.security.service.CustomUserDetails;
import com.sanez.service.NotaService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotaServiceImpl implements NotaService {

    private final NotaRepository notaRepository;
    private final UsuarioRepository usuarioRepository;

    public NotaServiceImpl(NotaRepository notaRepository, UsuarioRepository usuarioRepository) {
        this.notaRepository = notaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Crear nota
    @Override
    @Transactional
    public NotaResponseDTO crearNota(NotaRequestDTO notaRequestDTO) {
        Long usuarioId = obtenerIdUsuarioAutenticado();

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Usuario con id " + usuarioId + " no encontrado"));

        Nota nota = NotaMapper.toEntity(notaRequestDTO);
        nota.setUsuario(usuario);

        Nota notaGuardada = notaRepository.save(nota);
        return NotaMapper.toResponseDTO(notaGuardada);
    }

    // Obtener notas por usuario (solo lectura)
    @Override
    @Transactional(readOnly = true)
    public List<NotaResponseDTO> obtenerNotasPorUsuario() {
        Long usuarioId = obtenerIdUsuarioAutenticado();

        return notaRepository.findByUsuarioId(usuarioId).stream()
                .map(NotaMapper::toResponseDTO)
                .toList();
    }

    // Editar nota
    @Override
    @Transactional
    public NotaResponseDTO editarNota(Long notaId, NotaUpdateDTO notaUpdateDTO) {
        Long usuarioId = obtenerIdUsuarioAutenticado();
        Nota nota = obtenerNotaValidaParaUsuario(notaId, usuarioId);

        NotaMapper.actualizarNotaDesdeDTO(nota, notaUpdateDTO);

        Nota notaActualizada = notaRepository.save(nota);
        return NotaMapper.toResponseDTO(notaActualizada);
    }

    // Eliminar nota
    @Override
    @Transactional
    public void eliminarNota(Long notaId) {
        Long usuarioId = obtenerIdUsuarioAutenticado();
        Nota nota = obtenerNotaValidaParaUsuario(notaId, usuarioId);

        notaRepository.delete(nota);
    }

    // ============================================
    // MÉTODOS PRIVADOS AUXILIARES
    // ============================================

    // Obtiene el ID del usuario autenticado o lanza 401
    private Long obtenerIdUsuarioAutenticado() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        throw new AccesoNoAutorizadoException("Usuario no autenticado");
    }

    // Verifica que la nota exista y pertenezca al usuario
    private Nota obtenerNotaValidaParaUsuario(Long notaId, Long usuarioId) {
        Nota nota = notaRepository.findById(notaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Nota no encontrada"));

        if (!nota.getUsuario().getId().equals(usuarioId)) {
            throw new OperacionNoPermitidaException("No tienes permiso para esta nota");
        }

        return nota;
    }
}
