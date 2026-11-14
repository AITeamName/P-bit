import { useState } from 'react'
import type { SubmitEvent } from 'react'
import { Link as RouterLink, useNavigate } from 'react-router'
import { api, ApiError, GOOGLE_LOGIN_URL } from '../api'
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
  const { signIn } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  async function handleSubmit(e: SubmitEvent<HTMLFormElement>) {
    e.preventDefault()
    if (submitting) return
    setSubmitting(true)
    setErrorMessage(null)
    try {
      const res = await api.post<LoginResponse>('/auth/login', { email, password }, { auth: false })
      signIn({ userId: res.userId, accessToken: res.accessToken, onboardingCompleted: res.onboardingCompleted })
      // 온보딩을 마친 회원의 이동 경로는 정해지지 않아 이동하지 않는다.
      if (!res.onboardingCompleted) navigate('/onboarding', { replace: true })
    } catch (error) {
      if (error instanceof ApiError) setErrorMessage(error.userMessage)
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
              />
              <TextField
                id="password"
                type="password"
                label={t('login.password')}
                placeholder={t('login.passwordPlaceholder')}
                autoComplete="current-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                status={errorMessage ? 'error' : undefined}
                message={errorMessage ?? undefined}
              />
            </div>
          </div>
          <div className="auth-actions">
            <div className="auth-buttons">
              <Button type="submit" disabled={submitting}>
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
