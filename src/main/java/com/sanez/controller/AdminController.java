package com.sanez.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin", description = "Endpoints administrativos de prueba")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Operation(summary = "Dashboard admin", description = "Endpoint de prueba para verificar acceso de administrador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Acceso concedido"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos de administrador")
    })
    @GetMapping
    public String dashboard() {
        return "Hola admin";
    }
}