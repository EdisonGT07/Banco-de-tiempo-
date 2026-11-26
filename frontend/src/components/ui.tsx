import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react'
import type { ReactNode } from 'react'
import {
  BookOpen,
  CheckCircle2,
  GraduationCap,
  Hammer,
  HeartPulse,
  Languages,
  Palette,
  TriangleAlert,
  Code2,
} from 'lucide-react'

/* ------------------------------------------------------------------
   Mapa de iconos por categoría de saber
   ------------------------------------------------------------------ */
const ICONOS_CATEGORIA: Record<string, typeof Code2> = {
  'Tecnología': Code2,
  'Oficios': Hammer,
  'Bienestar': HeartPulse,
  'Idiomas': Languages,
  'Arte y cultura': Palette,
  'Educación': GraduationCap,
}

/** Icono asociado a una categoría; BookOpen como respaldo. */
export function IconoCategoria({ categoria }: { categoria: string }) {
  const Icono = ICONOS_CATEGORIA[categoria] ?? BookOpen
  return <Icono aria-hidden="true" />
}

/* ------------------------------------------------------------------
   Toast — notificación efímera en esquina inferior
   ------------------------------------------------------------------ */
interface Toast {
  id: number
  tipo: 'exito' | 'error'
  mensaje: string
}

const ToastContexto = createContext<(tipo: 'exito' | 'error', mensaje: string) => void>(
  () => undefined,
)

export function ProveedorToast({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([])
  const contador = useRef(0)

  const mostrar = useCallback((tipo: 'exito' | 'error', mensaje: string) => {
    const id = ++contador.current
    setToasts((actuales) => [...actuales, { id, tipo, mensaje }])
    window.setTimeout(() => {
      setToasts((actuales) => actuales.filter((toast) => toast.id !== id))
    }, 4200)
  }, [])

  const valor = useMemo(() => mostrar, [mostrar])

  return (
    <ToastContexto.Provider value={valor}>
      {children}
      <div className="toast-zona" role="status" aria-live="polite">
        {toasts.map((toast) => (
          <div key={toast.id} className={`toast toast-${toast.tipo}`}>
            {toast.tipo === 'exito'
              ? <CheckCircle2 size={18} aria-hidden="true" />
              : <TriangleAlert size={18} aria-hidden="true" />}
            <span>{toast.mensaje}</span>
          </div>
        ))}
      </div>
    </ToastContexto.Provider>
  )
}

export function useToast() {
  return useContext(ToastContexto)
}

/* ------------------------------------------------------------------
   EmptyState — estado vacío con número tipográfico
   ------------------------------------------------------------------ */
export function EstadoVacio({
  numero,
  titulo,
  descripcion,
  accion,
}: {
  numero: string
  titulo: string
  descripcion: string
  accion?: ReactNode
}) {
  return (
    <div className="estado-vacio">
      <span className="estado-vacio-numero" aria-hidden="true">{numero}</span>
      <div>
        <h3>{titulo}</h3>
        <p>{descripcion}</p>
      </div>
      {accion}
    </div>
  )
}

/* ------------------------------------------------------------------
   Skeleton de tarjeta de oferta
   ------------------------------------------------------------------ */
export function OfertaSkeleton() {
  return (
    <div className="oferta-skeleton" aria-hidden="true">
      <div className="skeleton oferta-skeleton-icono" />
      <div className="skeleton oferta-skeleton-h" />
      <div className="skeleton oferta-skeleton-p" />
      <div className="skeleton oferta-skeleton-p" />
    </div>
  )
}

/* ------------------------------------------------------------------
   Errores / éxitos inline
   ------------------------------------------------------------------ */
export function Alerta({ tipo, children }: { tipo: 'error' | 'exito' | 'info'; children: ReactNode }) {
  return (
    <p className={`alerta alerta-${tipo}`} role={tipo === 'error' ? 'alert' : 'status'}>
      {children}
    </p>
  )
}

/* ------------------------------------------------------------------
   Cerrar sesión al vencer el token (reutilizable)
   ------------------------------------------------------------------ */
export function useSesionVencida(alVencer: () => void) {
  useEffect(() => {
    function manejar() {
      alVencer()
    }
    window.addEventListener('pasaporte-saberes:sesion-vencida', manejar)
    return () => {
      window.removeEventListener('pasaporte-saberes:sesion-vencida', manejar)
    }
  }, [alVencer])
}
