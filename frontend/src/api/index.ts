import api from './client'
import type {
  AuthUser, Member, ProductionEntry, ProductionDashboard,
  FinancialTransaction, FinancialSummary, RevenueDistribution, MeetingMinutes
} from '../types'

// Auth
export const authApi = {
  login: (email: string, password: string) =>
    api.post<AuthUser>('/auth/login', { email, password }).then(r => r.data),
}

// Members
export const membersApi = {
  list: (includeInactive = false) =>
    api.get<Member[]>(`/members?includeInactive=${includeInactive}`).then(r => r.data),
  get: (id: number) => api.get<Member>(`/members/${id}`).then(r => r.data),
  create: (data: Partial<Member> & { password: string }) =>
    api.post<Member>('/members', data).then(r => r.data),
  update: (id: number, data: Partial<Member> & { newPassword?: string }) =>
    api.put<Member>(`/members/${id}`, data).then(r => r.data),
  deactivate: (id: number) => api.patch(`/members/${id}/deactivate`),
  reactivate: (id: number) => api.patch(`/members/${id}/reactivate`),
}

// Production
export const productionApi = {
  list: (from?: string, to?: string) => {
    const params = new URLSearchParams()
    if (from) params.set('from', from)
    if (to) params.set('to', to)
    return api.get<ProductionEntry[]>(`/production?${params}`).then(r => r.data)
  },
  listByMember: (memberId: number) =>
    api.get<ProductionEntry[]>(`/production/member/${memberId}`).then(r => r.data),
  listPending: () => api.get<ProductionEntry[]>('/production/pending').then(r => r.data),
  create: (data: {
    memberId: number; productType: string; quantityKg: number
    harvestDate: string; lotNumber?: string; notes?: string
  }) => api.post<ProductionEntry>('/production', data).then(r => r.data),
  validate: (id: number) => api.patch<ProductionEntry>(`/production/${id}/validate`).then(r => r.data),
  reject: (id: number) => api.patch<ProductionEntry>(`/production/${id}/reject`).then(r => r.data),
  delete: (id: number) => api.delete(`/production/${id}`),
  dashboard: (year: number) =>
    api.get<ProductionDashboard>(`/production/dashboard?year=${year}`).then(r => r.data),
}

// Finance
export const financeApi = {
  list: (from?: string, to?: string) => {
    const params = new URLSearchParams()
    if (from) params.set('from', from)
    if (to) params.set('to', to)
    return api.get<FinancialTransaction[]>(`/finance/transactions?${params}`).then(r => r.data)
  },
  create: (data: Partial<FinancialTransaction>) =>
    api.post<FinancialTransaction>('/finance/transactions', data).then(r => r.data),
  update: (id: number, data: Partial<FinancialTransaction>) =>
    api.put<FinancialTransaction>(`/finance/transactions/${id}`, data).then(r => r.data),
  delete: (id: number) => api.delete(`/finance/transactions/${id}`),
  summary: (year: number) =>
    api.get<FinancialSummary>(`/finance/summary?year=${year}`).then(r => r.data),
  exportExcel: (from: string, to: string) =>
    api.get(`/finance/export/excel?from=${from}&to=${to}`, { responseType: 'blob' }),
}

// Revenue
export const revenueApi = {
  list: () => api.get<RevenueDistribution[]>('/revenue').then(r => r.data),
  listFinalized: () => api.get<RevenueDistribution[]>('/revenue/finalized').then(r => r.data),
  get: (id: number) => api.get<RevenueDistribution>(`/revenue/${id}`).then(r => r.data),
  myShare: (id: number) => api.get<RevenueDistribution>(`/revenue/${id}/my-share`).then(r => r.data),
  calculate: (data: {
    periodLabel: string; periodStart: string; periodEnd: string
    totalRevenue: number; operatingCosts: number; simulation: boolean
  }) => api.post<RevenueDistribution>('/revenue/calculate', data).then(r => r.data),
  finalize: (id: number) => api.patch<RevenueDistribution>(`/revenue/${id}/finalize`).then(r => r.data),
  delete: (id: number) => api.delete(`/revenue/${id}`),
}

// Minutes
export const minutesApi = {
  list: () => api.get<MeetingMinutes[]>('/minutes').then(r => r.data),
  get: (id: number) => api.get<MeetingMinutes>(`/minutes/${id}`).then(r => r.data),
  create: (data: Partial<MeetingMinutes>) =>
    api.post<MeetingMinutes>('/minutes', data).then(r => r.data),
  update: (id: number, data: Partial<MeetingMinutes>) =>
    api.put<MeetingMinutes>(`/minutes/${id}`, data).then(r => r.data),
  publish: (id: number) => api.patch<MeetingMinutes>(`/minutes/${id}/publish`).then(r => r.data),
  delete: (id: number) => api.delete(`/minutes/${id}`),
}
