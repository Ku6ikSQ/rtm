import { Plus, Trash2 } from 'lucide-react'
import type { Platform } from '@/types/entities'

export interface LinkRow {
  _id: string
  platformId: string
  url: string
}

interface LinksEditorProps {
  links: LinkRow[]
  platforms: Platform[]
  onChange: (links: LinkRow[]) => void
}

export function LinksEditor({ links, platforms, onChange }: LinksEditorProps) {
  const usedPlatformIds = new Set(links.map((l) => l.platformId).filter(Boolean))

  function add() {
    const free = platforms.find((p) => !usedPlatformIds.has(p.id))
    onChange([...links, { _id: `l-${Date.now()}`, platformId: free?.id ?? '', url: '' }])
  }

  function update(id: string, field: keyof LinkRow, value: string) {
    onChange(links.map((l) => (l._id === id ? { ...l, [field]: value } : l)))
  }

  function remove(id: string) {
    onChange(links.filter((l) => l._id !== id))
  }

  const allUsed = platforms.length > 0 && usedPlatformIds.size >= platforms.length

  return (
    <div>
      <div className="mb-2 flex items-center justify-between">
        <label className="text-sm font-medium">
          Ссылки на платформы{links.length > 0 && <span className="ml-1 text-muted-foreground">({links.length})</span>}
        </label>
        <button
          type="button"
          onClick={add}
          disabled={allUsed}
          className="flex items-center gap-1 text-xs text-muted-foreground hover:text-foreground disabled:opacity-40"
        >
          <Plus className="h-3.5 w-3.5" />
          Добавить
        </button>
      </div>

      {links.length > 0 ? (
        <div className="space-y-2">
          {links.map((row) => {
            const availablePlatforms = platforms.filter(
              (p) => p.id === row.platformId || !usedPlatformIds.has(p.id),
            )
            return (
              <div key={row._id} className="flex items-center gap-2">
                <select
                  value={row.platformId}
                  onChange={(e) => update(row._id, 'platformId', e.target.value)}
                  className="h-8 w-36 flex-shrink-0 rounded border border-border bg-background px-2 text-sm focus:border-foreground focus:outline-none"
                >
                  <option value="">— платформа —</option>
                  {availablePlatforms.map((p) => (
                    <option key={p.id} value={p.id}>{p.name}</option>
                  ))}
                </select>
                <input
                  value={row.url}
                  onChange={(e) => update(row._id, 'url', e.target.value)}
                  placeholder="https://…"
                  className="h-8 min-w-0 flex-1 rounded border border-border bg-transparent px-2 text-sm focus:border-foreground focus:outline-none"
                />
                <button
                  type="button"
                  onClick={() => remove(row._id)}
                  className="flex-shrink-0 text-muted-foreground hover:text-destructive"
                >
                  <Trash2 className="h-4 w-4" />
                </button>
              </div>
            )
          })}
        </div>
      ) : (
        <p className="rounded border border-dashed border-border py-3 text-center text-xs text-muted-foreground">
          Ссылки не добавлены
        </p>
      )}
    </div>
  )
}
