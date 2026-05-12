import type { IUserService } from '@/api/interfaces/IUserService'
import type { User, UserRole, UpdateProfileDto } from '@/types/entities'
import { del, get, patch, post, uploadFile } from './apiClient'
import { mapUser } from './HttpAuthService'

interface BackendUserResponse {
  id: string
  username: string
  email: string
  imageUrl: string | null
  role: string
  createdAt: string
  isActive: boolean
}

interface BackendPage<T> {
  content: T[]
  totalPages: number
  totalElements: number
  page: number
  size: number
}

export class HttpUserService implements IUserService {
  async getAll(): Promise<User[]> {
    const first = await get<BackendPage<BackendUserResponse>>(
      '/api/v1/users?page=0&size=100&sort=createdAt,desc'
    )
    const users = first.content.map(mapUser)

    if (first.totalPages > 1) {
      const remaining = await Promise.all(
        Array.from({ length: first.totalPages - 1 }, (_, i) =>
          get<BackendPage<BackendUserResponse>>(
            `/api/v1/users?page=${i + 1}&size=100&sort=createdAt,desc`
          )
        )
      )
      for (const page of remaining) users.push(...page.content.map(mapUser))
    }

    return users
  }

  async getById(id: string): Promise<User> {
    const raw = await get<BackendUserResponse>(`/api/v1/users/${id}`)
    return mapUser(raw)
  }

  async updateProfile(dto: UpdateProfileDto): Promise<User> {
    const patches: Promise<unknown>[] = []
    if (dto.username !== undefined) {
      patches.push(patch('/api/v1/users/me/username', { username: dto.username }))
    }
    await Promise.all(patches)

    const raw = await get<BackendUserResponse>('/api/v1/users/me')
    return mapUser(raw)
  }

  async blockUser(id: string): Promise<User> {
    await post(`/api/v1/users/${id}/block`, null)
    return this.getById(id)
  }

  async unblockUser(id: string): Promise<User> {
    await post(`/api/v1/users/${id}/unblock`, null)
    return this.getById(id)
  }

  async changeRole(id: string, role: UserRole): Promise<User> {
    await patch(`/api/v1/users/${id}/role`, { role })
    return this.getById(id)
  }

  async uploadAvatar(file: File): Promise<string> {
    await uploadFile('/api/v1/users/me/avatar', file)
    const raw = await get<BackendUserResponse>('/api/v1/users/me')
    return raw.imageUrl ?? ''
  }

  async updateEmail(email: string): Promise<void> {
    await patch('/api/v1/users/me/email', { email })
  }

  async updatePassword(currentPassword: string, newPassword: string): Promise<void> {
    await patch('/api/v1/users/me/password', { currentPassword, newPassword })
  }

  deleteMe(): Promise<void> {
    return del('/api/v1/users/me')
  }
}