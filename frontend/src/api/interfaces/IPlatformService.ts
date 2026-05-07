import type { Platform, CreatePlatformDto } from '@/types/entities'

export interface IPlatformService {
  getAll(): Promise<Platform[]>
  getById(id: string): Promise<Platform>
  create(dto: CreatePlatformDto): Promise<Platform>
  update(id: string, dto: Partial<CreatePlatformDto>): Promise<Platform>
  delete(id: string): Promise<void>
}
