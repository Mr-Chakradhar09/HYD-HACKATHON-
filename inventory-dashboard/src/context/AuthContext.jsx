import { createContext, useContext, useState, useEffect } from 'react';
import { authApi } from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try { return JSON.parse(localStorage.getItem('authUser')); } catch { return null; }
  });
  const [loading, setLoading] = useState(false);

  const login = async (employeeCode, password) => {
    const res = await authApi.login(employeeCode, password);
    const data = res.data?.data;
    if (!data?.token) throw new Error('No token received');
    localStorage.setItem('authToken', data.token);
    const userObj = {
      employeeCode: data.employeeCode || employeeCode,
      fullName: data.fullName || data.name || '',
      roles: data.roles || [],
      email: data.email || '',
    };
    localStorage.setItem('authUser', JSON.stringify(userObj));
    setUser(userObj);
    return userObj;
  };

  const logout = () => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('authUser');
    setUser(null);
  };

  const hasRole = (role) => user?.roles?.includes(role);
  const isAdmin = () => hasRole('SYSTEM_ADMIN');
  const isManager = () => hasRole('SYSTEM_ADMIN') || hasRole('INVENTORY_MANAGER');
  const isProcurement = () => hasRole('PROCUREMENT_MANAGER');
  const isOperator = () => hasRole('WAREHOUSE_OPERATOR');
  const canViewDashboardKPI = () => !isOperator();
  const canViewReports = () => !isOperator();
  const canCreateReplenishment = () => isManager() || isProcurement();
  const canApproveReplenishment = () => isManager();
  const canViewAssignments = () => !isOperator();

  return (
    <AuthContext.Provider value={{ user, login, logout, hasRole, isAdmin, isManager, isProcurement, isOperator, canViewDashboardKPI, canViewReports, canCreateReplenishment, canApproveReplenishment, canViewAssignments, loading }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
