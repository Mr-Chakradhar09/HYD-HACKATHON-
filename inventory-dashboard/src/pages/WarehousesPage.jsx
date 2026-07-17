import { useState, useEffect, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { warehouseApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';
import {
  Plus, Search, X, Warehouse, MapPin, Phone, Mail,
  Package, ToggleLeft, ToggleRight, Edit3, Users, ChevronRight, UserPlus, Trash2, Eye, EyeOff
} from 'lucide-react';

function useIsMobile() {
  const [isMobile, setIsMobile] = useState(window.innerWidth < 768);
  useEffect(() => {
    const handleResize = () => setIsMobile(window.innerWidth < 768);
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);
  return isMobile;
}

const EMPTY_FORM = {
  warehouseCode: '', warehouseName: '', email: '', phone: '',
  capacity: '', capacityUnit: '',
  address: { addressLine1: '', addressLine2: '', city: '', state: '', country: '', postalCode: '' },
};

function WarehouseModal({ warehouse, onClose, onSaved }) {
  const [form, setForm] = useState(warehouse ? {
    ...warehouse, address: warehouse.address || EMPTY_FORM.address,
    capacityUnit: warehouse.capacityUnit || '',
    capacity: warehouse.capacity != null ? String(warehouse.capacity) : '',
    email: warehouse.email || '',
    phone: warehouse.phone || '',
  } : EMPTY_FORM);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const isEdit = !!warehouse;

  const set = (field, val) => setForm(f => ({ ...f, [field]: val }));
  const setAddr = (field, val) => setForm(f => ({ ...f, address: { ...f.address, [field]: val } }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true); setError('');
    try {
      const payload = { ...form, capacity: Number(form.capacity) };
      if (isEdit) await warehouseApi.update(warehouse.id, payload);
      else await warehouseApi.create(payload);
      toast.success(isEdit ? 'Warehouse updated!' : 'Warehouse created!');
      onSaved();
    } catch (err) {
      setError(err.response?.data?.message || 'An error occurred');
    } finally { setLoading(false); }
  };

  const isMobile = window.innerWidth < 640;

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <motion.div className="modal" initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }} exit={{ opacity: 0, scale: 0.95 }} style={{ maxWidth: isMobile ? '100%' : 560, padding: isMobile ? '18px' : undefined }}>
        <div className="modal-header">
          <h2 className="modal-title">{isEdit ? 'Edit Warehouse' : 'Create Warehouse'}</h2>
          <button className="btn btn-icon btn-secondary" onClick={onClose}><X size={18} /></button>
        </div>
        {error && <div className="alert alert-error"><X size={14} />{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="grid grid-2 gap-4">
            <div className="form-group">
              <label className="form-label">Warehouse Code *</label>
              <input className="form-control" placeholder="WAHO11223344" pattern="^WAHO\d{8}$" title="Must be WAHO followed by 8 digits (e.g. WAHO11223344)" value={form.warehouseCode} onChange={e => set('warehouseCode', e.target.value.toUpperCase())} required disabled={isEdit} />
            </div>
            <div className="form-group">
              <label className="form-label">Warehouse Name *</label>
              <input className="form-control" placeholder="Main Warehouse" value={form.warehouseName} onChange={e => set('warehouseName', e.target.value)} required />
            </div>
            <div className="form-group">
              <label className="form-label">Email</label>
              <input className="form-control" type="email" placeholder="wh@company.com" value={form.email} onChange={e => set('email', e.target.value)} />
            </div>
            <div className="form-group">
              <label className="form-label">Phone</label>
              <input className="form-control" placeholder="9876543210" value={form.phone} onChange={e => set('phone', e.target.value)} />
            </div>
            <div className="form-group">
              <label className="form-label">Capacity *</label>
              <input className="form-control" type="number" placeholder="5000" value={form.capacity} onChange={e => set('capacity', e.target.value)} required />
            </div>
            <div className="form-group">
              <label className="form-label">Capacity Unit</label>
              <select className="form-control" value={form.capacityUnit} onChange={e => set('capacityUnit', e.target.value)}>
                {['PALLET', 'UNIT', 'CUBIC_METER', 'SQUARE_METER'].map(u => <option key={u}>{u}</option>)}
              </select>
            </div>
          </div>
          <div style={{ marginBottom: 12, fontWeight: 700, fontSize: '0.8rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>Address</div>
          <div className="grid grid-2 gap-4">
            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
              <label className="form-label">Address Line 1 *</label>
              <input className="form-control" placeholder="123 Industrial Area" value={form.address.addressLine1} onChange={e => setAddr('addressLine1', e.target.value)} required />
            </div>
            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
              <label className="form-label">Address Line 2</label>
              <input className="form-control" placeholder="Block B" value={form.address.addressLine2} onChange={e => setAddr('addressLine2', e.target.value)} />
            </div>
            <div className="form-group">
              <label className="form-label">City *</label>
              <input className="form-control" placeholder="Hyderabad" value={form.address.city} onChange={e => setAddr('city', e.target.value)} required />
            </div>
            <div className="form-group">
              <label className="form-label">State *</label>
              <input className="form-control" placeholder="Telangana" value={form.address.state} onChange={e => setAddr('state', e.target.value)} required />
            </div>
            <div className="form-group">
              <label className="form-label">Country *</label>
              <input className="form-control" value={form.address.country} onChange={e => setAddr('country', e.target.value)} required />
            </div>
            <div className="form-group">
              <label className="form-label">Postal Code *</label>
              <input className="form-control" placeholder="500001" value={form.address.postalCode} onChange={e => setAddr('postalCode', e.target.value)} required />
            </div>
          </div>
          <div style={{ display: 'flex', gap: 12, justifyContent: 'flex-end', marginTop: 8 }}>
            <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? <><span className="spinner" /> Saving...</> : (isEdit ? 'Update' : 'Create')}
            </button>
          </div>
        </form>
      </motion.div>
    </div>
  );
}

function AssignModal({ warehouseId, onClose, onSaved }) {
  const { hasRole } = useAuth();
  const [step, setStep] = useState('role'); // 'role' | 'employee'
  const [warehouseRole, setWarehouseRole] = useState('');
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(false);
  const [assigning, setAssigning] = useState(null);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');

  const assignableRoles = (() => {
    const allRoles = [
      { value: 'WAREHOUSE_OPERATOR', label: 'Warehouse Operator' },
      { value: 'PROCUREMENT_MANAGER', label: 'Procurement Manager' },
      { value: 'INVENTORY_MANAGER', label: 'Inventory Manager' },
      { value: 'SYSTEM_ADMIN', label: 'System Admin' },
    ];
    if (hasRole('SYSTEM_ADMIN')) return allRoles;
    if (hasRole('INVENTORY_MANAGER')) return allRoles.filter(r => r.value === 'PROCUREMENT_MANAGER' || r.value === 'WAREHOUSE_OPERATOR');
    return [];
  })();

  const loadEmployees = async (role) => {
    setLoading(true); setError('');
    try {
      const res = await warehouseApi.getUnassignedEmployees();
      const all = res.data?.data || [];
      const filtered = all.filter(emp => emp.roles?.includes(role));
      setEmployees(filtered);
      setStep('employee');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load employees');
    } finally { setLoading(false); }
  };

  const handleRoleSelect = (role) => {
    setWarehouseRole(role);
    loadEmployees(role);
  };

  const handleAssign = async (emp) => {
    setAssigning(emp.employeeCode); setError('');
    try {
      await warehouseApi.assignEmployee(warehouseId, {
        employeeCode: emp.employeeCode,
        warehouseRole: warehouseRole,
      });
      toast.success(`${emp.fullName || emp.employeeCode} assigned as ${warehouseRole.replace(/_/g, ' ')}!`);
      onSaved();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to assign employee');
    } finally { setAssigning(null); }
  };

  const filteredEmployees = employees.filter(emp =>
    !search ||
    emp.fullName?.toLowerCase().includes(search.toLowerCase()) ||
    emp.employeeCode?.toLowerCase().includes(search.toLowerCase()) ||
    emp.email?.toLowerCase().includes(search.toLowerCase())
  );

  const roleLabel = warehouseRole.replace(/_/g, ' ');

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <motion.div className="modal" initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }} exit={{ opacity: 0, scale: 0.95 }} style={{ maxWidth: 480 }}>
        <div className="modal-header">
          <h2 className="modal-title">
            {step === 'role' ? 'Assign Employee' : `Assign as ${roleLabel}`}
          </h2>
          <button className="btn btn-icon btn-secondary" onClick={onClose}><X size={18} /></button>
        </div>
        {error && <div className="alert alert-error"><X size={14} />{error}</div>}

        {step === 'role' ? (
          <div>
            <div style={{ fontSize: '0.82rem', color: 'var(--text-muted)', marginBottom: 16 }}>
              Select the role to assign, then choose an available employee.
            </div>
            {assignableRoles.length === 0 ? (
              <div style={{ fontSize: '0.82rem', color: 'var(--text-muted)', padding: '20px 0', textAlign: 'center' }}>
                Your role does not have permission to assign employees.
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                {assignableRoles.map(r => (
                  <button
                    key={r.value}
                    className="btn btn-secondary"
                    onClick={() => handleRoleSelect(r.value)}
                    disabled={loading}
                    style={{ justifyContent: 'flex-start', padding: '12px 16px', fontSize: '0.88rem' }}
                  >
                    <Users size={16} style={{ marginRight: 10, opacity: 0.6 }} />
                    {r.label}
                    <ChevronRight size={14} style={{ marginLeft: 'auto', opacity: 0.4 }} />
                  </button>
                ))}
              </div>
            )}
            <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: 16 }}>
              <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
            </div>
          </div>
        ) : (
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12 }}>
              <button
                className="btn btn-icon btn-secondary"
                onClick={() => { setStep('role'); setEmployees([]); setSearch(''); setError(''); }}
                style={{ flexShrink: 0 }}
              >
                <ChevronRight size={16} style={{ transform: 'rotate(180deg)' }} />
              </button>
              <div className="search-box" style={{ flex: 1 }}>
                <Search size={14} />
                <input
                  placeholder="Search employees..."
                  value={search}
                  onChange={e => setSearch(e.target.value)}
                />
              </div>
            </div>

            {loading ? (
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8, padding: '30px 0', color: 'var(--text-muted)', fontSize: '0.82rem' }}>
                <span className="spinner" /> Loading employees...
              </div>
            ) : filteredEmployees.length === 0 ? (
              <div style={{ padding: '30px 0', textAlign: 'center' }}>
                <Users size={36} color="var(--text-muted)" style={{ marginBottom: 8 }} />
                <div style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-secondary)' }}>No available employees</div>
                <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: 4 }}>
                  All active employees with this role are already assigned.
                </div>
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 6, maxHeight: 320, overflowY: 'auto' }}>
                {filteredEmployees.map(emp => (
                  <button
                    key={emp.employeeCode}
                    onClick={() => handleAssign(emp)}
                    disabled={assigning !== null}
                    style={{
                      display: 'flex', alignItems: 'center', gap: 12,
                      padding: '10px 14px', background: 'var(--bg-surface)',
                      border: '1px solid var(--border-subtle)', borderRadius: 10,
                      cursor: assigning ? 'wait' : 'pointer', textAlign: 'left',
                      transition: 'all 0.15s',
                    }}
                    onMouseEnter={e => e.currentTarget.style.borderColor = '#6366f1'}
                    onMouseLeave={e => e.currentTarget.style.borderColor = 'var(--border-subtle)'}
                  >
                    <div style={{
                      width: 36, height: 36, borderRadius: 9,
                      background: 'linear-gradient(135deg, rgba(99,102,241,0.25), rgba(79,70,229,0.25))',
                      display: 'flex', alignItems: 'center', justifyContent: 'center',
                      fontSize: '0.82rem', fontWeight: 700, color: '#a5b4fc', flexShrink: 0,
                    }}>
                      {emp.fullName?.charAt(0) || '?'}
                    </div>
                    <div style={{ flex: 1, minWidth: 0 }}>
                      <div style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-primary)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                        {emp.fullName || 'No Name'}
                      </div>
                      <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', fontFamily: 'monospace' }}>
                        {emp.employeeCode}
                      </div>
                    </div>
                    {assigning === emp.employeeCode ? (
                      <span className="spinner" style={{ width: 16, height: 16, flexShrink: 0 }} />
                    ) : (
                      <UserPlus size={15} color="#6366f1" style={{ flexShrink: 0, opacity: 0.6 }} />
                    )}
                  </button>
                ))}
              </div>
            )}
            <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: 16 }}>
              <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
            </div>
          </div>
        )}
      </motion.div>
    </div>
  );
}

export default function WarehousesPage() {
  const { isAdmin, isManager, canViewAssignments } = useAuth();
  const isMobile = useIsMobile();
  const [warehouses, setWarehouses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [modal, setModal] = useState(null); // null | 'create' | warehouse object
  const [assignModal, setAssignModal] = useState(null); // warehouse id or null
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [selected, setSelected] = useState(null);
  const [assignments, setAssignments] = useState([]);
  const [assignmentsLoading, setAssignmentsLoading] = useState(false);
  const [myWarehouseId, setMyWarehouseId] = useState(null);
  const [showMyWarehouseOnly, setShowMyWarehouseOnly] = useState(false);

  useEffect(() => {
    if (isManager() && !isAdmin()) {
      warehouseApi.getMyAssignment()
        .then(res => {
          const a = res.data?.data;
          if (a?.warehouseId) setMyWarehouseId(a.warehouseId);
        })
        .catch(() => {});
    }
  }, []);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await warehouseApi.getAll({ page, size: 10 });
      const d = res.data?.data;
      setWarehouses(d?.content || d || []);
      setTotalPages(d?.totalPages || 1);
    } catch { toast.error('Failed to load warehouses'); }
    finally { setLoading(false); }
  }, [page]);

  useEffect(() => { load(); }, [load]);

  const loadAssignments = useCallback(async (warehouseId) => {
    if (!warehouseId) return;
    setAssignmentsLoading(true);
    try {
      const res = await warehouseApi.getAssignments(warehouseId);
      const d = res.data?.data;
      setAssignments(d?.content || d || []);
    } catch { setAssignments([]); }
    finally { setAssignmentsLoading(false); }
  }, []);

  useEffect(() => {
    if (selected) loadAssignments(selected.id);
    else setAssignments([]);
  }, [selected, loadAssignments]);

  const toggle = async (wh) => {
    try {
      if (wh.status === 'ACTIVE') { await warehouseApi.deactivate(wh.id); toast.success('Warehouse deactivated'); }
      else { await warehouseApi.activate(wh.id); toast.success('Warehouse activated'); }
      load();
    } catch (err) { toast.error(err.response?.data?.message || 'Failed'); }
  };

  const deleteWarehouse = async (wh) => {
    if (!confirm(`Permanently delete warehouse "${wh.warehouseName}"? This cannot be undone.`)) return;
    try {
      await warehouseApi.delete(wh.id);
      toast.success('Warehouse deleted');
      setSelected(null);
      load();
    } catch (err) { toast.error(err.response?.data?.message || 'Failed to delete'); }
  };

  const filtered = warehouses.filter(w => {
    if (showMyWarehouseOnly && myWarehouseId && w.id !== myWarehouseId) return false;
    if (!search) return true;
    return w.warehouseName?.toLowerCase().includes(search.toLowerCase()) ||
      w.warehouseCode?.toLowerCase().includes(search.toLowerCase());
  });

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Warehouses</h1>
          <p className="page-subtitle">Manage storage facilities and assignments</p>
        </div>
        {isAdmin() && (
          <button className="btn btn-primary" onClick={() => setModal('create')}>
            <Plus size={18} /> Add Warehouse
          </button>
        )}
      </div>

      {/* Search */}
      <div style={{ marginBottom: 20, display: 'flex', alignItems: 'center', gap: 12, flexWrap: 'wrap' }}>
        <div className="search-box" style={{ maxWidth: 380, flex: 1 }}>
          <Search size={16} />
          <input placeholder="Search by name or code..." value={search} onChange={e => setSearch(e.target.value)} />
          {search && <button onClick={() => setSearch('')} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)' }}><X size={14} /></button>}
        </div>
        {isManager() && !isAdmin() && myWarehouseId && (
          <button
            className={`btn ${showMyWarehouseOnly ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setShowMyWarehouseOnly(!showMyWarehouseOnly)}
            style={{ fontSize: '0.82rem', whiteSpace: 'nowrap' }}
          >
            {showMyWarehouseOnly ? <EyeOff size={14} /> : <Eye size={14} />}
            {showMyWarehouseOnly ? 'Show All' : 'My Warehouse'}
          </button>
        )}
      </div>

      {/* Two-column layout: list + detail */}
      <div style={{ display: 'grid', gridTemplateColumns: selected && !isMobile ? '1fr 360px' : '1fr', gap: 20 }}>
        <div className="table-wrapper">
          {loading ? (
            <div className="loading-state"><div className="spinner" /><span>Loading...</span></div>
          ) : filtered.length === 0 ? (
            <div className="empty-state">
              <Warehouse size={48} color="var(--text-muted)" />
              <h3>No warehouses found</h3>
              <p>Create your first warehouse to get started.</p>
            </div>
          ) : (
            <table className="data-table">
              <thead>
                <tr>
                  <th>Code</th><th>Name</th><th>Location</th><th>Capacity</th><th>Status</th>
                  {(isAdmin() || isManager()) && <th style={{ textAlign: 'right' }}>Actions</th>}
                </tr>
              </thead>
              <tbody>
                {filtered.map(wh => (
                  <tr key={wh.id} onClick={() => setSelected(s => s?.id === wh.id ? null : wh)} style={{ cursor: 'pointer', background: selected?.id === wh.id ? 'rgba(99,102,241,0.08)' : undefined }}>
                    <td><span style={{ fontFamily: 'monospace', fontSize: '0.82rem', color: '#818cf8', fontWeight: 700 }}>{wh.warehouseCode}</span></td>
                    <td><div style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{wh.warehouseName}</div></td>
                    <td style={{ color: 'var(--text-muted)', fontSize: '0.82rem' }}>{wh.address?.city}, {wh.address?.state}</td>
                    <td>{wh.capacity?.toLocaleString()} {wh.capacityUnit}</td>
                    <td>
                      <span className={`badge badge-${wh.status === 'ACTIVE' ? 'active' : 'inactive'}`}>{wh.status}</span>
                    </td>
                    {isAdmin() && (
                      <td onClick={e => e.stopPropagation()}>
                        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
                          <button className="btn btn-secondary btn-sm" onClick={async () => {
                            try {
                              const res = await warehouseApi.getById(wh.id);
                              setModal(res.data?.data || wh);
                            } catch { setModal(wh); }
                          }}><Edit3 size={13} /></button>
                          {wh.status === 'ACTIVE' ? (
                            <button className="btn btn-sm btn-danger" onClick={() => toggle(wh)} title="Deactivate">
                              <ToggleRight size={14} />
                            </button>
                          ) : (
                            <>
                              <button className="btn btn-sm btn-success" onClick={() => toggle(wh)} title="Activate">
                                <ToggleLeft size={14} />
                              </button>
                              <button className="btn btn-sm btn-danger" onClick={() => deleteWarehouse(wh)} title="Delete permanently">
                                <Trash2 size={13} />
                              </button>
                            </>
                          )}
                        </div>
                      </td>
                    )}
                    {isManager() && !isAdmin() && myWarehouseId === wh.id && (
                      <td onClick={e => e.stopPropagation()}>
                        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
                          <button className="btn btn-secondary btn-sm" onClick={async () => {
                            try {
                              const res = await warehouseApi.getById(wh.id);
                              setModal(res.data?.data || wh);
                            } catch { setModal(wh); }
                          }}><Edit3 size={13} /></button>
                        </div>
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          )}
          {/* Pagination */}
          {totalPages > 1 && (
            <div className="pagination">
              <button className="page-btn" disabled={page === 0} onClick={() => setPage(p => p - 1)}>←</button>
              {Array.from({ length: totalPages }, (_, i) => (
                <button key={i} className={`page-btn ${i === page ? 'active' : ''}`} onClick={() => setPage(i)}>{i + 1}</button>
              ))}
              <button className="page-btn" disabled={page === totalPages - 1} onClick={() => setPage(p => p + 1)}>→</button>
            </div>
          )}
        </div>

        {/* Detail panel */}
        <AnimatePresence>
          {selected && (
            <motion.div initial={{ opacity: 0, x: 20 }} animate={{ opacity: 1, x: 0 }} exit={{ opacity: 0, x: 20 }} className="card" style={{ height: 'fit-content' }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 20 }}>
                <h3>{selected.warehouseName}</h3>
                <button className="btn btn-icon btn-secondary" onClick={() => setSelected(null)}><X size={16} /></button>
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
                {[
                  { icon: Package, label: 'Code', value: selected.warehouseCode },
                  { icon: MapPin, label: 'Address', value: `${selected.address?.addressLine1}, ${selected.address?.city} - ${selected.address?.postalCode}` },
                  { icon: Phone, label: 'Phone', value: selected.phone },
                  { icon: Mail, label: 'Email', value: selected.email },
                  { icon: Warehouse, label: 'Capacity', value: `${selected.capacity?.toLocaleString()} ${selected.capacityUnit}` },
                ].map(({ icon: Icon, label, value }) => value && (
                  <div key={label} style={{ display: 'flex', gap: 12, alignItems: 'flex-start' }}>
                    <Icon size={16} color="var(--primary)" style={{ flexShrink: 0, marginTop: 2 }} />
                    <div>
                      <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.06em' }}>{label}</div>
                      <div style={{ fontSize: '0.88rem', color: 'var(--text-primary)', marginTop: 2 }}>{value}</div>
                    </div>
                  </div>
                ))}
              </div>
              <div style={{ marginTop: 20, paddingTop: 16, borderTop: '1px solid var(--border-subtle)' }}>
                <span className={`badge badge-${selected.status === 'ACTIVE' ? 'active' : 'inactive'}`} style={{ fontSize: '0.8rem', padding: '5px 14px' }}>
                  {selected.status}
                </span>
              </div>

              {/* Assignments Section */}
              {selected.status === 'ACTIVE' && canViewAssignments() && (
                <div style={{ marginTop: 16, paddingTop: 16, borderTop: '1px solid var(--border-subtle)' }}>
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 12 }}>
                    <h4 style={{ fontSize: '0.85rem', fontWeight: 700, color: 'var(--text-secondary)' }}>Assigned Employees</h4>
                    {isManager() && (isAdmin() || myWarehouseId === selected.id) && (
                      <button className="btn btn-primary btn-sm" onClick={() => setAssignModal(selected.id)}>
                        <UserPlus size={14} /> Assign
                      </button>
                    )}
                  </div>
                  {assignmentsLoading ? (
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, color: 'var(--text-muted)', fontSize: '0.82rem' }}>
                      <span className="spinner" style={{ width: 14, height: 14 }} /> Loading...
                    </div>
                  ) : assignments.length === 0 ? (
                    <p style={{ fontSize: '0.82rem', color: 'var(--text-muted)' }}>No employees assigned yet.</p>
                  ) : (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                      {assignments.map((a, i) => (
                        <div key={a.id || i} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '8px 10px', background: 'var(--bg-surface)', borderRadius: 8, border: '1px solid var(--border-subtle)' }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                            <div style={{ width: 28, height: 28, borderRadius: 7, background: 'linear-gradient(135deg, rgba(99,102,241,0.3), rgba(79,70,229,0.3))', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.7rem', fontWeight: 700, color: 'var(--primary)' }}>
                              {a.employeeCode?.charAt(a.employeeCode.length - 1) || '?'}
                            </div>
                            <div>
                              <div style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-primary)' }}>{a.employeeCode}</div>
                              <div style={{ fontSize: '0.68rem', color: 'var(--text-muted)' }}>{(a.role || '').replace(/_/g, ' ')}</div>
                            </div>
                          </div>
                          <span className={`badge badge-${a.status === 'ACTIVE' ? 'active' : 'inactive'}`} style={{ fontSize: '0.65rem' }}>{a.status || 'ACTIVE'}</span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}
            </motion.div>
          )}
        </AnimatePresence>
      </div>

      {/* Modal */}
      <AnimatePresence>
        {modal && (
          <WarehouseModal
            warehouse={modal === 'create' ? null : modal}
            onClose={() => setModal(null)}
            onSaved={() => { setModal(null); load(); }}
          />
        )}
        {assignModal && (
          <AssignModal
            warehouseId={assignModal}
            onClose={() => setAssignModal(null)}
            onSaved={() => { setAssignModal(null); loadAssignments(assignModal); }}
          />
        )}
      </AnimatePresence>
    </div>
  );
}
