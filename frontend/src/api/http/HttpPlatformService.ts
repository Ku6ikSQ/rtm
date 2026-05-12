import type { IPlatformService } from '@/api/interfaces/IPlatformService'
import type { Platform, CreatePlatformDto } from '@/types/entities'
import { del, get, patch, post, uploadFile } from './apiClient'

interface BackendPlatformResponse {
  id: string
  name: string
  logoUrl: string | null
}

interface BackendPage<T> {
  content: T[]
  totalPages: number
  page: number
}

function mapPlatform(raw: BackendPlatformResponse): Platform {
  return {
    id: raw.id,
    name: raw.name,
    logoUrl: raw.logoUrl ?? undefined,
  }
}

export class HttpPlatformService implements IPlatformService {
  async getAll(): Promise<Platform[]> {
    const first = await get<BackendPage<BackendPlatformResponse>>('/api/v1/platforms?page=0&size=100&sort=name')
    const platforms = first.content.map(mapPlatform)

    if (first.totalPages > 1) {
      const remaining = await Promise.all(
        Array.from({ length: first.totalPages - 1 }, (_, i) =>
          get<BackendPage<BackendPlatformResponse>>(`/api/v1/platforms?page=${i + 1}&size=100&sort=name`)
        )
      )
      for (const page of remaining) platforms.push(...page.content.map(mapPlatform))
    }

    return platforms
  }

  async getById(id: string): Promise<Platform> {
    const raw = await get<BackendPlatformResponse>(`/api/v1/platforms/${id}`)
    return mapPlatform(raw)
  }

  async create(dto: CreatePlatformDto): Promise<Platform> {
    const raw = await post<BackendPlatformResponse>('/api/v1/platforms', { name: dto.name })
    // Upload logo separately if provided
    if (dto.logoUrl) {
      // logoUrl here is used as a URL string from forms, not a File.
      // Actual file upload goes through uploadLogo (not in interface).
    }
    return mapPlatform(raw)
  }

  async update(id: string, dto: Partial<CreatePlatformDto>): Promise<Platform> {
    if (dto.name !== undefined) {
      await patch(`/api/v1/platforms/${id}/name`, { name: dto.name })
    }
    return this.getById(id)
  }

  delete(id: string): Promise<void> {
    return del(`/api/v1/platforms/${id}`)
  }

  async uploadLogo(id: string, file: File): Promise<string> {
    await uploadFile(`/api/v1/platforms/${id}/logo`, file)
    const platform = await get<BackendPlatformResponse>(`/api/v1/platforms/${id}`)
    return platform.logoUrl ?? ''
  }
}