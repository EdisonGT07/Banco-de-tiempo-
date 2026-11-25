# Frontend — Pasaporte de Saberes

Interfaz web construida con React, TypeScript y Vite.

## Requisitos

- Node.js 22.12 o posterior
- npm
- Backend Spring Boot disponible en `http://localhost:8080` para consumir la API

## Desarrollo local

```powershell
npm install
Copy-Item .env.example .env
npm run dev
```

Vite inicia la aplicación en `http://localhost:5173`. Las solicitudes a `/api/*`
se redirigen al backend configurado en `VITE_API_PROXY_TARGET` (por defecto,
`http://localhost:8080`). El cliente de API usa `/api/v1` como prefijo por defecto;
se puede cambiar con `VITE_API_BASE_URL`.

## Cuentas

La sección **Mi cuenta** permite registrar una persona, iniciar sesión y consultar
el perfil actual. El registro requiere nombre, correo, contraseña, documento,
fecha de nacimiento y aceptar el tratamiento de datos; la zona y el estrato son
opcionales, de acuerdo con el contrato de la API.

El inicio de sesión obtiene un JWT del backend y consulta el perfil en
`/api/v1/cuentas/me`. El token se mantiene en `sessionStorage` para la pestaña
actual y se elimina al cerrar sesión. La API actual emite tokens de corta
duración y no ofrece renovación automática; al vencer, se debe iniciar sesión
de nuevo.

Al iniciar sesión, el tablero consume:

- `GET /api/v1/agora/ofertas` para buscar saberes y paginar resultados.
- `GET /api/v1/agora/sugerencias` para mostrar posibles coincidencias.
- `POST /api/v1/catalogo/solicitudes-aprendizaje` para registrar interés.

El frontend solo se comunica con la API Spring; es el backend quien lee y escribe
en Supabase. La publicación de ofertas queda pendiente de un endpoint para
consultar habilidades e identificadores válidos.

## Otros flujos disponibles

- Consulta y retiro de la billetera.
- Inscripción, cancelación, handshake, confirmación de fin y reporte de sesiones.
- Calificación de sesiones completadas.
- Consulta del pasaporte, rangos y descarga de certificados PDF.
- Publicación de ofertas cuando se conoce el identificador de una habilidad.
- Impacto, revisión de reportes y desbloqueo para cuentas administradoras.

## Validación

```powershell
npm run lint
npm run build
```

El cliente HTTP común se encuentra en `src/services/api.ts`. Las llamadas
protegidas adjuntan el token como `Authorization: Bearer`. No se incluyen
credenciales ni configuración con secretos en el repositorio.
