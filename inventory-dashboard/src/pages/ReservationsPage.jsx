import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { reservationApi, inventoryApi, productApi } from '../services/api';
import toast from 'react-hot-toast';
import { BookMarked, Plus, X, Search, Info, CheckCircle } from 'lucide-react';

// The backend has no GET /reservations list endpoint.
// This page allows creating reservations and shows a session history.

function CreateReservationModal({ onClose, onCreated }) {
  const [step, setStep] = useState(1); // 1=search inventory, 2=fill form
  const [inventorySearch, setInventorySearch] = useState('');
  const [inventories, setInventories] = useState([]);
  const [products, setProducts] = useState([]);
  const [searching, setSearching] = useState(false);
  const [selected, setSelected] = useState(null);
  const [form, setForm] = useState({ referenceNumber: '', referenceType: '', reservedQuantity: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const searchInventory = async () => {
    if (!inventorySearch.trim()) return;
    setSearching(true);
    try {
      const [iRes, pRes] = await Promise.all([
        inventoryApi.search({ search: inventorySearch, size: 20 }),
        productApi.getAll({ size: 200 }),
      ]);
      const id = iRes.data?.data;
      setInventories(id?.content || id || []);
      const pd = pRes.data?.data;
      setProducts(pd?.content || pd || []);
    } catch { toast.error('Search failed'); }
    finally { setSearching(false); }
  };

  const getProdName = (productId) => products.find(p => p.id === productId)?.productName || `Product #${productId}`;

  const handleSubmit = async (e) => {
    e.preventDefault(); setLoading(true); setError('');
    try {
      const res = await reservationApi.create({
        inventoryId: selected.id,
        referenceNumber: form.referenceNumber,
        referenceType: form.referenceType,
        reservedQuantity: Number(form.reservedQuantity),
      });
      toast.success('Reservation created successfully!');
      onCreated(res.data?.data);
      onClose();
    } catch (err) { setError(err.response?.data?.message || 'Failed to create reservation'); }
    finally { setLoading(false); }
  };

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <motion.div className="modal" initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }}>
        <div className="modal-header">
          <div>
            <h2 className="modal-title">New Reservation</h2>
            <p style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: 2 }}>Step {step} of 2</p>
          </div>
          <button className="btn btn-icon btn-secondary" onClick={onClose}><X size={18} /></button>
        </div>

        {/* Progress bar */}
        <div style={{ height: 4, background: 'var(--border-subtle)', borderRadius: 2, marginBottom: 20, overflow: 'hidden' }}>
          <div style={{ height: '100%', width: step === 1 ? '50%' : '100%', background: 'var(--primary)', borderRadius: 2, transition: 'width 0.3s' }} />
        </div>

        {step === 1 && (
          <div>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: 14 }}>Search for an inventory item to reserve stock from:</p>
            <div style={{ display: 'flex', gap: 8, marginBottom: 16 }}>
              <div className="search-box" style={{ flex: 1 }}>
                <Search size={15} />
                <input placeholder="Search by product name..." value={inventorySearch} onChange={e => setInventorySearch(e.target.value)} onKeyDown={e => e.key === 'Enter' && searchInventory()} />
              </div>
              <button className="btn btn-primary" onClick={searchInventory} disabled={searching}>
                {searching ? <span className="spinner" /> : 'Search'}
              </button>
            </div>
            {inventories.length > 0 && (
              <div style={{ maxHeight: 300, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: 8 }}>
                {inventories.map(inv => (
                  <button key={inv.id} onClick={() => { setSelected(inv); setStep(2); }}
                    style={{
                      padding: '12px 16px', borderRadius: 10, border: '1px solid var(--border-subtle)',
                      background: 'var(--bg-surface)', cursor: 'pointer', textAlign: 'left',
                      display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                      fontFamily: 'Inter, sans-serif', transition: 'all 0.15s',
                    }}
                    onMouseEnter={e => e.currentTarget.style.borderColor = 'var(--primary)'}
                    onMouseLeave={e => e.currentTarget.style.borderColor = 'var(--border-subtle)'}
                  >
                    <div>
                      <div style={{ fontWeight: 600, color: 'var(--text-primary)', fontSize: '0.875rem' }}>{getProdName(inv.productId)}</div>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: 2 }}>Available: {inv.availableQuantity} units</div>
                    </div>
                    <span style={{ fontSize: '0.72rem', color: 'var(--primary)', fontWeight: 700 }}>SELECT →</span>
                  </button>
                ))}
              </div>
            )}
            {inventories.length === 0 && inventorySearch && !searching && (
              <div style={{ textAlign: 'center', color: 'var(--text-muted)', padding: '20px 0', fontSize: '0.85rem' }}>
                No inventory found. Try a different search term.
              </div>
            )}
          </div>
        )}

        {step === 2 && selected && (
          <div>
            {/* Selected inventory info */}
            <div style={{ background: 'var(--bg-surface)', borderRadius: 10, padding: '12px 16px', marginBottom: 20, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <div>
                <div style={{ fontWeight: 700, color: 'var(--text-primary)', fontSize: '0.9rem' }}>{getProdName(selected.productId)}</div>
                <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: 3 }}>Available: <span style={{ color: 'var(--success)', fontWeight: 700 }}>{selected.availableQuantity} units</span></div>
              </div>
              <button className="btn btn-secondary btn-sm" onClick={() => setStep(1)}>← Back</button>
            </div>

            {error && <div className="alert alert-error"><X size={14} />{error}</div>}

            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label className="form-label">Reference Number *</label>
                <input className="form-control" placeholder="SO-2024-001" value={form.referenceNumber} onChange={e => setForm(f => ({ ...f, referenceNumber: e.target.value }))} required />
              </div>
              <div className="form-group">
                <label className="form-label">Reference Type</label>
                <select className="form-control" value={form.referenceType} onChange={e => setForm(f => ({ ...f, referenceType: e.target.value }))}>
                  {['SALES_ORDER', 'PURCHASE_ORDER', 'TRANSFER_ORDER', 'INTERNAL'].map(t => <option key={t}>{t}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Quantity to Reserve * (max: {selected.availableQuantity})</label>
                <input className="form-control" type="number" min="1" max={selected.availableQuantity} placeholder="Enter quantity" value={form.reservedQuantity} onChange={e => setForm(f => ({ ...f, reservedQuantity: e.target.value }))} required />
              </div>
              <div style={{ display: 'flex', gap: 10, justifyContent: 'flex-end', marginTop: 8 }}>
                <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={loading}>
                  {loading ? <><span className="spinner" /> Creating...</> : <><BookMarked size={15} /> Create Reservation</>}
                </button>
              </div>
            </form>
          </div>
        )}
      </motion.div>
    </div>
  );
}

export default function ReservationsPage() {
  const [showModal, setShowModal] = useState(false);
  // Session-only list of created reservations (backend has no GET list endpoint)
  const [sessionReservations, setSessionReservations] = useState([]);

  const handleCreated = (reservation) => {
    setSessionReservations(prev => [reservation, ...prev]);
  };

  const cancelReservation = async (id, sessionId) => {
    if (!window.confirm('Cancel this reservation?')) return;
    // Try backend cancel
    if (id) {
      try {
        await reservationApi.cancel(id);
        toast.success('Reservation cancelled');
      } catch (err) {
        toast.error(err.response?.data?.message || 'Cancel failed');
        return;
      }
    }
    setSessionReservations(prev =>
      prev.map(r => r.sessionId === sessionId ? { ...r, status: 'CANCELLED' } : r)
    );
  };

  const STATUS_COLORS = { ACTIVE: 'active', CANCELLED: 'inactive', FULFILLED: 'success' };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Reservations</h1>
          <p className="page-subtitle">Create and track stock reservations</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowModal(true)}>
          <Plus size={17} /> New Reservation
        </button>
      </div>

      {/* Info banner */}
      <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }}
        style={{ display: 'flex', alignItems: 'flex-start', gap: 12, padding: '14px 18px', background: 'rgba(99,102,241,0.08)', border: '1px solid rgba(99,102,241,0.2)', borderRadius: 12, marginBottom: 24 }}>
        <Info size={18} color="var(--primary)" style={{ flexShrink: 0, marginTop: 2 }} />
        <div>
          <div style={{ fontWeight: 600, color: 'var(--primary)', fontSize: '0.875rem' }}>How Reservations Work</div>
          <div style={{ fontSize: '0.82rem', color: 'var(--text-muted)', marginTop: 4, lineHeight: 1.6 }}>
            Reservations lock stock for a specific reference (Sales Order, Transfer, etc.). Search for an inventory item, specify the quantity and reference, and the system will hold that stock. To cancel a reservation, use the Cancel button below.
          </div>
        </div>
      </motion.div>

      {/* Session reservations */}
      {sessionReservations.length > 0 ? (
        <div className="table-wrapper">
          <div style={{ padding: '14px 18px', borderBottom: '1px solid var(--border-subtle)', display: 'flex', alignItems: 'center', gap: 8 }}>
            <CheckCircle size={16} color="var(--success)" />
            <span style={{ fontWeight: 600, fontSize: '0.875rem', color: 'var(--text-primary)' }}>
              This Session ({sessionReservations.length})
            </span>
            <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>— reservations created in this session</span>
          </div>
          <table className="data-table">
            <thead>
              <tr><th>Reference</th><th>Type</th><th>Inventory ID</th><th>Qty Reserved</th><th>Status</th><th style={{ textAlign: 'right' }}>Actions</th></tr>
            </thead>
            <tbody>
              {sessionReservations.map((r, idx) => (
                <tr key={idx}>
                  <td><span style={{ fontFamily: 'monospace', fontSize: '0.84rem', color: 'var(--primary)', fontWeight: 700 }}>{r.referenceNumber}</span></td>
                  <td><span className="badge badge-info">{r.referenceType?.replace('_', ' ')}</span></td>
                  <td style={{ color: 'var(--text-muted)' }}>#{r.inventoryId}</td>
                  <td><span style={{ fontWeight: 800, fontSize: '1rem', color: '#06b6d4' }}>{r.reservedQuantity}</span></td>
                  <td><span className={`badge badge-${STATUS_COLORS[r.status] || 'info'}`}>{r.status || 'ACTIVE'}</span></td>
                  <td>
                    {(r.status || 'ACTIVE') === 'ACTIVE' && (
                      <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                        <button className="btn btn-danger btn-sm"
                          onClick={() => {
                            setSessionReservations(prev =>
                              prev.map((x, i) => i === idx ? { ...x, status: 'CANCELLED' } : x)
                            );
                            if (r.id) reservationApi.cancel(r.id).catch(() => {});
                          }}>
                          <X size={13} /> Cancel
                        </button>
                      </div>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <div className="empty-state">
          <BookMarked size={52} color="var(--text-muted)" />
          <h3>No reservations yet</h3>
          <p>Create your first reservation by clicking <strong>New Reservation</strong> above. You can also reserve stock directly from the Inventory page.</p>
        </div>
      )}

      <AnimatePresence>
        {showModal && (
          <CreateReservationModal
            onClose={() => setShowModal(false)}
            onCreated={handleCreated}
          />
        )}
      </AnimatePresence>
    </div>
  );
}
