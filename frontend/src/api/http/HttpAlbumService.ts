import type { IAlbumService } from '@/api/interfaces/IAlbumService'
import type { Album, AlbumFilters, CreateAlbumDto, UpdateAlbumDto, PageResult } from '@/types/entities'
import { get, post, put, del, request } from './apiClient'

function buildQuery(filters?: AlbumFilters): string {
  if (!filters) return ''
  const params = new URLSearchParams()
  if (filters.q) params.set('q', filters.q)
  if (filters.genreId) params.set('genreId', filters.genreId)
  if (filters.yearFrom != null) params.set('yearFrom', String(filters.yearFrom))
  if (filters.yearTo != null) params.set('yearTo', String(filters.yearTo))
  if (filters.ratingMin != null) params.set('ratingMin', String(filters.ratingMin))
  if (filters.ratingMax != null) params.set('ratingMax', String(filters.ratingMax))
  if (filters.sort) params.set('sort', filters.sort)
  if (filters.order) params.set('order', filters.order)
  if (filters.page != null) params.set('page', String(filters.page))
  if (filters.size != null) params.set('size', String(filters.size))
  const qs = params.toString()
  return qs ? `?${qs}` : ''
}

export class HttpAlbumService implements IAlbumService {
  getAll(filters?: AlbumFilters): Promise<PageResult<Album>> {
    return get(`/api/v1/albums${buildQuery(filters)}`)
  }

  getById(id: string): Promise<Album> {
    return get(`/api/v1/albums/${id}`)
  }

  create(dto: CreateAlbumDto): Promise<Album> {
    return post('/api/v1/albums', dto)
  }

  update(id: string, dto: UpdateAlbumDto): Promise<Album> {
    return put(`/api/v1/albums/${id}`, dto)
  }

  delete(id: string): Promise<void> {
    return del(`/api/v1/albums/${id}`)
  }

  async uploadCover(id: string, file: File): Promise<string> {
    const form = new FormData()
    form.append('file', file)
    const res = await request<{ url: string }>(`/api/v1/albums/${id}/cover`, {
      method: 'POST',
      body: form,
      headers: {},
    })
    return res.url
  }
}
