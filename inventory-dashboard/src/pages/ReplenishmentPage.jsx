import { useState, useEffect, useCallback } from 'react';
import { motion } from 'framer-motion';
import { replenishmentApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';
import { RefreshCw, CheckCircle, XCircle, Clock } from 'lucide-react';

export default function ReplenishmentPage() {
  const { canApproveReplenishment } = useAuth();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [activeTab, setActiveTab] = useState('all');

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await replenishmentApi.getAll({ page, size: 15 });
      const d = res.data?.data;
      setItems(d?.content || d || []);
      setTotalPages(d?.totalPages || 1);
    } catch { toast.error('Failed to load replenishment requests'); }
    finally { setLoading(false); }
  }, [page]);

  useEffect(() => { load(); }, [load]);

  const approve = async (id) => {
    try { await replenishmentApi.approve(id); toast.success('Approved!'); load(); }
    catch (err) { toast.error(err.response?.data?.message || 'Failed'); }
  };

  const reject = async (id) => {
    try { await replenishmentApi.reject(id); toast.success('Rejected'); load(); }
    catch (err) { toast.error(err.response?.data?.message || 'Failed'); }
  };

  const STATUS_CONFIG = {
    PENDING: { badge: 'warning', icon: Clock, label: 'Pending' },
    APPROVED: { badge: 'success', icon: CheckCircle, label: 'Approved' },
    REJECTED: { badge: 'danger', icon: XCircle, label: 'Rejected' },
    COMPLETED: { badge: 'info', icon: CheckCircle, label: 'Completed' },
  };

  const tabs = [
    { key: 'all', label: 'All' },
    { key: 'PENDING', label: '⏳ Pending' },
    { key: 'APPROVED', label: '✅ Approved' },
  ];
  const display = items.filter(i => activeTab === 'all' || i.status === activeTab);

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Replenishment</h1>
          <p className="page-subtitle">Manage stock replenishment requests</p>
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-4 gap-4 mb-6">
        {Object.entries({ PENDING: '#f59e0b', APPROVED: '#10b981', REJECTED: '#ef4444', COMPLETED: '#06b6d4' }).map(([s, color], i) => (
          <motion.div key={s} className="stat-card" initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.05 }}>
            <div className="stat-icon" style={{ background: `${color}20` }}><RefreshCw size={22} color={color} /></div>
            <div>
              <div className="stat-label">{s}</div>
              <div className="stat-value">{items.filter(x => x.status === s).length}</div>
            </div>
            <div style={{ position: 'absolute', top: 0, right: 0, width: 100, height: 100, borderRadius: '50%', background: color, opacity: 0.06, transform: 'translate(30%,-30%)' }} />
          </motion.div>
        ))}
      </div>

      <div className="tabs">
        {tabs.map(t => (
          <button key={t.key} className={`tab ${activeTab === t.key ? 'active' : ''}`} onClick={() => setActiveTab(t.key)}>{t.label}</button>
        ))}
      </div>

      <div className="table-wrapper">
        {loading ? (
          <div className="loading-state"><div className="spinner" /><span>Loading...</span></div>
        ) : display.length === 0 ? (
          <div className="empty-state">
            <RefreshCw size={48} color="var(--text-muted)" />
            <h3>No replenishment requests</h3>
            <p>Requests are auto-generated when stock falls below reorder level.</p>
          </div>
        ) : (
          <table className="data-table">
            <thead>
              <tr><th>ID</th><th>Product</th><th>Warehouse</th><th>Qty Requested</th><th>Status</th><th>Created</th>
              {canApproveReplenishment() && <th style={{ textAlign: 'right' }}>Actions</th>}</tr>
            </thead>
            <tbody>
              {display.map(r => {
                const sc = STATUS_CONFIG[r.status] || STATUS_CONFIG.PENDING;
                return (
                  <tr key={r.id}>
                    <td style={{ color: 'var(--text-muted)', fontSize: '0.8rem' }}>#{r.id}</td>
                    <td><div style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{r.productName || `Product #${r.productId}`}</div></td>
                    <td style={{ color: 'var(--text-secondary)' }}>{r.warehouseName || `Warehouse #${r.warehouseId}`}</td>
                    <td><span style={{ fontWeight: 800, color: '#818cf8', fontSize: '1rem' }}>{r.requestedQuantity || r.quantity}</span></td>
                    <td>
                      <span className={`badge badge-${sc.badge}`}>
                        {r.status}
                      </span>
                    </td>
                    <td style={{ color: 'var(--text-muted)', fontSize: '0.82rem' }}>
                      {r.createdAt ? new Date(r.createdAt).toLocaleDateString('en-IN') : '—'}
                    </td>
                    {canApproveReplenishment() && (
                      <td>
                        {r.status === 'PENDING' && (
                          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
                            <button className="btn btn-success btn-sm" onClick={() => approve(r.id)}><CheckCircle size={13} /> Approve</button>
                            <button className="btn btn-danger btn-sm" onClick={() => reject(r.id)}><XCircle size={13} /> Reject</button>
                          </div>
                        )}
                      </td>
                    )}
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
