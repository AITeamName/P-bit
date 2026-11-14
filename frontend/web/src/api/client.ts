// 서버 통신 공통 도구 (모든 화면이 이 도구로 서버에 요청)
// 공통 헤더, 로그인 토큰, 오류 변환, 토큰 만료 시 재발급 후 재시도
import { ApiError, NETWORK_ERROR, toApiError } from './errors.ts'

// 도구 생성 설정
export type ClientOptions = {
  baseUrl: string // 서버 주소
  fetchFn?: typeof fetch // 요청 전송 함수 (테스트에서 가짜 함수를 넣는 용도)
  onAuthFailure?: () => void // 로그인이 완전히 풀렸을 때 호출할 함수
}

// 요청별 옵션
export type RequestOptions = {
  method?: 'GET' | 'POST'
  query?: Record<string, string | number | boolean | undefined> // 주소 뒤 ?a=1&b=2 값
  body?: unknown // 서버로 보낼 내용 (JSON으로 변환)
  auth?: boolean // false면 로그인 토큰을 붙이지 않음 (로그인 요청 등)
  idempotent?: boolean // true면 중복 처리 방지 번호(Idempotency-Key) 추가
}

// 이 오류 코드면 토큰 문제: 토큰을 새로 받고 요청을 다시 전송
const REFRESHABLE_CODES = new Set(['TOKEN_EXPIRED', 'TOKEN_INVALID'])

// { a: 1, b: undefined } -> "?a=1" (undefined 값은 제외)
function toQuery(query: RequestOptions['query']): string {
  if (!query) return ''
  const params = new URLSearchParams()
  for (const [key, value] of Object.entries(query)) {
    if (value !== undefined) params.set(key, String(value))
  }
  const text = params.toString()
  return text ? `?${text}` : ''
}

export function createApiClient(options: ClientOptions) {
  // 서버 주소 끝의 / 제거 (주소 + /경로 이어 붙일 때 // 방지)
  const base = options.baseUrl.replace(/\/+$/, '')
  // 필요한 기본값: fetchFn을 안 넘기면 브라우저의 실제 fetch 사용
  const fetchFn = options.fetchFn ?? globalThis.fetch.bind(globalThis)

  // 로그인 통행증(액세스 토큰): 보안을 위해 브라우저 메모리에만 보관 (새로고침하면 사라짐)
  let accessToken: string | null = null
  // 진행 중인 토큰 재발급 작업 (동시에 여러 번 재발급하지 않기 위한 자리)
  let refreshing: Promise<void> | null = null

  // 서버에 요청 한 번 전송
  async function send(path: string, opts: RequestOptions, idempotencyKey?: string): Promise<Response> {
    const hasBody = opts.body !== undefined
    // 모든 요청의 공통 헤더: 웹에서 온 요청 표시, JSON 응답 요청
    const headers = new Headers({ 'X-Client-Channel': 'WEB', Accept: 'application/json' })
    if (hasBody) headers.set('Content-Type', 'application/json') // 보내는 내용이 JSON이라는 표시
    if (idempotencyKey) headers.set('Idempotency-Key', idempotencyKey)
    // auth를 false로 명시한 요청만 토큰 생략 (기본은 토큰이 있으면 붙임)
    if (opts.auth !== false && accessToken) headers.set('Authorization', `Bearer ${accessToken}`)

    try {
      return await fetchFn(`${base}${path}${toQuery(opts.query)}`, {
        // 참고: get/post가 항상 method를 넘겨서 이 기본값(GET)은 실제로 쓰이지 않음
        method: opts.method ?? 'GET',
        headers,
        body: hasBody ? JSON.stringify(opts.body) : undefined,
        credentials: 'include', // 쿠키(재발급용 refresh 쿠키) 함께 전송
      })
    } catch {
      // 서버에 닿지 못한 경우 (인터넷 끊김 등), 서버 문구가 없어서 userMessage는 null
      throw new ApiError({
        status: 0,
        code: NETWORK_ERROR,
        userMessage: null,
        retryable: true,
        requestId: null,
        fieldErrors: [],
        fallback: null,
      })
    }
  }

  // 재발급 한 번만 전송 (Refresh Token은 사용할 때마다 교체되어 동시에 여러 번 보내면 실패)
  // 이미 진행 중이면 새로 시작하지 않고 같은 작업을 함께 기다림
  function refreshSession(): Promise<void> {
    if (!refreshing) {
      refreshing = (async () => {
        const res = await send('/auth/refresh', { method: 'POST', auth: false })
        if (!res.ok) throw await toApiError(res)
        const data = (await res.json()) as { accessToken: string }
        accessToken = data.accessToken // 새 토큰을 메모리에 저장
      })().finally(() => {
        refreshing = null // 성공이든 실패든 끝나면 자리를 비워 다음 재발급 허용
      })
    }
    return refreshing
  }

  // 요청 전송과 결과 반환 (토큰이 만료됐으면 재발급 후 한 번 재시도)
  // 참고: get/post가 항상 opts를 넘겨서 아래 기본값 {}는 실제로 쓰이지 않음
  async function request<T>(path: string, opts: RequestOptions = {}): Promise<T> {
    // 중복 처리 방지 번호: 요청당 한 번만 생성, 재시도에도 같은 번호 사용
    const idempotencyKey = opts.idempotent ? crypto.randomUUID() : undefined
    const sentToken = accessToken // 이 요청에 사용한 토큰 기억
    let res = await send(path, opts, idempotencyKey)

    // 401(인증 실패)이면서 토큰을 붙인 요청일 때만 재발급 시도
    if (res.status === 401 && opts.auth !== false) {
      const failure = await toApiError(res.clone()) // 응답은 한 번만 읽을 수 있어 복사본으로 읽음
      if (REFRESHABLE_CODES.has(failure.code)) {
        // 이 요청이 나간 뒤 다른 요청이 이미 재발급했다면 재발급 생략, 현재 토큰으로 재시도
        // (토큰이 그대로일 때만 직접 재발급)
        if (accessToken === sentToken) {
          try {
            await refreshSession()
          } catch (e) {
            // 재발급마저 401로 실패: 로그인이 완전히 풀림 (토큰 삭제 후 알림)
            if (e instanceof ApiError && e.status === 401) {
              accessToken = null
              options.onAuthFailure?.() // 등록된 함수가 있으면 실행 (로그인 화면으로 이동 등)
            }
            throw e
          }
        }
        res = await send(path, opts, idempotencyKey) // 새 토큰으로 같은 요청 재전송
      }
    }

    if (!res.ok) throw await toApiError(res) // 실패 응답은 ApiError로 변환해 던짐
    if (res.status === 204) return undefined as T // 204: 성공, 돌려줄 내용 없음
    return (await res.json()) as T
  }

  // 밖에서 쓰는 기능
  return {
    // 기본값 {}: 호출하는 쪽이 옵션을 생략할 수 있게 함
    get: <T>(path: string, opts: Omit<RequestOptions, 'method' | 'body'> = {}) =>
      request<T>(path, { ...opts, method: 'GET' }),
    post: <T>(path: string, body?: unknown, opts: Omit<RequestOptions, 'method' | 'body'> = {}) =>
      request<T>(path, { ...opts, method: 'POST', body }),
    refreshSession, // 앱 시작 때 로그인 상태 복구용
    setAccessToken: (token: string) => {
      accessToken = token
    },
    clearAccessToken: () => {
      accessToken = null
    },
  }
}
