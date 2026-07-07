import React, { createContext, useContext, useState, useEffect, useMemo } from 'react';
import { createTheme, ThemeProvider } from '@mui/material/styles';

const ThemeContext = createContext();

export function ThemeContextProvider({ children }) {
  const [mode, setModeState] = useState(() => {
    const saved = localStorage.getItem('theme-mode');
    return saved || 'system';
  });

  const [systemIsDark, setSystemIsDark] = useState(() => {
    if (typeof window !== 'undefined') {
      return window.matchMedia('(prefers-color-scheme: dark)').matches;
    }
    return false;
  });

  useEffect(() => {
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
    const handler = (e) => {
      setSystemIsDark(e.matches);
    };
    mediaQuery.addEventListener('change', handler);
    return () => mediaQuery.removeEventListener('change', handler);
  }, []);

  const setMode = (newMode) => {
    setModeState(newMode);
    localStorage.setItem('theme-mode', newMode);
  };

  const isDarkMode = useMemo(() => {
    if (mode === 'system') {
      return systemIsDark;
    }
    return mode === 'dark';
  }, [mode, systemIsDark]);

  const theme = useMemo(() => {
    return createTheme({
      palette: {
        mode: isDarkMode ? 'dark' : 'light',
        primary: {
          main: isDarkMode ? '#6366f1' : '#4f46e5',
        },
        secondary: {
          main: '#0ea5e9',
        },
        background: {
          default: isDarkMode ? '#0b0f19' : '#f8fafc',
          paper: isDarkMode ? '#111827' : '#ffffff',
        },
        text: {
          primary: isDarkMode ? '#f9fafb' : '#0f172a',
          secondary: isDarkMode ? '#9ca3af' : '#475569',
        },
        divider: isDarkMode ? '#1f2937' : '#e2e8f0',
      },
      typography: {
        fontFamily: '"Outfit", "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
        h5: {
          fontWeight: 700,
        },
        h6: {
          fontWeight: 600,
        },
        body1: {
          fontSize: '0.925rem',
        },
        body2: {
          fontSize: '0.85rem',
        },
      },
      components: {
        MuiButton: {
          styleOverrides: {
            root: {
              textTransform: 'none',
              borderRadius: 8,
              fontWeight: 500,
              boxShadow: 'none',
              '&:hover': {
                boxShadow: 'none',
              },
            },
          },
        },
        MuiCard: {
          styleOverrides: {
            root: {
              borderRadius: 12,
              border: isDarkMode ? '1px solid #1f2937' : '1px solid #e2e8f0',
              boxShadow: isDarkMode 
                ? '0 4px 20px 0 rgba(0, 0, 0, 0.35)' 
                : '0 4px 20px 0 rgba(0, 0, 0, 0.03)',
              backgroundImage: 'none',
            },
          },
        },
        MuiTableHead: {
          styleOverrides: {
            root: {
              '& .MuiTableCell-head': {
                fontWeight: 600,
                fontSize: '11px',
                textTransform: 'uppercase',
                letterSpacing: '0.5px',
                color: isDarkMode ? '#9ca3af' : '#64748b',
                backgroundColor: isDarkMode ? '#1f2937' : '#f8fafc',
                borderBottom: isDarkMode ? '1px solid #374151' : '1px solid #e2e8f0',
              },
            },
          },
        },
        MuiTableCell: {
          styleOverrides: {
            root: {
              borderBottom: isDarkMode ? '1px solid #1f2937' : '1px solid #f1f5f9',
            },
          },
        },
        MuiDialog: {
          styleOverrides: {
            paper: {
              borderRadius: 16,
              backgroundImage: 'none',
            },
          },
        },
      },
    });
  }, [isDarkMode]);

  return (
    <ThemeContext.Provider value={{ mode, setMode, isDarkMode }}>
      <ThemeProvider theme={theme}>
        {children}
      </ThemeProvider>
    </ThemeContext.Provider>
  );
}

export const useThemeMode = () => useContext(ThemeContext);
