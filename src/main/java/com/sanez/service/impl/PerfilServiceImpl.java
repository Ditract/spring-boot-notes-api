package com.sanez.service.impl;

import com.sanez.dto.PerfilRequestDTO;
import com.sanez.dto.PerfilResponseDTO;
import com.sanez.exception.RecursoNoEncontradoException;
import com.sanez.mapper.PerfilMapper;
import com.sanez.model.Perfil;
import com.sanez.model.Usuario;
import com.sanez.repository.NotaRepository;
import com.sanez.repository.PerfilRepository;
import com.sanez.repository.UsuarioRepository;
import com.sanez.service.PerfilService;
import org.springframework.stereotype.Service;

@Service
public class PerfilServiceImpl implements PerfilService {

    private final PerfilRepository perfilRepository;
    private final UsuarioRepository usuarioRepository;
    private final PerfilMapper perfilMapper;
    private final NotaRepository notaRepository;

    public PerfilServiceImpl(PerfilRepository perfilRepository, UsuarioRepository usuarioRepository,
                             PerfilMapper perfilMapper, NotaRepository notaRepository) {
        this.perfilRepository = perfilRepository;
        this.usuarioRepository = usuarioRepository;
        this.perfilMapper = perfilMapper;
        this.notaRepository = notaRepository;
    }


    @Override
    public PerfilResponseDTO obtenerPerfilPorUsuarioId(Long usuarioId) {

        Perfil perfil = validarYObtenerPerfilPorUsuarioId(usuarioId);

        return perfilMapper.toResponseDTO(perfil);
    }

    @Override
    public PerfilResponseDTO actualizarPerfil(Long usuarioId, PerfilRequestDTO perfilRequestDTO) {

        Perfil perfil = validarYObtenerPerfilPorUsuarioId(usuarioId);

        perfilMapper.updateFromRequestDTO(perfilRequestDTO, perfil);
        perfilRepository.save(perfil);

        return perfilMapper.toResponseDTO(perfil);
    }

    @Override
    public void agregarFavorita(Long usuarioId, Long notaId) {

        Perfil perfil = validarYObtenerPerfilPorUsuarioId(usuarioId);

        //Verificar si la nota existe y pertenece al usuario
        notaRepository.findById(notaId)
                .filter(nota -> nota.getUsuario().getId().equals(usuarioId))
                .orElseThrow(() -> new RecursoNoEncontradoException("Nota no encontrada o no pertenece al Usuario"));
        if (!perfil.getNotasFavoritas().contains(notaId)) {
            perfil.getNotasFavoritas().add(notaId);
            perfilRepository.save(perfil);
        }
    }

    @Override
    public void removerFavorita(Long usuarioId, Long notaId) {

        Perfil perfil = validarYObtenerPerfilPorUsuarioId(usuarioId);

        perfil.getNotasFavoritas().remove(notaId);
        perfilRepository.save(perfil);
    }




    // Obtiene el perfil del usuario o lanza excepción si no existe
    private Perfil validarYObtenerPerfilPorUsuarioId(Long usuarioId){
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Perfil perfil = usuario.getPerfil();
        if (perfil == null){
            throw new RecursoNoEncontradoException("Perfil no encontrado");
        }

        return perfil;
    }
}
