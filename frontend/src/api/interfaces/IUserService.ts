import type { User, UserRole, UpdateProfileDto } from '@/types/entities'

export interface IUserService {
  getAll(): Promise<User[]>
  getById(id: string): Promise<User>
  updateProfile(id: string, dto: UpdateProfileDto): Promise<User>
  blockUser(id: string): Promise<User>
  changeRole(id: string, role: UserRole): Promise<User>
}
