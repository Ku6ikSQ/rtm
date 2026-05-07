import { cn } from '@/utils/cn'

interface EmptyStateProps {
  title?: string
  description?: string
  icon?: React.ReactNode
  action?: React.ReactNode
  className?: string
}

export function EmptyState({
  title = 'Ничего не найдено',
  description,
  icon,
  action,
  className,
}: EmptyStateProps) {
  return (
    <div className={cn('flex flex-col items-center gap-3 py-16 text-center text-muted-foreground', className)}>
      {icon && <div className="text-4xl">{icon}</div>}
      <p className="text-sm font-medium text-foreground">{title}</p>
      {description && <p className="max-w-xs text-xs">{description}</p>}
      {action && <div className="mt-2">{action}</div>}
    </div>
  )
}