import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '@/hooks/useAuth'
import { PageSpinner } from './Spinner'

export function GuestRoute() {
  const { isAuthenticated, isLoading } = useAuth()

  if (isLoading) return <PageSpinner />
  if (isAuthenticated) return <Navigate to="/" replace />

  return <Outlet />
}