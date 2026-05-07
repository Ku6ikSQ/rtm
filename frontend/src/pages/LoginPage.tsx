import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link, useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { useAuth } from '@/hooks/useAuth'
import { applyServerErrors } from '@/utils/applyServerErrors'
import type { LoginDto } from '@/types/entities'

const schema = z.object({
  email: z.string().min(1, 'Обязательное поле').email('Некорректный email'),
  password: z.string().min(8, 'Минимум 8 символов'),
})

export function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<LoginDto>({ resolver: zodResolver(schema) })

  async function onSubmit(data: LoginDto) {
    try {
      await login(data)
      navigate('/')
    } catch (err) {
      if (!applyServerErrors(err, setError)) {
        toast.error(err instanceof Error ? err.message : 'Ошибка входа')
      }
    }
  }

  return (
    <div className="flex min-h-[60vh] items-center justify-center">
      <div className="w-full max-w-sm">
        <h1 className="mb-6 text-xl font-bold">Вход</h1>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="mb-1 block text-sm font-medium">Email</label>
            <input
              {...register('email')}
              type="email"
              autoComplete="email"
              className="h-9 w-full rounded border border-border bg-transparent px-3 text-sm focus:border-foreground focus:outline-none"
            />
            {errors.email && <p className="mt-1 text-xs text-destructive">{errors.email.message}</p>}
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium">Пароль</label>
            <input
              {...register('password')}
              type="password"
              autoComplete="current-password"
              className="h-9 w-full rounded border border-border bg-transparent px-3 text-sm focus:border-foreground focus:outline-none"
            />
            {errors.password && <p className="mt-1 text-xs text-destructive">{errors.password.message}</p>}
          </div>
          <button
            type="submit"
            disabled={isSubmitting}
            className="h-9 w-full rounded bg-foreground text-sm font-medium text-background transition-opacity disabled:opacity-50"
          >
            {isSubmitting ? 'Входим…' : 'Войти'}
          </button>
        </form>
        <p className="mt-4 text-center text-sm text-muted-foreground">
          Нет аккаунта?{' '}
          <Link to="/register" className="text-foreground underline">
            Зарегистрироваться
          </Link>
        </p>
      </div>
    </div>
  )
}
