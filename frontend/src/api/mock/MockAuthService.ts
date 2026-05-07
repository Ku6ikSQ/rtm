import type { IAuthService } from '@/api/interfaces/IAuthService'
import type { User, LoginDto, RegisterDto, AuthTokens } from '@/types/entities'
import { ApiError } from '@/types/errors'
import { delay } from '@/utils/delay'
import { mockUsers } from './data'

const SESSION_KEY = 'rtm-mock-uid'

function getSessionUserId(): string | null {
  return sessionStorage.getItem(SESSION_KEY)
}
function setSessionUserId(id: string | null) {
  if (id) sessionStorage.setItem(SESSION_KEY, id)
  else sessionStorage.removeItem(SESSION_KEY)
}

function makeTokens(user: User): AuthTokens {
  return {
    accessToken: `mock-access-${user.id}`,
    refreshToken: `mock-refresh-${user.id}`,
  }
}

export class MockAuthService implements IAuthService {
  async login(dto: LoginDto): Promise<AuthTokens> {
    await delay(350)
    const user = mockUsers.find((u) => u.email === dto.email)
    if (!user) throw new ApiError(404, 'Пользователь не найден')
    if (!user.isActive) throw new ApiError(403, 'Аккаунт заблокирован')
    setSessionUserId(user.id)
    return makeTokens(user)
  }

  async register(dto: RegisterDto): Promise<AuthTokens> {
    await delay(400)
    if (mockUsers.find((u) => u.email === dto.email)) {
      throw new ApiError(409, 'Email already taken', { email: 'Этот email уже занят' })
    }
    if (mockUsers.find((u) => u.username === dto.username)) {
      throw new ApiError(409, 'Username already taken', { username: 'Username уже занят' })
    }
    const newUser: User = {
      id: String(Date.now()),
      username: dto.username,
      email: dto.email,
      role: 'USER',
      createdAt: new Date().toISOString(),
      isActive: true,
    }
    mockUsers.push(newUser)
    setSessionUserId(newUser.id)
    return makeTokens(newUser)
  }

  async logout(_refreshToken: string): Promise<void> {
    await delay(100)
    setSessionUserId(null)
  }

  async refreshToken(_refreshToken: string): Promise<AuthTokens> {
    await delay(200)
    const id = getSessionUserId()
    if (!id) throw new ApiError(401, 'Invalid token')
    const user = mockUsers.find((u) => u.id === id)
    if (!user) throw new ApiError(401, 'Invalid token')
    return makeTokens(user)
  }

  async getMe(): Promise<User> {
    await delay(200)
    const id = getSessionUserId()
    if (!id) throw new ApiError(401, 'Not authenticated')
    const user = mockUsers.find((u) => u.id === id)
    if (!user) throw new ApiError(401, 'Not authenticated')
    return user
  }
}
