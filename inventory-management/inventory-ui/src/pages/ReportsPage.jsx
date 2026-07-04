import { useState, useEffect } from 'react';
import { reportApi } from '../services/api';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';
import {
  Box, Card, Typography, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, ToggleButton, ToggleButtonGroup
} from '@mui/material';

const COLORS = ['#4f46e5', '#0ea5e9', '#22c55e', '#f59e0b', '#ef4444', '#8b5cf6'];

export default function ReportsPage() {
  const [inventoryReport, setInventoryReport] = useState([]);
  const [movementReport, setMovementReport] = useState([]);
  const [warehouseReport, setWarehouseReport] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeReport, setActiveReport] = useState('inventory');

  useEffect(() => {
    Promise.all([reportApi.inventory(), reportApi.movement(), reportApi.warehouse()])
      .then(([inv, mov, wh]) => { setInventoryReport(inv.data); setMovementReport(mov.data); setWarehouseReport(wh.data); })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Typography>Loading reports...</Typography>;

  return (
    <Box>
      <Card sx={{ mb: 2, p: 2 }}>
        <ToggleButtonGroup value={activeReport} exclusive onChange={(_, v) => v && setActiveReport(v)} size="small">
          <ToggleButton value="inventory">Inventory Report</ToggleButton>
          <ToggleButton value="movement">Movement Report</ToggleButton>
          <ToggleButton value="warehouse">Warehouse Report</ToggleButton>
        </ToggleButtonGroup>
      </Card>

      {activeReport === 'inventory' && (
        <Box>
          <Card sx={{ p: 2, mb: 2 }}>
            <Typography variant="h6" fontSize={16} fontWeight={600} sx={{ mb: 2 }}>Inventory Levels by Warehouse</Typography>
            <ResponsiveContainer width="100%" height={400}>
              <BarChart data={inventoryReport}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                <XAxis dataKey="warehouseId" tick={{ fontSize: 12 }} label={{ value: 'Warehouse ID', position: 'bottom' }} />
                <YAxis tick={{ fontSize: 12 }} label={{ value: 'Quantity', angle: -90, position: 'left' }} />
                <Tooltip />
                <Bar dataKey="quantity" fill="#4f46e5" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </Card>
          <Card>
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow><TableCell>Product ID</TableCell><TableCell>Warehouse ID</TableCell><TableCell>Quantity</TableCell><TableCell>Last Updated</TableCell></TableRow>
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
          <Card sx={{ p: 2, mb: 2 }}>
            <Typography variant="h6" fontSize={16} fontWeight={600} sx={{ mb: 2 }}>Stock Movements by Type</Typography>
            <ResponsiveContainer width="100%" height={350}>
              <BarChart data={movementReport}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                <XAxis dataKey="type" tick={{ fontSize: 12 }} />
                <YAxis tick={{ fontSize: 12 }} />
                <Tooltip />
                <Bar dataKey="count" fill="#0ea5e9" radius={[4, 4, 0, 0]} />
                <Bar dataKey="totalQuantity" fill="#4f46e5" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </Card>
          <Card sx={{ p: 2 }}>
            <Typography variant="h6" fontSize={16} fontWeight={600} sx={{ mb: 2 }}>Movement Distribution</Typography>
            <ResponsiveContainer width="100%" height={350}>
              <PieChart>
                <Pie data={movementReport} dataKey="count" nameKey="type" cx="50%" cy="50%" outerRadius={120} label>
                  {movementReport.map((_, idx) => <Cell key={idx} fill={COLORS[idx % COLORS.length]} />)}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </Card>
        </Box>
      )}

      {activeReport === 'warehouse' && (
        <Box>
          <Card sx={{ p: 2, mb: 2 }}>
            <Typography variant="h6" fontSize={16} fontWeight={600} sx={{ mb: 2 }}>Warehouse Stock Summary</Typography>
            <ResponsiveContainer width="100%" height={400}>
              <BarChart data={warehouseReport}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                <XAxis dataKey="warehouseId" tick={{ fontSize: 12 }} label={{ value: 'Warehouse', position: 'bottom' }} />
                <YAxis tick={{ fontSize: 12 }} />
                <Tooltip />
                <Bar dataKey="totalProducts" fill="#22c55e" name="Products" radius={[4, 4, 0, 0]} />
                <Bar dataKey="totalQuantity" fill="#4f46e5" name="Quantity" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </Card>
          <Card>
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow><TableCell>Warehouse ID</TableCell><TableCell>Total Products</TableCell><TableCell>Total Quantity</TableCell></TableRow>
                </TableHead>
                <TableBody>
                  {warehouseReport.map((item, idx) => (
                    <TableRow key={idx} hover>
                      <TableCell><Typography fontWeight={600}>Warehouse #{item.warehouseId}</Typography></TableCell>
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
