import { createApiClient } from './client.ts'
import { API_BASE_URL } from './config.ts'

export const api = createApiClient({ baseUrl: API_BASE_URL })

export { ApiError } from './errors.ts'
export { GOOGLE_LOGIN_URL } from './config.ts'
