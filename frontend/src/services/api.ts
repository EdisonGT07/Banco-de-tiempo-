import { getAccessToken } from './session'

const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || '/api/v1').replace(/\/+$/, '')

export interface ApiRequestInit extends RequestInit {
  authenticated?: boolean
}

export class ApiError extends Error {
  public readonly status: number

  constructor(
    status: number,
    message: string,
  ) {
    super(message || `La solicitud falló (${status})`)
    this.name = 'ApiError'
    this.status = status
  }
}

export async function apiRequest<T>(path: string, init: ApiRequestInit = {}): Promise<T> {
  const { authenticated = true, ...requestInit } = init
  const headers = new Headers(requestInit.headers)
  headers.set('Accept', 'application/json')
  if (requestInit.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  const token = authenticated ? getAccessToken() : null
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  const response = await fetch(`${apiBaseUrl}${path}`, { ...requestInit, headers })
  if (!response.ok) {
    if (response.status === 401 && token) {
      window.dispatchEvent(new Event('pasaporte-saberes:sesion-vencida'))
    }
    throw new ApiError(response.status, await readErrorMessage(response))
  }

  return response.json() as Promise<T>
}

async function readErrorMessage(response: Response): Promise<string> {
  if (!response.headers.get('content-type')?.includes('application/json')) {
    return response.text()
  }

  const body: unknown = await response.json()
  if (
    typeof body === 'object'
    && body !== null
    && 'mensaje' in body
    && typeof body.mensaje === 'string'
  ) {
    return body.mensaje
  }

  return `La solicitud falló (${response.status})`
}
