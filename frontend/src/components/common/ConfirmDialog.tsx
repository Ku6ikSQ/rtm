import { useEffect, useRef } from 'react'

interface ConfirmDialogProps {
  open: boolean
  title: string
  description?: string
  confirmLabel?: string
  onConfirm: () => void
  onCancel: () => void
  destructive?: boolean
}

export function ConfirmDialog({
  open,
  title,
  description,
  confirmLabel = 'Удалить',
  onConfirm,
  onCancel,
  destructive = true,
}: ConfirmDialogProps) {
  const confirmRef = useRef<HTMLButtonElement>(null)

  useEffect(() => {
    if (open) confirmRef.current?.focus()
  }, [open])

  useEffect(() => {
    if (!open) return
    function onKey(e: KeyboardEvent) {
      if (e.key === 'Escape') onCancel()
    }
    document.addEventListener('keydown', onKey)
    return () => document.removeEventListener('keydown', onKey)
  }, [open, onCancel])

  if (!open) return null

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-background/70 backdrop-blur-sm"
      onClick={onCancel}
    >
      <div
        className="mx-4 w-full max-w-sm rounded-lg border border-border bg-background p-6 shadow-lg"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 className="mb-2 text-base font-semibold">{title}</h2>
        {description && (
          <p className="mb-5 text-sm text-muted-foreground">{description}</p>
        )}
        <div className="flex justify-end gap-2">
          <button
            type="button"
            onClick={onCancel}
            className="h-9 rounded border border-border px-4 text-sm text-muted-foreground hover:text-foreground"
          >
            Отмена
          </button>
          <button
            ref={confirmRef}
            type="button"
            onClick={onConfirm}
            className={`h-9 rounded px-4 text-sm font-medium text-white ${
              destructive ? 'bg-destructive hover:opacity-90' : 'bg-foreground text-background hover:opacity-90'
            }`}
          >
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  )
}
