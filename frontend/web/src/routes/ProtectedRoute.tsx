// 로그인해야 볼 수 있는 화면의 문지기 부품
import { Navigate, Outlet } from 'react-router' // Navigate: 다른 주소로 보내기, Outlet: 안쪽 화면이 들어올 자리
import { useAuth } from '../stores/authContext.ts'

// 로그인 확인 중에는 아무것도 그리지 않음
// (확인 전에 로그인 화면으로 보내면 로그인한 사용자도 새로고침마다 튕김)
export default function ProtectedRoute() {
  const { status } = useAuth()

  if (status === 'loading') return null // 확인 중: 대기
  // 로그인하지 않음, 또는 서버 장애로 확인 불가: 로그인 화면으로 이동
  // 확인 불가 상태의 전용 화면은 정해질 때까지 임시로 로그인 화면 사용
  // replace: 뒤로 가기 기록에 현재 주소를 남기지 않음 (없으면 뒤로 가기를 눌러도 계속 튕김)
  if (status === 'anonymous' || status === 'unavailable') return <Navigate to="/login" replace />
  return <Outlet /> // 로그인됨: 안쪽 화면 표시
}
