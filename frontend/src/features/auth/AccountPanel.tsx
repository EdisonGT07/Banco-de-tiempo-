import { useState } from 'react'
import type { FormEvent } from 'react'
import { ArrowRight } from 'lucide-react'
import { ApiError } from '../../services/api'
import type { PerfilCuenta, RegistroCuenta, Zona } from '../../services/cuentas'
import { useAuth } from '../../auth/useAuth'
import { Alerta } from '../../components/ui'

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
    <section className="cuenta-seccion" id="cuenta" aria-labelledby="cuenta-titulo">
      <div className="cuenta-intro">
        <p className="eyebrow">Tu espacio en la comunidad</p>
        <h2 id="cuenta-titulo">
          Empieza a compartir<br />
          <em>lo que sabes.</em>
        </h2>
        <p>Ingresa o crea tu cuenta para dar el siguiente paso en Pasaporte de Saberes.</p>
      </div>

      <div className="cuenta-tarjeta">
        {cargandoPerfil ? (
          <p className="estado-cargando" role="status">Comprobando tu sesión…</p>
        ) : perfil ? (
          <PerfilPanel perfil={perfil} onCerrarSesion={cerrarSesion} />
        ) : (
          <>
            <div className="cuenta-tabs" role="tablist" aria-label="Acceso a tu cuenta">
              <button
                type="button"
                role="tab"
                aria-selected={modo === 'login'}
                className={`cuenta-tab${modo === 'login' ? ' activo' : ''}`}
                onClick={() => cambiarModo('login')}
              >
                Iniciar sesión
              </button>
              <button
                type="button"
                role="tab"
                aria-selected={modo === 'registro'}
                className={`cuenta-tab${modo === 'registro' ? ' activo' : ''}`}
                onClick={() => cambiarModo('registro')}
              >
                Crear cuenta
              </button>
            </div>

            {errorRestauracion && <Alerta tipo="error">{errorRestauracion}</Alerta>}

            <form
              className="cuenta-form"
              onSubmit={enviarFormulario}
              aria-labelledby={modo === 'login' ? 'titulo-login' : 'titulo-registro'}
            >
              <h3 id={modo === 'login' ? 'titulo-login' : 'titulo-registro'} className="visually-hidden">
                {modo === 'login' ? 'Iniciar sesión' : 'Crear cuenta'}
              </h3>

              {modo === 'registro' && (
                <>
                  <div className="campo">
                    <label className="campo-etiqueta" htmlFor="reg-nombre">Nombre completo</label>
                    <input
                      id="reg-nombre"
                      autoComplete="name"
                      required
                      value={registro.nombre}
                      onChange={(e) => setRegistro({ ...registro, nombre: e.target.value })}
                    />
                  </div>
                  <div className="campo">
                    <label className="campo-etiqueta" htmlFor="reg-documento">Número de documento</label>
                    <input
                      id="reg-documento"
                      autoComplete="off"
                      autoCorrect="off"
                      autoCapitalize="none"
                      spellCheck={false}
                      required
                      value={registro.documento}
                      onChange={(e) => setRegistro({ ...registro, documento: e.target.value })}
                    />
                  </div>
                  <div className="campo">
                    <label className="campo-etiqueta" htmlFor="reg-nacimiento">Fecha de nacimiento</label>
                    <input
                      id="reg-nacimiento"
                      autoComplete="bday"
                      type="date"
                      max={fechaMaximaNacimiento()}
                      required
                      value={registro.fechaNacimiento}
                      onChange={(e) => setRegistro({ ...registro, fechaNacimiento: e.target.value })}
                    />
                    <p className="campo-pista" id="pista-nacimiento">Debes tener 18 años o más.</p>
                  </div>
                  <div className="form-fila">
                    <div className="campo">
                      <label className="campo-etiqueta" htmlFor="reg-zona">
                        Zona <span className="campo-opcional">(opcional)</span>
                      </label>
                      <select
                        id="reg-zona"
                        value={registro.zona}
                        onChange={(e) => {
                          const zona = e.target.value
                          if (zona === '' || zona === 'URBANA' || zona === 'RURAL') {
                            setRegistro({ ...registro, zona })
                          }
                        }}
                      >
                        <option value="">Selecciona</option>
                        <option value="URBANA">Urbana</option>
                        <option value="RURAL">Rural</option>
                      </select>
                    </div>
                    <div className="campo">
                      <label className="campo-etiqueta" htmlFor="reg-estrato">
                        Estrato <span className="campo-opcional">(opcional)</span>
                      </label>
                      <select
                        id="reg-estrato"
                        value={registro.estrato}
                        onChange={(e) => setRegistro({ ...registro, estrato: e.target.value })}
                      >
                        <option value="">Sin indicar</option>
                        {[1, 2, 3, 4, 5, 6].map((est) => (
                          <option key={est} value={est}>{est}</option>
                        ))}
                      </select>
                    </div>
                  </div>
                </>
              )}

              <div className="campo">
                <label className="campo-etiqueta" htmlFor="campo-correo">Correo electrónico</label>
                <input
                  id="campo-correo"
                  autoComplete="email"
                  type="email"
                  required
                  value={modo === 'login' ? correo : registro.correo}
                  onChange={(e) => {
                    if (modo === 'login') setCorreo(e.target.value)
                    else setRegistro({ ...registro, correo: e.target.value })
                  }}
                />
              </div>

              <div className="campo">
                <label className="campo-etiqueta" htmlFor="campo-contrasena">Contraseña</label>
                <input
                  id="campo-contrasena"
                  autoComplete={modo === 'login' ? 'current-password' : 'new-password'}
                  type="password"
                  required
                  value={modo === 'login' ? contrasena : registro.contrasena}
                  onChange={(e) => {
                    if (modo === 'login') setContrasena(e.target.value)
                    else setRegistro({ ...registro, contrasena: e.target.value })
                  }}
                />
              </div>

              {modo === 'registro' && (
                <label className="campo-consentimiento">
                  <input
                    type="checkbox"
                    required
                    checked={registro.aceptaHabeasData}
                    onChange={(e) => setRegistro({ ...registro, aceptaHabeasData: e.target.checked })}
                  />
                  <span>Acepto el tratamiento de mis datos personales para crear mi cuenta.</span>
                </label>
              )}

              {error && <Alerta tipo="error">{error}</Alerta>}

              <button className="btn btn-primario btn-bloque" type="submit" disabled={enviando}>
                {enviando
                  ? 'Un momento…'
                  : modo === 'login'
                    ? 'Ingresar a mi cuenta'
                    : 'Crear cuenta'}
                {!enviando && <ArrowRight size={15} aria-hidden="true" />}
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
    <div className="perfil-panel">
      <p className="eyebrow">Sesión iniciada</p>
      <h3>{perfil.nombre}</h3>
      <p className="perfil-correo">{perfil.correo}</p>
      <dl className="perfil-tabla">
        <div><dt>Tipo de cuenta</dt><dd>{etiquetaCuenta(perfil)}</dd></div>
        {perfil.estado && <div><dt>Estado</dt><dd>{perfil.estado}</dd></div>}
        {perfil.fechaNacimiento && (
          <div>
            <dt>Fecha de nacimiento</dt>
            <dd>{perfil.fechaNacimiento}</dd>
          </div>
        )}
        {perfil.zona && (
          <div>
            <dt>Zona</dt>
            <dd>{perfil.zona === 'URBANA' ? 'Urbana' : 'Rural'}</dd>
          </div>
        )}
        {perfil.estrato && <div><dt>Estrato</dt><dd>{perfil.estrato}</dd></div>}
      </dl>
      <button
        className="btn btn-secundario perfil-cerrar"
        type="button"
        onClick={onCerrarSesion}
      >
        Cerrar sesión
      </button>
    </div>
  )
}
