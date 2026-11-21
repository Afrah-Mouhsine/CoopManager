import { useState, FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { Leaf, AlertCircle } from 'lucide-react'
import { authApi } from '../api'
import { useAuthStore } from '../store/authStore'

export default function LoginPage() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const { setUser } = useAuthStore()
  const navigate = useNavigate()

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const user = await authApi.login(email, password)
      setUser(user)
      navigate('/dashboard')
    } catch (err: any) {
      setError(err.response?.data?.detail || 'Email ou mot de passe incorrect')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-800 to-primary-600 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-white rounded-full shadow-lg mb-4">
            <Leaf className="w-8 h-8 text-primary-700" />
          </div>
          <h1 className="text-3xl font-bold text-white">CoopManager</h1>
          <p className="text-primary-200 mt-1">Gestion de Coopérative Agricole</p>
        </div>

        <div className="bg-white rounded-2xl shadow-xl p-8">
          <h2 className="text-xl font-semibold text-gray-900 mb-6">Connexion</h2>

          {error && (
            <div className="flex items-center gap-2 bg-red-50 text-red-700 border border-red-200 rounded-lg p-3 mb-4 text-sm">
              <AlertCircle className="w-4 h-4 flex-shrink-0" />
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Adresse email
              </label>
              <input
                type="email"
                value={email}
                onChange={e => setEmail(e.target.value)}
                className="input-field"
                placeholder="votre@email.ma"
                required
                autoFocus
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Mot de passe
              </label>
              <input
                type="password"
                value={password}
                onChange={e => setPassword(e.target.value)}
                className="input-field"
                placeholder="••••••••"
                required
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="btn-primary w-full mt-2"
            >
              {loading ? 'Connexion...' : 'Se connecter'}
            </button>
          </form>

          <div className="mt-6 pt-6 border-t border-gray-100 text-xs text-gray-400 space-y-1">
            <p className="font-medium text-gray-500">Comptes de démonstration :</p>
            <p>Président : president@coopmanager.ma / President@123</p>
            <p>Trésorier : tresorier@coopmanager.ma / Tresorier@123</p>
            <p>Membre : ahmed@coopmanager.ma / Membre@123</p>
          </div>
        </div>
      </div>
    </div>
  )
}
