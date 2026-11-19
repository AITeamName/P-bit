import { useState } from 'react'
import type { SubmitEvent } from 'react'
import { Link as RouterLink, useNavigate } from 'react-router'
import { api, ApiError, getUserMessage, GOOGLE_LOGIN_URL } from '../api'
import { Button, Header, TextField } from '../components'
import Text from '../i18n/Text.tsx'
import { t } from '../i18n/messages.ts'
import { useAuth } from '../stores/authContext.ts'
import './LoginPage.css'

type LoginResponse = {
  userId: string
  accessToken: string
  expiresIn: number
  onboardingCompleted: boolean
}

export default function LoginPage() {
  const { status, signIn } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [submitting, setSubmitting] = useState(false)
  // 서버가 알려 준 입력칸별 오류 문구 (없으면 undefined)
  const [emailError, setEmailError] = useState<string | undefined>(undefined)
  const [passwordError, setPasswordError] = useState<string | undefined>(undefined)

  async function handleSubmit(e: SubmitEvent<HTMLFormElement>) {
    e.preventDefault()
    // 시작 로그인 확인 중에 로그인하면 늦게 도착한 확인 결과가 새 로그인의 토큰과 쿠키를 덮어쓴다
    if (status === 'loading') return
    setSubmitting(true)
    setEmailError(undefined)
    setPasswordError(undefined)
    try {
      const res = await api.post<LoginResponse>('/auth/login', { email, password }, { auth: false })
      signIn({ userId: res.userId, accessToken: res.accessToken, onboardingCompleted: res.onboardingCompleted })
      // 온보딩을 마친 회원의 이동 경로는 정해지지 않아 이동하지 않는다.
      if (!res.onboardingCompleted) navigate('/onboarding', { replace: true })
    } catch (error) {
      // ApiError가 아니면 입력칸별 오류가 없으므로 빈 목록
      const fieldErrors = error instanceof ApiError ? error.fieldErrors : []
      const emailReason = fieldErrors.find((f) => f.field === 'email')?.reason
      const passwordReason = fieldErrors.find((f) => f.field === 'password')?.reason
      if (emailReason !== undefined || passwordReason !== undefined) {
        setEmailError(emailReason)
        setPasswordError(passwordReason)
      } else {
        // 입력칸에 해당하지 않는 오류는 비밀번호 칸 아래에 표시
        setPasswordError(getUserMessage(error, t('error.fallback')))
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="auth-page">
      <Header />
      <main className="auth-main">
        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          <div className="auth-top">
            <h1 className="auth-title">
              <Text id="login.title" />
            </h1>
            <div className="auth-fields">
              <TextField
                id="email"
                type="email"
                label={t('login.email')}
                placeholder={t('login.emailPlaceholder')}
                autoComplete="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                status={emailError ? 'error' : undefined}
                message={emailError}
              />
              <TextField
                id="password"
                type="password"
                label={t('login.password')}
                placeholder={t('login.passwordPlaceholder')}
                autoComplete="current-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                status={passwordError ? 'error' : undefined}
                message={passwordError}
              />
            </div>
          </div>
          <div className="auth-actions">
            <div className="auth-buttons">
              <Button type="submit" disabled={submitting || status === 'loading'}>
                <Text id="login.submit" />
              </Button>
              <div className="auth-or">
                <Text id="login.or" />
              </div>
              <button
                type="button"
                className="google-btn"
                onClick={() => window.location.assign(GOOGLE_LOGIN_URL)}
              >
                <img src="/icons/google.svg" alt="" />
                <Text id="login.google" />
              </button>
            </div>
            <div className="auth-signup">
              <span>
                <Text id="login.noAccount" />
              </span>
              <RouterLink className="link" to="/join/terms">
                <Text id="login.signup" />
              </RouterLink>
            </div>
          </div>
        </form>
      </main>
    </div>
  )
}
