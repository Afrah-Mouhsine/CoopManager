import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { useAuthStore } from './store/authStore'
import Layout from './components/Layout'
import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'
import MembersPage from './pages/MembersPage'
import ProductionPage from './pages/ProductionPage'
import RevenuePage from './pages/RevenuePage'
import FinancePage from './pages/FinancePage'
import MinutesPage from './pages/MinutesPage'

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const isAuthenticated = useAuthStore(s => s.isAuthenticated())
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <Layout />
            </ProtectedRoute>
          }
        >
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<DashboardPage />} />
          <Route path="members" element={<MembersPage />} />
          <Route path="production" element={<ProductionPage />} />
          <Route path="revenue" element={<RevenuePage />} />
          <Route path="finance" element={<FinancePage />} />
          <Route path="minutes" element={<MinutesPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
