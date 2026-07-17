import { useState, useEffect, useCallback } from 'react';
import { motion } from 'framer-motion';
import { Navigate } from 'react-router-dom';
import { movementApi, inventoryApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend
} from 'recharts';
import { BarChart3, TrendingUp, Package, AlertTriangle } from 'lucide-react';

const COLORS = ['#6366f1', '#10b981', '#f59e0b', '#ef4444', '#06b6d4', '#8b5cf6'];

const CustomTooltip = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null;
  return (
    <div style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: 10, padding: '10px 14px' }}>
      {label && <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginBottom: 6 }}>{label}</div>}
      {payload.map((p, i) => <div key={i} style={{ fontSize: '0.85rem', fontWeight: 600, color: p.color || p.fill }}>{p.name}: {p.value}</div>)}
    </div>
  );
};

export default function ReportsPage() {
  const { canViewReports } = useAuth();
  const [movements, setMovements] = useState([]);
  const [inventory, setInventory] = useState([]);
  const [loading, setLoading] = useState(true);

  if (!canViewReports()) return <Navigate to="/" replace />;

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [mRes, iRes] = await Promise.all([
        movementApi.getAll({ size: 500 }),
        inventoryApi.getAll({ size: 500 }),
      ]);
      const md = mRes.data?.data; setMovements(md?.content || md || []);
      const id = iRes.data?.data; setInventory(id?.content || id || []);
    } catch (e) { console.error(e); }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, [load]);

  // Movement by type
  const byType = {};
  movements.forEach(m => { byType[m.movementType] = (byType[m.movementType] || 0) + 1; });
  const typeData = Object.entries(byType).map(([name, value]) => ({ name: name.replace('_', ' '), value }));

  // Movement trend (last 30 days by week)
  const weeks = [];
  for (let i = 3; i >= 0; i--) {
    const start = new Date(); start.setDate(start.getDate() - (i + 1) * 7);
    const end = new Date(); end.setDate(end.getDate() - i * 7);
    const label = `Week ${4 - i}`;
    const wMovs = movements.filter(m => { const d = new Date(m.createdAt || m.movementDate || 0); return d >= start && d < end; });
    weeks.push({ label, receipt: wMovs.filter(m => m.movementType === 'GOODS_RECEIPT').length, issue: wMovs.filter(m => m.movementType === 'GOODS_ISSUE').length, transfer: wMovs.filter(m => m.movementType?.includes('TRANSFER')).length });
  }

  // Stock health
  const outOfStock = inventory.filter(i => i.currentQuantity === 0).length;
  const lowStock = inventory.filter(i => i.currentQuantity > 0 && i.currentQuantity < 10).length;
  const healthy = inventory.filter(i => i.currentQuantity >= 10).length;
  const healthData = [
    { name: 'Healthy', value: healthy },
    { name: 'Low Stock', value: lowStock },
    { name: 'Out of Stock', value: outOfStock },
  ].filter(d => d.value > 0);

  if (loading) return <div className="loading-state"><div className="spinner spinner-lg" /><span>Loading reports...</span></div>;

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Reports & Analytics</h1>
          <p className="page-subtitle">Visual insights into inventory performance</p>
        </div>
      </div>

      {/* Top stats */}
      <div className="grid grid-4 gap-4 mb-6">
        {[
          { label: 'Total Movements', value: movements.length, color: '#6366f1', icon: TrendingUp },
          { label: 'Inventory Items', value: inventory.length, color: '#06b6d4', icon: Package },
          { label: 'Low Stock Items', value: lowStock, color: '#f59e0b', icon: AlertTriangle },
          { label: 'Out of Stock', value: outOfStock, color: '#ef4444', icon: AlertTriangle },
        ].map((s, i) => (
          <motion.div key={s.label} className="stat-card" initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.05 }}>
            <div className="stat-icon" style={{ background: `${s.color}20` }}><s.icon size={22} color={s.color} /></div>
            <div>
              <div className="stat-label">{s.label}</div>
              <div className="stat-value">{s.value}</div>
            </div>
            <div style={{ position: 'absolute', top: 0, right: 0, width: 100, height: 100, borderRadius: '50%', background: s.color, opacity: 0.06, transform: 'translate(30%,-30%)' }} />
          </motion.div>
        ))}
      </div>

      {/* Charts */}
      <div className="grid gap-4 mb-6" style={{ gridTemplateColumns: '1fr 1fr' }}>
        {/* Weekly movement */}
        <motion.div className="card" initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.2 }}>
          <h3 style={{ marginBottom: 4 }}>Weekly Movement Volume</h3>
          <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: 20 }}>Last 4 weeks by type</p>
          <ResponsiveContainer width="100%" height={240}>
            <BarChart data={weeks} barSize={20}>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.04)" />
              <XAxis dataKey="label" tick={{ fill: 'var(--text-muted)', fontSize: 11 }} axisLine={false} tickLine={false} />
              <YAxis allowDecimals={false} tick={{ fill: 'var(--text-muted)', fontSize: 11 }} axisLine={false} tickLine={false} />
              <Tooltip content={<CustomTooltip />} />
              <Bar dataKey="receipt" name="Receipts" fill="#6366f1" radius={[4, 4, 0, 0]} />
              <Bar dataKey="issue" name="Issues" fill="#ef4444" radius={[4, 4, 0, 0]} />
              <Bar dataKey="transfer" name="Transfers" fill="#f59e0b" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </motion.div>

        {/* Stock Health */}
        <motion.div className="card" initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.25 }}>
          <h3 style={{ marginBottom: 4 }}>Stock Health Distribution</h3>
          <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: 20 }}>By inventory status</p>
          {healthData.length > 0 ? (
            <ResponsiveContainer width="100%" height={240}>
              <PieChart>
                <Pie data={healthData} cx="50%" cy="50%" outerRadius={90} paddingAngle={3} dataKey="value" label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`} labelLine={false}>
                  {healthData.map((_, i) => <Cell key={i} fill={['#10b981', '#f59e0b', '#ef4444'][i]} />)}
                </Pie>
                <Tooltip content={<CustomTooltip />} />
              </PieChart>
            </ResponsiveContainer>
          ) : (
            <div className="empty-state" style={{ padding: 40 }}>
              <Package size={40} color="var(--text-muted)" />
              <p>No inventory data</p>
            </div>
          )}
        </motion.div>
      </div>

      {/* Movement type breakdown */}
      {typeData.length > 0 && (
        <motion.div className="card" initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.3 }}>
          <h3 style={{ marginBottom: 4 }}>Movement Type Breakdown</h3>
          <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: 20 }}>All-time totals</p>
          <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap' }}>
            {typeData.map((d, i) => (
              <div key={d.name} style={{
                flex: 1, minWidth: 120, padding: '16px 20px', borderRadius: 12,
                background: `${COLORS[i % COLORS.length]}12`, border: `1px solid ${COLORS[i % COLORS.length]}25`,
              }}>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 8 }}>{d.name}</div>
                <div style={{ fontSize: '2rem', fontWeight: 800, color: COLORS[i % COLORS.length], letterSpacing: '-0.04em' }}>{d.value}</div>
              </div>
            ))}
          </div>
        </motion.div>
      )}
    </div>
  );
}
