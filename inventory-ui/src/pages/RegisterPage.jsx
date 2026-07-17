import { useNavigate } from 'react-router-dom';
import { useThemeMode } from '../context/ThemeContext';
import { authApi } from '../services/api';
import toast from 'react-hot-toast';
import { useFormik } from 'formik';
import * as yup from 'yup';
import { Box, Card, Typography, TextField, Button, Divider, Stack, Avatar, MenuItem } from '@mui/material';
import AppRegistrationIcon from '@mui/icons-material/AppRegistration';

const validationSchema = yup.object({
  employeeId: yup.string().required('Employee ID is required'),
  firstName: yup.string().required('First name is required'),
  lastName: yup.string().required('Last name is required'),
  email: yup.string().email('Enter a valid email').required('Email is required'),
  mobile: yup.string().required('Mobile number is required'),
  password: yup.string().min(4, 'Password should be at least 4 characters').required('Password is required'),
  role: yup.string().required('Role is required'),
});

const roles = [
  { value: 'SYSTEM_ADMIN', label: 'System Admin' },
  { value: 'INVENTORY_MANAGER', label: 'Inventory Manager' },
  { value: 'WAREHOUSE_MANAGER', label: 'Warehouse Manager' },
  { value: 'PROCUREMENT_MANAGER', label: 'Procurement Manager' },
];

export default function RegisterPage() {
  const { isDarkMode } = useThemeMode();
  const navigate = useNavigate();

  const formik = useFormik({
    initialValues: {
      employeeId: '',
      firstName: '',
      lastName: '',
      email: '',
      mobile: '',
      password: '',
      role: 'INVENTORY_MANAGER',
    },
    validationSchema,
    onSubmit: async (values, { setSubmitting }) => {
      try {
        // Simulate registration since backend changes are disabled
        await new Promise((resolve) => setTimeout(resolve, 800));
        toast.success('Simulation: Account registration requested! Please sign in using any of the demo accounts.');
        navigate('/login');
      } catch (err) {
        toast.error('Registration failed');
      } finally {
        setSubmitting(false);
      }
    },
  });

  return (
    <Box sx={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      background: isDarkMode
        ? 'radial-gradient(circle at 10% 20%, rgb(15, 23, 42) 0%, rgb(30, 27, 75) 90%)'
        : 'radial-gradient(circle at 10% 20%, rgb(241, 245, 249) 0%, rgb(224, 231, 255) 90%)',
      py: 4,
      px: 2
    }}>
      <Card sx={{
        p: 4.5,
        width: 500,
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
        <Stack alignItems="center" spacing={1.5} sx={{ mb: 3.5 }}>
          <Avatar sx={{
            bgcolor: isDarkMode ? '#6366f1' : '#4f46e5',
            width: 52,
            height: 52,
            boxShadow: '0 4px 15px 0 rgba(99, 102, 241, 0.3)'
          }}>
            <AppRegistrationIcon sx={{ fontSize: 28 }} />
          </Avatar>
          <Typography variant="h5" fontWeight={800} letterSpacing="-0.5px">Create Console Account</Typography>
          <Typography variant="body2" color="text.secondary">Enter your details to register</Typography>
        </Stack>
        <form onSubmit={formik.handleSubmit}>
          <Stack spacing={2.2}>
            <Stack direction="row" spacing={2}>
              <TextField
                fullWidth label="First Name" name="firstName" value={formik.values.firstName} onChange={formik.handleChange}
                onBlur={formik.handleBlur} error={formik.touched.firstName && Boolean(formik.errors.firstName)}
                helperText={formik.touched.firstName && formik.errors.firstName} placeholder="John"
                InputProps={{ sx: { borderRadius: 2 } }}
              />
              <TextField
                fullWidth label="Last Name" name="lastName" value={formik.values.lastName} onChange={formik.handleChange}
                onBlur={formik.handleBlur} error={formik.touched.lastName && Boolean(formik.errors.lastName)}
                helperText={formik.touched.lastName && formik.errors.lastName} placeholder="Doe"
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Stack>
            <Stack direction="row" spacing={2}>
              <TextField
                fullWidth label="Employee ID" name="employeeId" value={formik.values.employeeId} onChange={formik.handleChange}
                onBlur={formik.handleBlur} error={formik.touched.employeeId && Boolean(formik.errors.employeeId)}
                helperText={formik.touched.employeeId && formik.errors.employeeId} placeholder="EMP-123"
                InputProps={{ sx: { borderRadius: 2 } }}
              />
              <TextField
                fullWidth label="Mobile Number" name="mobile" value={formik.values.mobile} onChange={formik.handleChange}
                onBlur={formik.handleBlur} error={formik.touched.mobile && Boolean(formik.errors.mobile)}
                helperText={formik.touched.mobile && formik.errors.mobile} placeholder="+123456789"
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Stack>
            <TextField
              fullWidth label="Email Address" name="email" type="email" value={formik.values.email} onChange={formik.handleChange}
              onBlur={formik.handleBlur} error={formik.touched.email && Boolean(formik.errors.email)}
              helperText={formik.touched.email && formik.errors.email} placeholder="john.doe@inventory.com"
              InputProps={{ sx: { borderRadius: 2 } }}
            />
            <TextField
              fullWidth label="Password" name="password" type="password" value={formik.values.password} onChange={formik.handleChange}
              onBlur={formik.handleBlur} error={formik.touched.password && Boolean(formik.errors.password)}
              helperText={formik.touched.password && formik.errors.password} placeholder="••••••••"
              InputProps={{ sx: { borderRadius: 2 } }}
            />
            <TextField
              fullWidth label="Default Role" name="role" select value={formik.values.role} onChange={formik.handleChange}
              onBlur={formik.handleBlur} error={formik.touched.role && Boolean(formik.errors.role)}
              helperText={formik.touched.role && formik.errors.role}
              InputProps={{ sx: { borderRadius: 2 } }}
            >
              {roles.map(r => <MenuItem key={r.value} value={r.value}>{r.label}</MenuItem>)}
            </TextField>
            <Button
              type="submit" variant="contained" size="large" fullWidth disabled={formik.isSubmitting}
              sx={{
                py: 1.8,
                borderRadius: 2.5,
                fontWeight: 600,
                fontSize: 15,
                mt: 1.5,
                background: isDarkMode
                  ? 'linear-gradient(90deg, #6366f1 0%, #4f46e5 100%)'
                  : 'linear-gradient(90deg, #4f46e5 0%, #3730a3 100%)',
                color: 'white',
                '&:hover': { opacity: 0.95 }
              }}
            >
              {formik.isSubmitting ? 'Registering...' : 'Register Account'}
            </Button>
          </Stack>
        </form>
        <Divider sx={{ my: 3 }} />
        <Typography variant="body2" align="center" color="text.secondary">
          Already have an account?{' '}
          <Button variant="text" size="small" onClick={() => navigate('/login')} sx={{ fontWeight: 700, p: 0, minWidth: 'auto', verticalAlign: 'baseline' }}>
            Sign In
          </Button>
        </Typography>
      </Card>
    </Box>
  );
}
