import type { AlbumLink, CreateLinkInput } from '@/types/entities'

export interface IAlbumLinkService {
  getByAlbum(albumId: string): Promise<AlbumLink[]>
  create(albumId: string, dto: CreateLinkInput): Promise<AlbumLink>
  delete(albumId: string, platformId: string): Promise<void>
}