package com.sanez.service.impl;

import com.sanez.dto.NotaDTO;
import com.sanez.mapper.NotaMapper;
import com.sanez.model.Nota;
import com.sanez.model.Usuario;
import com.sanez.repository.NotaRepository;
import com.sanez.repository.UsuarioRepository;
import com.sanez.service.NotaService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

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
    public NotaDTO crearNota(Long usuarioId, NotaDTO notaDTO) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Nota nota = NotaMapper.toEntity(notaDTO);
        nota.setUsuario(usuario);

        Nota notaGuardada = notaRepository.save(nota);
        return NotaMapper.toDTO(notaGuardada);
    }

    @Override
    public List<NotaDTO> obtenerNotasPorUsuario(Long usuarioId) {
        return notaRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(NotaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminarNota(Long id) {
        notaRepository.deleteById(id);
    }
}
