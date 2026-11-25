import { apiRequest } from './api'

export type Zona = 'URBANA' | 'RURAL'
export type TipoCuenta = 'PERSONA' | 'ENTIDAD' | 'ADMIN'

export interface PerfilCuenta {
  id: number
  nombre: string
  correo: string
  fechaNacimiento: string | null
  zona: Zona | null
  estrato: number | null
  estado: string | null
  tipoCuenta: TipoCuenta
  tipoEntidad: string | null
  codigoInstitucional: string | null
  nivelAcceso: number | null
}

export interface RegistroCuenta {
  nombre: string
  correo: string
  contrasena: string
  documento: string
  fechaNacimiento: string
  zona?: Zona
  estrato?: number
  aceptaHabeasData: true
}

interface SesionAutenticada {
  token: string
  perfil: PerfilCuenta
}

export function registrarCuenta(datos: RegistroCuenta): Promise<PerfilCuenta> {
  return apiRequest<PerfilCuenta>('/cuentas/registro', {
    method: 'POST',
    authenticated: false,
    body: JSON.stringify(datos),
  })
}

export function iniciarSesion(correo: string, contrasena: string): Promise<SesionAutenticada> {
  return apiRequest<SesionAutenticada>('/cuentas/login', {
    method: 'POST',
    authenticated: false,
    body: JSON.stringify({ correo, contrasena }),
  })
}

export function obtenerMiPerfil(): Promise<PerfilCuenta> {
  return apiRequest<PerfilCuenta>('/cuentas/me')
}
