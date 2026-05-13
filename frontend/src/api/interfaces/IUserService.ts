import type { User, UserRole, UpdateProfileDto } from '@/types/entities'

export interface IUserService {
  getAll(): Promise<User[]>
  getById(id: string): Promise<User>
  updateProfile(dto: UpdateProfileDto): Promise<User>
  updatePassword(currentPassword: string, newPassword: string): Promise<void>
  blockUser(id: string): Promise<User>
  unblockUser(id: string): Promise<User>
  changeRole(id: string, role: UserRole): Promise<User>
  uploadAvatar(file: File): Promise<string>
}
