import type { Artist, CreateArtistDto } from '@/types/entities'

export interface IArtistService {
  getAll(): Promise<Artist[]>
  getById(id: string): Promise<Artist>
  create(dto: CreateArtistDto): Promise<Artist>
  update(id: string, dto: Partial<CreateArtistDto>): Promise<Artist>
  delete(id: string): Promise<void>
  uploadPhoto(id: string, file: File): Promise<string>
}
