import { useState } from 'react'
import type { FormEvent } from 'react'
import { ApiError } from '../../services/api'
import type { PerfilCuenta, RegistroCuenta, Zona } from '../../services/cuentas'
import { useAuth } from '../../auth/useAuth'
import { ArrowRight } from 'lucide-react'

type ModoFormulario = 'login' | 'registro'

interface ValoresRegistro {
  nombre: string
  correo: string
  contrasena: string
  documento: string
  fechaNacimiento: string
  zona: Zona | ''
  estrato: string
  aceptaHabeasData: boolean
}

const registroVacio: ValoresRegistro = {
  nombre: '',
  correo: '',
  contrasena: '',
  documento: '',
  fechaNacimiento: '',
  zona: '',
  estrato: '',
  aceptaHabeasData: false,
}

function fechaMaximaNacimiento(): string {
  const fecha = new Date()
  fecha.setUTCFullYear(fecha.getUTCFullYear() - 18)
  return fecha.toISOString().slice(0, 10)
}

function mensajeDeError(error: unknown): string {
  if (error instanceof ApiError) {
    if (error.status === 401) return 'El correo o la contraseña no son correctos.'
    if (error.status === 409) return 'Ya existe una cuenta con esos datos.'
  }
  return error instanceof Error ? error.message : 'No se pudo completar la solicitud.'
}

function etiquetaCuenta(perfil: PerfilCuenta): string {
  if (perfil.tipoCuenta === 'ENTIDAD') return 'Entidad aliada'
  if (perfil.tipoCuenta === 'ADMIN') return 'Administración'
  return 'Persona'
}

export function AccountPanel() {
  const {
    perfil,
    cargandoPerfil,
    errorRestauracion,
    iniciarSesion,
    registrarCuenta,
    cerrarSesion,
  } = useAuth()
  const [modo, setModo] = useState<ModoFormulario>('login')
  const [correo, setCorreo] = useState('')
  const [contrasena, setContrasena] = useState('')
  const [registro, setRegistro] = useState<ValoresRegistro>(registroVacio)
  const [error, setError] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  async function enviarFormulario(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    setEnviando(true)

    try {
      if (modo === 'login') {
        await iniciarSesion(correo.trim(), contrasena)
      } else {
        const datos: RegistroCuenta = {
          nombre: registro.nombre.trim(),
          correo: registro.correo.trim(),
          contrasena: registro.contrasena,
          documento: registro.documento.trim(),
          fechaNacimiento: registro.fechaNacimiento,
          ...(registro.zona ? { zona: registro.zona } : {}),
          ...(registro.estrato ? { estrato: Number(registro.estrato) } : {}),
          aceptaHabeasData: true,
        }
        await registrarCuenta(datos)
      }
    } catch (requestError) {
      setError(mensajeDeError(requestError))
    } finally {
      setEnviando(false)
    }
  }

  function cambiarModo(nuevoModo: ModoFormulario) {
    setModo(nuevoModo)
    setError(null)
  }

  return (
    <section className="account-section" id="cuenta">
      <div className="account-intro">
        <p className="eyebrow">TU ESPACIO EN LA COMUNIDAD</p>
        <h2>Empieza a compartir<br /><em>lo que sabes.</em></h2>
        <p>Ingresa o crea tu cuenta para dar el siguiente paso en Pasaporte de Saberes.</p>
      </div>

      <div className="account-card">
        {cargandoPerfil ? (
          <p className="account-status" role="status">Comprobando tu sesión...</p>
        ) : perfil ? (
          <PerfilPanel perfil={perfil} onCerrarSesion={cerrarSesion} />
        ) : (
          <>
            <div className="account-tabs" role="tablist" aria-label="Acceso a tu cuenta">
              <button
                type="button"
                role="tab"
                aria-selected={modo === 'login'}
                className={modo === 'login' ? 'account-tab active' : 'account-tab'}
                onClick={() => cambiarModo('login')}
              >
                Iniciar sesión
              </button>
              <button
                type="button"
                role="tab"
                aria-selected={modo === 'registro'}
                className={modo === 'registro' ? 'account-tab active' : 'account-tab'}
                onClick={() => cambiarModo('registro')}
              >
                Crear cuenta
              </button>
            </div>

            {errorRestauracion && <p className="form-message error" role="alert">{errorRestauracion}</p>}
            <form className="account-form" onSubmit={enviarFormulario}>
              {modo === 'registro' && (
                <>
                  <label>
                    Nombre completo
                    <input
                      autoComplete="name"
                      required
                      value={registro.nombre}
                      onChange={(event) => setRegistro({ ...registro, nombre: event.target.value })}
                    />
                  </label>
                  <label>
                    Número de documento
                    <input
                      autoComplete="off"
                      required
                      value={registro.documento}
                      onChange={(event) => setRegistro({ ...registro, documento: event.target.value })}
                    />
                  </label>
                  <label>
                    Fecha de nacimiento
                    <input
                      autoComplete="bday"
                      type="date"
                      max={fechaMaximaNacimiento()}
                      required
                      value={registro.fechaNacimiento}
                      onChange={(event) => setRegistro({ ...registro, fechaNacimiento: event.target.value })}
                    />
                    <span className="field-hint">Debes tener 18 años o más para crear una cuenta.</span>
                  </label>
                  <div className="form-row">
                    <label>
                      Zona <span className="optional">(opcional)</span>
                      <select
                        value={registro.zona}
                        onChange={(event) => {
                          const zona = event.target.value
                          if (zona === '' || zona === 'URBANA' || zona === 'RURAL') {
                            setRegistro({ ...registro, zona })
                          }
                        }}
                      >
                        <option value="">Selecciona</option>
                        <option value="URBANA">Urbana</option>
                        <option value="RURAL">Rural</option>
                      </select>
                    </label>
                    <label>
                      Estrato <span className="optional">(opcional)</span>
                      <select
                        value={registro.estrato}
                        onChange={(event) => setRegistro({ ...registro, estrato: event.target.value })}
                      >
                        <option value="">Sin indicar</option>
                        {[1, 2, 3, 4, 5, 6].map((estrato) => (
                          <option key={estrato} value={estrato}>{estrato}</option>
                        ))}
                      </select>
                    </label>
                  </div>
                </>
              )}

              <label>
                Correo electrónico
                <input
                  autoComplete="email"
                  type="email"
                  required
                  value={modo === 'login' ? correo : registro.correo}
                  onChange={(event) => {
                    if (modo === 'login') setCorreo(event.target.value)
                    else setRegistro({ ...registro, correo: event.target.value })
                  }}
                />
              </label>
              <label>
                Contraseña
                <input
                  autoComplete={modo === 'login' ? 'current-password' : 'new-password'}
                  type="password"
                  required
                  value={modo === 'login' ? contrasena : registro.contrasena}
                  onChange={(event) => {
                    if (modo === 'login') setContrasena(event.target.value)
                    else setRegistro({ ...registro, contrasena: event.target.value })
                  }}
                />
              </label>

              {modo === 'registro' && (
                <label className="consent-label">
                  <input
                    type="checkbox"
                    required
                    checked={registro.aceptaHabeasData}
                    onChange={(event) => setRegistro({
                      ...registro,
                      aceptaHabeasData: event.target.checked,
                    })}
                  />
                  <span>Acepto el tratamiento de mis datos personales para crear mi cuenta.</span>
                </label>
              )}

              {error && <p className="form-message error" role="alert">{error}</p>}
              <button className="button button-primary account-submit" type="submit" disabled={enviando}>
                {enviando
                  ? 'Un momento...'
                  : modo === 'login'
                    ? 'Ingresar a mi cuenta'
                    : 'Crear cuenta'}
                {!enviando && <ArrowRight size={15} />}
              </button>
            </form>
          </>
        )}
      </div>
    </section>
  )
}

function PerfilPanel({
  perfil,
  onCerrarSesion,
}: {
  perfil: PerfilCuenta
  onCerrarSesion: () => void
}) {
  return (
    <div className="profile-panel">
      <p className="eyebrow">SESIÓN INICIADA</p>
      <h3>Hola, {perfil.nombre}</h3>
      <p className="profile-email">{perfil.correo}</p>
      <dl className="profile-details">
        <div><dt>Tipo de cuenta</dt><dd>{etiquetaCuenta(perfil)}</dd></div>
        {perfil.estado && <div><dt>Estado</dt><dd>{perfil.estado}</dd></div>}
        {perfil.fechaNacimiento && (
          <div><dt>Fecha de nacimiento</dt><dd>{perfil.fechaNacimiento}</dd></div>
        )}
        {perfil.zona && <div><dt>Zona</dt><dd>{perfil.zona === 'URBANA' ? 'Urbana' : 'Rural'}</dd></div>}
        {perfil.estrato && <div><dt>Estrato</dt><dd>{perfil.estrato}</dd></div>}
      </dl>
      <button className="button button-outline profile-logout" type="button" onClick={onCerrarSesion}>
        Cerrar sesión
      </button>
    </div>
  )
}
