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

## Validación

```powershell
npm run lint
npm run build
```

El cliente HTTP común se encuentra en `src/services/api.ts`. No se incluyen
credenciales ni configuración con secretos en el repositorio.
