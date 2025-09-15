package com.sanez.repository;

import com.sanez.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Rol, Long> {
    Optional<Rol> findByNombre(String nombre);
}
