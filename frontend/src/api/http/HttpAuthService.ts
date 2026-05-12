import type { IAuthService } from '@/api/interfaces/IAuthService'
import type { User, LoginDto, RegisterDto, AuthTokens } from '@/types/entities'
import { get, post, setAccessToken } from './apiClient'

interface BackendUserResponse {
  id: string
  username: string
  email: string
  imageUrl: string | null
  role: string
  createdAt: string
  isActive: boolean
}

export function mapUser(raw: BackendUserResponse): User {
  return {
    id: raw.id,
    username: raw.username,
    email: raw.email,
    avatarUrl: raw.imageUrl ?? undefined,
    role: raw.role as User['role'],
    createdAt: raw.createdAt,
    isActive: raw.isActive,
  }
}

export class HttpAuthService implements IAuthService {
  async login(dto: LoginDto): Promise<AuthTokens> {
    const tokens = await post<AuthTokens>('/api/v1/auth/login', dto)
    setAccessToken(tokens.accessToken)
    localStorage.setItem('rtm-refresh', tokens.refreshToken)
    return tokens
  }

  async register(dto: RegisterDto): Promise<AuthTokens> {
    const { passwordConfirm: _, ...payload } = dto
    const tokens = await post<AuthTokens>('/api/v1/auth/register', payload)
    setAccessToken(tokens.accessToken)
    localStorage.setItem('rtm-refresh', tokens.refreshToken)
    return tokens
  }

  async logout(refreshToken: string): Promise<void> {
    try {
      await post('/api/v1/auth/logout', { refreshToken })
    } finally {
      setAccessToken(null)
      localStorage.removeItem('rtm-refresh')
    }
  }

  async refreshToken(refreshToken: string): Promise<AuthTokens> {
    const tokens = await post<AuthTokens>('/api/v1/auth/refresh', { refreshToken })
    setAccessToken(tokens.accessToken)
    localStorage.setItem('rtm-refresh', tokens.refreshToken)
    return tokens
  }

  async getMe(): Promise<User> {
    const raw = await get<BackendUserResponse>('/api/v1/users/me')
    return mapUser(raw)
  }
}