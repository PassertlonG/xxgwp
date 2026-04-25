import { create } from 'zustand'
import type { UserInfo } from '@/types/api'

interface AppState {
  user: UserInfo | null
  isLoggedIn: boolean
  setUser: (user: UserInfo | null) => void
  logout: () => void
}

export const useAppStore = create<AppState>((set) => ({
  user: null,
  isLoggedIn: false,
  setUser: (user) => set({ user, isLoggedIn: user !== null }),
  logout: () => set({ user: null, isLoggedIn: false }),
}))
