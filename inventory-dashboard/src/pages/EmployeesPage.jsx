import { useState, useEffect, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { authApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';
import { Plus, X, Search, Users, Edit3, ToggleLeft, ToggleRight, Trash2, Shield } from 'lucide-react';

const ALL_ROLES = ['SYSTEM_ADMIN', 'INVENTORY_MANAGER', 'WAREHOUSE_OPERATOR', 'PROCUREMENT_MANAGER'];

const ROLE_HIERARCHY = {
  SYSTEM_ADMIN: ALL_ROLES,
  INVENTORY_MANAGER: ['PROCUREMENT_MANAGER', 'WAREHOUSE_OPERATOR'],
  PROCUREMENT_MANAGER: [],
  WAREHOUSE_OPERATOR: [],
};

function EmployeeModal({ employee, onClose, onSaved, currentUserRoles }) {
  const isEdit = !!employee;
  const [form, setForm] = useState(isEdit ? { fullName: employee.fullName, email: employee.email, roles: employee.roles || [] }
    : { employeeCode: '', fullName: '', email: '', roles: [] });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const assignableRoles = (() => {
    for (const r of ALL_ROLES) {
      if (currentUserRoles?.includes(r)) return ROLE_HIERARCHY[r] || [];
    }
    return [];
  })();

  const toggleRole = (role) => {
    setForm(f => ({
      ...f,
      roles: f.roles.includes(role) ? f.roles.filter(r => r !== role) : [...f.roles, role]
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (form.roles.length === 0) { setError('At least one role is required'); return; }
    setLoading(true); setError('');
    try {
      if (isEdit) await authApi.updateEmployee(employee.employeeCode, form);
      else await authApi.createEmployee(form);
      toast.success(isEdit ? 'Employee updated!' : 'Employee created!');
      onSaved();
    } catch (err) { setError(err.response?.data?.message || 'Failed'); }
    finally { setLoading(false); }
  };

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <motion.div className="modal" style={{ maxWidth: 480 }} initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }}>
        <div className="modal-header">
          <h2 className="modal-title">{isEdit ? 'Edit Employee' : 'Add Employee'}</h2>
          <button className="btn btn-icon btn-secondary" onClick={onClose}><X size={18} /></button>
        </div>
        {error && <div className="alert alert-error"><X size={14} />{error}</div>}
        <form onSubmit={handleSubmit}>
          {!isEdit && (
            <div className="form-group">
              <label className="form-label">Employee Code * (format: EMP######)</label>
              <input className="form-control" placeholder="EMP000010" value={form.employeeCode} onChange={e => setForm(f => ({ ...f, employeeCode: e.target.value }))} required pattern="^EMP[0-9]{6}$" title="Must be EMP followed by 6 digits" />
            </div>
          )}
          <div className="form-group">
            <label className="form-label">Full Name *</label>
            <input className="form-control" placeholder="John Doe" value={form.fullName} onChange={e => setForm(f => ({ ...f, fullName: e.target.value }))} required />
          </div>
          <div className="form-group">
            <label className="form-label">Email *</label>
            <input className="form-control" type="email" placeholder="john@company.com" value={form.email} onChange={e => setForm(f => ({ ...f, email: e.target.value }))} required />
          </div>
          <div className="form-group">
            <label className="form-label">Roles * (select at least one)</label>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, marginTop: 4 }}>
              {ALL_ROLES.map(role => {
                const canAssign = assignableRoles.includes(role);
                const isSelected = form.roles.includes(role);
                return (
                  <button
                    key={role} type="button"
                    onClick={() => canAssign && toggleRole(role)}
                    disabled={!canAssign}
                    style={{
                      padding: '8px 14px', borderRadius: 8,
                      cursor: canAssign ? 'pointer' : 'not-allowed',
                      border: '1px solid', fontFamily: 'Inter, sans-serif',
                      fontSize: '0.78rem', fontWeight: 700, transition: 'all 0.2s',
                      borderColor: isSelected ? '#6366f1' : canAssign ? 'var(--border-subtle)' : 'var(--border-subtle)',
                      background: isSelected ? 'rgba(99,102,241,0.15)' : canAssign ? 'transparent' : 'rgba(0,0,0,0.03)',
                      color: isSelected ? '#818cf8' : canAssign ? 'var(--text-muted)' : 'var(--text-muted)',
                      opacity: canAssign ? 1 : 0.45,
                    }}
                  >
                    {isSelected ? '✓ ' : ''}{role.replace(/_/g, ' ')}
                  </button>
                );
              })}
            </div>
            {assignableRoles.length === 0 && (
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: 4 }}>
                Your role does not have permission to assign roles.
              </div>
            )}
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

export default function EmployeesPage() {
  const { isAdmin, user } = useAuth();
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [modal, setModal] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await authApi.getEmployees({ page, size: 15 });
      const d = res.data?.data;
      setEmployees(d?.content || d || []);
      setTotalPages(d?.totalPages || 1);
    } catch { toast.error('Failed to load employees'); }
    finally { setLoading(false); }
  }, [page]);

  useEffect(() => { load(); }, [load]);

  const toggle = async (emp) => {
    try {
      if (emp.status === 'ACTIVE') { await authApi.deactivateEmployee(emp.employeeCode); toast.success('Deactivated'); }
      else { await authApi.activateEmployee(emp.employeeCode); toast.success('Activated'); }
      load();
    } catch (err) { toast.error(err.response?.data?.message || 'Failed'); }
  };

  const filtered = employees.filter(e =>
    !search || e.fullName?.toLowerCase().includes(search.toLowerCase()) ||
    e.employeeCode?.toLowerCase().includes(search.toLowerCase()) ||
    e.email?.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Employees</h1>
          <p className="page-subtitle">Manage system users and role assignments</p>
        </div>
        {isAdmin() && (
          <button className="btn btn-primary" onClick={() => setModal('create')}><Plus size={18} /> Add Employee</button>
        )}
      </div>

      {/* Stats */}
      <div className="grid grid-4 gap-4 mb-6">
        {[
          { label: 'Total', value: employees.length, color: '#6366f1' },
          { label: 'Active', value: employees.filter(e => e.status === 'ACTIVE').length, color: '#10b981' },
          { label: 'Inactive', value: employees.filter(e => e.status !== 'ACTIVE').length, color: '#ef4444' },
          { label: 'Admins', value: employees.filter(e => e.roles?.includes('SYSTEM_ADMIN')).length, color: '#f59e0b' },
        ].map((s, i) => (
          <motion.div key={s.label} className="stat-card" initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.05 }}>
            <div className="stat-icon" style={{ background: `${s.color}20` }}><Users size={22} color={s.color} /></div>
            <div>
              <div className="stat-label">{s.label}</div>
              <div className="stat-value">{s.value}</div>
            </div>
            <div style={{ position: 'absolute', top: 0, right: 0, width: 100, height: 100, borderRadius: '50%', background: s.color, opacity: 0.06, transform: 'translate(30%,-30%)' }} />
          </motion.div>
        ))}
      </div>

      <div style={{ marginBottom: 16 }}>
        <div className="search-box" style={{ maxWidth: 380 }}>
          <Search size={16} />
          <input placeholder="Search by name, code or email..." value={search} onChange={e => setSearch(e.target.value)} />
          {search && <button onClick={() => setSearch('')} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)' }}><X size={14} /></button>}
        </div>
      </div>

      <div className="table-wrapper">
        {loading ? (
          <div className="loading-state"><div className="spinner" /><span>Loading employees...</span></div>
        ) : filtered.length === 0 ? (
          <div className="empty-state">
            <Users size={48} color="var(--text-muted)" />
            <h3>No employees found</h3>
            <p>{search ? 'Try a different search term.' : 'Add your first employee.'}</p>
          </div>
        ) : (
          <table className="data-table">
            <thead>
              <tr><th>Code</th><th>Name</th><th>Email</th><th>Roles</th><th>Status</th>
              {isAdmin() && <th style={{ textAlign: 'right' }}>Actions</th>}</tr>
            </thead>
            <tbody>
              {filtered.map(emp => (
                <tr key={emp.employeeCode} style={{ background: emp.employeeCode === user?.employeeCode ? 'rgba(99,102,241,0.05)' : undefined }}>
                  <td>
                    <span style={{ fontFamily: 'monospace', fontSize: '0.82rem', color: '#818cf8', fontWeight: 700 }}>{emp.employeeCode}</span>
                    {emp.employeeCode === user?.employeeCode && <span style={{ marginLeft: 6, fontSize: '0.68rem', color: '#6366f1', fontWeight: 700 }}>(you)</span>}
                  </td>
                  <td>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                      <div style={{ width: 32, height: 32, borderRadius: 8, background: 'linear-gradient(135deg, rgba(99,102,241,0.3), rgba(79,70,229,0.3))', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.82rem', fontWeight: 700, color: '#a5b4fc', flexShrink: 0 }}>
                        {emp.fullName?.charAt(0)}
                      </div>
                      <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{emp.fullName}</span>
                    </div>
                  </td>
                  <td style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>{emp.email}</td>
                  <td>
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: 4 }}>
                      {(emp.roles || []).map(r => (
                        <span key={r} className={`badge ${r === 'SYSTEM_ADMIN' ? 'badge-warning' : 'badge-primary'}`} style={{ fontSize: '0.65rem' }}>
                          {r === 'SYSTEM_ADMIN' ? '★ ' : ''}{r.replace(/_/g, ' ')}
                        </span>
                      ))}
                    </div>
                  </td>
                  <td><span className={`badge badge-${emp.status === 'ACTIVE' ? 'active' : 'inactive'}`}>{emp.status}</span></td>
                  {isAdmin() && (
                    <td>
                      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
                        <button className="btn btn-secondary btn-sm" onClick={() => setModal(emp)} title="Edit"><Edit3 size={13} /></button>
                        {emp.employeeCode !== user?.employeeCode && (
                          <button className={`btn btn-sm ${emp.status === 'ACTIVE' ? 'btn-danger' : 'btn-success'}`} onClick={() => toggle(emp)}>
                            {emp.status === 'ACTIVE' ? <ToggleRight size={14} /> : <ToggleLeft size={14} />}
                            {emp.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
                          </button>
                        )}
                      </div>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        )}
        {totalPages > 1 && (
          <div className="pagination">
            <button className="page-btn" disabled={page === 0} onClick={() => setPage(p => p - 1)}>←</button>
            {Array.from({ length: Math.min(totalPages, 7) }, (_, i) => <button key={i} className={`page-btn ${i === page ? 'active' : ''}`} onClick={() => setPage(i)}>{i + 1}</button>)}
            <button className="page-btn" disabled={page === totalPages - 1} onClick={() => setPage(p => p + 1)}>→</button>
          </div>
        )}
      </div>

      <AnimatePresence>
        {modal && (
          <EmployeeModal
            employee={modal === 'create' ? null : modal}
            onClose={() => setModal(null)}
            onSaved={() => { setModal(null); load(); }}
            currentUserRoles={user?.roles || []}
          />
        )}
      </AnimatePresence>
    </div>
  );
}
