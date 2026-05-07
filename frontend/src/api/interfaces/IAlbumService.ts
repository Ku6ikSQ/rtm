import type { Album, AlbumFilters, CreateAlbumDto, UpdateAlbumDto, PageResult } from '@/types/entities'

export interface IAlbumService {
  getAll(filters?: AlbumFilters): Promise<PageResult<Album>>
  getById(id: string): Promise<Album>
  create(dto: CreateAlbumDto): Promise<Album>
  update(id: string, dto: UpdateAlbumDto): Promise<Album>
  delete(id: string): Promise<void>
  uploadCover(id: string, file: File): Promise<string>
}
