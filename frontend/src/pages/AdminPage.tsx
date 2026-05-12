import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { toast } from 'sonner'
import { Plus, X } from 'lucide-react'
import { userService, genreService, platformService, artistService, albumService } from '@/api'
import { PageSpinner } from '@/components/common/Spinner'
import { cn } from '@/utils/cn'
import { formatDateShort } from '@/utils/formatters'
import { useAuth } from '@/hooks/useAuth'
import { ConfirmDialog } from '@/components/common/ConfirmDialog'
import type { UserRole, CreateGenreDto, CreatePlatformDto, CreateArtistDto } from '@/types/entities'

type Tab = 'users' | 'genres' | 'platforms' | 'artists' | 'stats'

export function AdminPage() {
  const { hasRole } = useAuth()
  const isAdmin = hasRole(['ADMIN'])
  const [tab, setTab] = useState<Tab>(() => {
    const saved = localStorage.getItem('admin-tab') as Tab | null
    const allowed: Tab[] = isAdmin
      ? ['users', 'genres', 'platforms', 'artists', 'stats']
      : ['genres', 'platforms', 'artists', 'stats']
    return saved && allowed.includes(saved) ? saved : isAdmin ? 'users' : 'genres'
  })

  const ALL_TABS: { id: Tab; label: string; adminOnly?: boolean }[] = [
    { id: 'users', label: 'Пользователи', adminOnly: true },
    { id: 'genres', label: 'Жанры' },
    { id: 'platforms', label: 'Платформы' },
    { id: 'artists', label: 'Артисты' },
    { id: 'stats', label: 'Статистика' },
  ]

  const tabs = ALL_TABS.filter((t) => !t.adminOnly || isAdmin)

  return (
    <div>
      <h1 className="mb-6 text-xl font-bold">Панель управления</h1>

      <div className="mb-6 flex overflow-x-auto border-b border-border">
        {tabs.map(({ id, label }) => (
          <button
            key={id}
            onClick={() => { setTab(id); localStorage.setItem('admin-tab', id) }}
            className={cn(
              'flex-shrink-0 px-4 py-2 text-sm transition-colors',
              tab === id
                ? 'border-b-2 border-foreground font-medium text-foreground'
                : 'text-muted-foreground hover:text-foreground',
            )}
          >
            {label}
          </button>
        ))}
      </div>

      {tab === 'users' && isAdmin && <UsersTab />}
      {tab === 'genres' && <GenresTab />}
      {tab === 'platforms' && <PlatformsTab />}
      {tab === 'artists' && <ArtistsTab />}
      {tab === 'stats' && <StatsTab />}
    </div>
  )
}

/* ─── Users ──────────────────────────────────────────────────────────── */

function UsersTab() {
  const queryClient = useQueryClient()
  const [search, setSearch] = useState('')
  const [roleFilter, setRoleFilter] = useState<UserRole | ''>('')

  const { data: users = [], isLoading } = useQuery({
    queryKey: ['admin-users'],
    queryFn: () => userService.getAll(),
  })

  const blockMutation = useMutation({
    mutationFn: ({ id, active }: { id: string; active: boolean }) =>
      active ? userService.blockUser(id) : userService.unblockUser(id),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['admin-users'] }); toast.success('Статус изменён') },
  })
  const roleMutation = useMutation({
    mutationFn: ({ id, role }: { id: string; role: UserRole }) => userService.changeRole(id, role),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['admin-users'] }); toast.success('Роль изменена') },
  })

  if (isLoading) return <PageSpinner />

  const filtered = users.filter((u) => {
    const matchSearch = !search || u.username.includes(search) || u.email.includes(search)
    const matchRole = !roleFilter || u.role === roleFilter
    return matchSearch && matchRole
  })

  return (
    <div>
      <div className="mb-4 flex flex-wrap gap-2">
        <input
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Поиск…"
          className="h-8 rounded border border-border bg-transparent px-3 text-sm focus:border-foreground focus:outline-none"
        />
        <select
          value={roleFilter}
          onChange={(e) => setRoleFilter(e.target.value as UserRole | '')}
          className="h-8 rounded border border-border bg-background px-2 text-sm focus:outline-none"
        >
          <option value="">Все роли</option>
          <option value="USER">USER</option>
          <option value="MODERATOR">MODERATOR</option>
          <option value="ADMIN">ADMIN</option>
        </select>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-border text-left text-xs uppercase tracking-wider text-muted-foreground">
              <th className="pb-2 pr-4 font-medium">Пользователь</th>
              <th className="pb-2 pr-4 font-medium">Роль</th>
              <th className="hidden pb-2 pr-4 font-medium sm:table-cell">Дата</th>
              <th className="pb-2 pr-4 font-medium">Статус</th>
              <th className="pb-2 font-medium">Действия</th>
            </tr>
          </thead>
          <tbody>
            {filtered.map((u) => (
              <tr key={u.id} className="border-b border-border last:border-0">
                <td className="py-2 pr-4">
                  <p className="font-medium">{u.username}</p>
                  <p className="text-xs text-muted-foreground">{u.email}</p>
                </td>
                <td className="py-2 pr-4">
                  <select
                    value={u.role}
                    onChange={(e) => roleMutation.mutate({ id: u.id, role: e.target.value as UserRole })}
                    className="h-7 rounded border border-border bg-background px-1.5 text-xs focus:outline-none"
                  >
                    <option value="USER">USER</option>
                    <option value="MODERATOR">MODERATOR</option>
                    <option value="ADMIN">ADMIN</option>
                  </select>
                </td>
                <td className="hidden py-2 pr-4 text-muted-foreground sm:table-cell">
                  {formatDateShort(u.createdAt)}
                </td>
                <td className="py-2 pr-4">
                  <span className={cn('text-xs', u.isActive ? 'text-foreground' : 'text-muted-foreground line-through')}>
                    {u.isActive ? 'Активен' : 'Заблокирован'}
                  </span>
                </td>
                <td className="py-2">
                  <button
                    onClick={() => blockMutation.mutate({ id: u.id, active: u.isActive })}
                    className="text-xs text-muted-foreground hover:text-foreground"
                  >
                    {u.isActive ? 'Заблокировать' : 'Разблокировать'}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

/* ─── Modal helper ───────────────────────────────────────────────────── */

function Modal({ title, onClose, children }: { title: string; onClose: () => void; children: React.ReactNode }) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-background/80 backdrop-blur-sm">
      <div className="w-full max-w-md rounded border border-border bg-background p-6 shadow-lg">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-base font-semibold">{title}</h2>
          <button onClick={onClose} className="text-muted-foreground hover:text-foreground">
            <X className="h-4 w-4" />
          </button>
        </div>
        {children}
      </div>
    </div>
  )
}

/* ─── Genres ─────────────────────────────────────────────────────────── */

const genreSchema = z.object({
  name: z.string().min(1, 'Обязательное поле'),
  slug: z.string().min(1, 'Обязательное поле').regex(/^[a-z0-9-]+$/, 'Только a-z, 0-9, дефис'),
  parentId: z.string().optional(),
  description: z.string().optional(),
})

function GenresTab() {
  const queryClient = useQueryClient()
  const [showCreate, setShowCreate] = useState(false)
  const [pendingDelete, setPendingDelete] = useState<string | null>(null)

  const { data: genres = [], isLoading } = useQuery({
    queryKey: ['genres'],
    queryFn: () => genreService.getAll(),
  })

  const deleteMutation = useMutation({
    mutationFn: (id: string) => genreService.delete(id),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['genres'] }); toast.success('Жанр удалён') },
    onError: () => toast.error('Ошибка'),
  })

  const form = useForm<CreateGenreDto>({ resolver: zodResolver(genreSchema) })

  const createMutation = useMutation({
    mutationFn: (dto: CreateGenreDto) => genreService.create(dto),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['genres'] })
      toast.success('Жанр добавлен')
      form.reset()
      setShowCreate(false)
    },
    onError: (err) => toast.error(err instanceof Error ? err.message : 'Ошибка'),
  })

  const roots = genres.filter((g) => !g.parentId)

  if (isLoading) return <PageSpinner />

  return (
    <div>
      <ConfirmDialog
        open={!!pendingDelete}
        title="Удалить жанр?"
        description="Жанр будет удалён. Альбомы в этом жанре не затронуты."
        confirmLabel="Удалить"
        onConfirm={() => { if (pendingDelete) deleteMutation.mutate(pendingDelete); setPendingDelete(null) }}
        onCancel={() => setPendingDelete(null)}
      />
      <div className="mb-4">
        <button
          onClick={() => setShowCreate(true)}
          className="flex h-8 items-center gap-1.5 rounded border border-border px-3 text-sm text-muted-foreground hover:text-foreground"
        >
          <Plus className="h-3.5 w-3.5" /> Новый жанр
        </button>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-border text-left text-xs uppercase tracking-wider text-muted-foreground">
              <th className="pb-2 pr-4 font-medium">Название</th>
              <th className="hidden pb-2 pr-4 font-medium sm:table-cell">Slug</th>
              <th className="hidden pb-2 pr-4 font-medium sm:table-cell">Альбомов</th>
              <th className="pb-2 font-medium">Действия</th>
            </tr>
          </thead>
          <tbody>
            {genres.map((g) => (
              <tr key={g.id} className="border-b border-border last:border-0">
                <td className="py-2 pr-4">
                  {g.parentId && <span className="mr-1 text-muted-foreground">↳</span>}
                  {g.name}
                </td>
                <td className="hidden py-2 pr-4 text-muted-foreground sm:table-cell">{g.slug}</td>
                <td className="hidden py-2 pr-4 text-muted-foreground sm:table-cell">{g.albumCount ?? 0}</td>
                <td className="py-2">
                  <button
                    onClick={() => setPendingDelete(g.id)}
                    className="text-xs text-muted-foreground hover:text-destructive"
                  >
                    Удалить
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {showCreate && (
        <Modal title="Новый жанр" onClose={() => setShowCreate(false)}>
          <form onSubmit={form.handleSubmit((d) => createMutation.mutate(d))} className="space-y-3">
            <FormField label="Название" error={form.formState.errors.name?.message}>
              <input {...form.register('name')} className={fieldCls} />
            </FormField>
            <FormField label="Slug" error={form.formState.errors.slug?.message}>
              <input {...form.register('slug')} className={fieldCls} placeholder="post-rock" />
            </FormField>
            <FormField label="Родительский жанр">
              <select {...form.register('parentId')} className={fieldCls + ' bg-background'}>
                <option value="">— нет —</option>
                {roots.map((g) => <option key={g.id} value={g.id}>{g.name}</option>)}
              </select>
            </FormField>
            <ModalActions onCancel={() => setShowCreate(false)} loading={createMutation.isPending} />
          </form>
        </Modal>
      )}
    </div>
  )
}

/* ─── Platforms ──────────────────────────────────────────────────────── */

const platformSchema = z.object({
  name: z.string().min(1, 'Обязательное поле'),
})
type PlatformFormValues = z.infer<typeof platformSchema>

function PlatformsTab() {
  const queryClient = useQueryClient()
  const [showCreate, setShowCreate] = useState(false)
  const [pendingDelete, setPendingDelete] = useState<string | null>(null)
  const [logoFile, setLogoFile] = useState<File | null>(null)

  const { data: platforms = [], isLoading } = useQuery({
    queryKey: ['platforms'],
    queryFn: () => platformService.getAll(),
  })

  const deleteMutation = useMutation({
    mutationFn: (id: string) => platformService.delete(id),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['platforms'] }); toast.success('Платформа удалена') },
  })

  const form = useForm<PlatformFormValues>({ resolver: zodResolver(platformSchema) })

  const createMutation = useMutation({
    mutationFn: (dto: CreatePlatformDto) => platformService.create(dto),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['platforms'] })
      toast.success('Платформа добавлена')
      form.reset()
      setLogoFile(null)
      setShowCreate(false)
    },
    onError: (err) => toast.error(err instanceof Error ? err.message : 'Ошибка'),
  })

  if (isLoading) return <PageSpinner />

  return (
    <div>
      <ConfirmDialog
        open={!!pendingDelete}
        title="Удалить платформу?"
        description="Платформа будет удалена."
        confirmLabel="Удалить"
        onConfirm={() => { if (pendingDelete) deleteMutation.mutate(pendingDelete); setPendingDelete(null) }}
        onCancel={() => setPendingDelete(null)}
      />
      <div className="mb-4">
        <button
          onClick={() => setShowCreate(true)}
          className="flex h-8 items-center gap-1.5 rounded border border-border px-3 text-sm text-muted-foreground hover:text-foreground"
        >
          <Plus className="h-3.5 w-3.5" /> Новая платформа
        </button>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-border text-left text-xs uppercase tracking-wider text-muted-foreground">
              <th className="pb-2 pr-4 font-medium">Платформа</th>
              <th className="pb-2 font-medium">Действия</th>
            </tr>
          </thead>
          <tbody>
            {platforms.map((p) => (
              <tr key={p.id} className="border-b border-border last:border-0">
                <td className="py-2 pr-4">{p.name}</td>
                <td className="py-2">
                  <button onClick={() => setPendingDelete(p.id)} className="text-xs text-muted-foreground hover:text-destructive">
                    Удалить
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {showCreate && (
        <Modal title="Новая платформа" onClose={() => setShowCreate(false)}>
          <form onSubmit={form.handleSubmit((d) => createMutation.mutate({ name: d.name, logoFile: logoFile ?? undefined }))} className="space-y-3">
            <FormField label="Название" error={form.formState.errors.name?.message}>
              <input {...form.register('name')} className={fieldCls} />
            </FormField>
            <FormField label="Логотип">
              <input
                type="file"
                accept="image/*"
                onChange={(e) => setLogoFile(e.target.files?.[0] ?? null)}
                className="w-full text-sm text-muted-foreground file:mr-3 file:rounded file:border file:border-border file:bg-transparent file:px-3 file:py-1.5 file:text-sm file:text-foreground hover:file:bg-muted"
              />
            </FormField>
            <ModalActions onCancel={() => setShowCreate(false)} loading={createMutation.isPending} />
          </form>
        </Modal>
      )}
    </div>
  )
}

/* ─── Artists ────────────────────────────────────────────────────────── */

const artistSchema = z.object({
  stageName: z.string().min(1, 'Обязательное поле'),
  realName: z.string().optional(),
  country: z.string().max(2, 'Код страны: 2 буквы').optional(),
  bio: z.string().max(1000).optional(),
})

function ArtistsTab() {
  const queryClient = useQueryClient()
  const [showCreate, setShowCreate] = useState(false)
  const [pendingDelete, setPendingDelete] = useState<string | null>(null)

  const { data: artists = [], isLoading } = useQuery({
    queryKey: ['artists'],
    queryFn: () => artistService.getAll(),
  })

  const deleteMutation = useMutation({
    mutationFn: (id: string) => artistService.delete(id),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['artists'] }); toast.success('Артист удалён') },
  })

  const form = useForm<CreateArtistDto>({ resolver: zodResolver(artistSchema) })

  const createMutation = useMutation({
    mutationFn: (dto: CreateArtistDto) => artistService.create(dto),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['artists'] })
      toast.success('Артист добавлен')
      form.reset()
      setShowCreate(false)
    },
    onError: (err) => toast.error(err instanceof Error ? err.message : 'Ошибка'),
  })

  if (isLoading) return <PageSpinner />

  return (
    <div>
      <ConfirmDialog
        open={!!pendingDelete}
        title="Удалить артиста?"
        description="Артист будет удалён. Альбомы с этим артистом не затронуты."
        confirmLabel="Удалить"
        onConfirm={() => { if (pendingDelete) deleteMutation.mutate(pendingDelete); setPendingDelete(null) }}
        onCancel={() => setPendingDelete(null)}
      />
      <div className="mb-4">
        <button
          onClick={() => setShowCreate(true)}
          className="flex h-8 items-center gap-1.5 rounded border border-border px-3 text-sm text-muted-foreground hover:text-foreground"
        >
          <Plus className="h-3.5 w-3.5" /> Новый артист
        </button>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-border text-left text-xs uppercase tracking-wider text-muted-foreground">
              <th className="pb-2 pr-4 font-medium">Артист</th>
              <th className="hidden pb-2 pr-4 font-medium sm:table-cell">Страна</th>
              <th className="pb-2 font-medium">Действия</th>
            </tr>
          </thead>
          <tbody>
            {artists.map((a) => (
              <tr key={a.id} className="border-b border-border last:border-0">
                <td className="py-2 pr-4">
                  <p className="font-medium">{a.stageName}</p>
                  {a.realName && <p className="text-xs text-muted-foreground">{a.realName}</p>}
                </td>
                <td className="hidden py-2 pr-4 text-muted-foreground sm:table-cell">{a.country ?? '—'}</td>
                <td className="py-2">
                  <button onClick={() => setPendingDelete(a.id)} className="text-xs text-muted-foreground hover:text-destructive">
                    Удалить
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {showCreate && (
        <Modal title="Новый артист" onClose={() => setShowCreate(false)}>
          <form onSubmit={form.handleSubmit((d) => createMutation.mutate(d))} className="space-y-3">
            <FormField label="Псевдоним *" error={form.formState.errors.stageName?.message}>
              <input {...form.register('stageName')} className={fieldCls} />
            </FormField>
            <FormField label="Настоящее имя">
              <input {...form.register('realName')} className={fieldCls} />
            </FormField>
            <FormField label="Страна (ISO 2)" error={form.formState.errors.country?.message}>
              <input {...form.register('country')} className={fieldCls} placeholder="DE" maxLength={2} />
            </FormField>
            <FormField label="Биография">
              <textarea {...form.register('bio')} rows={3} className={fieldCls} />
            </FormField>
            <ModalActions onCancel={() => setShowCreate(false)} loading={createMutation.isPending} />
          </form>
        </Modal>
      )}
    </div>
  )
}

/* ─── Stats ──────────────────────────────────────────────────────────── */

function StatsTab() {
  const { data: albumsPage } = useQuery({ queryKey: ['albums-count'], queryFn: () => albumService.getAll({ size: 1 }) })
  const { data: users = [] } = useQuery({ queryKey: ['admin-users'], queryFn: () => userService.getAll() })
  const { data: artists = [] } = useQuery({ queryKey: ['artists'], queryFn: () => artistService.getAll() })
  const { data: genres = [] } = useQuery({ queryKey: ['genres'], queryFn: () => genreService.getAll() })
  const { data: platforms = [] } = useQuery({ queryKey: ['platforms'], queryFn: () => platformService.getAll() })

  const cards = [
    { label: 'Альбомов', value: albumsPage?.totalElements ?? '…' },
    { label: 'Пользователей', value: users.length || 0 },
    { label: 'Артистов', value: artists.length || 0 },
    { label: 'Жанров', value: genres.length || 0 },
    { label: 'Активных', value: users.filter((u) => u.isActive).length || 0 },
    { label: 'Модераторов', value: users.filter((u) => u.role === 'MODERATOR').length || 0 },
    { label: 'Заблокировано', value: users.filter((u) => !u.isActive).length || 0 },
    { label: 'Платформ', value: platforms.length },
  ]

  return (
    <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
      {cards.map(({ label, value }) => (
        <div key={label} className="rounded border border-border p-4 text-center">
          <p className="text-2xl font-bold tabular-nums">{value}</p>
          <p className="mt-1 text-xs text-muted-foreground">{label}</p>
        </div>
      ))}
    </div>
  )
}

/* ─── Shared form helpers ────────────────────────────────────────────── */

const fieldCls = 'h-9 w-full rounded border border-border bg-transparent px-3 text-sm focus:border-foreground focus:outline-none'

function FormField({ label, error, children }: { label: string; error?: string; children: React.ReactNode }) {
  return (
    <div>
      <label className="mb-1 block text-sm font-medium">{label}</label>
      {children}
      {error && <p className="mt-1 text-xs text-destructive">{error}</p>}
    </div>
  )
}

function ModalActions({ onCancel, loading }: { onCancel: () => void; loading: boolean }) {
  return (
    <div className="flex justify-end gap-2 pt-2">
      <button type="button" onClick={onCancel} className="h-8 rounded border border-border px-4 text-sm text-muted-foreground hover:text-foreground">
        Отмена
      </button>
      <button type="submit" disabled={loading} className="h-8 rounded bg-foreground px-4 text-sm font-medium text-background disabled:opacity-50">
        {loading ? 'Сохраняем…' : 'Сохранить'}
      </button>
    </div>
  )
}
