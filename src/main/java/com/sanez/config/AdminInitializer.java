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

    public AdminInitializer(UsuarioRepository usuarioRepository,
                            RoleRepository roleRepository,
                            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        // --- Manejo del rol ADMIN ---
        Optional<Rol> rolAdminOpt = roleRepository.findByNombre("ADMIN");
        Rol rolAdmin;
        if (rolAdminOpt.isPresent()) {
            rolAdmin = rolAdminOpt.get();
            System.out.println("🔹 Rol ADMIN ya existía.");
        } else {
            rolAdmin = new Rol();
            rolAdmin.setNombre("ADMIN");
            rolAdmin = roleRepository.save(rolAdmin);
            System.out.println("✅ Rol ADMIN creado.");
        }

        // --- Manejo del rol USER ---
        Optional<Rol> rolUserOpt = roleRepository.findByNombre("USER");
        Rol rolUser;
        if (rolUserOpt.isPresent()) {
            rolUser = rolUserOpt.get();
            System.out.println("🔹 Rol USER ya existía.");
        } else {
            rolUser = new Rol();
            rolUser.setNombre("USER");
            rolUser = roleRepository.save(rolUser);
            System.out.println("✅ Rol USER creado.");
        }

        // --- Crear usuario administrador si no existe ---
        String adminEmail = "admin@gmail.com";
        Optional<Usuario> adminExistente = usuarioRepository.findByEmail(adminEmail);

        if (adminExistente.isEmpty()) {
            Usuario admin = new Usuario();
            admin.setNombre("Admin");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("JojoLala890?"));

            Set<Rol> roles = new HashSet<>();
            roles.add(rolAdmin); // Solo ADMIN al inicio
            admin.setRoles(roles);

            usuarioRepository.save(admin);
            System.out.println("✅ Usuario administrador creado correctamente.");
        } else {
            System.out.println("🔹 Usuario administrador ya existe.");
        }
    }
}
