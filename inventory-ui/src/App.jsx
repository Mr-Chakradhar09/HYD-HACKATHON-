import { useEffect } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import Dashboard from './pages/Dashboard';
import ProductsPage from './pages/ProductsPage';
import WarehousesPage from './pages/WarehousesPage';
import InventoryPage from './pages/InventoryPage';
import PurchaseRequestsPage from './pages/PurchaseRequestsPage';
import ReportsPage from './pages/ReportsPage';
import UsersPage from './pages/UsersPage';
import Layout from './components/Layout';
import { Box, CircularProgress } from '@mui/material';
import toast from 'react-hot-toast';

function PrivateRoute({ children }) {
  const { user, loading } = useAuth();
  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}><CircularProgress /></Box>;
  return user ? children : <Navigate to="/login" />;
}

function UnauthorizedRedirect() {
  useEffect(() => {
    toast.error('You are not authorized to access this page', { id: 'unauthorized-redirect' });
  }, []);
  return <Navigate to="/dashboard" />;
}

function RoleRoute({ allowedRoles, children }) {
  const { user, loading } = useAuth();
  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}><CircularProgress /></Box>;
  if (!user) return <Navigate to="/login" />;
  if (!allowedRoles.includes(user.role)) return <UnauthorizedRedirect />;
  return children;
}

export default function App() {
  const { user } = useAuth();
  return (
    <Routes>
      <Route path="/" element={user ? <Navigate to="/dashboard" /> : <LandingPage />} />
      <Route path="/login" element={user ? <Navigate to="/dashboard" /> : <LoginPage />} />
      
      <Route path="/dashboard" element={<PrivateRoute><Layout /></PrivateRoute>}>
        <Route index element={<Dashboard />} />
        <Route path="products" element={<RoleRoute allowedRoles={['SYSTEM_ADMIN','INVENTORY_MANAGER','WAREHOUSE_MANAGER']}>{<ProductsPage />}</RoleRoute>} />
        <Route path="warehouses" element={<RoleRoute allowedRoles={['SYSTEM_ADMIN']}>{<WarehousesPage />}</RoleRoute>} />
        <Route path="inventory" element={<RoleRoute allowedRoles={['INVENTORY_MANAGER','WAREHOUSE_MANAGER']}>{<InventoryPage />}</RoleRoute>} />
        <Route path="purchase-requests" element={<RoleRoute allowedRoles={['PROCUREMENT_MANAGER','INVENTORY_MANAGER','WAREHOUSE_MANAGER']}>{<PurchaseRequestsPage />}</RoleRoute>} />
        <Route path="reports" element={<ReportsPage />} />
        <Route path="users" element={<RoleRoute allowedRoles={['SYSTEM_ADMIN']}>{<UsersPage />}</RoleRoute>} />
      </Route>
      
      <Route path="*" element={<Navigate to={user ? "/dashboard" : "/"} />} />
    </Routes>
  );
}
