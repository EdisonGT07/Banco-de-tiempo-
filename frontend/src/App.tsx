import './App.css'
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
    <main>
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

      <section className="hero">
        <div className="hero-copy">
          <p className="eyebrow"><span className="eyebrow-dot" /> APRENDER TAMBIÉN ES COMPARTIR</p>
          <h1>Tu tiempo tiene<br />mucho <span>que enseñar.</span></h1>
          <p className="hero-description">
            Una comunidad donde intercambiamos conocimientos, crecemos juntos y cada hora compartida cuenta.
          </p>
          <a className="button button-primary" href="#cuenta">
            Únete a la comunidad <span aria-hidden="true">→</span>
          </a>
          <div className="hero-note">
            <span className="note-line" />
            <p>Un saber compartido puede abrir nuevos caminos.</p>
          </div>
        </div>
        <div
          className="hero-art"
          aria-label="Ilustración abstracta de intercambio de conocimientos"
          role="img"
        >
          <div className="art-orbit orbit-one" />
          <div className="art-orbit orbit-two" />
          <div className="art-center"><span>+</span></div>
          <div className="art-token token-one">IDEAS</div>
          <div className="art-token token-two">TIEMPO</div>
          <div className="art-token token-three">SABER</div>
          <span className="art-spark spark-one">✳</span>
          <span className="art-spark spark-two">✳</span>
          <span className="art-caption">CIRCULA LO QUE SABES</span>
        </div>
      </section>

      <section className="intro" id="como-funciona">
        <p className="eyebrow">UNA IDEA SENCILLA, UN GRAN CAMBIO</p>
        <div className="intro-content">
          <h2>Compartes lo que sabes.<br /><span>Aprendes lo que sueñas.</span></h2>
          <p>
            En Pasaporte de Saberes, el conocimiento se mueve de persona a persona.
            Ofrece una habilidad, encuentra algo nuevo que aprender y construye comunidad en el camino.
          </p>
        </div>
      </section>

      <section className="areas" id="areas">
        <div className="section-heading">
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
              <span className="area-arrow" aria-hidden="true">↗</span>
            </article>
          ))}
        </div>
      </section>

      <AccountPanel />

      <section className="closing" id="empezar">
        <p className="eyebrow">EL PRÓXIMO PASO EMPIEZA CONTIGO</p>
        <h2>Lo que sabes puede<br />inspirar a alguien.</h2>
        <a className="button button-light" href="#cuenta">
          Crea tu cuenta <span aria-hidden="true">→</span>
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
