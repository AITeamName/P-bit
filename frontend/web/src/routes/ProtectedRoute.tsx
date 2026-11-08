import { Navigate, Outlet } from 'react-router'
import { useAuth } from '../stores/authContext.ts'

// 로그인 확인 중에는 아무것도 그리지 않는다.
export default function ProtectedRoute() {
  const { status } = useAuth()

  if (status === 'loading') return null
  if (status === 'anonymous') return <Navigate to="/login" replace />
  return <Outlet />
}
