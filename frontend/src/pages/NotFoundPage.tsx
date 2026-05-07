import { Link, useRouteError, isRouteErrorResponse } from 'react-router-dom'

export function NotFoundPage() {
  const error = useRouteError()
  const is404 = !error || (isRouteErrorResponse(error) && error.status === 404)

  return (
    <div className="flex min-h-[60vh] flex-col items-center justify-center gap-4 text-center">
      <p className="text-6xl font-bold tracking-tight text-muted-foreground/30">
        {is404 ? '404' : '500'}
      </p>
      <h1 className="text-xl font-semibold">
        {is404 ? 'Страница не найдена' : 'Что-то пошло не так'}
      </h1>
      <p className="max-w-sm text-sm text-muted-foreground">
        {is404
          ? 'Такой страницы не существует или она была удалена.'
          : 'Произошла непредвиденная ошибка. Попробуйте обновить страницу.'}
      </p>
      <Link
        to="/"
        className="mt-2 h-9 rounded border border-border px-5 text-sm text-muted-foreground hover:text-foreground inline-flex items-center"
      >
        На главную
      </Link>
    </div>
  )
}
