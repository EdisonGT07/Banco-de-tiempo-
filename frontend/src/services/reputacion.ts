import { apiRequest } from './api'

export interface Resena {
  id: number
  sesionId: number
  autorId: number
  autorRol: 'TUTOR' | 'APRENDIZ'
  calificacion: number
  comentario: string
  creadaEn: string
}

export interface Reputacion {
  promedioPonderado: number
  aprendicesDistintos: number
  cantidadResenas: number
}

export function calificarSesion(
  sesionId: number,
  calificacion: number,
  comentario: string,
): Promise<Resena> {
  return apiRequest<Resena>(`/sesiones/${sesionId}/resenas`, {
    method: 'POST',
    body: JSON.stringify({ calificacion, comentario }),
  })
}

export function consultarReputacion(
  tutorId: number,
  habilidadId: number,
): Promise<Reputacion> {
  return apiRequest<Reputacion>(
    `/personas/${tutorId}/habilidades/${habilidadId}/reputacion`,
  )
}
