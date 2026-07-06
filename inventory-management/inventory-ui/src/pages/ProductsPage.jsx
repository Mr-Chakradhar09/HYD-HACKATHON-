import { useState, useEffect } from 'react';
import { productApi } from '../services/api';
import toast from 'react-hot-toast';
import { useFormik } from 'formik';
import * as yup from 'yup';
import {
  Box, Card, Typography, Button, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Chip, Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, MenuItem, Stack, IconButton, InputAdornment
} from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import SearchIcon from '@mui/icons-material/Search';

const categories = ['ELECTRONICS', 'GROCERY', 'FURNITURE', 'CLOTHING', 'OTHERS'];
const units = [
  { value: 'PCS', label: 'Pieces (PCS)' },
  { value: 'KG', label: 'Kilogram (KG)' },
  { value: 'M3', label: 'Cubic Meter (M3)' },
  { value: 'L', label: 'Liter (L)' },
  { value: 'M', label: 'Meter (M)' },
  { value: 'BOX', label: 'Box' },
  { value: 'PACK', label: 'Pack' },
];

const productSchema = yup.object({
  sku: yup.string().required('Required'),
  name: yup.string().required('Required'),
  description: yup.string(),
  category: yup.string(),
  unitPrice: yup.number().min(0, 'Must be positive').nullable(),
  unitOfMeasure: yup.string(),
  minimumStock: yup.number().min(0).nullable(),
});

export default function ProductsPage() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [search, setSearch] = useState('');

  useEffect(() => { fetchProducts(); }, []);

  const fetchProducts = async () => {
    try {
      const res = await productApi.getAll();
      setProducts(res.data);
    } catch { toast.error('Failed to load products'); }
    finally { setLoading(false); }
  };

  const handleSearch = async () => {
    try {
      const res = await productApi.getAll(search);
      setProducts(res.data);
    } catch { toast.error('Search failed'); }
  };

  const formik = useFormik({
    initialValues: { sku: '', name: '', description: '', category: 'ELECTRONICS', unitPrice: '', unitOfMeasure: 'PCS', minimumStock: 0 },
    validationSchema: productSchema,
    onSubmit: async (values, { setSubmitting, resetForm }) => {
      try {
        const data = { ...values, unitPrice: parseFloat(values.unitPrice) || 0, minimumStock: parseInt(values.minimumStock) || 0 };
        if (editing) {
          await productApi.update(editing.id, data);
          toast.success('Product updated');
        } else {
          await productApi.create(data);
          toast.success('Product created');
        }
        setDialogOpen(false);
        setEditing(null);
        resetForm();
        fetchProducts();
      } catch (err) { toast.error(err.response?.data?.message || 'Operation failed'); }
      finally { setSubmitting(false); }
    },
  });

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure?')) return;
    try {
      await productApi.delete(id);
      toast.success('Product deleted');
      fetchProducts();
    } catch { toast.error('Delete failed'); }
  };

  const openEdit = (p) => {
    setEditing(p);
    formik.setValues({
      sku: p.sku, name: p.name, description: p.description || '', category: p.category,
      unitPrice: p.unitPrice?.toString() || '', unitOfMeasure: p.unitOfMeasure || 'PCS', minimumStock: p.minimumStock || 0,
    });
    setDialogOpen(true);
  };

  const openCreate = () => { setEditing(null); formik.resetForm(); setDialogOpen(true); };

  if (loading) return <Typography>Loading...</Typography>;

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
        <Stack direction="row" spacing={1}>
          <TextField size="small" placeholder="Search products..." value={search} onChange={e => setSearch(e.target.value)} onKeyDown={e => e.key === 'Enter' && handleSearch()} InputProps={{ startAdornment: <InputAdornment position="start"><SearchIcon fontSize="small" /></InputAdornment> }} sx={{ width: 280 }} />
          <Button variant="outlined" onClick={handleSearch}>Search</Button>
        </Stack>
        <Button variant="contained" onClick={openCreate}>+ Add Product</Button>
      </Stack>

      <Card>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>SKU</TableCell>
                <TableCell>Name</TableCell>
                <TableCell>Category</TableCell>
                <TableCell>UOM</TableCell>
                <TableCell>Price</TableCell>
                <TableCell>Min Stock</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {products.length === 0 ? (
                <TableRow><TableCell colSpan={8} align="center">No products found</TableCell></TableRow>
              ) : products.map(p => (
                <TableRow key={p.id} hover>
                  <TableCell><Typography fontWeight={600}>{p.sku}</Typography></TableCell>
                  <TableCell>{p.name}</TableCell>
                  <TableCell><Chip label={p.category} size="small" variant="outlined" /></TableCell>
                  <TableCell>{p.unitOfMeasure || 'PCS'}</TableCell>
                  <TableCell>${p.unitPrice?.toFixed(2)}</TableCell>
                  <TableCell>{p.minimumStock}</TableCell>
                  <TableCell><Chip label={p.status} size="small" color={p.status === 'ACTIVE' ? 'success' : 'error'} /></TableCell>
                  <TableCell>
                    <Stack direction="row" spacing={0.5}>
                      <IconButton size="small" onClick={() => openEdit(p)}><EditIcon fontSize="small" /></IconButton>
                      <IconButton size="small" color="error" onClick={() => handleDelete(p.id)}><DeleteIcon fontSize="small" /></IconButton>
                    </Stack>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </Card>

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <form onSubmit={formik.handleSubmit}>
          <DialogTitle>{editing ? 'Edit Product' : 'Add Product'}</DialogTitle>
          <DialogContent>
            <Stack spacing={2} sx={{ mt: 1 }}>
              <Stack direction="row" spacing={2}>
                <TextField label="SKU" name="sku" value={formik.values.sku} onChange={formik.handleChange} onBlur={formik.handleBlur} error={formik.touched.sku && Boolean(formik.errors.sku)} helperText={formik.touched.sku && formik.errors.sku} fullWidth required />
                <TextField label="Name" name="name" value={formik.values.name} onChange={formik.handleChange} onBlur={formik.handleBlur} error={formik.touched.name && Boolean(formik.errors.name)} helperText={formik.touched.name && formik.errors.name} fullWidth required />
              </Stack>
              <TextField label="Description" name="description" value={formik.values.description} onChange={formik.handleChange} fullWidth multiline rows={2} />
              <Stack direction="row" spacing={2}>
                <TextField label="Category" name="category" select value={formik.values.category} onChange={formik.handleChange} fullWidth>
                  {categories.map(c => <MenuItem key={c} value={c}>{c}</MenuItem>)}
                </TextField>
                <TextField label="Unit of Measure" name="unitOfMeasure" select value={formik.values.unitOfMeasure} onChange={formik.handleChange} fullWidth>
                  {units.map(u => <MenuItem key={u.value} value={u.value}>{u.label}</MenuItem>)}
                </TextField>
              </Stack>
              <Stack direction="row" spacing={2}>
                <TextField label="Unit Price" name="unitPrice" type="number" value={formik.values.unitPrice} onChange={formik.handleChange} fullWidth InputProps={{ startAdornment: <InputAdornment position="start">$</InputAdornment> }} />
                <TextField label="Minimum Stock" name="minimumStock" type="number" value={formik.values.minimumStock} onChange={formik.handleChange} fullWidth />
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
