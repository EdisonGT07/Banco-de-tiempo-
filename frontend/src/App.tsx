import './App.css'
import { ArrowRight, ArrowUpRight, BookOpen, CircleDot } from 'lucide-react'
import { AuthProvider } from './auth/AuthContext'
import { useAuth } from './auth/useAuth'
import { AccountPanel } from './features/auth/AccountPanel'
import { LearningDashboard } from './features/learning/LearningDashboard'

const areas = [
  { number: '01', name: 'Tecnología', detail: 'Herramientas digitales y programación' },
  { number: '02', name: 'Oficios', detail: 'Conocimientos prácticos para compartir' },
  { number: '03', name: 'Bienestar', detail: 'Hábitos, movimiento y equilibrio' },
]

function App() {
  return (
    <AuthProvider>
      <PaginaPrincipal />
    </AuthProvider>
  )
}

function PaginaPrincipal() {
  const { perfil, cargandoPerfil } = useAuth()

  if (cargandoPerfil) {
    return <main className="app-initializing" role="status">Cargando tu espacio...</main>
  }

  return perfil ? <LearningDashboard /> : <PaginaPublica />
}

function PaginaPublica() {
  return (
    <main className="public-page">
      <header className="topbar">
        <a className="brand" href="#" aria-label="Pasaporte de Saberes, inicio">
          <span className="brand-mark">PS</span>
          <span>Pasaporte<span className="brand-light"> de Saberes</span></span>
        </a>
        <nav aria-label="Navegación principal">
          <a href="#como-funciona">Cómo funciona</a>
          <a href="#areas">Áreas de saber</a>
          <a href="#cuenta">Mi cuenta</a>
        </nav>
        <a className="button button-outline" href="#cuenta">Ingresar</a>
      </header>

      <section className="public-hero">
        <div className="public-hero-copy">
          <p className="eyebrow"><span className="eyebrow-dot" /> APRENDER TAMBIÉN ES COMPARTIR</p>
          <h1>Tu tiempo tiene mucho <em>que enseñar.</em></h1>
          <p className="public-hero-description">
            Una comunidad donde intercambiamos conocimientos, crecemos juntos y cada hora compartida cuenta.
          </p>
          <div className="public-hero-actions">
            <a className="button button-primary" href="#cuenta">
              Únete a la comunidad <ArrowRight size={16} />
            </a>
            <a className="button button-outline" href="#areas">
              Explorar saberes
            </a>
          </div>
          <div className="public-proof">
            <span className="proof-line" />
            <p>Un saber compartido puede abrir nuevos caminos.</p>
          </div>
        </div>
        <div className="public-hero-visual" aria-hidden="true">
          <span className="visual-ring" />
          <span className="visual-ring visual-ring-alt" />
          <span className="public-visual-core"><BookOpen /></span>
          <span className="visual-label visual-label-one"><CircleDot size={11} /> Ideas</span>
          <span className="visual-label visual-label-two"><CircleDot size={11} /> Tiempo</span>
          <span className="visual-label visual-label-three"><CircleDot size={11} /> Saberes</span>
        </div>
      </section>

      <section className="public-section" id="como-funciona">
        <p className="eyebrow">UNA IDEA SENCILLA, UN GRAN CAMBIO</p>
        <div className="public-section-heading">
          <h2>Compartes lo que sabes.<br /><em>Aprendes lo que sueñas.</em></h2>
          <p>
            En Pasaporte de Saberes, el conocimiento se mueve de persona a persona.
            Ofrece una habilidad, encuentra algo nuevo que aprender y construye comunidad en el camino.
          </p>
        </div>
      </section>

      <section className="public-section" id="areas">
        <div className="public-section-heading">
          <div>
            <p className="eyebrow">TODOS TENEMOS ALGO QUE APORTAR</p>
            <h2>Saberes para cada camino</h2>
          </div>
          <p>Una pequeña muestra de todo lo que podemos aprender en comunidad.</p>
        </div>
        <div className="area-grid">
          {areas.map((area) => (
            <article className="area-card" key={area.number}>
              <span className="area-number">{area.number}</span>
              <h3>{area.name}</h3>
              <p>{area.detail}</p>
              <ArrowUpRight aria-hidden="true" />
            </article>
          ))}
        </div>
      </section>

      <AccountPanel />

      <section className="closing" id="empezar">
        <p className="eyebrow">EL PRÓXIMO PASO EMPIEZA CONTIGO</p>
        <h2>Lo que sabes puede<br />inspirar a alguien.</h2>
        <a className="button button-light" href="#cuenta">
          Crea tu cuenta <ArrowRight size={16} />
        </a>
      </section>

      <footer>
        <a className="brand brand-footer" href="#">
          <span className="brand-mark">PS</span>
          <span>Pasaporte<span className="brand-light"> de Saberes</span></span>
        </a>
        <p>El conocimiento crece cuando se comparte.</p>
        <span className="footer-tag">APRENDER · COMPARTIR · CRECER</span>
      </footer>
    </main>
  )
}

export default App
