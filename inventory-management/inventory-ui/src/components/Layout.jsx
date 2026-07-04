import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  Box, Drawer, List, ListItem, ListItemButton, ListItemIcon, ListItemText,
  AppBar, Toolbar, Typography, Avatar, Button, Chip, Divider
} from '@mui/material';
import DashboardIcon from '@mui/icons-material/Dashboard';
import InventoryIcon from '@mui/icons-material/Inventory';
import WarehouseIcon from '@mui/icons-material/Warehouse';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import AssessmentIcon from '@mui/icons-material/Assessment';
import PeopleIcon from '@mui/icons-material/People';
import LogoutIcon from '@mui/icons-material/Logout';
import PrecisionManufacturingIcon from '@mui/icons-material/PrecisionManufacturing';

const drawerWidth = 260;

const navItems = [
  { path: '/', label: 'Dashboard', icon: <DashboardIcon />, roles: ['SYSTEM_ADMIN','INVENTORY_MANAGER','WAREHOUSE_MANAGER','PROCUREMENT_MANAGER'] },
  { path: '/products', label: 'Products', icon: <PrecisionManufacturingIcon />, roles: ['SYSTEM_ADMIN','INVENTORY_MANAGER','WAREHOUSE_MANAGER'] },
  { path: '/warehouses', label: 'Warehouses', icon: <WarehouseIcon />, roles: ['SYSTEM_ADMIN'] },
  { path: '/inventory', label: 'Inventory', icon: <InventoryIcon />, roles: ['INVENTORY_MANAGER','WAREHOUSE_MANAGER'] },
  { path: '/purchase-requests', label: 'Purchase Requests', icon: <ShoppingCartIcon />, roles: ['PROCUREMENT_MANAGER','INVENTORY_MANAGER','WAREHOUSE_MANAGER'] },
  { path: '/reports', label: 'Reports', icon: <AssessmentIcon />, roles: ['INVENTORY_MANAGER','PROCUREMENT_MANAGER','SYSTEM_ADMIN','WAREHOUSE_MANAGER'] },
  { path: '/users', label: 'Users', icon: <PeopleIcon />, roles: ['SYSTEM_ADMIN'] },
];

export default function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => { logout(); navigate('/login'); };

  const initials = user ? `${user.firstName?.[0] || ''}${user.lastName?.[0] || ''}` : '??';

  return (
    <Box sx={{ display: 'flex' }}>
      <Drawer
        variant="permanent"
        sx={{
          width: drawerWidth,
          flexShrink: 0,
          '& .MuiDrawer-paper': { width: drawerWidth, boxSizing: 'border-box', bgcolor: '#0f172a', color: 'white', borderRight: 'none' },
        }}
      >
        <Box sx={{ p: 2.5, display: 'flex', alignItems: 'center', gap: 1.5, borderBottom: '1px solid #334155' }}>
          <Avatar sx={{ bgcolor: '#4f46e5', width: 32, height: 32, fontSize: 16, fontWeight: 700 }}>I</Avatar>
          <Typography variant="h6" sx={{ fontWeight: 600, fontSize: 18 }}>Inventory MS</Typography>
        </Box>
        <Typography variant="caption" sx={{ px: 2.5, pt: 2, pb: 0.5, color: '#64748b', letterSpacing: 1, fontSize: 11, textTransform: 'uppercase' }}>
          Main Menu
        </Typography>
        <List sx={{ px: 1 }}>
          {navItems.filter(item => item.roles.includes(user?.role)).map(item => {
            const active = location.pathname === item.path;
            return (
              <ListItem key={item.path} disablePadding sx={{ mb: 0.5 }}>
                <ListItemButton
                  onClick={() => navigate(item.path)}
                  selected={active}
                  sx={{
                    borderRadius: 1.5,
                    color: active ? 'white' : '#94a3b8',
                    '&.Mui-selected': { bgcolor: 'rgba(79,70,229,0.15)', color: 'white', '&:hover': { bgcolor: 'rgba(79,70,229,0.2)' } },
                    '&:hover': { bgcolor: 'rgba(255,255,255,0.05)', color: 'white' },
                  }}
                >
                  <ListItemIcon sx={{ color: 'inherit', minWidth: 36 }}>{item.icon}</ListItemIcon>
                  <ListItemText primary={item.label} primaryTypographyProps={{ fontSize: 14 }} />
                </ListItemButton>
              </ListItem>
            );
          })}
        </List>
      </Drawer>
      <Box sx={{ flexGrow: 1, ml: 0 }}>
        <AppBar position="static" elevation={0} sx={{ bgcolor: 'white', borderBottom: '1px solid #e2e8f0', color: '#1e293b' }}>
          <Toolbar sx={{ justifyContent: 'space-between' }}>
            <Typography variant="h5" sx={{ fontWeight: 600, fontSize: 22 }}>
              {navItems.find(n => n.path === location.pathname)?.label || 'Dashboard'}
            </Typography>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              <Box sx={{ textAlign: 'right' }}>
                <Typography variant="body2" fontWeight={500}>{user?.firstName} {user?.lastName}</Typography>
                <Chip label={user?.role?.replace(/_/g, ' ')} size="small" sx={{ bgcolor: '#e9d5ff', color: '#9333ea', fontWeight: 500, fontSize: 11, height: 22 }} />
              </Box>
              <Avatar sx={{ bgcolor: '#4f46e5', width: 36, height: 36, fontSize: 14, fontWeight: 600 }}>{initials}</Avatar>
              <Button variant="outlined" size="small" startIcon={<LogoutIcon />} onClick={handleLogout} sx={{ color: '#64748b', borderColor: '#e2e8f0' }}>
                Logout
              </Button>
            </Box>
          </Toolbar>
        </AppBar>
        <Box sx={{ p: 3, bgcolor: '#f8fafc', minHeight: 'calc(100vh - 64px)' }}>
          <Outlet />
        </Box>
      </Box>
    </Box>
  );
}
