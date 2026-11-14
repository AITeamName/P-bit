import { BrowserRouter } from 'react-router'
import AppRoutes from './routes'
import { AuthProvider } from './stores/auth.tsx'

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  )
}
