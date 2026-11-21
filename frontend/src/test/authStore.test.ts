import { describe, it, expect, beforeEach } from 'vitest'
import { useAuthStore } from '../store/authStore'
import type { AuthUser } from '../types'

const mockUser: AuthUser = {
  memberId: 1,
  fullName: 'Mohammed Ouziad',
  email: 'president@coopmanager.ma',
  role: 'PRESIDENT',
  accessToken: 'access_token_here',
  refreshToken: 'refresh_token_here',
}

describe('authStore', () => {
  beforeEach(() => {
    useAuthStore.getState().logout()
  })

  it('should start with no user', () => {
    expect(useAuthStore.getState().user).toBeNull()
    expect(useAuthStore.getState().isAuthenticated()).toBe(false)
  })

  it('should set user on login', () => {
    useAuthStore.getState().setUser(mockUser)
    expect(useAuthStore.getState().user).toEqual(mockUser)
    expect(useAuthStore.getState().isAuthenticated()).toBe(true)
  })

  it('should clear user on logout', () => {
    useAuthStore.getState().setUser(mockUser)
    useAuthStore.getState().logout()
    expect(useAuthStore.getState().user).toBeNull()
    expect(useAuthStore.getState().isAuthenticated()).toBe(false)
  })

  it('should return true for matching role', () => {
    useAuthStore.getState().setUser(mockUser)
    expect(useAuthStore.getState().hasRole('PRESIDENT')).toBe(true)
    expect(useAuthStore.getState().hasRole('TREASURER')).toBe(false)
    expect(useAuthStore.getState().hasRole('PRESIDENT', 'TREASURER')).toBe(true)
  })

  it('should return false for all roles when not authenticated', () => {
    expect(useAuthStore.getState().hasRole('PRESIDENT')).toBe(false)
    expect(useAuthStore.getState().hasRole('MEMBER')).toBe(false)
  })

  it('should handle TREASURER role correctly', () => {
    useAuthStore.getState().setUser({ ...mockUser, role: 'TREASURER' })
    expect(useAuthStore.getState().hasRole('TREASURER')).toBe(true)
    expect(useAuthStore.getState().hasRole('PRESIDENT')).toBe(false)
    expect(useAuthStore.getState().hasRole('PRESIDENT', 'TREASURER')).toBe(true)
  })
})
