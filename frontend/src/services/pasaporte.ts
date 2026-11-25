import { apiRequest, ApiError } from './api'
import { getAccessToken } from './session'

export interface Aval {
  id: number
  entidadId: number
  credencialId: number
  fecha: string
  vigenciaHasta: string
  estado: string
}

export interface Credencial {
  id: number
  habilidad: string
  horasEnsenadas: number
  rating: number
  nivel: string
  aval: Aval | null
}

export interface Pasaporte {
  personaId: number
  nombre: string
  credenciales: Credencial[]
}

export interface Rangos {
  ensenanza: Rango | null
  aprendizaje: Rango | null
}

export interface Rango {
  id: number
  escala: string
  nombre: string
  orden: number
  minimoHoras: number
  minimoRating: number
  minimoAprendices: number
  minimoHabilidades: number
  minimoAsistencia: number
}

export function obtenerPasaporte(personaId: number): Promise<Pasaporte> {
  return apiRequest<Pasaporte>(`/pasaportes/personas/${personaId}`)
}

export function recalcularRangos(): Promise<Rangos> {
  return apiRequest<Rangos>('/pasaportes/personas/me/rangos/recalcular', { method: 'POST' })
}

export async function descargarCertificado(credencialId: number): Promise<void> {
  const token = getAccessToken()
  const response = await fetch(`/api/v1/pasaportes/credenciales/${credencialId}/certificado.pdf`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })
  if (!response.ok) {
    throw new ApiError(response.status, 'No se pudo generar el certificado.')
  }
  const blob = await response.blob()
  const url = URL.createObjectURL(blob)
  const enlace = document.createElement('a')
  enlace.href = url
  enlace.download = `certificado-${credencialId}.pdf`
  enlace.click()
  URL.revokeObjectURL(url)
}
