import './App.css'
import { ArrowRight, ArrowUpRight, BookOpen, CircleDot, Code2, GraduationCap, Hammer, HeartPulse, Languages, Palette } from 'lucide-react'
import { AuthProvider } from './auth/AuthContext'
import { useAuth } from './auth/useAuth'
import { AccountPanel } from './features/auth/AccountPanel'
import { LearningDashboard } from './features/learning/LearningDashboard'
import { ProveedorToast } from './components/ui'

/* ------------------------------------------------------------------
   Áreas de saber — reflejan las 6 categorías reales del catálogo
   ------------------------------------------------------------------ */
const AREAS = [
  { num: '01', nombre: 'Tecnología',    detalle: 'Herramientas digitales, programación y datos', Icono: Code2 },
  { num: '02', nombre: 'Oficios',       detalle: 'Conocimientos prácticos para el día a día',    Icono: Hammer },
  { num: '03', nombre: 'Bienestar',     detalle: 'Hábitos, movimiento y equilibrio personal',    Icono: HeartPulse },
  { num: '04', nombre: 'Idiomas',       detalle: 'Lenguas que abren puertas y conectan mundos',  Icono: Languages },
  { num: '05', nombre: 'Arte y cultura', detalle: 'Expresión, creatividad y patrimonio',          Icono: Palette },
  { num: '06', nombre: 'Educación',     detalle: 'Matemáticas, ciencias y pedagogía aplicada',   Icono: GraduationCap },
]

/* ------------------------------------------------------------------
   Raíz
   ------------------------------------------------------------------ */
function App() {
  return (
    <AuthProvider>
      <ProveedorToast>
        <PaginaPrincipal />
      </ProveedorToast>
    </AuthProvider>
  )
}

function PaginaPrincipal() {
  const { perfil, cargandoPerfil } = useAuth()

  if (cargandoPerfil) {
    return <main className="app-iniciando" role="status">Cargando tu espacio…</main>
  }

  return perfil ? <LearningDashboard /> : <PaginaPublica />
}

/* ------------------------------------------------------------------
   Landing pública
   ------------------------------------------------------------------ */
function PaginaPublica() {
  return (
    <main className="pagina-publica">
      {/* Topbar */}
      <header className="topbar">
        <a className="marca" href="#" aria-label="Pasaporte de Saberes, inicio">
          <span className="marca-sello" aria-hidden="true">PS</span>
          <span>Pasaporte<span className="marca-suave"> de Saberes</span></span>
        </a>
        <nav aria-label="Navegación principal">
          <a href="#como-funciona">Cómo funciona</a>
          <a href="#areas">Áreas</a>
          <a href="#cuenta">Mi cuenta</a>
        </nav>
        <a className="btn btn-secundario btn-sm" href="#cuenta">Ingresar</a>
      </header>

      {/* Hero */}
      <section className="hero" aria-labelledby="hero-titulo">
        <div className="hero-copia">
          <p className="eyebrow">
            <span className="eyebrow-punto" aria-hidden="true" />
            Aprender también es compartir
          </p>
          <h1 className="hero-h1" id="hero-titulo">
            Tu tiempo tiene mucho <em>que enseñar.</em>
          </h1>
          <p className="hero-descripcion">
            Una comunidad donde intercambiamos conocimientos, crecemos juntos
            y cada hora compartida queda registrada en tu pasaporte.
          </p>
          <div className="hero-acciones">
            <a className="btn btn-primario btn-lg" href="#cuenta">
              Únete a la comunidad <ArrowRight size={16} aria-hidden="true" />
            </a>
            <a className="btn btn-secundario btn-lg" href="#areas">
              Explorar saberes
            </a>
          </div>
          <div className="hero-prueba">
            <span className="hero-prueba-linea" aria-hidden="true" />
            <p>Un saber compartido puede abrir nuevos caminos.</p>
          </div>
        </div>
        <div className="hero-visual" aria-hidden="true">
          <span className="hero-anillo" />
          <span className="hero-anillo hero-anillo-alt" />
          <span className="hero-visual-nucleo"><BookOpen /></span>
          <span className="hero-etiqueta hero-etiqueta-1"><CircleDot size={10} /> Ideas</span>
          <span className="hero-etiqueta hero-etiqueta-2"><CircleDot size={10} /> Tiempo</span>
          <span className="hero-etiqueta hero-etiqueta-3"><CircleDot size={10} /> Saberes</span>
        </div>
      </section>

      {/* Cómo funciona */}
      <section className="seccion-publica" id="como-funciona" aria-labelledby="como-funciona-titulo">
        <p className="eyebrow">Una idea sencilla, un gran cambio</p>
        <div className="seccion-cabecera">
          <h2 id="como-funciona-titulo">
            Compartes lo que sabes.<br />
            <em>Aprendes lo que sueñas.</em>
          </h2>
          <p>
            En Pasaporte de Saberes el conocimiento se mueve de persona a persona.
            Ofrece una habilidad, encuentra algo nuevo que aprender y construye
            comunidad en el camino.
          </p>
        </div>
      </section>

      {/* Áreas */}
      <section className="seccion-publica" id="areas" aria-labelledby="areas-titulo">
        <div className="seccion-cabecera">
          <div>
            <p className="eyebrow">Todos tenemos algo que aportar</p>
            <h2 id="areas-titulo">Saberes para cada camino</h2>
          </div>
          <p>Una muestra de todo lo que podemos aprender en comunidad.</p>
        </div>
        <div className="areas-grid" role="list">
          {AREAS.map((area) => (
            <article className="area-card" key={area.num} role="listitem">
              <span className="area-numero">{area.num}</span>
              <area.Icono className="area-card-icono" size={18} aria-hidden="true" />
              <h3>{area.nombre}</h3>
              <p>{area.detalle}</p>
              <ArrowUpRight className="area-card-flecha" size={16} aria-hidden="true" />
            </article>
          ))}
        </div>
      </section>

      {/* Cuenta */}
      <AccountPanel />

      {/* CTA cierre */}
      <section className="cierre" id="empezar">
        <p className="eyebrow">El próximo paso empieza contigo</p>
        <h2>Lo que sabes puede<br />inspirar a alguien.</h2>
        <a className="btn btn-blanco btn-lg" href="#cuenta">
          Crea tu cuenta <ArrowRight size={16} aria-hidden="true" />
        </a>
      </section>

      {/* Footer */}
      <footer className="footer">
        <a className="marca" href="#" style={{ fontSize: '0.8125rem' }}>
          <span className="marca-sello" style={{ height: '32px', width: '32px', fontSize: '0.6875rem' }} aria-hidden="true">PS</span>
          <span>Pasaporte<span className="marca-suave"> de Saberes</span></span>
        </a>
        <p>El conocimiento crece cuando se comparte.</p>
        <span className="footer-tag">Aprender · Compartir · Crecer</span>
      </footer>
    </main>
  )
}

export default App
