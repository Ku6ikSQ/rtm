import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useParams } from 'react-router-dom'
import { Pencil } from 'lucide-react'
import { artistService, albumService } from '@/api'
import { useAuth } from '@/hooks/useAuth'
import { PageSpinner } from '@/components/common/Spinner'
import { AlbumGrid } from '@/components/album/AlbumGrid'
import { ArtistEditModal } from '@/components/artist/ArtistEditModal'

export function ArtistPage() {
  const { id } = useParams<{ id: string }>()
  const { hasRole } = useAuth()
  const canEdit = hasRole(['ADMIN', 'MODERATOR'])
  const [showEdit, setShowEdit] = useState(false)

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
      {showEdit && <ArtistEditModal artist={artist} onClose={() => setShowEdit(false)} />}

      {/* Artist header */}
      <div className="mb-8 flex flex-col items-center gap-4 sm:flex-row sm:items-start">
        <div className="flex h-40 w-40 flex-shrink-0 items-center justify-center overflow-hidden rounded-full border border-border bg-muted text-4xl font-bold text-muted-foreground">
          {artist.imageUrl ? (
            <img src={artist.imageUrl} alt={artist.stageName} className="h-full w-full object-cover" />
          ) : (
            artist.stageName[0]
          )}
        </div>
        <div className="text-center sm:text-left">
          <div className="flex items-center justify-center gap-2 sm:justify-start">
            <h1 className="text-2xl font-bold">{artist.stageName}</h1>
            {canEdit && (
              <button
                onClick={() => setShowEdit(true)}
                title="Редактировать"
                className="flex h-7 w-7 items-center justify-center rounded border border-border text-muted-foreground hover:text-foreground"
              >
                <Pencil className="h-3.5 w-3.5" />
              </button>
            )}
          </div>
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
