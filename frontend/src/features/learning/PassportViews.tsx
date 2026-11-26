import { useEffect, useState } from 'react'
import { Download, FileBadge } from 'lucide-react'
import { useAuth } from '../../auth/useAuth'
import {
  descargarCertificado,
  obtenerPasaporte,
  recalcularRangos,
} from '../../services/pasaporte'
import type { Pasaporte, Rangos } from '../../services/pasaporte'
import {
  desbloquearPersona,
  obtenerPanelImpacto,
  revisarReporte,
} from '../../services/administracion'
import type { PanelImpacto } from '../../services/administracion'
import { Alerta, EstadoVacio } from '../../components/ui'

/* ============================================================
   PASAPORTE
   ============================================================ */
export function VistaPasaporte() {
  const { perfil } = useAuth()
  const personaId = perfil?.id
  const [pasaporte, setPasaporte] = useState<Pasaporte | null>(null)
  const [rangos, setRangos] = useState<Rangos | null>(null)
  const [cargando, setCargando] = useState(true)
  const [procesando, setProcesando] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!personaId) return
    let vigente = true
    void obtenerPasaporte(personaId)
      .then((resultado) => { if (vigente) setPasaporte(resultado) })
      .catch((problema: unknown) => {
        if (vigente) setError(
          problema instanceof Error ? problema.message : 'No se pudo cargar el pasaporte.',
        )
      })
      .finally(() => { if (vigente) setCargando(false) })
    return () => { vigente = false }
  }, [personaId])

  async function recalcular() {
    setProcesando(true)
    setError(null)
    try {
      setRangos(await recalcularRangos())
    } catch (problema) {
      setError(
        problema instanceof Error ? problema.message : 'No se pudieron recalcular los rangos.',
      )
    } finally {
      setProcesando(false)
    }
  }

  return (
    <section className="dominio-vista">
      <div className="dominio-cabecera">
        <div>
          <p className="eyebrow">Tus aprendizajes y logros</p>
          <h2>Mi pasaporte</h2>
        </div>
        <button
          className="btn btn-secundario"
          type="button"
          disabled={procesando}
          onClick={() => void recalcular()}
        >
          {procesando ? 'Calculando…' : 'Recalcular rangos'}
        </button>
      </div>

      {cargando ? (
        <p className="estado-cargando" role="status">Abriendo tu pasaporte…</p>
      ) : pasaporte ? (
        <>
          {rangos && (
            <div className="rangos-grilla">
              {rangos.ensenanza && (
                <TarjetaRango titulo="Enseñanza" rango={rangos.ensenanza.nombre} />
              )}
              {rangos.aprendizaje && (
                <TarjetaRango titulo="Aprendizaje" rango={rangos.aprendizaje.nombre} />
              )}
            </div>
          )}

          {pasaporte.credenciales.length > 0 ? (
            <div className="credenciales-grid">
              {pasaporte.credenciales.map((credencial) => (
                <article className="credencial-card" key={credencial.id}>
                  <span
                    className={`credencial-sello${credencial.aval ? ' credencial-sello-validada' : ''}`}
                    aria-hidden="true"
                  >
                    <FileBadge />
                  </span>
                  <p className="kicker">{credencial.nivel}</p>
                  <h3>{credencial.habilidad}</h3>
                  <dl className="credencial-metricas">
                    <div>
                      <dt>Horas</dt>
                      <dd>{credencial.horasEnsenadas}</dd>
                    </div>
                    <div>
                      <dt>Rating</dt>
                      <dd>{credencial.rating?.toFixed?.(1) ?? '—'}</dd>
                    </div>
                  </dl>
                  {credencial.aval ? (
                    <span className="chip chip-oro" style={{ marginBottom: 'var(--esp-3)' }}>
                      Avalado hasta {credencial.aval.vigenciaHasta}
                    </span>
                  ) : (
                    <span className="chip chip-neutro" style={{ marginBottom: 'var(--esp-3)' }}>
                      Sin aval institucional
                    </span>
                  )}
                  <div className="credencial-accion">
                    <button
                      className="accion-texto"
                      type="button"
                      onClick={() => void descargarCertificado(credencial.id).catch((problema: unknown) =>
                        setError(
                          problema instanceof Error ? problema.message : 'No se pudo descargar.',
                        ))}
                    >
                      Descargar certificado <Download size={14} aria-hidden="true" />
                    </button>
                  </div>
                </article>
              ))}
            </div>
          ) : (
            <EstadoVacio
              numero="✦"
              titulo="Aún no tienes credenciales visibles"
              descripcion="Las credenciales aparecerán al completar sesiones y acumular resultados verificables."
            />
          )}
        </>
      ) : null}

      {error && <Alerta tipo="error">{error}</Alerta>}
    </section>
  )
}

function TarjetaRango({ titulo, rango }: { titulo: string; rango: string }) {
  return (
    <article className="rango-card">
      <p>{titulo}</p>
      <strong>{rango}</strong>
    </article>
  )
}

/* ============================================================
   ADMINISTRACIÓN
   ============================================================ */
export function VistaAdministracion() {
  const { perfil } = useAuth()
  const [panel, setPanel] = useState<PanelImpacto | null>(null)
  const [reporteId, setReporteId] = useState('')
  const [personaId, setPersonaId] = useState('')
  const [exito, setExito] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [procesando, setProcesando] = useState(false)

  useEffect(() => {
    if (perfil?.tipoCuenta !== 'ADMIN') return
    let vigente = true
    void obtenerPanelImpacto()
      .then((resultado) => { if (vigente) setPanel(resultado) })
      .catch((problema: unknown) => {
        if (vigente) setError(
          problema instanceof Error ? problema.message : 'No se pudo cargar el panel.',
        )
      })
    return () => { vigente = false }
  }, [perfil?.tipoCuenta])

  async function ejecutarAccion(accion: () => Promise<unknown>, texto: string) {
    setProcesando(true)
    setExito(null)
    setError(null)
    try {
      await accion()
      setExito(texto)
    } catch (problema) {
      setError(
        problema instanceof Error ? problema.message : 'No se pudo completar la acción.',
      )
    } finally {
      setProcesando(false)
    }
  }

  if (perfil?.tipoCuenta !== 'ADMIN') {
    return (
      <section className="dominio-vista">
        <EstadoVacio
          numero="⊘"
          titulo="Acceso restringido"
          descripcion="Esta sección requiere una cuenta administradora."
        />
      </section>
    )
  }

  return (
    <section className="dominio-vista">
      <div className="dominio-cabecera">
        <div>
          <p className="eyebrow">Control y seguimiento</p>
          <h2>Administración</h2>
        </div>
      </div>

      {panel && (
        <div className="impacto-grilla">
          <MetricaCard label="Horas intercambiadas" valor={String(panel.horasIntercambiadas)} />
          <MetricaCard label="Personas formadas" valor={String(panel.personasFormadas)} />
          <MetricaCard label="Habilidades nuevas" valor={String(panel.habilidadesNuevas)} />
          <MetricaCard label="Credenciales emitidas" valor={String(panel.credencialesEmitidas)} />
          <MetricaCard label="Verificaciones" valor={String(panel.verificaciones)} />
        </div>
      )}

      <div className="grilla-2" style={{ marginTop: 'var(--esp-6)' }}>
        <div className="panel">
          <h3>Revisión de reporte</h3>
          <div className="campo" style={{ marginBottom: 'var(--esp-4)' }}>
            <label className="campo-etiqueta" htmlFor="reporte-id">ID del reporte</label>
            <input
              id="reporte-id"
              type="number"
              min="1"
              value={reporteId}
              onChange={(e) => setReporteId(e.target.value)}
              inputMode="numeric"
            />
          </div>
          <div style={{ display: 'flex', gap: 'var(--esp-3)' }}>
            <button
              className="btn btn-primario"
              type="button"
              disabled={procesando || !reporteId}
              onClick={() => void ejecutarAccion(
                () => revisarReporte(Number(reporteId), true),
                'Reporte resuelto.',
              )}
            >
              Resolver
            </button>
            <button
              className="btn btn-peligro"
              type="button"
              disabled={procesando || !reporteId}
              onClick={() => void ejecutarAccion(
                () => revisarReporte(Number(reporteId), false),
                'Reporte rechazado.',
              )}
            >
              Rechazar
            </button>
          </div>
        </div>

        <div className="panel">
          <h3>Desbloquear persona</h3>
          <div className="campo" style={{ marginBottom: 'var(--esp-4)' }}>
            <label className="campo-etiqueta" htmlFor="persona-id">ID de la persona</label>
            <input
              id="persona-id"
              type="number"
              min="1"
              value={personaId}
              onChange={(e) => setPersonaId(e.target.value)}
              inputMode="numeric"
            />
          </div>
          <button
            className="btn btn-primario"
            type="button"
            disabled={procesando || !personaId}
            onClick={() => void ejecutarAccion(
              () => desbloquearPersona(Number(personaId)),
              'Persona desbloqueada.',
            )}
          >
            Desbloquear
          </button>
        </div>
      </div>

      {exito && <Alerta tipo="exito">{exito}</Alerta>}
      {error && <Alerta tipo="error">{error}</Alerta>}
    </section>
  )
}

function MetricaCard({ label, valor }: { label: string; valor: string }) {
  return (
    <article className="impacto-metrica">
      <p>{label}</p>
      <strong>{valor}</strong>
    </article>
  )
}
