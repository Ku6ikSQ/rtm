import type { IAuthService } from '@/api/interfaces/IAuthService'
import type { User, LoginDto, RegisterDto, AuthTokens } from '@/types/entities'
import { get, post, setAccessToken } from './apiClient'

export class HttpAuthService implements IAuthService {
  async login(dto: LoginDto): Promise<AuthTokens> {
    const tokens = await post<AuthTokens>('/api/v1/auth/login', dto)
    setAccessToken(tokens.accessToken)
    localStorage.setItem('rtm-refresh', tokens.refreshToken)
    return tokens
  }

  async register(dto: RegisterDto): Promise<AuthTokens> {
    const tokens = await post<AuthTokens>('/api/v1/auth/register', dto)
    setAccessToken(tokens.accessToken)
    localStorage.setItem('rtm-refresh', tokens.refreshToken)
    return tokens
  }

  async logout(refreshToken: string): Promise<void> {
    await post('/api/v1/auth/logout', { refreshToken })
    setAccessToken(null)
    localStorage.removeItem('rtm-refresh')
  }

  async refreshToken(refreshToken: string): Promise<AuthTokens> {
    const tokens = await post<AuthTokens>('/api/v1/auth/refresh', { refreshToken })
    setAccessToken(tokens.accessToken)
    localStorage.setItem('rtm-refresh', tokens.refreshToken)
    return tokens
  }

  async getMe(): Promise<User> {
    return get<User>('/api/v1/users/me')
  }
}
