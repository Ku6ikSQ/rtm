import type { IArtistService } from '@/api/interfaces/IArtistService'
import type { Artist, CreateArtistDto } from '@/types/entities'
import { del, get, patch, post, uploadFile } from './apiClient'

interface BackendArtistResponse {
  id: string
  stageName: string
  realName: string | null
  bio: string | null
  country: string | null
  imageUrl: string | null
  createdAt: string
}

interface BackendPage<T> {
  content: T[]
  totalPages: number
  page: number
}

function mapArtist(raw: BackendArtistResponse): Artist {
  return {
    id: raw.id,
    stageName: raw.stageName,
    realName: raw.realName ?? undefined,
    bio: raw.bio ?? undefined,
    country: raw.country ?? undefined,
    imageUrl: raw.imageUrl ?? undefined,
  }
}

export class HttpArtistService implements IArtistService {
  async getAll(): Promise<Artist[]> {
    const first = await get<BackendPage<BackendArtistResponse>>(
      '/api/v1/artists?page=0&size=100&sort=stageName'
    )
    const artists = first.content.map(mapArtist)

    if (first.totalPages > 1) {
      const remaining = await Promise.all(
        Array.from({ length: first.totalPages - 1 }, (_, i) =>
          get<BackendPage<BackendArtistResponse>>(
            `/api/v1/artists?page=${i + 1}&size=100&sort=stageName`
          )
        )
      )
      for (const page of remaining) artists.push(...page.content.map(mapArtist))
    }

    return artists
  }

  async getById(id: string): Promise<Artist> {
    const raw = await get<BackendArtistResponse>(`/api/v1/artists/${id}`)
    return mapArtist(raw)
  }

  async create(dto: CreateArtistDto): Promise<Artist> {
    const raw = await post<BackendArtistResponse>('/api/v1/artists', dto)
    return mapArtist(raw)
  }

  async update(id: string, dto: Partial<CreateArtistDto>): Promise<Artist> {
    const patches: Promise<unknown>[] = []
    if (dto.stageName !== undefined) patches.push(patch(`/api/v1/artists/${id}/stage-name`, { stageName: dto.stageName }))
    if (dto.realName !== undefined)  patches.push(patch(`/api/v1/artists/${id}/real-name`,  { realName: dto.realName }))
    if (dto.bio !== undefined)       patches.push(patch(`/api/v1/artists/${id}/bio`,        { bio: dto.bio }))
    if (dto.country !== undefined)   patches.push(patch(`/api/v1/artists/${id}/country`,    { country: dto.country }))
    await Promise.all(patches)
    return this.getById(id)
  }

  delete(id: string): Promise<void> {
    return del(`/api/v1/artists/${id}`)
  }

  async uploadPhoto(id: string, file: File): Promise<string> {
    await uploadFile(`/api/v1/artists/${id}/photo`, file)
    const raw = await get<BackendArtistResponse>(`/api/v1/artists/${id}`)
    return raw.imageUrl ?? ''
  }
}