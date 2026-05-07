import type { IPlatformService } from '@/api/interfaces/IPlatformService'
import type { Platform, CreatePlatformDto } from '@/types/entities'
import { ApiError } from '@/types/errors'
import { delay } from '@/utils/delay'
import { mockPlatforms } from './data'

export class MockPlatformService implements IPlatformService {
  async getAll(): Promise<Platform[]> {
    await delay(200)
    return [...mockPlatforms]
  }

  async getById(id: string): Promise<Platform> {
    await delay(150)
    const p = mockPlatforms.find((p) => p.id === id)
    if (!p) throw new ApiError(404, 'Платформа не найдена')
    return p
  }

  async create(dto: CreatePlatformDto): Promise<Platform> {
    await delay(300)
    const p: Platform = { id: String(Date.now()), ...dto }
    mockPlatforms.push(p)
    return p
  }

  async update(id: string, dto: Partial<CreatePlatformDto>): Promise<Platform> {
    await delay(250)
    const idx = mockPlatforms.findIndex((p) => p.id === id)
    if (idx === -1) throw new ApiError(404, 'Платформа не найдена')
    mockPlatforms[idx] = { ...mockPlatforms[idx], ...dto }
    return mockPlatforms[idx]
  }

  async delete(id: string): Promise<void> {
    await delay(200)
    const idx = mockPlatforms.findIndex((p) => p.id === id)
    if (idx === -1) throw new ApiError(404, 'Платформа не найдена')
    mockPlatforms.splice(idx, 1)
  }
}
