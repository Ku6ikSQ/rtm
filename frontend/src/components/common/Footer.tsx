import { useAuth } from '@/hooks/useAuth'

export function Footer() {
  const { role } = useAuth()

  return (
    <footer className="hidden border-t border-border px-6 py-3 text-xs text-muted-foreground md:flex md:items-center md:justify-between">
      <span>© {new Date().getFullYear()} RTM</span>
      <span>
        v0.1.0{role && ` · ${role}`}
      </span>
    </footer>
  )
}