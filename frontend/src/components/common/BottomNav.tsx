import { NavLink } from 'react-router-dom'
import { Home, Search, Plus, User, LogIn, Shield, ListTree } from 'lucide-react'
import { useAuth } from '@/hooks/useAuth'
import { cn } from '@/utils/cn'

export function BottomNav() {
  const { isAuthenticated, hasRole } = useAuth()

  const items = [
    { to: '/', icon: Home, label: 'Главная', end: true, show: true },
    { to: '/catalog', icon: Search, label: 'Каталог', show: true },
    { to: '/album/new', icon: Plus, label: 'Добавить', show: isAuthenticated },
    { to: '/profile', icon: User, label: 'Профиль', show: isAuthenticated },
    { to: '/login', icon: LogIn, label: 'Войти', show: !isAuthenticated },
    {
      to: hasRole(['ADMIN', 'MODERATOR']) ? '/admin' : '/genres',
      icon: hasRole(['ADMIN', 'MODERATOR']) ? Shield : ListTree,
      label: hasRole(['ADMIN', 'MODERATOR']) ? 'Админ' : 'Жанры',
      show: true,
    },
  ].filter((i) => i.show)

  const visible = items.slice(0, 5)

  return (
    <nav className="fixed bottom-0 left-0 right-0 z-40 border-t border-border bg-background md:hidden">
      <div className="grid h-14" style={{ gridTemplateColumns: `repeat(${visible.length}, 1fr)` }}>
        {visible.map(({ to, icon: Icon, label, end }) => (
          <NavLink
            key={to}
            to={to}
            end={end}
            className={({ isActive }) =>
              cn(
                'flex flex-col items-center justify-center gap-0.5 text-[10px]',
                isActive ? 'text-foreground' : 'text-muted-foreground',
              )
            }
          >
            <Icon className="h-5 w-5" />
            <span>{label}</span>
          </NavLink>
        ))}
      </div>
    </nav>
  )
}
