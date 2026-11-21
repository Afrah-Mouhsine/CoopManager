import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { AuthUser } from '../types'

interface AuthState {
  user: AuthUser | null
  setUser: (user: AuthUser) => void
  logout: () => void
  isAuthenticated: () => boolean
  hasRole: (...roles: AuthUser['role'][]) => boolean
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      setUser: (user) => set({ user }),
      logout: () => set({ user: null }),
      isAuthenticated: () => get().user !== null,
      hasRole: (...roles) => {
        const user = get().user
        return user !== null && roles.includes(user.role)
      },
    }),
    { name: 'coop-auth' }
  )
)
