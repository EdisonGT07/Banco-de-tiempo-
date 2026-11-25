import { useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import { ApiError } from '../services/api'
import {
  iniciarSesion as iniciarSesionApi,
  obtenerMiPerfil,
  registrarCuenta as registrarCuentaApi,
} from '../services/cuentas'
import type { PerfilCuenta, RegistroCuenta } from '../services/cuentas'
import { clearAccessToken, getAccessToken, saveAccessToken } from '../services/session'
import { AuthContext } from './auth-context'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [perfil, setPerfil] = useState<PerfilCuenta | null>(null)
  const [cargandoPerfil, setCargandoPerfil] = useState(true)
  const [errorRestauracion, setErrorRestauracion] = useState<string | null>(null)

  useEffect(() => {
    let activo = true

    function manejarSesionVencida() {
      clearAccessToken()
      setPerfil(null)
      setErrorRestauracion('Tu sesión venció. Inicia sesión nuevamente.')
      setCargandoPerfil(false)
    }

    window.addEventListener('pasaporte-saberes:sesion-vencida', manejarSesionVencida)

    async function restaurarSesion() {
      if (!getAccessToken()) {
        setCargandoPerfil(false)
        return
      }

      try {
        const perfilActual = await obtenerMiPerfil()
        if (activo) {
          setPerfil(perfilActual)
        }
      } catch (error) {
        if (!activo) return

        if (error instanceof ApiError && error.status === 401) {
          clearAccessToken()
          setErrorRestauracion('Tu sesión venció. Inicia sesión nuevamente.')
        } else {
          setErrorRestauracion(
            error instanceof Error
              ? `No se pudo recuperar tu perfil: ${error.message}`
              : 'No se pudo recuperar tu perfil.',
          )
        }
      } finally {
        if (activo) {
          setCargandoPerfil(false)
        }
      }
    }

    void restaurarSesion()
    return () => {
      activo = false
      window.removeEventListener('pasaporte-saberes:sesion-vencida', manejarSesionVencida)
    }
  }, [])

  async function iniciarSesion(correo: string, contrasena: string) {
    const sesion = await iniciarSesionApi(correo, contrasena)
    saveAccessToken(sesion.token)
    setPerfil(sesion.perfil)
    setErrorRestauracion(null)
  }

  async function registrarCuenta(datos: RegistroCuenta) {
    await registrarCuentaApi(datos)
    await iniciarSesion(datos.correo, datos.contrasena)
  }

  function cerrarSesion() {
    clearAccessToken()
    setPerfil(null)
    setErrorRestauracion(null)
  }

  return (
    <AuthContext.Provider
      value={{
        perfil,
        cargandoPerfil,
        errorRestauracion,
        iniciarSesion,
        registrarCuenta,
        cerrarSesion,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}
