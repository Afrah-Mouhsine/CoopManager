import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { membersApi } from '../api'
import { useAuthStore } from '../store/authStore'
import type { Member, Role } from '../types'
import PageHeader from '../components/PageHeader'
import { Plus, Pencil, UserX, UserCheck, X, Search } from 'lucide-react'

const ROLES: Role[] = ['PRESIDENT', 'TREASURER', 'MEMBER']
const ROLE_LABELS: Record<Role, string> = {
  PRESIDENT: 'Président',
  TREASURER: 'Trésorier',
  MEMBER: 'Membre',
}

const ROLE_COLORS: Record<Role, string> = {
  PRESIDENT: 'bg-purple-100 text-purple-800',
  TREASURER: 'bg-blue-100 text-blue-800',
  MEMBER: 'bg-gray-100 text-gray-700',
}

export default function MembersPage() {
  const { hasRole } = useAuthStore()
  const isPresident = hasRole('PRESIDENT')
  const [showForm, setShowForm] = useState(false)
  const [editingMember, setEditingMember] = useState<Member | null>(null)
  const [search, setSearch] = useState('')
  const [includeInactive, setIncludeInactive] = useState(false)
  const qc = useQueryClient()

  const { data: members = [], isLoading } = useQuery({
    queryKey: ['members', includeInactive],
    queryFn: () => membersApi.list(includeInactive),
  })

  const deactivate = useMutation({
    mutationFn: membersApi.deactivate,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['members'] }),
  })

  const reactivate = useMutation({
    mutationFn: membersApi.reactivate,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['members'] }),
  })

  const filtered = members.filter(m =>
    m.fullName.toLowerCase().includes(search.toLowerCase()) ||
    m.email.toLowerCase().includes(search.toLowerCase()) ||
    m.cultureType.toLowerCase().includes(search.toLowerCase())
  )

  return (
    <div className="p-6 max-w-7xl mx-auto">
      <PageHeader
        title="Registre des Membres"
        subtitle={`${members.filter(m => m.active).length} membres actifs`}
        action={isPresident && (
          <button onClick={() => { setEditingMember(null); setShowForm(true) }} className="btn-primary flex items-center gap-2">
            <Plus className="w-4 h-4" /> Ajouter un membre
          </button>
        )}
      />

      {/* Filters */}
      <div className="flex flex-wrap items-center gap-4 mb-4">
        <div className="relative flex-1 min-w-[200px]">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            type="text"
            value={search}
            onChange={e => setSearch(e.target.value)}
            placeholder="Rechercher un membre..."
            className="input-field pl-9"
          />
        </div>
        {isPresident && (
          <label className="flex items-center gap-2 text-sm text-gray-600 cursor-pointer">
            <input type="checkbox" checked={includeInactive} onChange={e => setIncludeInactive(e.target.checked)} className="rounded" />
            Inclure les membres inactifs
          </label>
        )}
      </div>

      {/* Table */}
      <div className="card p-0 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                {['Nom complet', 'Email', 'Type de culture', 'Rôle', 'Date d\'adhésion', 'Statut', isPresident ? 'Actions' : ''].map(h => (
                  <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {isLoading ? (
                <tr><td colSpan={7} className="text-center py-12 text-gray-400">Chargement...</td></tr>
              ) : filtered.length === 0 ? (
                <tr><td colSpan={7} className="text-center py-12 text-gray-400">Aucun membre trouvé</td></tr>
              ) : filtered.map(member => (
                <tr key={member.id} className={`hover:bg-gray-50 transition-colors ${!member.active ? 'opacity-60' : ''}`}>
                  <td className="px-4 py-3 font-medium text-gray-900">{member.fullName}</td>
                  <td className="px-4 py-3 text-gray-600">{member.email}</td>
                  <td className="px-4 py-3 text-gray-600">{member.cultureType}</td>
                  <td className="px-4 py-3">
                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${ROLE_COLORS[member.role]}`}>
                      {ROLE_LABELS[member.role]}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-gray-600">{new Date(member.joinDate).toLocaleDateString('fr-MA')}</td>
                  <td className="px-4 py-3">
                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${member.active ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-600'}`}>
                      {member.active ? 'Actif' : 'Inactif'}
                    </span>
                  </td>
                  {isPresident && (
                    <td className="px-4 py-3">
                      <div className="flex items-center gap-2">
                        <button
                          onClick={() => { setEditingMember(member); setShowForm(true) }}
                          className="p-1.5 text-gray-500 hover:text-primary-600 hover:bg-primary-50 rounded-lg transition-colors"
                          title="Modifier"
                        >
                          <Pencil className="w-4 h-4" />
                        </button>
                        {member.active ? (
                          <button
                            onClick={() => deactivate.mutate(member.id)}
                            className="p-1.5 text-gray-500 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                            title="Désactiver"
                          >
                            <UserX className="w-4 h-4" />
                          </button>
                        ) : (
                          <button
                            onClick={() => reactivate.mutate(member.id)}
                            className="p-1.5 text-gray-500 hover:text-green-600 hover:bg-green-50 rounded-lg transition-colors"
                            title="Réactiver"
                          >
                            <UserCheck className="w-4 h-4" />
                          </button>
                        )}
                      </div>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal form */}
      {showForm && (
        <MemberFormModal
          member={editingMember}
          onClose={() => { setShowForm(false); setEditingMember(null) }}
          onSaved={() => { setShowForm(false); setEditingMember(null); qc.invalidateQueries({ queryKey: ['members'] }) }}
        />
      )}
    </div>
  )
}

function MemberFormModal({ member, onClose, onSaved }: {
  member: Member | null
  onClose: () => void
  onSaved: () => void
}) {
  const isEdit = !!member
  const [form, setForm] = useState({
    firstName: member?.firstName ?? '',
    lastName: member?.lastName ?? '',
    email: member?.email ?? '',
    password: '',
    phone: member?.phone ?? '',
    address: member?.address ?? '',
    cin: member?.cin ?? '',
    cultureType: member?.cultureType ?? '',
    role: member?.role ?? 'MEMBER' as Role,
    joinDate: member?.joinDate ?? new Date().toISOString().slice(0, 10),
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      if (isEdit) {
        await membersApi.update(member!.id, { ...form, newPassword: form.password || undefined })
      } else {
        await membersApi.create(form)
      }
      onSaved()
    } catch (err: any) {
      setError(err.response?.data?.detail || 'Une erreur est survenue')
    } finally {
      setLoading(false)
    }
  }

  const field = (label: string, name: keyof typeof form, type = 'text', required = false) => (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-1">{label}{required && ' *'}</label>
      <input
        type={type}
        value={form[name] as string}
        onChange={e => setForm(f => ({ ...f, [name]: e.target.value }))}
        className="input-field"
        required={required}
      />
    </div>
  )

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between p-6 border-b">
          <h2 className="text-lg font-semibold">{isEdit ? 'Modifier le membre' : 'Ajouter un membre'}</h2>
          <button onClick={onClose} className="p-2 hover:bg-gray-100 rounded-lg"><X className="w-5 h-5" /></button>
        </div>
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {error && <div className="bg-red-50 text-red-700 border border-red-200 rounded-lg p-3 text-sm">{error}</div>}
          <div className="grid grid-cols-2 gap-4">
            {field('Prénom', 'firstName', 'text', true)}
            {field('Nom', 'lastName', 'text', true)}
            {field('Email', 'email', 'email', true)}
            {field(isEdit ? 'Nouveau mot de passe (laisser vide)' : 'Mot de passe', 'password', 'password', !isEdit)}
            {field('Téléphone', 'phone')}
            {field('CIN', 'cin')}
            {field('Type de culture', 'cultureType', 'text', true)}
            {field("Date d'adhésion", 'joinDate', 'date', true)}
          </div>
          <div className="col-span-2">
            {field('Adresse', 'address')}
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Rôle *</label>
            <select value={form.role} onChange={e => setForm(f => ({ ...f, role: e.target.value as Role }))} className="input-field">
              {ROLES.map(r => <option key={r} value={r}>{ROLE_LABELS[r]}</option>)}
            </select>
          </div>
          <div className="flex gap-3 pt-2">
            <button type="button" onClick={onClose} className="btn-secondary flex-1">Annuler</button>
            <button type="submit" disabled={loading} className="btn-primary flex-1">
              {loading ? 'Enregistrement...' : isEdit ? 'Mettre à jour' : 'Créer le membre'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
