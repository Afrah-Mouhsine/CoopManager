import { Outlet, NavLink, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'
import {
  LayoutDashboard, Users, Sprout, DollarSign, TrendingUp, FileText, LogOut, Leaf
} from 'lucide-react'
import clsx from 'clsx'

const navItems = [
  { to: '/dashboard', label: 'Tableau de bord', icon: LayoutDashboard, roles: ['PRESIDENT', 'TREASURER', 'MEMBER'] },
  { to: '/members', label: 'Membres', icon: Users, roles: ['PRESIDENT', 'TREASURER', 'MEMBER'] },
  { to: '/production', label: 'Production', icon: Sprout, roles: ['PRESIDENT', 'TREASURER', 'MEMBER'] },
  { to: '/revenue', label: 'Répartition', icon: TrendingUp, roles: ['PRESIDENT', 'TREASURER', 'MEMBER'] },
  { to: '/finance', label: 'Finances', icon: DollarSign, roles: ['PRESIDENT', 'TREASURER'] },
  { to: '/minutes', label: 'Procès-verbaux', icon: FileText, roles: ['PRESIDENT', 'TREASURER', 'MEMBER'] },
]

export default function Layout() {
  const { user, logout } = useAuthStore()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const allowedNavItems = navItems.filter(item =>
    user && item.roles.includes(user.role)
  )

  return (
    <div className="flex h-screen bg-gray-50">
      {/* Sidebar */}
      <aside className="w-64 bg-primary-800 text-white flex flex-col shadow-xl">
        {/* Logo */}
        <div className="p-6 border-b border-primary-700">
          <div className="flex items-center gap-3">
            <Leaf className="w-8 h-8 text-primary-300" />
            <div>
              <h1 className="font-bold text-lg leading-tight">CoopManager</h1>
              <p className="text-xs text-primary-300">Coopérative Agricole</p>
            </div>
          </div>
        </div>

        {/* Navigation */}
        <nav className="flex-1 py-4 overflow-y-auto">
          {allowedNavItems.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                clsx(
                  'flex items-center gap-3 px-6 py-3 text-sm font-medium transition-colors',
                  isActive
                    ? 'bg-primary-600 text-white border-r-4 border-primary-300'
                    : 'text-primary-200 hover:bg-primary-700 hover:text-white'
                )
              }
            >
              <Icon className="w-5 h-5 flex-shrink-0" />
              {label}
            </NavLink>
          ))}
        </nav>

        {/* User info + logout */}
        <div className="p-4 border-t border-primary-700">
          <div className="mb-3">
            <p className="text-sm font-medium text-white">{user?.fullName}</p>
            <p className="text-xs text-primary-300">{user?.role}</p>
          </div>
          <button
            onClick={handleLogout}
            className="flex items-center gap-2 text-primary-200 hover:text-white text-sm transition-colors w-full"
          >
            <LogOut className="w-4 h-4" />
            Se déconnecter
          </button>
        </div>
      </aside>

      {/* Main content */}
      <main className="flex-1 overflow-y-auto">
        <Outlet />
      </main>
    </div>
  )
}
