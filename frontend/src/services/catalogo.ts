import { apiRequest } from './api'

export type Modalidad = 'PRESENCIAL' | 'VIRTUAL'

export interface Oferta {
  ofertaId: number
  tutorId: number
  nombreTutor: string
  habilidad: string
  descripcion: string
  duracionHoras: number
  modalidad: Modalidad
  barrio: string | null
  calificacion: number | null
  rango: string | null
  nivelCredencial: string | null
}

export interface SugerenciaAprendizaje {
  personaId: number
  ofertaId: number
  tutorId: number
  habilidadId: number
  cruceMutuo: boolean
}

export interface InteresAprendizaje {
  personaId: number
  habilidadId: number
  habilidad: string
  creadaEn: string
}

export interface FiltrosOfertas {
  texto?: string
  habilidadId?: number
  pagina?: number
  tamanoPagina?: number
}

export function buscarOfertas(filtros: FiltrosOfertas = {}): Promise<Oferta[]> {
  const parametros = new URLSearchParams({
    pagina: String(filtros.pagina ?? 0),
    tamanoPagina: String(filtros.tamanoPagina ?? 12),
  })

  if (filtros.texto?.trim()) {
    parametros.set('texto', filtros.texto.trim())
  }
  if (filtros.habilidadId !== undefined) {
    parametros.set('habilidadId', String(filtros.habilidadId))
  }

  return apiRequest<Oferta[]>(`/agora/ofertas?${parametros.toString()}`)
}

export function obtenerSugerencias(): Promise<SugerenciaAprendizaje[]> {
  return apiRequest<SugerenciaAprendizaje[]>('/agora/sugerencias')
}

export function registrarInteresAprendizaje(
  habilidadId: number,
): Promise<InteresAprendizaje> {
  return apiRequest<InteresAprendizaje>('/catalogo/solicitudes-aprendizaje', {
    method: 'POST',
    body: JSON.stringify({ habilidadId }),
  })
}
