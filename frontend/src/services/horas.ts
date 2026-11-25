import { apiRequest } from './api'

export interface Billetera {
  id: number
  tipo: 'EMISOR' | 'USUARIO' | 'FONDO'
  saldoDisponible: number
  saldoReservado: number
  version: number
}

export interface Transaccion {
  id: number
  tipo: string
  horas: number
  billeteraOrigenId: number
  billeteraDestinoId: number
  sesionId: number | null
  creadaEn: string
}

export function consultarBilletera(): Promise<Billetera> {
  return apiRequest<Billetera>('/horas/billetera')
}

export function retirarDelFondo(horas: number): Promise<Transaccion> {
  const clave = crypto.randomUUID()
  return apiRequest<Transaccion>('/horas/retiros-fondo', {
    method: 'POST',
    headers: { 'Idempotency-Key': clave },
    body: JSON.stringify({ horas }),
  })
}
