import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';
import Layout from './components/Layout';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import Dashboard from './pages/Dashboard';
import WarehousesPage from './pages/WarehousesPage';
import ProductsPage from './pages/ProductsPage';
import InventoryPage from './pages/InventoryPage';
import MovementsPage from './pages/MovementsPage';
import ReservationsPage from './pages/ReservationsPage';
import ReplenishmentPage from './pages/ReplenishmentPage';
import TransfersPage from './pages/TransfersPage';
import ReportsPage from './pages/ReportsPage';
import EmployeesPage from './pages/EmployeesPage';

function ProtectedRoute({ children, adminOnly, rolesAllowed }) {
  const { user, isAdmin } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (adminOnly && !isAdmin()) return <Navigate to="/" replace />;
  if (rolesAllowed && !rolesAllowed.some(r => user.roles?.includes(r))) return <Navigate to="/" replace />;
  return <Layout>{children}</Layout>;
}

function AppRoutes() {
  const { user } = useAuth();
  return (
    <Routes>
      <Route path="/login" element={user ? <Navigate to="/" replace /> : <LoginPage />} />
      <Route path="/register" element={user ? <Navigate to="/" replace /> : <RegisterPage />} />
      <Route path="/"             element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
      <Route path="/warehouses"   element={<ProtectedRoute><WarehousesPage /></ProtectedRoute>} />
      <Route path="/products"     element={<ProtectedRoute><ProductsPage /></ProtectedRoute>} />
      <Route path="/inventory"    element={<ProtectedRoute><InventoryPage /></ProtectedRoute>} />
      <Route path="/movements"    element={<ProtectedRoute><MovementsPage /></ProtectedRoute>} />
      <Route path="/transfers"    element={<ProtectedRoute><TransfersPage /></ProtectedRoute>} />
      <Route path="/reservations" element={<ProtectedRoute><ReservationsPage /></ProtectedRoute>} />
      <Route path="/replenishment"element={<ProtectedRoute><ReplenishmentPage /></ProtectedRoute>} />
      <Route path="/reports"      element={<ProtectedRoute rolesAllowed={['SYSTEM_ADMIN', 'INVENTORY_MANAGER', 'PROCUREMENT_MANAGER']}><ReportsPage /></ProtectedRoute>} />
      <Route path="/employees"    element={<ProtectedRoute adminOnly><EmployeesPage /></ProtectedRoute>} />
      <Route path="*"             element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <ThemeProvider>
        <AuthProvider>
          <AppRoutes />
          <Toaster
            position="top-right"
            toastOptions={{
              duration: 4000,
              style: {
                background: 'var(--bg-card)',
                color: 'var(--text-primary)',
                border: '1px solid var(--border)',
                borderRadius: '12px',
                fontSize: '0.875rem',
                fontFamily: 'Inter, sans-serif',
                boxShadow: 'var(--shadow-card)',
              },
              success: { iconTheme: { primary: '#10b981', secondary: 'var(--bg-card)' } },
              error:   { iconTheme: { primary: '#ef4444', secondary: 'var(--bg-card)' } },
            }}
          />
        </AuthProvider>
      </ThemeProvider>
    </BrowserRouter>
  );
}
