import { useState, useCallback } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { toast } from 'sonner'
import { Trash2 } from 'lucide-react'
import { useAuth } from '@/hooks/useAuth'
import { userService, reviewService } from '@/api'
import { applyServerErrors } from '@/utils/applyServerErrors'
import { RatingBadge } from '@/components/common/RatingBadge'
import { PageSpinner } from '@/components/common/Spinner'
import { EmptyState } from '@/components/common/EmptyState'
import { ConfirmDialog } from '@/components/common/ConfirmDialog'
import { formatDateShort } from '@/utils/formatters'
import { cn } from '@/utils/cn'
import type { UpdateProfileDto, ChangePasswordDto } from '@/types/entities'

const profileSchema = z.object({
  username: z.string().min(3, 'Минимум 3 символа').max(50, 'Максимум 50 символов'),
})

const passwordSchema = z
  .object({
    currentPassword: z.string().min(1, 'Обязательное поле'),
    newPassword: z.string().min(8, 'Минимум 8 символов'),
    newPasswordConfirm: z.string().min(1, 'Обязательное поле'),
  })
  .refine((d) => d.newPassword === d.newPasswordConfirm, {
    path: ['newPasswordConfirm'],
    message: 'Пароли не совпадают',
  })

type Tab = 'profile' | 'reviews' | 'security'

export function ProfilePage() {
  const { user } = useAuth()
  const [tab, setTab] = useState<Tab>('profile')
  const queryClient = useQueryClient()

  const profileForm = useForm<UpdateProfileDto>({
    resolver: zodResolver(profileSchema),
    defaultValues: { username: user?.username ?? '' },
  })

  const passwordForm = useForm<ChangePasswordDto>({ resolver: zodResolver(passwordSchema) })

  const updateProfile = useMutation({
    mutationFn: (dto: UpdateProfileDto) => userService.updateProfile(user!.id, dto),
    onSuccess: (updated) => {
      queryClient.setQueryData(['me'], updated)
      toast.success('Профиль обновлён')
    },
    onError: (err) => {
      if (!applyServerErrors(err, profileForm.setError)) {
        toast.error(err instanceof Error ? err.message : 'Ошибка')
      }
    },
  })

  if (!user) return <PageSpinner />

  const TABS: { id: Tab; label: string }[] = [
    { id: 'profile', label: 'Профиль' },
    { id: 'reviews', label: 'Мои рецензии' },
    { id: 'security', label: 'Безопасность' },
  ]

  return (
    <div className="mx-auto max-w-2xl">
      <h1 className="mb-6 text-xl font-bold">Личный кабинет</h1>

      <div className="mb-6 flex border-b border-border">
        {TABS.map(({ id, label }) => (
          <button
            key={id}
            onClick={() => setTab(id)}
            className={cn(
              'px-4 py-2 text-sm transition-colors',
              tab === id
                ? 'border-b-2 border-foreground font-medium text-foreground'
                : 'text-muted-foreground hover:text-foreground',
            )}
          >
            {label}
          </button>
        ))}
      </div>

      {tab === 'profile' && (
        <ProfileTab user={user} form={profileForm} mutation={updateProfile} />
      )}
      {tab === 'reviews' && <ReviewsTab userId={user.id} />}
      {tab === 'security' && <SecurityTab form={passwordForm} />}
    </div>
  )
}

function ProfileTab({
  user,
  form,
  mutation,
}: {
  user: NonNullable<ReturnType<typeof useAuth>['user']>
  form: ReturnType<typeof useForm<UpdateProfileDto>>
  mutation: ReturnType<typeof useMutation<unknown, unknown, UpdateProfileDto>>
}) {
  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <div className="flex h-20 w-20 flex-shrink-0 items-center justify-center rounded-full border border-border bg-muted text-2xl font-bold">
          {user.username[0]?.toUpperCase()}
        </div>
        <div>
          <p className="font-medium">{user.username}</p>
          <p className="text-sm text-muted-foreground">{user.email}</p>
          <span className="mt-1 inline-block rounded border border-border px-2 py-0.5 text-xs text-muted-foreground">
            {user.role}
          </span>
        </div>
      </div>

      <form
        onSubmit={form.handleSubmit((data) =>
          (mutation as ReturnType<typeof useMutation<unknown, unknown, UpdateProfileDto>>).mutate(data),
        )}
        className="space-y-4"
      >
        <div>
          <label className="mb-1 block text-sm font-medium">Username</label>
          <input
            {...form.register('username')}
            className="h-9 w-full rounded border border-border bg-transparent px-3 text-sm focus:border-foreground focus:outline-none"
          />
          {form.formState.errors.username && (
            <p className="mt-1 text-xs text-destructive">{form.formState.errors.username.message}</p>
          )}
        </div>
        <button
          type="submit"
          disabled={(mutation as { isPending: boolean }).isPending}
          className="h-9 rounded bg-foreground px-5 text-sm font-medium text-background disabled:opacity-50"
        >
          Сохранить
        </button>
      </form>
    </div>
  )
}

function ReviewsTab({ userId }: { userId: string }) {
  const queryClient = useQueryClient()
  const [pendingDeleteId, setPendingDeleteId] = useState<string | null>(null)

  const { data: reviews = [], isLoading } = useQuery({
    queryKey: ['reviews', 'user', userId],
    queryFn: () => reviewService.getByUser(userId),
  })

  const deleteMutation = useMutation({
    mutationFn: (id: string) => reviewService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['reviews', 'user', userId] })
      toast.success('Рецензия удалена')
    },
    onError: () => toast.error('Не удалось удалить рецензию'),
  })

  const handleConfirm = useCallback(() => {
    if (pendingDeleteId) deleteMutation.mutate(pendingDeleteId)
    setPendingDeleteId(null)
  }, [pendingDeleteId, deleteMutation])

  if (isLoading) return <PageSpinner />

  if (reviews.length === 0) {
    return (
      <EmptyState
        title="У вас пока нет рецензий"
        description="Перейдите на страницу любого альбома и напишите первую рецензию"
      />
    )
  }

  return (
    <>
      <ConfirmDialog
        open={!!pendingDeleteId}
        title="Удалить рецензию?"
        description="Это действие нельзя отменить."
        confirmLabel="Удалить"
        onConfirm={handleConfirm}
        onCancel={() => setPendingDeleteId(null)}
      />
      <div className="space-y-3">
        {reviews.map((review) => (
          <div key={review.id} className="rounded border border-border p-4">
            <div className="flex items-start justify-between gap-3">
              <div className="min-w-0 flex-1">
                <Link
                  to={`/album/${review.albumId}`}
                  className="text-sm font-medium hover:underline"
                >
                  Альбом #{review.albumId}
                </Link>
                <p className="mt-0.5 text-xs text-muted-foreground">{formatDateShort(review.createdAt)}</p>
              </div>
              <div className="flex items-center gap-2">
                <RatingBadge rating={review.score} size="sm" />
                <button
                  onClick={() => setPendingDeleteId(review.id)}
                  className="flex h-6 w-6 items-center justify-center rounded text-muted-foreground hover:text-destructive"
                >
                  <Trash2 className="h-3.5 w-3.5" />
                </button>
              </div>
            </div>
            <p className="mt-2 text-sm leading-relaxed text-muted-foreground">{review.reviewText}</p>
          </div>
        ))}
      </div>
    </>
  )
}

function SecurityTab({ form }: { form: ReturnType<typeof useForm<ChangePasswordDto>> }) {
  return (
    <form
      className="space-y-4"
      onSubmit={form.handleSubmit(() => toast.info('Смена пароля будет доступна после подключения backend'))}
    >
      {(
        [
          { name: 'currentPassword' as const, label: 'Текущий пароль' },
          { name: 'newPassword' as const, label: 'Новый пароль' },
          { name: 'newPasswordConfirm' as const, label: 'Повторите новый пароль' },
        ] as const
      ).map(({ name, label }) => (
        <div key={name}>
          <label className="mb-1 block text-sm font-medium">{label}</label>
          <input
            {...form.register(name)}
            type="password"
            className="h-9 w-full rounded border border-border bg-transparent px-3 text-sm focus:border-foreground focus:outline-none"
          />
          {form.formState.errors[name] && (
            <p className="mt-1 text-xs text-destructive">{form.formState.errors[name]?.message}</p>
          )}
        </div>
      ))}
      <button
        type="submit"
        className="h-9 rounded bg-foreground px-5 text-sm font-medium text-background"
      >
        Изменить пароль
      </button>
    </form>
  )
}
