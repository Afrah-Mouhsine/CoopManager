import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { productionApi, membersApi } from '../api'
import { useAuthStore } from '../store/authStore'
import type { ProductionEntry } from '../types'
import PageHeader from '../components/PageHeader'
import StatusBadge from '../components/StatusBadge'
import { Plus, CheckCircle, XCircle, Trash2, X } from 'lucide-react'

export default function ProductionPage() {
  const { user, hasRole } = useAuthStore()
  const canValidate = hasRole('PRESIDENT', 'TREASURER')
  const [showForm, setShowForm] = useState(false)
  const [filterStatus, setFilterStatus] = useState<string>('ALL')
  const qc = useQueryClient()

  const { data: entries = [], isLoading } = useQuery({
    queryKey: ['production'],
    queryFn: () => productionApi.list(),
  })

  const validate = useMutation({
    mutationFn: productionApi.validate,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['production'] }),
  })
  const reject = useMutation({
    mutationFn: productionApi.reject,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['production'] }),
  })
  const remove = useMutation({
    mutationFn: productionApi.delete,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['production'] }),
  })

  const filtered = filterStatus === 'ALL'
    ? entries
    : entries.filter(e => e.status === filterStatus)

  const total = filtered.reduce((acc, e) => acc + Number(e.quantityKg), 0)

  return (
    <div className="p-6 max-w-7xl mx-auto">
      <PageHeader
        title="Suivi de la Production"
        subtitle={`${filtered.length} entrées · Total : ${total.toLocaleString('fr-MA')} kg`}
        action={
          <button onClick={() => setShowForm(true)} className="btn-primary flex items-center gap-2">
            <Plus className="w-4 h-4" /> Enregistrer une récolte
          </button>
        }
      />

      {/* Status filter tabs */}
      <div className="flex gap-2 mb-4">
        {['ALL', 'PENDING', 'VALIDATED', 'REJECTED'].map(status => (
          <button
            key={status}
            onClick={() => setFilterStatus(status)}
            className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${filterStatus === status
              ? 'bg-primary-600 text-white'
              : 'bg-white text-gray-600 border border-gray-200 hover:bg-gray-50'
              }`}
          >
            {status === 'ALL' ? 'Tous' : status === 'PENDING' ? 'En attente' : status === 'VALIDATED' ? 'Validés' : 'Rejetés'}
            {' '}
            <span className="ml-1 text-xs opacity-75">
              ({entries.filter(e => status === 'ALL' || e.status === status).length})
            </span>
          </button>
        ))}
      </div>

      <div className="card p-0 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                {['Membre', 'Produit', 'Quantité (kg)', 'Date récolte', 'N° Lot', 'Statut', 'Validé par', 'Actions'].map(h => (
                  <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {isLoading ? (
                <tr><td colSpan={8} className="text-center py-12 text-gray-400">Chargement...</td></tr>
              ) : filtered.length === 0 ? (
                <tr><td colSpan={8} className="text-center py-12 text-gray-400">Aucune entrée</td></tr>
              ) : filtered.map(entry => (
                <tr key={entry.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-4 py-3 font-medium text-gray-900">{entry.memberFullName}</td>
                  <td className="px-4 py-3 text-gray-600">{entry.productType}</td>
                  <td className="px-4 py-3 font-medium">{Number(entry.quantityKg).toLocaleString('fr-MA')}</td>
                  <td className="px-4 py-3 text-gray-600">{new Date(entry.harvestDate).toLocaleDateString('fr-MA')}</td>
                  <td className="px-4 py-3 text-gray-500">{entry.lotNumber || '—'}</td>
                  <td className="px-4 py-3"><StatusBadge status={entry.status} /></td>
                  <td className="px-4 py-3 text-gray-500 text-xs">{entry.validatedByName || '—'}</td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-1">
                      {canValidate && entry.status === 'PENDING' && (
                        <>
                          <button
                            onClick={() => validate.mutate(entry.id)}
                            className="p-1.5 text-green-600 hover:bg-green-50 rounded-lg"
                            title="Valider"
                          >
                            <CheckCircle className="w-4 h-4" />
                          </button>
                          <button
                            onClick={() => reject.mutate(entry.id)}
                            className="p-1.5 text-red-600 hover:bg-red-50 rounded-lg"
                            title="Rejeter"
                          >
                            <XCircle className="w-4 h-4" />
                          </button>
                        </>
                      )}
                      {canValidate && entry.status === 'PENDING' && (
                        <button
                          onClick={() => { if (confirm('Supprimer cette entrée ?')) remove.mutate(entry.id) }}
                          className="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg"
                          title="Supprimer"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {showForm && (
        <ProductionFormModal
          currentMemberId={user!.memberId}
          onClose={() => setShowForm(false)}
          onSaved={() => { setShowForm(false); qc.invalidateQueries({ queryKey: ['production'] }) }}
        />
      )}
    </div>
  )
}

function ProductionFormModal({ currentMemberId, onClose, onSaved }: {
  currentMemberId: number
  onClose: () => void
  onSaved: () => void
}) {
  const { hasRole } = useAuthStore()
  const canSelectMember = hasRole('PRESIDENT', 'TREASURER')

  const { data: members = [] } = useQuery({
    queryKey: ['members'],
    queryFn: () => membersApi.list(),
    enabled: canSelectMember,
  })

  const [form, setForm] = useState({
    memberId: currentMemberId,
    productType: '',
    quantityKg: '',
    harvestDate: new Date().toISOString().slice(0, 10),
    lotNumber: '',
    notes: '',
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await productionApi.create({
        ...form,
        memberId: Number(form.memberId),
        quantityKg: Number(form.quantityKg),
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
          <h2 className="text-lg font-semibold">Enregistrer une récolte</h2>
          <button onClick={onClose} className="p-2 hover:bg-gray-100 rounded-lg"><X className="w-5 h-5" /></button>
        </div>
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {error && <div className="bg-red-50 text-red-700 border border-red-200 rounded-lg p-3 text-sm">{error}</div>}

          {canSelectMember && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Membre *</label>
              <select
                value={form.memberId}
                onChange={e => setForm(f => ({ ...f, memberId: Number(e.target.value) }))}
                className="input-field" required
              >
                {members.filter(m => m.active).map(m => (
                  <option key={m.id} value={m.id}>{m.fullName}</option>
                ))}
              </select>
            </div>
          )}

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Type de produit *</label>
            <input type="text" value={form.productType}
              onChange={e => setForm(f => ({ ...f, productType: e.target.value }))}
              className="input-field" placeholder="Ex: Olive, Argan..." required />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Quantité (kg) *</label>
              <input type="number" step="0.001" min="0" value={form.quantityKg}
                onChange={e => setForm(f => ({ ...f, quantityKg: e.target.value }))}
                className="input-field" required />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Date de récolte *</label>
              <input type="date" value={form.harvestDate}
                onChange={e => setForm(f => ({ ...f, harvestDate: e.target.value }))}
                className="input-field" required />
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">N° de lot</label>
            <input type="text" value={form.lotNumber}
              onChange={e => setForm(f => ({ ...f, lotNumber: e.target.value }))}
              className="input-field" placeholder="Ex: LOT-2025-001" />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Notes</label>
            <textarea value={form.notes}
              onChange={e => setForm(f => ({ ...f, notes: e.target.value }))}
              className="input-field resize-none" rows={2} />
          </div>

          <div className="flex gap-3 pt-2">
            <button type="button" onClick={onClose} className="btn-secondary flex-1">Annuler</button>
            <button type="submit" disabled={loading} className="btn-primary flex-1">
              {loading ? 'Enregistrement...' : 'Enregistrer'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
