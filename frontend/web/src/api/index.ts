import { createApiClient } from './client.ts'
import { API_BASE_URL } from './config.ts'

let authFailureHandler: (() => void) | null = null

export const api = createApiClient({
  baseUrl: API_BASE_URL,
  onAuthFailure: () => authFailureHandler?.(),
})

export function setAuthFailureHandler(handler: (() => void) | null) {
  authFailureHandler = handler
}

export { ApiError } from './errors.ts'
export { GOOGLE_LOGIN_URL } from './config.ts'
