import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link, useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { useAuth } from '@/hooks/useAuth'
import { applyServerErrors } from '@/utils/applyServerErrors'
import type { RegisterDto } from '@/types/entities'

const schema = z
  .object({
    username: z.string().min(3, 'Минимум 3 символа').max(50, 'Максимум 50 символов'),
    email: z.string().min(1, 'Обязательное поле').email('Некорректный email'),
    password: z.string().min(8, 'Минимум 8 символов'),
    passwordConfirm: z.string().min(1, 'Обязательное поле'),
  })
  .refine((d) => d.password === d.passwordConfirm, {
    path: ['passwordConfirm'],
    message: 'Пароли не совпадают',
  })

export function RegisterPage() {
  const { register: registerUser } = useAuth()
  const navigate = useNavigate()

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<RegisterDto>({ resolver: zodResolver(schema) })

  async function onSubmit(data: RegisterDto) {
    try {
      await registerUser(data)
      navigate('/')
    } catch (err) {
      if (!applyServerErrors(err, setError)) {
        toast.error(err instanceof Error ? err.message : 'Ошибка регистрации')
      }
    }
  }

  return (
    <div className="flex min-h-[60vh] items-center justify-center">
      <div className="w-full max-w-sm">
        <h1 className="mb-6 text-xl font-bold">Регистрация</h1>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          {[
            { name: 'username' as const, label: 'Username', type: 'text', auto: 'username' },
            { name: 'email' as const, label: 'Email', type: 'email', auto: 'email' },
            { name: 'password' as const, label: 'Пароль', type: 'password', auto: 'new-password' },
            { name: 'passwordConfirm' as const, label: 'Повторите пароль', type: 'password', auto: 'new-password' },
          ].map(({ name, label, type, auto }) => (
            <div key={name}>
              <label className="mb-1 block text-sm font-medium">{label}</label>
              <input
                {...register(name)}
                type={type}
                autoComplete={auto}
                className="h-9 w-full rounded border border-border bg-transparent px-3 text-sm focus:border-foreground focus:outline-none"
              />
              {errors[name] && (
                <p className="mt-1 text-xs text-destructive">{errors[name]?.message}</p>
              )}
            </div>
          ))}
          <button
            type="submit"
            disabled={isSubmitting}
            className="h-9 w-full rounded bg-foreground text-sm font-medium text-background transition-opacity disabled:opacity-50"
          >
            {isSubmitting ? 'Регистрируемся…' : 'Зарегистрироваться'}
          </button>
        </form>
        <p className="mt-4 text-center text-sm text-muted-foreground">
          Уже есть аккаунт?{' '}
          <Link to="/login" className="text-foreground underline">
            Войти
          </Link>
        </p>
      </div>
    </div>
  )
}
