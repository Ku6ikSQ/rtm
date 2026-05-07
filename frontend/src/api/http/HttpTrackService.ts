import type { ITrackService, TrackDto } from '@/api/interfaces/ITrackService'
import type { Track } from '@/types/entities'
import { get, post, put, del } from './apiClient'

export class HttpTrackService implements ITrackService {
  getByAlbum(albumId: string): Promise<Track[]> { return get(`/api/v1/albums/${albumId}/tracks`) }
  create(albumId: string, dto: TrackDto): Promise<Track> { return post(`/api/v1/albums/${albumId}/tracks`, dto) }
  update(id: string, dto: Partial<TrackDto>): Promise<Track> { return put(`/api/v1/tracks/${id}`, dto) }
  delete(id: string): Promise<void> { return del(`/api/v1/tracks/${id}`) }
}
