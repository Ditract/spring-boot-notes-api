package com.sanez.config;



import com.sanez.model.Rol;
import com.sanez.model.Usuario;
import com.sanez.repository.RoleRepository;
import com.sanez.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(UsuarioRepository usuarioRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public void run(String... args) {
        Optional<Usuario> adminExistente = usuarioRepository.findByEmail("admin@sanez.com");

        if (adminExistente.isEmpty()) {
            Rol rolAdmin = roleRepository.findByNombre("ADMIN")
                    .orElseGet(() -> {
                        Rol nuevoRol = new Rol();
                        nuevoRol.setNombre("ADMIN");
                        return roleRepository.save(nuevoRol);
                    });

            Usuario admin = new Usuario();
            admin.setNombre("Admin");
            admin.setEmail("admin@gmail.com");
            admin.setPassword(passwordEncoder.encode("JojoLala890?")); // La contraseña se cifra
            Set<Rol> roles = new HashSet<>();
            roles.add(rolAdmin);
            admin.setRoles(roles);

            usuarioRepository.save(admin);
            System.out.println("✅ Usuario administrador creado correctamente.");
        } else {
            System.out.println("🔹 Usuario administrador ya existe.");
        }
    }
}
