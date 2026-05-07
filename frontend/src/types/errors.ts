export interface ServerProblemDetail {
  type?: string
  title: string
  status: number
  detail: string
  instance?: string
  errors?: string[]
  fieldErrors?: Record<string, string>
}

export class ApiError extends Error {
  readonly status: number
  readonly fieldErrors?: Record<string, string>

  constructor(status: number, message: string, fieldErrors?: Record<string, string>) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.fieldErrors = fieldErrors
  }

  hasFieldErrors(): boolean {
    return !!this.fieldErrors && Object.keys(this.fieldErrors).length > 0
  }

  static fromProblemDetail(status: number, body: ServerProblemDetail): ApiError {
    const fieldErrors: Record<string, string> = {}

    if (body.fieldErrors) {
      Object.assign(fieldErrors, body.fieldErrors)
    }

    // Parse Spring's MethodArgumentNotValidException errors like "field: message"
    if (body.errors) {
      for (const err of body.errors) {
        const colonIdx = err.indexOf(':')
        if (colonIdx > 0) {
          const field = err.slice(0, colonIdx).trim()
          const msg = err.slice(colonIdx + 1).trim()
          fieldErrors[field] = msg
        }
      }
    }

    return new ApiError(
      status,
      body.detail || body.title || 'Unexpected error',
      Object.keys(fieldErrors).length > 0 ? fieldErrors : undefined,
    )
  }
}
