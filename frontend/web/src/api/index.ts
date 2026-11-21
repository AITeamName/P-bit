import { createApiClient } from './client.ts'
import { locale } from '../i18n/messages.ts'
import { API_BASE_URL } from './config.ts'

let authFailureHandler: (() => void) | null = null

export const api = createApiClient({
  baseUrl: API_BASE_URL,
  language: locale,
  onAuthFailure: () => authFailureHandler?.(),
})

export function setAuthFailureHandler(handler: (() => void) | null) {
  authFailureHandler = handler
}

export { ApiError, getUserMessage } from './errors.ts'
export { GOOGLE_LOGIN_URL } from './config.ts'
