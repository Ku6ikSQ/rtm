import { ApiError } from '@/types/errors'
import type { ServerProblemDetail } from '@/types/errors'
import type { AuthTokens } from '@/types/entities'

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''

let accessToken: string | null = null
let refreshPromise: Promise<AuthTokens> | null = null

export function setAccessToken(token: string | null) {
  accessToken = token
}

export function getAccessToken(): string | null {
  return accessToken
}

async function parseError(res: Response): Promise<ApiError> {
  try {
    const body: ServerProblemDetail = await res.json()
    return ApiError.fromProblemDetail(res.status, body)
  } catch {
    return new ApiError(res.status, res.statusText || 'Request failed')
  }
}

async function doFetch(input: RequestInfo, init?: RequestInit): Promise<Response> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(init?.headers as Record<string, string>),
  }
  if (accessToken) {
    headers['Authorization'] = `Bearer ${accessToken}`
  }
  return fetch(BASE_URL + input, { ...init, headers })
}

async function tryRefresh(): Promise<void> {
  const { authService } = await import('@/api')
  const refreshToken = localStorage.getItem('rtm-refresh')
  if (!refreshToken) throw new ApiError(401, 'No refresh token')

  if (!refreshPromise) {
    refreshPromise = authService.refreshToken(refreshToken).finally(() => {
      refreshPromise = null
    })
  }

  const tokens = await refreshPromise
  setAccessToken(tokens.accessToken)
  localStorage.setItem('rtm-refresh', tokens.refreshToken)
}

export async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let res = await doFetch(path, init)

  if (res.status === 401 && accessToken) {
    try {
      await tryRefresh()
      res = await doFetch(path, init)
    } catch {
      setAccessToken(null)
      localStorage.removeItem('rtm-refresh')
      throw new ApiError(401, 'Session expired')
    }
  }

  if (!res.ok) {
    throw await parseError(res)
  }

  if (res.status === 204) return undefined as T
  return res.json()
}

export function get<T>(path: string): Promise<T> {
  return request<T>(path, { method: 'GET' })
}

export function post<T>(path: string, body?: unknown): Promise<T> {
  return request<T>(path, { method: 'POST', body: body ? JSON.stringify(body) : undefined })
}

export function put<T>(path: string, body?: unknown): Promise<T> {
  return request<T>(path, { method: 'PUT', body: body ? JSON.stringify(body) : undefined })
}

export function patch<T>(path: string, body?: unknown): Promise<T> {
  return request<T>(path, { method: 'PATCH', body: body ? JSON.stringify(body) : undefined })
}

export function del(path: string): Promise<void> {
  return request<void>(path, { method: 'DELETE' })
}

export async function uploadFile(path: string, file: File): Promise<void> {
  const form = new FormData()
  form.append('file', file)
  // Let the browser set Content-Type with boundary for multipart
  await request<void>(path, { method: 'PUT', body: form, headers: {} })
}
