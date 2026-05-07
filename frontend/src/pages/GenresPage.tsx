import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { genreService } from '@/api'
import { PageSpinner } from '@/components/common/Spinner'
import type { Genre } from '@/types/entities'

export function GenresPage() {
  const { data: genres = [], isLoading } = useQuery({
    queryKey: ['genres'],
    queryFn: () => genreService.getAll(),
  })

  if (isLoading) return <PageSpinner />

  const roots = genres.filter((g) => !g.parentId)
  const children = (parentId: string) => genres.filter((g) => g.parentId === parentId)

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold">Жанры</h1>
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {roots.map((root) => (
          <GenreCard key={root.id} root={root} children={children(root.id)} />
        ))}
      </div>
    </div>
  )
}

function GenreCard({ root, children }: { root: Genre; children: Genre[] }) {
  return (
    <div className="rounded border border-border p-4">
      <Link
        to={`/catalog?genreId=${root.id}`}
        className="flex items-center justify-between hover:text-muted-foreground"
      >
        <span className="font-medium">{root.name}</span>
        {root.albumCount != null && (
          <span className="text-xs text-muted-foreground">{root.albumCount}</span>
        )}
      </Link>
      {children.length > 0 && (
        <>
          <div className="my-3 border-t border-border" />
          <ul className="space-y-1.5">
            {children.map((child) => (
              <li key={child.id}>
                <Link
                  to={`/catalog?genreId=${child.id}`}
                  className="flex items-center justify-between pl-3 text-sm text-muted-foreground hover:text-foreground"
                >
                  <span>↳ {child.name}</span>
                  {child.albumCount != null && <span className="text-xs">{child.albumCount}</span>}
                </Link>
              </li>
            ))}
          </ul>
        </>
      )}
    </div>
  )
}
