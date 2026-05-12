import { useQuery } from '@tanstack/react-query'
import { useParams } from 'react-router-dom'
import { artistService, albumService } from '@/api'
import { PageSpinner } from '@/components/common/Spinner'
import { AlbumGrid } from '@/components/album/AlbumGrid'

export function ArtistPage() {
  const { id } = useParams<{ id: string }>()

  const { data: artist, isLoading: loadingArtist } = useQuery({
    queryKey: ['artist', id],
    queryFn: () => artistService.getById(id!),
    enabled: !!id,
  })

  const { data: albumsPage, isLoading: loadingAlbums } = useQuery({
    queryKey: ['albums', { artistId: id }],
    // artistId filter: backend will use it after update (see docs/backend-update.md #10).
    // Client-side filter below is a safety fallback for partial backend support.
    queryFn: () => albumService.getAll({ artistId: id, size: 100 }),
    enabled: !!id,
  })

  if (loadingArtist) return <PageSpinner />
  if (!artist) return <p className="py-8 text-center text-muted-foreground">Артист не найден</p>

  const albums = (albumsPage?.content ?? []).filter((a) =>
    a.artists.some((aa) => aa.artistId === id),
  )

  return (
    <div>
      {/* Artist header */}
      <div className="mb-8 flex flex-col items-center gap-4 sm:flex-row sm:items-start">
        <div className="flex h-40 w-40 flex-shrink-0 items-center justify-center rounded-full border border-border bg-muted text-4xl font-bold text-muted-foreground">
          {artist.stageName[0]}
        </div>
        <div className="text-center sm:text-left">
          <h1 className="text-2xl font-bold">{artist.stageName}</h1>
          {artist.realName && (
            <p className="mt-0.5 text-sm text-muted-foreground">{artist.realName}</p>
          )}
          <div className="mt-2 flex flex-wrap justify-center gap-3 text-sm text-muted-foreground sm:justify-start">
            {artist.country && <span>{artist.country}</span>}
            <span>{albums.length} релизов</span>
          </div>
          {artist.bio && <p className="mt-3 max-w-xl text-sm text-muted-foreground">{artist.bio}</p>}
        </div>
      </div>

      {/* Discography */}
      <section>
        <h2 className="mb-4 text-sm font-medium uppercase tracking-wider text-muted-foreground">
          Дискография
        </h2>
        {loadingAlbums ? (
          <PageSpinner />
        ) : albums.length === 0 ? (
          <p className="text-sm text-muted-foreground">Альбомы не найдены</p>
        ) : (
          <AlbumGrid albums={albums} />
        )}
      </section>
    </div>
  )
}
