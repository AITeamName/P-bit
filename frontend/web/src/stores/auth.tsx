import { useCallback, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { api, ApiError, setAuthFailureHandler } from '../api'
import { AuthContext } from './authContext.ts'
import type { AuthStatus, Session } from './authContext.ts'

type MeResponse = { userId?: string; onboardingCompleted?: boolean }

export function AuthProvider({ children }: { children: ReactNode }) {
  const [status, setStatus] = useState<AuthStatus>('loading')
  const [userId, setUserId] = useState<string | null>(null)
  const [onboardingCompleted, setOnboardingCompleted] = useState(false)

  const signIn = useCallback((session: Session) => {
    api.setAccessToken(session.accessToken)
    setUserId(session.userId)
    setOnboardingCompleted(session.onboardingCompleted)
    setStatus('authenticated')
  }, [])

  const signOut = useCallback(() => {
    api.clearAccessToken()
    setUserId(null)
    setOnboardingCompleted(false)
    setStatus('anonymous')
  }, [])

  useEffect(() => {
    setAuthFailureHandler(signOut)
    return () => setAuthFailureHandler(null)
  }, [signOut])

  // 새로고침하면 메모리의 토큰이 사라지므로 refresh 쿠키로 다시 로그인 상태를 만든다.
  useEffect(() => {
    let cancelled = false
    ;(async () => {
      try {
        await api.refreshSession()
        const me = await api.get<MeResponse>('/users/me')
        if (cancelled) return
        setUserId(me.userId ?? null)
        setOnboardingCompleted(me.onboardingCompleted === true)
        setStatus('authenticated')
      } catch (error) {
        if (cancelled) return
        if (error instanceof ApiError && error.status === 401) {
          signOut()
        } else {
          // 서버 장애, 네트워크 끊김은 로그아웃과 구분하고 원인을 남긴다.
          console.error('로그인 상태를 확인하지 못했습니다', error)
          setStatus('unavailable')
        }
      }
    })()
    return () => {
      cancelled = true
    }
  }, [signOut])

  const value = useMemo(
    () => ({ status, userId, onboardingCompleted, signIn, signOut }),
    [status, userId, onboardingCompleted, signIn, signOut],
  )
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
