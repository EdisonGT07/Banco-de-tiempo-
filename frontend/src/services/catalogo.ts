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

export interface NuevaOferta {
  habilidadId: number
  descripcion: string
  duracionHoras: number
  modalidad: Modalidad
  barrio?: string
  destinoHoras: 'BILLETERA_PROPIA' | 'FONDO' | 'REGALO'
  receptorRegaloId?: number
}

export interface OfertaPublicada {
  id: number
  tutorId: number
  nombreTutor: string
  habilidadId: number
  habilidad: string
  descripcion: string
  duracionHoras: number
  modalidad: Modalidad
  barrio: string | null
  destinoHoras: string
  receptorRegaloId: number | null
  activa: boolean
  creadaEn: string
}

export interface Habilidad {
  id: number
  nombre: string
  categoria: string
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

export function publicarOferta(datos: NuevaOferta): Promise<OfertaPublicada> {
  return apiRequest<OfertaPublicada>('/catalogo/ofertas', {
    method: 'POST',
    body: JSON.stringify(datos),
  })
}

export function listarHabilidades(): Promise<Habilidad[]> {
  return apiRequest<Habilidad[]>('/catalogo/habilidades')
}

export function crearHabilidad(
  nombre: string,
  categoria: string,
): Promise<Habilidad> {
  return apiRequest<Habilidad>('/catalogo/habilidades', {
    method: 'POST',
    body: JSON.stringify({ nombre, categoria }),
  })
}
