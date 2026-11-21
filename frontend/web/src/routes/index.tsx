// 주소(URL)별 화면 표
import { Route, Routes } from 'react-router' // Routes: 표 전체, Route: 표의 한 줄
import ProtectedRoute from './ProtectedRoute.tsx' // 로그인해야 볼 수 있게 지키는 문지기
import LoginPage from '../pages/LoginPage.tsx'

// 아직 없는 화면 자리용 빈 부품 (아무것도 그리지 않음)
const Empty = () => null

export default function AppRoutes() {
  return (
    // 현재 주소와 맞는 한 줄만 표시, 표에 없는 주소(예: /)는 빈 화면
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      {/* 약관 동의 화면 (아직 없음) */}
      <Route path="/join/terms" element={<Empty />} />
      {/* 회원가입 화면 (아직 없음) */}
      <Route path="/join" element={<Empty />} />
      {/* Google 신규 가입 화면 (아직 없음) */}
      <Route path="/join/nickname" element={<Empty />} />
      {/* Google 로그인 후 돌아오는 주소 (아직 없음) */}
      <Route path="/auth/callback" element={<Empty />} />
      {/* 주소 없는 Route: 안쪽 경로 전체를 문지기 부품으로 감쌈 (안쪽은 로그인해야 접근) */}
      <Route element={<ProtectedRoute />}>
        <Route path="/onboarding" element={<Empty />} />
      </Route>
    </Routes>
  )
}
