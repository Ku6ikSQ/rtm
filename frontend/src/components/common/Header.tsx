import { Link, NavLink, useNavigate } from 'react-router-dom'
import { Sun, Moon, LogOut, User, Shield, Search, Plus } from 'lucide-react'
import { useAuth } from '@/hooks/useAuth'
import { useTheme } from '@/hooks/useTheme'
import { cn } from '@/utils/cn'
import { useState } from 'react'

export function Header() {
  const { isAuthenticated, user, logout, hasRole } = useAuth()
  const { theme, toggle } = useTheme()
  const navigate = useNavigate()
  const [searchQuery, setSearchQuery] = useState('')

  function handleSearch(e: React.FormEvent) {
    e.preventDefault()
    if (searchQuery.trim()) {
      navigate(`/catalog?q=${encodeURIComponent(searchQuery.trim())}`)
    }
  }

  return (
    <header className="sticky top-0 z-40 border-b border-border bg-background">
      <div className="mx-auto flex h-12 max-w-7xl items-center gap-4 px-4">
        {/* Logo */}
        <Link
          to="/"
          className="flex-shrink-0 border border-border px-2 py-0.5 text-sm font-bold tracking-widest"
        >
          RTM
        </Link>

        {/* Desktop nav */}
        <nav className="hidden items-center gap-4 text-sm md:flex">
          {[
            { to: '/', label: 'Главная', end: true },
            { to: '/catalog', label: 'Каталог' },
            { to: '/genres', label: 'Жанры' },
          ].map(({ to, label, end }) => (
            <NavLink
              key={to}
              to={to}
              end={end}
              className={({ isActive }) =>
                cn('transition-colors hover:text-foreground', isActive ? 'text-foreground' : 'text-muted-foreground')
              }
            >
              {label}
            </NavLink>
          ))}
        </nav>

        {/* Desktop search */}
        <form onSubmit={handleSearch} className="hidden flex-1 md:flex">
          <div className="relative w-full max-w-sm">
            <Search className="absolute left-2.5 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-muted-foreground" />
            <input
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Поиск альбомов, артистов…"
              className="h-8 w-full rounded border border-border bg-transparent pl-8 pr-3 text-sm placeholder:text-muted-foreground focus:border-foreground focus:outline-none"
            />
          </div>
        </form>

        <div className="flex flex-1 items-center justify-end gap-2 md:flex-none">
          {/* Theme toggle */}
          <button
            onClick={toggle}
            className="flex h-8 w-8 items-center justify-center rounded border border-border text-muted-foreground transition-colors hover:text-foreground"
            aria-label="Переключить тему"
          >
            {theme === 'dark' ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
          </button>

          {/* Desktop auth */}
          <div className="hidden items-center gap-2 md:flex">
            {isAuthenticated ? (
              <>
                {hasRole(['ADMIN', 'MODERATOR']) && (
                  <>
                    <Link
                      to="/album/new"
                      className="flex h-8 items-center gap-1 rounded border border-border px-2.5 text-xs text-muted-foreground transition-colors hover:text-foreground"
                    >
                      <Plus className="h-3.5 w-3.5" />
                      Альбом
                    </Link>
                    <Link
                      to="/admin"
                      className="flex h-8 items-center gap-1.5 rounded border border-border px-2.5 text-xs text-muted-foreground transition-colors hover:text-foreground"
                    >
                      <Shield className="h-3.5 w-3.5" />
                      Управление
                    </Link>
                  </>
                )}
                <Link
                  to="/profile"
                  className="flex h-8 items-center gap-1.5 rounded border border-border px-2.5 text-xs text-muted-foreground transition-colors hover:text-foreground"
                >
                  <User className="h-3.5 w-3.5" />
                  {user?.username}
                </Link>
                <button
                  onClick={() => logout()}
                  className="flex h-8 items-center gap-1.5 rounded border border-border px-2.5 text-xs text-muted-foreground transition-colors hover:text-foreground"
                >
                  <LogOut className="h-3.5 w-3.5" />
                </button>
              </>
            ) : (
              <>
                <Link
                  to="/login"
                  className="flex h-8 items-center px-3 text-xs text-muted-foreground transition-colors hover:text-foreground"
                >
                  Войти
                </Link>
                <Link
                  to="/register"
                  className="flex h-8 items-center rounded border border-foreground bg-foreground px-3 text-xs text-background transition-colors hover:opacity-80"
                >
                  Регистрация
                </Link>
              </>
            )}
          </div>
        </div>
      </div>
    </header>
  )
}