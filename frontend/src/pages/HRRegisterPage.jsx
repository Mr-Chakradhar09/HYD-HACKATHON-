import React, { useState, useEffect } from 'react';
import { Box, Card, CardContent, Typography, TextField, Button, Grid, Chip, Select, MenuItem, FormControl, InputLabel, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, InputAdornment } from '@mui/material';
import { PersonAddOutlined, EmailOutlined, BadgeOutlined, LocationOnOutlined, CheckCircleOutlined, HourglassEmptyOutlined } from '@mui/icons-material';
import { useSelector, useDispatch } from 'react-redux';
import { toast } from 'react-toastify';
import { hrRegisterEmployee } from '../store/authSlice';
import { addNotification } from '../store/notificationSlice';
import { api } from '../services/api';

export default function HRRegisterPage() {
  const dispatch = useDispatch();
  const hrUser = useSelector((s) => s.auth.user);
  const pendingRegistrations = useSelector((s) => s.auth.pendingRegistrations);
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [location, setLocation] = useState(hrUser?.location || 'Chennai');
  const [backendUsers, setBackendUsers] = useState([]);

  useEffect(() => {
    const fetchBackendUsers = async () => {
      try {
        const users = await api.getUsersByLocation(hrUser?.location || 'Chennai');
        if (users) {
          const mapped = users.map(u => ({
            name: `${u.firstName} ${u.lastName || ''}`.trim(),
            email: u.email,
            location: u.location,
            status: u.active ? 'ACTIVE' : 'PENDING'
          }));
          setBackendUsers(mapped);
        }
      } catch (err) {
        console.warn("Failed to fetch backend users:", err.message);
      }
    };
    if (hrUser?.location) {
      fetchBackendUsers();
    }
  }, [hrUser]);

  const combinedRegistrations = [
    ...backendUsers.filter(bu => !pendingRegistrations[bu.email]),
    ...Object.values(pendingRegistrations)
  ];

  const handleRegister = async (e) => {
    e.preventDefault();
    const targetEmail = email.trim().toLowerCase();
    const targetName = name.trim();
    if (!targetName || !targetEmail) {
      toast.error('Please fill in all fields');
      return;
    }
    if (!targetEmail.includes('@')) {
      toast.error('Please enter a valid email address');
      return;
    }

    try {
      // 1. Try real backend employee registration
      const userDto = await api.registerEmployee(targetEmail, targetName, location);
      dispatch(hrRegisterEmployee({
        email: targetEmail,
        name: targetName,
        location,
        employeeId: userDto.employeeId
      }));
      setBackendUsers(prev => [
        {
          name: targetName,
          email: targetEmail,
          location: location,
          status: 'PENDING'
        },
        ...prev
      ]);
      dispatch(addNotification({
        id: `N-${Date.now()}`,
        recipient: 'HR',
        subject: 'Employee Registered',
        message: `${targetName} (${targetEmail}) has been pre-registered for ${location} on user-service.`,
        timestamp: new Date().toISOString(),
      }));
      toast.success(`Employee registered successfully on backend!`);
      setName('');
      setEmail('');
      return;
    } catch (err) {
      console.warn("Backend employee registration failed. Trying local fallback...", err.message);
    }

    // 2. Local fallback registration
    if (pendingRegistrations[targetEmail]) {
      toast.error('This email is already registered locally.');
      return;
    }

    dispatch(hrRegisterEmployee({ email: targetEmail, name: targetName, location }));
    dispatch(addNotification({
      id: `N-${Date.now()}`,
      recipient: 'HR',
      subject: 'Employee Registered (Local)',
      message: `${targetName} (${targetEmail}) registered locally for ${location}.`,
      timestamp: new Date().toISOString(),
    }));
    toast.success(`Employee registered locally!`);
    setName('');
    setEmail('');
  };

  return (
    <Box sx={{ maxWidth: 900, mx: 'auto' }}>
      <Box className="fade-in-up" sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 4 }}>
        <PersonAddOutlined sx={{ color: '#6C63FF', fontSize: 28 }} />
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 700 }}>Register New Employee</Typography>
          <Typography variant="body2" sx={{ color: '#94A3B8' }}>Pre-register employee details. This authorizes them to create their account and automatically directs them to complete the onboarding survey.</Typography>
        </Box>
      </Box>

      <Card className="fade-in-up" sx={{ mb: 4, animationDelay: '100ms', border: '1px solid rgba(108,99,255,0.2)' }}>
        <CardContent sx={{ p: 4 }}>
          <form onSubmit={handleRegister}>
            <Grid container spacing={2.5}>
              <Grid item xs={12} sm={6}>
                <TextField fullWidth label="Full Name" value={name} onChange={(e) => setName(e.target.value)} placeholder="e.g. Arun Kumar"
                  InputProps={{ startAdornment: <InputAdornment position="start"><BadgeOutlined sx={{ color: '#6C63FF' }} /></InputAdornment> }} />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField fullWidth label="Employee Email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="e.g. arun.kumar@virtusa.com" type="email"
                  InputProps={{ startAdornment: <InputAdornment position="start"><EmailOutlined sx={{ color: '#6C63FF' }} /></InputAdornment> }} />
              </Grid>
              <Grid item xs={12} sm={6}>
                <FormControl fullWidth>
                  <InputLabel>Office Location</InputLabel>
                  <Select value={location} label="Office Location" onChange={(e) => setLocation(e.target.value)}
                    startAdornment={<InputAdornment position="start"><LocationOnOutlined sx={{ color: '#6C63FF' }} /></InputAdornment>}>
                    <MenuItem value="Chennai">Chennai</MenuItem>
                    <MenuItem value="Pune">Pune</MenuItem>
                    <MenuItem value="Hyderabad">Hyderabad</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Button fullWidth type="submit" variant="contained" size="large" startIcon={<PersonAddOutlined />}
                  sx={{ height: 56, background: 'linear-gradient(135deg, #6C63FF 0%, #5A52E0 100%)', fontSize: 16, '&:hover': { background: 'linear-gradient(135deg, #7B73FF 0%, #6C63FF 100%)' } }}>
                  Register Employee
                </Button>
              </Grid>
            </Grid>
          </form>
        </CardContent>
      </Card>

      {combinedRegistrations.length > 0 && (
        <Card className="fade-in-up" sx={{ animationDelay: '200ms' }}>
          <CardContent sx={{ p: 3 }}>
            <Typography variant="h6" sx={{ mb: 2 }}>Registered Employees</Typography>
            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell sx={{ color: '#94A3B8', borderBottom: '1px solid rgba(255,255,255,0.06)', fontWeight: 600 }}>Name</TableCell>
                    <TableCell sx={{ color: '#94A3B8', borderBottom: '1px solid rgba(255,255,255,0.06)', fontWeight: 600 }}>Email</TableCell>
                    <TableCell sx={{ color: '#94A3B8', borderBottom: '1px solid rgba(255,255,255,0.06)', fontWeight: 600 }}>Location</TableCell>
                    <TableCell sx={{ color: '#94A3B8', borderBottom: '1px solid rgba(255,255,255,0.06)', fontWeight: 600 }}>Status</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {combinedRegistrations.map((reg) => (
                    <TableRow key={reg.email}>
                      <TableCell sx={{ borderBottom: '1px solid rgba(255,255,255,0.06)' }}>{reg.name}</TableCell>
                      <TableCell sx={{ borderBottom: '1px solid rgba(255,255,255,0.06)', color: '#94A3B8' }}>{reg.email}</TableCell>
                      <TableCell sx={{ borderBottom: '1px solid rgba(255,255,255,0.06)' }}>
                        <Chip label={reg.location} size="small" sx={{ bgcolor: 'rgba(108,99,255,0.12)', color: '#6C63FF' }} />
                      </TableCell>
                      <TableCell sx={{ borderBottom: '1px solid rgba(255,255,255,0.06)' }}>
                        {reg.status === 'ACTIVE' || reg.status === 'COMPLETED'
                          ? <Chip icon={<CheckCircleOutlined />} label="Active" size="small" sx={{ bgcolor: 'rgba(0,217,166,0.15)', color: '#00D9A6' }} />
                          : <Chip icon={<HourglassEmptyOutlined />} label="Pending Signup" size="small" sx={{ bgcolor: 'rgba(255,181,71,0.15)', color: '#FFB547' }} />
                        }
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </CardContent>
        </Card>
      )}
    </Box>
  );
}
