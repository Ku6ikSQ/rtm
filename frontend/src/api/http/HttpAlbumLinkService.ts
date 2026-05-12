import type { IAlbumLinkService } from '@/api/interfaces/IAlbumLinkService'
import type { AlbumLink, CreateLinkInput } from '@/types/entities'
import { del, get, post } from './apiClient'

interface BackendAlbumLinkResponse {
  albumId: string
  platformId: string
  url: string
  platformName: string
  platformLogoUrl: string | null
}

function mapLink(raw: BackendAlbumLinkResponse): AlbumLink {
  return {
    albumId: raw.albumId,
    platformId: raw.platformId,
    url: raw.url,
    platform: {
      id: raw.platformId,
      name: raw.platformName,
      logoUrl: raw.platformLogoUrl ?? undefined,
    },
  }
}

export class HttpAlbumLinkService implements IAlbumLinkService {
  async getByAlbum(albumId: string): Promise<AlbumLink[]> {
    const links = await get<BackendAlbumLinkResponse[]>(
      `/api/v1/album-links/by-album/${albumId}`
    )
    return links.map(mapLink)
  }

  async create(albumId: string, dto: CreateLinkInput): Promise<AlbumLink> {
    const raw = await post<BackendAlbumLinkResponse>('/api/v1/album-links', {
      albumId,
      platformId: dto.platformId,
      url: dto.url,
    })
    return mapLink(raw)
  }

  delete(albumId: string, platformId: string): Promise<void> {
    return del(`/api/v1/album-links/${albumId}/${platformId}`)
  }
}