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
import type { Habilidad, NuevaOferta } from '../../services/catalogo'
import { calificarSesion } from '../../services/reputacion'
import { Alerta } from '../../components/ui'
import { Star } from 'lucide-react'

export type VistaDominio =
  | 'inicio'
  | 'explorar'
  | 'recomendado'
  | 'billetera'
  | 'sesiones'
  | 'pasaporte'
  | 'publicar'
  | 'administracion'

/* ============================================================
   PUBLICAR OFERTA
   ============================================================ */
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
  const [destinoHoras, setDestinoHoras] = useState<'BILLETERA_PROPIA' | 'FONDO'>('BILLETERA_PROPIA')
  const [procesando, setProcesando] = useState(false)
  const [exito, setExito] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let vigente = true
    void listarHabilidades()
      .then((resultado) => { if (vigente) setHabilidades(resultado) })
      .catch((problema: unknown) => {
        if (vigente) setError(
          problema instanceof Error ? problema.message : 'No se pudieron cargar los saberes.',
        )
      })
      .finally(() => { if (vigente) setCargandoHabilidades(false) })
    return () => { vigente = false }
  }, [])

  async function crearNuevoSaber() {
    if (!nuevoSaber.trim()) return
    setCreandoSaber(true)
    setError(null)
    try {
      const habilidad = await crearHabilidad(nuevoSaber.trim(), nuevaCategoria)
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
    setExito(null)
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
      setExito('Oferta publicada correctamente.')
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
    <section className="dominio-vista">
      <div className="dominio-cabecera">
        <div>
          <p className="eyebrow">Comparte lo que sabes</p>
          <h2>Publicar saber</h2>
        </div>
      </div>

      <form className="publicar-form" onSubmit={publicar} aria-label="Formulario publicar oferta">
        <div className="publicar-grilla">
          <div className="campo">
            <label className="campo-etiqueta" htmlFor="pub-saber">Saber</label>
            <select
              id="pub-saber"
              required
              value={habilidadId}
              onChange={(e) => setHabilidadId(e.target.value)}
              disabled={cargandoHabilidades}
            >
              <option value="">
                {cargandoHabilidades ? 'Cargando saberes…' : 'Selecciona un saber'}
              </option>
              {habilidades.map((h) => (
                <option key={h.id} value={h.id}>{h.nombre} · {h.categoria}</option>
              ))}
            </select>
          </div>
          <div className="campo">
            <label className="campo-etiqueta" htmlFor="pub-duracion">Duración por sesión (horas)</label>
            <input
              id="pub-duracion"
              type="number"
              min="0.01"
              max="99.99"
              step="0.01"
              required
              value={duracion}
              onChange={(e) => setDuracion(e.target.value)}
            />
          </div>
          <div className="campo">
            <label className="campo-etiqueta" htmlFor="pub-modalidad">Modalidad</label>
            <select
              id="pub-modalidad"
              value={modalidad}
              onChange={(e) => setModalidad(e.target.value as 'PRESENCIAL' | 'VIRTUAL')}
            >
              <option value="VIRTUAL">Virtual</option>
              <option value="PRESENCIAL">Presencial</option>
            </select>
          </div>
          <div className="campo">
            <label className="campo-etiqueta" htmlFor="pub-destino">Destino de las horas</label>
            <select
              id="pub-destino"
              value={destinoHoras}
              onChange={(e) => setDestinoHoras(e.target.value as 'BILLETERA_PROPIA' | 'FONDO')}
            >
              <option value="BILLETERA_PROPIA">Mi billetera</option>
              <option value="FONDO">Fondo comunitario</option>
            </select>
          </div>
        </div>

        <div className="nuevo-saber-panel" role="group" aria-labelledby="nuevo-saber-titulo">
          <p id="nuevo-saber-titulo">¿No encuentras tu saber?</p>
          <div className="publicar-grilla">
            <div className="campo">
              <label className="campo-etiqueta" htmlFor="nuevo-saber-nombre">Nombre del saber</label>
              <input
                id="nuevo-saber-nombre"
                value={nuevoSaber}
                onChange={(e) => setNuevoSaber(e.target.value)}
                placeholder="Ej. Fotografía analógica"
              />
            </div>
            <div className="campo">
              <label className="campo-etiqueta" htmlFor="nuevo-saber-categoria">Categoría</label>
              <select
                id="nuevo-saber-categoria"
                value={nuevaCategoria}
                onChange={(e) => setNuevaCategoria(e.target.value)}
              >
                <option>Tecnología</option>
                <option>Oficios</option>
                <option>Bienestar</option>
                <option>Idiomas</option>
                <option>Arte y cultura</option>
                <option>Educación</option>
                <option>Otro</option>
              </select>
            </div>
          </div>
          <button
            className="btn btn-secundario btn-sm"
            type="button"
            disabled={creandoSaber || !nuevoSaber.trim()}
            onClick={() => void crearNuevoSaber()}
            style={{ marginTop: 'var(--esp-3)' }}
          >
            {creandoSaber ? 'Creando…' : 'Agregar como nuevo saber'}
          </button>
        </div>

        <div className="campo">
          <label className="campo-etiqueta" htmlFor="pub-descripcion">Describe tu oferta</label>
          <textarea
            id="pub-descripcion"
            rows={4}
            required
            value={descripcion}
            onChange={(e) => setDescripcion(e.target.value)}
            placeholder="Qué enseñarás, para quién y cómo…"
          />
        </div>

        <div className="campo">
          <label className="campo-etiqueta" htmlFor="pub-barrio">
            Barrio <span className="campo-opcional">(opcional)</span>
          </label>
          <input
            id="pub-barrio"
            value={barrio}
            onChange={(e) => setBarrio(e.target.value)}
          />
        </div>

        <button className="btn btn-primario" type="submit" disabled={procesando}>
          {procesando ? 'Publicando…' : 'Publicar oferta'}
        </button>
      </form>

      {exito && <Alerta tipo="exito">{exito}</Alerta>}
      {error && <Alerta tipo="error">{error}</Alerta>}
    </section>
  )
}

/* ============================================================
   BILLETERA
   ============================================================ */
export function VistaBilletera() {
  const [billetera, setBilletera] = useState<Billetera | null>(null)
  const [horas, setHoras] = useState('')
  const [cargando, setCargando] = useState(true)
  const [procesando, setProcesando] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [exito, setExito] = useState<string | null>(null)

  useEffect(() => {
    let vigente = true
    void consultarBilletera()
      .then((resultado) => { if (vigente) setBilletera(resultado) })
      .catch((problema: unknown) => {
        if (vigente) setError(
          problema instanceof Error ? problema.message : 'No se pudo consultar la billetera.',
        )
      })
      .finally(() => { if (vigente) setCargando(false) })
    return () => { vigente = false }
  }, [])

  async function retirar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    setExito(null)
    setProcesando(true)
    try {
      await retirarDelFondo(Number(horas))
      setExito('Retiro registrado correctamente.')
      setHoras('')
      setBilletera(await consultarBilletera())
    } catch (problema) {
      setError(
        problema instanceof Error ? problema.message : 'No se pudo registrar el retiro.',
      )
    } finally {
      setProcesando(false)
    }
  }

  return (
    <section className="dominio-vista">
      <div className="dominio-cabecera">
        <div>
          <p className="eyebrow">Tu balance de tiempo</p>
          <h2>Mi billetera</h2>
        </div>
      </div>

      {cargando ? (
        <p className="estado-cargando" role="status">Consultando tu billetera…</p>
      ) : billetera ? (
        <>
          <div className="billetera-grilla">
            <article className="billetera-card billetera-card-primaria">
              <p>Horas disponibles</p>
              <strong>{billetera.saldoDisponible}</strong>
              <span>Hrs</span>
            </article>
            <article className="billetera-card">
              <p>Horas reservadas</p>
              <strong>{billetera.saldoReservado}</strong>
              <span>Hrs</span>
            </article>
            <article className="billetera-card">
              <p>Tipo de billetera</p>
              <strong className="billetera-tipo">{billetera.tipo}</strong>
            </article>
          </div>

          <div className="panel" style={{ maxWidth: 480 }}>
            <h3>Retirar horas del fondo</h3>
            <p>Solicita un retiro del fondo comunitario a tu billetera personal.</p>
            <form onSubmit={retirar} aria-label="Formulario de retiro">
              <div className="campo" style={{ marginBottom: 'var(--esp-4)' }}>
                <label className="campo-etiqueta" htmlFor="horas-retiro">
                  Cantidad de horas
                </label>
                <input
                  id="horas-retiro"
                  type="number"
                  min="0.01"
                  step="0.01"
                  required
                  value={horas}
                  onChange={(e) => setHoras(e.target.value)}
                />
              </div>
              <button className="btn btn-primario" type="submit" disabled={procesando}>
                {procesando ? 'Procesando…' : 'Solicitar retiro'}
              </button>
            </form>
          </div>
        </>
      ) : null}

      {exito && <Alerta tipo="exito">{exito}</Alerta>}
      {error && <Alerta tipo="error">{error}</Alerta>}
    </section>
  )
}

/* ============================================================
   SESIONES (rediseñada como panel de operaciones)
   ============================================================ */
export function VistaSesiones() {
  const [sesionId, setSesionId] = useState('')
  const [calificacion, setCalificacion] = useState('5')
  const [comentario, setComentario] = useState('')
  const [token, setToken] = useState('')
  const [codigoCorto, setCodigoCorto] = useState('')
  const [motivo, setMotivo] = useState('')
  const [foto, setFoto] = useState<File | undefined>()
  const [consienteEvidencia, setConsienteEvidencia] = useState(false)
  const [sesion, setSesion] = useState<Sesion | null>(null)
  const [handshakeInfo, setHandshakeInfo] = useState<{ expiraEn: string; codigoCorto: string } | null>(null)
  const [procesando, setProcesando] = useState(false)
  const [exito, setExito] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  const idSesion = Number(sesionId)

  async function ejecutar(accion: () => Promise<unknown>, textoExito: string) {
    setError(null)
    setExito(null)
    setProcesando(true)
    try {
      const resultado = await accion()
      if (resultado && typeof resultado === 'object' && 'id' in resultado) {
        setSesion(resultado as Sesion)
      }
      setExito(textoExito)
    } catch (problema) {
      setError(problema instanceof Error ? problema.message : 'No se pudo completar la acción.')
    } finally {
      setProcesando(false)
    }
  }

  async function generarHandshake() {
    setError(null)
    setExito(null)
    setProcesando(true)
    try {
      const resultado = await iniciarHandshake(idSesion)
      setToken(resultado.token)
      setCodigoCorto(resultado.codigoCorto)
      setHandshakeInfo({ expiraEn: resultado.expiraEn, codigoCorto: resultado.codigoCorto })
      setExito('Handshake generado. Comparte el código con el participante.')
    } catch (problema) {
      setError(
        problema instanceof Error ? problema.message : 'No se pudo iniciar el handshake.',
      )
    } finally {
      setProcesando(false)
    }
  }

  async function confirmar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    await ejecutar(
      () => confirmarHandshake(idSesion, { token, codigoCorto, consienteEvidencia, foto }),
      'Confirmación de asistencia registrada.',
    )
  }

  return (
    <section className="dominio-vista">
      <div className="dominio-cabecera">
        <div>
          <p className="eyebrow">Gestiona tu experiencia compartida</p>
          <h2>Mis sesiones</h2>
        </div>
      </div>

      <div className="sesiones-layout">
        {/* Columna izquierda: acciones directas */}
        <div style={{ display: 'grid', gap: 'var(--esp-5)' }}>

          {/* Panel ciclo de sesión */}
          <div className="panel">
            <h3>Gestionar sesión</h3>
            <p>Introduce el ID de la sesión para realizar acciones sobre ella.</p>
            <div className="campo sesion-id-campo">
              <label className="campo-etiqueta" htmlFor="sesion-id">
                Identificador de sesión
              </label>
              <input
                id="sesion-id"
                type="number"
                min="1"
                value={sesionId}
                onChange={(e) => setSesionId(e.target.value)}
                placeholder="Ej. 12"
                inputMode="numeric"
              />
            </div>
            <div className="sesion-acciones-grilla">
              <button
                className="btn btn-secundario"
                type="button"
                disabled={procesando || !sesionId}
                onClick={() => void ejecutar(() => inscribirseSesion(idSesion), 'Te inscribiste en la sesión.')}
              >
                Inscribirme
              </button>
              <button
                className="btn btn-fantasma"
                type="button"
                disabled={procesando || !sesionId}
                onClick={() => void ejecutar(() => cancelarInscripcion(idSesion), 'Inscripción cancelada.')}
              >
                Cancelar inscripción
              </button>
              <button
                className="btn btn-secundario"
                type="button"
                disabled={procesando || !sesionId}
                onClick={() => void generarHandshake()}
              >
                Generar handshake
              </button>
              <button
                className="btn btn-primario"
                type="button"
                disabled={procesando || !sesionId}
                onClick={() => void ejecutar(() => confirmarFin(idSesion), 'Fin de sesión confirmado.')}
              >
                Confirmar fin
              </button>
            </div>

            {handshakeInfo && (
              <div className="handshake-info">
                <strong>Código de handshake</strong>
                <span className="handshake-codigo">{handshakeInfo.codigoCorto}</span>
                <p style={{ marginTop: 'var(--esp-2)', color: 'var(--tinta-suave)', fontSize: 'var(--fs-meta)' }}>
                  Expira el {new Date(handshakeInfo.expiraEn).toLocaleString('es-CO')}
                </p>
              </div>
            )}

            {sesion && (
              <div style={{ marginTop: 'var(--esp-4)', padding: 'var(--esp-3)', background: 'var(--papel)', borderRadius: 'var(--radio-s)', fontSize: 'var(--fs-meta)', color: 'var(--tinta-suave)' }}>
                <strong style={{ color: 'var(--tinta)', display: 'block', marginBottom: 'var(--esp-1)' }}>
                  Estado: {sesion.estado}
                </strong>
                {sesion.aprendizId && <span>Aprendiz ID: {sesion.aprendizId}</span>}
              </div>
            )}
          </div>

          {/* Reportar problema */}
          <div className="panel">
            <h3>Reportar un problema</h3>
            <div className="campo" style={{ marginBottom: 'var(--esp-4)' }}>
              <label className="campo-etiqueta" htmlFor="reporte-motivo">Motivo</label>
              <textarea
                id="reporte-motivo"
                rows={3}
                value={motivo}
                onChange={(e) => setMotivo(e.target.value)}
                placeholder="Describe qué ocurrió…"
              />
            </div>
            <button
              className="btn btn-peligro"
              type="button"
              disabled={procesando || !sesionId || !motivo.trim()}
              onClick={() => void ejecutar(
                () => reportarProblema(idSesion, motivo.trim()),
                'Problema reportado.',
              )}
            >
              Enviar reporte
            </button>
          </div>
        </div>

        {/* Columna derecha: confirmar asistencia y calificar */}
        <div style={{ display: 'grid', gap: 'var(--esp-5)' }}>

          {/* Confirmar handshake */}
          <form className="panel" onSubmit={confirmar} aria-label="Confirmar asistencia">
            <h3>Confirmar asistencia</h3>
            <div className="panel-grilla">
              <div className="campo">
                <label className="campo-etiqueta" htmlFor="hs-token">Token</label>
                <input
                  id="hs-token"
                  value={token}
                  onChange={(e) => setToken(e.target.value)}
                  required
                  autoCorrect="off"
                  autoCapitalize="none"
                />
              </div>
              <div className="campo">
                <label className="campo-etiqueta" htmlFor="hs-codigo">Código corto</label>
                <input
                  id="hs-codigo"
                  value={codigoCorto}
                  onChange={(e) => setCodigoCorto(e.target.value)}
                  required
                  autoCorrect="off"
                  autoCapitalize="none"
                />
              </div>
              <div className="campo">
                <label className="campo-etiqueta" htmlFor="hs-foto">
                  Evidencia fotográfica <span className="campo-opcional">(opcional)</span>
                </label>
                <input
                  id="hs-foto"
                  type="file"
                  accept="image/jpeg,image/png,image/webp"
                  onChange={(e) => setFoto(e.target.files?.[0])}
                />
              </div>
              <label className="campo-consentimiento">
                <input
                  type="checkbox"
                  checked={consienteEvidencia}
                  onChange={(e) => setConsienteEvidencia(e.target.checked)}
                />
                <span>Consiento el uso de la evidencia para verificar la sesión.</span>
              </label>
            </div>
            <button
              className="btn btn-primario"
              type="submit"
              disabled={procesando || !sesionId}
              style={{ marginTop: 'var(--esp-4)' }}
            >
              Confirmar asistencia
            </button>
          </form>

          {/* Calificar */}
          <div className="panel">
            <h3>Calificar una sesión</h3>
            <div className="panel-grilla">
              <div className="campo">
                <label className="campo-etiqueta" htmlFor="cal-calificacion">Calificación</label>
                <select
                  id="cal-calificacion"
                  value={calificacion}
                  onChange={(e) => setCalificacion(e.target.value)}
                >
                  {[1, 2, 3, 4, 5].map((valor) => (
                    <option key={valor} value={valor}>
                      {valor} de 5 {'★'.repeat(valor)}
                    </option>
                  ))}
                </select>
              </div>
              <div className="campo">
                <label className="campo-etiqueta" htmlFor="cal-comentario">
                  Comentario <span className="campo-opcional">(opcional)</span>
                </label>
                <textarea
                  id="cal-comentario"
                  rows={3}
                  value={comentario}
                  maxLength={2000}
                  onChange={(e) => setComentario(e.target.value)}
                  placeholder="Cuéntanos cómo fue…"
                />
              </div>
            </div>
            <button
              className="btn btn-primario"
              type="button"
              disabled={procesando || !sesionId}
              style={{ marginTop: 'var(--esp-4)' }}
              onClick={() => void ejecutar(
                () => calificarSesion(idSesion, Number(calificacion), comentario.trim()),
                'Reseña registrada.',
              )}
            >
              <Star size={16} aria-hidden="true" /> Enviar calificación
            </button>
          </div>
        </div>
      </div>

      {exito && <Alerta tipo="exito">{exito}</Alerta>}
      {error && <Alerta tipo="error">{error}</Alerta>}
    </section>
  )
}
