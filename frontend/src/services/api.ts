const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || '/api/v1').replace(/\/+$/, '')

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

export async function apiRequest<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers)
  headers.set('Accept', 'application/json')
  if (init.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  const response = await fetch(`${apiBaseUrl}${path}`, { ...init, headers })
  if (!response.ok) {
    throw new ApiError(response.status, await response.text())
  }

  return response.json() as Promise<T>
}
