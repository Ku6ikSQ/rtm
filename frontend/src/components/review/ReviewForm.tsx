import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { toast } from 'sonner'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { reviewService } from '@/api'
import { applyServerErrors } from '@/utils/applyServerErrors'
import type { CreateReviewDto } from '@/types/entities'

const schema = z.object({
  score: z.coerce.number().min(1, 'Мин. 1').max(10, 'Макс. 10'),
  reviewText: z.string().min(10, 'Минимум 10 символов').max(5000, 'Максимум 5000 символов'),
})

interface ReviewFormProps {
  albumId: string
}

export function ReviewForm({ albumId }: ReviewFormProps) {
  const queryClient = useQueryClient()

  const {
    register,
    handleSubmit,
    setError,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<CreateReviewDto>({ resolver: zodResolver(schema) })

  const mutation = useMutation({
    mutationFn: (data: CreateReviewDto) => reviewService.create(albumId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['reviews', albumId] })
      queryClient.invalidateQueries({ queryKey: ['album', albumId] })
      reset()
      toast.success('Рецензия опубликована')
    },
    onError: (err) => {
      if (!applyServerErrors(err, setError)) {
        toast.error(err instanceof Error ? err.message : 'Ошибка')
      }
    },
  })

  return (
    <form
      onSubmit={handleSubmit((data) => mutation.mutate(data))}
      className="rounded border border-dashed border-border p-4"
    >
      <p className="mb-3 text-sm font-medium">Написать рецензию</p>
      <div className="mb-3 flex items-center gap-3">
        <label className="text-sm text-muted-foreground">Оценка</label>
        <select
          {...register('score')}
          className="h-8 rounded border border-border bg-transparent px-2 text-sm focus:border-foreground focus:outline-none"
        >
          {Array.from({ length: 10 }, (_, i) => 10 - i).map((n) => (
            <option key={n} value={n}>
              {n}
            </option>
          ))}
        </select>
        {errors.score && <p className="text-xs text-destructive">{errors.score.message}</p>}
      </div>
      <textarea
        {...register('reviewText')}
        rows={4}
        placeholder="Ваши впечатления от альбома…"
        className="w-full resize-y rounded border border-border bg-transparent px-3 py-2 text-sm placeholder:text-muted-foreground focus:border-foreground focus:outline-none"
      />
      {errors.reviewText && (
        <p className="mt-1 text-xs text-destructive">{errors.reviewText.message}</p>
      )}
      <button
        type="submit"
        disabled={isSubmitting || mutation.isPending}
        className="mt-3 h-8 rounded bg-foreground px-4 text-sm font-medium text-background transition-opacity disabled:opacity-50"
      >
        Опубликовать
      </button>
    </form>
  )
}
