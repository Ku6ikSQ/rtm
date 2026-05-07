import { Link } from 'react-router-dom'
import { RatingBadge } from '@/components/common/RatingBadge'
import { cn } from '@/utils/cn'
import type { Album } from '@/types/entities'

interface AlbumCardProps {
  album: Album
  className?: string
}

function CoverPlaceholder({ albumId, title }: { albumId: string; title: string }) {
  const patterns = ['stripes', 'dots', 'grid', 'lines', 'circle', 'diag']
  const pattern = patterns[parseInt(albumId, 10) % patterns.length]
  const letter = title[0]?.toUpperCase() ?? '?'

  return (
    <div
      className={cn(
        'flex aspect-square w-full items-center justify-center bg-muted text-2xl font-bold text-muted-foreground',
        pattern === 'stripes' && 'bg-[repeating-linear-gradient(45deg,transparent,transparent_4px,hsl(var(--border))_4px,hsl(var(--border))_5px)]',
        pattern === 'dots' && 'bg-[radial-gradient(hsl(var(--border))_1px,transparent_1px)] bg-[size:8px_8px]',
        pattern === 'grid' && 'bg-[linear-gradient(hsl(var(--border))_1px,transparent_1px),linear-gradient(90deg,hsl(var(--border))_1px,transparent_1px)] bg-[size:12px_12px]',
      )}
    >
      <span className="drop-shadow">{letter}</span>
    </div>
  )
}

export function AlbumCard({ album, className }: AlbumCardProps) {
  const primaryArtist = album.artists[0]?.artist

  return (
    <Link to={`/album/${album.id}`} className={cn('group block', className)}>
      <div className="overflow-hidden rounded border border-border">
        {album.coverUrl ? (
          <img
            src={album.coverUrl}
            alt={album.title}
            className="aspect-square w-full object-cover transition-opacity group-hover:opacity-80"
          />
        ) : (
          <CoverPlaceholder albumId={album.id} title={album.title} />
        )}
      </div>
      <div className="mt-2 space-y-0.5">
        <div className="flex items-start justify-between gap-2">
          <p className="line-clamp-1 text-sm font-medium group-hover:underline">{album.title}</p>
          <RatingBadge rating={album.avgRating} size="sm" className="flex-shrink-0" />
        </div>
        {primaryArtist && (
          <p className="text-xs text-muted-foreground">{primaryArtist.stageName}</p>
        )}
        <p className="text-xs text-muted-foreground">{album.releaseYear}</p>
      </div>
    </Link>
  )
}
