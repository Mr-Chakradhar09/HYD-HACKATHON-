import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { inventoryApi, productApi, warehouseApi, purchaseRequestApi } from '../services/api';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';
import {
  Box, Grid, Card, CardContent, Typography, Button, Stack, Avatar, IconButton
} from '@mui/material';
import InventoryIcon from '@mui/icons-material/Inventory';
import WarehouseIcon from '@mui/icons-material/Warehouse';
import PrecisionManufacturingIcon from '@mui/icons-material/PrecisionManufacturing';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';

const COLORS = ['#4f46e5', '#0ea5e9', '#22c55e', '#f59e0b', '#ef4444'];

const statCards = [
  { label: 'Total Products', key: 'products', icon: <PrecisionManufacturingIcon />, color: '#2563eb', bg: '#dbeafe' },
  { label: 'Warehouses', key: 'warehouses', icon: <WarehouseIcon />, color: '#16a34a', bg: '#dcfce7' },
  { label: 'Inventory Items', key: 'inventoryItems', icon: <InventoryIcon />, color: '#d97706', bg: '#fef3c7' },
  { label: 'Pending PR', key: 'pendingPR', icon: <ShoppingCartIcon />, color: '#9333ea', bg: '#e9d5ff' },
];

export default function Dashboard() {
  const { user } = useAuth();
  const [stats, setStats] = useState({ products: 0, warehouses: 0, inventoryItems: 0, pendingPR: 0 });
  const [inventoryData, setInventoryData] = useState([]);
  const [loading, setLoading] = useState(true);

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

        const products = prodRes.status === 'fulfilled' ? prodRes.value.data.length : 0;
        const warehouses = whRes.status === 'fulfilled' ? whRes.value.data.length : 0;
        const inventory = invRes.status === 'fulfilled' ? invRes.value.data : [];
        const pendingPR = prRes.status === 'fulfilled' ? prRes.value.data.filter(p => p.status === 'PENDING').length : 0;

        setStats({ products, warehouses, inventoryItems: inventory.length, pendingPR });

        const whMap = {};
        inventory.forEach(item => {
          const key = `Warehouse ${item.warehouseId}`;
          whMap[key] = (whMap[key] || 0) + item.quantity;
        });
        setInventoryData(Object.entries(whMap).map(([name, value]) => ({ name, value })));
      } catch (err) {
        console.error('Dashboard fetch error:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [user]);

  if (loading) return <Typography>Loading dashboard...</Typography>;

  return (
    <Box>
      <Grid container spacing={2} sx={{ mb: 3 }}>
        {statCards.map(s => (
          <Grid item xs={12} sm={6} md={3} key={s.key}>
            <Card>
              <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2, p: '16px !important' }}>
                <Avatar sx={{ bgcolor: s.bg, color: s.color, width: 48, height: 48 }}>{s.icon}</Avatar>
                <Box>
                  <Typography variant="h5" fontWeight={700}>{stats[s.key]}</Typography>
                  <Typography variant="body2" color="text.secondary">{s.label}</Typography>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      {inventoryData.length > 0 && (
        <Grid container spacing={2} sx={{ mb: 3 }}>
          <Grid item xs={12} md={7}>
            <Card>
              <CardContent>
                <Typography variant="h6" fontSize={16} fontWeight={600} sx={{ mb: 2 }}>Inventory by Warehouse</Typography>
                <ResponsiveContainer width="100%" height={300}>
                  <BarChart data={inventoryData}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                    <XAxis dataKey="name" tick={{ fontSize: 12 }} />
                    <YAxis tick={{ fontSize: 12 }} />
                    <Tooltip />
                    <Bar dataKey="value" fill="#4f46e5" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} md={5}>
            <Card>
              <CardContent>
                <Typography variant="h6" fontSize={16} fontWeight={600} sx={{ mb: 2 }}>Distribution</Typography>
                <ResponsiveContainer width="100%" height={300}>
                  <PieChart>
                    <Pie data={inventoryData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={100} label>
                      {inventoryData.map((_, idx) => <Cell key={idx} fill={COLORS[idx % COLORS.length]} />)}
                    </Pie>
                    <Tooltip />
                  </PieChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}

      <Card>
        <CardContent>
          <Typography variant="h6" fontSize={16} fontWeight={600} sx={{ mb: 2 }}>Quick Actions</Typography>
          <Stack direction="row" spacing={1} flexWrap="wrap">
            <Button variant="contained" onClick={() => window.location.href = '/inventory'}>Manage Inventory</Button>
            <Button variant="contained" color="success" onClick={() => window.location.href = '/products'}>View Products</Button>
            <Button variant="outlined" onClick={() => window.location.href = '/reports'}>View Reports</Button>
          </Stack>
        </CardContent>
      </Card>
    </Box>
  );
}
