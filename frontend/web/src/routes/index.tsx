import { Route, Routes } from 'react-router'
import ProtectedRoute from './ProtectedRoute.tsx'
import LoginPage from '../pages/LoginPage.tsx'

const Empty = () => null

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/join/terms" element={<Empty />} />
      <Route path="/join" element={<Empty />} />
      <Route path="/join/nickname" element={<Empty />} />
      <Route path="/auth/callback" element={<Empty />} />
      <Route element={<ProtectedRoute />}>
        <Route path="/onboarding" element={<Empty />} />
      </Route>
    </Routes>
  )
}
