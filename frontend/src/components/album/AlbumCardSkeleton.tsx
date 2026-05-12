export function AlbumCardSkeleton() {
  return (
    <div className="animate-pulse">
      <div className="aspect-square w-full rounded border border-border bg-muted" />
      <div className="mt-2 space-y-1.5">
        <div className="flex items-center justify-between gap-2">
          <div className="h-3.5 w-3/4 rounded bg-muted" />
          <div className="h-5 w-7 flex-shrink-0 rounded bg-muted" />
        </div>
        <div className="h-3 w-1/2 rounded bg-muted" />
        <div className="h-3 w-10 rounded bg-muted" />
      </div>
    </div>
  )
}
