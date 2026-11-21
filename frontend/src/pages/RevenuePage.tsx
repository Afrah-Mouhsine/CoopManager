import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { revenueApi } from '../api'
import { useAuthStore } from '../store/authStore'
import type { RevenueDistribution } from '../types'
import PageHeader from '../components/PageHeader'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from 'recharts'
import { Plus, CheckCircle, Trash2, X, Calculator } from 'lucide-react'

const COLORS = ['#166534', '#4d7c0f', '#92400e', '#1d4ed8', '#7c3aed', '#be185d']

export default function RevenuePage() {
  const { user, hasRole } = useAuthStore()
  const canManage = hasRole('PRESIDENT', 'TREASURER')
  const [showForm, setShowForm] = useState(false)
  const [selected, setSelected] = useState<RevenueDistribution | null>(null)
  const qc = useQueryClient()

  const { data: distributions = [], isLoading } = useQuery({
    queryKey: ['revenue', canManage],
    queryFn: () => canManage ? revenueApi.list() : revenueApi.listFinalized(),
  })

  const finalize = useMutation({
    mutationFn: revenueApi.finalize,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['revenue'] }),
  })

  const remove = useMutation({
    mutationFn: revenueApi.delete,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['revenue'] }); setSelected(null) },
  })

  return (
    <div className="p-6 max-w-7xl mx-auto">
      <PageHeader
        title="Répartition des Revenus"
        subtitle="Calcul et simulation de la distribution des bénéfices"
        action={canManage && (
          <button onClick={() => setShowForm(true)} className="btn-primary flex items-center gap-2">
            <Calculator className="w-4 h-4" /> Nouveau calcul
          </button>
        )}
      />

      <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
        {/* List */}
        <div className="xl:col-span-1 space-y-3">
          {isLoading ? (
            <div className="card text-center text-gray-400 py-8">Chargement...</div>
          ) : distributions.length === 0 ? (
            <div className="card text-center text-gray-400 py-8">Aucune répartition</div>
          ) : distributions.map(dist => (
            <button
              key={dist.id}
              onClick={() => setSelected(dist)}
              className={`w-full card text-left transition-all cursor-pointer ${selected?.id === dist.id ? 'ring-2 ring-primary-500' : 'hover:shadow-md'}`}
            >
              <div className="flex items-start justify-between">
                <div>
                  <p className="font-semibold text-gray-900">{dist.periodLabel}</p>
                  <p className="text-xs text-gray-500 mt-0.5">
                    {new Date(dist.periodStart).toLocaleDateString('fr-MA')} – {new Date(dist.periodEnd).toLocaleDateString('fr-MA')}
                  </p>
                </div>
                <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${dist.simulation ? 'bg-yellow-100 text-yellow-700' : 'bg-green-100 text-green-700'}`}>
                  {dist.simulation ? 'Simulation' : 'Finalisé'}
                </span>
              </div>
              <div className="mt-3 grid grid-cols-2 gap-2 text-sm">
                <div>
                  <p className="text-gray-400 text-xs">Revenu net</p>
                  <p className="font-bold text-primary-700">{Number(dist.netRevenue).toLocaleString('fr-MA')} MAD</p>
                </div>
                <div>
                  <p className="text-gray-400 text-xs">Membres</p>
                  <p className="font-medium">{dist.memberShares.length}</p>
                </div>
              </div>
            </button>
          ))}
        </div>

        {/* Detail */}
        <div className="xl:col-span-2">
          {selected ? (
            <DistributionDetail
              distribution={selected}
              canManage={canManage}
              currentMemberId={user!.memberId}
              onFinalize={() => finalize.mutate(selected.id)}
              onDelete={() => { if (confirm('Supprimer cette simulation ?')) remove.mutate(selected.id) }}
            />
          ) : (
            <div className="card flex items-center justify-center h-64 text-gray-400">
              Sélectionnez une répartition pour voir les détails
            </div>
          )}
        </div>
      </div>

      {showForm && canManage && (
        <RevenueFormModal
          onClose={() => setShowForm(false)}
          onSaved={() => { setShowForm(false); qc.invalidateQueries({ queryKey: ['revenue'] }) }}
        />
      )}
    </div>
  )
}

function DistributionDetail({ distribution, canManage, currentMemberId, onFinalize, onDelete }: {
  distribution: RevenueDistribution
  canManage: boolean
  currentMemberId: number
  onFinalize: () => void
  onDelete: () => void
}) {
  const chartData = distribution.memberShares.map(s => ({
    name: s.memberFullName.split(' ')[0],
    amount: Number(s.shareAmount),
    percentage: Number(s.sharePercentage),
  }))

  return (
    <div className="card space-y-6">
      {/* Header */}
      <div className="flex items-start justify-between">
        <div>
          <h2 className="text-xl font-bold text-gray-900">{distribution.periodLabel}</h2>
          <p className="text-sm text-gray-500">
            {new Date(distribution.periodStart).toLocaleDateString('fr-MA')} – {new Date(distribution.periodEnd).toLocaleDateString('fr-MA')}
          </p>
        </div>
        {canManage && (
          <div className="flex gap-2">
            {distribution.simulation && !distribution.finalized && (
              <button onClick={onFinalize} className="btn-primary flex items-center gap-1 text-sm">
                <CheckCircle className="w-4 h-4" /> Finaliser
              </button>
            )}
            {distribution.simulation && (
              <button onClick={onDelete} className="btn-danger flex items-center gap-1 text-sm">
                <Trash2 className="w-4 h-4" /> Supprimer
              </button>
            )}
          </div>
        )}
      </div>

      {/* KPI summary */}
      <div className="grid grid-cols-3 gap-4">
        {[
          { label: 'Revenu total', value: `${Number(distribution.totalRevenue).toLocaleString('fr-MA')} MAD` },
          { label: 'Frais de fonctionnement', value: `${Number(distribution.operatingCosts).toLocaleString('fr-MA')} MAD` },
          { label: 'Revenu net distribuable', value: `${Number(distribution.netRevenue).toLocaleString('fr-MA')} MAD`, highlight: true },
        ].map(({ label, value, highlight }) => (
          <div key={label} className={`rounded-xl p-4 ${highlight ? 'bg-primary-50 border border-primary-200' : 'bg-gray-50'}`}>
            <p className="text-xs text-gray-500">{label}</p>
            <p className={`text-lg font-bold mt-1 ${highlight ? 'text-primary-700' : 'text-gray-900'}`}>{value}</p>
          </div>
        ))}
      </div>

      {/* Bar chart */}
      {chartData.length > 0 && (
        <div>
          <h3 className="text-sm font-semibold text-gray-700 mb-3">Part par membre (MAD)</h3>
          <ResponsiveContainer width="100%" height={180}>
            <BarChart data={chartData} layout="vertical">
              <CartesianGrid strokeDasharray="3 3" horizontal={false} />
              <XAxis type="number" tick={{ fontSize: 11 }} tickFormatter={v => `${(v / 1000).toFixed(0)}k`} />
              <YAxis type="category" dataKey="name" tick={{ fontSize: 11 }} width={70} />
              <Tooltip formatter={(v: number) => `${v.toLocaleString('fr-MA')} MAD`} />
              <Bar dataKey="amount" name="Montant" radius={[0, 4, 4, 0]}>
                {chartData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>
      )}

      {/* Table of shares */}
      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-gray-200">
              <th className="text-left py-2 text-gray-500 font-medium">Membre</th>
              <th className="text-right py-2 text-gray-500 font-medium">Production (kg)</th>
              <th className="text-right py-2 text-gray-500 font-medium">Part (%)</th>
              <th className="text-right py-2 text-gray-500 font-medium">Montant (MAD)</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {distribution.memberShares.map(share => (
              <tr
                key={share.memberId}
                className={`${share.memberId === currentMemberId ? 'bg-primary-50 font-semibold' : ''}`}
              >
                <td className="py-2.5 text-gray-900">
                  {share.memberFullName}
                  {share.memberId === currentMemberId && (
                    <span className="ml-2 text-xs text-primary-600 font-medium">(vous)</span>
                  )}
                </td>
                <td className="py-2.5 text-right text-gray-600">{Number(share.totalProduction).toLocaleString('fr-MA')}</td>
                <td className="py-2.5 text-right text-gray-600">{Number(share.sharePercentage).toFixed(1)}%</td>
                <td className="py-2.5 text-right font-bold text-primary-700">{Number(share.shareAmount).toLocaleString('fr-MA')}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

function RevenueFormModal({ onClose, onSaved }: { onClose: () => void; onSaved: () => void }) {
  const [form, setForm] = useState({
    periodLabel: '',
    periodStart: '',
    periodEnd: '',
    totalRevenue: '',
    operatingCosts: '',
    simulation: true,
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await revenueApi.calculate({
        ...form,
        totalRevenue: Number(form.totalRevenue),
        operatingCosts: Number(form.operatingCosts),
      })
      onSaved()
    } catch (err: any) {
      setError(err.response?.data?.detail || 'Une erreur est survenue')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-lg">
        <div className="flex items-center justify-between p-6 border-b">
          <h2 className="text-lg font-semibold">Calculer la répartition</h2>
          <button onClick={onClose} className="p-2 hover:bg-gray-100 rounded-lg"><X className="w-5 h-5" /></button>
        </div>
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {error && <div className="bg-red-50 text-red-700 border border-red-200 rounded-lg p-3 text-sm">{error}</div>}

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Libellé de la période *</label>
            <input type="text" value={form.periodLabel}
              onChange={e => setForm(f => ({ ...f, periodLabel: e.target.value }))}
              className="input-field" placeholder="Ex: Campagne Olive 2025" required />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Début période *</label>
              <input type="date" value={form.periodStart}
                onChange={e => setForm(f => ({ ...f, periodStart: e.target.value }))}
                className="input-field" required />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Fin période *</label>
              <input type="date" value={form.periodEnd}
                onChange={e => setForm(f => ({ ...f, periodEnd: e.target.value }))}
                className="input-field" required />
            </div>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Revenu total (MAD) *</label>
              <input type="number" step="0.01" min="0" value={form.totalRevenue}
                onChange={e => setForm(f => ({ ...f, totalRevenue: e.target.value }))}
                className="input-field" required />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Frais de fonctionnement (MAD) *</label>
              <input type="number" step="0.01" min="0" value={form.operatingCosts}
                onChange={e => setForm(f => ({ ...f, operatingCosts: e.target.value }))}
                className="input-field" required />
            </div>
          </div>
          <label className="flex items-center gap-2 cursor-pointer">
            <input type="checkbox" checked={form.simulation}
              onChange={e => setForm(f => ({ ...f, simulation: e.target.checked }))}
              className="rounded" />
            <span className="text-sm text-gray-700">Mode simulation (peut être modifié avant finalisation)</span>
          </label>
          <div className="flex gap-3 pt-2">
            <button type="button" onClick={onClose} className="btn-secondary flex-1">Annuler</button>
            <button type="submit" disabled={loading} className="btn-primary flex-1">
              {loading ? 'Calcul...' : 'Calculer'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
