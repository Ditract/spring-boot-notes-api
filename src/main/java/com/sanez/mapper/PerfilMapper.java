package com.sanez.mapper;

import com.sanez.dto.PerfilRequestDTO;
import com.sanez.dto.PerfilResponseDTO;
import com.sanez.model.Perfil;
import org.springframework.stereotype.Component;

@Component
public class PerfilMapper {


    public PerfilResponseDTO toResponseDTO(Perfil perfil){
        PerfilResponseDTO perfilResponseDTO = new PerfilResponseDTO();
        perfilResponseDTO.setId(perfil.getId());
        perfilResponseDTO.setNombre(perfil.getNombre());
        perfil.setNotasFavoritas(perfilResponseDTO.getNotasFavoritas());

        return perfilResponseDTO;
    }

    public void updateFromRequestDTO(PerfilRequestDTO perfilRequestDTO, Perfil perfil){
        if(perfilRequestDTO.getNombre() != null){
            perfil.setNombre(perfilRequestDTO.getNombre());
        }
        if(perfilRequestDTO.getNotasFavoritas() != null){
            perfil.setNotasFavoritas(perfilRequestDTO.getNotasFavoritas());
        }
    }



}
