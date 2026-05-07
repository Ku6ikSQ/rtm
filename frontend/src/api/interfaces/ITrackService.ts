import type { Track } from '@/types/entities'

export interface TrackDto {
  title: string
  trackNumber: number
  durationSeconds: number
}

export interface ITrackService {
  getByAlbum(albumId: string): Promise<Track[]>
  create(albumId: string, dto: TrackDto): Promise<Track>
  update(id: string, dto: Partial<TrackDto>): Promise<Track>
  delete(id: string): Promise<void>
}
