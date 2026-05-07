import { createContext, useContext, useEffect, type ReactNode } from 'react'
import { authService } from '@/api'
import { useAuthStore } from '@/store/authStore'
import type { LoginDto, RegisterDto } from '@/types/entities'

interface AuthContextValue {
  login: (dto: LoginDto) => Promise<void>
  register: (dto: RegisterDto) => Promise<void>
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const { setUser, setAccessToken, setLoading, clear } = useAuthStore()

  useEffect(() => {
    async function restoreSession() {
      try {
        const user = await authService.getMe()
        setUser(user)
      } catch {
        // no active session
      } finally {
        setLoading(false)
      }
    }
    restoreSession()
  }, [setUser, setLoading])

  async function login(dto: LoginDto) {
    const tokens = await authService.login(dto)
    setAccessToken(tokens.accessToken)
    const user = await authService.getMe()
    setUser(user)
  }

  async function register(dto: RegisterDto) {
    const tokens = await authService.register(dto)
    setAccessToken(tokens.accessToken)
    const user = await authService.getMe()
    setUser(user)
  }

  async function logout() {
    const token = useAuthStore.getState().accessToken ?? ''
    try {
      await authService.logout(token)
    } catch {
      // ignore
    } finally {
      clear()
    }
  }

  return <AuthContext.Provider value={{ login, register, logout }}>{children}</AuthContext.Provider>
}

export function useAuthContext() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuthContext must be inside AuthProvider')
  return ctx
}
