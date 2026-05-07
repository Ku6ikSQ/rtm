import type { ITrackService, TrackDto } from '@/api/interfaces/ITrackService'
import type { Track } from '@/types/entities'
import { ApiError } from '@/types/errors'
import { delay } from '@/utils/delay'
import { mockTracks, mockAlbums } from './data'

export class MockTrackService implements ITrackService {
  async getByAlbum(albumId: string): Promise<Track[]> {
    await delay(200)
    return mockTracks.filter((t) => t.albumId === albumId).sort((a, b) => a.trackNumber - b.trackNumber)
  }

  async create(albumId: string, dto: TrackDto): Promise<Track> {
    await delay(300)
    const track: Track = { id: String(Date.now()), albumId, ...dto }
    mockTracks.push(track)
    const album = mockAlbums.find((a) => a.id === albumId)
    if (album) album.tracks = mockTracks.filter((t) => t.albumId === albumId)
    return track
  }

  async update(id: string, dto: Partial<TrackDto>): Promise<Track> {
    await delay(250)
    const idx = mockTracks.findIndex((t) => t.id === id)
    if (idx === -1) throw new ApiError(404, 'Трек не найден')
    mockTracks[idx] = { ...mockTracks[idx], ...dto }
    return mockTracks[idx]
  }

  async delete(id: string): Promise<void> {
    await delay(200)
    const idx = mockTracks.findIndex((t) => t.id === id)
    if (idx === -1) throw new ApiError(404, 'Трек не найден')
    const albumId = mockTracks[idx].albumId
    mockTracks.splice(idx, 1)
    const album = mockAlbums.find((a) => a.id === albumId)
    if (album) album.tracks = mockTracks.filter((t) => t.albumId === albumId)
  }
}
