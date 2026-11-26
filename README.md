# Pasaporte de Saberes

Proyecto full-stack: backend en Spring Boot (Java) y frontend en React + Vite.

---

## Requisitos previos

| Herramienta | Versión mínima |
|-------------|---------------|
| Java JDK    | 21            |
| Node.js     | 22.12         |
| npm         | incluido con Node |
| Maven       | no hace falta instalarlo, el proyecto trae `mvnw` |

---

## Variables de entorno

Deben estar configuradas en las **variables de entorno del sistema** (o de usuario) de Windows.  
En PowerShell, para configurarlas de forma permanente:

```powershell
[System.Environment]::SetEnvironmentVariable("NOMBRE", "valor", "User")
```

### Base de datos (Supabase)

| Variable             | Descripción                              |
|----------------------|------------------------------------------|
| `SUPABASE_DB_URL`    | URL JDBC de la base de datos PostgreSQL  |
| `SUPABASE_DB_USER`   | Usuario de la base de datos              |
| `SUPABASE_DB_PASSWORD` | Contraseña de la base de datos         |

### Seguridad

| Variable             | Descripción                                                        |
|----------------------|--------------------------------------------------------------------|
| `JWT_SECRET_BASE64`  | Clave secreta para firmar tokens JWT (Base64, mínimo 64 bytes)     |
| `DOC_PEPPER`         | Secreto HMAC para documentos (Base64, 32 bytes)                    |
| `FIRMA_CLAVE`        | Secreto para firma de credenciales (Base64, 32 bytes)              |

> ⚠️ Estos tres secretos deben ser **iguales en todas las máquinas del equipo**.
> Compartirlos solo por canales privados; nunca subirlos a un repositorio público.

#### Valores del equipo (repo privado — no compartir fuera del equipo)

```powershell
[System.Environment]::SetEnvironmentVariable("JWT_SECRET_BASE64", "***REMOVED***", "User")
[System.Environment]::SetEnvironmentVariable("DOC_PEPPER", "***REMOVED***", "User")
[System.Environment]::SetEnvironmentVariable("FIRMA_CLAVE", "***REMOVED***", "User")
```

### Parámetros de negocio

| Variable                                      | Ejemplo   | Descripción                                      |
|-----------------------------------------------|-----------|--------------------------------------------------|
| `PASAPORTE_BONO_INICIAL`                      | `5.00`    | Horas de bono al registrarse                     |
| `PASAPORTE_VIGENCIA_HANDSHAKE`                | `PT30M`   | Tiempo máximo para confirmar inicio de sesión    |
| `PASAPORTE_RATING_MINIMO_VALIDACION`          | `3.0`     | Rating mínimo para validar un pasaporte          |
| `PASAPORTE_APRENDICES_MINIMOS_VALIDACION`     | `3`       | Aprendices mínimos para validar un pasaporte     |

### CORS

| Variable        | Valor para desarrollo local                         |
|-----------------|-----------------------------------------------------|
| `CORS_ORIGINS`  | `http://localhost:5173,http://localhost:5174`        |

### Opcionales (funcionalidades extra)

| Variable                        | Descripción                                 |
|---------------------------------|---------------------------------------------|
| `SUPABASE_URL`                  | URL base del proyecto Supabase (storage)    |
| `SUPABASE_STORAGE_BUCKET`       | Bucket para evidencias (default: evidencias)|
| `SUPABASE_SERVICE_ROLE_KEY`     | Service role key de Supabase                |
| `SMTP_HOST`                     | Servidor SMTP para notificaciones           |
| `SMTP_PORT`                     | Puerto SMTP (default: 587)                  |
| `SMTP_USERNAME`                 | Usuario SMTP                                |
| `SMTP_PASSWORD`                 | Contraseña SMTP                             |

---

## Correr el proyecto

Desde la raíz del proyecto, abre PowerShell y ejecuta:

```powershell
.\start.ps1
```

Esto abre dos ventanas:
- **Backend** en `http://localhost:8080` (listo en ~20 segundos)
- **Frontend** en `http://localhost:5173`

> Si PowerShell bloquea la ejecución de scripts, corre esto una sola vez:
> ```powershell
> Set-ExecutionPolicy -Scope CurrentUser RemoteSigned
> ```

### O manualmente (una terminal por proceso)

```powershell
# Backend
cd pasaporte-saberes
.\mvnw.cmd spring-boot:run

# Frontend (otra terminal)
cd frontend
npm install      # solo la primera vez
npm run dev
```

---

## Swagger UI

Con el backend corriendo, la documentación de la API está en:

```
http://localhost:8080/swagger-ui.html
```
