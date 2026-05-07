import { AlbumCard } from './AlbumCard'
import { EmptyState } from '@/components/common/EmptyState'
import type { Album } from '@/types/entities'

interface AlbumGridProps {
  albums: Album[]
  emptyMessage?: string
}

export function AlbumGrid({ albums, emptyMessage }: AlbumGridProps) {
  if (albums.length === 0) {
    return <EmptyState title={emptyMessage ?? 'Альбомы не найдены'} />
  }

  return (
    <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6">
      {albums.map((album) => (
        <AlbumCard key={album.id} album={album} />
      ))}
    </div>
  )
}
