const raw: string | undefined = import.meta.env.VITE_API_BASE_URL
if (!raw) throw new Error('VITE_API_BASE_URL이 설정되지 않았습니다. .env.example을 .env로 복사하세요.')

export const API_BASE_URL = raw.replace(/\/+$/, '')

// Google 로그인 시작 주소는 Base URL(/api/v1) 밖에 있어서 서버 주소를 따로 쓴다.
const SERVER_ORIGIN = new URL(API_BASE_URL).origin
export const GOOGLE_LOGIN_URL = `${SERVER_ORIGIN}/oauth2/authorization/google?client=web`
