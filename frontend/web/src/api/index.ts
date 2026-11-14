// 앱 전체가 공유하는 서버 통신 도구(api) 생성과 내보내기
import { createApiClient } from './client.ts' // 서버 통신 도구 생성 함수
import { API_BASE_URL } from './config.ts' // .env의 서버 주소

// 로그인이 완전히 풀렸을 때 실행할 함수를 담는 자리 (처음에는 비어 있음)
// 통신 도구는 앱 시작 때 먼저 만들어지고 로그인 상태는 화면이 그려진 뒤 생겨서, 나중에 연결하려고 자리를 먼저 마련
let authFailureHandler: (() => void) | null = null

export const api = createApiClient({
  baseUrl: API_BASE_URL,
  // 토큰 재발급까지 실패하면 호출, 자리에 든 함수를 실행
  // ?.(): 자리가 비어 있으면(null) 아무 일도 하지 않음
  onAuthFailure: () => authFailureHandler?.(),
})

// 로그인이 풀렸을 때 할 일 등록 (null을 넘기면 등록 해제)
export function setAuthFailureHandler(handler: (() => void) | null) {
  authFailureHandler = handler
}

// 한 파일에서 가져다 쓰도록 다시 내보냄
export { ApiError } from './errors.ts'
export { GOOGLE_LOGIN_URL } from './config.ts'
