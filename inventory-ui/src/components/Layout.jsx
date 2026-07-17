import { useState } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useThemeMode } from '../context/ThemeContext';
import {
  Box, Drawer, List, ListItem, ListItemButton, ListItemIcon, ListItemText,
  AppBar, Toolbar, Typography, Avatar, Button, Chip, Menu, MenuItem, IconButton, Tooltip, Badge, Divider, Stack
} from '@mui/material';
import DashboardIcon from '@mui/icons-material/Dashboard';
import InventoryIcon from '@mui/icons-material/Inventory';
import WarehouseIcon from '@mui/icons-material/Warehouse';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import AssessmentIcon from '@mui/icons-material/Assessment';
import PeopleIcon from '@mui/icons-material/People';
import LogoutIcon from '@mui/icons-material/Logout';
import PrecisionManufacturingIcon from '@mui/icons-material/PrecisionManufacturing';
import LightModeIcon from '@mui/icons-material/LightMode';
import DarkModeIcon from '@mui/icons-material/DarkMode';
import SettingsBrightnessIcon from '@mui/icons-material/SettingsBrightness';
import NotificationsIcon from '@mui/icons-material/Notifications';
import MenuIcon from '@mui/icons-material/Menu';
import MenuOpenIcon from '@mui/icons-material/MenuOpen';

const drawerWidth = 260;

const navItems = [
  { path: '/dashboard', label: 'Dashboard', icon: <DashboardIcon />, roles: ['SYSTEM_ADMIN','INVENTORY_MANAGER','WAREHOUSE_MANAGER','PROCUREMENT_MANAGER'] },
  { path: '/dashboard/products', label: 'Products', icon: <PrecisionManufacturingIcon />, roles: ['SYSTEM_ADMIN','INVENTORY_MANAGER','WAREHOUSE_MANAGER'] },
  { path: '/dashboard/warehouses', label: 'Warehouses', icon: <WarehouseIcon />, roles: ['SYSTEM_ADMIN'] },
  { path: '/dashboard/inventory', label: 'Inventory', icon: <InventoryIcon />, roles: ['INVENTORY_MANAGER','WAREHOUSE_MANAGER'] },
  { path: '/dashboard/purchase-requests', label: 'Purchase Requests', icon: <ShoppingCartIcon />, roles: ['PROCUREMENT_MANAGER','INVENTORY_MANAGER','WAREHOUSE_MANAGER'] },
  { path: '/dashboard/reports', label: 'Reports', icon: <AssessmentIcon />, roles: ['INVENTORY_MANAGER','PROCUREMENT_MANAGER','SYSTEM_ADMIN','WAREHOUSE_MANAGER'] },
  { path: '/dashboard/users', label: 'Users', icon: <PeopleIcon />, roles: ['SYSTEM_ADMIN'] },
];

const initialNotifications = [
  { id: 1, title: 'Low Stock Alert', desc: 'SKU "EL-LAP-092" quantity is below minimum threshold at Warehouse A.', time: '5 mins ago', read: false },
  { id: 2, title: 'New Purchase Request', desc: 'Procurement manager created PR #104 for 25 units of "LED Display".', time: '1 hr ago', read: false },
  { id: 3, title: 'Stock Transfer Complete', desc: 'Successfully transferred 100 units of "USB Hub" from WH-01 to WH-03.', time: '4 hrs ago', read: true },
];

export default function Layout() {
  const { user, logout } = useAuth();
  const { mode, setMode, isDarkMode } = useThemeMode();
  const navigate = useNavigate();
  const location = useLocation();

  const [collapsed, setCollapsed] = useState(false);
  const [anchorEl, setAnchorEl] = useState(null);
  const openThemeMenu = (event) => setAnchorEl(event.currentTarget);
  const handleCloseThemeMenu = () => setAnchorEl(null);

  // Notifications Popover State
  const [notifAnchorEl, setNotifAnchorEl] = useState(null);
  const [notifications, setNotifications] = useState(initialNotifications);

  const openNotifMenu = (event) => setNotifAnchorEl(event.currentTarget);
  const handleCloseNotifMenu = () => setNotifAnchorEl(null);

  const handleMarkAllRead = () => {
    setNotifications(prev => prev.map(n => ({ ...n, read: true })));
  };

  const handleSetTheme = (newMode) => {
    setMode(newMode);
    handleCloseThemeMenu();
  };

  const handleLogout = () => { logout(); navigate('/login'); };

  const initials = user ? `${user.firstName?.[0] || ''}${user.lastName?.[0] || ''}` : '??';
  const unreadCount = notifications.filter(n => !n.read).length;

  return (
    <Box sx={{ display: 'flex' }}>
      <Drawer
        variant="permanent"
        sx={{
          width: collapsed ? 76 : drawerWidth,
          flexShrink: 0,
          transition: 'width 0.2s',
          '& .MuiDrawer-paper': {
            width: collapsed ? 76 : drawerWidth,
            boxSizing: 'border-box',
            bgcolor: isDarkMode ? '#0f172a' : '#1e293b',
            color: 'white',
            borderRight: isDarkMode ? '1px solid #1f2937' : 'none',
            transition: 'width 0.2s',
            overflowX: 'hidden'
          },
        }}
      >
        <Box sx={{
          p: 2.5,
          display: 'flex',
          justifyContent: collapsed ? 'center' : 'flex-start',
          alignItems: 'center',
          gap: 1.5,
          borderBottom: isDarkMode ? '1px solid #1f2937' : '1px solid #334155'
        }}>
          <Avatar sx={{ bgcolor: isDarkMode ? '#6366f1' : '#4f46e5', width: 32, height: 32, fontSize: 16, fontWeight: 700 }}>I</Avatar>
          {!collapsed && (
            <Typography variant="h6" sx={{ fontWeight: 700, fontSize: 18, letterSpacing: '0.5px' }}>Inventory MS</Typography>
          )}
        </Box>
        
        {!collapsed && (
          <Typography variant="caption" sx={{ px: 2.5, pt: 2.5, pb: 0.5, color: '#64748b', letterSpacing: 1.2, fontSize: 10, fontWeight: 600, textTransform: 'uppercase' }}>
            Main Menu
          </Typography>
        )}
        
        <List sx={{ px: 1.5, pt: collapsed ? 2 : 0.5 }}>
          {navItems.filter(item => item.roles.includes(user?.role)).map(item => {
            const active = location.pathname === item.path;
            
            const buttonContent = (
              <ListItemButton
                onClick={() => navigate(item.path)}
                selected={active}
                sx={{
                  borderRadius: 2,
                  py: 1,
                  px: collapsed ? 1.5 : 2,
                  justifyContent: collapsed ? 'center' : 'flex-start',
                  color: active ? 'white' : '#94a3b8',
                  '&.Mui-selected': {
                    bgcolor: isDarkMode ? 'rgba(99,102,241,0.2)' : 'rgba(79,70,229,0.2)',
                    color: 'white',
                    fontWeight: 600,
                    '&:hover': { bgcolor: isDarkMode ? 'rgba(99,102,241,0.25)' : 'rgba(79,70,229,0.25)' }
                  },
                  '&:hover': { bgcolor: 'rgba(255,255,255,0.05)', color: 'white' },
                }}
              >
                <ListItemIcon sx={{ color: 'inherit', minWidth: collapsed ? 0 : 36, justifyContent: 'center' }}>{item.icon}</ListItemIcon>
                {!collapsed && (
                  <ListItemText primary={item.label} primaryTypographyProps={{ fontSize: 13.5, fontWeight: active ? 600 : 400 }} />
                )}
              </ListItemButton>
            );

            return (
              <ListItem key={item.path} disablePadding sx={{ mb: 0.5 }}>
                {collapsed ? (
                  <Tooltip title={item.label} placement="right">
                    {buttonContent}
                  </Tooltip>
                ) : (
                  buttonContent
                )}
              </ListItem>
            );
          })}
        </List>
      </Drawer>
      
      <Box sx={{ flexGrow: 1, ml: 0 }}>
        <AppBar position="static" elevation={0} sx={{ bgcolor: 'background.paper', borderBottom: '1px solid', borderColor: 'divider', color: 'text.primary' }}>
          <Toolbar sx={{ justifyContent: 'space-between', px: '24px !important' }}>
            <Stack direction="row" alignItems="center" spacing={1}>
              <IconButton onClick={() => setCollapsed(prev => !prev)} sx={{ color: 'text.secondary', mr: 1 }}>
                {collapsed ? <MenuIcon /> : <MenuOpenIcon />}
              </IconButton>
              <Typography variant="h5" sx={{ fontWeight: 700, fontSize: 20 }}>
                {navItems.find(n => n.path === location.pathname)?.label || 'Dashboard'}
              </Typography>
            </Stack>
            
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2.5 }}>
              
              {/* Notification Badge Button */}
              <Tooltip title="Notifications">
                <IconButton onClick={openNotifMenu} sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 2, p: 0.75, color: 'text.primary' }}>
                  <Badge badgeContent={unreadCount} color="error">
                    <NotificationsIcon sx={{ fontSize: 20 }} />
                  </Badge>
                </IconButton>
              </Tooltip>

              <Menu
                anchorEl={notifAnchorEl}
                open={Boolean(notifAnchorEl)}
                onClose={handleCloseNotifMenu}
                PaperProps={{
                  sx: {
                    mt: 1,
                    width: 320,
                    boxShadow: '0 4px 20px 0 rgba(0, 0, 0, 0.1)',
                    borderRadius: 3,
                    maxHeight: 400,
                  }
                }}
              >
                <Box sx={{ p: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Typography variant="subtitle2" fontWeight={700}>Notifications</Typography>
                  {unreadCount > 0 && (
                    <Button size="small" onClick={handleMarkAllRead} sx={{ fontSize: 11, fontWeight: 600, p: 0 }}>
                      Mark all read
                    </Button>
                  )}
                </Box>
                <Divider />
                {notifications.length === 0 ? (
                  <Box sx={{ p: 3, textAlign: 'center' }}>
                    <Typography variant="body2" color="text.secondary">No notifications</Typography>
                  </Box>
                ) : (
                  <List disablePadding sx={{ py: 0.5 }}>
                    {notifications.map((n) => (
                      <MenuItem
                        key={n.id}
                        onClick={() => {
                          setNotifications(prev => prev.map(item => item.id === n.id ? { ...item, read: true } : item));
                        }}
                        sx={{
                          py: 1.5,
                          px: 2,
                          alignItems: 'flex-start',
                          flexDirection: 'column',
                          gap: 0.5,
                          borderBottom: '1px solid',
                          borderColor: 'divider',
                          bgcolor: n.read ? 'transparent' : (isDarkMode ? 'rgba(99,102,241,0.05)' : 'rgba(79,70,229,0.03)'),
                          '&:last-child': { borderBottom: 'none' }
                        }}
                      >
                        <Stack direction="row" justifyContent="space-between" width="100%" alignItems="center">
                          <Typography variant="body2" fontWeight={n.read ? 600 : 800} color={n.read ? 'text.primary' : 'primary.main'}>
                            {n.title}
                          </Typography>
                          <Typography variant="caption" color="text.secondary" sx={{ fontSize: 9.5 }}>
                            {n.time}
                          </Typography>
                        </Stack>
                        <Typography variant="caption" color="text.secondary" sx={{ lineHeight: 1.4, display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden', whiteSpace: 'normal' }}>
                          {n.desc}
                        </Typography>
                      </MenuItem>
                    ))}
                  </List>
                )}
              </Menu>

              <Tooltip title="Theme mode">
                <IconButton onClick={openThemeMenu} sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 2, p: 0.75 }}>
                  {mode === 'light' ? <LightModeIcon sx={{ fontSize: 20 }} /> : mode === 'dark' ? <DarkModeIcon sx={{ fontSize: 20 }} /> : <SettingsBrightnessIcon sx={{ fontSize: 20 }} />}
                </IconButton>
              </Tooltip>
              
              <Menu
                anchorEl={anchorEl}
                open={Boolean(anchorEl)}
                onClose={handleCloseThemeMenu}
                PaperProps={{
                  sx: {
                    mt: 1,
                    boxShadow: '0 4px 20px 0 rgba(0, 0, 0, 0.1)',
                    borderRadius: 2
                  }
                }}
              >
                <MenuItem onClick={() => handleSetTheme('light')} selected={mode === 'light'} sx={{ fontSize: 13, gap: 1 }}>
                  <LightModeIcon fontSize="small" /> Light
                </MenuItem>
                <MenuItem onClick={() => handleSetTheme('dark')} selected={mode === 'dark'} sx={{ fontSize: 13, gap: 1 }}>
                  <DarkModeIcon fontSize="small" /> Dark
                </MenuItem>
                <MenuItem onClick={() => handleSetTheme('system')} selected={mode === 'system'} sx={{ fontSize: 13, gap: 1 }}>
                  <SettingsBrightnessIcon fontSize="small" /> System
                </MenuItem>
              </Menu>

              <Box sx={{ textAlign: 'right' }}>
                <Typography variant="body2" fontWeight={600} sx={{ lineHeight: 1.2 }}>{user?.firstName} {user?.lastName}</Typography>
                <Chip label={user?.role?.replace(/_/g, ' ')} size="small" sx={{ bgcolor: isDarkMode ? '#3b0764' : '#f3e8ff', color: isDarkMode ? '#d8b4fe' : '#7e22ce', fontWeight: 600, fontSize: 10, height: 20, mt: 0.5 }} />
              </Box>
              <Avatar sx={{ bgcolor: isDarkMode ? '#6366f1' : '#4f46e5', width: 36, height: 36, fontSize: 14, fontWeight: 700 }}>{initials}</Avatar>
              <Button variant="outlined" size="small" startIcon={<LogoutIcon />} onClick={handleLogout} sx={{ color: 'text.secondary', borderColor: 'divider', px: 1.5, py: 0.75 }}>
                Logout
              </Button>
            </Box>
          </Toolbar>
        </AppBar>
        <Box sx={{ p: 4, bgcolor: 'background.default', minHeight: 'calc(100vh - 64px)' }}>
          <Outlet />
        </Box>
      </Box>
    </Box>
  );
}
