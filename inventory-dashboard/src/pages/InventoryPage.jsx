import { useState, useEffect, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useNavigate } from 'react-router-dom';
import { inventoryApi, adjustmentApi, productApi, warehouseApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';
import { X, AlertTriangle, Search, TrendingUp, TrendingDown, BookMarked, RefreshCw, Eye, EyeOff } from 'lucide-react';

function AdjustModal({ inventory, getProdName, onClose, onSaved }) {
  const [form, setForm] = useState({ adjustmentType: 'INCREASE', quantity: '', reason: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault(); setLoading(true); setError('');
    try {
      await adjustmentApi.create({
        inventoryId: inventory.id,
        adjustmentType: form.adjustmentType,
        quantity: Number(form.quantity),
        reason: form.reason,
      });
      toast.success('Adjustment applied!');
      onSaved();
    } catch (err) { setError(err.response?.data?.message || 'Adjustment failed'); }
    finally { setLoading(false); }
  };

  const typeConfig = {
    INCREASE: { color: '#10b981', label: '↑ Increase', bg: 'rgba(16,185,129,0.1)' },
    DECREASE: { color: '#ef4444', label: '↓ Decrease', bg: 'rgba(239,68,68,0.1)' },
    DAMAGE:   { color: '#f59e0b', label: '⚠ Damage',   bg: 'rgba(245,158,11,0.1)' },
  };

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <motion.div className="modal" style={{ maxWidth: 440 }} initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }}>
        <div className="modal-header">
          <h2 className="modal-title">Stock Adjustment</h2>
          <button className="btn btn-icon btn-secondary" onClick={onClose}><X size={18} /></button>
        </div>

        {/* Current stock info */}
        <div style={{ background: 'var(--bg-surface)', borderRadius: 10, padding: '14px 16px', marginBottom: 20, display: 'grid', gridTemplateColumns: 'repeat(3,1fr)', gap: 12, textAlign: 'center' }}>
          <div>
            <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 4 }}>Total</div>
            <div style={{ fontSize: '1.4rem', fontWeight: 800, color: 'var(--text-primary)' }}>{inventory.currentQuantity}</div>
          </div>
          <div>
            <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 4 }}>Available</div>
            <div style={{ fontSize: '1.4rem', fontWeight: 800, color: '#10b981' }}>{inventory.availableQuantity}</div>
          </div>
          <div>
            <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 4 }}>Reserved</div>
            <div style={{ fontSize: '1.4rem', fontWeight: 800, color: '#f59e0b' }}>{inventory.reservedQuantity || 0}</div>
          </div>
        </div>
        <div style={{ fontSize: '0.82rem', color: 'var(--text-muted)', marginBottom: 14, textAlign: 'center' }}>
          {getProdName(inventory.productId)}
        </div>

        {error && <div className="alert alert-error"><X size={14} />{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Adjustment Type</label>
            <div style={{ display: 'flex', gap: 8 }}>
              {Object.entries(typeConfig).map(([key, cfg]) => (
                <button key={key} type="button"
                  onClick={() => setForm(f => ({ ...f, adjustmentType: key }))}
                  style={{
                    flex: 1, padding: '9px 8px', borderRadius: 9, cursor: 'pointer',
                    fontSize: '0.78rem', fontWeight: 700, fontFamily: 'Inter, sans-serif',
                    border: `1px solid ${form.adjustmentType === key ? cfg.color : 'var(--border-subtle)'}`,
                    background: form.adjustmentType === key ? cfg.bg : 'transparent',
                    color: form.adjustmentType === key ? cfg.color : 'var(--text-muted)',
                    transition: 'all 0.15s',
                  }}
                >{cfg.label}</button>
              ))}
            </div>
          </div>
          <div className="form-group">
            <label className="form-label">Quantity *</label>
            <input className="form-control" type="number" min="1" placeholder="Enter quantity" value={form.quantity} onChange={e => setForm(f => ({ ...f, quantity: e.target.value }))} required />
          </div>
          <div className="form-group">
            <label className="form-label">Reason *</label>
            <textarea className="form-control" rows={2} placeholder="Reason for adjustment..." value={form.reason} onChange={e => setForm(f => ({ ...f, reason: e.target.value }))} required />
          </div>
          <div style={{ display: 'flex', gap: 10, justifyContent: 'flex-end', marginTop: 8 }}>
            <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? <><span className="spinner" /> Applying...</> : 'Apply Adjustment'}
            </button>
          </div>
        </form>
      </motion.div>
    </div>
  );
}

export default function InventoryPage() {
  const { isManager, isAdmin } = useAuth();
  const navigate = useNavigate();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [adjustTarget, setAdjustTarget] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [products, setProducts] = useState([]);
  const [warehouses, setWarehouses] = useState([]);
  const [activeTab, setActiveTab] = useState('all');
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
      const [iRes, pRes, wRes] = await Promise.all([
        inventoryApi.getAll({ page, size: 15 }),
        productApi.getAll({ size: 200 }),
        warehouseApi.getAll({ size: 100 }),
      ]);
      const id = iRes.data?.data; setItems(id?.content || id || []); setTotalPages(id?.totalPages || 1);
      const pd = pRes.data?.data; setProducts(pd?.content || pd || []);
      const wd = wRes.data?.data; setWarehouses(wd?.content || wd || []);
    } catch { toast.error('Failed to load inventory'); }
    finally { setLoading(false); }
  }, [page]);

  useEffect(() => { load(); }, [load]);

  const getProdName = (id) => products.find(p => p.id === id)?.productName || `Product #${id}`;
  const getWhName  = (id) => warehouses.find(w => w.id === id)?.warehouseName || `Warehouse #${id}`;

  const lowStock = items.filter(i => i.currentQuantity < 10);
  const display = (activeTab === 'low' ? lowStock : items).filter(i => {
    if (showMyWarehouseOnly && myWarehouseId && i.warehouseId !== myWarehouseId) return false;
    if (!search) return true;
    return getProdName(i.productId).toLowerCase().includes(search.toLowerCase()) ||
      getWhName(i.warehouseId).toLowerCase().includes(search.toLowerCase());
  });

  const getStockColor = (qty) => qty <= 0 ? '#ef4444' : qty < 10 ? '#f59e0b' : '#10b981';

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Inventory</h1>
          <p className="page-subtitle">Monitor stock levels, apply adjustments and manage reservations</p>
        </div>
      </div>

      {/* Summary cards */}
      <div className="grid grid-4 gap-4 mb-6">
        {[
          { label: 'Total Items', value: items.length, color: '#6366f1', icon: RefreshCw },
          { label: 'Low Stock', value: lowStock.length, color: '#f59e0b', icon: AlertTriangle },
          { label: 'Out of Stock', value: items.filter(i => i.currentQuantity === 0).length, color: '#ef4444', icon: TrendingDown },
          { label: 'Total Reserved', value: items.reduce((s, i) => s + (i.reservedQuantity || 0), 0), color: '#06b6d4', icon: BookMarked },
        ].map((s, i) => (
          <motion.div key={s.label} className="stat-card" initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.05 }}>
            <div className="stat-icon" style={{ background: `${s.color}18` }}><s.icon size={20} color={s.color} /></div>
            <div>
              <div className="stat-label">{s.label}</div>
              <div className="stat-value">{s.value}</div>
            </div>
            <div style={{ position: 'absolute', top: 0, right: 0, width: 90, height: 90, borderRadius: '50%', background: s.color, opacity: 0.07, transform: 'translate(30%,-30%)' }} />
          </motion.div>
        ))}
      </div>

      {/* Filters */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 16, flexWrap: 'wrap', gap: 10, minWidth: 0 }}>
        <div className="tabs" style={{ margin: 0, flexShrink: 0 }}>
          <button className={`tab ${activeTab === 'all' ? 'active' : ''}`} onClick={() => setActiveTab('all')}>All ({items.length})</button>
          <button className={`tab ${activeTab === 'low' ? 'active' : ''}`} onClick={() => setActiveTab('low')}>
            Low Stock ({lowStock.length})
          </button>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8, flex: '1 1 200px', minWidth: 0, justifyContent: 'flex-end' }}>
          {isManager() && !isAdmin() && myWarehouseId && (
            <button
              className={`btn ${showMyWarehouseOnly ? 'btn-primary' : 'btn-secondary'}`}
              onClick={() => setShowMyWarehouseOnly(!showMyWarehouseOnly)}
              style={{ fontSize: '0.82rem', whiteSpace: 'nowrap', flexShrink: 0 }}
            >
              {showMyWarehouseOnly ? <EyeOff size={14} /> : <Eye size={14} />}
              {showMyWarehouseOnly ? 'Show All' : 'My Warehouse'}
            </button>
          )}
          <div className="search-box" style={{ flex: '1 1 200px', minWidth: 0, maxWidth: 360 }}>
            <Search size={15} />
            <input placeholder="Search product or warehouse..." value={search} onChange={e => setSearch(e.target.value)} />
            {search && <button onClick={() => setSearch('')} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)', display: 'flex' }}><X size={13} /></button>}
          </div>
        </div>
      </div>

      <div className="table-wrapper">
        {loading ? (
          <div className="loading-state"><div className="spinner" /><span>Loading inventory...</span></div>
        ) : display.length === 0 ? (
          <div className="empty-state">
            <AlertTriangle size={48} color="var(--text-muted)" />
            <h3>{activeTab === 'low' ? 'No low stock items' : 'No inventory items'}</h3>
            <p>{search ? 'Try a different search term.' : 'No inventory records found.'}</p>
          </div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Product</th><th>Warehouse</th><th>Stock</th><th>Available</th><th>Reserved</th><th>Status</th>
                <th style={{ textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {display.map(item => (
                <tr key={item.id}>
                  <td>
                    <div style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{getProdName(item.productId)}</div>
                    <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>ID #{item.productId}</div>
                  </td>
                  <td style={{ color: 'var(--text-secondary)', fontSize: '0.84rem' }}>{getWhName(item.warehouseId)}</td>
                  <td>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 9 }}>
                      <div style={{ width: 52, height: 5, background: 'var(--border-subtle)', borderRadius: 3, overflow: 'hidden', flexShrink: 0 }}>
                        <div style={{ height: '100%', width: `${Math.min((item.currentQuantity / (item.maxStockLevel || 100)) * 100, 100)}%`, background: getStockColor(item.currentQuantity), borderRadius: 3 }} />
                      </div>
                      <span style={{ fontWeight: 800, color: getStockColor(item.currentQuantity) }}>{item.currentQuantity}</span>
                    </div>
                  </td>
                  <td style={{ color: '#10b981', fontWeight: 700 }}>{item.availableQuantity}</td>
                  <td style={{ color: '#f59e0b' }}>{item.reservedQuantity || 0}</td>
                  <td><span className={`badge badge-${item.status === 'ACTIVE' ? 'active' : 'inactive'}`}>{item.status}</span></td>
                  <td>
                    <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
                      {isManager() && (
                        <button className="btn btn-secondary btn-sm" onClick={() => setAdjustTarget(item)} title="Stock Adjustment">
                          <TrendingUp size={13} /> Adjust
                        </button>
                      )}
                      <button className="btn btn-secondary btn-sm" onClick={() => navigate('/reservations')} title="Create Reservation">
                        <BookMarked size={13} /> Reserve
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
        {totalPages > 1 && (
          <div className="pagination">
            <button className="page-btn" disabled={page === 0} onClick={() => setPage(p => p - 1)}>←</button>
            {Array.from({ length: Math.min(totalPages, 7) }, (_, i) => (
              <button key={i} className={`page-btn ${i === page ? 'active' : ''}`} onClick={() => setPage(i)}>{i + 1}</button>
            ))}
            <button className="page-btn" disabled={page === totalPages - 1} onClick={() => setPage(p => p + 1)}>→</button>
          </div>
        )}
      </div>

      <AnimatePresence>
        {adjustTarget && (
          <AdjustModal
            inventory={adjustTarget}
            getProdName={getProdName}
            onClose={() => setAdjustTarget(null)}
            onSaved={() => { setAdjustTarget(null); load(); }}
          />
        )}
      </AnimatePresence>
    </div>
  );
}
