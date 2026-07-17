import { useState, useEffect, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { productApi, categoryApi, warehouseApi, inventoryApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';
import { Plus, Search, X, Tag, ShoppingBag, Edit3, ToggleLeft, ToggleRight, DollarSign, FolderOpen, Eye, EyeOff } from 'lucide-react';

function CategoryModal({ category, myWarehouseId, isAdmin, onClose, onSaved }) {
  const isEdit = !!category;
  const [form, setForm] = useState(isEdit ? { ...category } : { name: '', description: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const set = (f, v) => setForm(p => ({ ...p, [f]: v }));

  const handleSubmit = async (e) => {
    e.preventDefault(); setLoading(true); setError('');
    try {
      const payload = { ...form };
      if (!isEdit && !isAdmin && myWarehouseId) payload.warehouseId = myWarehouseId;
      if (isEdit) await categoryApi.update(category.id, payload);
      else await categoryApi.create(payload);
      toast.success(isEdit ? 'Category updated!' : 'Category created!');
      onSaved();
    } catch (err) { setError(err.response?.data?.message || 'Failed'); }
    finally { setLoading(false); }
  };

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <motion.div className="modal" style={{ maxWidth: 420 }} initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }} exit={{ opacity: 0, scale: 0.95 }}>
        <div className="modal-header">
          <h2 className="modal-title">{isEdit ? 'Edit Category' : 'Add Category'}</h2>
          <button className="btn btn-icon btn-secondary" onClick={onClose}><X size={18} /></button>
        </div>
        {error && <div className="alert alert-error"><X size={14} />{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Category Name *</label>
            <input className="form-control" placeholder="e.g. Electronics, Raw Materials" value={form.name} onChange={e => set('name', e.target.value)} required autoFocus />
          </div>
          <div className="form-group">
            <label className="form-label">Description</label>
            <textarea className="form-control" rows={3} placeholder="Optional description for this category..." value={form.description} onChange={e => set('description', e.target.value)} style={{ resize: 'vertical' }} />
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

function ProductModal({ product, categories, warehouses, myWarehouseId, isAdmin, onClose, onSaved }) {
  const isEdit = !!product;
  const [form, setForm] = useState(isEdit ? { ...product, categoryId: product.categoryId || '', warehouseId: '' } : { sku: '', productName: '', brand: '', description: '', categoryId: '', unit: '', price: '', reorderLevel: '', maxStockLevel: '', warehouseId: myWarehouseId || '', initialStock: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [whSearch, setWhSearch] = useState('');
  const [whDropdownOpen, setWhDropdownOpen] = useState(false);
  const set = (f, v) => setForm(p => ({ ...p, [f]: v }));

  const filteredWarehouses = warehouses.filter(w =>
    w.status === 'ACTIVE' && (
      !whSearch ||
      w.warehouseName?.toLowerCase().includes(whSearch.toLowerCase()) ||
      w.warehouseCode?.toLowerCase().includes(whSearch.toLowerCase())
    )
  );

  const selectedWh = warehouses.find(w => w.id === Number(form.warehouseId));

  const handleSubmit = async (e) => {
    e.preventDefault(); setLoading(true); setError('');
    try {
      const payload = { ...form, price: Number(form.price), categoryId: Number(form.categoryId), reorderLevel: Number(form.reorderLevel) || 0, maxStockLevel: Number(form.maxStockLevel) || 0 };
      let productResult;
      if (isEdit) {
        productResult = await productApi.update(product.id, payload);
        toast.success('Product updated!');
      } else {
        productResult = await productApi.create(payload);
        toast.success('Product created!');
        if (form.warehouseId) {
          try {
            await inventoryApi.create({ productId: productResult.data?.data?.id, warehouseId: Number(form.warehouseId), initialQuantity: Number(form.initialStock) || 0 });
            toast.success('Product mapped to warehouse!');
          } catch (invErr) {
            toast.error(invErr.response?.data?.message || 'Product created but warehouse mapping failed');
          }
        }
      }
      onSaved();
    } catch (err) { setError(err.response?.data?.message || 'Failed'); }
    finally { setLoading(false); }
  };

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <motion.div className="modal" initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }} exit={{ opacity: 0, scale: 0.95 }}>
        <div className="modal-header">
          <h2 className="modal-title">{isEdit ? 'Edit Product' : 'Add Product'}</h2>
          <button className="btn btn-icon btn-secondary" onClick={onClose}><X size={18} /></button>
        </div>
        {error && <div className="alert alert-error"><X size={14} />{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="grid grid-2 gap-4">
            <div className="form-group">
              <label className="form-label">SKU *</label>
              <input className="form-control" placeholder="SKU-001" value={form.sku} onChange={e => set('sku', e.target.value)} required disabled={isEdit} />
            </div>
            <div className="form-group">
              <label className="form-label">Product Name *</label>
              <input className="form-control" placeholder="Widget Pro" value={form.productName} onChange={e => set('productName', e.target.value)} required />
            </div>
            <div className="form-group">
              <label className="form-label">Brand</label>
              <input className="form-control" placeholder="Acme Corp" value={form.brand} onChange={e => set('brand', e.target.value)} />
            </div>
            <div className="form-group">
              <label className="form-label">Category *</label>
              <select className="form-control" value={form.categoryId} onChange={e => set('categoryId', e.target.value)} required>
                <option value="">Select category...</option>
                {categories.map(c => <option key={c.id} value={c.id}>{c.name || c.categoryName}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Unit *</label>
              <select className="form-control" value={form.unit} onChange={e => set('unit', e.target.value)} required>
                <option value="">Select unit...</option>
                {['PIECE', 'KG', 'LITER', 'METER', 'BOX', 'PALLET', 'CARTON'].map(u => <option key={u} value={u}>{u}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Price *</label>
              <input className="form-control" type="number" step="0.01" placeholder="0.00" value={form.price} onChange={e => set('price', e.target.value)} required />
            </div>
            <div className="form-group">
              <label className="form-label">Reorder Level</label>
              <input className="form-control" type="number" placeholder="10" value={form.reorderLevel} onChange={e => set('reorderLevel', e.target.value)} />
            </div>
            <div className="form-group">
              <label className="form-label">Max Stock Level</label>
              <input className="form-control" type="number" placeholder="500" value={form.maxStockLevel} onChange={e => set('maxStockLevel', e.target.value)} />
            </div>
            {!isEdit && (
              <div className="form-group" style={{ gridColumn: '1/-1' }}>
                <label className="form-label">{isAdmin ? 'Map to Warehouse *' : 'Your Warehouse'}</label>
                {isAdmin ? (
                  <div style={{ position: 'relative' }}>
                    <input
                      className="form-control"
                      placeholder="Search warehouse..."
                      value={whSearch || (selectedWh ? `${selectedWh.warehouseCode} - ${selectedWh.warehouseName}` : '')}
                      onChange={e => { setWhSearch(e.target.value); setWhDropdownOpen(true); set('warehouseId', ''); }}
                      onFocus={() => setWhDropdownOpen(true)}
                      required={!myWarehouseId}
                    />
                    {whDropdownOpen && filteredWarehouses.length > 0 && (
                      <div style={{ position: 'absolute', top: '100%', left: 0, right: 0, background: 'var(--bg-card)', border: '1px solid var(--border-subtle)', borderRadius: 8, maxHeight: 180, overflowY: 'auto', zIndex: 100, marginTop: 4 }}>
                        {filteredWarehouses.map(w => (
                          <div
                            key={w.id}
                            onClick={() => { set('warehouseId', w.id); setWhSearch(''); setWhDropdownOpen(false); }}
                            style={{ padding: '8px 12px', cursor: 'pointer', fontSize: '0.82rem', borderBottom: '1px solid var(--border-subtle)' }}
                            onMouseEnter={e => e.currentTarget.style.background = 'rgba(99,102,241,0.08)'}
                            onMouseLeave={e => e.currentTarget.style.background = 'transparent'}
                          >
                            <span style={{ fontFamily: 'monospace', fontWeight: 700, color: '#818cf8' }}>{w.warehouseCode}</span>
                            <span style={{ marginLeft: 8, color: 'var(--text-primary)' }}>{w.warehouseName}</span>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                ) : (
                  <input className="form-control" value={selectedWh ? `${selectedWh.warehouseCode} - ${selectedWh.warehouseName}` : 'Loading...'} disabled />
                )}
              </div>
            )}
            {!isEdit && form.warehouseId && (
              <div className="form-group" style={{ gridColumn: '1/-1' }}>
                <label className="form-label">Initial Stock Quantity</label>
                <input className="form-control" type="number" min="0" placeholder="0" value={form.initialStock} onChange={e => set('initialStock', e.target.value)} />
                <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)', marginTop: 4, display: 'block' }}>Starting stock for this product in the selected warehouse</span>
              </div>
            )}
            <div className="form-group" style={{ gridColumn: '1/-1' }}>
              <label className="form-label">Description</label>
              <textarea className="form-control" rows={2} placeholder="Product description..." value={form.description} onChange={e => set('description', e.target.value)} style={{ resize: 'vertical' }} />
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

export default function ProductsPage() {
  const { isManager, isAdmin } = useAuth();
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [warehouses, setWarehouses] = useState([]);
  const [inventories, setInventories] = useState([]);
  const [myWarehouseId, setMyWarehouseId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [modal, setModal] = useState(null);
  const [categoryModal, setCategoryModal] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [activeTab, setActiveTab] = useState('products');
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
      const [pRes, cRes, wRes, iRes] = await Promise.all([
        productApi.getAll({ page, size: 100 }),
        categoryApi.getAll({ size: 100 }),
        warehouseApi.getAll({ size: 100 }),
        inventoryApi.getAll({ size: 500 }),
      ]);
      const pd = pRes.data?.data; setProducts(pd?.content || pd || []); setTotalPages(pd?.totalPages || 1);
      const cd = cRes.data?.data; setCategories(cd?.content || cd || []);
      const wd = wRes.data?.data; setWarehouses(wd?.content || wd || []);
      const id = iRes.data?.data; setInventories(id?.content || id || []);
    } catch { toast.error('Failed to load'); }
    finally { setLoading(false); }
  }, [page]);

  useEffect(() => { load(); }, [load]);

  const toggleProduct = async (p) => {
    try {
      if (p.status === 'ACTIVE') { await productApi.deactivate(p.id); toast.success('Deactivated'); }
      else { await productApi.activate(p.id); toast.success('Activated'); }
      load();
    } catch (err) { toast.error(err.response?.data?.message || 'Failed'); }
  };

  const getProdWarehouses = (productId) => {
    const invs = inventories.filter(i => i.productId === productId && i.status === 'ACTIVE');
    return invs.map(i => {
      const wh = warehouses.find(w => w.id === i.warehouseId);
      return wh ? wh.warehouseName : `WH#${i.warehouseId}`;
    });
  };

  const getProdWarehouseIds = (productId) => {
    return inventories.filter(i => i.productId === productId && i.status === 'ACTIVE').map(i => i.warehouseId);
  };

  const filtered = products.filter(p => {
    if (showMyWarehouseOnly && myWarehouseId) {
      const whIds = getProdWarehouseIds(p.id);
      if (!whIds.includes(myWarehouseId)) return false;
    }
    if (!search) return true;
    return p.productName?.toLowerCase().includes(search.toLowerCase()) ||
      p.sku?.toLowerCase().includes(search.toLowerCase()) ||
      p.brand?.toLowerCase().includes(search.toLowerCase());
  });

  const getCatName = (id) => categories.find(c => c.id === id)?.name || categories.find(c => c.id === id)?.categoryName || '—';

  const filteredCategories = categories.filter(c => {
    if (isAdmin()) return true;
    return !c.warehouseId || c.warehouseId === myWarehouseId;
  });

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Products</h1>
          <p className="page-subtitle">Product catalogue and category management</p>
        </div>
        {isManager() && (
          <button className="btn btn-primary" onClick={() => setModal('create')}><Plus size={18} /> Add Product</button>
        )}
      </div>

      <div className="tabs">
        <button className={`tab ${activeTab === 'products' ? 'active' : ''}`} onClick={() => setActiveTab('products')}>Products ({filtered.length})</button>
        <button className={`tab ${activeTab === 'categories' ? 'active' : ''}`} onClick={() => setActiveTab('categories')}>Categories ({filteredCategories.length})</button>
      </div>

      {activeTab === 'products' && (
        <>
          <div style={{ marginBottom: 16, display: 'flex', alignItems: 'center', gap: 12, flexWrap: 'wrap' }}>
            <div className="search-box" style={{ maxWidth: 380, flex: 1 }}>
              <Search size={16} />
              <input placeholder="Search by name, SKU or brand..." value={search} onChange={e => setSearch(e.target.value)} />
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

          <div className="table-wrapper">
            {loading ? (
              <div className="loading-state"><div className="spinner" /><span>Loading products...</span></div>
            ) : filtered.length === 0 ? (
              <div className="empty-state">
                <ShoppingBag size={48} color="var(--text-muted)" />
                <h3>No products found</h3>
                <p>{search ? 'Try a different search term.' : 'Add your first product.'}</p>
              </div>
            ) : (
              <table className="data-table">
                <thead>
                  <tr><th>SKU</th><th>Name</th><th>Brand</th><th>Warehouse</th><th>Category</th><th>Unit</th><th>Price</th><th>Status</th>{isManager() && <th style={{ textAlign: 'right' }}>Actions</th>}</tr>
                </thead>
                <tbody>
                  {filtered.map(p => (
                    <tr key={p.id}>
                      <td><span style={{ fontFamily: 'monospace', fontSize: '0.82rem', color: '#22d3ee', fontWeight: 700 }}>{p.sku}</span></td>
                      <td><div style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{p.productName}</div></td>
                      <td>{p.brand || '—'}</td>
                      <td style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>{getProdWarehouses(p.id).join(', ') || '—'}</td>
                      <td>{getCatName(p.categoryId)}</td>
                      <td><span className="badge badge-info">{p.unit}</span></td>
                      <td style={{ fontWeight: 700, color: 'var(--success)' }}>{Number(p.price || 0).toLocaleString('en-IN', { style: 'currency', currency: 'INR' })}</td>
                      <td><span className={`badge badge-${p.status === 'ACTIVE' ? 'active' : 'inactive'}`}>{p.status}</span></td>
                      {isManager() && (
                        <td>
                          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
                            <button className="btn btn-secondary btn-sm" onClick={() => setModal(p)} title="Edit"><Edit3 size={13} /></button>
                            <button className={`btn btn-sm ${p.status === 'ACTIVE' ? 'btn-danger' : 'btn-success'}`} onClick={() => toggleProduct(p)} title={p.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}>
                              {p.status === 'ACTIVE' ? <ToggleRight size={14} /> : <ToggleLeft size={14} />}
                            </button>
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
                <button className="page-btn" disabled={page === 0} onClick={() => setPage(p => p - 1)}>&larr;</button>
                {Array.from({ length: totalPages }, (_, i) => <button key={i} className={`page-btn ${i === page ? 'active' : ''}`} onClick={() => setPage(i)}>{i + 1}</button>)}
                <button className="page-btn" disabled={page === totalPages - 1} onClick={() => setPage(p => p + 1)}>&rarr;</button>
              </div>
            )}
          </div>
        </>
      )}

      {activeTab === 'categories' && (
        <>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16, flexWrap: 'wrap', gap: 10 }}>
            <p style={{ fontSize: '0.84rem', color: 'var(--text-muted)' }}>Manage product categories</p>
            {isManager() && (
              <button className="btn btn-primary btn-sm" onClick={() => setCategoryModal('create')}><Plus size={15} /> Add Category</button>
            )}
          </div>

          <div className="table-wrapper">
            {loading ? (
              <div className="loading-state"><div className="spinner" /><span>Loading categories...</span></div>
            ) : filteredCategories.length === 0 ? (
              <div className="empty-state">
                <FolderOpen size={48} color="var(--text-muted)" />
                <h3>No categories yet</h3>
                <p>Create your first category to organize products.</p>
                {isManager() && (
                  <button className="btn btn-primary btn-sm" style={{ marginTop: 8 }} onClick={() => setCategoryModal('create')}><Plus size={15} /> Add Category</button>
                )}
              </div>
            ) : (
              <table className="data-table">
                <thead><tr><th>ID</th><th>Name</th><th>Description</th><th>Scope</th>{isManager() && <th style={{ textAlign: 'right' }}>Actions</th>}</tr></thead>
                <tbody>
                  {filteredCategories.map(c => (
                    <tr key={c.id}>
                      <td style={{ color: 'var(--text-muted)', fontSize: '0.8rem' }}>#{c.id}</td>
                      <td style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{c.name || c.categoryName}</td>
                      <td style={{ color: 'var(--text-muted)', maxWidth: 300, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{c.description || '—'}</td>
                      <td>
                        <span className={`badge ${c.warehouseId ? 'badge-info' : 'badge-active'}`} style={{ fontSize: '0.7rem' }}>
                          {c.warehouseId ? (warehouses.find(w => w.id === c.warehouseId)?.warehouseName || 'Warehouse') : 'Global'}
                        </span>
                      </td>
                      {isManager() && (
                        <td>
                          <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                            <button className="btn btn-secondary btn-sm" onClick={() => setCategoryModal(c)} title="Edit"><Edit3 size={13} /></button>
                          </div>
                        </td>
                      )}
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </>
      )}

      <AnimatePresence>
        {modal && (
          <ProductModal
            product={modal === 'create' ? null : modal}
            categories={categories}
            warehouses={warehouses}
            myWarehouseId={myWarehouseId}
            isAdmin={isAdmin()}
            onClose={() => setModal(null)}
            onSaved={() => { setModal(null); load(); }}
          />
        )}
        {categoryModal && (
          <CategoryModal
            category={categoryModal === 'create' ? null : categoryModal}
            myWarehouseId={myWarehouseId}
            isAdmin={isAdmin()}
            onClose={() => setCategoryModal(null)}
            onSaved={() => { setCategoryModal(null); load(); }}
          />
        )}
      </AnimatePresence>
    </div>
  );
}
