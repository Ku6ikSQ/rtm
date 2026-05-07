import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '@/hooks/useAuth'
import { PageSpinner } from './Spinner'
import type { UserRole } from '@/types/entities'

interface ProtectedRouteProps {
  roles?: UserRole[]
}

export function ProtectedRoute({ roles }: ProtectedRouteProps) {
  const { isAuthenticated, isLoading, hasRole } = useAuth()

  if (isLoading) return <PageSpinner />
  if (!isAuthenticated) return <Navigate to="/login" replace />
  if (roles && !hasRole(roles)) return <Navigate to="/" replace />

  return <Outlet />
}