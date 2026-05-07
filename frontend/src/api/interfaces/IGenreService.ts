import type { Genre, CreateGenreDto } from '@/types/entities'

export interface IGenreService {
  getAll(): Promise<Genre[]>
  getById(id: string): Promise<Genre>
  create(dto: CreateGenreDto): Promise<Genre>
  update(id: string, dto: Partial<CreateGenreDto>): Promise<Genre>
  delete(id: string): Promise<void>
}
