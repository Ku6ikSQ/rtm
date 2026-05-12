import type { IAlbumLinkService } from '@/api/interfaces/IAlbumLinkService'
import type { AlbumLink, CreateLinkInput } from '@/types/entities'
import { ApiError } from '@/types/errors'
import { delay } from '@/utils/delay'
import { mockAlbums, mockPlatforms } from './data'

export class MockAlbumLinkService implements IAlbumLinkService {
  async getByAlbum(albumId: string): Promise<AlbumLink[]> {
    await delay(150)
    const album = mockAlbums.find(a => a.id === albumId)
    return album?.links ?? []
  }

  async create(albumId: string, dto: CreateLinkInput): Promise<AlbumLink> {
    await delay(200)
    const album = mockAlbums.find(a => a.id === albumId)
    if (!album) throw new ApiError(404, 'Album not found')

    const exists = album.links.some(l => l.platformId === dto.platformId)
    if (exists) throw new ApiError(409, 'Link for this platform already exists')

    const platform = mockPlatforms.find(p => p.id === dto.platformId)
    const link: AlbumLink = {
      albumId,
      platformId: dto.platformId,
      url: dto.url,
      platform,
    }
    album.links.push(link)
    return link
  }

  async delete(albumId: string, platformId: string): Promise<void> {
    await delay(150)
    const album = mockAlbums.find(a => a.id === albumId)
    if (!album) throw new ApiError(404, 'Album not found')
    const idx = album.links.findIndex(l => l.platformId === platformId)
    if (idx === -1) throw new ApiError(404, 'Link not found')
    album.links.splice(idx, 1)
  }
}