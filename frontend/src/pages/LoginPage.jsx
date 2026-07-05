import React, { useState } from 'react';
import { Box, Card, CardContent, TextField, Button, Typography, InputAdornment, IconButton, Divider } from '@mui/material';
import { FavoriteBorderOutlined, PersonOutlined, LockOutlined, VisibilityOutlined, VisibilityOffOutlined } from '@mui/icons-material';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { login } from '../store/authSlice';
import { api } from '../services/api';
import { DUMMY_USERS } from '../data/dummyData';

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPw, setShowPw] = useState(false);
  const [isHovered, setIsHovered] = useState(false);
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const registeredUsers = useSelector((s) => s.auth.registeredUsers);

  const handleLogin = async (e) => {
    e.preventDefault();
    const input = username.toLowerCase().trim();

    try {
      // 1. Try real backend login
      const data = await api.login(username, password);
      localStorage.setItem('token', data.token);
      dispatch(login(data.user));
      toast.success(`Welcome back, ${data.user.name}!`);
      navigate('/app/dashboard');
      return;
    } catch (err) {
      console.warn("Backend auth failed. Trying local demo fallback...", err.message);
    }

    // 2. Check pre-seeded demo users (local fallback)
    const demoUser = DUMMY_USERS[input];
    if (demoUser) {
      dispatch(login(demoUser));
      toast.success(`Welcome back, ${demoUser.name}!`);
      navigate('/app/dashboard');
      return;
    }

    // 3. Check self-registered users (local fallback)
    const regUser = registeredUsers[input];
    if (regUser) {
      if (regUser.password === password) {
        dispatch(login({ id: regUser.id, username: regUser.username, email: regUser.email, name: regUser.name, location: regUser.location, role: regUser.role, status: 'ACTIVE' }));
        toast.success(`Welcome back, ${regUser.name}!`);
        navigate('/app/dashboard');
      } else {
        toast.error('Incorrect password. Please try again.');
      }
      return;
    }

    toast.error('Invalid credentials. Try demo accounts (hr1 / globalhr).');
  };

  return (
    <Box sx={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'radial-gradient(circle at 50% 50%, #1A1C29 0%, #0A0E1A 100%)', position: 'relative', overflow: 'hidden' }}>
      
      {/* Background Micro-animations */}
      <Box sx={{ position: 'absolute', width: 400, height: 400, background: 'radial-gradient(circle, rgba(108,99,255,0.15) 0%, rgba(0,0,0,0) 70%)', top: '-10%', left: '-10%', borderRadius: '50%', filter: 'blur(60px)', animation: 'float 10s ease-in-out infinite' }} />
      <Box sx={{ position: 'absolute', width: 500, height: 500, background: 'radial-gradient(circle, rgba(0,217,166,0.1) 0%, rgba(0,0,0,0) 70%)', bottom: '-15%', right: '-10%', borderRadius: '50%', filter: 'blur(80px)', animation: 'float 14s ease-in-out infinite reverse' }} />

      <Card 
        onMouseEnter={() => setIsHovered(true)} 
        onMouseLeave={() => setIsHovered(false)}
        className="fade-in-up" 
        sx={{ 
          maxWidth: 440, 
          width: '100%', 
          mx: 2, 
          background: 'rgba(20, 24, 39, 0.65)', 
          backdropFilter: 'blur(24px)', 
          border: '1px solid rgba(255, 255, 255, 0.08)',
          boxShadow: isHovered ? '0 30px 60px rgba(0,0,0,0.6), 0 0 20px rgba(108,99,255,0.2)' : '0 20px 40px rgba(0,0,0,0.4)',
          borderRadius: 4,
          transition: 'all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275)',
          transform: isHovered ? 'translateY(-5px)' : 'translateY(0)',
          zIndex: 1
        }}
      >
        <CardContent sx={{ p: { xs: 4, sm: 5 } }}>
          <Box sx={{ textAlign: 'center', mb: 5 }}>
            <Box sx={{ display: 'inline-flex', alignItems: 'center', gap: 1.5, mb: 2, padding: '12px 24px', background: 'rgba(255,255,255,0.03)', borderRadius: 3, border: '1px solid rgba(255,255,255,0.05)' }}>
              <FavoriteBorderOutlined sx={{ color: '#6C63FF', fontSize: 36, animation: 'pulse 2s infinite' }} />
              <Typography variant="h3" sx={{ background: 'linear-gradient(135deg, #6C63FF, #00D9A6)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent', fontWeight: 900, letterSpacing: '-0.5px' }}>PulseAI</Typography>
            </Box>
            <Typography variant="body1" sx={{ color: '#94A3B8', fontWeight: 500 }}>Intelligent Enterprise Survey Platform</Typography>
          </Box>

          <form onSubmit={handleLogin}>
            <TextField 
              fullWidth 
              label="Username or Email" 
              value={username} 
              onChange={(e) => setUsername(e.target.value)} 
              sx={{ mb: 3 }}
              InputProps={{ 
                startAdornment: <InputAdornment position="start"><PersonOutlined sx={{ color: '#6C63FF' }} /></InputAdornment>,
                sx: { borderRadius: 2, bgcolor: 'rgba(255,255,255,0.03)', '&:hover': { bgcolor: 'rgba(255,255,255,0.05)' } }
              }} 
            />
            <TextField 
              fullWidth 
              label="Password" 
              type={showPw ? 'text' : 'password'} 
              value={password} 
              onChange={(e) => setPassword(e.target.value)} 
              sx={{ mb: 4 }}
              InputProps={{
                startAdornment: <InputAdornment position="start"><LockOutlined sx={{ color: '#6C63FF' }} /></InputAdornment>,
                endAdornment: <InputAdornment position="end"><IconButton onClick={() => setShowPw(!showPw)} size="small" sx={{ color: '#94A3B8' }}>{showPw ? <VisibilityOffOutlined /> : <VisibilityOutlined />}</IconButton></InputAdornment>,
                sx: { borderRadius: 2, bgcolor: 'rgba(255,255,255,0.03)', '&:hover': { bgcolor: 'rgba(255,255,255,0.05)' } }
              }} 
            />
            <Button 
              fullWidth 
              type="submit" 
              variant="contained" 
              size="large" 
              sx={{ 
                py: 1.8, 
                borderRadius: 2,
                background: 'linear-gradient(135deg, #6C63FF 0%, #5A52E0 100%)', 
                fontSize: 16, 
                fontWeight: 700,
                textTransform: 'none',
                boxShadow: '0 8px 20px rgba(108,99,255,0.3)',
                '&:hover': { 
                  background: 'linear-gradient(135deg, #7B73FF 0%, #6C63FF 100%)',
                  boxShadow: '0 12px 28px rgba(108,99,255,0.4)',
                  transform: 'translateY(-2px)'
                },
                transition: 'all 0.2s'
              }}
            >
              Sign In to PulseAI
            </Button>
          </form>

          <Divider sx={{ my: 3, borderColor: 'rgba(255,255,255,0.08)' }} />
          <Box sx={{ mt: 3, p: 2, borderRadius: 2, bgcolor: 'rgba(255,255,255,0.03)', border: '1px solid rgba(255,255,255,0.05)' }}>
            <Typography variant="caption" sx={{ color: '#94A3B8', display: 'block', textAlign: 'center' }}>
              Demo: <strong style={{ color: '#6C63FF' }}>employee1</strong> · <strong style={{ color: '#00D9A6' }}>hr1</strong> · <strong style={{ color: '#FFB547' }}>globalhr</strong>
              <br />
              <span style={{ color: '#64748B', fontSize: 11 }}>Password for seeded demo accounts is: <strong>password</strong></span>
            </Typography>
          </Box>
        </CardContent>
      </Card>
      
      <style>{`
        @keyframes float {
          0%, 100% { transform: translateY(0) scale(1); }
          50% { transform: translateY(-30px) scale(1.05); }
        }
        @keyframes pulse {
          0% { transform: scale(1); opacity: 1; }
          50% { transform: scale(1.1); opacity: 0.8; }
          100% { transform: scale(1); opacity: 1; }
        }
      `}</style>
    </Box>
  );
}
