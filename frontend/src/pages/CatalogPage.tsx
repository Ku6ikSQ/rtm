import { useState, useEffect } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useSearchParams } from 'react-router-dom'
import { SlidersHorizontal } from 'lucide-react'
import { albumService, genreService } from '@/api'
import { AlbumGrid } from '@/components/album/AlbumGrid'
import { AlbumCardSkeleton } from '@/components/album/AlbumCardSkeleton'
import { AlbumFilters } from '@/components/album/AlbumFilters'
import { cn } from '@/utils/cn'
import type { AlbumFilters as Filters } from '@/types/entities'

const SORT_OPTIONS: { value: NonNullable<Filters['sort']>; label: string }[] = [
  { value: 'rating', label: 'Рейтинг' },
  { value: 'year', label: 'Год' },
  { value: 'createdAt', label: 'Дата' },
]

export function CatalogPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [showMobileFilters, setShowMobileFilters] = useState(false)

  const [filters, setFilters] = useState<Filters>({
    q: searchParams.get('q') ?? undefined,
    genreId: searchParams.get('genreId') ?? undefined,
    sort: 'rating',
    order: 'desc',
    page: 0,
    size: 24,
  })

  // Draft state for mobile panel — only committed on "Применить"
  const [draft, setDraft] = useState<Filters>(filters)

  useEffect(() => {
    const q = searchParams.get('q')
    const genreId = searchParams.get('genreId')
    if (q || genreId) {
      setFilters((f) => ({ ...f, q: q ?? undefined, genreId: genreId ?? undefined }))
    }
  }, []) // eslint-disable-line react-hooks/exhaustive-deps

  const { data, isLoading } = useQuery({
    queryKey: ['albums', filters],
    queryFn: () => albumService.getAll(filters),
  })

  const { data: genres = [] } = useQuery({
    queryKey: ['genres'],
    queryFn: () => genreService.getAll(),
  })

  function updateFilters(partial: Partial<Filters>) {
    setFilters((f) => ({ ...f, ...partial, page: 0 }))
    const params = new URLSearchParams(searchParams)
    if ('q' in partial) {
      partial.q ? params.set('q', partial.q) : params.delete('q')
    }
    if ('genreId' in partial) {
      partial.genreId ? params.set('genreId', partial.genreId) : params.delete('genreId')
    }
    setSearchParams(params, { replace: true })
  }

  function resetFilters() {
    const empty: Filters = { sort: 'rating', order: 'desc', page: 0, size: 24 }
    setFilters(empty)
    setDraft(empty)
    setSearchParams({}, { replace: true })
  }

  function openMobileFilters() {
    setDraft(filters)
    setShowMobileFilters(true)
  }

  function applyMobileFilters() {
    updateFilters(draft)
    setShowMobileFilters(false)
  }

  function resetMobileFilters() {
    resetFilters()
    setShowMobileFilters(false)
  }

  const activeFilterCount = [filters.q, filters.genreId, filters.yearFrom, filters.yearTo, filters.ratingMin, filters.ratingMax].filter(Boolean).length

  const selectedGenreName = filters.genreId
    ? (genres.find((g) => g.id === filters.genreId)?.name ?? `ID ${filters.genreId}`)
    : null

  return (
    <div>
      <div className="mb-4 flex items-center justify-between">
        <h1 className="text-xl font-bold">Каталог</h1>
        <div className="flex items-center gap-2">
          <div className="flex items-center gap-1">
            {SORT_OPTIONS.map((o) => {
              const isActive = filters.sort === o.value
              const dir = isActive ? (filters.order ?? 'desc') : 'desc'
              return (
                <button
                  key={o.value}
                  onClick={() =>
                    isActive
                      ? updateFilters({ order: dir === 'desc' ? 'asc' : 'desc' })
                      : updateFilters({ sort: o.value, order: 'desc' })
                  }
                  className={cn(
                    'flex h-8 items-center gap-0.5 rounded border px-2.5 text-sm transition-colors',
                    isActive
                      ? 'border-foreground bg-foreground text-background'
                      : 'border-border text-muted-foreground hover:text-foreground',
                  )}
                >
                  {o.label}
                  {isActive && <span className="ml-0.5">{dir === 'desc' ? '↓' : '↑'}</span>}
                </button>
              )
            })}
          </div>
          <button
            onClick={openMobileFilters}
            className={cn(
              'flex h-8 items-center gap-1.5 rounded border px-3 text-sm md:hidden',
              activeFilterCount > 0
                ? 'border-foreground text-foreground'
                : 'border-border text-muted-foreground',
            )}
          >
            <SlidersHorizontal className="h-3.5 w-3.5" />
            Фильтры{activeFilterCount > 0 && ` (${activeFilterCount})`}
          </button>
        </div>
      </div>

      {/* Active filter chips */}
      {activeFilterCount > 0 && (
        <div className="mb-4 flex flex-wrap gap-2">
          {filters.q && (
            <FilterChip label={`«${filters.q}»`} onRemove={() => updateFilters({ q: undefined })} />
          )}
          {filters.genreId && selectedGenreName && (
            <FilterChip label={`Жанр: ${selectedGenreName}`} onRemove={() => updateFilters({ genreId: undefined })} />
          )}
          {(filters.yearFrom || filters.yearTo) && (
            <FilterChip
              label={`Год: ${filters.yearFrom ?? '…'} — ${filters.yearTo ?? '…'}`}
              onRemove={() => updateFilters({ yearFrom: undefined, yearTo: undefined })}
            />
          )}
          {(filters.ratingMin != null || filters.ratingMax != null) && (
            <FilterChip
              label={`Рейтинг: ${filters.ratingMin ?? '0'} — ${filters.ratingMax ?? '10'}`}
              onRemove={() => updateFilters({ ratingMin: undefined, ratingMax: undefined })}
            />
          )}
        </div>
      )}

      <div className="flex gap-6">
        {/* Sidebar — desktop */}
        <div className="hidden w-52 flex-shrink-0 md:block">
          <AlbumFilters filters={filters} onChange={updateFilters} onReset={resetFilters} />
        </div>

        {/* Mobile filters bottom sheet */}
        {showMobileFilters && (
          <div
            className="fixed inset-0 z-50 bg-background/80 backdrop-blur md:hidden"
            onClick={() => setShowMobileFilters(false)}
          >
            <div
              className="absolute bottom-0 left-0 right-0 max-h-[85vh] overflow-y-auto rounded-t-xl border-t border-border bg-background p-4"
              onClick={(e) => e.stopPropagation()}
            >
              <div className="mb-4 flex items-center justify-between">
                <span className="text-sm font-medium">Фильтры</span>
                <button
                  onClick={() => setShowMobileFilters(false)}
                  className="text-muted-foreground hover:text-foreground"
                >
                  ✕
                </button>
              </div>
              <AlbumFilters filters={draft} onChange={(p) => setDraft((d) => ({ ...d, ...p }))} onReset={resetMobileFilters} />
              <button
                onClick={applyMobileFilters}
                className="mt-4 h-9 w-full rounded bg-foreground text-sm font-medium text-background"
              >
                Применить
              </button>
            </div>
          </div>
        )}

        {/* Main content */}
        <div className="min-w-0 flex-1">
          {isLoading ? (
            <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6">
              {Array.from({ length: 24 }, (_, i) => <AlbumCardSkeleton key={i} />)}
            </div>
          ) : (
            <>
              <AlbumGrid albums={data?.content ?? []} />
              {data && data.totalPages > 1 && (
                <Pagination
                  page={filters.page ?? 0}
                  totalPages={data.totalPages}
                  onChange={(page) => setFilters((f) => ({ ...f, page }))}
                />
              )}
            </>
          )}
        </div>
      </div>
    </div>
  )
}

function FilterChip({ label, onRemove }: { label: string; onRemove: () => void }) {
  return (
    <span className="flex items-center gap-1 rounded-full border border-border px-2.5 py-0.5 text-xs">
      {label}
      <button onClick={onRemove} className="ml-0.5 text-muted-foreground hover:text-foreground">
        ×
      </button>
    </span>
  )
}

function buildPageItems(page: number, totalPages: number): (number | '...')[] {
  if (totalPages <= 7) return Array.from({ length: totalPages }, (_, i) => i)

  const items: (number | '...')[] = []
  const SIBLINGS = 1

  const rangeStart = Math.max(1, page - SIBLINGS)
  const rangeEnd = Math.min(totalPages - 2, page + SIBLINGS)

  items.push(0)
  if (rangeStart > 1) items.push('...')
  for (let i = rangeStart; i <= rangeEnd; i++) items.push(i)
  if (rangeEnd < totalPages - 2) items.push('...')
  items.push(totalPages - 1)

  return items
}

function Pagination({ page, totalPages, onChange }: { page: number; totalPages: number; onChange: (p: number) => void }) {
  const items = buildPageItems(page, totalPages)

  return (
    <div className="mt-6 flex items-center justify-center gap-1">
      <button
        onClick={() => onChange(page - 1)}
        disabled={page === 0}
        className="h-8 rounded border border-border px-3 text-sm disabled:opacity-40"
      >
        ← Пред
      </button>
      {items.map((item, idx) =>
        item === '...' ? (
          <span key={`ellipsis-${idx}`} className="flex h-8 w-8 items-center justify-center text-sm text-muted-foreground">
            …
          </span>
        ) : (
          <button
            key={item}
            onClick={() => onChange(item)}
            className={cn(
              'h-8 w-8 rounded border text-sm',
              item === page ? 'border-foreground bg-foreground text-background' : 'border-border hover:border-foreground/50',
            )}
          >
            {item + 1}
          </button>
        ),
      )}
      <button
        onClick={() => onChange(page + 1)}
        disabled={page >= totalPages - 1}
        className="h-8 rounded border border-border px-3 text-sm disabled:opacity-40"
      >
        След →
      </button>
    </div>
  )
}
