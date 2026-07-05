import React, { Suspense, lazy, useState, useEffect, useMemo } from 'react';
import { Provider } from 'react-redux';
import { ThemeProvider, CssBaseline } from '@mui/material';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import CircularProgress from '@mui/material/CircularProgress';
import Box from '@mui/material/Box';
import 'react-toastify/dist/ReactToastify.css';

import store from './store/store';
import { getAppTheme } from './theme';
import DashboardLayout from './components/Layout/DashboardLayout';
import { ColorModeContext } from './components/ThemeModeContext';

const LoginPage = lazy(() => import('./pages/LoginPage'));
const DashboardPage = lazy(() => import('./pages/DashboardPage'));
const SurveyPage = lazy(() => import('./pages/SurveyPage'));
const DraftsPage = lazy(() => import('./pages/DraftsPage'));
const AnalyticsPage = lazy(() => import('./pages/AnalyticsPage'));
const ReportsPage = lazy(() => import('./pages/ReportsPage'));
const NotificationsPage = lazy(() => import('./pages/NotificationsPage'));
const RegisterPage = lazy(() => import('./pages/RegisterPage'));
const HRRegisterPage = lazy(() => import('./pages/HRRegisterPage'));
const ManageSurveysPage = lazy(() => import('./pages/ManageSurveysPage'));

function LoadingFallback() {
  return (
    <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: '100vh' }}>
      <CircularProgress sx={{ color: '#6C63FF' }} />
    </Box>
  );
}

export default function App() {
  const [themeMode, setThemeMode] = useState(() => {
    return localStorage.getItem('theme_mode') || 'system';
  });

  const [activePaletteMode, setActivePaletteMode] = useState(() => {
    const saved = localStorage.getItem('theme_mode') || 'system';
    if (saved === 'system') {
      return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    }
    return saved;
  });

  useEffect(() => {
    if (themeMode === 'system') {
      const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
      const handleChange = (e) => {
        setActivePaletteMode(e.matches ? 'dark' : 'light');
      };
      setActivePaletteMode(mediaQuery.matches ? 'dark' : 'light');
      mediaQuery.addEventListener('change', handleChange);
      return () => mediaQuery.removeEventListener('change', handleChange);
    } else {
      setActivePaletteMode(themeMode);
    }
  }, [themeMode]);

  const handleSetThemeMode = (mode) => {
    setThemeMode(mode);
    localStorage.setItem('theme_mode', mode);
  };

  const theme = useMemo(() => getAppTheme(activePaletteMode), [activePaletteMode]);

  return (
    <Provider store={store}>
      <ColorModeContext.Provider value={{ themeMode, setThemeMode: handleSetThemeMode }}>
        <ThemeProvider theme={theme}>
          <CssBaseline />
          <BrowserRouter>
            <Suspense fallback={<LoadingFallback />}>
              <Routes>
                <Route path="/" element={<Navigate to="/login" replace />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
                <Route path="/app" element={<DashboardLayout />}>
                  <Route path="dashboard" element={<DashboardPage />} />
                  <Route path="survey" element={<SurveyPage />} />
                  <Route path="drafts" element={<DraftsPage />} />
                  <Route path="analytics" element={<AnalyticsPage />} />
                  <Route path="reports" element={<ReportsPage />} />
                  <Route path="manage-surveys" element={<ManageSurveysPage />} />
                  <Route path="register-employee" element={<HRRegisterPage />} />
                  <Route path="notifications" element={<NotificationsPage />} />
                </Route>
              </Routes>
            </Suspense>
          </BrowserRouter>
          <ToastContainer position="top-right" autoClose={3000} theme={activePaletteMode} toastStyle={{ background: activePaletteMode === 'dark' ? '#1E293B' : '#FFFFFF', color: activePaletteMode === 'dark' ? '#F1F5F9' : '#1E293B', border: '1px solid rgba(108,99,255,0.2)', borderRadius: 12 }} />
        </ThemeProvider>
      </ColorModeContext.Provider>
    </Provider>
  );
}
