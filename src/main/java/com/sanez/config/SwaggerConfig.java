package com.sanez.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${app.base-url}")
    private String baseUrl;

    @Bean
    public OpenAPI notasAppOpenAPI() {

        // Esquema de seguridad
        final String securitySchemeName = "Bearer Authentication";

        // Servidor de desarrollo
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8080");
        devServer.setDescription("Servidor de Desarrollo");

        // Servidor de producción
        Server prodServer = new Server();
        prodServer.setUrl(baseUrl);
        prodServer.setDescription("Servidor de Producción");

        // Información de contacto
        Contact contact = new Contact();
        contact.setName("Héctor Sanez"); // Cambiar esto
        contact.setEmail("sanezh.contacto@gmail.com"); // Cambiar esto
        contact.setUrl("https://github.com/Ditract"); // Cambiar esto

        // Licencia
        License mitLicense = new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");

        // Información general de la API
        Info info = new Info()
                .title("Notas App API")
                .version("1.0.0")
                .contact(contact)
                .description("API REST para gestión de notas personales con autenticación JWT.\n\n" +
                        "**Funcionalidades principales:**\n" +
                        "- Registro y autenticación de usuarios con JWT\n" +
                        "- Verificación de cuenta por email\n" +
                        "- Recuperación de contraseña\n" +
                        "- CRUD completo de notas\n" +
                        "- Gestión de perfil de usuario\n" +
                        "- Sistema de notas favoritas\n" +
                        "- Panel de administración\n\n" +
                        "**Para usar endpoints protegidos:**\n" +
                        "1. Registrarse en `/api/auth/signup`\n" +
                        "2. Verificar cuenta con el token recibido por email\n" +
                        "3. Iniciar sesión en `/api/auth/signin` para obtener el JWT\n" +
                        "4. Hacer clic en el botón 'Authorize' arriba\n" +
                        "5. Ingresar el token en formato: `Bearer {tu-token}`")
                .license(mitLicense);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer))
                // Configuración de seguridad JWT
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Ingresa el token JWT obtenido al iniciar sesión")
                        )
                );
    }
}
