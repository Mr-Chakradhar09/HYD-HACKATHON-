import { useState, useEffect, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { movementApi, productApi, warehouseApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';
import { Plus, X, ArrowLeftRight, Search, Filter } from 'lucide-react';

const MOVEMENT_COLORS = {
  GOODS_RECEIPT: '#10b981', GOODS_ISSUE: '#ef4444',
  TRANSFER_OUT: '#f59e0b', TRANSFER_IN: '#06b6d4', ADJUSTMENT: '#8b5cf6',
  ADJUSTMENT_INCREASE: '#8b5cf6', ADJUSTMENT_DECREASE: '#8b5cf6',
  RETURN_IN: '#06b6d4', RETURN_OUT: '#f59e0b',
  DAMAGE: '#ef4444', DISPOSAL: '#ef4444', CYCLE_COUNT: '#8b5cf6', INITIAL_STOCK: '#10b981',
};

function CreateMovementModal({ products, warehouses, onClose, onSaved }) {
  const { hasRole } = useAuth();
  const [form, setForm] = useState({ movementType: '', productId: '', sourceWarehouseId: '', destinationWarehouseId: '', quantity: '', referenceType: '', referenceNumber: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const set = (f, v) => setForm(p => ({ ...p, [f]: v }));

  // Transfers are removed — they go through the approval workflow
  const allMovementTypes = [
    { key: 'GOODS_RECEIPT', label: 'Goods Receipt', color: '#10b981' },
    { key: 'GOODS_ISSUE', label: 'Goods Issue', color: '#ef4444' },
    { key: 'ADJUSTMENT', label: 'Adjustment', color: '#8b5cf6' },
  ];

  const needsSource = ['GOODS_ISSUE'].includes(form.movementType);

  const handleSubmit = async (e) => {
    e.preventDefault(); setLoading(true); setError('');
    try {
      const payload = {
        movementType: form.movementType,
        productId: Number(form.productId),
        quantity: Number(form.quantity),
        referenceType: form.referenceType,
        referenceNumber: form.referenceNumber,
      };
      if (form.sourceWarehouseId) payload.sourceWarehouseId = Number(form.sourceWarehouseId);
      if (form.destinationWarehouseId) payload.destinationWarehouseId = Number(form.destinationWarehouseId);
      await movementApi.create(payload);
      toast.success('Movement recorded!');
      onSaved();
    } catch (err) { setError(err.response?.data?.message || 'Failed to create movement'); }
    finally { setLoading(false); }
  };

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <motion.div className="modal" initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }}>
        <div className="modal-header">
          <h2 className="modal-title">Record Movement</h2>
          <button className="btn btn-icon btn-secondary" onClick={onClose}><X size={18} /></button>
        </div>
        {error && <div className="alert alert-error"><X size={14} />{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Movement Type *</label>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
              {allMovementTypes.map(t => (
                <button key={t.key} type="button" onClick={() => set('movementType', t.key)}
                  style={{
                    padding: '8px 14px', borderRadius: 8, border: '1px solid', cursor: 'pointer',
                    fontSize: '0.78rem', fontWeight: 700, fontFamily: 'Inter, sans-serif',
                    borderColor: form.movementType === t.key ? t.color : 'var(--border-subtle)',
                    background: form.movementType === t.key ? `${t.color}18` : 'transparent',
                    color: form.movementType === t.key ? t.color : 'var(--text-muted)',
                  }}
                >{t.label}</button>
              ))}
            </div>
            <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', marginTop: 6 }}>
              Transfers are handled through the Transfer Request workflow.
            </div>
          </div>
          <div className="grid grid-2 gap-4">
            <div className="form-group" style={{ gridColumn: '1/-1' }}>
              <label className="form-label">Product *</label>
              <select className="form-control" value={form.productId} onChange={e => set('productId', e.target.value)} required>
                <option value="">Select product...</option>
                {products.map(p => <option key={p.id} value={p.id}>{p.productName} ({p.sku})</option>)}
              </select>
            </div>
            {needsSource && (
              <div className="form-group">
                <label className="form-label">Source Warehouse *</label>
                <select className="form-control" value={form.sourceWarehouseId} onChange={e => set('sourceWarehouseId', e.target.value)} required={needsSource}>
                  <option value="">Select warehouse...</option>
                  {warehouses.map(w => <option key={w.id} value={w.id}>{w.warehouseName}</option>)}
                </select>
              </div>
            )}
            {form.movementType === 'GOODS_RECEIPT' && (
              <div className="form-group">
                <label className="form-label">Destination Warehouse *</label>
                <select className="form-control" value={form.destinationWarehouseId} onChange={e => set('destinationWarehouseId', e.target.value)} required>
                  <option value="">Select warehouse...</option>
                  {warehouses.map(w => <option key={w.id} value={w.id}>{w.warehouseName}</option>)}
                </select>
              </div>
            )}
            <div className="form-group">
              <label className="form-label">Quantity *</label>
              <input className="form-control" type="number" min="1" placeholder="0" value={form.quantity} onChange={e => set('quantity', e.target.value)} required />
            </div>
            <div className="form-group">
              <label className="form-label">Reference Type</label>
              <select className="form-control" value={form.referenceType} onChange={e => set('referenceType', e.target.value)}>
                {['PURCHASE_ORDER', 'SALES_ORDER', 'RETURN', 'INTERNAL'].map(t => <option key={t}>{t}</option>)}
              </select>
            </div>
            <div className="form-group" style={{ gridColumn: '1/-1' }}>
              <label className="form-label">Reference Number *</label>
              <input className="form-control" placeholder="PO-2024-001" value={form.referenceNumber} onChange={e => set('referenceNumber', e.target.value)} required />
            </div>
          </div>
          <div style={{ display: 'flex', gap: 12, justifyContent: 'flex-end', marginTop: 8 }}>
            <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? <><span className="spinner" /> Recording...</> : 'Record Movement'}
            </button>
          </div>
        </form>
      </motion.div>
    </div>
  );
}

export default function MovementsPage() {
  const { isManager } = useAuth();
  const [movements, setMovements] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [products, setProducts] = useState([]);
  const [warehouses, setWarehouses] = useState([]);
  const [search, setSearch] = useState('');
  const [typeFilter, setTypeFilter] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const params = { page, size: 15 };
      if (typeFilter) params.movementType = typeFilter;
      const [mRes, pRes, wRes] = await Promise.all([
        movementApi.getAll(params),
        productApi.getAll({ size: 200 }),
        warehouseApi.getAll({ size: 100 }),
      ]);
      const md = mRes.data?.data; setMovements(md?.content || md || []); setTotalPages(md?.totalPages || 1);
      const pd = pRes.data?.data; setProducts(pd?.content || pd || []);
      const wd = wRes.data?.data; setWarehouses(wd?.content || wd || []);
    } catch { toast.error('Failed to load movements'); }
    finally { setLoading(false); }
  }, [page, typeFilter]);

  useEffect(() => { load(); }, [load]);

  const getProd = (id) => products.find(p => p.id === id);
  const getWh = (id) => warehouses.find(w => w.id === id);

  const filtered = movements.filter(m =>
    !search || getProd(m.productId)?.productName?.toLowerCase().includes(search.toLowerCase()) ||
    m.referenceNumber?.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Stock Movements</h1>
          <p className="page-subtitle">Track all inventory in/out/transfer records</p>
        </div>
        {isManager() && (
          <button className="btn btn-primary" onClick={() => setShowModal(true)}><Plus size={18} /> Record Movement</button>
        )}
      </div>

      {/* Filters */}
      <div style={{ display: 'flex', gap: 12, marginBottom: 20, flexWrap: 'wrap', alignItems: 'center', minWidth: 0 }}>
        <div className="search-box" style={{ flex: '1 1 200px', minWidth: 0, maxWidth: 360 }}>
          <Search size={16} />
          <input placeholder="Search by product or reference..." value={search} onChange={e => setSearch(e.target.value)} />
          {search && <button onClick={() => setSearch('')} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)' }}><X size={14} /></button>}
        </div>
        <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
          <button className={`btn btn-sm ${!typeFilter ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setTypeFilter('')}>All</button>
          {Object.keys(MOVEMENT_COLORS).map(t => (
            <button key={t} className={`btn btn-sm ${typeFilter === t ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setTypeFilter(typeFilter === t ? '' : t)}
              style={typeFilter === t ? {} : { borderColor: `${MOVEMENT_COLORS[t]}40`, color: MOVEMENT_COLORS[t] }}
            >
              {t.replace('_', ' ')}
            </button>
          ))}
        </div>
      </div>

      <div className="table-wrapper">
        {loading ? (
          <div className="loading-state"><div className="spinner" /><span>Loading movements...</span></div>
        ) : filtered.length === 0 ? (
          <div className="empty-state">
            <ArrowLeftRight size={48} color="var(--text-muted)" />
            <h3>No movements found</h3>
            <p>Record your first stock movement.</p>
          </div>
        ) : (
          <table className="data-table">
            <thead>
              <tr><th>Type</th><th>Product</th><th>From</th><th>To</th><th>Qty</th><th>Reference</th><th>Date</th></tr>
            </thead>
            <tbody>
              {filtered.map(m => {
                const prod = getProd(m.productId);
                const srcWh = getWh(m.sourceWarehouseId);
                const dstWh = getWh(m.destinationWarehouseId);
                const color = MOVEMENT_COLORS[m.movementType] || '#818cf8';
                return (
                  <tr key={m.id}>
                    <td>
                      <span style={{ display: 'inline-flex', alignItems: 'center', gap: 6, padding: '4px 10px', borderRadius: 20, background: `${color}15`, border: `1px solid ${color}30`, fontSize: '0.72rem', fontWeight: 700, color, whiteSpace: 'nowrap' }}>
                        {m.movementType?.replace('_', ' ')}
                      </span>
                    </td>
                    <td>
                      <div style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{prod?.productName || `#${m.productId}`}</div>
                      <div style={{ fontSize: '0.73rem', color: 'var(--text-muted)' }}>{prod?.sku}</div>
                    </td>
                    <td style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>{srcWh?.warehouseName || '—'}</td>
                    <td style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>{dstWh?.warehouseName || '—'}</td>
                    <td><span style={{ fontWeight: 800, fontSize: '1rem', color }}>{m.quantity}</span></td>
                    <td>
                      <div style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>{m.referenceNumber}</div>
                      <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>{m.referenceType}</div>
                    </td>
                    <td style={{ color: 'var(--text-muted)', fontSize: '0.82rem' }}>
                      {m.createdAt ? new Date(m.createdAt).toLocaleDateString('en-IN') : '—'}
                    </td>
                  </tr>
                );
              })}
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
        {showModal && <CreateMovementModal products={products} warehouses={warehouses} onClose={() => setShowModal(false)} onSaved={() => { setShowModal(false); load(); }} />}
      </AnimatePresence>
    </div>
  );
}
