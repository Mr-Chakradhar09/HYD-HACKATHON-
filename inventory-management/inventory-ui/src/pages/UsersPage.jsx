import { useState, useEffect } from 'react';
import { userApi, warehouseApi } from '../services/api';
import toast from 'react-hot-toast';
import { useFormik } from 'formik';
import * as yup from 'yup';
import {
  Box, Card, Typography, Button, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Chip, Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, MenuItem, Stack, IconButton
} from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import BlockIcon from '@mui/icons-material/Block';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';

const roles = [
  { value: 'SYSTEM_ADMIN', label: 'System Admin' },
  { value: 'INVENTORY_MANAGER', label: 'Inventory Manager' },
  { value: 'WAREHOUSE_MANAGER', label: 'Warehouse Manager' },
  { value: 'PROCUREMENT_MANAGER', label: 'Procurement Manager' },
];

const managerRoles = ['INVENTORY_MANAGER', 'WAREHOUSE_MANAGER', 'PROCUREMENT_MANAGER'];

const userSchema = yup.object({
  employeeId: yup.string().required('Required'),
  firstName: yup.string().required('Required'),
  lastName: yup.string().required('Required'),
  email: yup.string().email('Invalid email').required('Required'),
  mobile: yup.string().required('Required'),
  password: yup.string().when('isEditing', { is: false, then: () => yup.string().min(4, 'Min 4 chars').required('Required') }),
  role: yup.string().required('Required'),
  warehouseId: yup.string().nullable(),
});

export default function UsersPage() {
  const [users, setUsers] = useState([]);
  const [warehouses, setWarehouses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editing, setEditing] = useState(null);

  useEffect(() => {
    Promise.all([userApi.getAll(), warehouseApi.getAll()])
      .then(([u, w]) => { setUsers(u.data); setWarehouses(w.data); })
      .catch(() => toast.error('Failed to load data'))
      .finally(() => setLoading(false));
  }, []);

  const formik = useFormik({
    initialValues: { employeeId: '', firstName: '', lastName: '', email: '', mobile: '', password: '', role: 'WAREHOUSE_MANAGER', warehouseId: '', isEditing: false },
    validationSchema: userSchema,
    onSubmit: async (values, { setSubmitting, resetForm }) => {
      try {
        const payload = { ...values, warehouseId: values.warehouseId ? Number(values.warehouseId) : null };
        delete payload.isEditing;
        if (editing) {
          delete payload.password;
          delete payload.email;
          delete payload.employeeId;
          await userApi.update(editing.id, payload);
          toast.success('User updated');
        } else {
          await userApi.create(payload);
          toast.success('User created');
        }
        setDialogOpen(false);
        setEditing(null);
        resetForm();
        const res = await userApi.getAll();
        setUsers(res.data);
      } catch (err) {
        toast.error(err.response?.data?.message || 'Operation failed');
      } finally { setSubmitting(false); }
    },
  });

  const handleToggleStatus = async (u) => {
    try {
      if (u.status === 'ACTIVE') {
        await userApi.deactivate(u.id);
        toast.success('User deactivated');
      } else {
        await userApi.activate(u.id);
        toast.success('User activated');
      }
      const res = await userApi.getAll();
      setUsers(res.data);
    } catch { toast.error('Status change failed'); }
  };

  const openEdit = (u) => {
    setEditing(u);
    formik.setValues({
      employeeId: u.employeeId, firstName: u.firstName, lastName: u.lastName,
      email: u.email, mobile: u.mobile, password: '', role: u.role,
      warehouseId: u.warehouseId?.toString() || '', isEditing: true,
    });
    setDialogOpen(true);
  };

  const openCreate = () => {
    setEditing(null);
    formik.resetForm();
    formik.setFieldValue('isEditing', false);
    setDialogOpen(true);
  };

  const needsWarehouse = (role) => managerRoles.includes(role);

  if (loading) return <Typography>Loading...</Typography>;

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
        <Typography variant="h6" fontWeight={600}>All Users</Typography>
        <Button variant="contained" onClick={openCreate}>+ Add User</Button>
      </Stack>

      <Card>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Employee ID</TableCell>
                <TableCell>Name</TableCell>
                <TableCell>Email</TableCell>
                <TableCell>Role</TableCell>
                <TableCell>Warehouse</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {users.length === 0 ? (
                <TableRow><TableCell colSpan={7} align="center">No users found</TableCell></TableRow>
              ) : users.map(u => (
                <TableRow key={u.id} hover>
                  <TableCell><Typography fontWeight={600}>{u.employeeId}</Typography></TableCell>
                  <TableCell>{u.firstName} {u.lastName}</TableCell>
                  <TableCell>{u.email}</TableCell>
                  <TableCell><Chip label={u.role?.replace(/_/g, ' ')} size="small" sx={{ bgcolor: '#e9d5ff', color: '#9333ea' }} /></TableCell>
                  <TableCell>{u.warehouseId ? warehouses.find(w => w.id === u.warehouseId)?.name || `WH #${u.warehouseId}` : '-'}</TableCell>
                  <TableCell>
                    <Chip label={u.status} size="small" color={u.status === 'ACTIVE' ? 'success' : 'error'} />
                  </TableCell>
                  <TableCell>
                    <Stack direction="row" spacing={0.5}>
                      <IconButton size="small" onClick={() => openEdit(u)}><EditIcon fontSize="small" /></IconButton>
                      <IconButton size="small" color={u.status === 'ACTIVE' ? 'warning' : 'success'} onClick={() => handleToggleStatus(u)}>
                        {u.status === 'ACTIVE' ? <BlockIcon fontSize="small" /> : <CheckCircleIcon fontSize="small" />}
                      </IconButton>
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
          <DialogTitle>{editing ? 'Edit User' : 'Add User'}</DialogTitle>
          <DialogContent>
            <Stack spacing={2} sx={{ mt: 1 }}>
              <TextField label="Employee ID" name="employeeId" value={formik.values.employeeId} onChange={formik.handleChange} onBlur={formik.handleBlur} error={formik.touched.employeeId && Boolean(formik.errors.employeeId)} helperText={formik.touched.employeeId && formik.errors.employeeId} fullWidth required disabled={!!editing} />
              <Stack direction="row" spacing={2}>
                <TextField label="First Name" name="firstName" value={formik.values.firstName} onChange={formik.handleChange} onBlur={formik.handleBlur} error={formik.touched.firstName && Boolean(formik.errors.firstName)} helperText={formik.touched.firstName && formik.errors.firstName} fullWidth required />
                <TextField label="Last Name" name="lastName" value={formik.values.lastName} onChange={formik.handleChange} onBlur={formik.handleBlur} error={formik.touched.lastName && Boolean(formik.errors.lastName)} helperText={formik.touched.lastName && formik.errors.lastName} fullWidth required />
              </Stack>
              <TextField label="Email" name="email" type="email" value={formik.values.email} onChange={formik.handleChange} onBlur={formik.handleBlur} error={formik.touched.email && Boolean(formik.errors.email)} helperText={formik.touched.email && formik.errors.email} fullWidth required disabled={!!editing} />
              <Stack direction="row" spacing={2}>
                <TextField label="Mobile" name="mobile" value={formik.values.mobile} onChange={formik.handleChange} onBlur={formik.handleBlur} error={formik.touched.mobile && Boolean(formik.errors.mobile)} helperText={formik.touched.mobile && formik.errors.mobile} fullWidth required />
                {!editing && (
                  <TextField label="Password" name="password" type="password" value={formik.values.password} onChange={formik.handleChange} onBlur={formik.handleBlur} error={formik.touched.password && Boolean(formik.errors.password)} helperText={formik.touched.password && formik.errors.password} fullWidth required />
                )}
              </Stack>
              <TextField label="Role" name="role" select value={formik.values.role} onChange={(e) => { formik.handleChange(e); if (!needsWarehouse(e.target.value)) formik.setFieldValue('warehouseId', ''); }} fullWidth required>
                {roles.map(r => <MenuItem key={r.value} value={r.value}>{r.label}</MenuItem>)}
              </TextField>
              {needsWarehouse(formik.values.role) && (
                <TextField label="Assign Warehouse" name="warehouseId" select value={formik.values.warehouseId} onChange={formik.handleChange} fullWidth>
                  <MenuItem value="">Select warehouse</MenuItem>
                  {warehouses.map(w => <MenuItem key={w.id} value={w.id.toString()}>{w.code} - {w.name}</MenuItem>)}
                </TextField>
              )}
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
