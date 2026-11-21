import { useQuery } from '@tanstack/react-query'
import {
  LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, PieChart, Pie, Cell, Legend
} from 'recharts'
import { productionApi, financeApi, membersApi } from '../api'
import { useAuthStore } from '../store/authStore'
import PageHeader from '../components/PageHeader'
import { Users, Sprout, DollarSign, TrendingUp } from 'lucide-react'

const COLORS = ['#166534', '#4d7c0f', '#92400e', '#1d4ed8', '#7c3aed', '#be185d']

export default function DashboardPage() {
  const { user } = useAuthStore()
  const year = new Date().getFullYear()

  const { data: prodDashboard } = useQuery({
    queryKey: ['production-dashboard', year],
    queryFn: () => productionApi.dashboard(year),
  })

  const { data: financeSummary } = useQuery({
    queryKey: ['finance-summary', year],
    queryFn: () => financeApi.summary(year),
    enabled: user?.role !== 'MEMBER',
  })

  const { data: members } = useQuery({
    queryKey: ['members'],
    queryFn: () => membersApi.list(),
  })

  const pieData = prodDashboard?.byProductType
    ? Object.entries(prodDashboard.byProductType).map(([name, value]) => ({ name, value }))
    : []

  return (
    <div className="p-6 max-w-7xl mx-auto">
      <PageHeader
        title="Tableau de bord"
        subtitle={`Bienvenue, ${user?.fullName} — ${new Date().toLocaleDateString('fr-MA', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}`}
      />

      {/* KPI cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4 mb-6">
        <StatCard
          icon={<Users className="w-6 h-6 text-primary-600" />}
          label="Membres actifs"
          value={members?.length ?? '—'}
          bg="bg-primary-50"
        />
        <StatCard
          icon={<Sprout className="w-6 h-6 text-green-600" />}
          label="Production validée (kg)"
          value={prodDashboard ? Number(prodDashboard.totalValidatedKg).toLocaleString('fr-MA') : '—'}
          bg="bg-green-50"
        />
        <StatCard
          icon={<Sprout className="w-6 h-6 text-yellow-600" />}
          label="Dépôts en attente"
          value={prodDashboard?.pendingCount ?? '—'}
          bg="bg-yellow-50"
        />
        {user?.role !== 'MEMBER' && (
          <StatCard
            icon={<DollarSign className="w-6 h-6 text-blue-600" />}
            label="Solde net (MAD)"
            value={financeSummary
              ? Number(financeSummary.netBalance).toLocaleString('fr-MA', { style: 'currency', currency: 'MAD' })
              : '—'}
            bg="bg-blue-50"
          />
        )}
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-2 gap-6 mb-6">
        {/* Monthly production chart */}
        <div className="card">
          <h3 className="text-base font-semibold text-gray-900 mb-4">
            Production mensuelle {year} (kg)
          </h3>
          <ResponsiveContainer width="100%" height={240}>
            <BarChart data={prodDashboard?.monthlyTrend ?? []}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="monthLabel" tick={{ fontSize: 12 }} />
              <YAxis tick={{ fontSize: 12 }} />
              <Tooltip formatter={(v: number) => `${v.toLocaleString('fr-MA')} kg`} />
              <Bar dataKey="totalKg" name="Production (kg)" fill="#166534" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* Product type pie */}
        <div className="card">
          <h3 className="text-base font-semibold text-gray-900 mb-4">
            Répartition par produit
          </h3>
          {pieData.length > 0 ? (
            <ResponsiveContainer width="100%" height={240}>
              <PieChart>
                <Pie data={pieData} cx="50%" cy="50%" outerRadius={90} dataKey="value"
                  label={({ name, percent }) => `${name} (${(percent * 100).toFixed(0)}%)`}
                  labelLine={false}>
                  {pieData.map((_, i) => (
                    <Cell key={i} fill={COLORS[i % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip formatter={(v: number) => `${v.toLocaleString('fr-MA')} kg`} />
              </PieChart>
            </ResponsiveContainer>
          ) : (
            <p className="text-sm text-gray-400 text-center py-12">Aucune donnée disponible</p>
          )}
        </div>
      </div>

      {/* Financial monthly trend (treasurer / president only) */}
      {user?.role !== 'MEMBER' && financeSummary && (
        <div className="card">
          <h3 className="text-base font-semibold text-gray-900 mb-4">
            Revenus vs Dépenses {year} (MAD)
          </h3>
          <ResponsiveContainer width="100%" height={240}>
            <LineChart data={financeSummary.monthlyTrend}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="monthLabel" tick={{ fontSize: 12 }} />
              <YAxis tick={{ fontSize: 12 }} />
              <Tooltip formatter={(v: number) => `${v.toLocaleString('fr-MA')} MAD`} />
              <Legend />
              <Line type="monotone" dataKey="income" name="Revenus" stroke="#16a34a" strokeWidth={2} dot={false} />
              <Line type="monotone" dataKey="expenses" name="Dépenses" stroke="#dc2626" strokeWidth={2} dot={false} />
              <Line type="monotone" dataKey="net" name="Solde net" stroke="#2563eb" strokeWidth={2} strokeDasharray="5 5" dot={false} />
            </LineChart>
          </ResponsiveContainer>
        </div>
      )}
    </div>
  )
}

function StatCard({ icon, label, value, bg }: {
  icon: React.ReactNode; label: string; value: string | number; bg: string
}) {
  return (
    <div className={`card flex items-center gap-4 ${bg}`}>
      <div className="p-3 bg-white rounded-xl shadow-sm">{icon}</div>
      <div>
        <p className="text-sm text-gray-500">{label}</p>
        <p className="text-2xl font-bold text-gray-900">{value}</p>
      </div>
    </div>
  )
}
