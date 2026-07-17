import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useThemeMode } from '../context/ThemeContext';
import {
  Box, Container, Typography, Button, Grid, Card, CardContent, Stack, AppBar, Toolbar, Avatar, LinearProgress
} from '@mui/material';
import InventoryIcon from '@mui/icons-material/Inventory';
import SecurityIcon from '@mui/icons-material/Security';
import SpeedIcon from '@mui/icons-material/Speed';
import InsightsIcon from '@mui/icons-material/Insights';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';

export default function LandingPage() {
  const { user } = useAuth();
  const { isDarkMode } = useThemeMode();
  const navigate = useNavigate();

  const handleStart = () => {
    if (user) {
      navigate('/dashboard');
    } else {
      navigate('/login');
    }
  };

  const features = [
    {
      title: 'Real-time Tracking',
      desc: 'Monitor stock levels, movements, and transfers instantly across multiple warehouses.',
      icon: <SpeedIcon sx={{ fontSize: 32, color: isDarkMode ? '#6366f1' : '#4f46e5' }} />
    },
    {
      title: 'Smart Analytics',
      desc: 'Gain visibility into distribution metrics and warehouse capacity logs via custom reports.',
      icon: <InsightsIcon sx={{ fontSize: 32, color: isDarkMode ? '#0ea5e9' : '#0ea5e9' }} />
    },
    {
      title: 'Enterprise Security',
      desc: 'Robust role-based access control protecting critical operations for admins and managers.',
      icon: <SecurityIcon sx={{ fontSize: 32, color: '#10b981' }} />
    }
  ];

  return (
    <Box sx={{
      minHeight: '100vh',
      bgcolor: 'background.default',
      color: 'text.primary',
      transition: 'background-color 0.3s, color 0.3s'
    }}>
      {/* Navigation Header */}
      <AppBar position="static" color="transparent" elevation={0} sx={{ borderBottom: '1px solid', borderColor: 'divider' }}>
        <Container maxWidth="lg">
          <Toolbar sx={{ justifyContent: 'space-between', px: '0px !important' }}>
            <Stack direction="row" alignItems="center" spacing={1}>
              <Avatar sx={{ bgcolor: isDarkMode ? '#6366f1' : '#4f46e5', width: 36, height: 36 }}>
                <InventoryIcon sx={{ fontSize: 20 }} />
              </Avatar>
              <Typography variant="h6" sx={{ fontWeight: 800, letterSpacing: '0.5px' }}>
                Inventory MS
              </Typography>
            </Stack>
            <Stack direction="row" spacing={1.5} alignItems="center">
              {user ? (
                <Button variant="contained" onClick={() => navigate('/dashboard')} sx={{ borderRadius: 2.5, px: 3 }}>
                  Workspace
                </Button>
              ) : (
                <Button variant="contained" onClick={() => navigate('/login')} sx={{ borderRadius: 2.5, px: 3, fontWeight: 600 }}>
                  Sign In
                </Button>
              )}
            </Stack>
          </Toolbar>
        </Container>
      </AppBar>

      {/* Hero Section */}
      <Container maxWidth="lg" sx={{ pt: { xs: 8, md: 12 }, pb: { xs: 8, md: 10 } }}>
        <Box sx={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          textAlign: 'center',
          maxWidth: 900,
          mx: 'auto',
          mb: 8
        }}>
          <Typography
            variant="h2"
            sx={{
              fontWeight: 800,
              fontSize: { xs: '2.5rem', md: '3.75rem' },
              lineHeight: 1.15,
              mb: 2.5,
              background: isDarkMode
                ? 'linear-gradient(135deg, #ffffff 0%, #a5b4fc 100%)'
                : 'linear-gradient(135deg, #1e1b4b 0%, #4f46e5 100%)',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent',
            }}
          >
            Intelligent Logistics & Inventory Console
          </Typography>
          <Typography variant="body1" color="text.secondary" sx={{ fontSize: '1.1rem', lineHeight: 1.6, mb: 4.5, maxWidth: 680, mx: 'auto' }}>
            Streamline operations, optimize storage levels, control warehouse capacities, and execute real-time stock transfers with our enterprise-grade management console.
          </Typography>
          <Button
            variant="contained"
            size="large"
            onClick={handleStart}
            endIcon={<ArrowForwardIcon />}
            sx={{
              px: 4.5,
              py: 1.8,
              borderRadius: 3,
              fontWeight: 600,
              fontSize: 15,
              boxShadow: isDarkMode
                ? '0 4px 20px 0 rgba(99, 102, 241, 0.4)'
                : '0 4px 20px 0 rgba(79, 70, 229, 0.3)',
            }}
          >
            {user ? 'Go to Dashboard' : 'Get Started'}
          </Button>

          {/* Centered Horizontal System Status Summary */}
          <Card sx={{
            width: '100%',
            maxWidth: 650,
            mt: 8,
            p: 5,
            borderRadius: 4,
            background: isDarkMode ? 'rgba(17, 24, 39, 0.4)' : 'rgba(255, 255, 255, 0.6)',
            backdropFilter: 'blur(20px)',
            border: '1px solid',
            borderColor: 'divider',
            boxShadow: isDarkMode
              ? '0 10px 40px 0 rgba(0, 0, 0, 0.3)'
              : '0 10px 40px 0 rgba(99, 102, 241, 0.04)',
          }}>
            <Stack
              direction={{ xs: 'column', sm: 'row' }}
              spacing={3}
              justifyContent="space-around"
              alignItems="center"
              divider={<Box sx={{ width: { xs: '40%', sm: '1px' }, height: { xs: '1px', sm: '40px' }, bgcolor: 'divider' }} />}
            >
              <Box>
                <Typography variant="caption" color="text.secondary" sx={{ fontSize: 10, fontWeight: 700, letterSpacing: 1, textTransform: 'uppercase' }}>WAREHOUSES</Typography>
                <Typography variant="h6" fontWeight={800} sx={{ fontSize: 18, mt: 0.5 }}>04 Active</Typography>
              </Box>
              <Box>
                <Typography variant="caption" color="text.secondary" sx={{ fontSize: 10, fontWeight: 700, letterSpacing: 1, textTransform: 'uppercase' }}>SKU DIRECTORY</Typography>
                <Typography variant="h6" fontWeight={800} sx={{ fontSize: 18, mt: 0.5 }}>140+ Items</Typography>
              </Box>
              <Box>
                <Typography variant="caption" color="text.secondary" sx={{ fontSize: 10, fontWeight: 700, letterSpacing: 1, textTransform: 'uppercase' }}>DATABASE STATUS</Typography>
                <Typography variant="h6" fontWeight={800} sx={{ fontSize: 18, mt: 0.5, color: '#10b981' }}>Connected</Typography>
              </Box>
              <Box>
                <Typography variant="caption" color="text.secondary" sx={{ fontSize: 10, fontWeight: 700, letterSpacing: 1, textTransform: 'uppercase' }}>ACTIVE SESSIONS</Typography>
                <Typography variant="h6" fontWeight={800} sx={{ fontSize: 18, mt: 0.5 }}>12 Online</Typography>
              </Box>
            </Stack>
          </Card>
        </Box>

        {/* Feature Grid Section */}
        <Box sx={{ mt: { xs: 8, md: 12 } }}>
          <Typography variant="h4" align="center" sx={{ fontWeight: 800, mb: 6 }}>
            Consolidated Platform Capabilities
          </Typography>
          <Box sx={{
            display: 'flex',
            flexDirection: 'row',
            flexWrap: 'nowrap',
            gap: 3,
            width: '100%',
            justifyContent: 'center',
            alignItems: 'stretch'
          }}>
            {features.map((f, i) => (
              <Card key={i} sx={{
                flex: '1 1 0px',
                maxWidth: 'calc(33.333% - 16px)',
                transition: 'transform 0.2s, box-shadow 0.2s',
                '&:hover': {
                  transform: 'translateY(-4px)',
                  boxShadow: isDarkMode ? '0 12px 30px 0 rgba(0,0,0,0.4)' : '0 12px 30px 0 rgba(0,0,0,0.05)'
                }
              }}>
                <CardContent sx={{ p: { xs: 2, sm: 3.5 }, display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center', height: '100%' }}>
                  <Box sx={{ p: 1, borderRadius: '50%', bgcolor: isDarkMode ? 'rgba(255,255,255,0.02)' : 'rgba(0,0,0,0.02)', mb: 2, display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                    {f.icon}
                  </Box>
                  <Typography variant="h6" fontWeight={800} sx={{ mb: 1, fontSize: { xs: 13, sm: 16 } }}>{f.title}</Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ lineHeight: 1.5, fontSize: { xs: 11, sm: 13.5 } }}>{f.desc}</Typography>
                </CardContent>
              </Card>
            ))}
          </Box>
        </Box>
      </Container>
    </Box>
  );
}

// Simple Helper Component
function Divider() {
  const { isDarkMode } = useThemeMode();
  return <Box sx={{ height: '1px', bgcolor: isDarkMode ? '#1f2937' : '#e2e8f0', my: 1 }} />;
}

// Simple Chip Component wrapper for Landing Page
function Chip({ label, color, size }) {
  return (
    <Box sx={{
      bgcolor: color === 'success' ? '#dcfce7' : 'rgba(0,0,0,0.05)',
      color: color === 'success' ? '#15803d' : 'inherit',
      fontSize: '11px',
      fontWeight: 700,
      px: 1.5,
      py: 0.5,
      borderRadius: 1.5
    }}>
      {label}
    </Box>
  );
}
