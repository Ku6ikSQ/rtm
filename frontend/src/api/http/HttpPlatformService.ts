import type { IPlatformService } from '@/api/interfaces/IPlatformService'
import type { Platform, CreatePlatformDto } from '@/types/entities'
import { get, post, put, del } from './apiClient'

export class HttpPlatformService implements IPlatformService {
  getAll(): Promise<Platform[]> { return get('/api/v1/platforms') }
  getById(id: string): Promise<Platform> { return get(`/api/v1/platforms/${id}`) }
  create(dto: CreatePlatformDto): Promise<Platform> { return post('/api/v1/platforms', dto) }
  update(id: string, dto: Partial<CreatePlatformDto>): Promise<Platform> { return put(`/api/v1/platforms/${id}`, dto) }
  delete(id: string): Promise<void> { return del(`/api/v1/platforms/${id}`) }
}
