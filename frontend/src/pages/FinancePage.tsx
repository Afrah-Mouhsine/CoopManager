import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { financeApi } from '../api'
import type { FinancialTransaction, TransactionType, TransactionCategory } from '../types'
import PageHeader from '../components/PageHeader'
import { Plus, Pencil, Trash2, X, Download, TrendingUp, TrendingDown } from 'lucide-react'

const CATEGORY_LABELS: Record<TransactionCategory, string> = {
  SALE: 'Vente', SUBSIDY: 'Subvention', OTHER_INCOME: 'Autre revenu',
  INPUTS: 'Intrants', LOGISTICS: 'Logistique', EQUIPMENT: 'Équipement',
  SALARY: 'Salaires', MAINTENANCE: 'Maintenance', OTHER_EXPENSE: 'Autre dépense',
}

const INCOME_CATEGORIES: TransactionCategory[] = ['SALE', 'SUBSIDY', 'OTHER_INCOME']
const EXPENSE_CATEGORIES: TransactionCategory[] = ['INPUTS', 'LOGISTICS', 'EQUIPMENT', 'SALARY', 'MAINTENANCE', 'OTHER_EXPENSE']

export default function FinancePage() {
  const [showForm, setShowForm] = useState(false)
  const [editingTx, setEditingTx] = useState<FinancialTransaction | null>(null)
  const [filterType, setFilterType] = useState<'ALL' | TransactionType>('ALL')
  const [fromDate, setFromDate] = useState(new Date().toISOString().slice(0, 7) + '-01')
  const [toDate, setToDate] = useState(new Date().toISOString().slice(0, 10))
  const qc = useQueryClient()
  const year = new Date().getFullYear()

  const { data: transactions = [], isLoading } = useQuery({
    queryKey: ['finance-transactions', fromDate, toDate],
    queryFn: () => financeApi.list(fromDate, toDate),
  })

  const { data: summary } = useQuery({
    queryKey: ['finance-summary', year],
    queryFn: () => financeApi.summary(year),
  })

  const remove = useMutation({
    mutationFn: financeApi.delete,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['finance'] }),
  })

  const handleExport = async () => {
    const response = await financeApi.exportExcel(fromDate, toDate)
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.download = `rapport-financier-${fromDate}-${toDate}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
  }

  const filtered = filterType === 'ALL' ? transactions : transactions.filter(t => t.type === filterType)
  const totalIncome = transactions.filter(t => t.type === 'INCOME').reduce((s, t) => s + Number(t.amount), 0)
  const totalExpense = transactions.filter(t => t.type === 'EXPENSE').reduce((s, t) => s + Number(t.amount), 0)

  return (
    <div className="p-6 max-w-7xl mx-auto">
      <PageHeader
        title="Gestion Financière"
        subtitle="Livre de caisse numérique"
        action={
          <div className="flex gap-2">
            <button onClick={handleExport} className="btn-secondary flex items-center gap-2">
              <Download className="w-4 h-4" /> Exporter Excel
            </button>
            <button onClick={() => { setEditingTx(null); setShowForm(true) }} className="btn-primary flex items-center gap-2">
              <Plus className="w-4 h-4" /> Nouvelle écriture
            </button>
          </div>
        }
      />

      {/* Summary KPIs */}
      <div className="grid grid-cols-3 gap-4 mb-6">
        <div className="card bg-green-50 border-green-200">
          <div className="flex items-center gap-3">
            <TrendingUp className="w-6 h-6 text-green-600" />
            <div>
              <p className="text-xs text-gray-500">Revenus (période)</p>
              <p className="text-xl font-bold text-green-700">{totalIncome.toLocaleString('fr-MA')} MAD</p>
            </div>
          </div>
        </div>
        <div className="card bg-red-50 border-red-200">
          <div className="flex items-center gap-3">
            <TrendingDown className="w-6 h-6 text-red-600" />
            <div>
              <p className="text-xs text-gray-500">Dépenses (période)</p>
              <p className="text-xl font-bold text-red-700">{totalExpense.toLocaleString('fr-MA')} MAD</p>
            </div>
          </div>
        </div>
        <div className={`card ${(totalIncome - totalExpense) >= 0 ? 'bg-primary-50 border-primary-200' : 'bg-orange-50 border-orange-200'}`}>
          <div className="flex items-center gap-3">
            <div className="w-6 h-6 text-primary-600 font-bold text-lg">Σ</div>
            <div>
              <p className="text-xs text-gray-500">Solde net (période)</p>
              <p className={`text-xl font-bold ${(totalIncome - totalExpense) >= 0 ? 'text-primary-700' : 'text-orange-700'}`}>
                {(totalIncome - totalExpense).toLocaleString('fr-MA')} MAD
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Filters */}
      <div className="flex flex-wrap gap-3 mb-4">
        <div className="flex gap-2">
          {(['ALL', 'INCOME', 'EXPENSE'] as const).map(type => (
            <button
              key={type}
              onClick={() => setFilterType(type)}
              className={`px-3 py-1.5 rounded-lg text-sm font-medium transition-colors ${filterType === type ? 'bg-primary-600 text-white' : 'bg-white text-gray-600 border border-gray-200 hover:bg-gray-50'}`}
            >
              {type === 'ALL' ? 'Tous' : type === 'INCOME' ? 'Revenus' : 'Dépenses'}
            </button>
          ))}
        </div>
        <div className="flex items-center gap-2 ml-auto">
          <input type="date" value={fromDate} onChange={e => setFromDate(e.target.value)} className="input-field w-auto" />
          <span className="text-gray-500">–</span>
          <input type="date" value={toDate} onChange={e => setToDate(e.target.value)} className="input-field w-auto" />
        </div>
      </div>

      <div className="card p-0 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                {['Date', 'Type', 'Catégorie', 'Description', 'Référence', 'Montant (MAD)', 'Saisi par', 'Actions'].map(h => (
                  <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {isLoading ? (
                <tr><td colSpan={8} className="text-center py-12 text-gray-400">Chargement...</td></tr>
              ) : filtered.length === 0 ? (
                <tr><td colSpan={8} className="text-center py-12 text-gray-400">Aucune transaction</td></tr>
              ) : filtered.map(tx => (
                <tr key={tx.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-4 py-3 text-gray-600">{new Date(tx.transactionDate).toLocaleDateString('fr-MA')}</td>
                  <td className="px-4 py-3">
                    <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${tx.type === 'INCOME' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
                      {tx.type === 'INCOME' ? 'Revenu' : 'Dépense'}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-gray-600">{CATEGORY_LABELS[tx.category]}</td>
                  <td className="px-4 py-3 text-gray-900 max-w-xs truncate">{tx.description}</td>
                  <td className="px-4 py-3 text-gray-500">{tx.reference || '—'}</td>
                  <td className={`px-4 py-3 font-bold ${tx.type === 'INCOME' ? 'text-green-700' : 'text-red-700'}`}>
                    {tx.type === 'INCOME' ? '+' : '–'}{Number(tx.amount).toLocaleString('fr-MA')}
                  </td>
                  <td className="px-4 py-3 text-gray-500 text-xs">{tx.recordedByName}</td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-1">
                      <button
                        onClick={() => { setEditingTx(tx); setShowForm(true) }}
                        className="p-1.5 text-gray-500 hover:text-primary-600 hover:bg-primary-50 rounded-lg"
                      >
                        <Pencil className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => { if (confirm('Supprimer cette écriture ?')) remove.mutate(tx.id) }}
                        className="p-1.5 text-gray-500 hover:text-red-600 hover:bg-red-50 rounded-lg"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {showForm && (
        <TransactionFormModal
          transaction={editingTx}
          onClose={() => { setShowForm(false); setEditingTx(null) }}
          onSaved={() => { setShowForm(false); setEditingTx(null); qc.invalidateQueries({ queryKey: ['finance-transactions'] }) }}
        />
      )}
    </div>
  )
}

function TransactionFormModal({ transaction, onClose, onSaved }: {
  transaction: FinancialTransaction | null
  onClose: () => void
  onSaved: () => void
}) {
  const isEdit = !!transaction
  const [form, setForm] = useState({
    type: transaction?.type ?? 'INCOME' as TransactionType,
    category: transaction?.category ?? 'SALE' as TransactionCategory,
    description: transaction?.description ?? '',
    amount: transaction?.amount?.toString() ?? '',
    transactionDate: transaction?.transactionDate ?? new Date().toISOString().slice(0, 10),
    reference: transaction?.reference ?? '',
    notes: transaction?.notes ?? '',
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const categories = form.type === 'INCOME' ? INCOME_CATEGORIES : EXPENSE_CATEGORIES

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      if (isEdit) {
        await financeApi.update(transaction!.id, { ...form, amount: Number(form.amount) })
      } else {
        await financeApi.create({ ...form, amount: Number(form.amount) })
      }
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
          <h2 className="text-lg font-semibold">{isEdit ? 'Modifier l\'écriture' : 'Nouvelle écriture'}</h2>
          <button onClick={onClose} className="p-2 hover:bg-gray-100 rounded-lg"><X className="w-5 h-5" /></button>
        </div>
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {error && <div className="bg-red-50 text-red-700 border border-red-200 rounded-lg p-3 text-sm">{error}</div>}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Type *</label>
            <div className="flex gap-3">
              {(['INCOME', 'EXPENSE'] as const).map(t => (
                <button key={t} type="button"
                  onClick={() => setForm(f => ({ ...f, type: t, category: t === 'INCOME' ? 'SALE' : 'INPUTS' }))}
                  className={`flex-1 py-2 rounded-lg text-sm font-medium border transition-colors ${form.type === t
                    ? t === 'INCOME' ? 'bg-green-600 text-white border-green-600' : 'bg-red-600 text-white border-red-600'
                    : 'border-gray-200 text-gray-600 hover:bg-gray-50'}`}>
                  {t === 'INCOME' ? 'Revenu' : 'Dépense'}
                </button>
              ))}
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Catégorie *</label>
            <select value={form.category}
              onChange={e => setForm(f => ({ ...f, category: e.target.value as TransactionCategory }))}
              className="input-field">
              {categories.map(c => <option key={c} value={c}>{CATEGORY_LABELS[c]}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Description *</label>
            <input type="text" value={form.description}
              onChange={e => setForm(f => ({ ...f, description: e.target.value }))}
              className="input-field" required />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Montant (MAD) *</label>
              <input type="number" step="0.01" min="0" value={form.amount}
                onChange={e => setForm(f => ({ ...f, amount: e.target.value }))}
                className="input-field" required />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Date *</label>
              <input type="date" value={form.transactionDate}
                onChange={e => setForm(f => ({ ...f, transactionDate: e.target.value }))}
                className="input-field" required />
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Référence</label>
            <input type="text" value={form.reference}
              onChange={e => setForm(f => ({ ...f, reference: e.target.value }))}
              className="input-field" placeholder="Ex: VTE-2025-001" />
          </div>
          <div className="flex gap-3 pt-2">
            <button type="button" onClick={onClose} className="btn-secondary flex-1">Annuler</button>
            <button type="submit" disabled={loading} className="btn-primary flex-1">
              {loading ? 'Enregistrement...' : isEdit ? 'Mettre à jour' : 'Enregistrer'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
