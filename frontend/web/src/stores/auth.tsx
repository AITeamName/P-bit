// 로그인 상태 제공 부품 (상태 보관, 로그인/로그아웃 처리, 앱 시작 시 로그인 복구)
import { useCallback, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { api, ApiError, setAuthFailureHandler } from '../api'
import { AuthContext } from './authContext.ts'
import type { AuthStatus, Session } from './authContext.ts'

// 서버 "내 정보 조회" 응답에서 읽는 값 (?: 없을 수 있음)
type MeResponse = { userId?: string; onboardingCompleted?: boolean }

// children: 안쪽 화면들 (이 화면들이 로그인 상태를 사용)
export function AuthProvider({ children }: { children: ReactNode }) {
  // 바뀌면 화면이 다시 그려지는 값
  const [status, setStatus] = useState<AuthStatus>('loading') // 처음에는 확인 중
  const [userId, setUserId] = useState<string | null>(null)
  const [onboardingCompleted, setOnboardingCompleted] = useState(false)

  // 로그인 성공 시 호출
  // useCallback: 함수를 매번 새로 만들지 않고 재사용 ([]: 처음 한 번만 생성)
  const signIn = useCallback((session: Session) => {
    api.setAccessToken(session.accessToken) // 이후 요청에 자동 첨부되도록 통신 도구에 저장
    setUserId(session.userId)
    setOnboardingCompleted(session.onboardingCompleted)
    setStatus('authenticated')
  }, [])

  // 로그아웃, 로그인이 풀렸을 때 호출 (저장한 값 모두 비움)
  const signOut = useCallback(() => {
    api.clearAccessToken()
    setUserId(null)
    setOnboardingCompleted(false)
    setStatus('anonymous')
  }, [])

  // 재발급까지 실패하면 signOut을 실행하도록 통신 도구에 등록 (로그인이 풀리면 로그인 화면으로 보내는 연결)
  useEffect(() => {
    setAuthFailureHandler(signOut)
    return () => setAuthFailureHandler(null) // 화면에서 사라질 때 등록 해제
  }, [signOut])

  // 새로고침하면 메모리의 토큰이 사라져서 refresh 쿠키로 로그인 상태 복구
  useEffect(() => {
    // 확인 도중 취소됐는지 표시 (화면이 사라지거나 효과가 다시 시작되면 true)
    let cancelled = false
    // 즉시 실행하는 비동기 함수 (useEffect 안에서 async를 직접 못 써서 감쌈)
    // 맨 앞의 ;: 이 프로젝트는 줄 끝에 ;를 쓰지 않아서 필요
    // 없으면 "(" 로 시작하는 이 줄이 위 줄(let cancelled = false)과 한 문장으로 이어져 오류
    // 그래서 "(" 로 시작하는 줄은 맨 앞에 ;를 붙여 위 줄과 분리
    ;(async () => {
      try {
        await api.refreshSession() // 쿠키로 새 통행증 발급 (실패하면 아래 catch)
        const me = await api.get<MeResponse>('/users/me') // 받은 통행증으로 내 정보 조회
        if (cancelled) return // 그 사이 취소됐으면 중단 (사라진 화면의 상태를 바꾸는 오류 방지)
        // 주의: 응답에 이 값이 없어도 오류 없이 null/false로 넘어감
        setUserId(me.userId ?? null)
        setOnboardingCompleted(me.onboardingCompleted === true)
        setStatus('authenticated')
      } catch (error) {
        if (cancelled) return
        if (error instanceof ApiError && error.status === 401) {
          // 쿠키가 없거나 만료: 로그인하지 않음
          signOut()
        } else {
          // 서버 장애, 네트워크 끊김 등: 로그아웃과 구분하고 원인을 콘솔에 남김
          console.error('로그인 상태를 확인하지 못했습니다', error)
          setStatus('unavailable')
        }
      }
    })()
    // 정리 함수: 화면이 사라질 때 취소 표시
    // 개발 모드에서는 효과가 두 번 실행되어, 먼저 실행된 쪽의 결과가 반영되지 않게 하려고 필요
    return () => {
      cancelled = true
    }
  }, [signOut])

  // 보관함에 넣을 값 묶음 (useMemo: 안의 값이 바뀔 때만 새로 생성)
  // 매번 새로 만들면 안쪽 화면 전체가 불필요하게 다시 그려짐
  const value = useMemo(
    () => ({ status, userId, onboardingCompleted, signIn, signOut }),
    [status, userId, onboardingCompleted, signIn, signOut],
  )
  // 보관함에 값을 넣어 안쪽 화면(children)이 꺼내 쓰게 함
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
