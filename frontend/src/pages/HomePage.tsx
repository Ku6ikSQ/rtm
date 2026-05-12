import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useNavigate, Link } from 'react-router-dom'
import { Search } from 'lucide-react'
import { albumService, genreService, reviewService } from '@/api'
import { AlbumCard } from '@/components/album/AlbumCard'
import { RatingBadge } from '@/components/common/RatingBadge'
import { PageSpinner } from '@/components/common/Spinner'
import { formatDateShort } from '@/utils/formatters'

export function HomePage() {
  const navigate = useNavigate()
  const [q, setQ] = useState('')

  const { data: topPage, isLoading } = useQuery({
    queryKey: ['albums', 'top8'],
    queryFn: () => albumService.getAll({ sort: 'rating', order: 'desc', size: 8 }),
  })

  const { data: genres = [] } = useQuery({
    queryKey: ['genres'],
    queryFn: () => genreService.getAll(),
  })

  const { data: recentReviews = [] } = useQuery({
    queryKey: ['reviews', 'recent'],
    queryFn: () => reviewService.getRecent(4),
  })

  function handleSearch(e: React.FormEvent) {
    e.preventDefault()
    if (q.trim()) navigate(`/catalog?q=${encodeURIComponent(q.trim())}`)
  }

  const popularGenres = genres.filter((g) => !g.parentId).slice(0, 8)

  return (
    <div className="space-y-12">
      {/* Hero */}
      <section className="py-10 text-center">
        <h1 className="mb-2 text-3xl font-bold tracking-tight md:text-4xl">
          Каталог музыкальных рецензий
        </h1>
        <p className="mb-6 text-muted-foreground">Открывайте альбомы, читайте и пишите рецензии</p>
        <form onSubmit={handleSearch} className="mx-auto flex max-w-md gap-2">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <input
              value={q}
              onChange={(e) => setQ(e.target.value)}
              placeholder="Альбом, артист…"
              className="h-10 w-full rounded border border-border bg-transparent pl-9 pr-3 text-sm focus:border-foreground focus:outline-none"
            />
          </div>
          <button
            type="submit"
            className="h-10 rounded border border-foreground bg-foreground px-5 text-sm font-medium text-background hover:opacity-80"
          >
            Найти
          </button>
        </form>

        {popularGenres.length > 0 && (
          <div className="mt-4 flex flex-wrap justify-center gap-2">
            {popularGenres.map((g) => (
              <Link
                key={g.id}
                to={`/catalog?genreId=${g.id}`}
                className="rounded-full border border-border px-3 py-1 text-xs text-muted-foreground transition-colors hover:border-foreground hover:text-foreground"
              >
                {g.name}
              </Link>
            ))}
          </div>
        )}
      </section>

      {/* Top albums */}
      <section>
        <div className="mb-4 flex items-center justify-between">
          <h2 className="font-semibold">Топ альбомов</h2>
          <Link to="/catalog" className="text-sm text-muted-foreground hover:text-foreground">
            Все →
          </Link>
        </div>
        {isLoading ? (
          <PageSpinner />
        ) : (
          <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6">
            {(topPage?.content ?? []).map((album) => (
              <AlbumCard key={album.id} album={album} />
            ))}
          </div>
        )}
      </section>

      {/* Recent reviews */}
      {recentReviews.length > 0 && (
        <section>
          <h2 className="mb-4 font-semibold">Последние рецензии</h2>
          <div className="space-y-3">
            {recentReviews.map((review) => (
              <Link
                key={review.id}
                to={`/album/${review.albumId}`}
                className="flex gap-3 rounded border border-border p-3 transition-colors hover:border-foreground/40"
              >
                <div className="flex h-14 w-14 flex-shrink-0 items-center justify-center rounded border border-border bg-muted text-xl font-bold text-muted-foreground">
                  ♪
                </div>
                <div className="min-w-0 flex-1">
                  <div className="flex items-start justify-between gap-2">
                    <p className="truncate text-sm font-medium">{`Альбом #${review.albumId}`}</p>
                    <RatingBadge rating={review.score} size="sm" className="flex-shrink-0" />
                  </div>
                  <p className="mt-0.5 text-xs text-muted-foreground">
                    {review.author?.username} · {formatDateShort(review.createdAt)}
                  </p>
                  <p className="mt-1 line-clamp-2 text-xs text-muted-foreground">{review.reviewText}</p>
                </div>
              </Link>
            ))}
          </div>
        </section>
      )}
    </div>
  )
}
