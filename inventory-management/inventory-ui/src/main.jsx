import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { Toaster } from 'react-hot-toast'
import { ThemeProvider, createTheme, CssBaseline } from '@mui/material'
import { AuthProvider } from './context/AuthContext'
import App from './App'
import './index.css'

const theme = createTheme({
  palette: {
    primary: { main: '#4f46e5' },
    secondary: { main: '#0ea5e9' },
    success: { main: '#22c55e' },
    warning: { main: '#f59e0b' },
    error: { main: '#ef4444' },
  },
  typography: {
    fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
  },
  components: {
    MuiButton: { styleOverrides: { root: { textTransform: 'none', borderRadius: 6 } } },
    MuiCard: { styleOverrides: { root: { borderRadius: 10, boxShadow: '0 1px 3px rgba(0,0,0,0.08)' } } },
    MuiTableHead: { styleOverrides: { root: { '& .MuiTableCell-head': { fontWeight: 600, fontSize: '12px', textTransform: 'uppercase', letterSpacing: '0.5px', color: '#64748b' } } } },
  },
})

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <BrowserRouter>
        <AuthProvider>
          <App />
          <Toaster position="top-right" />
        </AuthProvider>
      </BrowserRouter>
    </ThemeProvider>
  </React.StrictMode>
)
