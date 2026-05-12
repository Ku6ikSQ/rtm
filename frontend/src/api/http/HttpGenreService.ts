import type { IGenreService } from '@/api/interfaces/IGenreService'
import type { Genre, CreateGenreDto } from '@/types/entities'
import { del, get, patch, post } from './apiClient'

interface BackendGenreResponse {
  id: string
  name: string
  slug: string
  description: string | null
  parentId: string | null
  albumCount: number
}

interface BackendPage<T> {
  content: T[]
  totalPages: number
  page: number
}

function mapGenre(raw: BackendGenreResponse): Genre {
  return {
    id: raw.id,
    name: raw.name,
    slug: raw.slug,
    description: raw.description ?? undefined,
    parentId: raw.parentId ?? undefined,
    albumCount: raw.albumCount,
  }
}

export class HttpGenreService implements IGenreService {
  async getAll(): Promise<Genre[]> {
    // Fetch first page to learn totalPages, then fetch the rest in parallel
    const first = await get<BackendPage<BackendGenreResponse>>('/api/v1/genres?page=0&size=100&sort=name')
    const genres = first.content.map(mapGenre)

    if (first.totalPages > 1) {
      const remaining = await Promise.all(
        Array.from({ length: first.totalPages - 1 }, (_, i) =>
          get<BackendPage<BackendGenreResponse>>(`/api/v1/genres?page=${i + 1}&size=100&sort=name`)
        )
      )
      for (const page of remaining) genres.push(...page.content.map(mapGenre))
    }

    return genres
  }

  async getById(id: string): Promise<Genre> {
    const raw = await get<BackendGenreResponse>(`/api/v1/genres/${id}`)
    return mapGenre(raw)
  }

  async create(dto: CreateGenreDto): Promise<Genre> {
    const raw = await post<BackendGenreResponse>('/api/v1/genres', dto)
    return mapGenre(raw)
  }

  async update(id: string, dto: Partial<CreateGenreDto>): Promise<Genre> {
    const patches: Promise<unknown>[] = []

    if (dto.name !== undefined) {
      patches.push(patch(`/api/v1/genres/${id}/name`, { name: dto.name }))
    }
    if (dto.slug !== undefined) {
      patches.push(patch(`/api/v1/genres/${id}/slug`, { slug: dto.slug }))
    }
    if (dto.description !== undefined) {
      patches.push(patch(`/api/v1/genres/${id}/description`, { description: dto.description }))
    }
    if (dto.parentId !== undefined) {
      patches.push(patch(`/api/v1/genres/${id}/parent`, { parentId: dto.parentId }))
    }

    await Promise.all(patches)
    return this.getById(id)
  }

  delete(id: string): Promise<void> {
    return del(`/api/v1/genres/${id}`)
  }
}