import { cn } from '@/utils/cn'
import { formatRating } from '@/utils/formatters'

interface RatingBadgeProps {
  rating: number
  size?: 'sm' | 'md' | 'lg'
  className?: string
}

export function RatingBadge({ rating, size = 'md', className }: RatingBadgeProps) {
  return (
    <span
      className={cn(
        'inline-flex items-center justify-center rounded border border-border font-medium tabular-nums',
        size === 'sm' && 'h-5 min-w-[2rem] px-1.5 text-xs',
        size === 'md' && 'h-6 min-w-[2.5rem] px-2 text-sm',
        size === 'lg' && 'h-8 min-w-[3rem] px-2 text-base',
        rating >= 8 && 'border-foreground bg-foreground text-background',
        rating >= 5 && rating < 8 && 'bg-transparent text-foreground',
        rating < 5 && 'bg-transparent text-muted-foreground',
        className,
      )}
    >
      {formatRating(rating)}
    </span>
  )
}