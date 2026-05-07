import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { reviewService } from '@/api'
import { ReviewCard } from '@/components/album/ReviewCard'
import { ReviewForm } from './ReviewForm'
import { PageSpinner } from '@/components/common/Spinner'
import { EmptyState } from '@/components/common/EmptyState'
import { useAuth } from '@/hooks/useAuth'
import type { Review } from '@/types/entities'

interface ReviewListProps {
  albumId: string
}

export function ReviewList({ albumId }: ReviewListProps) {
  const { user, isAuthenticated, hasRole } = useAuth()
  const canModerate = hasRole(['ADMIN', 'MODERATOR'])
  const queryClient = useQueryClient()

  const { data: reviews = [], isLoading } = useQuery({
    queryKey: ['reviews', albumId],
    queryFn: () => reviewService.getByAlbum(albumId),
  })

  const deleteMutation = useMutation({
    mutationFn: (id: string) => reviewService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['reviews', albumId] })
      queryClient.invalidateQueries({ queryKey: ['album', albumId] })
      toast.success('Рецензия удалена')
    },
    onError: () => toast.error('Не удалось удалить рецензию'),
  })

  const userHasReview = reviews.some((r: Review) => r.authorId === user?.id)

  return (
    <section>
      <h2 className="mb-4 text-lg font-semibold">
        Рецензии {reviews.length > 0 && <span className="text-muted-foreground">({reviews.length})</span>}
      </h2>

      {isAuthenticated && !userHasReview && <ReviewForm albumId={albumId} />}

      {isLoading ? (
        <PageSpinner />
      ) : reviews.length === 0 ? (
        <EmptyState
          title="Рецензий пока нет"
          description="Станьте первым, кто напишет рецензию на этот альбом"
        />
      ) : (
        <div className="mt-4 space-y-3">
          {reviews.map((review: Review) => (
            <ReviewCard
              key={review.id}
              review={review}
              currentUserId={user?.id}
              canModerate={canModerate}
              onDelete={(id) => deleteMutation.mutate(id)}
            />
          ))}
        </div>
      )}
    </section>
  )
}
