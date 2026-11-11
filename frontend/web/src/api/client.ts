import { ApiError, NETWORK_ERROR, toApiError } from './errors.ts'

export type ClientOptions = {
  baseUrl: string
  fetchFn?: typeof fetch
  onAuthFailure?: () => void
}

export type RequestOptions = {
  method?: 'GET' | 'POST'
  query?: Record<string, string | number | boolean | undefined>
  body?: unknown
  auth?: boolean
  idempotent?: boolean
}

const REFRESHABLE_CODES = new Set(['TOKEN_EXPIRED', 'TOKEN_INVALID'])

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
  const base = options.baseUrl.replace(/\/+$/, '')
  const fetchFn = options.fetchFn ?? globalThis.fetch.bind(globalThis)

  let accessToken: string | null = null
  let refreshing: Promise<void> | null = null

  async function send(path: string, opts: RequestOptions, idempotencyKey?: string): Promise<Response> {
    const hasBody = opts.body !== undefined
    const headers = new Headers({ 'X-Client-Channel': 'WEB', Accept: 'application/json' })
    if (hasBody) headers.set('Content-Type', 'application/json')
    if (idempotencyKey) headers.set('Idempotency-Key', idempotencyKey)
    if (opts.auth !== false && accessToken) headers.set('Authorization', `Bearer ${accessToken}`)

    try {
      return await fetchFn(`${base}${path}${toQuery(opts.query)}`, {
        method: opts.method ?? 'GET',
        headers,
        body: hasBody ? JSON.stringify(opts.body) : undefined,
        credentials: 'include',
      })
    } catch {
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

  // Refresh Token은 사용할 때마다 교체되므로, 동시에 여러 요청이 와도 재발급은 한 번만 보낸다.
  function refreshSession(): Promise<void> {
    if (!refreshing) {
      refreshing = (async () => {
        const res = await send('/auth/refresh', { method: 'POST', auth: false })
        if (!res.ok) throw await toApiError(res)
        const data = (await res.json()) as { accessToken: string }
        accessToken = data.accessToken
      })().finally(() => {
        refreshing = null
      })
    }
    return refreshing
  }

  async function request<T>(path: string, opts: RequestOptions = {}): Promise<T> {
    const idempotencyKey = opts.idempotent ? crypto.randomUUID() : undefined
    const sentToken = accessToken
    let res = await send(path, opts, idempotencyKey)

    if (res.status === 401 && opts.auth !== false) {
      const failure = await toApiError(res.clone())
      if (REFRESHABLE_CODES.has(failure.code)) {
        // 이 요청이 나간 뒤 다른 요청이 이미 재발급했다면 다시 재발급하지 않고 현재 토큰으로 재시도한다.
        if (accessToken === sentToken) {
          try {
            await refreshSession()
          } catch (e) {
            if (e instanceof ApiError && e.status === 401) {
              accessToken = null
              options.onAuthFailure?.()
            }
            throw e
          }
        }
        res = await send(path, opts, idempotencyKey)
      }
    }

    if (!res.ok) throw await toApiError(res)
    if (res.status === 204) return undefined as T
    return (await res.json()) as T
  }

  return {
    get: <T>(path: string, opts: Omit<RequestOptions, 'method' | 'body'> = {}) =>
      request<T>(path, { ...opts, method: 'GET' }),
    post: <T>(path: string, body?: unknown, opts: Omit<RequestOptions, 'method' | 'body'> = {}) =>
      request<T>(path, { ...opts, method: 'POST', body }),
    refreshSession,
    setAccessToken: (token: string) => {
      accessToken = token
    },
    clearAccessToken: () => {
      accessToken = null
    },
  }
}
