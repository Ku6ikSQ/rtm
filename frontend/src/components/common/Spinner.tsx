import { cn } from '@/utils/cn'

interface SpinnerProps {
  className?: string
  size?: 'sm' | 'md' | 'lg'
}

export function Spinner({ className, size = 'md' }: SpinnerProps) {
  return (
    <div
      role="status"
      className={cn(
        'animate-spin rounded-full border-2 border-border border-t-foreground',
        size === 'sm' && 'h-4 w-4',
        size === 'md' && 'h-6 w-6',
        size === 'lg' && 'h-10 w-10',
        className,
      )}
    />
  )
}

export function PageSpinner() {
  return (
    <div className="flex min-h-[40vh] items-center justify-center">
      <Spinner size="lg" />
    </div>
  )
}