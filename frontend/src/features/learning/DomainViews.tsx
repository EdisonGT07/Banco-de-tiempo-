import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { consultarBilletera, retirarDelFondo } from '../../services/horas'
import type { Billetera } from '../../services/horas'
import {
  cancelarInscripcion,
  confirmarFin,
  confirmarHandshake,
  iniciarHandshake,
  inscribirseSesion,
  reportarProblema,
} from '../../services/sesiones'
import type { Sesion } from '../../services/sesiones'
import {
  crearHabilidad,
  listarHabilidades,
  publicarOferta,
} from '../../services/catalogo'
import type { NuevaOferta } from '../../services/catalogo'
import type { Habilidad } from '../../services/catalogo'
import { calificarSesion } from '../../services/reputacion'

export type VistaDominio =
  | 'inicio'
  | 'explorar'
  | 'recomendado'
  | 'billetera'
  | 'sesiones'
  | 'pasaporte'
  | 'publicar'
  | 'administracion'

export function VistaPublicarOferta() {
  const [habilidades, setHabilidades] = useState<Habilidad[]>([])
  const [habilidadId, setHabilidadId] = useState('')
  const [cargandoHabilidades, setCargandoHabilidades] = useState(true)
  const [nuevoSaber, setNuevoSaber] = useState('')
  const [nuevaCategoria, setNuevaCategoria] = useState('Tecnología')
  const [creandoSaber, setCreandoSaber] = useState(false)
  const [descripcion, setDescripcion] = useState('')
  const [duracion, setDuracion] = useState('1')
  const [modalidad, setModalidad] = useState<'PRESENCIAL' | 'VIRTUAL'>('VIRTUAL')
  const [barrio, setBarrio] = useState('')
  const [destinoHoras, setDestinoHoras] = useState<
    'BILLETERA_PROPIA' | 'FONDO'
  >('BILLETERA_PROPIA')
  const [procesando, setProcesando] = useState(false)
  const [mensaje, setMensaje] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let vigente = true
    void listarHabilidades()
      .then((resultado) => {
        if (vigente) setHabilidades(resultado)
      })
      .catch((problema: unknown) => {
        if (vigente) setError(
          problema instanceof Error
            ? problema.message
            : 'No se pudieron cargar los saberes.',
        )
      })
      .finally(() => {
        if (vigente) setCargandoHabilidades(false)
      })
    return () => {
      vigente = false
    }
  }, [])

  async function crearNuevoSaber() {
    if (!nuevoSaber.trim()) return
    setCreandoSaber(true)
    setError(null)
    try {
      const habilidad = await crearHabilidad(
        nuevoSaber.trim(),
        nuevaCategoria,
      )
      setHabilidades((actuales) =>
        [...actuales, habilidad].sort((a, b) => a.nombre.localeCompare(b.nombre, 'es')))
      setHabilidadId(String(habilidad.id))
      setNuevoSaber('')
    } catch (problema) {
      setError(problema instanceof Error ? problema.message : 'No se pudo crear el saber.')
    } finally {
      setCreandoSaber(false)
    }
  }

  async function publicar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setProcesando(true)
    setMensaje(null)
    setError(null)
    try {
      const datos: NuevaOferta = {
        habilidadId: Number(habilidadId),
        descripcion: descripcion.trim(),
        duracionHoras: Number(duracion),
        modalidad,
        destinoHoras,
        ...(barrio.trim() ? { barrio: barrio.trim() } : {}),
      }
      await publicarOferta(datos)
      setMensaje('Oferta publicada correctamente.')
      setDescripcion('')
      setDuracion('1')
      setBarrio('')
    } catch (problema) {
      setError(problema instanceof Error ? problema.message : 'No se pudo publicar la oferta.')
    } finally {
      setProcesando(false)
    }
  }

  return (
    <section className="domain-view">
      <div className="learning-section-heading">
        <div>
          <p className="eyebrow">COMPARTE LO QUE SABES</p>
          <h2>Publicar saber</h2>
        </div>
      </div>
      <form className="domain-card publish-form" onSubmit={publicar}>
        <div className="publish-grid">
          <label className="domain-field">
            Saber
            <select
              required
              value={habilidadId}
              onChange={(event) => setHabilidadId(event.target.value)}
              disabled={cargandoHabilidades}
            >
              <option value="">
                {cargandoHabilidades ? 'Cargando saberes...' : 'Selecciona un saber'}
              </option>
              {habilidades.map((habilidad) => (
                <option key={habilidad.id} value={habilidad.id}>
                  {habilidad.nombre} · {habilidad.categoria}
                </option>
              ))}
            </select>
          </label>
          <label className="domain-field">
            Duración por sesión
            <input
              type="number"
              min="0.01"
              max="99.99"
              step="0.01"
              required
              value={duracion}
              onChange={(event) => setDuracion(event.target.value)}
            />
          </label>
          <label className="domain-field">
            Modalidad
            <select value={modalidad} onChange={(event) =>
              setModalidad(event.target.value as 'PRESENCIAL' | 'VIRTUAL')}>
              <option value="VIRTUAL">Virtual</option>
              <option value="PRESENCIAL">Presencial</option>
            </select>
          </label>
          <label className="domain-field">
            Destino de las horas
            <select value={destinoHoras} onChange={(event) =>
              setDestinoHoras(event.target.value as 'BILLETERA_PROPIA' | 'FONDO')}>
              <option value="BILLETERA_PROPIA">Mi billetera</option>
              <option value="FONDO">Fondo comunitario</option>
            </select>
          </label>
        </div>
        <div className="create-skill-card">
          <p>¿No encuentras tu saber?</p>
          <div className="publish-grid">
            <label className="domain-field">
              Nombre del saber
              <input
                value={nuevoSaber}
                onChange={(event) => setNuevoSaber(event.target.value)}
                placeholder="Ej. Fotografía"
              />
            </label>
            <label className="domain-field">
              Categoría
              <select
                value={nuevaCategoria}
                onChange={(event) => setNuevaCategoria(event.target.value)}
              >
                <option>Tecnología</option>
                <option>Oficios</option>
                <option>Bienestar</option>
                <option>Idiomas</option>
                <option>Arte y cultura</option>
                <option>Educación</option>
                <option>Otro</option>
              </select>
            </label>
          </div>
          <button
            className="button button-outline"
            type="button"
            disabled={creandoSaber || !nuevoSaber.trim()}
            onClick={() => void crearNuevoSaber()}
          >
            {creandoSaber ? 'Creando...' : 'Agregar como nuevo saber'}
          </button>
        </div>
        <label className="domain-field">
          Describe tu oferta
          <textarea
            rows={4}
            required
            value={descripcion}
            onChange={(event) => setDescripcion(event.target.value)}
            placeholder="Qué enseñarás, para quién y cómo..."
          />
        </label>
        <div className="publish-grid">
          <label className="domain-field">
            Barrio <span className="optional">(opcional)</span>
            <input value={barrio} onChange={(event) => setBarrio(event.target.value)} />
          </label>
        </div>
        <button className="button button-primary domain-submit" type="submit" disabled={procesando}>
          {procesando ? 'Publicando...' : 'Publicar oferta'}
        </button>
      </form>
      {mensaje && <p className="learning-success" role="status">{mensaje}</p>}
      {error && <p className="learning-error" role="alert">{error}</p>}
    </section>
  )
}

export function VistaBilletera() {
  const [billetera, setBilletera] = useState<Billetera | null>(null)
  const [horas, setHoras] = useState('')
  const [cargando, setCargando] = useState(true)
  const [procesando, setProcesando] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [mensaje, setMensaje] = useState<string | null>(null)

  useEffect(() => {
    let vigente = true
    void consultarBilletera()
      .then((resultado) => {
        if (vigente) setBilletera(resultado)
      })
      .catch((problema: unknown) => {
        if (vigente) setError(problema instanceof Error ? problema.message : 'No se pudo consultar la billetera.')
      })
      .finally(() => {
        if (vigente) setCargando(false)
      })
    return () => {
      vigente = false
    }
  }, [])

  async function retirar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    setMensaje(null)
    setProcesando(true)
    try {
      await retirarDelFondo(Number(horas))
      setMensaje('Retiro registrado correctamente.')
      setHoras('')
      setBilletera(await consultarBilletera())
    } catch (problema) {
      setError(problema instanceof Error ? problema.message : 'No se pudo registrar el retiro.')
    } finally {
      setProcesando(false)
    }
  }

  return (
    <section className="domain-view">
      <div className="learning-section-heading">
        <div>
          <p className="eyebrow">TU BALANCE DE TIEMPO</p>
          <h2>Mi billetera</h2>
        </div>
      </div>
      {cargando ? (
        <p className="learning-status" role="status">Consultando tu billetera...</p>
      ) : billetera ? (
        <>
          <div className="wallet-grid">
            <article className="wallet-card wallet-primary">
              <p>Horas disponibles</p>
              <strong>{billetera.saldoDisponible}</strong>
              <span>Hrs</span>
            </article>
            <article className="wallet-card">
              <p>Horas reservadas</p>
              <strong>{billetera.saldoReservado}</strong>
              <span>Hrs</span>
            </article>
            <article className="wallet-card">
              <p>Tipo de billetera</p>
              <strong className="wallet-type">{billetera.tipo}</strong>
            </article>
          </div>
          <div className="domain-card">
            <h3>Retirar horas del fondo</h3>
            <p>El retiro usa una clave de idempotencia automática para evitar cobrarse dos veces.</p>
            <form className="domain-form" onSubmit={retirar}>
              <label>
                Cantidad de horas
                <input
                  type="number"
                  min="0.01"
                  step="0.01"
                  required
                  value={horas}
                  onChange={(event) => setHoras(event.target.value)}
                />
              </label>
              <button className="button button-primary" type="submit" disabled={procesando}>
                {procesando ? 'Procesando...' : 'Solicitar retiro'}
              </button>
            </form>
          </div>
        </>
      ) : null}
      {mensaje && <p className="learning-success" role="status">{mensaje}</p>}
      {error && <p className="learning-error" role="alert">{error}</p>}
    </section>
  )
}

export function VistaSesiones() {
  const [sesionId, setSesionId] = useState('')
  const [calificacion, setCalificacion] = useState('5')
  const [comentario, setComentario] = useState('')
  const [token, setToken] = useState('')
  const [codigo, setCodigo] = useState('')
  const [motivo, setMotivo] = useState('')
  const [foto, setFoto] = useState<File | undefined>()
  const [consienteEvidencia, setConsienteEvidencia] = useState(false)
  const [sesion, setSesion] = useState<Sesion | null>(null)
  const [procesando, setProcesando] = useState(false)
  const [mensaje, setMensaje] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  function id(): number {
    return Number(sesionId)
  }

  async function ejecutar(accion: () => Promise<unknown>, textoExito: string) {
    setError(null)
    setMensaje(null)
    setProcesando(true)
    try {
      const resultado = await accion()
      if (resultado && typeof resultado === 'object' && 'id' in resultado) {
        setSesion(resultado as Sesion)
      }
      setMensaje(textoExito)
    } catch (problema) {
      setError(problema instanceof Error ? problema.message : 'No se pudo completar la acción.')
    } finally {
      setProcesando(false)
    }
  }

  async function generarHandshake() {
    setError(null)
    setMensaje(null)
    setProcesando(true)
    try {
      const resultado = await iniciarHandshake(id())
      setToken(resultado.token)
      setCodigo(resultado.codigoCorto)
      setMensaje(`Handshake generado. Expira el ${new Date(resultado.expiraEn).toLocaleString('es-CO')}.`)
    } catch (problema) {
      setError(problema instanceof Error ? problema.message : 'No se pudo iniciar el handshake.')
    } finally {
      setProcesando(false)
    }
  }

  async function confirmar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    await ejecutar(
      () => confirmarHandshake(id(), {
        token,
        codigoCorto: codigo,
        consienteEvidencia,
        foto,
      }),
      'Confirmación registrada.',
    )
  }

  return (
    <section className="domain-view">
      <div className="learning-section-heading">
        <div>
          <p className="eyebrow">GESTIONA TU EXPERIENCIA COMPARTIDA</p>
          <h2>Mis sesiones</h2>
        </div>
      </div>
      <div className="session-layout">
        <div className="domain-card">
          <h3>Sesión</h3>
          <label className="domain-field">
            Identificador de sesión
            <input
              type="number"
              min="1"
              value={sesionId}
              onChange={(event) => setSesionId(event.target.value)}
              placeholder="Ej. 51"
            />
          </label>
          <div className="action-grid">
            <button
              className="button button-outline"
              type="button"
              disabled={procesando || !sesionId}
              onClick={() => void ejecutar(
                () => inscribirseSesion(id()),
                'Te inscribiste en la sesión.',
              )}
            >
              Inscribirme
            </button>
            <button
              className="button button-outline"
              type="button"
              disabled={procesando || !sesionId}
              onClick={() => void ejecutar(
                () => cancelarInscripcion(id()),
                'Inscripción cancelada.',
              )}
            >
              Cancelar
            </button>
            <button
              className="button button-outline"
              type="button"
              disabled={procesando || !sesionId}
              onClick={() => void generarHandshake()}
            >
              Generar handshake
            </button>
            <button
              className="button button-primary"
              type="button"
              disabled={procesando || !sesionId}
              onClick={() => void ejecutar(
                () => confirmarFin(id()),
                'Fin de sesión confirmado.',
              )}
            >
              Confirmar fin
            </button>
          </div>
        </div>

        <form className="domain-card" onSubmit={confirmar}>
          <h3>Confirmar asistencia</h3>
          <label className="domain-field">
            Token
            <input value={token} onChange={(event) => setToken(event.target.value)} required />
          </label>
          <label className="domain-field">
            Código corto
            <input value={codigo} onChange={(event) => setCodigo(event.target.value)} required />
          </label>
          <label className="domain-field">
            Evidencia fotográfica <span className="optional">(opcional)</span>
            <input
              type="file"
              accept="image/jpeg,image/png,image/webp"
              onChange={(event) => setFoto(event.target.files?.[0])}
            />
          </label>
          <label className="consent-label">
            <input
              type="checkbox"
              checked={consienteEvidencia}
              onChange={(event) => setConsienteEvidencia(event.target.checked)}
            />
            <span>Consiento el uso de la evidencia para verificar la sesión.</span>
          </label>
          <button
            className="button button-primary domain-submit"
            type="submit"
            disabled={procesando || !sesionId}
          >
            Confirmar asistencia
          </button>
        </form>

        <div className="domain-card">
          <h3>Reportar un problema</h3>
          <label className="domain-field">
            Motivo
            <textarea
              rows={4}
              value={motivo}
              onChange={(event) => setMotivo(event.target.value)}
              placeholder="Describe qué ocurrió..."
              required
            />
          </label>
          <button
            className="button button-outline domain-submit"
            type="button"
            disabled={procesando || !sesionId || !motivo.trim()}
            onClick={() => void ejecutar(
              () => reportarProblema(id(), motivo.trim()),
              'Problema reportado.',
            )}
          >
            Enviar reporte
          </button>
        </div>

        <div className="domain-card">
          <h3>Calificar una sesión</h3>
          <label className="domain-field">
            Calificación
            <select value={calificacion} onChange={(event) => setCalificacion(event.target.value)}>
              {[1, 2, 3, 4, 5].map((valor) => (
                <option key={valor} value={valor}>{valor} de 5</option>
              ))}
            </select>
          </label>
          <label className="domain-field">
            Comentario <span className="optional">(opcional)</span>
            <textarea
              rows={3}
              value={comentario}
              maxLength={2000}
              onChange={(event) => setComentario(event.target.value)}
              placeholder="Cuéntanos cómo fue..."
            />
          </label>
          <button
            className="button button-primary domain-submit"
            type="button"
            disabled={procesando || !sesionId}
            onClick={() => void ejecutar(
              () => calificarSesion(id(), Number(calificacion), comentario.trim()),
              'Reseña registrada.',
            )}
          >
            Enviar calificación
          </button>
        </div>
      </div>
      {sesion && (
        <div className="session-result">
          <strong>Estado actual:</strong> {sesion.estado}
          {sesion.aprendizId && <span>Aprendiz: {sesion.aprendizId}</span>}
        </div>
      )}
      {mensaje && <p className="learning-success" role="status">{mensaje}</p>}
      {error && <p className="learning-error" role="alert">{error}</p>}
    </section>
  )
}
