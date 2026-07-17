import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useThemeMode } from '../context/ThemeContext';
import { inventoryApi, productApi, warehouseApi, purchaseRequestApi } from '../services/api';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, AreaChart, Area } from 'recharts';
import {
  Box, Grid, Card, CardContent, Typography, Button, Stack, Avatar, useTheme, Chip, ToggleButtonGroup, ToggleButton, Table, TableBody, TableCell, TableContainer, TableRow, Alert
} from '@mui/material';
import InventoryIcon from '@mui/icons-material/Inventory';
import WarehouseIcon from '@mui/icons-material/Warehouse';
import PrecisionManufacturingIcon from '@mui/icons-material/PrecisionManufacturing';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';

const statCards = [
  { label: 'Total Products', key: 'products', icon: <PrecisionManufacturingIcon />, color: '#6366f1', bg: 'rgba(99,102,241,0.15)' },
  { label: 'Warehouses', key: 'warehouses', icon: <WarehouseIcon />, color: '#10b981', bg: 'rgba(16,185,129,0.15)' },
  { label: 'Inventory Items', key: 'inventoryItems', icon: <InventoryIcon />, color: '#f59e0b', bg: 'rgba(245,158,11,0.15)' },
  { label: 'Pending PR', key: 'pendingPR', icon: <ShoppingCartIcon />, color: '#ec4899', bg: 'rgba(236,72,153,0.15)' },
];

export default function Dashboard() {
  const { user } = useAuth();
  const { isDarkMode } = useThemeMode();
  const theme = useTheme();
  const navigate = useNavigate();

  const [stats, setStats] = useState({ products: 0, warehouses: 0, inventoryItems: 0, pendingPR: 0 });
  const [inventoryData, setInventoryData] = useState([]);
  const [lowStockAlerts, setLowStockAlerts] = useState([]);
  const [recentRequests, setRecentRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [chartType, setChartType] = useState('bar'); // 'bar' | 'area'

  const colorsList = isDarkMode
    ? ['#6366f1', '#0ea5e9', '#10b981', '#f59e0b', '#ef4444']
    : ['#4f46e5', '#0ea5e9', '#22c55e', '#f59e0b', '#ef4444'];

  useEffect(() => {
    const fetchData = async () => {
      try {
        const invParams = user?.warehouseId && user.role !== 'PROCUREMENT_MANAGER' && user.role !== 'SYSTEM_ADMIN'
          ? user.warehouseId : undefined;

        const [prodRes, whRes, invRes, prRes] = await Promise.allSettled([
          productApi.getAll(),
          warehouseApi.getAll(),
          inventoryApi.getAll(invParams),
          purchaseRequestApi.getAll(user?.warehouseId && user.role !== 'PROCUREMENT_MANAGER' && user.role !== 'SYSTEM_ADMIN' ? user.warehouseId : undefined),
        ]);

        const productsList = prodRes.status === 'fulfilled' ? prodRes.value.data : [];
        const warehousesList = whRes.status === 'fulfilled' ? whRes.value.data : [];
        const inventoryList = invRes.status === 'fulfilled' ? invRes.value.data : [];
        const requestsList = prRes.status === 'fulfilled' ? prRes.value.data : [];

        // Count pending
        const pendingPR = requestsList.filter(p => p.status === 'PENDING').length;

        setStats({
          products: productsList.length,
          warehouses: warehousesList.length,
          inventoryItems: inventoryList.length,
          pendingPR
        });

        // Setup chart data
        const whMap = {};
        inventoryList.forEach(item => {
          const wh = warehousesList.find(w => w.id === item.warehouseId);
          const key = wh ? wh.code : `WH #${item.warehouseId}`;
          whMap[key] = (whMap[key] || 0) + item.quantity;
        });
        setInventoryData(Object.entries(whMap).map(([name, value]) => ({ name, value })));

        // Calculate low stock alerts
        const alerts = [];
        inventoryList.forEach(item => {
          const product = productsList.find(p => p.id === item.productId);
          if (product && product.minimumStock != null && item.quantity < product.minimumStock) {
            const wh = warehousesList.find(w => w.id === item.warehouseId);
            alerts.push({
              id: item.id,
              productName: product.name,
              sku: product.sku,
              warehouseName: wh ? wh.name : `WH #${item.warehouseId}`,
              quantity: item.quantity,
              minStock: product.minimumStock,
            });
          }
        });
        setLowStockAlerts(alerts.slice(0, 5)); // show top 5 alerts

        // Setup recent requests
        const sortedRequests = [...requestsList]
          .sort((a, b) => b.id - a.id)
          .slice(0, 5)
          .map(r => {
            const product = productsList.find(p => p.id === r.productId);
            const wh = warehousesList.find(w => w.id === r.warehouseId);
            return {
              ...r,
              productName: product ? product.name : `Product #${r.productId}`,
              warehouseName: wh ? wh.name : `WH #${r.warehouseId}`
            };
          });
        setRecentRequests(sortedRequests);

      } catch (err) {
        console.error('Dashboard fetch error:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [user]);

  if (loading) return <Typography sx={{ p: 2 }}>Loading dashboard...</Typography>;

  return (
    <Box>
      {/* Stat Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        {statCards.map(s => (
          <Grid item xs={12} sm={6} md={3} key={s.key}>
            <Card sx={{
              transition: 'transform 0.2s, box-shadow 0.2s',
              borderLeft: `5px solid ${s.color}`,
              '&:hover': {
                transform: 'translateY(-4px)',
                boxShadow: isDarkMode ? '0 12px 30px 0 rgba(0,0,0,0.5)' : '0 12px 30px 0 rgba(0,0,0,0.06)'
              }
            }}>
              <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2.5, p: '24px !important' }}>
                <Avatar sx={{
                  bgcolor: s.bg,
                  color: s.color,
                  width: 52,
                  height: 52
                }}>
                  {s.icon}
                </Avatar>
                <Box>
                  <Typography variant="h5" fontWeight={800} sx={{ mb: 0.5 }}>{stats[s.key]}</Typography>
                  <Typography variant="body2" color="text.secondary" fontWeight={600}>{s.label}</Typography>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      {/* Main Stats Charts Section */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} md={8}>
          <Card>
            <CardContent sx={{ p: 3 }}>
              <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 3 }}>
                <Typography variant="h6" fontSize={16} fontWeight={700}>Inventory Stock by Warehouse</Typography>
                <ToggleButtonGroup
                  value={chartType}
                  exclusive
                  onChange={(_, v) => v && setChartType(v)}
                  size="small"
                  sx={{ height: 32 }}
                >
                  <ToggleButton value="bar">Bar</ToggleButton>
                  <ToggleButton value="area">Area</ToggleButton>
                </ToggleButtonGroup>
              </Stack>
              <ResponsiveContainer width="100%" height={320}>
                {chartType === 'bar' ? (
                  <BarChart data={inventoryData}>
                    <CartesianGrid strokeDasharray="3 3" stroke={isDarkMode ? '#1f2937' : '#f1f5f9'} />
                    <XAxis dataKey="name" tick={{ fontSize: 12, fill: theme.palette.text.secondary }} axisLine={false} tickLine={false} />
                    <YAxis tick={{ fontSize: 12, fill: theme.palette.text.secondary }} axisLine={false} tickLine={false} />
                    <Tooltip
                      contentStyle={{
                        backgroundColor: theme.palette.background.paper,
                        borderColor: theme.palette.divider,
                        borderRadius: 8,
                        color: theme.palette.text.primary,
                        boxShadow: '0 4px 20px 0 rgba(0,0,0,0.08)'
                      }}
                    />
                    <Bar dataKey="value" fill={isDarkMode ? '#6366f1' : '#4f46e5'} radius={[6, 6, 0, 0]} maxBarSize={50} />
                  </BarChart>
                ) : (
                  <AreaChart data={inventoryData}>
                    <defs>
                      <linearGradient id="colorValue" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor={isDarkMode ? '#6366f1' : '#4f46e5'} stopOpacity={0.4}/>
                        <stop offset="95%" stopColor={isDarkMode ? '#6366f1' : '#4f46e5'} stopOpacity={0.0}/>
                      </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" stroke={isDarkMode ? '#1f2937' : '#f1f5f9'} />
                    <XAxis dataKey="name" tick={{ fontSize: 12, fill: theme.palette.text.secondary }} axisLine={false} tickLine={false} />
                    <YAxis tick={{ fontSize: 12, fill: theme.palette.text.secondary }} axisLine={false} tickLine={false} />
                    <Tooltip
                      contentStyle={{
                        backgroundColor: theme.palette.background.paper,
                        borderColor: theme.palette.divider,
                        borderRadius: 8,
                        color: theme.palette.text.primary,
                        boxShadow: '0 4px 20px 0 rgba(0,0,0,0.08)'
                      }}
                    />
                    <Area type="monotone" dataKey="value" stroke={isDarkMode ? '#6366f1' : '#4f46e5'} strokeWidth={3} fillOpacity={1} fill="url(#colorValue)" />
                  </AreaChart>
                )}
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={4}>
          <Card sx={{ height: '100%' }}>
            <CardContent sx={{ p: 3, display: 'flex', flexDirection: 'column', height: '100%' }}>
              <Typography variant="h6" fontSize={16} fontWeight={700} sx={{ mb: 3 }}>Capacity Distribution</Typography>
              {inventoryData.length > 0 ? (
                <Box sx={{ flexGrow: 1, display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                  <ResponsiveContainer width="100%" height={260}>
                    <PieChart>
                      <Pie data={inventoryData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={85} innerRadius={55} paddingAngle={4} label>
                        {inventoryData.map((_, idx) => <Cell key={idx} fill={colorsList[idx % colorsList.length]} />)}
                      </Pie>
                      <Tooltip
                        contentStyle={{
                          backgroundColor: theme.palette.background.paper,
                          borderColor: theme.palette.divider,
                          borderRadius: 8,
                          color: theme.palette.text.primary
                        }}
                      />
                    </PieChart>
                  </ResponsiveContainer>
                </Box>
              ) : (
                <Typography align="center" color="text.secondary" sx={{ my: 'auto' }}>No data to display</Typography>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Low Stock Alerts & Recent Purchase Requests Section */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        {/* Low Stock Alerts */}
        <Grid item xs={12} md={6}>
          <Card sx={{ height: '100%' }}>
            <CardContent sx={{ p: 3 }}>
              <Stack direction="row" alignItems="center" spacing={1} sx={{ mb: 3 }}>
                <WarningAmberIcon sx={{ color: '#ef4444' }} />
                <Typography variant="h6" fontSize={16} fontWeight={700}>Low Stock Alerts</Typography>
              </Stack>
              {lowStockAlerts.length > 0 ? (
                <TableContainer>
                  <Table size="small">
                    <TableBody>
                      {lowStockAlerts.map(alert => (
                        <TableRow key={alert.id} hover>
                          <TableCell sx={{ py: 1.5 }}>
                            <Typography variant="body2" fontWeight={600}>{alert.productName}</Typography>
                            <Typography variant="caption" color="text.secondary" sx={{ fontFamily: 'monospace' }}>{alert.sku}</Typography>
                          </TableCell>
                          <TableCell sx={{ py: 1.5 }}>
                            <Typography variant="body2" color="text.secondary">{alert.warehouseName}</Typography>
                          </TableCell>
                          <TableCell sx={{ py: 1.5 }} align="right">
                            <Chip
                              label={`${alert.quantity} / ${alert.minStock}`}
                              size="small"
                              color="error"
                              variant="outlined"
                              sx={{ fontWeight: 700, fontSize: 11 }}
                            />
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              ) : (
                <Alert severity="success" sx={{ borderRadius: 2 }}>
                  All stock items satisfy minimum threshold requirements.
                </Alert>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Recent Purchase Requests */}
        <Grid item xs={12} md={6}>
          <Card sx={{ height: '100%' }}>
            <CardContent sx={{ p: 3 }}>
              <Typography variant="h6" fontSize={16} fontWeight={700} sx={{ mb: 3 }}>Recent Purchase Requests</Typography>
              {recentRequests.length > 0 ? (
                <TableContainer>
                  <Table size="small">
                    <TableBody>
                      {recentRequests.map(req => (
                        <TableRow key={req.id} hover>
                          <TableCell sx={{ py: 1.5 }}>
                            <Typography variant="body2" fontWeight={600}>{req.productName}</Typography>
                            <Typography variant="caption" color="text.secondary">{req.warehouseName}</Typography>
                          </TableCell>
                          <TableCell sx={{ py: 1.5 }} align="center">
                            <Chip
                              label={req.status}
                              size="small"
                              color={req.status === 'PENDING' ? 'warning' : 'success'}
                              sx={{ fontWeight: 600, fontSize: 10, height: 20 }}
                            />
                          </TableCell>
                          <TableCell sx={{ py: 1.5 }} align="right">
                            <Typography variant="body2" fontWeight={700}>Qty: {req.requiredQuantity}</Typography>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              ) : (
                <Typography align="center" color="text.secondary" sx={{ py: 4 }}>No recent purchase requests</Typography>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Quick Actions */}
      <Card sx={{ border: '1px dashed', borderColor: 'divider', bgcolor: 'transparent', boxShadow: 'none' }}>
        <CardContent sx={{ p: 3 }}>
          <Typography variant="h6" fontSize={16} fontWeight={700} sx={{ mb: 2 }}>Workspace Operations</Typography>
          <Stack direction="row" spacing={2} flexWrap="wrap" useFlexGap sx={{ gap: 1.5 }}>
            <Button variant="contained" size="large" onClick={() => navigate('/dashboard/inventory')} sx={{ px: 3, py: 1.2 }}>
              Manage Inventory
            </Button>
            <Button variant="contained" color="success" size="large" onClick={() => navigate('/dashboard/products')} sx={{ px: 3, py: 1.2, color: 'white' }}>
              View Products
            </Button>
            <Button variant="outlined" size="large" onClick={() => navigate('/dashboard/reports')} sx={{ px: 3, py: 1.2 }}>
              View Reports
            </Button>
          </Stack>
        </CardContent>
      </Card>
    </Box>
  );
}
