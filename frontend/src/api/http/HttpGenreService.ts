import type { IGenreService } from '@/api/interfaces/IGenreService'
import type { Genre, CreateGenreDto } from '@/types/entities'
import { get, post, put, del } from './apiClient'

export class HttpGenreService implements IGenreService {
  getAll(): Promise<Genre[]> { return get('/api/v1/genres') }
  getById(id: string): Promise<Genre> { return get(`/api/v1/genres/${id}`) }
  create(dto: CreateGenreDto): Promise<Genre> { return post('/api/v1/genres', dto) }
  update(id: string, dto: Partial<CreateGenreDto>): Promise<Genre> { return put(`/api/v1/genres/${id}`, dto) }
  delete(id: string): Promise<void> { return del(`/api/v1/genres/${id}`) }
}
