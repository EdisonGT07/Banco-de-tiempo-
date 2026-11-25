import { apiRequest } from './api'

export interface Sesion {
  id: number
  ofertaId: number
  tutorId: number
  aprendizId: number | null
  iniciaEn: string
  lugarOEnlace: string
  estado: string
  finConfirmacionTutor: string | null
  finConfirmacionAprendiz: string | null
  inasistente: string | null
  version: number
  creadaEn: string
}

export interface Handshake {
  evidenciaId: number
  expiraEn: string
  token: string
  codigoCorto: string
}

export interface Reporte {
  id: number
  sesionId: number
  autorId: number
  revisorId: number | null
  motivo: string
  estado: string
  creadoEn: string
}

export function inscribirseSesion(sesionId: number): Promise<Sesion> {
  return apiRequest<Sesion>(`/sesiones/${sesionId}/inscripcion`, { method: 'POST' })
}

export function cancelarInscripcion(sesionId: number): Promise<Sesion> {
  return apiRequest<Sesion>(`/sesiones/${sesionId}/inscripcion`, { method: 'DELETE' })
}

export function iniciarHandshake(sesionId: number): Promise<Handshake> {
  return apiRequest<Handshake>(`/sesiones/${sesionId}/handshake`, { method: 'POST' })
}

export function confirmarFin(sesionId: number): Promise<Sesion> {
  return apiRequest<Sesion>(`/sesiones/${sesionId}/confirmacion-fin`, { method: 'POST' })
}

export function reportarProblema(sesionId: number, motivo: string): Promise<Reporte> {
  return apiRequest<Reporte>(`/sesiones/${sesionId}/reportes`, {
    method: 'POST',
    body: JSON.stringify({ motivo }),
  })
}

export interface ConfirmarHandshakeInput {
  token: string
  codigoCorto: string
  latitud?: number
  longitud?: number
  consienteEvidencia: boolean
  foto?: File
}

export function confirmarHandshake(
  sesionId: number,
  datos: ConfirmarHandshakeInput,
): Promise<Sesion> {
  const solicitud = {
    token: datos.token,
    codigoCorto: datos.codigoCorto,
    latitud: datos.latitud ?? null,
    longitud: datos.longitud ?? null,
    consienteEvidencia: datos.consienteEvidencia,
  }
  const form = new FormData()
  form.append(
    'solicitud',
    new Blob([JSON.stringify(solicitud)], { type: 'application/json' }),
  )
  if (datos.foto) form.append('foto', datos.foto)

  return apiRequest<Sesion>(`/sesiones/${sesionId}/confirmacion-handshake`, {
    method: 'POST',
    body: form,
  })
}
