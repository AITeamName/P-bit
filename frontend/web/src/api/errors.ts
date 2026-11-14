// 서버 오류 응답을 앱에서 쓰기 쉬운 모양(ApiError)으로 변환

// 입력값 검증 실패 한 항목 (어느 칸이 왜 틀렸는지)
export type FieldError = {
  field: string
  reason: string
}

// 서버가 오류일 때 보내기로 약속한 응답 모양
type ErrorBody = {
  error: {
    code: string // 오류 종류 코드 (예: INVALID_CREDENTIALS)
    userMessage: string // 사용자에게 그대로 보여 줘도 되는 문구
    retryable: boolean // 다시 시도하면 될 수 있는 오류인지
    requestId: string | null // 서버 로그에서 요청을 찾기 위한 번호
    fieldErrors: FieldError[] // 칸별 오류 목록
  }
  fallback: unknown // 오류와 함께 오는 대체 값 (없을 수 있음)
}

// 서버에 닿지 못한 경우의 오류 코드 (인터넷 끊김 등)
export const NETWORK_ERROR = 'NETWORK_ERROR'
// 서버가 약속한 모양이 아닌 오류를 보낸 경우의 오류 코드
const UNKNOWN_ERROR = 'UNKNOWN_ERROR'

// ApiError 생성에 필요한 값
type ApiErrorInit = {
  status: number
  code: string
  userMessage: string | null
  retryable: boolean
  requestId: string | null
  fieldErrors: FieldError[]
  fallback: unknown
}

// 서버 통신 오류 (서버가 문구를 주지 않은 경우 userMessage는 null: 네트워크 끊김, JSON 아닌 응답)
// Error 상속: catch에서 instanceof ApiError로 통신 오류만 구분 가능
export class ApiError extends Error {
  readonly status: number // HTTP 상태 번호 (401, 500 등), 서버에 닿지 못하면 0
  readonly code: string
  readonly userMessage: string | null
  readonly retryable: boolean
  readonly requestId: string | null
  readonly fieldErrors: FieldError[]
  readonly fallback: unknown

  constructor(init: ApiErrorInit) {
    super(init.code) // 기본 오류 메시지는 오류 코드
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

// 응답 본문이 약속한 오류 모양인지 검사 (맞으면 ErrorBody로 취급)
function isErrorBody(value: unknown): value is ErrorBody {
  if (typeof value !== 'object' || value === null || !('error' in value)) return false
  const error = (value as { error: unknown }).error
  // 최소 조건: error 안에 문자열 code가 있어야 약속한 모양으로 인정
  return typeof error === 'object' && error !== null && typeof (error as { code?: unknown }).code === 'string'
}

// 서버 오류 응답(Response)을 ApiError 하나로 변환
export async function toApiError(res: Response): Promise<ApiError> {
  const headerRequestId = res.headers.get('X-Request-Id') // 응답 헤더의 요청 번호
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
      // 아래 줄들의 기본값: 서버가 해당 값을 빼먹은 경우의 안전한 처리
      // 문구가 없으면 임의 문구를 만들지 않고 null (화면이 문구를 지어내지 않게)
      userMessage: typeof error.userMessage === 'string' ? error.userMessage : null,
      // true로 명시된 경우만 재시도 가능
      retryable: error.retryable === true,
      // 본문에 없으면 같은 정보인 응답 헤더 값 사용
      requestId: error.requestId ?? headerRequestId,
      // 칸별 오류 목록이 없으면 빈 목록
      fieldErrors: Array.isArray(error.fieldErrors) ? error.fieldErrors : [],
      // 대체 값이 없으면 null
      fallback: body.fallback ?? null,
    })
  }
  // 약속한 모양이 아니면 UNKNOWN_ERROR 코드 (원인 불명 오류라는 점이 코드에 드러남)
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
