import type { IGenreService } from '@/api/interfaces/IGenreService'
import type { Genre, CreateGenreDto } from '@/types/entities'
import { ApiError } from '@/types/errors'
import { delay } from '@/utils/delay'
import { mockGenres } from './data'

export class MockGenreService implements IGenreService {
  async getAll(): Promise<Genre[]> {
    await delay(200)
    return [...mockGenres]
  }

  async getById(id: string): Promise<Genre> {
    await delay(150)
    const genre = mockGenres.find((g) => g.id === id)
    if (!genre) throw new ApiError(404, 'Жанр не найден')
    return genre
  }

  async create(dto: CreateGenreDto): Promise<Genre> {
    await delay(300)
    if (mockGenres.find((g) => g.slug === dto.slug)) {
      throw new ApiError(409, 'Slug already taken', { slug: 'Этот slug уже используется' })
    }
    const genre: Genre = { id: String(Date.now()), ...dto, albumCount: 0 }
    mockGenres.push(genre)
    return genre
  }

  async update(id: string, dto: Partial<CreateGenreDto>): Promise<Genre> {
    await delay(250)
    const idx = mockGenres.findIndex((g) => g.id === id)
    if (idx === -1) throw new ApiError(404, 'Жанр не найден')
    mockGenres[idx] = { ...mockGenres[idx], ...dto }
    return mockGenres[idx]
  }

  async delete(id: string): Promise<void> {
    await delay(200)
    const idx = mockGenres.findIndex((g) => g.id === id)
    if (idx === -1) throw new ApiError(404, 'Жанр не найден')
    mockGenres.splice(idx, 1)
  }
}
