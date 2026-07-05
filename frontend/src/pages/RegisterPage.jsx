import React, { useState } from 'react';
import { Box, Card, CardContent, TextField, Button, Typography, InputAdornment, IconButton, Alert } from '@mui/material';
import { FavoriteBorderOutlined, EmailOutlined, LockOutlined, VisibilityOutlined, VisibilityOffOutlined, CheckCircleOutlined, HowToRegOutlined } from '@mui/icons-material';
import { useSelector, useDispatch } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { completeRegistration, login } from '../store/authSlice';
import { api } from '../services/api';

export default function RegisterPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPw, setShowPw] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [step, setStep] = useState('email'); // 'email' -> 'password'
  const [foundUser, setFoundUser] = useState(null);

  const dispatch = useDispatch();
  const navigate = useNavigate();
  const pendingRegistrations = useSelector((s) => s.auth.pendingRegistrations);
  const registeredUsers = useSelector((s) => s.auth.registeredUsers);

  const handleVerifyEmail = async (e) => {
    e.preventDefault();
    const normalizedEmail = email.toLowerCase().trim();
    
    try {
      // 1. Try real backend verification
      const userDto = await api.verifyEmail(normalizedEmail);
      setFoundUser(userDto);
      setStep('password');
      toast.success(`Email verified! Welcome, ${userDto.name}. Please create your password.`);
      return;
    } catch (err) {
      console.warn("Backend email verification failed. Trying local fallback...", err.message);
    }

    // 2. Check local fallback
    if (registeredUsers[normalizedEmail]) {
      toast.info('This account is already active. Please sign in instead.');
      navigate('/login');
      return;
    }

    const pending = pendingRegistrations[normalizedEmail];
    if (pending && pending.status === 'PENDING') {
      setFoundUser(pending);
      setStep('password');
      toast.success(`Email verified! Welcome, ${pending.name}. Please create your password.`);
    } else {
      toast.error('This email is not pre-registered by HR. Please ask your HR manager to register you.');
    }
  };

  const handleCreateAccount = async (e) => {
    e.preventDefault();
    const normalizedEmail = email.toLowerCase().trim();
    if (password.length < 6) {
      toast.error('Password must be at least 6 characters');
      return;
    }
    if (password !== confirmPassword) {
      toast.error('Passwords do not match');
      return;
    }

    try {
      // 1. Try real backend registration
      const data = await api.completeRegistration(normalizedEmail, password);
      localStorage.setItem('token', data.token);
      dispatch(login(data.user));
      toast.success(`Account created! Welcome to PulseAI, ${data.user.name}! 🎉`);
      navigate('/app/survey'); // Go directly to onboarding survey!
      return;
    } catch (err) {
      console.warn("Backend registration completion failed. Trying local fallback...", err.message);
    }

    // 2. Local fallback registration
    dispatch(completeRegistration({ email: normalizedEmail, password }));
    const newUser = {
      id: `USR-${Date.now().toString().slice(-4)}`,
      username: email.split('@')[0],
      email: normalizedEmail,
      name: foundUser.name,
      location: foundUser.location,
      role: 'EMPLOYEE',
      status: 'PENDING_ONBOARDING',
    };
    dispatch(login(newUser));
    toast.success(`Account created successfully (local mode)! Welcome to PulseAI, ${foundUser.name}! 🎉`);
    navigate('/app/survey'); // Go directly to onboarding survey!
  };

  return (
    <Box sx={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'radial-gradient(ellipse at center, rgba(0,217,166,0.1) 0%, #0A0E1A 70%)' }}>
      <Card className="fade-in-up" sx={{ maxWidth: 460, width: '100%', mx: 2, background: 'rgba(17,24,39,0.85)', backdropFilter: 'blur(20px)', border: '1px solid rgba(0,217,166,0.2)' }}>
        <CardContent sx={{ p: 4 }}>
          <Box sx={{ textAlign: 'center', mb: 4 }}>
            <Box sx={{ display: 'inline-flex', alignItems: 'center', gap: 1, mb: 1 }}>
              <FavoriteBorderOutlined sx={{ color: '#6C63FF', fontSize: 32 }} />
              <Typography variant="h4" sx={{ background: 'linear-gradient(135deg, #6C63FF, #00D9A6)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent', fontWeight: 800 }}>PulseAI</Typography>
            </Box>
            <Typography variant="body2" sx={{ color: '#94A3B8' }}>
              {step === 'email' ? 'Verify Employee Email' : 'Set Your Password'}
            </Typography>
          </Box>

          {step === 'email' && (
            <form onSubmit={handleVerifyEmail}>
              <Alert severity="info" sx={{ mb: 3, bgcolor: 'rgba(99,179,237,0.08)', border: '1px solid rgba(99,179,237,0.2)', '& .MuiAlert-icon': { color: '#63B3ED' } }}>
                Enter the email address registered for you by your HR manager to get access.
              </Alert>
              <TextField fullWidth label="Your Work Email" value={email} onChange={(e) => setEmail(e.target.value)} type="email" placeholder="you@virtusa.com" sx={{ mb: 3 }}
                InputProps={{ startAdornment: <InputAdornment position="start"><EmailOutlined sx={{ color: '#00D9A6' }} /></InputAdornment> }} />
              <Button fullWidth type="submit" variant="contained" size="large"
                sx={{ py: 1.5, background: 'linear-gradient(135deg, #00D9A6 0%, #00B88A 100%)', color: '#0A0E1A', fontSize: 16, fontWeight: 700, '&:hover': { background: 'linear-gradient(135deg, #33E3BA 0%, #00D9A6 100%)' } }}>
                Verify Email
              </Button>
              <Button fullWidth onClick={() => navigate('/login')} sx={{ mt: 2, color: '#94A3B8' }}>
                Already have an account? Sign In
              </Button>
            </form>
          )}

          {step === 'password' && foundUser && (
            <form onSubmit={handleCreateAccount}>
              <Box sx={{ mb: 3, p: 2, borderRadius: 2, bgcolor: 'rgba(0,217,166,0.06)', border: '1px solid rgba(0,217,166,0.15)' }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                  <CheckCircleOutlined sx={{ color: '#00D9A6', fontSize: 20 }} />
                  <Typography variant="body2" sx={{ color: '#00D9A6', fontWeight: 600 }}>Email Verified</Typography>
                </Box>
                <Typography variant="body2" sx={{ color: '#94A3B8' }}>
                  Welcome, <strong style={{ color: '#F1F5F9' }}>{foundUser.name}</strong> · {foundUser.location}
                </Typography>
              </Box>

              <TextField fullWidth label="Create Password" type={showPw ? 'text' : 'password'} value={password} onChange={(e) => setPassword(e.target.value)} sx={{ mb: 2.5 }}
                helperText="Minimum 6 characters"
                InputProps={{
                  startAdornment: <InputAdornment position="start"><LockOutlined sx={{ color: '#00D9A6' }} /></InputAdornment>,
                  endAdornment: <InputAdornment position="end"><IconButton onClick={() => setShowPw(!showPw)} size="small" sx={{ color: '#94A3B8' }}>{showPw ? <VisibilityOffOutlined /> : <VisibilityOutlined />}</IconButton></InputAdornment>,
                }} />
              <TextField fullWidth label="Confirm Password" type={showConfirm ? 'text' : 'password'} value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} sx={{ mb: 3 }}
                error={confirmPassword.length > 0 && password !== confirmPassword}
                helperText={confirmPassword.length > 0 && password !== confirmPassword ? 'Passwords do not match' : ''}
                InputProps={{
                  startAdornment: <InputAdornment position="start"><LockOutlined sx={{ color: '#00D9A6' }} /></InputAdornment>,
                  endAdornment: <InputAdornment position="end"><IconButton onClick={() => setShowConfirm(!showConfirm)} size="small" sx={{ color: '#94A3B8' }}>{showConfirm ? <VisibilityOffOutlined /> : <VisibilityOutlined />}</IconButton></InputAdornment>,
                }} />
              <Button fullWidth type="submit" variant="contained" size="large" startIcon={<HowToRegOutlined />}
                sx={{ py: 1.5, background: 'linear-gradient(135deg, #00D9A6 0%, #00B88A 100%)', color: '#0A0E1A', fontSize: 16, fontWeight: 700, '&:hover': { background: 'linear-gradient(135deg, #33E3BA 0%, #00D9A6 100%)' } }}>
                Create Account & Log In
              </Button>
            </form>
          )}
        </CardContent>
      </Card>
    </Box>
  );
}
