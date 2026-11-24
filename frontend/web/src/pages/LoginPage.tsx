// 로그인 화면 (이메일/비밀번호 로그인, Google로 계속, 회원가입 이동)
import { useState } from 'react' // 화면에서 바뀌는 값(입력 글자 등)을 저장하는 React 도구
import type { SubmitEvent } from 'react' // 폼 제출 이벤트 타입
import { Link as RouterLink, useNavigate } from 'react-router' // 화면 이동용 링크와 이동 함수
import { api, ApiError, getUserMessage, GOOGLE_LOGIN_URL } from '../api' // 서버 통신 도구, 오류 종류와 문구 도구, Google 로그인 주소
import { Button, Header, TextField } from '../components' // 공통 화면 부품
import Text from '../i18n/Text.tsx' // 화면 문구 표시 ({br} 줄바꿈 적용)
import { t } from '../i18n/messages.ts' // 글자만 필요한 곳(placeholder 등)의 문구
import { useAuth } from '../stores/authContext.ts' // 로그인 상태
import './LoginPage.css'

// 로그인 성공 시 서버가 돌려주는 값의 모양
type LoginResponse = {
  userId: string
  accessToken: string
  expiresIn: number
  onboardingCompleted: boolean
}

export default function LoginPage() {
  const { status, signIn } = useAuth() // status: 시작 로그인 확인 상태, signIn: 로그인 성공 시 상태에 저장하는 함수
  const navigate = useNavigate() // 코드에서 다른 화면으로 이동하는 함수
  // 입력값과 화면 상태 (바뀌면 화면이 다시 그려짐)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [submitting, setSubmitting] = useState(false) // 서버에 요청 중인지
  // 서버가 알려 준 입력칸별 오류 문구 (없으면 undefined)
  const [emailError, setEmailError] = useState<string | undefined>(undefined)
  const [passwordError, setPasswordError] = useState<string | undefined>(undefined)

  // 로그인 버튼 클릭, 입력창에서 Enter 시 실행
  async function handleSubmit(e: SubmitEvent<HTMLFormElement>) {
    e.preventDefault() // 폼 제출로 화면이 새로고침되는 브라우저 기본 동작 방지
    // 시작 로그인 확인 중에는 제출 무시
    // (확인 결과가 늦게 도착해 방금 로그인한 계정의 토큰과 쿠키를 덮어쓰는 문제 방지)
    if (status === 'loading') return
    setSubmitting(true)
    // 이전 오류 문구 삭제
    setEmailError(undefined)
    setPasswordError(undefined)
    try {
      // 서버에 로그인 요청 (아직 통행증이 없어서 auth: false로 토큰을 붙이지 않음)
      const res = await api.post<LoginResponse>('/auth/login', { email, password }, { auth: false })
      // 성공하면 받은 값을 로그인 상태에 저장
      signIn({ userId: res.userId, accessToken: res.accessToken, onboardingCompleted: res.onboardingCompleted })
      // 온보딩을 마친 회원의 이동 경로는 미정이라 이동하지 않음
      if (!res.onboardingCompleted) navigate('/onboarding', { replace: true })
    } catch (error) {
      // ApiError가 아니면 입력칸별 오류가 없으므로 빈 목록
      const fieldErrors = error instanceof ApiError ? error.fieldErrors : []
      const emailReason = fieldErrors.find((f) => f.field === 'email')?.reason
      const passwordReason = fieldErrors.find((f) => f.field === 'password')?.reason
      if (emailReason !== undefined || passwordReason !== undefined) {
        // 서버가 칸별로 알려 준 오류는 해당 칸 아래에 표시
        setEmailError(emailReason)
        setPasswordError(passwordReason)
      } else {
        // 입력칸에 해당하지 않는 오류는 비밀번호 칸 아래에 표시 (서버 문구가 없으면 기본 문구)
        setPasswordError(getUserMessage(error, t('error.fallback')))
      }
    } finally {
      // 성공이든 실패든 요청이 끝나면 버튼을 다시 누를 수 있게 복구
      setSubmitting(false)
    }
  }

  return (
    <div className="auth-page">
      <Header />
      <main className="auth-main">
        {/* noValidate: 브라우저 기본 입력 검사 말풍선 끔 (검사는 서버 응답으로 처리) */}
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
                autoComplete="email" // 브라우저가 저장된 이메일을 제안하도록 허용
                value={email}
                onChange={(e) => setEmail(e.target.value)} // 입력할 때마다 상태에 저장
                // 이메일 칸에 대한 오류가 있을 때만 오류 모양(빨간 테두리)과 문구
                status={emailError ? 'error' : undefined}
                message={emailError}
              />
              <TextField
                id="password"
                type="password"
                label={t('login.password')}
                placeholder={t('login.passwordPlaceholder')}
                autoComplete="current-password" // 브라우저가 저장된 비밀번호를 제안하도록 허용
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                // 비밀번호 칸에 대한 오류가 있을 때만 오류 모양과 문구
                status={passwordError ? 'error' : undefined}
                message={passwordError}
              />
            </div>
          </div>
          <div className="auth-actions">
            <div className="auth-buttons">
              {/* type="submit": 누르면 위 form의 handleSubmit 실행, 요청 중이거나 시작 확인 중에는 누를 수 없음 */}
              <Button type="submit" disabled={submitting || status === 'loading'}>
                <Text id="login.submit" />
              </Button>
              <div className="auth-or">
                <Text id="login.or" />
              </div>
              <button
                type="button"
                className="google-btn"
                // 서버의 Google 로그인 시작 주소로 이동 (서버가 Google 화면으로 다시 보냄)
                onClick={() => window.location.assign(GOOGLE_LOGIN_URL)}
              >
                {/* alt="": 옆에 "Google로 계속" 글자가 있어서 로고 이미지는 읽어 줄 필요 없음 */}
                <img src="/icons/google.svg" alt="" />
                <Text id="login.google" />
              </button>
            </div>
            <div className="auth-signup">
              <span>
                <Text id="login.noAccount" />
              </span>
              {/* 앱 안에서 화면만 바꾸는 링크 (페이지 전체를 새로 불러오지 않음) */}
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
