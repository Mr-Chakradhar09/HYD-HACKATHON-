import React, { useContext, useState } from 'react';
import { Box, Typography, Badge, Avatar, IconButton, Menu, MenuItem, ListItemIcon, ListItemText } from '@mui/material';
import { NotificationsOutlined, DarkModeOutlined, LightModeOutlined, SettingsBrightnessOutlined } from '@mui/icons-material';
import { useSelector } from 'react-redux';
import { useLocation, useNavigate } from 'react-router-dom';
import { ColorModeContext } from '../ThemeModeContext';

const PAGE_TITLES = {
  '/app/dashboard': 'Dashboard',
  '/app/survey': 'Pulse Survey',
  '/app/drafts': 'AI-Generated Drafts',
  '/app/analytics': 'Global Analytics',
  '/app/reports': 'Reports',
  '/app/notifications': 'Notifications',
};

export default function TopBar() {
  const location = useLocation();
  const navigate = useNavigate();
  const user = useSelector((s) => s.auth.user);
  const notifications = useSelector((s) => s.notification.notifications);
  const title = PAGE_TITLES[location.pathname] || 'PulseAI';
  const initials = user?.name?.split(' ').map(w => w[0]).join('') || 'U';

  const { themeMode, setThemeMode } = useContext(ColorModeContext);
  const [anchorEl, setAnchorEl] = useState(null);
  const open = Boolean(anchorEl);

  const handleOpenMenu = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleCloseMenu = () => {
    setAnchorEl(null);
  };

  const handleSelectMode = (mode) => {
    setThemeMode(mode);
    handleCloseMenu();
  };

  const getThemeIcon = () => {
    if (themeMode === 'light') return <LightModeOutlined />;
    if (themeMode === 'dark') return <DarkModeOutlined />;
    return <SettingsBrightnessOutlined />;
  };

  return (
    <Box sx={{ height: 64, px: 4, display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: '1px solid', borderColor: 'divider' }}>
      <Typography variant="h5" sx={{ fontWeight: 700, color: 'text.primary' }}>{title}</Typography>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
        {/* Theme Toggle Icon Button */}
        <IconButton onClick={handleOpenMenu} sx={{ color: 'text.secondary' }}>
          {getThemeIcon()}
        </IconButton>
        <Menu
          anchorEl={anchorEl}
          open={open}
          onClose={handleCloseMenu}
          sx={{
            '& .MuiPaper-root': {
              mt: 1,
              minWidth: 150,
              boxShadow: '0 4px 20px rgba(0,0,0,0.15)',
              border: '1px solid',
              borderColor: 'divider',
            }
          }}
        >
          <MenuItem selected={themeMode === 'light'} onClick={() => handleSelectMode('light')}>
            <ListItemIcon><LightModeOutlined fontSize="small" /></ListItemIcon>
            <ListItemText>Light</ListItemText>
          </MenuItem>
          <MenuItem selected={themeMode === 'dark'} onClick={() => handleSelectMode('dark')}>
            <ListItemIcon><DarkModeOutlined fontSize="small" /></ListItemIcon>
            <ListItemText>Dark</ListItemText>
          </MenuItem>
          <MenuItem selected={themeMode === 'system'} onClick={() => handleSelectMode('system')}>
            <ListItemIcon><SettingsBrightnessOutlined fontSize="small" /></ListItemIcon>
            <ListItemText>System</ListItemText>
          </MenuItem>
        </Menu>

        <IconButton onClick={() => navigate('/app/notifications')} sx={{ color: 'text.secondary' }}>
          <Badge badgeContent={notifications.length} color="primary" max={9}>
            <NotificationsOutlined />
          </Badge>
        </IconButton>
        <Avatar sx={{ width: 36, height: 36, bgcolor: '#6C63FF', fontSize: 14, fontWeight: 600 }}>{initials}</Avatar>
      </Box>
    </Box>
  );
}
