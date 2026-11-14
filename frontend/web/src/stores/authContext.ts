// 로그인 상태 보관함(Context) 정의 (값은 auth.tsx의 AuthProvider가 채우고, 이 파일은 모양과 꺼내 쓰는 도구만 정의)
import { createContext, useContext } from 'react'

// 로그인 상태 세 가지
//  loading: 확인 중 (앱 시작, 새로고침 직후)
//  authenticated: 로그인됨
//  anonymous: 로그인하지 않음 (서버가 로그인 정보가 없거나 만료됐다고 응답)
//  unavailable: 확인하지 못함 (서버 장애, 네트워크 끊김 등 로그인 여부와 무관한 오류)
export type AuthStatus = 'loading' | 'authenticated' | 'anonymous' | 'unavailable'

// 로그인 성공 시 서버 응답 중 앱이 저장하는 값
export type Session = {
  userId: string // 사용자 번호
  accessToken: string // 로그인 통행증 (이후 요청마다 첨부)
  onboardingCompleted: boolean // 온보딩(첫 설정 과정) 완료 여부
}

// 보관함에 담긴 값
export type AuthContextValue = {
  status: AuthStatus
  userId: string | null // 로그인 전에는 null
  onboardingCompleted: boolean
  signIn: (session: Session) => void // 로그인 성공 시 호출
  signOut: () => void // 로그아웃, 로그인이 풀렸을 때 호출
}

// 보관함 본체 (처음 값은 null, AuthProvider가 실제 값 채움)
export const AuthContext = createContext<AuthContextValue | null>(null)

// 화면에서 로그인 상태를 꺼내 쓰는 도구 (예: const { status } = useAuth())
export function useAuth(): AuthContextValue {
  const value = useContext(AuthContext)
  // AuthProvider 밖에서 쓰면 값이 없음: 조용히 넘어가지 않고 바로 오류
  if (!value) throw new Error('useAuth는 AuthProvider 안에서 써야 합니다')
  return value
}
