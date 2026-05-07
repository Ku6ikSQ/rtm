import type { User, LoginDto, RegisterDto, AuthTokens } from '@/types/entities'

export interface IAuthService {
  login(dto: LoginDto): Promise<AuthTokens>
  register(dto: RegisterDto): Promise<AuthTokens>
  logout(refreshToken: string): Promise<void>
  refreshToken(refreshToken: string): Promise<AuthTokens>
  getMe(): Promise<User>
}
