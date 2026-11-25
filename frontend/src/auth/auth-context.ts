import { createContext } from 'react'
import type { PerfilCuenta, RegistroCuenta } from '../services/cuentas'

export interface AuthContextValue {
  perfil: PerfilCuenta | null
  cargandoPerfil: boolean
  errorRestauracion: string | null
  iniciarSesion: (correo: string, contrasena: string) => Promise<void>
  registrarCuenta: (datos: RegistroCuenta) => Promise<void>
  cerrarSesion: () => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)
