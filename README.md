# 📝 Notas App - API REST

> API REST para gestión de notas personales.

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Latest-blue.svg)](https://www.postgresql.org/)
[![JWT](https://img.shields.io/badge/JWT-Enabled-success.svg)](https://jwt.io/)
[![Tests](https://img.shields.io/badge/Tests-40%20Passing-success.svg)](/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📑 Contenido

- [Demo en Vivo](#-demo-en-vivo)
- [Descripción](#-descripción)
- [Capturas de Pantalla](#-capturas-de-pantalla)
- [Características Principales](#-características-principales)
- [Tech Stack](#️-tech-stack)
- [Arquitectura](#-arquitectura)
- [Funcionalidades Detalladas](#-funcionalidades-detalladas)
- [Instalación y Ejecución Local](#-instalación-y-ejecución-local)
- [Uso de la API](#-uso-de-la-api)
- [Próximas Mejoras](#-próximas-mejoras)

---

## 🚀 Demo en Vivo

- **🌐 API Backend:** [https://spring-boot-notes-api.onrender.com](https://spring-boot-notes-api.onrender.com)
- **💻 Frontend:** [https://ditract.github.io/notas-app-frontend/](https://ditract.github.io/notas-app-frontend/)
- **📚 Documentación API (Swagger):** [https://spring-boot-notes-api.onrender.com/swagger-ui/index.html](https://spring-boot-notes-api.onrender.com/swagger-ui/index.html)

> ⚠️ **Nota:** El backend está en Render (plan gratuito) y puede tardar ~30 segundos en despertar si no ha recibido tráfico recientemente. Por favor, ten paciencia en la primera carga.

---

## 📖 Descripción

API REST completa para gestión de notas personales que permite a los usuarios registrarse, autenticarse y administrar sus notas de forma segura. Implementa autenticación JWT, verificación de cuenta por email, recuperación de contraseña y un sistema de notas favoritas.

---

## 📸 Capturas de Pantalla

### Interfaz Principal
![Dashboard Principal](screenshots/dashboard.png)

### Autenticación
![Login](screenshots/login.png)
![Registro](screenshots/registro.png)

### Gestión de Notas
![Crear Nota](screenshots/crear-nota.png)
![Editar Nota](screenshots/editar-nota.png)

### Documentación API (Swagger)
![Swagger UI](screenshots/swagger.png)

---

## ✨ Características Principales

- 🔐 **Autenticación Completa**: JWT + Verificación por email + Recuperación de contraseña
- 📝 **CRUD de Notas**: Crear, leer, actualizar y eliminar notas personales
- ⭐ **Sistema de Favoritos**: Marca y organiza tus notas importantes
- 👤 **Gestión de Perfil**: Actualiza tu información y contraseña
- 🛡️ **Seguridad Robusta**: Spring Security + JWT + Validaciones de contraseña fuerte
- 📧 **Emails Transaccionales**: Verificación de cuenta y reset de contraseña
- 👨‍💼 **Panel de Administración**: Gestión de usuarios (CRUD sin frontend)
- 🧪 **Testing**: 40+ tests unitarios e integración
- 📚 **Documentación Swagger**: API documentada con OpenAPI 3

---

## 🛠️ Tech Stack

### Backend
- **Framework:** Spring Boot 3.4.2
- **Lenguaje:** Java 17
- **Seguridad:** Spring Security + JWT (jjwt 0.12.6)
- **Base de Datos:** PostgreSQL (Producción) / H2 (Desarrollo)
- **ORM:** Spring Data JPA + Hibernate
- **Validaciones:** Bean Validation (Hibernate Validator)
- **Email:** Spring Mail (Mailtrap dev / Outlook prod)
- **Documentación:** Springdoc OpenAPI 3 (Swagger)
- **Testing:** JUnit 5 + Mockito + Spring Boot Test

### Herramientas & Deployment
- **Build Tool:** Maven
- **Containerización:** Docker
- **Deploy:** Render (Backend) + GitHub Pages (Frontend)
- **Perfiles:** Dev (H2 + Mailtrap) / Prod (PostgreSQL + Outlook)
- **CORS:** Configurado para localhost y producción

---

## 📁 Arquitectura

El proyecto sigue una **arquitectura en capas** (Layered Architecture) para mantener una clara separación de responsabilidades:

```
📦 Notas App
├── 🎮 Controller Layer    → Endpoints REST
├── 💼 Service Layer       → Lógica de negocio
├── 🗄️  Repository Layer   → Acceso a datos
├── 📊 Model Layer         → Entidades JPA
├── 🔄 DTO Layer           → Transferencia de datos
├── 🛡️  Security Layer     → JWT + Configuración
└── ⚠️  Exception Layer    → Manejo global de errores
```

### Principios y Buenas Prácticas Aplicados
- ✅ Separación de responsabilidades (SoC)
- ✅ Inyección de dependencias
- ✅ DTOs para encapsulación
- ✅ Manejo centralizado de excepciones
- ✅ Validaciones en múltiples capas
- ✅ Código limpio y mantenible

---

## 🔑 Funcionalidades Detalladas

### 🔐 Autenticación y Seguridad
- Registro de usuarios con validación de email
- Verificación de cuenta por correo electrónico (token válido 24h)
- Inicio de sesión con JWT (token válido 1h)
- Recuperación de contraseña por email (token válido 1h)
- Validación de contraseña fuerte (min 8 chars, mayúscula, minúscula, número, carácter especial)
- Prevención de reutilización de contraseña anterior
- Roles de usuario (USER, ADMIN)

### 📝 Gestión de Notas
- Crear notas con título y contenido
- Listar todas las notas del usuario autenticado
- Editar notas propias (título y/o contenido)
- Eliminar notas propias
- Sistema de notas favoritas (agregar/remover)
- Validación de propiedad (solo puedes editar/eliminar tus notas)

### 👤 Perfil de Usuario
- Ver perfil con información personal
- Actualizar nombre de usuario
- Cambiar contraseña
- Ver lista de notas favoritas en el perfil

### 👨‍💼 Panel de Administración
- Crear usuarios con roles específicos
- Listar todos los usuarios
- Ver detalles de cualquier usuario
- Eliminar usuarios (elimina en cascada sus notas)

---

## 🚀 Instalación y Ejecución Local

### Prerrequisitos

- **Java 17** o superior
- **Maven 3.6+**
- **Cuenta de Mailtrap** (gratuita) - [Crear cuenta](https://mailtrap.io)

> **Nota:** No necesitas instalar una base de datos. El proyecto usa H2 (base de datos en memoria) en desarrollo.

---

### Paso 1: Clonar el Repositorio

```bash
git clone https://github.com/tu-usuario/notas-app-backend.git
cd notas-app-backend
```

---

### Paso 2: Configurar Mailtrap

1. Ve a [Mailtrap.io](https://mailtrap.io) y crea una cuenta gratuita
2. En tu inbox, ve a **SMTP Settings**
3. Copia las credenciales (username y password)
4. Abre `src/main/resources/application-dev.properties`
5. Actualiza estas líneas con tus credenciales:

```properties
spring.mail.username=TU_MAILTRAP_USERNAME
spring.mail.password=TU_MAILTRAP_PASSWORD
```

---

### Paso 3: Ejecutar la Aplicación

```bash
mvn clean install
mvn spring-boot:run
```

La aplicación estará disponible en:
- **API:** `http://localhost:8080`
- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
- **H2 Console:** `http://localhost:8080/h2-console`
    - JDBC URL: `jdbc:h2:mem:testdb`
    - Username: `sa`
    - Password: *(dejar vacío)*

---

### Paso 4: Usuario Administrador

Al iniciar la aplicación, se crea automáticamente un usuario administrador:

- **Email:** `admin@gmail.com`
- **Password:** `JojoLala890?`

Puedes usar estas credenciales para:
- Probar endpoints de administrador en Swagger
- Gestionar usuarios desde `/api/usuarios`
- Acceder al panel admin en `/api/admin`

---

### Ejecutar Tests

```bash
# Todos los tests (40 tests)
mvn test

# Tests de una clase específica
mvn test -Dtest=AuthServiceTest

# Con reporte de cobertura
mvn test jacoco:report
```

---

### Dockerización (Opcional)

El proyecto incluye un `Dockerfile` para deployment:

```bash
# Construir imagen
docker build -t notas-app .

# Ejecutar contenedor
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e MAIL_USERNAME=tu-username \
  -e MAIL_PASSWORD=tu-password \
  notas-app
```

---

## 📚 Uso de la API

### Flujo Básico

1. **Registrarse**: `POST /api/auth/signup`
   ```json
   {
     "email": "usuario@example.com",
     "password": "Password123!"
   }
   ```

2. **Verificar email**: Revisa tu inbox de Mailtrap y copia el token

3. **Verificar cuenta**: `GET /api/auth/verify?token={TOKEN}`

4. **Iniciar sesión**: `POST /api/auth/signin`
   ```json
   {
     "email": "usuario@example.com",
     "password": "Password123!"
   }
   ```

5. **Usar el JWT**: En Swagger, haz clic en **Authorize** y pega el token

### Documentación Completa

Toda la documentación de endpoints está en **Swagger UI**:
- **Local:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **Producción:** [https://spring-boot-notes-api.onrender.com/swagger-ui/index.html](https://spring-boot-notes-api.onrender.com/swagger-ui/index.html)

---

## 🔧 Perfiles de Spring

El proyecto usa perfiles para diferentes entornos:

| Perfil | Base de Datos | Email | Uso |
|--------|---------------|-------|-----|
| **dev** | H2 (en memoria) | Mailtrap | Desarrollo local |
| **prod** | PostgreSQL | Outlook | Producción (Render) |

Por defecto, el perfil **dev** está activo. No necesitas cambiar nada para desarrollo local.

---

## 🚀 Próximas Mejoras

- [ ] Paginación de notas
- [ ] Búsqueda y filtros de notas
- [ ] Categorías/etiquetas para notas
- [ ] Backup de notas
- [ ] Refresh tokens
- [ ] Rate limiting
- [ ] Exportar notas (PDF, TXT)

---


📫 **Contacto:** [LinkedIn](https://linkedin.com/in/tu-perfil)