import { useQuery } from '@tanstack/react-query'
import { genreService } from '@/api'
import type { AlbumFilters as Filters } from '@/types/entities'

interface AlbumFiltersProps {
  filters: Filters
  onChange: (f: Partial<Filters>) => void
  onReset: () => void
}

export function AlbumFilters({ filters, onChange, onReset }: AlbumFiltersProps) {
  const { data: genres = [] } = useQuery({
    queryKey: ['genres'],
    queryFn: () => genreService.getAll(),
  })

  const rootGenres = genres.filter((g) => !g.parentId)

  return (
    <aside className="space-y-5">
      {/* Search */}
      <div>
        <label className="mb-1 block text-xs font-medium uppercase tracking-wider text-muted-foreground">
          Поиск
        </label>
        <input
          value={filters.q ?? ''}
          onChange={(e) => onChange({ q: e.target.value || undefined })}
          placeholder="Название, артист…"
          className="h-8 w-full rounded border border-border bg-transparent px-3 text-sm focus:border-foreground focus:outline-none"
        />
      </div>

      {/* Genre */}
      <div>
        <label className="mb-1 block text-xs font-medium uppercase tracking-wider text-muted-foreground">
          Жанр
        </label>
        <select
          value={filters.genreId ?? ''}
          onChange={(e) => onChange({ genreId: e.target.value || undefined })}
          className="h-8 w-full rounded border border-border bg-background px-2 text-sm focus:border-foreground focus:outline-none"
        >
          <option value="">Все жанры</option>
          {rootGenres.map((g) => (
            <optgroup key={g.id} label={g.name}>
              <option value={g.id}>{g.name}</option>
              {genres
                .filter((c) => c.parentId === g.id)
                .map((c) => (
                  <option key={c.id} value={c.id}>
                    ↳ {c.name}
                  </option>
                ))}
            </optgroup>
          ))}
        </select>
      </div>

      {/* Year range */}
      <div>
        <label className="mb-1 block text-xs font-medium uppercase tracking-wider text-muted-foreground">
          Год
        </label>
        <div className="flex items-center gap-2">
          <input
            type="number"
            value={filters.yearFrom ?? ''}
            onChange={(e) => onChange({ yearFrom: e.target.value ? Number(e.target.value) : undefined })}
            placeholder="От"
            min={1950}
            max={2030}
            className="h-8 w-full rounded border border-border bg-transparent px-2 text-sm focus:border-foreground focus:outline-none"
          />
          <span className="text-muted-foreground">—</span>
          <input
            type="number"
            value={filters.yearTo ?? ''}
            onChange={(e) => onChange({ yearTo: e.target.value ? Number(e.target.value) : undefined })}
            placeholder="До"
            min={1950}
            max={2030}
            className="h-8 w-full rounded border border-border bg-transparent px-2 text-sm focus:border-foreground focus:outline-none"
          />
        </div>
      </div>

      {/* Rating range */}
      <div>
        <label className="mb-1 block text-xs font-medium uppercase tracking-wider text-muted-foreground">
          Рейтинг
        </label>
        <div className="flex items-center gap-2">
          <input
            type="number"
            value={filters.ratingMin ?? ''}
            onChange={(e) => onChange({ ratingMin: e.target.value ? Number(e.target.value) : undefined })}
            placeholder="От"
            min={0}
            max={10}
            step={0.1}
            className="h-8 w-full rounded border border-border bg-transparent px-2 text-sm focus:border-foreground focus:outline-none"
          />
          <span className="text-muted-foreground">—</span>
          <input
            type="number"
            value={filters.ratingMax ?? ''}
            onChange={(e) => onChange({ ratingMax: e.target.value ? Number(e.target.value) : undefined })}
            placeholder="До"
            min={0}
            max={10}
            step={0.1}
            className="h-8 w-full rounded border border-border bg-transparent px-2 text-sm focus:border-foreground focus:outline-none"
          />
        </div>
      </div>

      <button
        onClick={onReset}
        className="w-full rounded border border-border py-1.5 text-sm text-muted-foreground transition-colors hover:border-foreground hover:text-foreground"
      >
        Сбросить фильтры
      </button>
    </aside>
  )
}
