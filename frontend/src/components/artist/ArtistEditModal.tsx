import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { X } from 'lucide-react'
import { artistService } from '@/api'
import type { Artist } from '@/types/entities'

const schema = z.object({
  stageName: z.string().min(1, 'Обязательное поле'),
  realName: z.string().optional(),
  country: z.string().max(2, 'Код страны: 2 буквы').optional(),
  bio: z.string().max(1000).optional(),
})

type FormValues = z.infer<typeof schema>

const fieldCls =
  'h-9 w-full rounded border border-border bg-transparent px-3 text-sm focus:border-foreground focus:outline-none'

interface Props {
  artist: Artist
  onClose: () => void
}

export function ArtistEditModal({ artist, onClose }: Props) {
  const queryClient = useQueryClient()
  const [photoFile, setPhotoFile] = useState<File | null>(null)
  const [photoPreview, setPhotoPreview] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      stageName: artist.stageName,
      realName: artist.realName ?? '',
      country: artist.country ?? '',
      bio: artist.bio ?? '',
    },
  })

  const mutation = useMutation({
    mutationFn: async (data: FormValues) => {
      await artistService.update(artist.id, {
        stageName: data.stageName,
        realName: data.realName || undefined,
        country: data.country || undefined,
        bio: data.bio || undefined,
      })
      if (photoFile) await artistService.uploadPhoto(artist.id, photoFile)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['artist', artist.id] })
      queryClient.invalidateQueries({ queryKey: ['artists'] })
      toast.success('Артист обновлён')
      onClose()
    },
    onError: (err) => toast.error(err instanceof Error ? err.message : 'Ошибка'),
  })

  const currentPhoto = photoPreview ?? artist.imageUrl ?? null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-background/80 backdrop-blur-sm">
      <div className="w-full max-w-md rounded border border-border bg-background p-6 shadow-lg">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-base font-semibold">Редактировать артиста</h2>
          <button onClick={onClose} className="text-muted-foreground hover:text-foreground">
            <X className="h-4 w-4" />
          </button>
        </div>

        <form onSubmit={handleSubmit((d) => mutation.mutate(d))} className="space-y-3">
          <div>
            <label className="mb-1 block text-sm font-medium">Псевдоним *</label>
            <input {...register('stageName')} className={fieldCls} />
            {errors.stageName && (
              <p className="mt-1 text-xs text-destructive">{errors.stageName.message}</p>
            )}
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium">Настоящее имя</label>
            <input {...register('realName')} className={fieldCls} />
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium">Страна (ISO 2)</label>
            <input {...register('country')} className={fieldCls} placeholder="DE" maxLength={2} />
            {errors.country && (
              <p className="mt-1 text-xs text-destructive">{errors.country.message}</p>
            )}
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium">Биография</label>
            <textarea
              {...register('bio')}
              rows={3}
              className="w-full resize-y rounded border border-border bg-transparent px-3 py-2 text-sm focus:border-foreground focus:outline-none"
            />
          </div>

          <div>
            <label className="mb-2 block text-sm font-medium">Фото</label>
            <div className="flex items-center gap-3">
              <div className="h-16 w-16 flex-shrink-0 overflow-hidden rounded border border-border bg-muted">
                {currentPhoto ? (
                  <img src={currentPhoto} alt="Фото" className="h-full w-full object-cover" />
                ) : (
                  <div className="flex h-full w-full items-center justify-center text-xs text-muted-foreground">
                    Нет
                  </div>
                )}
              </div>
              <input
                type="file"
                accept="image/*"
                onChange={(e) => {
                  const f = e.target.files?.[0] ?? null
                  setPhotoFile(f)
                  setPhotoPreview(f ? URL.createObjectURL(f) : null)
                }}
                className="text-sm text-muted-foreground file:mr-3 file:rounded file:border file:border-border file:bg-transparent file:px-3 file:py-1.5 file:text-sm file:text-foreground hover:file:bg-muted"
              />
            </div>
          </div>

          <div className="flex justify-end gap-2 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="h-8 rounded border border-border px-4 text-sm text-muted-foreground hover:text-foreground"
            >
              Отмена
            </button>
            <button
              type="submit"
              disabled={mutation.isPending}
              className="h-8 rounded bg-foreground px-4 text-sm font-medium text-background disabled:opacity-50"
            >
              {mutation.isPending ? 'Сохраняем…' : 'Сохранить'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
