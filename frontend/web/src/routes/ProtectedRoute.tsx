import { Navigate, Outlet } from 'react-router'
import { useAuth } from '../stores/authContext.ts'

// 로그인 확인 중에는 아무것도 그리지 않는다.
// 확인하지 못한 경우(서버 장애)는 전용 화면이 정해질 때까지 로그인 화면으로 보낸다.
export default function ProtectedRoute() {
  const { status } = useAuth()

  if (status === 'loading') return null
  if (status === 'anonymous' || status === 'unavailable') return <Navigate to="/login" replace />
  return <Outlet />
}
