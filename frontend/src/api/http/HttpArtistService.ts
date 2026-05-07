import type { IArtistService } from '@/api/interfaces/IArtistService'
import type { Artist, CreateArtistDto } from '@/types/entities'
import { get, post, put, del, request } from './apiClient'

export class HttpArtistService implements IArtistService {
  getAll(): Promise<Artist[]> { return get('/api/v1/artists') }
  getById(id: string): Promise<Artist> { return get(`/api/v1/artists/${id}`) }
  create(dto: CreateArtistDto): Promise<Artist> { return post('/api/v1/artists', dto) }
  update(id: string, dto: Partial<CreateArtistDto>): Promise<Artist> { return put(`/api/v1/artists/${id}`, dto) }
  delete(id: string): Promise<void> { return del(`/api/v1/artists/${id}`) }
  async uploadPhoto(id: string, file: File): Promise<string> {
    const form = new FormData()
    form.append('file', file)
    const res = await request<{ url: string }>(`/api/v1/artists/${id}/photo`, { method: 'POST', body: form, headers: {} })
    return res.url
  }
}
