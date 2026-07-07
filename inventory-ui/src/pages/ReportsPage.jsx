import { useState, useEffect } from 'react';
import { reportApi } from '../services/api';
import { useThemeMode } from '../context/ThemeContext';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';
import {
  Box, Card, Typography, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, ToggleButton, ToggleButtonGroup, useTheme
} from '@mui/material';

export default function ReportsPage() {
  const [inventoryReport, setInventoryReport] = useState([]);
  const [movementReport, setMovementReport] = useState([]);
  const [warehouseReport, setWarehouseReport] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeReport, setActiveReport] = useState('inventory');
  const { isDarkMode } = useThemeMode();
  const theme = useTheme();

  const colorsList = isDarkMode
    ? ['#6366f1', '#0ea5e9', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6']
    : ['#4f46e5', '#0ea5e9', '#22c55e', '#f59e0b', '#ef4444', '#8b5cf6'];

  useEffect(() => {
    Promise.all([reportApi.inventory(), reportApi.movement(), reportApi.warehouse()])
      .then(([inv, mov, wh]) => { setInventoryReport(inv.data); setMovementReport(mov.data); setWarehouseReport(wh.data); })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Typography sx={{ p: 2 }}>Loading reports...</Typography>;

  return (
    <Box>
      <Card sx={{ mb: 3, p: 2, border: '1px solid', borderColor: 'divider' }}>
        <ToggleButtonGroup value={activeReport} exclusive onChange={(_, v) => v && setActiveReport(v)} size="small">
          <ToggleButton value="inventory">Inventory Report</ToggleButton>
          <ToggleButton value="movement">Movement Report</ToggleButton>
          <ToggleButton value="warehouse">Warehouse Report</ToggleButton>
        </ToggleButtonGroup>
      </Card>

      {activeReport === 'inventory' && (
        <Box>
          <Card sx={{ p: 3, mb: 3 }}>
            <Typography variant="h6" fontSize={16} fontWeight={700} sx={{ mb: 3 }}>Inventory Levels by Warehouse</Typography>
            <ResponsiveContainer width="100%" height={360}>
              <BarChart data={inventoryReport}>
                <CartesianGrid strokeDasharray="3 3" stroke={isDarkMode ? '#1f2937' : '#f1f5f9'} />
                <XAxis dataKey="warehouseId" tick={{ fontSize: 12, fill: theme.palette.text.secondary }} label={{ value: 'Warehouse ID', position: 'bottom', fill: theme.palette.text.primary, style: { textAnchor: 'middle', marginTop: 10 } }} axisLine={false} tickLine={false} />
                <YAxis tick={{ fontSize: 12, fill: theme.palette.text.secondary }} label={{ value: 'Quantity', angle: -90, position: 'left', fill: theme.palette.text.primary }} axisLine={false} tickLine={false} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: theme.palette.background.paper,
                    borderColor: theme.palette.divider,
                    borderRadius: 8,
                    color: theme.palette.text.primary
                  }}
                />
                <Bar dataKey="quantity" fill={isDarkMode ? '#6366f1' : '#4f46e5'} radius={[6, 6, 0, 0]} maxBarSize={60} />
              </BarChart>
            </ResponsiveContainer>
          </Card>
          <Card>
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Product ID</TableCell>
                    <TableCell>Warehouse ID</TableCell>
                    <TableCell>Quantity</TableCell>
                    <TableCell>Last Updated</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {inventoryReport.map((item, idx) => (
                    <TableRow key={idx} hover>
                      <TableCell>{item.productId}</TableCell>
                      <TableCell>{item.warehouseId}</TableCell>
                      <TableCell><Typography fontWeight={700}>{item.quantity}</Typography></TableCell>
                      <TableCell>{item.lastUpdated ? new Date(item.lastUpdated).toLocaleString() : '-'}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Card>
        </Box>
      )}

      {activeReport === 'movement' && (
        <Box>
          <Card sx={{ p: 3, mb: 3 }}>
            <Typography variant="h6" fontSize={16} fontWeight={700} sx={{ mb: 3 }}>Stock Movements by Type</Typography>
            <ResponsiveContainer width="100%" height={360}>
              <BarChart data={movementReport}>
                <CartesianGrid strokeDasharray="3 3" stroke={isDarkMode ? '#1f2937' : '#f1f5f9'} />
                <XAxis dataKey="type" tick={{ fontSize: 12, fill: theme.palette.text.secondary }} axisLine={false} tickLine={false} />
                <YAxis tick={{ fontSize: 12, fill: theme.palette.text.secondary }} axisLine={false} tickLine={false} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: theme.palette.background.paper,
                    borderColor: theme.palette.divider,
                    borderRadius: 8,
                    color: theme.palette.text.primary
                  }}
                />
                <Bar dataKey="count" fill={isDarkMode ? '#0ea5e9' : '#0ea5e9'} radius={[6, 6, 0, 0]} name="Movement Count" />
                <Bar dataKey="totalQuantity" fill={isDarkMode ? '#6366f1' : '#4f46e5'} radius={[6, 6, 0, 0]} name="Total Quantity" />
              </BarChart>
            </ResponsiveContainer>
          </Card>
          <Card sx={{ p: 3 }}>
            <Typography variant="h6" fontSize={16} fontWeight={700} sx={{ mb: 3 }}>Movement Distribution</Typography>
            <ResponsiveContainer width="100%" height={360}>
              <PieChart>
                <Pie data={movementReport} dataKey="count" nameKey="type" cx="50%" cy="50%" outerRadius={120} label>
                  {movementReport.map((_, idx) => <Cell key={idx} fill={colorsList[idx % colorsList.length]} />)}
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
          </Card>
        </Box>
      )}

      {activeReport === 'warehouse' && (
        <Box>
          <Card sx={{ p: 3, mb: 3 }}>
            <Typography variant="h6" fontSize={16} fontWeight={700} sx={{ mb: 3 }}>Warehouse Stock Summary</Typography>
            <ResponsiveContainer width="100%" height={360}>
              <BarChart data={warehouseReport}>
                <CartesianGrid strokeDasharray="3 3" stroke={isDarkMode ? '#1f2937' : '#f1f5f9'} />
                <XAxis dataKey="warehouseId" tick={{ fontSize: 12, fill: theme.palette.text.secondary }} label={{ value: 'Warehouse ID', position: 'bottom', fill: theme.palette.text.primary }} axisLine={false} tickLine={false} />
                <YAxis tick={{ fontSize: 12, fill: theme.palette.text.secondary }} axisLine={false} tickLine={false} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: theme.palette.background.paper,
                    borderColor: theme.palette.divider,
                    borderRadius: 8,
                    color: theme.palette.text.primary
                  }}
                />
                <Bar dataKey="totalProducts" fill="#10b981" name="Products" radius={[6, 6, 0, 0]} />
                <Bar dataKey="totalQuantity" fill={isDarkMode ? '#6366f1' : '#4f46e5'} name="Quantity" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </Card>
          <Card>
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Warehouse ID</TableCell>
                    <TableCell>Total Products</TableCell>
                    <TableCell>Total Quantity</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {warehouseReport.map((item, idx) => (
                    <TableRow key={idx} hover>
                      <TableCell><Typography fontWeight={700}>Warehouse #{item.warehouseId}</Typography></TableCell>
                      <TableCell>{item.totalProducts}</TableCell>
                      <TableCell>{item.totalQuantity}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Card>
        </Box>
      )}
    </Box>
  );
}
