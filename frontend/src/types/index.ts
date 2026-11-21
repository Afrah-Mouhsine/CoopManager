export type Role = 'PRESIDENT' | 'TREASURER' | 'MEMBER'

export interface AuthUser {
  memberId: number
  fullName: string
  email: string
  role: Role
  accessToken: string
  refreshToken: string
}

export interface Member {
  id: number
  firstName: string
  lastName: string
  fullName: string
  email: string
  phone?: string
  address?: string
  cin?: string
  cultureType: string
  joinDate: string
  leaveDate?: string
  role: Role
  active: boolean
  createdAt: string
}

export type ValidationStatus = 'PENDING' | 'VALIDATED' | 'REJECTED'

export interface ProductionEntry {
  id: number
  memberId: number
  memberFullName: string
  productType: string
  quantityKg: number
  harvestDate: string
  lotNumber?: string
  notes?: string
  status: ValidationStatus
  validatedByName?: string
  validatedAt?: string
  createdAt: string
}

export interface ProductionDashboard {
  totalValidatedKg: number
  pendingCount: number
  byProductType: Record<string, number>
  monthlyTrend: Array<{ month: number; monthLabel: string; totalKg: number }>
}

export type TransactionType = 'INCOME' | 'EXPENSE'
export type TransactionCategory =
  | 'SALE' | 'SUBSIDY' | 'OTHER_INCOME'
  | 'INPUTS' | 'LOGISTICS' | 'EQUIPMENT' | 'SALARY' | 'MAINTENANCE' | 'OTHER_EXPENSE'

export interface FinancialTransaction {
  id: number
  type: TransactionType
  category: TransactionCategory
  description: string
  amount: number
  transactionDate: string
  reference?: string
  notes?: string
  recordedByName: string
  createdAt: string
}

export interface FinancialSummary {
  totalIncome: number
  totalExpenses: number
  netBalance: number
  expensesByCategory: Record<string, number>
  monthlyTrend: Array<{
    month: number
    monthLabel: string
    income: number
    expenses: number
    net: number
  }>
}

export interface MemberShare {
  memberId: number
  memberFullName: string
  totalProduction: number
  sharePercentage: number
  shareAmount: number
}

export interface RevenueDistribution {
  id: number
  periodLabel: string
  periodStart: string
  periodEnd: string
  totalRevenue: number
  operatingCosts: number
  netRevenue: number
  simulation: boolean
  finalized: boolean
  createdByName?: string
  createdAt: string
  memberShares: MemberShare[]
}

export interface MeetingMinutes {
  id: number
  title: string
  meetingDate: string
  meetingType: string
  agenda?: string
  content: string
  decisions?: string
  attendeesCount?: number
  published: boolean
  publishedByName?: string
  publishedAt?: string
  createdAt: string
}
