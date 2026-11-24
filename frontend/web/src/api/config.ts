// 서버 주소 설정 (.env의 VITE_API_BASE_URL)

// .env에서 읽은 서버 주소 (없을 수 있어 undefined 포함)
const raw: string | undefined = import.meta.env.VITE_API_BASE_URL
// 기본값 없음: 주소가 없으면 엉뚱한 서버로 요청되지 않게 즉시 중단
if (!raw) throw new Error('VITE_API_BASE_URL이 설정되지 않았습니다. .env.example을 .env로 복사하세요.')

// 끝의 / 제거 (경로를 이어 붙일 때 // 방지)
export const API_BASE_URL = raw.replace(/\/+$/, '')

// Google 로그인 시작 주소는 Base URL(/api/v1) 밖에 있어서 서버 주소를 따로 사용
// origin: 주소에서 http://localhost:8080 같은 서버 부분만 추출
const SERVER_ORIGIN = new URL(API_BASE_URL).origin
export const GOOGLE_LOGIN_URL = `${SERVER_ORIGIN}/oauth2/authorization/google?client=web`
