// 앱 최상위 화면 부품 (화면 이동과 로그인 상태를 앱 전체에 제공)
import { BrowserRouter } from 'react-router' // 주소창 기반 화면 이동
import AppRoutes from './routes' // 주소별 화면 표
import { AuthProvider } from './stores/auth.tsx' // 로그인 상태 제공 부품

export default function App() {
  return (
    // 바깥부터 주소 기능 > 로그인 상태 > 주소별 화면 (안쪽 모든 화면에서 두 기능을 쓰기 위한 순서)
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  )
}
