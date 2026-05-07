import { useAuthStore } from '@/store/authStore'
import { useAuthContext } from '@/context/AuthContext'
import type { UserRole } from '@/types/entities'

export function useAuth() {
  const { user, isLoading } = useAuthStore()
  const { login, register, logout } = useAuthContext()

  return {
    user,
    isLoading,
    isAuthenticated: !!user,
    role: user?.role ?? null,
    hasRole: (roles: UserRole[]) => !!user && roles.includes(user.role),
    login,
    register,
    logout,
  }
}
