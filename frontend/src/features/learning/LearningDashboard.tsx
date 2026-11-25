import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { useAuth } from '../../auth/useAuth'
import { ApiError } from '../../services/api'
import {
  buscarOfertas,
  obtenerSugerencias,
  registrarInteresAprendizaje,
} from '../../services/catalogo'
import type {
  Oferta,
  SugerenciaAprendizaje,
} from '../../services/catalogo'

const TAMANO_PAGINA = 12

function mostrarError(error: unknown): string {
  return error instanceof Error
    ? error.message
    : 'No se pudo completar la solicitud. Intenta de nuevo.'
}

function iniciales(nombre: string): string {
  return nombre
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((parte) => parte[0].toLocaleUpperCase('es'))
    .join('')
}

function formatoHoras(horas: number): string {
  return `${new Intl.NumberFormat('es-CO', { maximumFractionDigits: 2 }).format(horas)} h`
}

function esModalidadPresencial(modalidad: string): boolean {
  return modalidad === 'PRESENCIAL'
}

export function LearningDashboard() {
  const { perfil, cerrarSesion } = useAuth()
  const [busqueda, setBusqueda] = useState('')
  const [ofertas, setOfertas] = useState<Oferta[]>([])
  const [pagina, setPagina] = useState(0)
  const [hayMas, setHayMas] = useState(false)
  const [cargandoOfertas, setCargandoOfertas] = useState(true)
  const [cargandoMas, setCargandoMas] = useState(false)
  const [errorOfertas, setErrorOfertas] = useState<string | null>(null)
  const [sugerencias, setSugerencias] = useState<SugerenciaAprendizaje[]>([])
  const [ofertasSugeridas, setOfertasSugeridas] = useState<Map<number, Oferta>>(new Map())
  const [cargandoSugerencias, setCargandoSugerencias] = useState(true)
  const [errorSugerencias, setErrorSugerencias] = useState<string | null>(null)
  const [habilidadesSolicitadas, setHabilidadesSolicitadas] = useState<Set<number>>(new Set())
  const [solicitandoHabilidad, setSolicitandoHabilidad] = useState<number | null>(null)
  const [mensajeSolicitud, setMensajeSolicitud] = useState<string | null>(null)
  const [errorSolicitud, setErrorSolicitud] = useState<string | null>(null)

  useEffect(() => {
    let vigente = true

    const temporizador = window.setTimeout(() => {
      setCargandoOfertas(true)
      setErrorOfertas(null)
      void buscarOfertas({ texto: busqueda, pagina: 0, tamanoPagina: TAMANO_PAGINA })
        .then((resultados) => {
          if (!vigente) return
          setOfertas(resultados)
          setPagina(0)
          setHayMas(resultados.length === TAMANO_PAGINA)
        })
        .catch((error: unknown) => {
          if (vigente) setErrorOfertas(mostrarError(error))
        })
        .finally(() => {
          if (vigente) setCargandoOfertas(false)
        })
    }, busqueda ? 250 : 0)

    return () => {
      vigente = false
      window.clearTimeout(temporizador)
    }
  }, [busqueda])

  useEffect(() => {
    let vigente = true

    if (perfil?.tipoCuenta !== 'PERSONA') {
      return () => {
        vigente = false
      }
    }

    void obtenerSugerencias()
      .then(async (resultados) => {
        const habilidadIds = [
          ...new Set(resultados.map((sugerencia) => sugerencia.habilidadId)),
        ].slice(0, 6)
        const gruposOfertas = await Promise.all(
          habilidadIds.map((habilidadId) => buscarOfertas({
            habilidadId,
            pagina: 0,
            tamanoPagina: TAMANO_PAGINA,
          })),
        )
        if (!vigente) return

        const mapaOfertas = new Map<number, Oferta>()
        for (const grupo of gruposOfertas) {
          for (const oferta of grupo) {
            mapaOfertas.set(oferta.ofertaId, oferta)
          }
        }
        setSugerencias(resultados)
        setOfertasSugeridas(mapaOfertas)
      })
      .catch((error: unknown) => {
        if (vigente) setErrorSugerencias(mostrarError(error))
      })
      .finally(() => {
        if (vigente) setCargandoSugerencias(false)
      })

    return () => {
      vigente = false
    }
  }, [perfil?.id, perfil?.tipoCuenta])

  function enviarBusqueda(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const campo = event.currentTarget.elements.namedItem('busqueda')
    if (campo instanceof HTMLInputElement) {
      setBusqueda(campo.value.trim())
    }
  }

  async function cargarMasOfertas() {
    setCargandoMas(true)
    setErrorOfertas(null)
    try {
      const siguientePagina = pagina + 1
      const resultados = await buscarOfertas({
        texto: busqueda,
        pagina: siguientePagina,
        tamanoPagina: TAMANO_PAGINA,
      })
      setOfertas((actuales) => [...actuales, ...resultados])
      setPagina(siguientePagina)
      setHayMas(resultados.length === TAMANO_PAGINA)
    } catch (error) {
      setErrorOfertas(mostrarError(error))
    } finally {
      setCargandoMas(false)
    }
  }

  async function solicitarAprender(sugerencia: SugerenciaAprendizaje) {
    setSolicitandoHabilidad(sugerencia.habilidadId)
    setMensajeSolicitud(null)
    setErrorSolicitud(null)
    try {
      await registrarInteresAprendizaje(sugerencia.habilidadId)
      setHabilidadesSolicitadas((anteriores) =>
        new Set(anteriores).add(sugerencia.habilidadId))
      setMensajeSolicitud('Listo, agregamos esta habilidad a tus intereses.')
    } catch (error) {
      setErrorSolicitud(
        error instanceof ApiError && error.status === 409
          ? error.message
          : mostrarError(error),
      )
    } finally {
      setSolicitandoHabilidad(null)
    }
  }

  if (!perfil) return null

  const sugerenciasVisibles = sugerencias
    .map((sugerencia) => ({
      sugerencia,
      oferta: ofertasSugeridas.get(sugerencia.ofertaId),
    }))
    .filter((resultado): resultado is {
      sugerencia: SugerenciaAprendizaje
      oferta: Oferta
    } => resultado.oferta !== undefined)

  return (
    <div className="learning-shell">
      <aside className="learning-sidebar">
        <a className="brand learning-brand" href="#inicio">
          <span className="brand-mark">PS</span>
          <span>Pasaporte<span className="brand-light"> de Saberes</span></span>
        </a>
        <p className="sidebar-label">TU APRENDIZAJE</p>
        <nav className="learning-nav" aria-label="Navegación de aprendizaje">
          <a className="learning-nav-link active" href="#inicio"><span>⌂</span> Inicio</a>
          <a className="learning-nav-link" href="#explorar"><span>⌕</span> Explorar saberes</a>
          {perfil.tipoCuenta === 'PERSONA' && (
            <a className="learning-nav-link" href="#recomendado"><span>✦</span> Para ti</a>
          )}
        </nav>
        <div className="sidebar-profile">
          <span className="profile-avatar">{iniciales(perfil.nombre)}</span>
          <span className="sidebar-profile-copy">
            <strong>{perfil.nombre}</strong>
            <small>{perfil.correo}</small>
          </span>
        </div>
      </aside>

      <div className="learning-content">
        <header className="learning-topbar">
          <div className="breadcrumb">Pasaporte <span>/</span> Mi espacio</div>
          <button className="button button-outline" type="button" onClick={cerrarSesion}>
            Cerrar sesión
          </button>
        </header>

        <main className="learning-main" id="inicio">
          <section className="welcome-card">
            <div className="welcome-copy">
              <p className="eyebrow">TU SIGUIENTE PASO EMPIEZA AQUÍ</p>
              <h1>¡Hola, {perfil.nombre.split(' ')[0]}!</h1>
              <p>¿Qué te gustaría aprender o compartir hoy?</p>
              <form className="learning-search" role="search" onSubmit={enviarBusqueda}>
                <label className="visually-hidden" htmlFor="search-offers">Buscar saberes</label>
                <input
                  id="search-offers"
                  name="busqueda"
                  type="search"
                  placeholder="Busca una habilidad, por ejemplo: guitarra"
                  defaultValue={busqueda}
                />
                <button className="button button-primary" type="submit">Buscar</button>
              </form>
            </div>
            <div className="welcome-art" aria-hidden="true">
              <span className="welcome-sun" />
              <span className="welcome-book">✧</span>
              <span className="welcome-star">✦</span>
              <span className="welcome-dot" />
            </div>
          </section>

          {perfil.tipoCuenta === 'PERSONA' && (
            <section className="learning-section" id="recomendado">
              <div className="learning-section-heading">
                <div>
                  <p className="eyebrow">CONEXIONES QUE PUEDEN INSPIRARTE</p>
                  <h2>Un buen lugar para empezar</h2>
                </div>
                <span className="section-marker">PARA TI</span>
              </div>
              {cargandoSugerencias ? (
                <p className="learning-status" role="status">Buscando sugerencias para ti...</p>
              ) : errorSugerencias ? (
                <p className="learning-error" role="alert">{errorSugerencias}</p>
              ) : sugerenciasVisibles.length > 0 ? (
                <>
                  <div className="recommendation-grid">
                    {sugerenciasVisibles.map(({ sugerencia, oferta }) => {
                      const yaSolicitada = habilidadesSolicitadas.has(sugerencia.habilidadId)
                      const enviandoEsta = solicitandoHabilidad === sugerencia.habilidadId
                      return (
                        <article className="recommendation-card" key={sugerencia.ofertaId}>
                          <div className="recommendation-top">
                            <span className="recommendation-icon">{oferta.habilidad.slice(0, 1).toLocaleUpperCase('es')}</span>
                            <span className={sugerencia.cruceMutuo ? 'match-badge mutual' : 'match-badge'}>
                              {sugerencia.cruceMutuo ? 'Interés mutuo' : 'Sugerencia'}
                            </span>
                          </div>
                          <p className="card-kicker">PODRÍA INTERESARTE</p>
                          <h3>{oferta.habilidad}</h3>
                          <p className="recommendation-tutor">Con {oferta.nombreTutor}</p>
                          <p className="recommendation-description">{oferta.descripcion}</p>
                          <button
                            className="text-action"
                            type="button"
                            disabled={yaSolicitada || enviandoEsta}
                            onClick={() => void solicitarAprender(sugerencia)}
                          >
                            {yaSolicitada
                              ? 'Añadido a tus intereses'
                              : enviandoEsta
                                ? 'Enviando...'
                                : 'Quiero aprender esto'}
                            {!yaSolicitada && !enviandoEsta && <span aria-hidden="true">→</span>}
                          </button>
                        </article>
                      )
                    })}
                  </div>
                  {mensajeSolicitud && <p className="learning-success" role="status">{mensajeSolicitud}</p>}
                  {errorSolicitud && <p className="learning-error" role="alert">{errorSolicitud}</p>}
                </>
              ) : (
                <div className="learning-empty recommendation-empty">
                  <span className="empty-icon">✦</span>
                  <div>
                    <h3>Las buenas conexiones toman tiempo</h3>
                    <p>Cuando haya una coincidencia para tu perfil, la verás aquí. Mientras tanto, explora los saberes disponibles.</p>
                  </div>
                  <a className="text-action" href="#explorar">Explorar ofertas <span aria-hidden="true">↓</span></a>
                </div>
              )}
            </section>
          )}

          <section className="learning-section" id="explorar">
            <div className="learning-section-heading">
              <div>
                <p className="eyebrow">SABERES COMPARTIDOS POR LA COMUNIDAD</p>
                <h2>Explora nuevas habilidades</h2>
              </div>
              <span className="section-marker">{ofertas.length} EN PANTALLA</span>
            </div>

            {cargandoOfertas ? (
              <p className="learning-status" role="status">Cargando saberes...</p>
            ) : errorOfertas ? (
              <div className="learning-empty">
                <h3>No pudimos cargar las ofertas</h3>
                <p>{errorOfertas}</p>
              </div>
            ) : ofertas.length > 0 ? (
              <>
                <div className="offer-grid">
                  {ofertas.map((oferta) => (
                    <OfertaCard oferta={oferta} key={oferta.ofertaId} />
                  ))}
                </div>
                {hayMas && (
                  <button
                    className="button button-outline load-more"
                    type="button"
                    disabled={cargandoMas}
                    onClick={() => void cargarMasOfertas()}
                  >
                    {cargandoMas ? 'Cargando...' : 'Ver más saberes'}
                  </button>
                )}
              </>
            ) : (
              <div className="learning-empty">
                <span className="empty-icon">⌕</span>
                <h3>{busqueda ? 'No encontramos resultados' : 'Todavía no hay saberes publicados'}</h3>
                <p>
                  {busqueda
                    ? 'Prueba con otra palabra para ampliar tu búsqueda.'
                    : 'Cuando la comunidad publique ofertas, aparecerán aquí.'}
                </p>
              </div>
            )}
          </section>
        </main>
      </div>
    </div>
  )
}

function OfertaCard({ oferta }: { oferta: Oferta }) {
  return (
    <article className="offer-card">
      <div className="offer-card-top">
        <span className="offer-icon">{oferta.habilidad.slice(0, 1).toLocaleUpperCase('es')}</span>
        <span className="offer-duration">{formatoHoras(oferta.duracionHoras)}</span>
      </div>
      <p className="card-kicker">SABER DE LA COMUNIDAD</p>
      <h3>{oferta.habilidad}</h3>
      <p className="offer-description">{oferta.descripcion}</p>
      <div className="offer-tutor">
        <span className="tutor-avatar">{iniciales(oferta.nombreTutor)}</span>
        <span>Comparte <strong>{oferta.nombreTutor}</strong></span>
      </div>
      <div className="offer-meta">
        <span>{esModalidadPresencial(oferta.modalidad) ? 'Presencial' : 'Virtual'}</span>
        {oferta.barrio && <span>{oferta.barrio}</span>}
        {typeof oferta.calificacion === 'number' && (
          <span aria-label={`Calificación ${oferta.calificacion} de 5`}>★ {oferta.calificacion.toFixed(1)}</span>
        )}
      </div>
      {(oferta.rango || oferta.nivelCredencial) && (
        <div className="offer-credential">
          {oferta.rango && <span>{oferta.rango}</span>}
          {oferta.nivelCredencial && <span>{oferta.nivelCredencial}</span>}
        </div>
      )}
    </article>
  )
}
