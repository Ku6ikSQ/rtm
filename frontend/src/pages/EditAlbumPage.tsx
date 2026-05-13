import { useEffect, useState, useId } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useParams, useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { Plus, Trash2, GripVertical } from 'lucide-react'
import {
  DndContext,
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  type DragEndEvent,
} from '@dnd-kit/core'
import {
  SortableContext,
  sortableKeyboardCoordinates,
  useSortable,
  verticalListSortingStrategy,
  arrayMove,
} from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'
import { albumService, artistService, genreService, platformService } from '@/api'
import { PageSpinner } from '@/components/common/Spinner'
import { LinksEditor, type LinkRow } from '@/components/album/LinksEditor'
import type { UpdateAlbumDto, CreateTrackInput } from '@/types/entities'

const schema = z.object({
  title: z.string().min(1, 'Обязательное поле').max(200),
  releaseYear: z.coerce.number().int().min(1900).max(2100),
  description: z.string().max(2000).optional(),
  primaryArtistId: z.string().min(1, 'Выберите артиста'),
  genreIds: z.array(z.string()).min(1, 'Выберите хотя бы один жанр'),
})

type FormValues = z.infer<typeof schema>

interface TrackRow {
  _id: string
  title: string
  duration: string
}

function secondsToDisplay(s: number): string {
  if (!s) return ''
  const m = Math.floor(s / 60)
  const sec = s % 60
  return `${m}:${String(sec).padStart(2, '0')}`
}

function parseDuration(value: string): number | undefined {
  const trimmed = value.trim()
  if (!trimmed) return undefined
  const mmss = trimmed.match(/^(\d+):([0-5]\d)$/)
  if (mmss) return parseInt(mmss[1]) * 60 + parseInt(mmss[2])
  const secs = parseInt(trimmed)
  return Number.isFinite(secs) ? secs : undefined
}

function SortableTrackRow({
  row,
  index,
  onChange,
  onRemove,
}: {
  row: TrackRow
  index: number
  onChange: (id: string, field: keyof TrackRow, value: string) => void
  onRemove: (id: string) => void
}) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
    id: row._id,
  })

  return (
    <div
      ref={setNodeRef}
      style={{ transform: CSS.Transform.toString(transform), transition }}
      className={`flex items-center gap-2 rounded border border-border p-2 ${isDragging ? 'opacity-50 bg-muted' : 'bg-background'}`}
    >
      <button
        type="button"
        {...attributes}
        {...listeners}
        className="cursor-grab touch-none text-muted-foreground hover:text-foreground active:cursor-grabbing"
        aria-label="Перетащить"
      >
        <GripVertical className="h-4 w-4" />
      </button>
      <span className="w-5 flex-shrink-0 text-center text-xs text-muted-foreground">{index + 1}</span>
      <input
        value={row.title}
        onChange={(e) => onChange(row._id, 'title', e.target.value)}
        placeholder="Название трека"
        className="h-8 min-w-0 flex-1 rounded border border-border bg-transparent px-2 text-sm focus:border-foreground focus:outline-none"
      />
      <input
        value={row.duration}
        onChange={(e) => onChange(row._id, 'duration', e.target.value)}
        placeholder="3:45"
        title="Длительность (мм:сс или секунды)"
        className="h-8 w-20 flex-shrink-0 rounded border border-border bg-transparent px-2 text-center text-sm focus:border-foreground focus:outline-none"
      />
      <button
        type="button"
        onClick={() => onRemove(row._id)}
        className="flex-shrink-0 text-muted-foreground hover:text-destructive"
      >
        <Trash2 className="h-4 w-4" />
      </button>
    </div>
  )
}

export function EditAlbumPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const dndId = useId()

  const [tracks, setTracks] = useState<TrackRow[]>([])
  const [links, setLinks] = useState<LinkRow[]>([])
  const [tracksInitialized, setTracksInitialized] = useState(false)
  const [coverFile, setCoverFile] = useState<File | null>(null)
  const [coverPreview, setCoverPreview] = useState<string | null>(null)

  const { data: album, isLoading: loadingAlbum } = useQuery({
    queryKey: ['album', id],
    queryFn: () => albumService.getById(id!),
    enabled: !!id,
  })

  const { data: artists = [], isLoading: loadingArtists } = useQuery({
    queryKey: ['artists'],
    queryFn: () => artistService.getAll(),
  })

  const { data: genres = [], isLoading: loadingGenres } = useQuery({
    queryKey: ['genres'],
    queryFn: () => genreService.getAll(),
  })

  const { data: platforms = [], isLoading: loadingPlatforms } = useQuery({
    queryKey: ['platforms'],
    queryFn: () => platformService.getAll(),
  })

  const {
    register,
    handleSubmit,
    reset,
    watch,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({ resolver: zodResolver(schema) })

  const selectedGenreIds = watch('genreIds') ?? []

  useEffect(() => {
    if (album && !tracksInitialized) {
      reset({
        title: album.title,
        releaseYear: album.releaseYear,
        description: album.description ?? '',
        primaryArtistId: album.artists[0]?.artistId ?? '',
        genreIds: album.genres.map((g) => g.id),
      })
      setTracks(
        album.tracks.map((t) => ({
          _id: `t-${t.id}`,
          title: t.title,
          duration: secondsToDisplay(t.durationSeconds),
        })),
      )
      setLinks(
        album.links.map((l) => ({
          _id: `l-${l.platformId}`,
          platformId: l.platformId,
          url: l.url,
        })),
      )
      setTracksInitialized(true)
    }
  }, [album, reset, tracksInitialized])

  const mutation = useMutation({
    mutationFn: async (data: FormValues) => {
      const trackInputs: CreateTrackInput[] = tracks
        .filter((t) => t.title.trim())
        .map((t) => ({
          title: t.title.trim(),
          durationSeconds: parseDuration(t.duration),
        }))

      const dto: UpdateAlbumDto = {
        title: data.title,
        releaseYear: data.releaseYear,
        description: data.description,
        artistIds: [data.primaryArtistId],
        genreIds: data.genreIds,
        tracks: trackInputs,
        links: links.filter((l) => l.platformId && l.url.trim()).map((l) => ({ platformId: l.platformId, url: l.url.trim() })),
      }
      await albumService.update(id!, dto)
      if (coverFile) await albumService.uploadCover(id!, coverFile)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['album', id] })
      queryClient.invalidateQueries({ queryKey: ['albums'] })
      toast.success('Альбом обновлён')
      navigate(`/album/${id}`)
    },
    onError: (err) => toast.error(err instanceof Error ? err.message : 'Ошибка'),
  })

  function toggleGenre(gid: string) {
    const current = selectedGenreIds
    const next = current.includes(gid) ? current.filter((x) => x !== gid) : [...current, gid]
    setValue('genreIds', next, { shouldValidate: true })
  }

  function addTrack() {
    setTracks((prev) => [...prev, { _id: `t-${Date.now()}`, title: '', duration: '' }])
  }

  function updateTrack(rowId: string, field: keyof TrackRow, value: string) {
    setTracks((prev) => prev.map((t) => (t._id === rowId ? { ...t, [field]: value } : t)))
  }

  function removeTrack(rowId: string) {
    setTracks((prev) => prev.filter((t) => t._id !== rowId))
  }

  const sensors = useSensors(
    useSensor(PointerSensor),
    useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates }),
  )

  function handleDragEnd(event: DragEndEvent) {
    const { active, over } = event
    if (over && active.id !== over.id) {
      setTracks((prev) => {
        const oldIndex = prev.findIndex((t) => t._id === active.id)
        const newIndex = prev.findIndex((t) => t._id === over.id)
        return arrayMove(prev, oldIndex, newIndex)
      })
    }
  }

  if (loadingAlbum || loadingArtists || loadingGenres || loadingPlatforms) return <PageSpinner />
  if (!album) return <p className="py-8 text-center text-muted-foreground">Альбом не найден</p>

  const rootGenres = genres.filter((g) => !g.parentId)

  return (
    <div className="mx-auto max-w-lg">
      <h1 className="mb-6 text-xl font-bold">Редактировать «{album.title}»</h1>

      <form onSubmit={handleSubmit((d) => mutation.mutate(d))} className="space-y-5">
        {/* Cover */}
        <div>
          <label className="mb-2 block text-sm font-medium">Обложка</label>
          <div className="flex items-center gap-3">
            <div className="h-24 w-24 flex-shrink-0 overflow-hidden rounded border border-border bg-muted">
              {(coverPreview ?? album.coverUrl) ? (
                <img src={coverPreview ?? album.coverUrl} alt="Обложка" className="h-full w-full object-cover" />
              ) : (
                <div className="flex h-full w-full items-center justify-center text-xs text-muted-foreground">Нет</div>
              )}
            </div>
            <input
              type="file"
              accept="image/*"
              onChange={(e) => {
                const f = e.target.files?.[0] ?? null
                setCoverFile(f)
                setCoverPreview(f ? URL.createObjectURL(f) : null)
              }}
              className="text-sm text-muted-foreground file:mr-3 file:rounded file:border file:border-border file:bg-transparent file:px-3 file:py-1.5 file:text-sm file:text-foreground hover:file:bg-muted"
            />
          </div>
        </div>

        {/* Title */}
        <div>
          <label className="mb-1 block text-sm font-medium">Название *</label>
          <input
            {...register('title')}
            className="h-9 w-full rounded border border-border bg-transparent px-3 text-sm focus:border-foreground focus:outline-none"
          />
          {errors.title && <p className="mt-1 text-xs text-destructive">{errors.title.message}</p>}
        </div>

        {/* Year */}
        <div>
          <label className="mb-1 block text-sm font-medium">Год выпуска *</label>
          <input
            {...register('releaseYear')}
            type="number"
            min={1900}
            max={2030}
            className="h-9 w-40 rounded border border-border bg-transparent px-3 text-sm focus:border-foreground focus:outline-none"
          />
          {errors.releaseYear && (
            <p className="mt-1 text-xs text-destructive">{errors.releaseYear.message}</p>
          )}
        </div>

        {/* Artist */}
        <div>
          <label className="mb-1 block text-sm font-medium">Артист *</label>
          <select
            {...register('primaryArtistId')}
            className="h-9 w-full rounded border border-border bg-background px-2 text-sm focus:border-foreground focus:outline-none"
          >
            <option value="">— выберите артиста —</option>
            {artists.map((a) => (
              <option key={a.id} value={a.id}>
                {a.stageName} {a.country ? `(${a.country})` : ''}
              </option>
            ))}
          </select>
          {errors.primaryArtistId && (
            <p className="mt-1 text-xs text-destructive">{errors.primaryArtistId.message}</p>
          )}
        </div>

        {/* Genres */}
        <div>
          <label className="mb-2 block text-sm font-medium">
            Жанры * {selectedGenreIds.length > 0 && `(выбрано: ${selectedGenreIds.length})`}
          </label>
          <div className="max-h-48 overflow-y-auto rounded border border-border p-3">
            {rootGenres.map((root) => {
              const children = genres.filter((g) => g.parentId === root.id)
              return (
                <div key={root.id} className="mb-2">
                  <label className="flex cursor-pointer items-center gap-2 text-sm font-medium">
                    <input
                      type="checkbox"
                      checked={selectedGenreIds.includes(root.id)}
                      onChange={() => toggleGenre(root.id)}
                      className="rounded"
                    />
                    {root.name}
                  </label>
                  {children.map((child) => (
                    <label
                      key={child.id}
                      className="ml-4 flex cursor-pointer items-center gap-2 py-0.5 text-sm text-muted-foreground"
                    >
                      <input
                        type="checkbox"
                        checked={selectedGenreIds.includes(child.id)}
                        onChange={() => toggleGenre(child.id)}
                        className="rounded"
                      />
                      {child.name}
                    </label>
                  ))}
                </div>
              )
            })}
          </div>
          {errors.genreIds && (
            <p className="mt-1 text-xs text-destructive">{errors.genreIds.message}</p>
          )}
        </div>

        {/* Description */}
        <div>
          <label className="mb-1 block text-sm font-medium">Описание</label>
          <textarea
            {...register('description')}
            rows={3}
            className="w-full resize-y rounded border border-border bg-transparent px-3 py-2 text-sm placeholder:text-muted-foreground focus:border-foreground focus:outline-none"
          />
          {errors.description && (
            <p className="mt-1 text-xs text-destructive">{errors.description.message}</p>
          )}
        </div>

        {/* Tracks */}
        <div>
          <div className="mb-2 flex items-center justify-between">
            <label className="text-sm font-medium">
              Треклист {tracks.length > 0 && <span className="text-muted-foreground">({tracks.length})</span>}
            </label>
            <button
              type="button"
              onClick={addTrack}
              className="flex items-center gap-1 text-xs text-muted-foreground hover:text-foreground"
            >
              <Plus className="h-3.5 w-3.5" />
              Добавить трек
            </button>
          </div>

          {tracks.length > 0 ? (
            <DndContext
              id={dndId}
              sensors={sensors}
              collisionDetection={closestCenter}
              onDragEnd={handleDragEnd}
            >
              <SortableContext items={tracks.map((t) => t._id)} strategy={verticalListSortingStrategy}>
                <div className="space-y-1.5">
                  {tracks.map((row, i) => (
                    <SortableTrackRow
                      key={row._id}
                      row={row}
                      index={i}
                      onChange={updateTrack}
                      onRemove={removeTrack}
                    />
                  ))}
                </div>
              </SortableContext>
            </DndContext>
          ) : (
            <p className="rounded border border-dashed border-border py-4 text-center text-xs text-muted-foreground">
              Треки не добавлены — нажмите «Добавить трек»
            </p>
          )}
          <p className="mt-1 text-xs text-muted-foreground">
            Длительность в формате <span className="font-mono">3:45</span> или секундами.
          </p>
        </div>

        {/* Links */}
        <LinksEditor links={links} platforms={platforms} onChange={setLinks} />

        <div className="flex gap-3 pt-1">
          <button
            type="submit"
            disabled={isSubmitting || mutation.isPending}
            className="h-9 rounded bg-foreground px-6 text-sm font-medium text-background disabled:opacity-50"
          >
            {mutation.isPending ? 'Сохраняем…' : 'Сохранить'}
          </button>
          <button
            type="button"
            onClick={() => navigate(`/album/${id}`)}
            className="h-9 rounded border border-border px-4 text-sm text-muted-foreground hover:text-foreground"
          >
            Отмена
          </button>
        </div>
      </form>
    </div>
  )
}
