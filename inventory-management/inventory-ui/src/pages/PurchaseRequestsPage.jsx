import { useState, useEffect } from 'react';
import { purchaseRequestApi, productApi, warehouseApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';
import { useFormik } from 'formik';
import * as yup from 'yup';
import {
  Box, Card, Typography, Button, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Chip, Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, MenuItem, Stack
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';

export default function PurchaseRequestsPage() {
  const { user } = useAuth();
  const [requests, setRequests] = useState([]);
  const [products, setProducts] = useState([]);
  const [warehouses, setWarehouses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);

  const isProcurement = user?.role === 'PROCUREMENT_MANAGER';
  const isWarehouseRestricted = user?.warehouseId && (user.role === 'WAREHOUSE_MANAGER' || user.role === 'INVENTORY_MANAGER');
  const params = isWarehouseRestricted && !isProcurement ? user.warehouseId : undefined;

  useEffect(() => {
    Promise.all([purchaseRequestApi.getAll(params), productApi.getAll(), warehouseApi.getAll()])
      .then(([pr, prod, wh]) => { setRequests(pr.data); setProducts(prod.data); setWarehouses(wh.data); })
      .catch(() => toast.error('Failed to load data'))
      .finally(() => setLoading(false));
  }, [user]);

  const formik = useFormik({
    initialValues: { productId: '', warehouseId: user?.warehouseId?.toString() || '', requiredQuantity: '' },
    validationSchema: yup.object({
      productId: yup.string().required('Required'),
      warehouseId: yup.string().required('Required'),
      requiredQuantity: yup.number().min(1, 'Min 1').required('Required'),
    }),
    onSubmit: async (values, { setSubmitting, resetForm }) => {
      try {
        const data = { ...values, requiredQuantity: parseInt(values.requiredQuantity), productId: parseInt(values.productId), warehouseId: parseInt(values.warehouseId) };
        await purchaseRequestApi.create(data);
        toast.success('Purchase request created');
        setDialogOpen(false);
        resetForm();
        if (user?.warehouseId) formik.setFieldValue('warehouseId', user.warehouseId.toString());
        const res = await purchaseRequestApi.getAll(params);
        setRequests(res.data);
      } catch (err) { toast.error(err.response?.data?.message || 'Failed to create'); }
      finally { setSubmitting(false); }
    },
  });

  const handleApprove = async (id) => {
    try {
      await purchaseRequestApi.approve(id);
      toast.success('Purchase request approved');
      const res = await purchaseRequestApi.getAll(params);
      setRequests(res.data);
    } catch { toast.error('Failed to approve'); }
  };

  const getProductName = (id) => products.find(p => p.id === id)?.name || `Product #${id}`;
  const getWhName = (id) => warehouses.find(w => w.id === id)?.name || `WH #${id}`;

  if (loading) return <Typography>Loading...</Typography>;

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
        <Typography variant="h6" fontWeight={600}>All Purchase Requests</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => { formik.resetForm(); if (user?.warehouseId) formik.setFieldValue('warehouseId', user.warehouseId.toString()); setDialogOpen(true); }}>
          New Request
        </Button>
      </Stack>

      <Card>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>ID</TableCell>
                <TableCell>Product</TableCell>
                <TableCell>Warehouse</TableCell>
                <TableCell>Quantity</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Created</TableCell>
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {requests.length === 0 ? (
                <TableRow><TableCell colSpan={7} align="center">No purchase requests</TableCell></TableRow>
              ) : requests.map(pr => (
                <TableRow key={pr.id} hover>
                  <TableCell>#{pr.id}</TableCell>
                  <TableCell>{getProductName(pr.productId)}</TableCell>
                  <TableCell>{getWhName(pr.warehouseId)}</TableCell>
                  <TableCell><Typography fontWeight={700}>{pr.requiredQuantity}</Typography></TableCell>
                  <TableCell>
                    <Chip label={pr.status} size="small" color={pr.status === 'PENDING' ? 'warning' : 'success'} />
                  </TableCell>
                  <TableCell>{pr.createdAt ? new Date(pr.createdAt).toLocaleDateString() : '-'}</TableCell>
                  <TableCell>
                    {pr.status === 'PENDING' && (
                      <Button size="small" variant="contained" color="success" startIcon={<CheckCircleIcon />} onClick={() => handleApprove(pr.id)}>
                        Approve
                      </Button>
                    )}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </Card>

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <form onSubmit={formik.handleSubmit}>
          <DialogTitle>Create Purchase Request</DialogTitle>
          <DialogContent>
            <Stack spacing={2} sx={{ mt: 1 }}>
              <TextField label="Product" name="productId" select value={formik.values.productId} onChange={formik.handleChange} fullWidth required>
                <MenuItem value="">Select product</MenuItem>
                {products.map(p => <MenuItem key={p.id} value={p.id.toString()}>{p.sku} - {p.name}</MenuItem>)}
              </TextField>
              <TextField label="Warehouse" name="warehouseId" select value={formik.values.warehouseId} onChange={formik.handleChange} fullWidth required disabled={!!user?.warehouseId && !isProcurement}>
                <MenuItem value="">Select warehouse</MenuItem>
                {warehouses.map(w => <MenuItem key={w.id} value={w.id.toString()}>{w.code} - {w.name}</MenuItem>)}
              </TextField>
              <TextField label="Required Quantity" name="requiredQuantity" type="number" value={formik.values.requiredQuantity} onChange={formik.handleChange} fullWidth required />
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setDialogOpen(false)}>Cancel</Button>
            <Button type="submit" variant="contained">Create</Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>
  );
}
