import { createTheme } from '@mui/material/styles';

export const getAppTheme = (mode) => {
  const isDark = mode === 'dark';
  return createTheme({
    palette: {
      mode: isDark ? 'dark' : 'light',
      primary: { main: '#6C63FF', light: '#8B83FF', dark: '#5A52E0' },
      secondary: { main: '#00D9A6', light: '#33E3BA', dark: '#00B88A' },
      success: { main: '#00D9A6' },
      warning: { main: '#FFB547' },
      error: { main: '#FF6B6B' },
      info: { main: '#63B3ED' },
      background: {
        default: isDark ? '#0A0E1A' : '#F8FAFC',
        paper: isDark ? '#111827' : '#FFFFFF',
      },
      text: {
        primary: isDark ? '#F1F5F9' : '#1E293B',
        secondary: isDark ? '#94A3B8' : '#64748B',
      },
      divider: isDark ? 'rgba(255,255,255,0.06)' : 'rgba(0,0,0,0.08)',
    },
    typography: {
      fontFamily: "'Inter', 'Roboto', sans-serif",
      h4: { fontWeight: 700 },
      h5: { fontWeight: 700 },
      h6: { fontWeight: 600 },
      button: { fontWeight: 600 },
    },
    shape: { borderRadius: 12 },
    components: {
      MuiCard: {
        styleOverrides: {
          root: {
            backgroundImage: 'none',
            backgroundColor: isDark ? '#111827' : '#FFFFFF',
            border: isDark ? '1px solid rgba(108, 99, 255, 0.12)' : '1px solid rgba(0, 0, 0, 0.08)',
            transition: 'border-color 0.3s ease, box-shadow 0.3s ease',
            '&:hover': {
              borderColor: 'rgba(108, 99, 255, 0.3)',
              boxShadow: isDark ? '0 4px 30px rgba(108, 99, 255, 0.08)' : '0 4px 20px rgba(108, 99, 255, 0.05)',
            },
          },
        },
        defaultProps: { elevation: 0 },
      },
      MuiButton: {
        styleOverrides: {
          root: { textTransform: 'none', borderRadius: 8, fontWeight: 600 },
        },
      },
      MuiPaper: {
        styleOverrides: { root: { backgroundImage: 'none' } },
      },
      MuiChip: {
        styleOverrides: { root: { borderRadius: 8 } },
      },
      MuiTextField: {
        styleOverrides: {
          root: {
            '& .MuiOutlinedInput-root': {
              '& fieldset': { borderColor: isDark ? 'rgba(255,255,255,0.12)' : 'rgba(0,0,0,0.12)' },
              '&:hover fieldset': { borderColor: 'rgba(108,99,255,0.5)' },
            },
          },
        },
      },
    },
  });
};

export default getAppTheme('dark');
