import { useState, useEffect } from 'react';
import { warehouseApi } from '../services/api';
import toast from 'react-hot-toast';
import { useFormik } from 'formik';
import * as yup from 'yup';
import {
  Box, Card, Typography, Button, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Chip, Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, Stack, IconButton
} from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';

const whSchema = yup.object({
  code: yup.string().required('Required'),
  name: yup.string().required('Required'),
  city: yup.string(),
  state: yup.string(),
  country: yup.string(),
  capacity: yup.number().min(0).nullable(),
});

export default function WarehousesPage() {
  const [warehouses, setWarehouses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editing, setEditing] = useState(null);

  useEffect(() => { fetchWarehouses(); }, []);

  const fetchWarehouses = async () => {
    try {
      const res = await warehouseApi.getAll();
      setWarehouses(res.data);
    } catch { toast.error('Failed to load warehouses'); }
    finally { setLoading(false); }
  };

  const formik = useFormik({
    initialValues: { code: '', name: '', city: '', state: '', country: '', capacity: '' },
    validationSchema: whSchema,
    onSubmit: async (values, { setSubmitting, resetForm }) => {
      try {
        const data = { ...values, capacity: parseInt(values.capacity) || 0 };
        if (editing) {
          await warehouseApi.update(editing.id, data);
          toast.success('Warehouse updated');
        } else {
          await warehouseApi.create(data);
          toast.success('Warehouse created');
        }
        setDialogOpen(false); setEditing(null); resetForm();
        fetchWarehouses();
      } catch (err) { toast.error(err.response?.data?.message || 'Operation failed'); }
      finally { setSubmitting(false); }
    },
  });

  const openEdit = (w) => {
    setEditing(w);
    formik.setValues({ code: w.code, name: w.name, city: w.city || '', state: w.state || '', country: w.country || '', capacity: w.capacity?.toString() || '' });
    setDialogOpen(true);
  };

  const openCreate = () => { setEditing(null); formik.resetForm(); setDialogOpen(true); };

  if (loading) return <Typography>Loading...</Typography>;

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
        <Typography variant="h6" fontWeight={600}>All Warehouses</Typography>
        <Button variant="contained" onClick={openCreate}>+ Add Warehouse</Button>
      </Stack>

      <Card>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Code</TableCell>
                <TableCell>Name</TableCell>
                <TableCell>City</TableCell>
                <TableCell>State</TableCell>
                <TableCell>Country</TableCell>
                <TableCell>Capacity</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {warehouses.length === 0 ? (
                <TableRow><TableCell colSpan={8} align="center">No warehouses found</TableCell></TableRow>
              ) : warehouses.map(w => (
                <TableRow key={w.id} hover>
                  <TableCell><Typography fontWeight={600}>{w.code}</Typography></TableCell>
                  <TableCell>{w.name}</TableCell>
                  <TableCell>{w.city}</TableCell>
                  <TableCell>{w.state}</TableCell>
                  <TableCell>{w.country}</TableCell>
                  <TableCell>{w.capacity}</TableCell>
                  <TableCell><Chip label={w.status} size="small" color="success" /></TableCell>
                  <TableCell>
                    <IconButton size="small" onClick={() => openEdit(w)}><EditIcon fontSize="small" /></IconButton>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </Card>

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <form onSubmit={formik.handleSubmit}>
          <DialogTitle>{editing ? 'Edit Warehouse' : 'Add Warehouse'}</DialogTitle>
          <DialogContent>
            <Stack spacing={2} sx={{ mt: 1 }}>
              <Stack direction="row" spacing={2}>
                <TextField label="Code" name="code" value={formik.values.code} onChange={formik.handleChange} onBlur={formik.handleBlur} error={formik.touched.code && Boolean(formik.errors.code)} helperText={formik.touched.code && formik.errors.code} fullWidth required />
                <TextField label="Name" name="name" value={formik.values.name} onChange={formik.handleChange} onBlur={formik.handleBlur} error={formik.touched.name && Boolean(formik.errors.name)} helperText={formik.touched.name && formik.errors.name} fullWidth required />
              </Stack>
              <Stack direction="row" spacing={2}>
                <TextField label="City" name="city" value={formik.values.city} onChange={formik.handleChange} fullWidth />
                <TextField label="State" name="state" value={formik.values.state} onChange={formik.handleChange} fullWidth />
              </Stack>
              <Stack direction="row" spacing={2}>
                <TextField label="Country" name="country" value={formik.values.country} onChange={formik.handleChange} fullWidth />
                <TextField label="Capacity" name="capacity" type="number" value={formik.values.capacity} onChange={formik.handleChange} fullWidth />
              </Stack>
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setDialogOpen(false)}>Cancel</Button>
            <Button type="submit" variant="contained">{editing ? 'Update' : 'Create'}</Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>
  );
}
