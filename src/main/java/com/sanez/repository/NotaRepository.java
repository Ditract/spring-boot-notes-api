package com.sanez.repository;

import com.sanez.model.Nota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotaRepository extends JpaRepository<Nota, Long> {
    List<Nota> findByUsuarioId(Long usuarioId);
}
