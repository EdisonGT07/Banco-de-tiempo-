# Levanta backend (Spring Boot) y frontend (Vite) de una sola vez.
# Uso:  .\start.ps1      (desde la raiz del proyecto)

$root = $PSScriptRoot

# Ventana 1: backend
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$root\pasaporte-saberes'; .\mvnw.cmd spring-boot:run"

# Ventana 2: frontend
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$root\frontend'; npm run dev"

Write-Host "Backend:  http://localhost:8080  (listo en ~20 segundos)"
Write-Host "Frontend: http://localhost:5173  (o 5174 si el 5173 esta ocupado)"
