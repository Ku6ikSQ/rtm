import type { IUserService } from '@/api/interfaces/IUserService'
import type { User, UserRole, UpdateProfileDto } from '@/types/entities'
import { get, put, patch } from './apiClient'

export class HttpUserService implements IUserService {
  getAll(): Promise<User[]> { return get('/api/v1/users') }
  getById(id: string): Promise<User> { return get(`/api/v1/users/${id}`) }
  updateProfile(id: string, dto: UpdateProfileDto): Promise<User> { return put(`/api/v1/users/${id}`, dto) }
  blockUser(id: string): Promise<User> { return patch(`/api/v1/users/${id}/block`) }
  changeRole(id: string, role: UserRole): Promise<User> { return patch(`/api/v1/users/${id}/role`, { role }) }
}
