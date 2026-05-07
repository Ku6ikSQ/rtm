import { useState } from 'react'
import { Trash2, Pencil } from 'lucide-react'
import { RatingBadge } from '@/components/common/RatingBadge'
import { ConfirmDialog } from '@/components/common/ConfirmDialog'
import { formatDateShort } from '@/utils/formatters'
import type { Review } from '@/types/entities'

interface ReviewCardProps {
  review: Review
  currentUserId?: string
  canModerate?: boolean
  onDelete?: (id: string) => void
  onEdit?: (review: Review) => void
}

export function ReviewCard({ review, currentUserId, canModerate, onDelete, onEdit }: ReviewCardProps) {
  const isOwner = currentUserId === review.authorId
  const canDelete = isOwner || canModerate
  const [confirmOpen, setConfirmOpen] = useState(false)

  return (
    <div className="rounded border border-border p-4">
      <ConfirmDialog
        open={confirmOpen}
        title="Удалить рецензию?"
        description="Это действие нельзя отменить."
        confirmLabel="Удалить"
        onConfirm={() => { setConfirmOpen(false); onDelete?.(review.id) }}
        onCancel={() => setConfirmOpen(false)}
      />

      <div className="mb-3 flex items-start justify-between gap-3">
        <div className="flex items-center gap-2">
          <div className="flex h-7 w-7 items-center justify-center rounded-full bg-muted text-xs font-bold">
            {review.author?.username?.[0]?.toUpperCase() ?? '?'}
          </div>
          <div>
            <p className="text-sm font-medium">{review.author?.username ?? 'Аноним'}</p>
            <p className="text-xs text-muted-foreground">{formatDateShort(review.createdAt)}</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <RatingBadge rating={review.score} size="sm" />
          {(isOwner || canDelete) && (
            <div className="flex gap-1">
              {isOwner && onEdit && (
                <button
                  onClick={() => onEdit(review)}
                  className="flex h-6 w-6 items-center justify-center rounded text-muted-foreground hover:text-foreground"
                >
                  <Pencil className="h-3.5 w-3.5" />
                </button>
              )}
              {canDelete && onDelete && (
                <button
                  onClick={() => setConfirmOpen(true)}
                  className="flex h-6 w-6 items-center justify-center rounded text-muted-foreground hover:text-destructive"
                >
                  <Trash2 className="h-3.5 w-3.5" />
                </button>
              )}
            </div>
          )}
        </div>
      </div>
      <p className="text-sm leading-relaxed text-muted-foreground">{review.reviewText}</p>
    </div>
  )
}
