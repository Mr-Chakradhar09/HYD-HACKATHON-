import React from 'react';
import { Box } from '@mui/material';
import { Outlet, Navigate, useLocation } from 'react-router-dom';
import { useSelector } from 'react-redux';
import Sidebar from './Sidebar';
import TopBar from './TopBar';

export default function DashboardLayout() {
  const { isAuthenticated, role, user } = useSelector((s) => s.auth);
  const location = useLocation();

  if (!isAuthenticated) return <Navigate to="/login" replace />;

  if (role === 'EMPLOYEE' && user?.status === 'PENDING_ONBOARDING' && location.pathname !== '/app/survey') {
    return <Navigate to="/app/survey" replace />;
  }

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      <Sidebar role={role} />
      <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
        <TopBar />
        <Box sx={{ flex: 1, p: 4, overflowY: 'auto' }}>
          <Outlet />
        </Box>
      </Box>
    </Box>
  );
}
