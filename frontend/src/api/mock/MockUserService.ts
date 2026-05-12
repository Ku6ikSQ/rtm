import type { IUserService } from '@/api/interfaces/IUserService'
import type { User, UserRole, UpdateProfileDto } from '@/types/entities'
import { ApiError } from '@/types/errors'
import { delay } from '@/utils/delay'
import { mockUsers } from './data'

export class MockUserService implements IUserService {
  async getAll(): Promise<User[]> {
    await delay(300)
    return [...mockUsers]
  }

  async getById(id: string): Promise<User> {
    await delay(200)
    const user = mockUsers.find((u) => u.id === id)
    if (!user) throw new ApiError(404, 'Пользователь не найден')
    return user
  }

  async updateProfile(dto: UpdateProfileDto): Promise<User> {
    await delay(300)
    const id = sessionStorage.getItem('rtm-mock-uid')
    if (!id) throw new ApiError(401, 'Необходимо войти в аккаунт')
    const idx = mockUsers.findIndex((u) => u.id === id)
    if (idx === -1) throw new ApiError(404, 'Пользователь не найден')
    if (dto.username && mockUsers.find((u) => u.username === dto.username && u.id !== id)) {
      throw new ApiError(409, 'Username already taken', { username: 'Username уже занят' })
    }
    if (dto.username) mockUsers[idx].username = dto.username
    return mockUsers[idx]
  }

  async blockUser(id: string): Promise<User> {
    await delay(250)
    const idx = mockUsers.findIndex((u) => u.id === id)
    if (idx === -1) throw new ApiError(404, 'Пользователь не найден')
    mockUsers[idx] = { ...mockUsers[idx], isActive: !mockUsers[idx].isActive }
    return mockUsers[idx]
  }

  async changeRole(id: string, role: UserRole): Promise<User> {
    await delay(250)
    const idx = mockUsers.findIndex((u) => u.id === id)
    if (idx === -1) throw new ApiError(404, 'Пользователь не найден')
    mockUsers[idx] = { ...mockUsers[idx], role }
    return mockUsers[idx]
  }

  async updatePassword(_password: string): Promise<void> {
    await delay(300)
    // Mock: accepts any password without verification
  }
}
