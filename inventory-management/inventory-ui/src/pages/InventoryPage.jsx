import { useState, useEffect } from 'react';
import { inventoryApi, productApi, warehouseApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';
import { useFormik } from 'formik';
import * as yup from 'yup';
import {
  Box, Card, Typography, Button, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, MenuItem, Stack, ToggleButton, ToggleButtonGroup
} from '@mui/material';

export default function InventoryPage() {
  const { user } = useAuth();
  const [inventory, setInventory] = useState([]);
  const [products, setProducts] = useState([]);
  const [warehouses, setWarehouses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('view');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [modalType, setModalType] = useState('inbound');

  const isWarehouseRestricted = user?.warehouseId && (user.role === 'WAREHOUSE_MANAGER' || user.role === 'INVENTORY_MANAGER');

  useEffect(() => {
    const params = isWarehouseRestricted ? user.warehouseId : undefined;
    Promise.all([inventoryApi.getAll(params), productApi.getAll(), warehouseApi.getAll()])
      .then(([inv, prod, wh]) => { setInventory(inv.data); setProducts(prod.data); setWarehouses(wh.data); })
      .catch(() => toast.error('Failed to load data'))
      .finally(() => setLoading(false));
  }, [user]);

  const formik = useFormik({
    initialValues: { productId: '', warehouseId: user?.warehouseId?.toString() || '', quantity: '', reference: '' },
    validationSchema: yup.object({
      productId: yup.string().required('Required'),
      warehouseId: yup.string().required('Required'),
      quantity: yup.number().min(1, 'Min 1').required('Required'),
    }),
    onSubmit: async (values, { setSubmitting, resetForm }) => {
      try {
        const data = { ...values, quantity: parseInt(values.quantity), productId: parseInt(values.productId), warehouseId: parseInt(values.warehouseId) };
        if (modalType === 'inbound') await inventoryApi.inbound(data);
        else if (modalType === 'outbound') await inventoryApi.outbound(data);
        else if (modalType === 'adjustment') await inventoryApi.adjustment(data);
        toast.success(`${modalType.charAt(0).toUpperCase() + modalType.slice(1)} successful`);
        setDialogOpen(false);
        resetForm();
        const params = isWarehouseRestricted ? user.warehouseId : undefined;
        const res = await inventoryApi.getAll(params);
        setInventory(res.data);
      } catch (err) { toast.error(err.response?.data?.message || 'Operation failed'); }
      finally { setSubmitting(false); }
    },
  });

  const transferFormik = useFormik({
    initialValues: { productId: '', fromWarehouseId: user?.warehouseId?.toString() || '', toWarehouseId: '', quantity: '', reference: '' },
    validationSchema: yup.object({
      productId: yup.string().required('Required'),
      fromWarehouseId: yup.string().required('Required'),
      toWarehouseId: yup.string().required('Required'),
      quantity: yup.number().min(1).required('Required'),
    }),
    onSubmit: async (values, { setSubmitting, resetForm }) => {
      try {
        const data = { ...values, quantity: parseInt(values.quantity), productId: parseInt(values.productId), fromWarehouseId: parseInt(values.fromWarehouseId), toWarehouseId: parseInt(values.toWarehouseId) };
        await inventoryApi.transfer(data);
        toast.success('Transfer successful');
        setDialogOpen(false);
        resetForm();
        const params = isWarehouseRestricted ? user.warehouseId : undefined;
        const res = await inventoryApi.getAll(params);
        setInventory(res.data);
      } catch (err) { toast.error(err.response?.data?.message || 'Transfer failed'); }
      finally { setSubmitting(false); }
    },
  });

  const openModal = (type) => {
    setModalType(type);
    formik.resetForm();
    if (user?.warehouseId) formik.setFieldValue('warehouseId', user.warehouseId.toString());
    transferFormik.resetForm();
    if (user?.warehouseId) transferFormik.setFieldValue('fromWarehouseId', user.warehouseId.toString());
    setDialogOpen(true);
  };

  const getProductName = (id) => products.find(p => p.id === id)?.name || `Product #${id}`;
  const getWhName = (id) => warehouses.find(w => w.id === id)?.name || `WH #${id}`;
  const getProductUom = (id) => products.find(p => p.id === id)?.unitOfMeasure || '';

  if (loading) return <Typography>Loading...</Typography>;

  return (
    <Box>
      <Card sx={{ mb: 2 }}>
        <Box sx={{ p: 2 }}>
          <ToggleButtonGroup value={activeTab} exclusive onChange={(_, v) => v && setActiveTab(v)} size="small" sx={{ mb: 2 }}>
            <ToggleButton value="view">View Inventory</ToggleButton>
            <ToggleButton value="actions">Stock Actions</ToggleButton>
          </ToggleButtonGroup>

          {activeTab === 'view' && (
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Product</TableCell>
                    <TableCell>UOM</TableCell>
                    <TableCell>Warehouse</TableCell>
                    <TableCell>Quantity</TableCell>
                    <TableCell>Last Updated</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {inventory.length === 0 ? (
                    <TableRow><TableCell colSpan={5} align="center">No inventory records</TableCell></TableRow>
                  ) : inventory.map(item => (
                    <TableRow key={item.id} hover>
                      <TableCell>{getProductName(item.productId)}</TableCell>
                      <TableCell>{getProductUom(item.productId) || 'PCS'}</TableCell>
                      <TableCell>{getWhName(item.warehouseId)}</TableCell>
                      <TableCell><Typography fontWeight={700}>{item.quantity}</Typography></TableCell>
                      <TableCell>{item.lastUpdated ? new Date(item.lastUpdated).toLocaleString() : '-'}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}

          {activeTab === 'actions' && (
            <Stack direction="row" spacing={1} flexWrap="wrap">
              <Button variant="contained" color="success" onClick={() => openModal('inbound')}>+ Inbound Stock</Button>
              <Button variant="contained" color="warning" onClick={() => openModal('outbound')}>- Outbound Stock</Button>
              <Button variant="contained" onClick={() => { setModalType('transfer'); openModal('transfer'); }}>Transfer Stock</Button>
              <Button variant="outlined" onClick={() => openModal('adjustment')}>Adjust Stock</Button>
            </Stack>
          )}
        </Box>
      </Card>

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        {modalType !== 'transfer' ? (
          <form onSubmit={formik.handleSubmit}>
            <DialogTitle>{modalType === 'inbound' ? 'Inbound Stock' : modalType === 'outbound' ? 'Outbound Stock' : 'Adjust Stock'}</DialogTitle>
            <DialogContent>
              <Stack spacing={2} sx={{ mt: 1 }}>
                <TextField label="Product" name="productId" select value={formik.values.productId} onChange={formik.handleChange} fullWidth required>
                  <MenuItem value="">Select product</MenuItem>
                  {products.map(p => <MenuItem key={p.id} value={p.id.toString()}>{p.sku} - {p.name}</MenuItem>)}
                </TextField>
                <TextField label="Warehouse" name="warehouseId" select value={formik.values.warehouseId} onChange={formik.handleChange} fullWidth required disabled={isWarehouseRestricted}>
                  <MenuItem value="">Select warehouse</MenuItem>
                  {warehouses.map(w => <MenuItem key={w.id} value={w.id.toString()}>{w.code} - {w.name}</MenuItem>)}
                </TextField>
                <TextField label="Quantity" name="quantity" type="number" value={formik.values.quantity} onChange={formik.handleChange} onBlur={formik.handleBlur} error={formik.touched.quantity && Boolean(formik.errors.quantity)} helperText={formik.touched.quantity && formik.errors.quantity} fullWidth required />
                {modalType === 'adjustment' && <Typography variant="caption" color="warning.main">This will set the exact quantity.</Typography>}
                <TextField label="Reference (optional)" name="reference" value={formik.values.reference} onChange={formik.handleChange} fullWidth />
              </Stack>
            </DialogContent>
            <DialogActions>
              <Button onClick={() => setDialogOpen(false)}>Cancel</Button>
              <Button type="submit" variant="contained">Submit</Button>
            </DialogActions>
          </form>
        ) : (
          <form onSubmit={transferFormik.handleSubmit}>
            <DialogTitle>Transfer Stock</DialogTitle>
            <DialogContent>
              <Stack spacing={2} sx={{ mt: 1 }}>
                <TextField label="Product" name="productId" select value={transferFormik.values.productId} onChange={transferFormik.handleChange} fullWidth required>
                  <MenuItem value="">Select product</MenuItem>
                  {products.map(p => <MenuItem key={p.id} value={p.id.toString()}>{p.sku} - {p.name}</MenuItem>)}
                </TextField>
                <TextField label="From Warehouse" name="fromWarehouseId" select value={transferFormik.values.fromWarehouseId} onChange={transferFormik.handleChange} fullWidth required disabled={isWarehouseRestricted}>
                  <MenuItem value="">Select source</MenuItem>
                  {warehouses.map(w => <MenuItem key={w.id} value={w.id.toString()}>{w.code} - {w.name}</MenuItem>)}
                </TextField>
                <TextField label="To Warehouse" name="toWarehouseId" select value={transferFormik.values.toWarehouseId} onChange={transferFormik.handleChange} fullWidth required>
                  <MenuItem value="">Select destination</MenuItem>
                  {warehouses.map(w => <MenuItem key={w.id} value={w.id.toString()}>{w.code} - {w.name}</MenuItem>)}
                </TextField>
                <TextField label="Quantity" name="quantity" type="number" value={transferFormik.values.quantity} onChange={transferFormik.handleChange} fullWidth required />
                <TextField label="Reference (optional)" name="reference" value={transferFormik.values.reference} onChange={transferFormik.handleChange} fullWidth />
              </Stack>
            </DialogContent>
            <DialogActions>
              <Button onClick={() => setDialogOpen(false)}>Cancel</Button>
              <Button type="submit" variant="contained">Transfer</Button>
            </DialogActions>
          </form>
        )}
      </Dialog>
    </Box>
  );
}
