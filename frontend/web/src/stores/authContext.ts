import { createContext, useContext } from 'react'

export type AuthStatus = 'loading' | 'authenticated' | 'anonymous'

export type Session = {
  userId: string
  accessToken: string
  onboardingCompleted: boolean
}

export type AuthContextValue = {
  status: AuthStatus
  userId: string | null
  onboardingCompleted: boolean
  signIn: (session: Session) => void
  signOut: () => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)

export function useAuth(): AuthContextValue {
  const value = useContext(AuthContext)
  if (!value) throw new Error('useAuth는 AuthProvider 안에서 써야 한다')
  return value
}
