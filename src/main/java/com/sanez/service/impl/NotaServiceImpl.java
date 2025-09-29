package com.sanez.service.impl;

import com.sanez.dto.NotaDTO;
import com.sanez.exception.OperacionNoPermitidaException;
import com.sanez.exception.RecursoNoEncontradoException;
import com.sanez.mapper.NotaMapper;
import com.sanez.model.Nota;
import com.sanez.model.Usuario;
import com.sanez.repository.NotaRepository;
import com.sanez.repository.UsuarioRepository;
import com.sanez.security.service.CustomUserDetails;
import com.sanez.service.NotaService;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Transactional
public class NotaServiceImpl implements NotaService {

    private final NotaRepository notaRepository;
    private final UsuarioRepository usuarioRepository;

    public NotaServiceImpl(NotaRepository notaRepository, UsuarioRepository usuarioRepository) {
        this.notaRepository = notaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    //Crear nota
    @Override
    public NotaDTO crearNota(NotaDTO notaDTO) {
        Long usuarioId = obtenerIdUsuarioAutenticado();

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Usuario con id " + usuarioId + " no encontrado"));

        Nota nota = NotaMapper.toEntity(notaDTO);
        nota.setUsuario(usuario);

        Nota notaGuardada = notaRepository.save(nota);
        return NotaMapper.toDTO(notaGuardada);
    }

    //Obtener nota por usuario
    @Override
    public List<NotaDTO> obtenerNotasPorUsuario() {
        Long usuarioId = obtenerIdUsuarioAutenticado();

        return notaRepository.findByUsuarioId(usuarioId).stream()
                .map(NotaMapper::toDTO)
                .toList();
    }

    //Eliminar nota
    @Override
    public void eliminarNota(Long id) {
        Long usuarioId = obtenerIdUsuarioAutenticado();

        Nota nota = notaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Nota no encontrada"));

        if (!nota.getUsuario().getId().equals(usuarioId)) {
            throw new OperacionNoPermitidaException("No tienes permiso para eliminar esta nota");
        }
        notaRepository.delete(nota);
    }


    // ============================================
    // MÉTODOS PRIVADOS AUXILIARES
    // ============================================

    private Long obtenerIdUsuarioAutenticado() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails userDetails){
            return userDetails.getId();
        }
        throw new RecursoNoEncontradoException("Error al resolver la información del usuario");
    }
}