import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import {
  ArrowRight,
  BadgeCheck,
  CalendarDays,
  Compass,
  House,
  Plus,
  ShieldCheck,
  Sparkles,
  WalletCards,
} from 'lucide-react'
import { useAuth } from '../../auth/useAuth'
import { ApiError } from '../../services/api'
import {
  buscarOfertas,
  listarHabilidades,
  obtenerSugerencias,
  registrarInteresAprendizaje,
} from '../../services/catalogo'
import type {
  Habilidad,
  Oferta,
  SugerenciaAprendizaje,
} from '../../services/catalogo'
import type { VistaDominio } from './DomainViews'
import {
  VistaBilletera,
  VistaSesiones,
  VistaPublicarOferta,
} from './DomainViews'
import { VistaAdministracion, VistaPasaporte } from './PassportViews'
import {
  Alerta,
  EstadoVacio,
  IconoCategoria,
  OfertaSkeleton,
  useToast,
} from '../../components/ui'

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

export function LearningDashboard() {
  const { perfil, cerrarSesion } = useAuth()
  const toast = useToast()
  const [vistaActiva, setVistaActiva] = useState<VistaDominio>('inicio')
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
  const [categorias, setCategorias] = useState<Map<string, string>>(new Map())
  const [habilidadesSolicitadas, setHabilidadesSolicitadas] = useState<Set<number>>(new Set())
  const [solicitandoHabilidad, setSolicitandoHabilidad] = useState<number | null>(null)

  /* Mapa habilidad → categoría (para iconos) */
  useEffect(() => {
    let vigente = true
    void listarHabilidades()
      .then((habilidades: Habilidad[]) => {
        if (!vigente) return
        setCategorias(
          new Map(habilidades.map((habilidad) => [habilidad.nombre, habilidad.categoria])),
        )
      })
      .catch(() => {
        /* el mapa de categorías es cosmético; si falla, iconos de respaldo */
      })
    return () => {
      vigente = false
    }
  }, [])

  /* Búsqueda con debounce */
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

  /* Sugerencias */
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
      .catch(() => {
        /* sugerencias son enriquecimiento; su fallo no bloquea la pantalla */
        if (vigente) setSugerencias([])
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
    try {
      await registrarInteresAprendizaje(sugerencia.habilidadId)
      setHabilidadesSolicitadas((anteriores) =>
        new Set(anteriores).add(sugerencia.habilidadId))
      toast('exito', 'Agregamos esta habilidad a tus intereses.')
    } catch (error) {
      const mensaje = error instanceof ApiError && error.status === 409
        ? error.message
        : mostrarError(error)
      toast('error', mensaje)
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
    <div className="app-shell">
      {/* Sidebar */}
      <aside className="sidebar">
        <a className="marca sidebar-marca" href="#inicio">
          <span className="marca-sello" aria-hidden="true">PS</span>
          <span>Pasaporte<span className="marca-suave"> de Saberes</span></span>
        </a>
        <p className="sidebar-seccion-etiqueta">Tu aprendizaje</p>
        <nav className="sidebar-nav" aria-label="Navegación de aprendizaje">
          <NavBoton
            activo={vistaActiva === 'inicio'}
            onClick={() => setVistaActiva('inicio')}
            icono={<House aria-hidden="true" />}
          >
            Inicio
          </NavBoton>
          <NavBoton
            activo={vistaActiva === 'explorar'}
            onClick={() => setVistaActiva('explorar')}
            icono={<Compass aria-hidden="true" />}
          >
            Explorar
          </NavBoton>
          {perfil.tipoCuenta === 'PERSONA' && (
            <NavBoton
              activo={vistaActiva === 'recomendado'}
              onClick={() => setVistaActiva('recomendado')}
              icono={<Sparkles aria-hidden="true" />}
            >
              Para ti
            </NavBoton>
          )}
          <NavBoton
            activo={vistaActiva === 'billetera'}
            onClick={() => setVistaActiva('billetera')}
            icono={<WalletCards aria-hidden="true" />}
          >
            Billetera
          </NavBoton>
          <NavBoton
            activo={vistaActiva === 'sesiones'}
            onClick={() => setVistaActiva('sesiones')}
            icono={<CalendarDays aria-hidden="true" />}
          >
            Sesiones
          </NavBoton>
          <NavBoton
            activo={vistaActiva === 'pasaporte'}
            onClick={() => setVistaActiva('pasaporte')}
            icono={<BadgeCheck aria-hidden="true" />}
          >
            Pasaporte
          </NavBoton>
          {perfil.tipoCuenta === 'PERSONA' && (
            <NavBoton
              activo={vistaActiva === 'publicar'}
              onClick={() => setVistaActiva('publicar')}
              icono={<Plus aria-hidden="true" />}
            >
              Publicar
            </NavBoton>
          )}
          {perfil.tipoCuenta === 'ADMIN' && (
            <NavBoton
              activo={vistaActiva === 'administracion'}
              onClick={() => setVistaActiva('administracion')}
              icono={<ShieldCheck aria-hidden="true" />}
            >
              Administración
            </NavBoton>
          )}
        </nav>
        <div className="sidebar-perfil">
          <span className="avatar" aria-hidden="true">{iniciales(perfil.nombre)}</span>
          <span className="sidebar-perfil-copia">
            <strong>{perfil.nombre}</strong>
            <small>{perfil.correo}</small>
          </span>
        </div>
      </aside>

      {/* Contenido */}
      <div className="contenido-app">
        <header className="topbar-app">
          <div className="topbar-breadcrumb">
            Pasaporte <span>/</span> {ETIQUETAS_VISTA[vistaActiva]}
          </div>
          <button className="btn btn-fantasma btn-sm" type="button" onClick={cerrarSesion}>
            Cerrar sesión
          </button>
        </header>

        <main className="main-app" id="inicio">
          {vistaActiva === 'billetera' && <VistaBilletera />}
          {vistaActiva === 'sesiones' && <VistaSesiones />}
          {vistaActiva === 'pasaporte' && <VistaPasaporte />}
          {vistaActiva === 'publicar' && <VistaPublicarOferta />}
          {vistaActiva === 'administracion' && <VistaAdministracion />}

          {(vistaActiva === 'inicio' || vistaActiva === 'explorar' || vistaActiva === 'recomendado') && (
            <>
              {vistaActiva === 'inicio' && (
                <section className="bienvenida" aria-labelledby="bienvenida-titulo">
                  <div className="bienvenida-copia">
                    <p className="eyebrow">
                      <span className="eyebrow-punto" aria-hidden="true" />
                      Tu siguiente paso empieza aquí
                    </p>
                    <h1 className="bienvenida-h1" id="bienvenida-titulo">
                      Hola, {perfil.nombre.split(' ')[0]}.
                    </h1>
                    <p className="bienvenida-subtitulo">
                      ¿Qué te gustaría aprender o compartir hoy?
                    </p>
                    <form
                      className="busqueda-bienvenida"
                      role="search"
                      onSubmit={enviarBusqueda}
                    >
                      <label className="visually-hidden" htmlFor="buscar-ofertas">Buscar saberes</label>
                      <input
                        id="buscar-ofertas"
                        name="busqueda"
                        type="search"
                        placeholder="Busca una habilidad, por ejemplo: guitarra"
                        defaultValue={busqueda}
                      />
                      <button className="btn btn-primario" type="submit">Buscar</button>
                    </form>
                  </div>
                  <div className="bienvenida-arte" aria-hidden="true">
                    <Sparkles />
                  </div>
                </section>
              )}

              {perfil.tipoCuenta === 'PERSONA' &&
                (vistaActiva === 'inicio' || vistaActiva === 'recomendado') && (
                <section className="seccion-app" id="recomendado" aria-labelledby="recomendado-titulo">
                  <div className="seccion-app-cabecera">
                    <div>
                      <p className="eyebrow">Conexiones que pueden inspirarte</p>
                      <h2 id="recomendado-titulo">Un buen lugar para empezar</h2>
                    </div>
                    <span className="seccion-marcador">Para ti</span>
                  </div>

                  {cargandoSugerencias ? (
                    <div className="ofertas-skeleton" role="status" aria-label="Cargando sugerencias">
                      <OfertaSkeleton />
                      <OfertaSkeleton />
                      <OfertaSkeleton />
                    </div>
                  ) : sugerenciasVisibles.length > 0 ? (
                    <div className="recomendaciones-grid">
                      {sugerenciasVisibles.map(({ sugerencia, oferta }) => {
                        const yaSolicitada = habilidadesSolicitadas.has(sugerencia.habilidadId)
                        const enviandoEsta = solicitandoHabilidad === sugerencia.habilidadId
                        const categoria = categorias.get(oferta.habilidad) ?? ''
                        return (
                          <article className="recomendacion-card" key={sugerencia.ofertaId}>
                            <div className="recomendacion-top">
                              <span className="recomendacion-icono" aria-hidden="true">
                                <IconoCategoria categoria={categoria} />
                              </span>
                              <span className={`chip${sugerencia.cruceMutuo ? ' chip-oro' : ' chip-neutro'}`}>
                                {sugerencia.cruceMutuo ? 'Interés mutuo' : 'Sugerencia'}
                              </span>
                            </div>
                            <p className="kicker">Podría interesarte</p>
                            <h3>{oferta.habilidad}</h3>
                            <p className="recomendacion-tutor">Con {oferta.nombreTutor}</p>
                            <p className="recomendacion-descripcion">{oferta.descripcion}</p>
                            <button
                              className="accion-texto recomendacion-accion"
                              type="button"
                              disabled={yaSolicitada || enviandoEsta}
                              onClick={() => void solicitarAprender(sugerencia)}
                            >
                              {yaSolicitada
                                ? 'En tus intereses'
                                : enviandoEsta
                                  ? 'Enviando…'
                                  : 'Quiero aprender esto'}
                              {!yaSolicitada && !enviandoEsta && <ArrowRight size={14} aria-hidden="true" />}
                            </button>
                          </article>
                        )
                      })}
                    </div>
                  ) : (
                    <EstadoVacio
                      numero="§"
                      titulo="Las buenas conexiones toman tiempo"
                      descripcion="Cuando haya una coincidencia para tu perfil, la verás aquí. Mientras tanto, explora los saberes disponibles."
                      accion={
                        <button className="accion-texto" type="button" onClick={() => setVistaActiva('explorar')}>
                          Explorar ofertas <ArrowRight size={14} aria-hidden="true" />
                        </button>
                      }
                    />
                  )}
                </section>
              )}

              {(vistaActiva === 'inicio' || vistaActiva === 'explorar') && (
                <section className="seccion-app" id="explorar" aria-labelledby="explorar-titulo">
                  <div className="seccion-app-cabecera">
                    <div>
                      <p className="eyebrow">Saberes compartidos por la comunidad</p>
                      <h2 id="explorar-titulo">Explora nuevas habilidades</h2>
                    </div>
                    <span className="seccion-marcador">{ofertas.length} en pantalla</span>
                  </div>

                  {cargandoOfertas ? (
                    <div className="ofertas-skeleton" role="status" aria-label="Cargando saberes">
                      <OfertaSkeleton />
                      <OfertaSkeleton />
                      <OfertaSkeleton />
                      <OfertaSkeleton />
                      <OfertaSkeleton />
                      <OfertaSkeleton />
                    </div>
                  ) : errorOfertas ? (
                    <Alerta tipo="error">{errorOfertas}</Alerta>
                  ) : ofertas.length > 0 ? (
                    <>
                      <div className="ofertas-grid">
                        {ofertas.map((oferta) => (
                          <TarjetaOferta
                            oferta={oferta}
                            categoria={categorias.get(oferta.habilidad) ?? ''}
                            key={oferta.ofertaId}
                          />
                        ))}
                      </div>
                      {hayMas && (
                        <button
                          className="btn btn-secundario btn-cargar-mas"
                          type="button"
                          disabled={cargandoMas}
                          onClick={() => void cargarMasOfertas()}
                        >
                          {cargandoMas ? 'Cargando…' : 'Ver más saberes'}
                        </button>
                      )}
                    </>
                  ) : (
                    <EstadoVacio
                      numero={busqueda ? '∅' : '01'}
                      titulo={busqueda ? 'No encontramos resultados' : 'Todavía no hay saberes publicados'}
                      descripcion={
                        busqueda
                          ? 'Prueba con otra palabra para ampliar tu búsqueda.'
                          : 'Cuando la comunidad publique ofertas, aparecerán aquí.'
                      }
                    />
                  )}
                </section>
              )}
            </>
          )}
        </main>
      </div>
    </div>
  )
}

const ETIQUETAS_VISTA: Record<VistaDominio, string> = {
  inicio: 'Inicio',
  explorar: 'Explorar',
  recomendado: 'Para ti',
  billetera: 'Billetera',
  sesiones: 'Sesiones',
  pasaporte: 'Pasaporte',
  publicar: 'Publicar',
  administracion: 'Administración',
}

function NavBoton({
  activo,
  onClick,
  icono,
  children,
}: {
  activo: boolean
  onClick: () => void
  icono: React.ReactNode
  children: React.ReactNode
}) {
  return (
    <button
      className={`sidebar-nav-enlace${activo ? ' activo' : ''}`}
      type="button"
      aria-current={activo ? 'page' : undefined}
      onClick={onClick}
    >
      {icono}
      {children}
    </button>
  )
}

function TarjetaOferta({
  oferta,
  categoria,
}: {
  oferta: Oferta
  categoria: string
}) {
  return (
    <article className="oferta-card">
      <div className="oferta-card-top">
        <span className="oferta-icono" aria-hidden="true">
          <IconoCategoria categoria={categoria} />
        </span>
        <span className="chip chip-neutro">{formatoHoras(oferta.duracionHoras)}</span>
      </div>
      <p className="kicker">Saber de la comunidad</p>
      <h3>{oferta.habilidad}</h3>
      <p className="oferta-descripcion">{oferta.descripcion}</p>
      <div className="oferta-tutor">
        <span className="avatar avatar-sm" aria-hidden="true">{iniciales(oferta.nombreTutor)}</span>
        <span>Comparte <strong>{oferta.nombreTutor}</strong></span>
      </div>
      <div className="oferta-meta">
        <span className="chip chip-neutro">
          {oferta.modalidad === 'PRESENCIAL' ? 'Presencial' : 'Virtual'}
        </span>
        {oferta.barrio && <span className="chip chip-neutro">{oferta.barrio}</span>}
        {typeof oferta.calificacion === 'number' && (
          <span className="chip chip-oro" aria-label={`Calificación ${oferta.calificacion} de 5`}>
            {oferta.calificacion.toFixed(1)} / 5
          </span>
        )}
      </div>
      {(oferta.rango || oferta.nivelCredencial) && (
        <div className="oferta-credencial">
          {oferta.rango && <span className="chip chip-primario">{oferta.rango}</span>}
          {oferta.nivelCredencial && <span className="chip chip-primario">{oferta.nivelCredencial}</span>}
        </div>
      )}
    </article>
  )
}
