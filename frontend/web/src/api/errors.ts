export type FieldError = {
  field: string
  reason: string
}

type ErrorBody = {
  error: {
    code: string
    userMessage: string
    retryable: boolean
    requestId: string | null
    fieldErrors: FieldError[]
  }
  fallback: unknown
}

export const NETWORK_ERROR = 'NETWORK_ERROR'
const UNKNOWN_ERROR = 'UNKNOWN_ERROR'

type ApiErrorInit = {
  status: number
  code: string
  userMessage: string | null
  retryable: boolean
  requestId: string | null
  fieldErrors: FieldError[]
  fallback: unknown
}

// 서버가 주지 않은 오류(네트워크 끊김, JSON이 아닌 응답)는 userMessage가 null이다.
export class ApiError extends Error {
  readonly status: number
  readonly code: string
  readonly userMessage: string | null
  readonly retryable: boolean
  readonly requestId: string | null
  readonly fieldErrors: FieldError[]
  readonly fallback: unknown

  constructor(init: ApiErrorInit) {
    super(init.code)
    this.name = 'ApiError'
    this.status = init.status
    this.code = init.code
    this.userMessage = init.userMessage
    this.retryable = init.retryable
    this.requestId = init.requestId
    this.fieldErrors = init.fieldErrors
    this.fallback = init.fallback
  }
}

function isErrorBody(value: unknown): value is ErrorBody {
  if (typeof value !== 'object' || value === null || !('error' in value)) return false
  const error = (value as { error: unknown }).error
  return typeof error === 'object' && error !== null && typeof (error as { code?: unknown }).code === 'string'
}

export async function toApiError(res: Response): Promise<ApiError> {
  const headerRequestId = res.headers.get('X-Request-Id')
  let body: unknown = null
  try {
    body = await res.json()
  } catch {
    // JSON이 아닌 응답
  }
  if (isErrorBody(body)) {
    const { error } = body
    return new ApiError({
      status: res.status,
      code: error.code,
      userMessage: typeof error.userMessage === 'string' ? error.userMessage : null,
      retryable: error.retryable === true,
      requestId: error.requestId ?? headerRequestId,
      fieldErrors: Array.isArray(error.fieldErrors) ? error.fieldErrors : [],
      fallback: body.fallback ?? null,
    })
  }
  return new ApiError({
    status: res.status,
    code: UNKNOWN_ERROR,
    userMessage: null,
    retryable: false,
    requestId: headerRequestId,
    fieldErrors: [],
    fallback: null,
  })
}
