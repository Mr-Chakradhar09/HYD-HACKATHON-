import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useThemeMode } from '../context/ThemeContext';
import { authApi } from '../services/api';
import toast from 'react-hot-toast';
import { useFormik } from 'formik';
import * as yup from 'yup';
import { Box, Card, Typography, TextField, Button, Divider, Stack, Avatar, IconButton, InputAdornment, Chip } from '@mui/material';
import InventoryIcon from '@mui/icons-material/Inventory';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings';
import SpeedIcon from '@mui/icons-material/Speed';

const validationSchema = yup.object({
  email: yup.string().email('Enter a valid email').required('Email is required'),
  password: yup.string().min(4, 'Password should be at least 4 characters').required('Password is required'),
});

const demoAccounts = [
  { label: 'Admin', email: 'admin@inventory.com', pass: 'admin123', role: 'SYSTEM_ADMIN' },
  { label: 'Warehouse Manager', email: 'warehouse@inventory.com', pass: 'warehouse123', role: 'WAREHOUSE_MANAGER' },
  { label: 'Inventory Manager', email: 'manager@inventory.com', pass: 'manager123', role: 'INVENTORY_MANAGER' },
  { label: 'Procurement Manager', email: 'procurement@inventory.com', pass: 'procurement123', role: 'PROCUREMENT_MANAGER' },
];

export default function LoginPage() {
  const { login } = useAuth();
  const { isDarkMode } = useThemeMode();
  const navigate = useNavigate();
  const [showPassword, setShowPassword] = useState(false);

  const formik = useFormik({
    initialValues: { email: '', password: '' },
    validationSchema,
    onSubmit: async (values, { setSubmitting }) => {
      try {
        const res = await authApi.login(values);
        localStorage.setItem('token', res.data.token);
        login({
          firstName: res.data.firstName,
          lastName: res.data.lastName,
          email: res.data.email,
          role: res.data.role,
          warehouseId: res.data.warehouseId,
        });
        toast.success('Login successful');
        navigate('/dashboard');
      } catch (err) {
        toast.error(err.response?.data?.message || 'Login failed');
      } finally {
        setSubmitting(false);
      }
    },
  });

  const handleQuickFill = (acc) => {
    formik.setFieldValue('email', acc.email);
    formik.setFieldValue('password', acc.pass);
    toast.success(`Quick-filled ${acc.label} credentials!`);
  };

  return (
    <Box sx={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      background: isDarkMode
        ? 'radial-gradient(circle at 10% 20%, rgb(15, 23, 42) 0%, rgb(30, 27, 75) 90%)'
        : 'radial-gradient(circle at 10% 20%, rgb(241, 245, 249) 0%, rgb(224, 231, 255) 90%)',
      p: { xs: 2, md: 4 }
    }}>
      <Box sx={{
        display: 'flex',
        flexDirection: { xs: 'column', md: 'row' },
        alignItems: 'center',
        justifyContent: 'center',
        gap: { xs: 4, md: 8 },
        maxWidth: 1000,
        width: '100%',
        mx: 'auto'
      }}>
        {/* Left Side: Welcome Banner */}
        <Box sx={{
          maxWidth: 420,
          textAlign: { xs: 'center', md: 'left' },
          color: isDarkMode ? '#f8fafc' : '#0f172a',
          display: 'flex',
          flexDirection: 'column',
          alignItems: { xs: 'center', md: 'flex-start' }
        }}>
          <Typography
            variant="h3"
            sx={{
              fontWeight: 800,
              fontSize: { xs: '2rem', md: '2.5rem' },
              lineHeight: 1.2,
              mb: 2,
              background: isDarkMode
                ? 'linear-gradient(135deg, #ffffff 0%, #a5b4fc 100%)'
                : 'linear-gradient(135deg, #1e1b4b 0%, #4f46e5 100%)',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent',
            }}
          >
            Welcome Back to Inventory MS
          </Typography>
          <Typography variant="body1" color="text.secondary" sx={{ mb: 4, fontSize: '0.975rem', lineHeight: 1.6 }}>
            Access your secure workspace to manage active stock records, update regional warehouse parameters, and authorize purchase request forms.
          </Typography>
          <Stack spacing={2} sx={{ width: '100%', display: { xs: 'none', md: 'flex' } }}>
            <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
              <Avatar sx={{ bgcolor: 'rgba(99,102,241,0.1)', color: '#6366f1', width: 40, height: 40 }}>
                <SpeedIcon fontSize="small" />
              </Avatar>
              <Box>
                <Typography variant="body2" fontWeight={700}>Real-time Operations</Typography>
                <Typography variant="caption" color="text.secondary">Telemetry tracking and capacity monitors</Typography>
              </Box>
            </Box>
            <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
              <Avatar sx={{ bgcolor: 'rgba(16,185,129,0.1)', color: '#10b981', width: 40, height: 40 }}>
                <AdminPanelSettingsIcon fontSize="small" />
              </Avatar>
              <Box>
                <Typography variant="body2" fontWeight={700}>Secure RBAC</Typography>
                <Typography variant="caption" color="text.secondary">Role-based gateways and resource shields</Typography>
              </Box>
            </Box>
          </Stack>
        </Box>

        {/* Right Side: Login Card */}
        <Card sx={{
          p: 4.5,
          width: 440,
          maxWidth: '100%',
          borderRadius: 4,
          background: isDarkMode ? 'rgba(17, 24, 39, 0.7)' : 'rgba(255, 255, 255, 0.7)',
          backdropFilter: 'blur(20px) saturate(160%)',
          border: '1px solid',
          borderColor: isDarkMode ? 'rgba(255, 255, 255, 0.08)' : 'rgba(255, 255, 255, 0.5)',
          boxShadow: isDarkMode
            ? '0 10px 40px 0 rgba(0, 0, 0, 0.5)'
            : '0 10px 40px 0 rgba(99, 102, 241, 0.08)',
        }}>
          <Stack alignItems="center" spacing={1.5} sx={{ mb: 4 }}>
            <Avatar sx={{
              bgcolor: isDarkMode ? '#6366f1' : '#4f46e5',
              width: 48,
              height: 48,
              boxShadow: '0 4px 15px 0 rgba(99, 102, 241, 0.3)'
            }}>
              <InventoryIcon sx={{ fontSize: 24 }} />
            </Avatar>
            <Typography variant="h5" fontWeight={800} letterSpacing="-0.5px">Sign In</Typography>
            <Typography variant="body2" color="text.secondary">Enter your details to sign in</Typography>
          </Stack>
          <form onSubmit={formik.handleSubmit}>
            <Stack spacing={3}>
              <TextField
                fullWidth
                label="Email Address"
                name="email"
                type="email"
                value={formik.values.email}
                onChange={formik.handleChange}
                onBlur={formik.handleBlur}
                error={formik.touched.email && Boolean(formik.errors.email)}
                helperText={formik.touched.email && formik.errors.email}
                placeholder="admin@inventory.com"
                variant="outlined"
                InputProps={{
                  sx: { borderRadius: 2.5 }
                }}
              />
              <TextField
                fullWidth
                label="Password"
                name="password"
                type={showPassword ? 'text' : 'password'}
                value={formik.values.password}
                onChange={formik.handleChange}
                onBlur={formik.handleBlur}
                error={formik.touched.password && Boolean(formik.errors.password)}
                helperText={formik.touched.password && formik.errors.password}
                placeholder="••••••••"
                variant="outlined"
                InputProps={{
                  endAdornment: (
                    <InputAdornment position="end">
                      <IconButton onClick={() => setShowPassword(p => !p)} edge="end">
                        {showPassword ? <VisibilityOff /> : <Visibility />}
                      </IconButton>
                    </InputAdornment>
                  ),
                  sx: { borderRadius: 2.5 }
                }}
              />
              <Button
                type="submit"
                variant="contained"
                size="large"
                fullWidth
                disabled={formik.isSubmitting}
                sx={{
                  py: 1.8,
                  borderRadius: 2.5,
                  fontWeight: 600,
                  fontSize: 15,
                  background: isDarkMode
                    ? 'linear-gradient(90deg, #6366f1 0%, #4f46e5 100%)'
                    : 'linear-gradient(90deg, #4f46e5 0%, #3730a3 100%)',
                  color: 'white',
                  '&:hover': {
                    opacity: 0.95
                  }
                }}
              >
                {formik.isSubmitting ? 'Verifying...' : 'Sign In'}
              </Button>
            </Stack>
          </form>
          
          <Divider sx={{ my: 3.5 }} />
          
          {/* Quick Fill section */}
          <Box>
            <Typography variant="caption" fontWeight={700} color="text.secondary" sx={{ display: 'block', mb: 1.5, letterSpacing: 0.8 }}>
              DEMO ACCOUNTS (CLICK TO QUICK-FILL)
            </Typography>
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
              {demoAccounts.map((acc, idx) => (
                <Chip
                  key={idx}
                  label={acc.label}
                  onClick={() => handleQuickFill(acc)}
                  size="small"
                  variant="outlined"
                  sx={{
                    borderRadius: 2,
                    fontSize: 11,
                    fontWeight: 500,
                    cursor: 'pointer',
                    '&:hover': {
                      bgcolor: isDarkMode ? 'rgba(255,255,255,0.05)' : 'rgba(0,0,0,0.03)'
                    }
                  }}
                />
              ))}
            </Box>
          </Box>
        </Card>
      </Box>
    </Box>
  );
}
