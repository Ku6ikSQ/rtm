import { type FieldValues, type Path, type UseFormSetError } from 'react-hook-form'
import { ApiError } from '@/types/errors'

export function applyServerErrors<T extends FieldValues>(
  error: unknown,
  setError: UseFormSetError<T>,
): boolean {
  if (!(error instanceof ApiError) || !error.hasFieldErrors()) return false
  for (const [field, message] of Object.entries(error.fieldErrors!)) {
    setError(field as Path<T>, { type: 'server', message })
  }
  return true
}
