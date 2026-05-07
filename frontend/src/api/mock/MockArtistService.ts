import type { IArtistService } from '@/api/interfaces/IArtistService'
import type { Artist, CreateArtistDto } from '@/types/entities'
import { ApiError } from '@/types/errors'
import { delay } from '@/utils/delay'
import { mockArtists } from './data'

export class MockArtistService implements IArtistService {
  async getAll(): Promise<Artist[]> {
    await delay(250)
    return [...mockArtists]
  }

  async getById(id: string): Promise<Artist> {
    await delay(200)
    const artist = mockArtists.find((a) => a.id === id)
    if (!artist) throw new ApiError(404, 'Артист не найден')
    return artist
  }

  async create(dto: CreateArtistDto): Promise<Artist> {
    await delay(350)
    const artist: Artist = { id: String(Date.now()), ...dto }
    mockArtists.push(artist)
    return artist
  }

  async update(id: string, dto: Partial<CreateArtistDto>): Promise<Artist> {
    await delay(300)
    const idx = mockArtists.findIndex((a) => a.id === id)
    if (idx === -1) throw new ApiError(404, 'Артист не найден')
    mockArtists[idx] = { ...mockArtists[idx], ...dto }
    return mockArtists[idx]
  }

  async delete(id: string): Promise<void> {
    await delay(250)
    const idx = mockArtists.findIndex((a) => a.id === id)
    if (idx === -1) throw new ApiError(404, 'Артист не найден')
    mockArtists.splice(idx, 1)
  }

  async uploadPhoto(_id: string, _file: File): Promise<string> {
    await delay(500)
    return `https://placeholder.co/200x200?text=Photo`
  }
}
