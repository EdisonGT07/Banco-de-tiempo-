import { apiRequest } from './api'

export interface PanelImpacto {
  horasIntercambiadas: number
  personasFormadas: number
  habilidadesNuevas: number
  credencialesEmitidas: number
  verificaciones: number
}

export function obtenerPanelImpacto(): Promise<PanelImpacto> {
  return apiRequest<PanelImpacto>('/admin/impacto')
}

export function desbloquearPersona(personaId: number): Promise<unknown> {
  return apiRequest<unknown>(`/admin/personas/${personaId}/desbloqueo`, { method: 'POST' })
}

export function revisarReporte(reporteId: number, resolver: boolean): Promise<unknown> {
  return apiRequest<unknown>(`/admin/reportes/${reporteId}`, {
    method: 'PATCH',
    body: JSON.stringify({ resolver }),
  })
}
