import type { IAlbumService } from '@/api/interfaces/IAlbumService'
import type { Album, AlbumFilters, CreateAlbumDto, UpdateAlbumDto, PageResult } from '@/types/entities'
import { ApiError } from '@/types/errors'
import { delay } from '@/utils/delay'
import { mockAlbums, mockArtists, mockGenres, mockPlatforms } from './data'

export class MockAlbumService implements IAlbumService {
  async getAll(filters?: AlbumFilters): Promise<PageResult<Album>> {
    await delay(300)
    let items = [...mockAlbums]

    if (filters?.q) {
      const q = filters.q.toLowerCase()
      items = items.filter(
        (a) =>
          a.title.toLowerCase().includes(q) ||
          a.artists.some((aa) => aa.artist?.stageName.toLowerCase().includes(q)),
      )
    }
    if (filters?.genreId) {
      items = items.filter((a) => a.genres.some((g) => g.id === filters.genreId))
    }
    if (filters?.yearFrom) {
      items = items.filter((a) => a.releaseYear >= filters.yearFrom!)
    }
    if (filters?.yearTo) {
      items = items.filter((a) => a.releaseYear <= filters.yearTo!)
    }
    if (filters?.ratingMin != null) {
      items = items.filter((a) => a.avgRating >= filters.ratingMin!)
    }
    if (filters?.ratingMax != null) {
      items = items.filter((a) => a.avgRating <= filters.ratingMax!)
    }

    const sort = filters?.sort ?? 'rating'
    const order = filters?.order ?? 'desc'
    items.sort((a, b) => {
      let cmp = 0
      if (sort === 'rating') cmp = a.avgRating - b.avgRating
      else if (sort === 'year') cmp = a.releaseYear - b.releaseYear
      else cmp = a.createdAt.localeCompare(b.createdAt)
      return order === 'desc' ? -cmp : cmp
    })

    const page = filters?.page ?? 0
    const size = filters?.size ?? 12
    const start = page * size
    const content = items.slice(start, start + size)

    return {
      content,
      totalElements: items.length,
      totalPages: Math.ceil(items.length / size),
      page,
      size,
    }
  }

  async getById(id: string): Promise<Album> {
    await delay(200)
    const album = mockAlbums.find((a) => a.id === id)
    if (!album) throw new ApiError(404, 'Альбом не найден')
    return album
  }

  async create(dto: CreateAlbumDto): Promise<Album> {
    await delay(400)
    const album: Album = {
      id: String(Date.now()),
      title: dto.title,
      description: dto.description,
      releaseYear: dto.releaseYear,
      avgRating: 0,
      reviewCount: 0,
      createdAt: new Date().toISOString(),
      artists: dto.artistIds.map((artistId, order) => ({
        albumId: '',
        artistId,
        role: 'MAIN',
        order,
        artist: mockArtists.find((a) => a.id === artistId),
      })),
      genres: dto.genreIds.map((id) => mockGenres.find((g) => g.id === id)!).filter(Boolean),
      tracks: (dto.tracks ?? []).map((t, i) => ({
        id: `${Date.now()}-${i}`,
        albumId: '',
        title: t.title,
        trackNumber: i + 1,
        durationSeconds: t.durationSeconds ?? 0,
      })),
      links: (dto.links ?? []).map((l) => ({
        albumId: '',
        platformId: l.platformId,
        url: l.url,
        platform: mockPlatforms.find((p) => p.id === l.platformId),
      })),
    }
    album.artists.forEach((aa) => (aa.albumId = album.id))
    album.tracks.forEach((t) => (t.albumId = album.id))
    album.links.forEach((l) => (l.albumId = album.id))
    mockAlbums.push(album)
    return album
  }

  async update(id: string, dto: UpdateAlbumDto): Promise<Album> {
    await delay(300)
    const idx = mockAlbums.findIndex((a) => a.id === id)
    if (idx === -1) throw new ApiError(404, 'Альбом не найден')
    const album = { ...mockAlbums[idx] }
    if (dto.title) album.title = dto.title
    if (dto.description !== undefined) album.description = dto.description
    if (dto.releaseYear) album.releaseYear = dto.releaseYear
    if (dto.artistIds) {
      album.artists = dto.artistIds.map((artistId, order) => ({
        albumId: id,
        artistId,
        role: 'MAIN' as const,
        order,
        artist: mockArtists.find((a) => a.id === artistId),
      }))
    }
    if (dto.genreIds) {
      album.genres = dto.genreIds.map((gid) => mockGenres.find((g) => g.id === gid)!).filter(Boolean)
    }
    if (dto.tracks !== undefined) {
      album.tracks = dto.tracks
        .filter((t) => t.title.trim())
        .map((t, i) => ({
          id: `${Date.now()}-${i}`,
          albumId: id,
          title: t.title.trim(),
          trackNumber: i + 1,
          durationSeconds: t.durationSeconds ?? 0,
        }))
    }
    if (dto.links !== undefined) {
      album.links = dto.links
        .filter((l) => l.platformId && l.url.trim())
        .map((l) => ({
          albumId: id,
          platformId: l.platformId,
          url: l.url.trim(),
          platform: mockPlatforms.find((p) => p.id === l.platformId),
        }))
    }
    mockAlbums[idx] = album
    return album
  }

  async delete(id: string): Promise<void> {
    await delay(250)
    const idx = mockAlbums.findIndex((a) => a.id === id)
    if (idx === -1) throw new ApiError(404, 'Альбом не найден')
    mockAlbums.splice(idx, 1)
  }

  async uploadCover(_id: string, _file: File): Promise<string> {
    await delay(500)
    return `https://placeholder.co/300x300?text=Cover`
  }
}
