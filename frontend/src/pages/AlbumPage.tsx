import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useParams, Link, useNavigate } from 'react-router-dom'
import { ExternalLink, Pencil, Trash2 } from 'lucide-react'
import { toast } from 'sonner'
import { albumService } from '@/api'
import { useAuth } from '@/hooks/useAuth'
import { PageSpinner } from '@/components/common/Spinner'
import { RatingBadge } from '@/components/common/RatingBadge'
import { TrackList } from '@/components/album/TrackList'
import { ReviewList } from '@/components/review/ReviewList'
import { ConfirmDialog } from '@/components/common/ConfirmDialog'

export function AlbumPage() {
  const { id } = useParams<{ id: string }>()
  const { user, hasRole } = useAuth()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [confirmOpen, setConfirmOpen] = useState(false)

  const { data: album, isLoading } = useQuery({
    queryKey: ['album', id],
    queryFn: () => albumService.getById(id!),
    enabled: !!id,
  })

  const canEdit = !!user && hasRole(['ADMIN', 'MODERATOR'])

  const deleteMutation = useMutation({
    mutationFn: () => albumService.delete(id!),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['albums'] })
      toast.success('Альбом удалён')
      navigate('/catalog')
    },
    onError: (err) => toast.error(err instanceof Error ? err.message : 'Ошибка'),
  })

  if (isLoading) return <PageSpinner />
  if (!album) return <p className="py-8 text-center text-muted-foreground">Альбом не найден</p>

  const primaryArtist = album.artists[0]?.artist

  return (
    <div className="space-y-8">
      <ConfirmDialog
        open={confirmOpen}
        title="Удалить альбом?"
        description={`«${album.title}» будет удалён без возможности восстановления.`}
        confirmLabel="Удалить"
        onConfirm={() => { setConfirmOpen(false); deleteMutation.mutate() }}
        onCancel={() => setConfirmOpen(false)}
      />

      {/* Album header */}
      <div className="flex flex-col gap-6 sm:flex-row">
        {/* Cover */}
        <div className="mx-auto h-56 w-56 flex-shrink-0 overflow-hidden rounded border border-border sm:mx-0 sm:h-64 sm:w-64">
          {album.coverUrl ? (
            <img src={album.coverUrl} alt={album.title} className="h-full w-full object-cover" />
          ) : (
            <div className="flex h-full w-full items-center justify-center bg-muted text-5xl font-bold text-muted-foreground">
              {album.title[0]}
            </div>
          )}
        </div>

        {/* Meta */}
        <div className="flex-1">
          <div className="flex items-start justify-between gap-3">
            <h1 className="text-2xl font-bold leading-tight md:text-3xl">{album.title}</h1>
            {canEdit && (
              <div className="flex gap-1.5">
                <Link
                  to={`/album/${album.id}/edit`}
                  className="flex h-8 items-center gap-1.5 rounded border border-border px-2.5 text-xs text-muted-foreground hover:text-foreground"
                >
                  <Pencil className="h-3.5 w-3.5" />
                  Изменить
                </Link>
                <button
                  onClick={() => setConfirmOpen(true)}
                  className="flex h-8 items-center gap-1.5 rounded border border-border px-2.5 text-xs text-muted-foreground hover:border-destructive hover:text-destructive"
                >
                  <Trash2 className="h-3.5 w-3.5" />
                  Удалить
                </button>
              </div>
            )}
          </div>

          {primaryArtist && (
            <Link
              to={`/artist/${primaryArtist.id}`}
              className="mt-1 inline-block text-base text-muted-foreground hover:text-foreground"
            >
              {primaryArtist.stageName}
            </Link>
          )}

          <div className="mt-3 flex flex-wrap items-center gap-3 text-sm text-muted-foreground">
            <span>{album.releaseYear}</span>
            <RatingBadge rating={album.avgRating} size="md" />
            <span>{album.reviewCount} {getReviewWord(album.reviewCount)}</span>
            {album.tracks.length > 0 && (
              <span>{album.tracks.length} треков</span>
            )}
          </div>

          {album.genres.length > 0 && (
            <div className="mt-3 flex flex-wrap gap-1.5">
              {album.genres.map((g) => (
                <Link
                  key={g.id}
                  to={`/catalog?genreId=${g.id}`}
                  className="rounded-full border border-border px-2.5 py-0.5 text-xs text-muted-foreground hover:border-foreground hover:text-foreground"
                >
                  {g.name}
                </Link>
              ))}
            </div>
          )}

          {album.description && (
            <p className="mt-4 text-sm leading-relaxed text-muted-foreground">{album.description}</p>
          )}

          {album.links.length > 0 && (
            <div className="mt-5">
              <p className="mb-2 text-xs font-medium uppercase tracking-wider text-muted-foreground">
                Слушать на
              </p>
              <div className="flex flex-wrap gap-2">
                {album.links.map((link) => (
                  <a
                    key={link.platformId}
                    href={link.url}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="flex h-8 items-center gap-1.5 rounded border border-border px-3 text-xs text-muted-foreground transition-colors hover:border-foreground hover:text-foreground"
                  >
                    {link.platform?.name ?? 'Платформа'}
                    <ExternalLink className="h-3 w-3" />
                  </a>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Tracklist */}
      {album.tracks.length > 0 && (
        <section>
          <h2 className="mb-4 text-lg font-semibold">Треклист</h2>
          <TrackList tracks={album.tracks} />
        </section>
      )}

      {/* Reviews */}
      <ReviewList albumId={album.id} />
    </div>
  )
}

function getReviewWord(n: number): string {
  if (n % 100 >= 11 && n % 100 <= 19) return 'рецензий'
  switch (n % 10) {
    case 1: return 'рецензия'
    case 2:
    case 3:
    case 4: return 'рецензии'
    default: return 'рецензий'
  }
}
