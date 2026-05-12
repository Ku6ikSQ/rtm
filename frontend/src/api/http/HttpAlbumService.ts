import type { IAlbumService } from '@/api/interfaces/IAlbumService'
import type {
  Album,
  AlbumArtist,
  AlbumFilters,
  AlbumLink,
  CreateAlbumDto,
  Genre,
  PageResult,
  Track,
  UpdateAlbumDto,
} from '@/types/entities'
import { del, get, patch, post, uploadFile } from './apiClient'

// ── Backend shapes ─────────────────────────────────────────────────────────────

interface BackendArtistSummary {
  albumId: string
  artistId: string
  stageName: string
  role: string
  order: number
}

interface BackendAlbumResponse {
  id: string
  title: string
  description: string | null
  releaseYear: number
  coverUrl: string | null
  avgRating: string | number
  reviewCount: number
  createdAt: string
  createdBy: string
  artists: BackendArtistSummary[]
}

interface BackendAlbumGenreResponse {
  albumId: string
  genreId: string
}

interface BackendTrackResponse {
  id: string
  title: string
  albumId: string
  trackNumber: number
  durationSeconds: number
}

interface BackendArtistResponse {
  id: string
  stageName: string
  realName: string | null
  bio: string | null
  country: string | null
  imageUrl: string | null
}

interface BackendGenreResponse {
  id: string
  name: string
  slug: string
  description: string | null
  parentId: string | null
}

interface BackendAlbumLinkResponse {
  albumId: string
  platformId: string
  url: string
  platformName: string
  platformLogoUrl: string | null
}

interface BackendPage<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

// ── Mappers ────────────────────────────────────────────────────────────────────

function mapAlbumBase(raw: BackendAlbumResponse): Omit<Album, 'artists' | 'genres' | 'tracks' | 'links'> {
  return {
    id: raw.id,
    title: raw.title,
    description: raw.description ?? undefined,
    releaseYear: raw.releaseYear,
    coverUrl: raw.coverUrl ?? undefined,
    avgRating: Number(raw.avgRating ?? 0),
    reviewCount: raw.reviewCount ?? 0,
    createdAt: raw.createdAt,
  }
}

// Builds AlbumArtist[] from the embedded summary (no extra requests — stageName only)
function mapEmbeddedArtists(summaries: BackendArtistSummary[]): AlbumArtist[] {
  return summaries.map(s => ({
    albumId: s.albumId,
    artistId: s.artistId,
    role: s.role as AlbumArtist['role'],
    order: s.order,
    artist: { id: s.artistId, stageName: s.stageName },
  }))
}

// ── Query builder ──────────────────────────────────────────────────────────────

function buildQuery(filters?: AlbumFilters): string {
  if (!filters) return ''
  const p = new URLSearchParams()
  if (filters.q)              p.set('title', filters.q)
  if (filters.genreId)        p.set('genreId', filters.genreId)
  if (filters.artistId)       p.set('artistId', filters.artistId)
  if (filters.yearFrom != null) p.set('releaseYear', String(filters.yearFrom))
  if (filters.page != null)  p.set('page', String(filters.page))
  if (filters.size != null)  p.set('size', String(filters.size))
  if (filters.sort === 'year')       p.set('sort', 'releaseYear')
  else if (filters.sort === 'rating') p.set('sort', 'avgRating')
  else if (filters.sort)              p.set('sort', filters.sort)
  if (filters.order) p.set('order', filters.order)
  const qs = p.toString()
  return qs ? `?${qs}` : ''
}

// ── Service ────────────────────────────────────────────────────────────────────

export class HttpAlbumService implements IAlbumService {
  async getAll(filters?: AlbumFilters): Promise<PageResult<Album>> {
    const page = await get<BackendPage<BackendAlbumResponse>>(
      `/api/v1/albums${buildQuery(filters)}`
    )
    return {
      content: page.content.map(raw => ({
        ...mapAlbumBase(raw),
        // Use embedded artist summaries — no extra requests needed for list view
        artists: mapEmbeddedArtists(raw.artists ?? []),
        genres: [],
        tracks: [],
        links: [],
      })),
      page: page.page,
      size: page.size,
      totalElements: page.totalElements,
      totalPages: page.totalPages,
    }
  }

  async getById(id: string): Promise<Album> {
    const raw = await get<BackendAlbumResponse>(`/api/v1/albums/${id}`)

    // Fetch genres and tracks in parallel.
    // For artists — use embedded summary IDs to fetch full artist details in parallel.
    const artistIds = (raw.artists ?? []).map(a => a.artistId)

    const [genreLinks, tracksPage, rawLinks, artistDetails] = await Promise.all([
      get<BackendAlbumGenreResponse[]>(`/api/v1/album-genres/by-album/${id}`),
      get<BackendPage<BackendTrackResponse>>(`/api/v1/tracks?albumId=${id}&size=200`),
      get<BackendAlbumLinkResponse[]>(`/api/v1/album-links/by-album/${id}`),
      Promise.all(artistIds.map(aid => get<BackendArtistResponse>(`/api/v1/artists/${aid}`))),
    ])

    const genreDetails = await Promise.all(
      genreLinks.map(link => get<BackendGenreResponse>(`/api/v1/genres/${link.genreId}`))
    )

    const artists: AlbumArtist[] = (raw.artists ?? []).map((summary, i) => ({
      albumId: summary.albumId,
      artistId: summary.artistId,
      role: summary.role as AlbumArtist['role'],
      order: summary.order,
      artist: {
        id: artistDetails[i].id,
        stageName: artistDetails[i].stageName,
        realName: artistDetails[i].realName ?? undefined,
        bio: artistDetails[i].bio ?? undefined,
        country: artistDetails[i].country ?? undefined,
        imageUrl: artistDetails[i].imageUrl ?? undefined,
      },
    }))

    const genres: Genre[] = genreDetails.map(g => ({
      id: g.id,
      name: g.name,
      slug: g.slug,
      description: g.description ?? undefined,
      parentId: g.parentId ?? undefined,
    }))

    const tracks: Track[] = (tracksPage.content ?? []).map(t => ({
      id: t.id,
      title: t.title,
      albumId: t.albumId,
      trackNumber: t.trackNumber,
      durationSeconds: t.durationSeconds,
    }))

    const links: AlbumLink[] = rawLinks.map(l => ({
      albumId: l.albumId,
      platformId: l.platformId,
      url: l.url,
      platform: { id: l.platformId, name: l.platformName, logoUrl: l.platformLogoUrl ?? undefined },
    }))

    return { ...mapAlbumBase(raw), artists, genres, tracks, links }
  }

  async create(dto: CreateAlbumDto): Promise<Album> {
    const album = await post<BackendAlbumResponse>('/api/v1/albums', {
      title: dto.title,
      description: dto.description,
      releaseYear: dto.releaseYear,
    })

    await Promise.all([
      ...dto.artistIds.map((artistId, index) =>
        post('/api/v1/album-artists', { albumId: album.id, artistId, role: 'MAIN', order: index + 1 })
      ),
      ...dto.genreIds.map(genreId =>
        post('/api/v1/album-genres', { albumId: album.id, genreId })
      ),
    ])

    if (dto.tracks?.length) {
      for (let i = 0; i < dto.tracks.length; i++) {
        const t = dto.tracks[i]
        await post('/api/v1/tracks', {
          albumId: album.id,
          title: t.title,
          trackNumber: i + 1,
          durationSeconds: t.durationSeconds ?? 0,
        })
      }
    }

    if (dto.links?.length) {
      await Promise.all(
        dto.links.map(link =>
          post('/api/v1/album-links', { albumId: album.id, platformId: link.platformId, url: link.url })
        )
      )
    }

    return this.getById(album.id)
  }

  async update(id: string, dto: UpdateAlbumDto): Promise<Album> {
    const ops: Promise<unknown>[] = []

    if (dto.title !== undefined)
      ops.push(patch(`/api/v1/albums/${id}/title`, { title: dto.title }))
    if (dto.description !== undefined)
      ops.push(patch(`/api/v1/albums/${id}/description`, { description: dto.description }))
    if (dto.releaseYear !== undefined)
      ops.push(patch(`/api/v1/albums/${id}/release-year`, { releaseYear: dto.releaseYear }))

    if (dto.artistIds !== undefined) {
      ops.push((async () => {
        const current = await get<{ artistId: string }[]>(`/api/v1/album-artists/by-album/${id}`)
        const currentIds = current.map(a => a.artistId)
        const toRemove = currentIds.filter(aid => !dto.artistIds!.includes(aid))
        const toAdd = dto.artistIds!.filter(aid => !currentIds.includes(aid))
        await Promise.all([
          ...toRemove.map(artistId => del(`/api/v1/album-artists/${id}/${artistId}`)),
          ...toAdd.map(artistId => post('/api/v1/album-artists', {
            albumId: id, artistId, role: 'MAIN',
            order: dto.artistIds!.indexOf(artistId) + 1,
          })),
        ])
      })())
    }

    if (dto.genreIds !== undefined) {
      ops.push((async () => {
        const current = await get<{ genreId: string }[]>(`/api/v1/album-genres/by-album/${id}`)
        const currentIds = current.map(g => g.genreId)
        const toRemove = currentIds.filter(gid => !dto.genreIds!.includes(gid))
        const toAdd = dto.genreIds!.filter(gid => !currentIds.includes(gid))
        await Promise.all([
          ...toRemove.map(genreId => del(`/api/v1/album-genres/${id}/${genreId}`)),
          ...toAdd.map(genreId => post('/api/v1/album-genres', { albumId: id, genreId })),
        ])
      })())
    }

    if (dto.tracks !== undefined) {
      ops.push((async () => {
        const page = await get<BackendPage<BackendTrackResponse>>(`/api/v1/tracks?albumId=${id}&size=200`)
        await Promise.all(page.content.map(t => del(`/api/v1/tracks/${t.id}`)))
        for (let i = 0; i < dto.tracks!.length; i++) {
          const t = dto.tracks![i]
          await post('/api/v1/tracks', {
            albumId: id,
            title: t.title,
            trackNumber: i + 1,
            durationSeconds: t.durationSeconds ?? 0,
          })
        }
      })())
    }

    if (dto.links !== undefined) {
      ops.push((async () => {
        const current = await get<BackendAlbumLinkResponse[]>(`/api/v1/album-links/by-album/${id}`)
        await Promise.all(current.map(l => del(`/api/v1/album-links/${id}/${l.platformId}`)))
        await Promise.all(dto.links!.map(link =>
          post('/api/v1/album-links', { albumId: id, platformId: link.platformId, url: link.url })
        ))
      })())
    }

    await Promise.all(ops)
    return this.getById(id)
  }

  delete(id: string): Promise<void> {
    return del(`/api/v1/albums/${id}`)
  }

  async uploadCover(id: string, file: File): Promise<string> {
    await uploadFile(`/api/v1/albums/${id}/cover`, file)
    const album = await get<BackendAlbumResponse>(`/api/v1/albums/${id}`)
    return album.coverUrl ?? ''
  }
}