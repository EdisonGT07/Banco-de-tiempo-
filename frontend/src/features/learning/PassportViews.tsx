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
      .then((resultado) => {
        if (vigente) setPasaporte(resultado)
      })
      .catch((problema: unknown) => {
        if (vigente) setError(problema instanceof Error ? problema.message : 'No se pudo cargar el pasaporte.')
      })
      .finally(() => {
        if (vigente) setCargando(false)
      })
    return () => {
      vigente = false
    }
  }, [personaId])

  async function recalcular() {
    setProcesando(true)
    setError(null)
    try {
      setRangos(await recalcularRangos())
    } catch (problema) {
      setError(problema instanceof Error ? problema.message : 'No se pudieron recalcular los rangos.')
    } finally {
      setProcesando(false)
    }
  }

  return (
    <section className="domain-view">
      <div className="learning-section-heading">
        <div>
          <p className="eyebrow">TUS APRENDIZAJES Y LOGROS</p>
          <h2>Mi pasaporte</h2>
        </div>
        <button className="button button-outline" type="button" disabled={procesando} onClick={() => void recalcular()}>
          {procesando ? 'Calculando...' : 'Recalcular rangos'}
        </button>
      </div>
      {cargando ? (
        <p className="learning-status" role="status">Abriendo tu pasaporte...</p>
      ) : pasaporte ? (
        <>
          {rangos && (
            <div className="rank-grid">
              {rangos.ensenanza && <RankCard titulo="Enseñanza" rango={rangos.ensenanza.nombre} />}
              {rangos.aprendizaje && <RankCard titulo="Aprendizaje" rango={rangos.aprendizaje.nombre} />}
            </div>
          )}
          {pasaporte.credenciales.length > 0 ? (
            <div className="credential-grid">
              {pasaporte.credenciales.map((credencial) => (
                <article className="credential-card" key={credencial.id}>
                  <div className="credential-badge">{credencial.habilidad.slice(0, 1).toLocaleUpperCase('es')}</div>
                  <p className="card-kicker">{credencial.nivel}</p>
                  <h3>{credencial.habilidad}</h3>
                  <dl className="credential-metrics">
                    <div><dt>Horas</dt><dd>{credencial.horasEnsenadas}</dd></div>
                    <div><dt>Rating</dt><dd>{credencial.rating?.toFixed?.(1) ?? '—'}</dd></div>
                  </dl>
                  {credencial.aval ? (
                    <span className="aval-badge">Avalado hasta {credencial.aval.vigenciaHasta}</span>
                  ) : (
                    <span className="aval-badge pending">Sin aval institucional</span>
                  )}
                  <button
                    className="text-action"
                    type="button"
                    onClick={() => void descargarCertificado(credencial.id).catch((problema: unknown) =>
                      setError(problema instanceof Error ? problema.message : 'No se pudo descargar.'))
                    }
                  >
                    Descargar certificado <Download size={14} />
                  </button>
                </article>
              ))}
            </div>
          ) : (
            <div className="learning-empty">
              <span className="empty-icon"><FileBadge size={18} /></span>
              <div>
                <h3>Aún no tienes credenciales visibles</h3>
                <p>Las credenciales aparecerán al completar sesiones y acumular resultados verificables.</p>
              </div>
            </div>
          )}
        </>
      ) : null}
      {error && <p className="learning-error" role="alert">{error}</p>}
    </section>
  )
}

function RankCard({ titulo, rango }: { titulo: string; rango: string }) {
  return (
    <article className="rank-card">
      <p>{titulo}</p>
      <strong>{rango}</strong>
    </article>
  )
}

export function VistaAdministracion() {
  const { perfil } = useAuth()
  const [panel, setPanel] = useState<PanelImpacto | null>(null)
  const [reporteId, setReporteId] = useState('')
  const [personaId, setPersonaId] = useState('')
  const [mensaje, setMensaje] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [procesando, setProcesando] = useState(false)

  useEffect(() => {
    if (perfil?.tipoCuenta !== 'ADMIN') return
    let vigente = true
    void obtenerPanelImpacto()
      .then((resultado) => {
        if (vigente) setPanel(resultado)
      })
      .catch((problema: unknown) => {
        if (vigente) setError(problema instanceof Error ? problema.message : 'No se pudo cargar el panel.')
      })
    return () => {
      vigente = false
    }
  }, [perfil?.tipoCuenta])

  async function ejecutarAccion(accion: () => Promise<unknown>, texto: string) {
    setProcesando(true)
    setMensaje(null)
    setError(null)
    try {
      await accion()
      setMensaje(texto)
    } catch (problema) {
      setError(problema instanceof Error ? problema.message : 'No se pudo completar la acción.')
    } finally {
      setProcesando(false)
    }
  }

  if (perfil?.tipoCuenta !== 'ADMIN') {
    return (
      <section className="domain-view">
        <div className="learning-empty">
          <h3>Esta sección requiere una cuenta administradora</h3>
        </div>
      </section>
    )
  }

  return (
    <section className="domain-view">
      <div className="learning-section-heading">
        <div>
          <p className="eyebrow">CONTROL Y SEGUIMIENTO</p>
          <h2>Administración</h2>
        </div>
      </div>
      {panel && (
        <div className="impact-grid">
          <MetricCard label="Horas intercambiadas" value={String(panel.horasIntercambiadas)} />
          <MetricCard label="Personas formadas" value={String(panel.personasFormadas)} />
          <MetricCard label="Habilidades nuevas" value={String(panel.habilidadesNuevas)} />
          <MetricCard label="Credenciales" value={String(panel.credencialesEmitidas)} />
          <MetricCard label="Verificaciones" value={String(panel.verificaciones)} />
        </div>
      )}
      <div className="session-layout">
        <div className="domain-card">
          <h3>Revisión de reporte</h3>
          <label className="domain-field">
            ID del reporte
            <input type="number" min="1" value={reporteId} onChange={(event) => setReporteId(event.target.value)} />
          </label>
          <div className="action-grid">
            <button
              className="button button-primary"
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
              className="button button-outline"
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
        <div className="domain-card">
          <h3>Desbloquear persona</h3>
          <label className="domain-field">
            ID de la persona
            <input type="number" min="1" value={personaId} onChange={(event) => setPersonaId(event.target.value)} />
          </label>
          <button
            className="button button-primary domain-submit"
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
      {mensaje && <p className="learning-success" role="status">{mensaje}</p>}
      {error && <p className="learning-error" role="alert">{error}</p>}
    </section>
  )
}

function MetricCard({ label, value }: { label: string; value: string }) {
  return (
    <article className="metric-card">
      <strong>{value}</strong>
      <p>{label}</p>
    </article>
  )
}
