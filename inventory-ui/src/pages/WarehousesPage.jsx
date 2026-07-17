import { useState, useEffect } from 'react';
import { warehouseApi } from '../services/api';
import toast from 'react-hot-toast';
import { useFormik } from 'formik';
import * as yup from 'yup';
import {
  Box, Card, Typography, Button, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Chip, Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, Stack, IconButton, MenuItem, Select, InputLabel, FormControl
} from '@mui/material';

const CITY_DATA = [
  { city: 'New York', state: 'NY', country: 'USA' },
  { city: 'Los Angeles', state: 'CA', country: 'USA' },
  { city: 'Chicago', state: 'IL', country: 'USA' },
  { city: 'London', state: 'England', country: 'UK' },
  { city: 'Toronto', state: 'ON', country: 'Canada' },
  { city: 'Sydney', state: 'NSW', country: 'Australia' },
  { city: 'Mumbai', state: 'MH', country: 'India' },
  { city: 'Delhi', state: 'DL', country: 'India' },
  { city: 'Bangalore', state: 'KA', country: 'India' },
  { city: 'Hyderabad', state: 'TS', country: 'India' },
  { city: 'Chennai', state: 'TN', country: 'India' },
  { city: 'Kolkata', state: 'WB', country: 'India' },
  { city: 'Pune', state: 'MH', country: 'India' },
  { city: 'Ahmedabad', state: 'GJ', country: 'India' },
  { city: 'Tokyo', state: 'Tokyo', country: 'Japan' }
];
import EditIcon from '@mui/icons-material/Edit';
import BlockIcon from '@mui/icons-material/Block';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';

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

  const handleToggleStatus = async (warehouse) => {
    try {
      if (warehouse.status === 'INACTIVE') {
        await warehouseApi.activate(warehouse.id);
        toast.success('Warehouse activated');
      } else {
        await warehouseApi.deactivate(warehouse.id);
        toast.success('Warehouse deactivated');
      }
      fetchWarehouses();
    } catch (err) {
      toast.error('Failed to update warehouse status');
    }
  };

  if (loading) return <Typography>Loading...</Typography>;

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
        <Typography variant="h6" fontWeight={600}>All Warehouses</Typography>
        <Button variant="contained" sx={{marginLeft: 5}} onClick={openCreate}>+ Add Warehouse</Button>
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
                  <TableCell><Chip label={w.status} size="small" color={w.status === 'INACTIVE' ? 'error' : 'success'} /></TableCell>
                  <TableCell>
                    <IconButton size="small" onClick={() => openEdit(w)}><EditIcon fontSize="small" /></IconButton>
                    <IconButton size="small" color={w.status === 'INACTIVE' ? 'success' : 'error'} onClick={() => handleToggleStatus(w)}>
                      {w.status === 'INACTIVE' ? <CheckCircleIcon fontSize="small" /> : <BlockIcon fontSize="small" />}
                    </IconButton>
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
                <FormControl fullWidth>
                  <InputLabel id="city-label">City</InputLabel>
                  <Select
                    labelId="city-label"
                    name="city"
                    value={formik.values.city}
                    label="City"
                    onChange={(e) => {
                      const selectedCity = e.target.value;
                      formik.setFieldValue('city', selectedCity);
                      const cityInfo = CITY_DATA.find(c => c.city === selectedCity);
                      if (cityInfo) {
                        formik.setFieldValue('state', cityInfo.state);
                        formik.setFieldValue('country', cityInfo.country);
                      }
                    }}
                  >
                    <MenuItem value=""><em>None</em></MenuItem>
                    {CITY_DATA.map(c => (
                      <MenuItem key={c.city} value={c.city}>{c.city}</MenuItem>
                    ))}
                  </Select>
                </FormControl>
                <TextField label="State" name="state" value={formik.values.state} onChange={formik.handleChange} fullWidth InputLabelProps={{ shrink: true }} />
              </Stack>
              <Stack direction="row" spacing={2}>
                <TextField label="Country" name="country" value={formik.values.country} onChange={formik.handleChange} fullWidth InputLabelProps={{ shrink: true }} />
                <TextField label="Capacity" name="capacity" type="number" placeholder="e.g., 5000 (pallets/sq ft)" value={formik.values.capacity} onChange={formik.handleChange} fullWidth />
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
