import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { minutesApi } from '../api'
import { useAuthStore } from '../store/authStore'
import type { MeetingMinutes } from '../types'
import PageHeader from '../components/PageHeader'
import { Plus, BookOpen, Send, Trash2, Pencil, X, Calendar } from 'lucide-react'

export default function MinutesPage() {
  const { hasRole } = useAuthStore()
  const isPresident = hasRole('PRESIDENT')
  const [showForm, setShowForm] = useState(false)
  const [editingMinutes, setEditingMinutes] = useState<MeetingMinutes | null>(null)
  const [selected, setSelected] = useState<MeetingMinutes | null>(null)
  const qc = useQueryClient()

  const { data: minutesList = [], isLoading } = useQuery({
    queryKey: ['minutes'],
    queryFn: () => minutesApi.list(),
  })

  const publish = useMutation({
    mutationFn: minutesApi.publish,
    onSuccess: (updated) => {
      qc.invalidateQueries({ queryKey: ['minutes'] })
      setSelected(updated)
    },
  })

  const remove = useMutation({
    mutationFn: minutesApi.delete,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['minutes'] }); setSelected(null) },
  })

  return (
    <div className="p-6 max-w-7xl mx-auto">
      <PageHeader
        title="Procès-verbaux"
        subtitle="Archives des assemblées générales et réunions"
        action={isPresident && (
          <button onClick={() => { setEditingMinutes(null); setShowForm(true) }} className="btn-primary flex items-center gap-2">
            <Plus className="w-4 h-4" /> Nouveau PV
          </button>
        )}
      />

      <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
        {/* List */}
        <div className="xl:col-span-1 space-y-2">
          {isLoading ? (
            <div className="card text-center text-gray-400 py-8">Chargement...</div>
          ) : minutesList.length === 0 ? (
            <div className="card text-center text-gray-400 py-8">Aucun PV disponible</div>
          ) : minutesList.map(m => (
            <button
              key={m.id}
              onClick={() => setSelected(m)}
              className={`w-full card text-left transition-all cursor-pointer p-4 ${selected?.id === m.id ? 'ring-2 ring-primary-500' : 'hover:shadow-md'}`}
            >
              <div className="flex items-start justify-between gap-2">
                <div className="flex items-center gap-2">
                  <BookOpen className="w-4 h-4 text-primary-600 flex-shrink-0" />
                  <p className="font-medium text-gray-900 text-sm line-clamp-1">{m.title}</p>
                </div>
                <span className={`text-xs px-2 py-0.5 rounded-full flex-shrink-0 font-medium ${m.published ? 'bg-green-100 text-green-700' : 'bg-yellow-100 text-yellow-700'}`}>
                  {m.published ? 'Publié' : 'Brouillon'}
                </span>
              </div>
              <div className="flex items-center gap-1 mt-2 text-xs text-gray-500">
                <Calendar className="w-3 h-3" />
                {new Date(m.meetingDate).toLocaleDateString('fr-MA', { day: 'numeric', month: 'long', year: 'numeric' })}
                <span className="mx-1">·</span>
                {m.meetingType}
              </div>
            </button>
          ))}
        </div>

        {/* Detail */}
        <div className="xl:col-span-2">
          {selected ? (
            <div className="card space-y-4">
              <div className="flex items-start justify-between">
                <div>
                  <h2 className="text-xl font-bold text-gray-900">{selected.title}</h2>
                  <p className="text-sm text-gray-500 mt-1">
                    {selected.meetingType} — {new Date(selected.meetingDate).toLocaleDateString('fr-MA', { day: 'numeric', month: 'long', year: 'numeric' })}
                    {selected.attendeesCount && ` — ${selected.attendeesCount} participants`}
                  </p>
                  {selected.published && selected.publishedByName && (
                    <p className="text-xs text-green-600 mt-1">
                      Publié par {selected.publishedByName}
                      {selected.publishedAt && ` le ${new Date(selected.publishedAt).toLocaleDateString('fr-MA')}`}
                    </p>
                  )}
                </div>
                {isPresident && (
                  <div className="flex gap-2">
                    {!selected.published && (
                      <>
                        <button
                          onClick={() => { setEditingMinutes(selected); setShowForm(true) }}
                          className="p-2 text-gray-500 hover:text-primary-600 hover:bg-primary-50 rounded-lg"
                          title="Modifier"
                        >
                          <Pencil className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => publish.mutate(selected.id)}
                          className="btn-primary flex items-center gap-1 text-sm"
                        >
                          <Send className="w-4 h-4" /> Publier
                        </button>
                        <button
                          onClick={() => { if (confirm('Supprimer ce PV ?')) remove.mutate(selected.id) }}
                          className="p-2 text-gray-500 hover:text-red-600 hover:bg-red-50 rounded-lg"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </>
                    )}
                  </div>
                )}
              </div>

              {selected.agenda && (
                <div>
                  <h3 className="text-sm font-semibold text-gray-700 mb-1">Ordre du jour</h3>
                  <div className="bg-gray-50 rounded-lg p-3 text-sm text-gray-700 whitespace-pre-wrap">{selected.agenda}</div>
                </div>
              )}

              <div>
                <h3 className="text-sm font-semibold text-gray-700 mb-1">Compte-rendu</h3>
                <div className="bg-gray-50 rounded-lg p-3 text-sm text-gray-700 whitespace-pre-wrap">{selected.content}</div>
              </div>

              {selected.decisions && (
                <div>
                  <h3 className="text-sm font-semibold text-gray-700 mb-1">Décisions prises</h3>
                  <div className="bg-primary-50 border border-primary-100 rounded-lg p-3 text-sm text-gray-700 whitespace-pre-wrap">{selected.decisions}</div>
                </div>
              )}
            </div>
          ) : (
            <div className="card flex items-center justify-center h-64 text-gray-400">
              Sélectionnez un PV pour lire son contenu
            </div>
          )}
        </div>
      </div>

      {showForm && isPresident && (
        <MinutesFormModal
          minutes={editingMinutes}
          onClose={() => { setShowForm(false); setEditingMinutes(null) }}
          onSaved={() => { setShowForm(false); setEditingMinutes(null); qc.invalidateQueries({ queryKey: ['minutes'] }) }}
        />
      )}
    </div>
  )
}

function MinutesFormModal({ minutes, onClose, onSaved }: {
  minutes: MeetingMinutes | null
  onClose: () => void
  onSaved: () => void
}) {
  const isEdit = !!minutes
  const [form, setForm] = useState({
    title: minutes?.title ?? '',
    meetingDate: minutes?.meetingDate ?? new Date().toISOString().slice(0, 10),
    meetingType: minutes?.meetingType ?? 'Assemblée Générale',
    agenda: minutes?.agenda ?? '',
    content: minutes?.content ?? '',
    decisions: minutes?.decisions ?? '',
    attendeesCount: minutes?.attendeesCount?.toString() ?? '',
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const payload = { ...form, attendeesCount: form.attendeesCount ? Number(form.attendeesCount) : undefined }
      if (isEdit) {
        await minutesApi.update(minutes!.id, payload)
      } else {
        await minutesApi.create(payload)
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
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between p-6 border-b">
          <h2 className="text-lg font-semibold">{isEdit ? 'Modifier le PV' : 'Nouveau procès-verbal'}</h2>
          <button onClick={onClose} className="p-2 hover:bg-gray-100 rounded-lg"><X className="w-5 h-5" /></button>
        </div>
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {error && <div className="bg-red-50 text-red-700 border border-red-200 rounded-lg p-3 text-sm">{error}</div>}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Titre *</label>
            <input type="text" value={form.title}
              onChange={e => setForm(f => ({ ...f, title: e.target.value }))}
              className="input-field" required />
          </div>
          <div className="grid grid-cols-3 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Date *</label>
              <input type="date" value={form.meetingDate}
                onChange={e => setForm(f => ({ ...f, meetingDate: e.target.value }))}
                className="input-field" required />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Type de réunion</label>
              <select value={form.meetingType}
                onChange={e => setForm(f => ({ ...f, meetingType: e.target.value }))}
                className="input-field">
                {['Assemblée Générale', 'Réunion du Bureau', 'Réunion Extraordinaire'].map(t => (
                  <option key={t} value={t}>{t}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Nb participants</label>
              <input type="number" min="1" value={form.attendeesCount}
                onChange={e => setForm(f => ({ ...f, attendeesCount: e.target.value }))}
                className="input-field" />
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Ordre du jour</label>
            <textarea value={form.agenda} onChange={e => setForm(f => ({ ...f, agenda: e.target.value }))}
              className="input-field resize-none" rows={3} />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Compte-rendu *</label>
            <textarea value={form.content} onChange={e => setForm(f => ({ ...f, content: e.target.value }))}
              className="input-field resize-none" rows={6} required />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Décisions prises</label>
            <textarea value={form.decisions} onChange={e => setForm(f => ({ ...f, decisions: e.target.value }))}
              className="input-field resize-none" rows={3} />
          </div>
          <div className="flex gap-3 pt-2">
            <button type="button" onClick={onClose} className="btn-secondary flex-1">Annuler</button>
            <button type="submit" disabled={loading} className="btn-primary flex-1">
              {loading ? 'Enregistrement...' : isEdit ? 'Mettre à jour' : 'Créer le PV'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
