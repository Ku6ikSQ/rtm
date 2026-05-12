import type { ITrackService, TrackDto } from '@/api/interfaces/ITrackService'
import type { Track } from '@/types/entities'
import { del, get, patch, post } from './apiClient'

interface BackendTrackResponse {
  id: string
  title: string
  albumId: string
  trackNumber: number
  durationSeconds: number
}

interface BackendPage<T> {
  content: T[]
}

function mapTrack(raw: BackendTrackResponse): Track {
  return {
    id: raw.id,
    title: raw.title,
    albumId: raw.albumId,
    trackNumber: raw.trackNumber,
    durationSeconds: raw.durationSeconds,
  }
}

export class HttpTrackService implements ITrackService {
  async getByAlbum(albumId: string): Promise<Track[]> {
    const page = await get<BackendPage<BackendTrackResponse>>(
      `/api/v1/tracks?albumId=${albumId}&size=200&sort=trackNumber`
    )
    return page.content.map(mapTrack)
  }

  async create(albumId: string, dto: TrackDto): Promise<Track> {
    const raw = await post<BackendTrackResponse>('/api/v1/tracks', {
      albumId,
      title: dto.title,
      trackNumber: dto.trackNumber,
      durationSeconds: dto.durationSeconds,
    })
    return mapTrack(raw)
  }

  async update(id: string, dto: Partial<TrackDto>): Promise<Track> {
    const patches: Promise<unknown>[] = []

    if (dto.title !== undefined) {
      patches.push(patch(`/api/v1/tracks/${id}/title`, { title: dto.title }))
    }
    if (dto.trackNumber !== undefined) {
      patches.push(patch(`/api/v1/tracks/${id}/track-number`, { trackNumber: dto.trackNumber }))
    }
    if (dto.durationSeconds !== undefined) {
      patches.push(patch(`/api/v1/tracks/${id}/duration-seconds`, { durationSeconds: dto.durationSeconds }))
    }

    await Promise.all(patches)
    const raw = await get<BackendTrackResponse>(`/api/v1/tracks/${id}`)
    return mapTrack(raw)
  }

  delete(id: string): Promise<void> {
    return del(`/api/v1/tracks/${id}`)
  }
}