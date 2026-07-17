import { useState, useEffect, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { transferRequestApi, productApi, warehouseApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';
import {
  Plus, X, ArrowLeftRight, Search, Check, Ban, Clock, Send,
  ChevronRight, Package, Truck, ArrowDownToLine, Star, AlertTriangle
} from 'lucide-react';

const STATUS_COLORS = {
  PENDING: '#f59e0b',
  APPROVED: '#10b981',
  DISPATCHED: '#3b82f6',
  REJECTED: '#ef4444',
  CANCELLED: '#6b7280',
  COMPLETED: '#059669',
};

function CreateTransferWizard({ products, warehouses, userWarehouseId, onClose, onSaved }) {
  const { hasRole } = useAuth();
  const [step, setStep] = useState(1);
  const [search, setSearch] = useState('');
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [quantity, setQuantity] = useState('');
  const [recommendations, setRecommendations] = useState([]);
  const [loadingRecs, setLoadingRecs] = useState(false);
  const [selectedSource, setSelectedSource] = useState(null);
  const [reason, setReason] = useState('');
  const [remarks, setRemarks] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const canCreate = hasRole('INVENTORY_MANAGER') || hasRole('PROCUREMENT_MANAGER');

  const filteredProducts = products.filter(p =>
    !search || p.productName?.toLowerCase().includes(search.toLowerCase()) ||
    p.sku?.toLowerCase().includes(search.toLowerCase())
  );

  const handleSelectProduct = (product) => {
    setSelectedProduct(product);
    setStep(2);
    setSearch('');
  };

  const handleCheckAvailability = async () => {
    if (!selectedProduct || !quantity || Number(quantity) <= 0) return;
    setLoadingRecs(true); setError('');
    try {
      const apiBase = '/api/v1';
      const res = await fetch(`${apiBase}/inventories/transfer-recommendations?productId=${selectedProduct.id}&quantity=${Number(quantity)}`, {
        headers: { 'Authorization': `Bearer ${localStorage.getItem('authToken')}` }
      });
      const data = await res.json();
      if (data.success && data.data) {
        setRecommendations(data.data);
        const recommended = data.data.find(r => r.isRecommended);
        if (recommended) setSelectedSource(recommended.warehouseId);
        setStep(3);
      } else {
        setError(data.message || 'Failed to get recommendations');
      }
    } catch (err) { setError('Failed to check availability'); }
    finally { setLoadingRecs(false); }
  };

  const handleSubmit = async () => {
    if (!selectedProduct || !selectedSource || !quantity) return;
    setSubmitting(true); setError('');
    try {
      await transferRequestApi.create({
        productId: selectedProduct.id,
        sourceWarehouseId: Number(selectedSource),
        destinationWarehouseId: Number(userWarehouseId),
        quantity: Number(quantity),
        reason,
        remarks,
      });
      toast.success('Transfer request submitted for approval!');
      onSaved();
    } catch (err) { setError(err.response?.data?.message || 'Failed to create transfer request'); }
    finally { setSubmitting(false); }
  };

  if (!canCreate) {
    return (
      <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
        <motion.div className="modal" initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }} style={{ maxWidth: 420 }}>
          <div className="modal-header">
            <h2 className="modal-title">Transfer Request</h2>
            <button className="btn btn-icon btn-secondary" onClick={onClose}><X size={18} /></button>
          </div>
          <div style={{ padding: '20px 0', textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
            Only Inventory Manager and Procurement Manager can create transfer requests.
          </div>
          <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
            <button className="btn btn-secondary" onClick={onClose}>Close</button>
          </div>
        </motion.div>
      </div>
    );
  }

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <motion.div className="modal" initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }} style={{ maxWidth: 600 }}>
        <div className="modal-header">
          <h2 className="modal-title">
            {step === 1 && 'Step 1: Select Product'}
            {step === 2 && 'Step 2: Enter Quantity'}
            {step === 3 && 'Step 3: Select Source Warehouse'}
          </h2>
          <button className="btn btn-icon btn-secondary" onClick={onClose}><X size={18} /></button>
        </div>
        {error && <div className="alert alert-error"><X size={14} />{error}</div>}

        {/* Step indicators */}
        <div style={{ display: 'flex', gap: 8, marginBottom: 20 }}>
          {[1, 2, 3].map(s => (
            <div key={s} style={{
              flex: 1, height: 4, borderRadius: 2,
              background: step >= s ? '#6366f1' : 'var(--border-subtle)',
              transition: 'background 0.3s'
            }} />
          ))}
        </div>

        {/* Step 1: Search Product */}
        {step === 1 && (
          <div>
            <div className="search-box" style={{ marginBottom: 16 }}>
              <Search size={16} />
              <input
                placeholder="Search by name or SKU..."
                value={search}
                onChange={e => setSearch(e.target.value)}
                autoFocus
              />
            </div>
            <div style={{ maxHeight: 360, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: 6 }}>
              {filteredProducts.map(p => (
                <button key={p.id} onClick={() => handleSelectProduct(p)}
                  style={{
                    display: 'flex', alignItems: 'center', gap: 12, padding: '10px 14px',
                    background: 'var(--bg-surface)', border: '1px solid var(--border-subtle)',
                    borderRadius: 10, cursor: 'pointer', textAlign: 'left', transition: 'all 0.15s',
                  }}
                  onMouseEnter={e => e.currentTarget.style.borderColor = '#6366f1'}
                  onMouseLeave={e => e.currentTarget.style.borderColor = 'var(--border-subtle)'}
                >
                  <Package size={18} color="#6366f1" style={{ flexShrink: 0 }} />
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ fontSize: '0.88rem', fontWeight: 600, color: 'var(--text-primary)' }}>{p.productName}</div>
                    <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', fontFamily: 'monospace' }}>{p.sku}</div>
                  </div>
                  <ChevronRight size={14} color="var(--text-muted)" />
                </button>
              ))}
              {filteredProducts.length === 0 && (
                <div style={{ padding: '20px 0', textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.85rem' }}>No products found</div>
              )}
            </div>
            <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: 16 }}>
              <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
            </div>
          </div>
        )}

        {/* Step 2: Enter Quantity */}
        {step === 2 && (
          <div>
            <div style={{ padding: '12px 16px', background: 'var(--bg-surface)', borderRadius: 10, border: '1px solid var(--border-subtle)', marginBottom: 16 }}>
              <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginBottom: 4 }}>SELECTED PRODUCT</div>
              <div style={{ fontSize: '0.95rem', fontWeight: 700, color: 'var(--text-primary)' }}>{selectedProduct?.productName}</div>
              <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', fontFamily: 'monospace' }}>{selectedProduct?.sku}</div>
            </div>
            <div className="form-group">
              <label className="form-label">Required Quantity *</label>
              <input className="form-control" type="number" min="1" placeholder="e.g. 50"
                value={quantity} onChange={e => setQuantity(e.target.value)} autoFocus />
            </div>
            <div className="form-group">
              <label className="form-label">Reason</label>
              <input className="form-control" placeholder="e.g. Low stock at destination" value={reason} onChange={e => setReason(e.target.value)} />
            </div>
            <div className="form-group">
              <label className="form-label">Remarks</label>
              <input className="form-control" placeholder="Additional notes..." value={remarks} onChange={e => setRemarks(e.target.value)} />
            </div>
            <div style={{ display: 'flex', gap: 12, justifyContent: 'flex-end', marginTop: 8 }}>
              <button className="btn btn-secondary" onClick={() => setStep(1)}>Back</button>
              <button className="btn btn-primary" onClick={handleCheckAvailability} disabled={!quantity || Number(quantity) <= 0 || loadingRecs}>
                {loadingRecs ? <><span className="spinner" /> Checking...</> : <><Search size={15} /> Check Availability</>}
              </button>
            </div>
          </div>
        )}

        {/* Step 3: Select Source Warehouse */}
        {step === 3 && (
          <div>
            <div style={{ padding: '12px 16px', background: 'var(--bg-surface)', borderRadius: 10, border: '1px solid var(--border-subtle)', marginBottom: 16 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <div>
                  <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginBottom: 2 }}>PRODUCT</div>
                  <div style={{ fontSize: '0.88rem', fontWeight: 700 }}>{selectedProduct?.productName}</div>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginBottom: 2 }}>QUANTITY</div>
                  <div style={{ fontSize: '1.2rem', fontWeight: 800, color: '#6366f1' }}>{quantity}</div>
                </div>
              </div>
            </div>

            <div style={{ fontSize: '0.82rem', fontWeight: 700, color: 'var(--text-secondary)', marginBottom: 10 }}>Available Warehouses</div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8, maxHeight: 300, overflowY: 'auto' }}>
              {recommendations.map(rec => {
                const wh = warehouses.find(w => w.id === rec.warehouseId);
                const isSelected = selectedSource === rec.warehouseId;
                return (
                  <div key={rec.warehouseId}
                    onClick={() => rec.recommendationReason !== 'Insufficient' && setSelectedSource(rec.warehouseId)}
                    style={{
                      padding: '12px 16px', borderRadius: 10, cursor: rec.recommendationReason === 'Insufficient' ? 'not-allowed' : 'pointer',
                      border: `2px solid ${isSelected ? '#6366f1' : rec.isRecommended ? '#10b981' : 'var(--border-subtle)'}`,
                      background: isSelected ? 'rgba(99,102,241,0.08)' : rec.isRecommended ? 'rgba(16,185,129,0.05)' : 'var(--bg-surface)',
                      opacity: rec.recommendationReason === 'Insufficient' ? 0.5 : 1,
                      transition: 'all 0.15s',
                    }}
                  >
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                        {rec.isRecommended && <Star size={16} fill="#10b981" color="#10b981" />}
                        <div>
                          <div style={{ fontSize: '0.88rem', fontWeight: 700, color: 'var(--text-primary)' }}>
                            {wh?.warehouseName || `Warehouse #${rec.warehouseId}`}
                          </div>
                          <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', fontFamily: 'monospace' }}>
                            {rec.warehouseCode || `#${rec.warehouseId}`}
                          </div>
                        </div>
                      </div>
                      <div style={{ textAlign: 'right' }}>
                        <div style={{ fontSize: '0.82rem', fontWeight: 700 }}>
                          <span style={{ color: 'var(--text-primary)' }}>{rec.freeStock}</span>
                          <span style={{ color: 'var(--text-muted)', fontWeight: 400 }}> free</span>
                        </div>
                        <div style={{
                          fontSize: '0.68rem', fontWeight: 700,
                          color: rec.recommendationReason === 'Recommended' ? '#10b981' :
                                 rec.recommendationReason === 'Available' ? '#f59e0b' : '#ef4444'
                        }}>
                          {rec.isRecommended && <Star size={10} fill="currentColor" style={{ marginRight: 3 }} />}
                          {rec.recommendationReason}
                        </div>
                      </div>
                    </div>
                  </div>
                );
              })}
              {recommendations.length === 0 && (
                <div style={{ padding: '20px 0', textAlign: 'center', color: 'var(--text-muted)' }}>
                  No warehouses have this product in stock.
                </div>
              )}
            </div>
            <div style={{ display: 'flex', gap: 12, justifyContent: 'flex-end', marginTop: 16 }}>
              <button className="btn btn-secondary" onClick={() => setStep(2)}>Back</button>
              <button className="btn btn-primary" onClick={handleSubmit} disabled={!selectedSource || submitting}>
                {submitting ? <><span className="spinner" /> Submitting...</> : <><Send size={15} /> Submit for Approval</>}
              </button>
            </div>
          </div>
        )}
      </motion.div>
    </div>
  );
}

function DispatchModal({ transfer, warehouses, products, onClose, onDispatched }) {
  const [dispatchedQty, setDispatchedQty] = useState(String(transfer.quantity));
  const [remarks, setRemarks] = useState('');
  const [loading, setLoading] = useState(false);

  const wh = warehouses.find(w => w.id === transfer.sourceWarehouseId);
  const prod = products.find(p => p.id === transfer.productId);

  const handleDispatch = async () => {
    setLoading(true);
    try {
      await transferRequestApi.dispatch(transfer.id, { dispatchedQuantity: Number(dispatchedQty), remarks });
      toast.success('Stock dispatched successfully!');
      onDispatched();
    } catch (err) { toast.error(err.response?.data?.message || 'Failed to dispatch'); }
    finally { setLoading(false); }
  };

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <motion.div className="modal" initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }} style={{ maxWidth: 480 }}>
        <div className="modal-header">
          <h2 className="modal-title"><Truck size={18} style={{ marginRight: 8 }} />Dispatch Stock</h2>
          <button className="btn btn-icon btn-secondary" onClick={onClose}><X size={18} /></button>
        </div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12, marginBottom: 16 }}>
          {[
            { label: 'Request #', value: transfer.requestNumber },
            { label: 'Product', value: prod?.productName || `#${transfer.productId}` },
            { label: 'Destination', value: warehouses.find(w => w.id === transfer.destinationWarehouseId)?.warehouseName || '—' },
            { label: 'Requested Qty', value: transfer.quantity },
          ].map(({ label, value }) => (
            <div key={label} style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)', fontWeight: 700, textTransform: 'uppercase' }}>{label}</span>
              <span style={{ fontSize: '0.88rem', fontWeight: 600, color: 'var(--text-primary)' }}>{value}</span>
            </div>
          ))}
        </div>
        <div className="form-group">
          <label className="form-label">Dispatch Quantity *</label>
          <input className="form-control" type="number" min="1" max={transfer.quantity} value={dispatchedQty} onChange={e => setDispatchedQty(e.target.value)} />
        </div>
        <div className="form-group">
          <label className="form-label">Remarks</label>
          <input className="form-control" placeholder="Dispatch notes..." value={remarks} onChange={e => setRemarks(e.target.value)} />
        </div>
        <div style={{ display: 'flex', gap: 12, justifyContent: 'flex-end' }}>
          <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
          <button className="btn btn-primary" onClick={handleDispatch} disabled={loading || !dispatchedQty || Number(dispatchedQty) <= 0}>
            {loading ? <><span className="spinner" /> Dispatching...</> : <><Truck size={15} /> Confirm Dispatch</>}
          </button>
        </div>
      </motion.div>
    </div>
  );
}

function ReceiveModal({ transfer, warehouses, products, onClose, onReceived }) {
  const [receivedQty, setReceivedQty] = useState(String(transfer.dispatchedQuantity || transfer.quantity));
  const [remarks, setRemarks] = useState('');
  const [loading, setLoading] = useState(false);

  const prod = products.find(p => p.id === transfer.productId);

  const handleReceive = async () => {
    setLoading(true);
    try {
      await transferRequestApi.receive(transfer.id, { receivedQuantity: Number(receivedQty), remarks });
      toast.success('Stock received successfully!');
      onReceived();
    } catch (err) { toast.error(err.response?.data?.message || 'Failed to receive'); }
    finally { setLoading(false); }
  };

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <motion.div className="modal" initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }} style={{ maxWidth: 480 }}>
        <div className="modal-header">
          <h2 className="modal-title"><ArrowDownToLine size={18} style={{ marginRight: 8 }} />Receive Stock</h2>
          <button className="btn btn-icon btn-secondary" onClick={onClose}><X size={18} /></button>
        </div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12, marginBottom: 16 }}>
          {[
            { label: 'Request #', value: transfer.requestNumber },
            { label: 'Product', value: prod?.productName || `#${transfer.productId}` },
            { label: 'From', value: warehouses.find(w => w.id === transfer.sourceWarehouseId)?.warehouseName || '—' },
            { label: 'Dispatched Qty', value: transfer.dispatchedQuantity },
          ].map(({ label, value }) => (
            <div key={label} style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)', fontWeight: 700, textTransform: 'uppercase' }}>{label}</span>
              <span style={{ fontSize: '0.88rem', fontWeight: 600, color: 'var(--text-primary)' }}>{value}</span>
            </div>
          ))}
        </div>
        <div className="form-group">
          <label className="form-label">Received Quantity *</label>
          <input className="form-control" type="number" min="1" max={transfer.dispatchedQuantity} value={receivedQty} onChange={e => setReceivedQty(e.target.value)} />
        </div>
        <div className="form-group">
          <label className="form-label">Remarks</label>
          <input className="form-control" placeholder="Receipt notes..." value={remarks} onChange={e => setRemarks(e.target.value)} />
        </div>
        <div style={{ display: 'flex', gap: 12, justifyContent: 'flex-end' }}>
          <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
          <button className="btn btn-primary" onClick={handleReceive} disabled={loading || !receivedQty || Number(receivedQty) <= 0}>
            {loading ? <><span className="spinner" /> Receiving...</> : <><ArrowDownToLine size={15} /> Confirm Receipt</>}
          </button>
        </div>
      </motion.div>
    </div>
  );
}

function TransferDetailModal({ transfer, warehouses, products, onClose, onAction, userWarehouseId }) {
  const { hasRole } = useAuth();
  const [loading, setLoading] = useState(false);
  const [rejectReason, setRejectReason] = useState('');
  const [showReject, setShowReject] = useState(false);

  const canApprove = hasRole('SYSTEM_ADMIN') || hasRole('PROCUREMENT_MANAGER') || hasRole('INVENTORY_MANAGER');
  const isPending = transfer.status === 'PENDING';
  const isApproved = transfer.status === 'APPROVED';
  const isDispatched = transfer.status === 'DISPATCHED';

  const canDispatch = isApproved && (hasRole('SYSTEM_ADMIN') || userWarehouseId === transfer.sourceWarehouseId);
  const canReceive = isDispatched && (hasRole('SYSTEM_ADMIN') || userWarehouseId === transfer.destinationWarehouseId);

  const getWh = (id) => warehouses.find(w => w.id === id);
  const prod = products.find(p => p.id === transfer.productId);

  const handleApprove = async () => {
    setLoading(true);
    try {
      await transferRequestApi.approve(transfer.id, {});
      toast.success('Transfer approved!');
      onAction();
    } catch (err) { toast.error(err.response?.data?.message || 'Failed to approve'); }
    finally { setLoading(false); }
  };

  const handleReject = async () => {
    setLoading(true);
    try {
      await transferRequestApi.reject(transfer.id, { rejectionReason: rejectReason });
      toast.success('Transfer rejected.');
      onAction();
    } catch (err) { toast.error(err.response?.data?.message || 'Failed to reject'); }
    finally { setLoading(false); }
  };

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <motion.div className="modal" initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }} style={{ maxWidth: 520 }}>
        <div className="modal-header">
          <h2 className="modal-title">{transfer.requestNumber}</h2>
          <button className="btn btn-icon btn-secondary" onClick={onClose}><X size={18} /></button>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 12, marginBottom: 20 }}>
          {[
            { label: 'Status', value: <span style={{ padding: '4px 12px', borderRadius: 20, background: `${STATUS_COLORS[transfer.status] || '#6b7280'}15`, border: `1px solid ${STATUS_COLORS[transfer.status] || '#6b7280'}30`, fontSize: '0.78rem', fontWeight: 700, color: STATUS_COLORS[transfer.status] }}>{transfer.status}</span> },
            { label: 'Product', value: prod?.productName || `#${transfer.productId}` },
            { label: 'From', value: getWh(transfer.sourceWarehouseId)?.warehouseName || `#${transfer.sourceWarehouseId}` },
            { label: 'To', value: getWh(transfer.destinationWarehouseId)?.warehouseName || `#${transfer.destinationWarehouseId}` },
            { label: 'Requested Qty', value: transfer.quantity },
            { label: 'Requested By', value: transfer.requestedBy },
            { label: 'Reason', value: transfer.reason || '—' },
          ].map(({ label, value }) => (
            <div key={label} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.05em' }}>{label}</span>
              <span style={{ fontSize: '0.88rem', color: 'var(--text-primary)' }}>{value}</span>
            </div>
          ))}
          {transfer.approvedBy && (
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)', fontWeight: 700, textTransform: 'uppercase' }}>Approved By</span>
              <span style={{ fontSize: '0.88rem', color: 'var(--text-primary)' }}>{transfer.approvedBy}</span>
            </div>
          )}
          {transfer.rejectionReason && (
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)', fontWeight: 700 }}>Rejection Reason</span>
              <span style={{ fontSize: '0.88rem', color: '#ef4444' }}>{transfer.rejectionReason}</span>
            </div>
          )}
          {transfer.dispatchedQuantity && (
            <>
              <div style={{ borderTop: '1px solid var(--border-subtle)', paddingTop: 12, marginTop: 4 }}>
                <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', fontWeight: 700, marginBottom: 8, textTransform: 'uppercase' }}>Dispatch Details</div>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>Dispatched Qty</span>
                <span style={{ fontWeight: 700 }}>{transfer.dispatchedQuantity}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>Dispatched By</span>
                <span>{transfer.dispatchedBy}</span>
              </div>
            </>
          )}
          {transfer.receivedQuantity && (
            <>
              <div style={{ borderTop: '1px solid var(--border-subtle)', paddingTop: 12, marginTop: 4 }}>
                <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', fontWeight: 700, marginBottom: 8, textTransform: 'uppercase' }}>Receipt Details</div>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>Received Qty</span>
                <span style={{ fontWeight: 700 }}>{transfer.receivedQuantity}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>Received By</span>
                <span>{transfer.receivedBy}</span>
              </div>
            </>
          )}
        </div>

        {/* Approve/Reject buttons for PENDING */}
        {isPending && canApprove && (
          <div style={{ borderTop: '1px solid var(--border-subtle)', paddingTop: 16 }}>
            {showReject ? (
              <div>
                <div className="form-group">
                  <label className="form-label">Rejection Reason *</label>
                  <input className="form-control" placeholder="Why is this being rejected?" value={rejectReason} onChange={e => setRejectReason(e.target.value)} />
                </div>
                <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
                  <button className="btn btn-secondary btn-sm" onClick={() => setShowReject(false)}>Cancel</button>
                  <button className="btn btn-danger btn-sm" onClick={handleReject} disabled={loading || !rejectReason}>
                    {loading ? <span className="spinner" /> : <><Ban size={14} /> Reject</>}
                  </button>
                </div>
              </div>
            ) : (
              <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
                <button className="btn btn-danger btn-sm" onClick={() => setShowReject(true)}>
                  <Ban size={14} /> Reject
                </button>
                <button className="btn btn-success btn-sm" onClick={handleApprove} disabled={loading}>
                  {loading ? <span className="spinner" /> : <><Check size={14} /> Approve</>}
                </button>
              </div>
            )}
          </div>
        )}
      </motion.div>
    </div>
  );
}

export default function TransfersPage() {
  const { user, hasRole } = useAuth();
  const [transfers, setTransfers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreate, setShowCreate] = useState(false);
  const [selected, setSelected] = useState(null);
  const [dispatchModal, setDispatchModal] = useState(null);
  const [receiveModal, setReceiveModal] = useState(null);
  const [products, setProducts] = useState([]);
  const [warehouses, setWarehouses] = useState([]);
  const [userWarehouseId, setUserWarehouseId] = useState(null);
  const [tab, setTab] = useState('all');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [stats, setStats] = useState({ pendingCount: 0 });

  const canApprove = hasRole('SYSTEM_ADMIN') || hasRole('PROCUREMENT_MANAGER') || hasRole('INVENTORY_MANAGER');
  const canDispatch = hasRole('SYSTEM_ADMIN') || hasRole('INVENTORY_MANAGER') || hasRole('WAREHOUSE_OPERATOR');
  const canReceive = hasRole('SYSTEM_ADMIN') || hasRole('INVENTORY_MANAGER') || hasRole('WAREHOUSE_OPERATOR');

  useEffect(() => {
    if (hasRole('INVENTORY_MANAGER') || hasRole('WAREHOUSE_OPERATOR')) {
      fetch('/api/v1/warehouses/my-assignment', {
        headers: { 'Authorization': `Bearer ${localStorage.getItem('authToken')}` }
      }).then(r => r.json()).then(d => {
        if (d.data?.warehouseId) setUserWarehouseId(d.data.warehouseId);
      }).catch(() => {});
    }
  }, []);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const params = { page, size: 15 };
      let res;
      if (tab === 'pending' && canApprove) {
        res = await transferRequestApi.getPending(params);
      } else if (tab === 'my') {
        res = await transferRequestApi.getMy(params);
      } else {
        res = await transferRequestApi.getAll(params);
      }
      const d = res.data?.data;
      setTransfers(d?.content || d || []);
      setTotalPages(d?.totalPages || 1);

      const [pRes, wRes] = await Promise.all([
        productApi.getAll({ size: 200 }),
        warehouseApi.getAll({ size: 100 }),
      ]);
      setProducts(pRes.data?.data?.content || pRes.data?.data || []);
      setWarehouses(wRes.data?.data?.content || wRes.data?.data || []);

      if (canApprove) {
        const sRes = await transferRequestApi.getStats();
        setStats(sRes.data?.data || { pendingCount: 0 });
      }
    } catch { toast.error('Failed to load transfer requests'); }
    finally { setLoading(false); }
  }, [page, tab, canApprove]);

  useEffect(() => { load(); }, [load]);

  const getWh = (id) => warehouses.find(w => w.id === id);
  const getProd = (id) => products.find(p => p.id === id);

  const getBadgeStyle = (status) => {
    const color = STATUS_COLORS[status] || '#6b7280';
    const icon = status === 'PENDING' ? <Clock size={11} /> :
                 status === 'APPROVED' ? <Check size={11} /> :
                 status === 'DISPATCHED' ? <Truck size={11} /> :
                 status === 'REJECTED' ? <Ban size={11} /> :
                 status === 'COMPLETED' ? <Check size={11} /> : null;
    return { icon, style: { display: 'inline-flex', alignItems: 'center', gap: 4, padding: '4px 10px', borderRadius: 20, background: `${color}15`, border: `1px solid ${color}30`, fontSize: '0.72rem', fontWeight: 700, color } };
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Transfer Requests</h1>
          <p className="page-subtitle">Inter-warehouse stock transfer approval workflow</p>
        </div>
        <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
          {canApprove && stats.pendingCount > 0 && (
            <span style={{ padding: '6px 14px', borderRadius: 20, background: 'rgba(245,158,11,0.15)', color: '#f59e0b', fontSize: '0.78rem', fontWeight: 700 }}>
              {stats.pendingCount} pending
            </span>
          )}
          {(hasRole('INVENTORY_MANAGER') || hasRole('PROCUREMENT_MANAGER')) && (
            <button className="btn btn-primary" onClick={() => setShowCreate(true)}>
              <Plus size={18} /> New Transfer
            </button>
          )}
        </div>
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', gap: 4, marginBottom: 20, borderBottom: '1px solid var(--border-subtle)', paddingBottom: 0, overflowX: 'auto' }}>
        {[
          { key: 'all', label: 'All' },
          ...(canApprove ? [{ key: 'pending', label: `Pending${stats.pendingCount > 0 ? ` (${stats.pendingCount})` : ''}` }] : []),
          ...(canDispatch ? [{ key: 'dispatch', label: 'To Dispatch' }] : []),
          ...(canReceive ? [{ key: 'receipt', label: 'To Receive' }] : []),
          { key: 'my', label: 'My Requests' },
        ].map(t => (
          <button key={t.key} onClick={() => { setTab(t.key); setPage(0); }}
            style={{
              padding: '10px 18px', border: 'none', cursor: 'pointer', fontSize: '0.82rem', fontWeight: 700, whiteSpace: 'nowrap',
              background: 'transparent', color: tab === t.key ? '#6366f1' : 'var(--text-muted)',
              borderBottom: tab === t.key ? '2px solid #6366f1' : '2px solid transparent',
              marginBottom: -1, transition: 'all 0.15s',
            }}
          >{t.label}</button>
        ))}
      </div>

      {/* Table */}
      <div className="table-wrapper">
        {loading ? (
          <div className="loading-state"><div className="spinner" /><span>Loading transfers...</span></div>
        ) : transfers.length === 0 ? (
          <div className="empty-state">
            <ArrowLeftRight size={48} color="var(--text-muted)" />
            <h3>No transfer requests</h3>
            <p>Create a new transfer request to get started.</p>
          </div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Request #</th>
                <th>Product</th>
                <th>From</th>
                <th>To</th>
                <th>Qty</th>
                <th>Status</th>
                <th>Requested By</th>
                <th>Date</th>
                <th style={{ textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {transfers.map(t => {
                const prod = getProd(t.productId);
                const badge = getBadgeStyle(t.status);
                const isSource = userWarehouseId === t.sourceWarehouseId;
                const isDest = userWarehouseId === t.destinationWarehouseId;
                return (
                  <tr key={t.id} style={{ cursor: 'pointer' }}>
                    <td onClick={() => setSelected(t)}>
                      <span style={{ fontFamily: 'monospace', fontSize: '0.82rem', color: '#818cf8', fontWeight: 700 }}>
                        {t.requestNumber}
                      </span>
                    </td>
                    <td onClick={() => setSelected(t)}>
                      <div style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{prod?.productName || `#${t.productId}`}</div>
                      <div style={{ fontSize: '0.73rem', color: 'var(--text-muted)' }}>{prod?.sku}</div>
                    </td>
                    <td onClick={() => setSelected(t)} style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>{getWh(t.sourceWarehouseId)?.warehouseName || '—'}</td>
                    <td onClick={() => setSelected(t)} style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>{getWh(t.destinationWarehouseId)?.warehouseName || '—'}</td>
                    <td onClick={() => setSelected(t)}><span style={{ fontWeight: 800, fontSize: '1rem', color: '#6366f1' }}>{t.quantity}</span></td>
                    <td onClick={() => setSelected(t)}>
                      <span style={badge.style}>{badge.icon} {t.status}</span>
                    </td>
                    <td onClick={() => setSelected(t)} style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>{t.requestedBy}</td>
                    <td onClick={() => setSelected(t)} style={{ color: 'var(--text-muted)', fontSize: '0.82rem' }}>
                      {t.createdAt ? new Date(t.createdAt).toLocaleDateString('en-IN') : '—'}
                    </td>
                    <td onClick={e => e.stopPropagation()}>
                      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 6 }}>
                        {t.status === 'APPROVED' && (hasRole('SYSTEM_ADMIN') || isSource) && (
                          <button className="btn btn-primary btn-sm" onClick={() => setDispatchModal(t)}>
                            <Truck size={13} /> Dispatch
                          </button>
                        )}
                        {t.status === 'DISPATCHED' && (hasRole('SYSTEM_ADMIN') || isDest) && (
                          <button className="btn btn-primary btn-sm" onClick={() => setReceiveModal(t)}>
                            <ArrowDownToLine size={13} /> Receive
                          </button>
                        )}
                      </div>
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
        {showCreate && (
          <CreateTransferWizard
            products={products}
            warehouses={warehouses}
            userWarehouseId={userWarehouseId}
            onClose={() => setShowCreate(false)}
            onSaved={() => { setShowCreate(false); load(); }}
          />
        )}
        {selected && (
          <TransferDetailModal
            transfer={selected}
            warehouses={warehouses}
            products={products}
            userWarehouseId={userWarehouseId}
            onClose={() => setSelected(null)}
            onAction={() => { setSelected(null); load(); }}
          />
        )}
        {dispatchModal && (
          <DispatchModal
            transfer={dispatchModal}
            warehouses={warehouses}
            products={products}
            onClose={() => setDispatchModal(null)}
            onDispatched={() => { setDispatchModal(null); load(); }}
          />
        )}
        {receiveModal && (
          <ReceiveModal
            transfer={receiveModal}
            warehouses={warehouses}
            products={products}
            onClose={() => setReceiveModal(null)}
            onReceived={() => { setReceiveModal(null); load(); }}
          />
        )}
      </AnimatePresence>
    </div>
  );
}
