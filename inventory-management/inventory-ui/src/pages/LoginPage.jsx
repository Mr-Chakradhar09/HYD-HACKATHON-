import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { authApi } from '../services/api';
import toast from 'react-hot-toast';
import { useFormik } from 'formik';
import * as yup from 'yup';
import { Box, Card, Typography, TextField, Button, Divider, Stack, Avatar } from '@mui/material';
import InventoryIcon from '@mui/icons-material/Inventory';

const validationSchema = yup.object({
  email: yup.string().email('Enter a valid email').required('Email is required'),
  password: yup.string().min(4, 'Password should be at least 4 characters').required('Password is required'),
});

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();

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
        navigate('/');
      } catch (err) {
        toast.error(err.response?.data?.message || 'Login failed');
      } finally {
        setSubmitting(false);
      }
    },
  });

  return (
    <Box sx={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'linear-gradient(135deg, #1e293b 0%, #0f172a 100%)' }}>
      <Card sx={{ p: 4, width: 420, maxWidth: '90vw' }}>
        <Stack alignItems="center" spacing={1} sx={{ mb: 4 }}>
          <Avatar sx={{ bgcolor: '#4f46e5', width: 48, height: 48 }}>
            <InventoryIcon />
          </Avatar>
          <Typography variant="h5" fontWeight={700}>Inventory Management</Typography>
          <Typography variant="body2" color="text.secondary">Sign in to your account</Typography>
        </Stack>
        <form onSubmit={formik.handleSubmit}>
          <Stack spacing={2.5}>
            <TextField
              fullWidth label="Email" name="email" type="email"
              value={formik.values.email} onChange={formik.handleChange}
              onBlur={formik.handleBlur}
              error={formik.touched.email && Boolean(formik.errors.email)}
              helperText={formik.touched.email && formik.errors.email}
              placeholder="Enter your email"
            />
            <TextField
              fullWidth label="Password" name="password" type="password"
              value={formik.values.password} onChange={formik.handleChange}
              onBlur={formik.handleBlur}
              error={formik.touched.password && Boolean(formik.errors.password)}
              helperText={formik.touched.password && formik.errors.password}
              placeholder="Enter your password"
            />
            <Button type="submit" variant="contained" size="large" fullWidth disabled={formik.isSubmitting} sx={{ py: 1.5 }}>
              {formik.isSubmitting ? 'Signing in...' : 'Sign In'}
            </Button>
          </Stack>
        </form>
        <Divider sx={{ my: 3 }} />
        <Box sx={{ p: 2, bgcolor: '#f8fafc', borderRadius: 1, fontSize: 12, color: '#64748b' }}>
          <Typography variant="caption" fontWeight={600}>Demo Credentials:</Typography><br />
          admin@inventory.com / admin123 — SYSTEM_ADMIN<br />
          warehouse@inventory.com / warehouse123 — WAREHOUSE_MANAGER<br />
          manager@inventory.com / manager123 — INVENTORY_MANAGER<br />
          procurement@inventory.com / procurement123 — PROCUREMENT_MANAGER
        </Box>
      </Card>
    </Box>
  );
}
