import React from 'react';
import { Drawer, Box, List, ListItemButton, ListItemIcon, ListItemText, Typography, IconButton, Chip, Divider } from '@mui/material';
import { DashboardOutlined, AssignmentOutlined, AutoAwesomeOutlined, NotificationsOutlined, BarChartOutlined, MapOutlined, LogoutOutlined, FavoriteBorderOutlined, PersonAddOutlined } from '@mui/icons-material';
import { NavLink, useLocation, useNavigate } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import { logout } from '../../store/authSlice';

const DRAWER_WIDTH = 260;

const NAV_ITEMS = {
  EMPLOYEE: [
    { label: 'Dashboard', icon: <DashboardOutlined />, path: '/app/dashboard' },
    { label: 'My Survey', icon: <AssignmentOutlined />, path: '/app/survey' },
    { label: 'Notifications', icon: <NotificationsOutlined />, path: '/app/notifications' },
  ],
  HR: [
    { label: 'Dashboard', icon: <DashboardOutlined />, path: '/app/dashboard' },
    { label: 'Manage Surveys', icon: <AssignmentOutlined />, path: '/app/manage-surveys' },
    { label: 'Review Drafts', icon: <AutoAwesomeOutlined />, path: '/app/drafts' },
    { label: 'Register Employee', icon: <PersonAddOutlined />, path: '/app/register-employee' },
    { label: 'Reports', icon: <MapOutlined />, path: '/app/reports' },
    { label: 'Notifications', icon: <NotificationsOutlined />, path: '/app/notifications' },
  ],
  GLOBAL_HR: [
    { label: 'Dashboard', icon: <DashboardOutlined />, path: '/app/dashboard' },
    { label: 'Global Analytics', icon: <BarChartOutlined />, path: '/app/analytics' },
    { label: 'Location Reports', icon: <MapOutlined />, path: '/app/reports' },
    { label: 'Notifications', icon: <NotificationsOutlined />, path: '/app/notifications' },
  ],
};

export default function Sidebar({ role }) {
  const location = useLocation();
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const user = useSelector((s) => s.auth.user);
  let items = NAV_ITEMS[role] || NAV_ITEMS.EMPLOYEE;
  if (role === 'EMPLOYEE' && user?.status === 'PENDING_ONBOARDING') {
    items = [
      { label: 'Onboarding Survey', icon: <AssignmentOutlined />, path: '/app/survey' }
    ];
  }

  const handleLogout = () => {
    dispatch(logout());
    navigate('/login');
  };

  return (
    <Drawer variant="permanent" sx={{
      width: DRAWER_WIDTH, flexShrink: 0,
      '& .MuiDrawer-paper': {
        width: DRAWER_WIDTH, boxSizing: 'border-box',
        background: '#3B82F6',
        borderRight: '1px solid rgba(255,255,255,0.1)',
        color: '#FFFFFF',
      },
    }}>
      <Box sx={{ p: 3, display: 'flex', alignItems: 'center', gap: 1.5 }}>
        <FavoriteBorderOutlined sx={{ color: '#FFFFFF', fontSize: 28 }} />
        <Typography variant="h5" sx={{ color: '#FFFFFF', fontWeight: 800 }}>
          PulseAI
        </Typography>
      </Box>
      <Divider sx={{ borderColor: 'rgba(255,255,255,0.15)' }} />
      <List sx={{ px: 1.5, pt: 2, flex: 1 }}>
        {items.map((item) => {
          const isActive = location.pathname === item.path;
          return (
            <ListItemButton key={item.path} component={NavLink} to={item.path} sx={{
              borderRadius: 2, mb: 0.5, color: isActive ? '#FFFFFF' : 'rgba(255,255,255,0.7)',
              bgcolor: isActive ? 'rgba(255,255,255,0.2)' : 'transparent',
              '&:hover': { bgcolor: 'rgba(255,255,255,0.1)', color: '#FFFFFF' },
              transition: 'all 0.2s ease',
            }}>
              <ListItemIcon sx={{ color: 'inherit', minWidth: 40 }}>{item.icon}</ListItemIcon>
              <ListItemText primary={item.label} primaryTypographyProps={{ fontSize: 14, fontWeight: isActive ? 600 : 400 }} />
            </ListItemButton>
          );
        })}
      </List>
      <Divider sx={{ borderColor: 'rgba(255,255,255,0.15)' }} />
      <Box sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
        <Box sx={{ flex: 1 }}>
          <Typography variant="body2" sx={{ fontWeight: 600, color: '#FFFFFF' }}>{user?.name}</Typography>
          <Chip label={role} size="small" sx={{ mt: 0.5, fontSize: 10, height: 20, bgcolor: 'rgba(255,255,255,0.2)', color: '#FFFFFF' }} />
        </Box>
        <IconButton onClick={handleLogout} sx={{ color: 'rgba(255,255,255,0.7)', '&:hover': { color: '#FF6B6B' } }}>
          <LogoutOutlined fontSize="small" />
        </IconButton>
      </Box>
    </Drawer>
  );
}
