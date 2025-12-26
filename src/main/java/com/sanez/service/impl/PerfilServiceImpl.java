package com.sanez.service.impl;

import com.sanez.dto.perfil.CambiarPasswordRequest;
import com.sanez.dto.perfil.PerfilRequestDTO;
import com.sanez.dto.perfil.PerfilResponseDTO;
import com.sanez.exception.RecursoNoEncontradoException;
import com.sanez.mapper.PerfilMapper;
import com.sanez.model.Perfil;
import com.sanez.model.Usuario;
import com.sanez.repository.NotaRepository;
import com.sanez.repository.PerfilRepository;
import com.sanez.repository.UsuarioRepository;
import com.sanez.security.service.CustomUserDetails;
import com.sanez.service.PerfilService;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class PerfilServiceImpl implements PerfilService {

    private final PerfilRepository perfilRepository;
    private final UsuarioRepository usuarioRepository;
    private final PerfilMapper perfilMapper;
    private final NotaRepository notaRepository;
    private final PasswordEncoder passwordEncoder;

    public PerfilServiceImpl
            (PerfilRepository perfilRepository,
             UsuarioRepository usuarioRepository,
             PerfilMapper perfilMapper,
             NotaRepository notaRepository,
             PasswordEncoder passwordEncoder)
    {
        this.perfilRepository = perfilRepository;
        this.usuarioRepository = usuarioRepository;
        this.perfilMapper = perfilMapper;
        this.notaRepository = notaRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public PerfilResponseDTO obtenerMiPerfil() {
        Long usuarioId = obtenerIdUsuarioAutenticado();
        Perfil perfil = validarYObtenerPerfilPorUsuarioId(usuarioId);

        return perfilMapper.toResponseDTO(perfil);
    }

    @Override
    public PerfilResponseDTO actualizarMiPerfil(PerfilRequestDTO perfilRequestDTO) {
        Long usuarioId = obtenerIdUsuarioAutenticado();
        Perfil perfil = validarYObtenerPerfilPorUsuarioId(usuarioId);

        perfilMapper.updateFromRequestDTO(perfilRequestDTO, perfil);
        perfilRepository.save(perfil);

        return perfilMapper.toResponseDTO(perfil);
    }

    @Override
    public void agregarNotaFavorita(Long notaId) {

        Long usuarioId = obtenerIdUsuarioAutenticado();
        Perfil perfil = validarYObtenerPerfilPorUsuarioId(usuarioId);

        validarNotaPerteneceAlUsuario(notaId, usuarioId);

        if (!perfil.getNotasFavoritas().contains(notaId)) {
            perfil.getNotasFavoritas().add(notaId);
            perfilRepository.save(perfil);
        }
    }

    @Override
    public void removerNotaFavorita(Long notaId) {
        Long usuarioId = obtenerIdUsuarioAutenticado();
        Perfil perfil = validarYObtenerPerfilPorUsuarioId(usuarioId);

        validarNotaPerteneceAlUsuario(notaId, usuarioId);

        perfil.getNotasFavoritas().remove(notaId);
        perfilRepository.save(perfil);
    }

    @Override
    public void cambiarPassword(CambiarPasswordRequest request) {

        Long usuarioId = obtenerIdUsuarioAutenticado();

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        // Validar que la contraseña actual sea correcta
        if (!passwordEncoder.matches(request.getPasswordActual(), usuario.getPassword())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }

        // Validar que la nueva contraseña sea diferente a la actual
        if (passwordEncoder.matches(request.getNuevaPassword(), usuario.getPassword())) {
            throw new IllegalArgumentException("La nueva contraseña debe ser diferente a la actual");
        }

        // Encriptar y actualizar la contraseña
        usuario.setPassword(passwordEncoder.encode(request.getNuevaPassword()));
        usuarioRepository.save(usuario);
    }


    // ============================================
    // MÉTODOS PRIVADOS AUXILIARES
    // ============================================

    private void validarNotaPerteneceAlUsuario(Long notaId, Long usuarioId) {
        notaRepository.findById(notaId)
                .filter(nota -> nota.getUsuario().getId().equals(usuarioId))
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Nota no encontrada o no pertenece al usuario."));
    }

    private Perfil validarYObtenerPerfilPorUsuarioId(Long usuarioId){
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Perfil perfil = usuario.getPerfil();
        if (perfil == null){
            throw new RecursoNoEncontradoException("Perfil no encontrado");
        }

        return perfil;
    }

    private Long obtenerIdUsuarioAutenticado() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails userDetails){
            return userDetails.getId();
        }
        throw new RecursoNoEncontradoException("Error al resolver la información del usuario");
    }
}
