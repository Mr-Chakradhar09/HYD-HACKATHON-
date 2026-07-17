import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  Warehouse, Package, ClipboardList, ArrowLeftRight,
  RefreshCw, ShoppingBag, AlertTriangle, Activity, TrendingUp
} from 'lucide-react';
import {
  AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, PieChart, Pie, Cell
} from 'recharts';
import { warehouseApi, inventoryApi, movementApi, replenishmentApi, productApi } from '../services/api';

const COLORS = ['#6366f1', '#10b981', '#f59e0b', '#ef4444', '#06b6d4'];

const ChartTooltip = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null;
  return (
    <div style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: 10, padding: '10px 14px', boxShadow: 'var(--shadow-card)' }}>
      {label && <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginBottom: 5 }}>{label}</div>}
      {payload.map((p, i) => (
        <div key={i} style={{ fontSize: '0.84rem', fontWeight: 600, color: p.color || p.fill }}>{p.name}: {p.value}</div>
      ))}
    </div>
  );
};

function StatCard({ icon: Icon, label, value, sub, color, delay }) {
  return (
    <motion.div className="stat-card" initial={{ opacity: 0, y: 18 }} animate={{ opacity: 1, y: 0 }} transition={{ delay, duration: 0.35 }}>
      <div className="stat-icon" style={{ background: `${color}18` }}>
        <Icon size={21} color={color} />
      </div>
      <div style={{ flex: 1 }}>
        <div className="stat-label">{label}</div>
        <div className="stat-value">{value ?? <span className="spinner" style={{ width: 22, height: 22 }} />}</div>
        {sub && <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: 2 }}>{sub}</div>}
      </div>
      <div style={{ position: 'absolute', top: 0, right: 0, width: 90, height: 90, borderRadius: '50%', background: color, opacity: 0.07, transform: 'translate(30%,-30%)' }} />
    </motion.div>
  );
}

function useIsMobile() {
  const [isMobile, setIsMobile] = useState(window.innerWidth < 768);
  useEffect(() => {
    const handleResize = () => setIsMobile(window.innerWidth < 768);
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);
  return isMobile;
}

export default function Dashboard() {
  const navigate = useNavigate();
  const { canViewDashboardKPI } = useAuth();
  const isMobile = useIsMobile();
  const [stats, setStats] = useState({ warehouses: null, inventory: null, movements: null, products: null });
  const [trendData, setTrendData] = useState([]);
  const [typeData, setTypeData] = useState([]);
  const [lowStockCount, setLowStockCount] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        const [whRes, invRes, movRes, prodRes] = await Promise.allSettled([
          warehouseApi.getAll({ page: 0, size: 1 }),
          inventoryApi.getAll({ page: 0, size: 1 }),
          movementApi.getAll({ page: 0, size: 200 }),
          productApi.getAll({ page: 0, size: 1 }),
        ]);

        const whData = whRes.value?.data?.data;
        const invData = invRes.value?.data?.data;
        const prodData = prodRes.value?.data?.data;

        setStats({
          warehouses: whData?.totalElements ?? (Array.isArray(whData) ? whData.length : null),
          inventory: invData?.totalElements ?? (Array.isArray(invData) ? invData.length : null),
          products: prodData?.totalElements ?? (Array.isArray(prodData) ? prodData.length : null),
          movements: movRes.value?.data?.data?.totalElements ?? 0,
        });

        // Build movement trend and type data from the 200 most recent
        const movList = (() => {
          const d = movRes.value?.data?.data;
          return d?.content || d || [];
        })();

        // 7-day trend
        const days = [];
        for (let i = 6; i >= 0; i--) {
          const d = new Date(); d.setDate(d.getDate() - i);
          const label = d.toLocaleDateString('en', { weekday: 'short' });
          const dayMovs = movList.filter(m => {
            const md = new Date(m.createdAt || m.movementDate || 0);
            return md.toDateString() === d.toDateString();
          });
          days.push({
            label,
            Receipts: dayMovs.filter(m => m.movementType === 'GOODS_RECEIPT').length,
            Issues: dayMovs.filter(m => m.movementType === 'GOODS_ISSUE').length,
          });
        }
        setTrendData(days);

        // Type breakdown
        const byType = {};
        movList.forEach(m => { byType[m.movementType] = (byType[m.movementType] || 0) + 1; });
        setTypeData(Object.entries(byType).map(([name, value]) => ({ name: name.replace(/_/g, ' '), value })));

        // Low stock: get all inventory and count qty < 10
        const allInvRes = await inventoryApi.getAll({ size: 500 });
        const allInv = allInvRes.data?.data?.content || allInvRes.data?.data || [];
        setLowStockCount(allInv.filter(i => i.currentQuantity < 10).length);
      } catch (e) {
        console.error('Dashboard load error:', e);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const quickActions = [
    { label: 'View Warehouses', icon: Warehouse, path: '/warehouses', color: '#6366f1' },
    { label: 'Low Stock Alert', icon: AlertTriangle, path: '/inventory', color: '#f59e0b', badge: lowStockCount },
    { label: 'Record Movement', icon: ArrowLeftRight, path: '/movements', color: '#10b981' },
    { label: 'Browse Products', icon: ShoppingBag, path: '/products', color: '#06b6d4' },
  ];

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Dashboard</h1>
          <p className="page-subtitle">Real-time overview of your inventory ecosystem</p>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 7, color: 'var(--success)', background: 'rgba(16,185,129,0.1)', border: '1px solid rgba(16,185,129,0.2)', padding: '7px 14px', borderRadius: 10, fontSize: '0.8rem', fontWeight: 600 }}>
          <Activity size={13} /> All Systems Operational
        </div>
      </div>

      {/* KPI Stats */}
      {canViewDashboardKPI() && (
        <div className="grid grid-4 gap-4 mb-6">
          <StatCard icon={Warehouse}     label="Warehouses"    value={stats.warehouses} sub="Registered facilities" color="#6366f1" delay={0}    />
          <StatCard icon={ShoppingBag}   label="Products"      value={stats.products}   sub="In catalogue"          color="#06b6d4" delay={0.05}  />
          <StatCard icon={ClipboardList} label="Inventory Bins" value={stats.inventory} sub="Active stock records"  color="#10b981" delay={0.1}   />
          <StatCard icon={AlertTriangle} label="Low Stock"     value={lowStockCount}    sub="Items below 10 units"  color="#f59e0b" delay={0.15}  />
        </div>
      )}

      {/* Charts */}
      {canViewDashboardKPI() && (
        <div className="grid gap-4 mb-6" style={{ gridTemplateColumns: isMobile ? '1fr' : 'minmax(0,2fr) minmax(0,1fr)' }}>
        {/* Trend chart */}
        <motion.div className="card" initial={{ opacity: 0, y: 18 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.2 }}>
          <div style={{ display: 'flex', alignItems: isMobile ? 'flex-start' : 'center', justifyContent: 'space-between', marginBottom: 18, flexDirection: isMobile ? 'column' : 'row', gap: isMobile ? 10 : 0 }}>
            <div>
              <h3 style={{ marginBottom: 2 }}>Movement Activity</h3>
              <p style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>Last 7 days</p>
            </div>
            <div style={{ display: 'flex', gap: 14 }}>
              {[{ c: '#6366f1', l: 'Receipts' }, { c: '#10b981', l: 'Issues' }].map(x => (
                <div key={x.l} style={{ display: 'flex', alignItems: 'center', gap: 5, fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                  <div style={{ width: 8, height: 8, borderRadius: 2, background: x.c }} />
                  {x.l}
                </div>
              ))}
            </div>
          </div>
          <ResponsiveContainer width="100%" height={isMobile ? 180 : 210}>
            <AreaChart data={trendData} margin={{ left: -10 }}>
              <defs>
                <linearGradient id="gReceipt" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#6366f1" stopOpacity={0.25} />
                  <stop offset="95%" stopColor="#6366f1" stopOpacity={0} />
                </linearGradient>
                <linearGradient id="gIssue" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#10b981" stopOpacity={0.25} />
                  <stop offset="95%" stopColor="#10b981" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="var(--border-subtle)" />
              <XAxis dataKey="label" tick={{ fill: 'var(--text-muted)', fontSize: 11 }} axisLine={false} tickLine={false} />
              <YAxis allowDecimals={false} tick={{ fill: 'var(--text-muted)', fontSize: 11 }} axisLine={false} tickLine={false} width={28} />
              <Tooltip content={<ChartTooltip />} />
              <Area type="monotone" dataKey="Receipts" stroke="#6366f1" strokeWidth={2} fill="url(#gReceipt)" dot={false} />
              <Area type="monotone" dataKey="Issues" stroke="#10b981" strokeWidth={2} fill="url(#gIssue)" dot={false} />
            </AreaChart>
          </ResponsiveContainer>
        </motion.div>

        {/* Type breakdown */}
        <motion.div className="card" initial={{ opacity: 0, y: 18 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.25 }}>
          <h3 style={{ marginBottom: 3 }}>By Movement Type</h3>
          <p style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginBottom: 16 }}>Overall distribution</p>
          {typeData.length > 0 ? (
            <>
              <div style={{ display: 'flex', justifyContent: 'center' }}>
                <PieChart width={isMobile ? 140 : 160} height={isMobile ? 140 : 160}>
                  <Pie data={typeData} cx="50%" cy="50%" innerRadius={isMobile ? 36 : 42} outerRadius={isMobile ? 62 : 72} paddingAngle={3} dataKey="value">
                    {typeData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                  </Pie>
                  <Tooltip content={<ChartTooltip />} />
                </PieChart>
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 7, marginTop: 10 }}>
                {typeData.map((d, i) => (
                  <div key={d.name} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 7 }}>
                      <div style={{ width: 8, height: 8, borderRadius: 2, background: COLORS[i % COLORS.length], flexShrink: 0 }} />
                      <span style={{ fontSize: '0.76rem', color: 'var(--text-muted)' }}>{d.name}</span>
                    </div>
                    <span style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--text-primary)' }}>{d.value}</span>
                  </div>
                ))}
              </div>
            </>
          ) : (
            <div className="empty-state" style={{ padding: '30px 0' }}>
              <ArrowLeftRight size={32} color="var(--text-muted)" />
              <p>No movement data yet</p>
            </div>
          )}
        </motion.div>
      </div>
      )}

      {/* Quick actions */}
      <motion.div className="card" initial={{ opacity: 0, y: 18 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.3 }}>
        <h3 style={{ marginBottom: 14 }}>Quick Actions</h3>
        <div style={{ display: 'grid', gridTemplateColumns: isMobile ? 'repeat(2, 1fr)' : 'repeat(4, 1fr)', gap: 10 }}>
          {quickActions.map(a => (
            <button key={a.label} onClick={() => navigate(a.path)}
              style={{
                display: 'flex', alignItems: 'center', gap: 9,
                padding: '11px 18px', borderRadius: 10, border: `1px solid ${a.color}25`,
                background: `${a.color}10`, color: a.color,
                cursor: 'pointer', fontFamily: 'Inter, sans-serif',
                fontSize: '0.84rem', fontWeight: 600, transition: 'all 0.2s',
                position: 'relative', justifyContent: 'flex-start',
              }}
              onMouseEnter={e => { e.currentTarget.style.background = `${a.color}1e`; e.currentTarget.style.transform = 'translateY(-2px)'; }}
              onMouseLeave={e => { e.currentTarget.style.background = `${a.color}10`; e.currentTarget.style.transform = ''; }}
            >
              <a.icon size={17} />
              {a.label}
              {a.badge > 0 && (
                <span style={{ position: 'absolute', top: -7, right: -7, minWidth: 18, height: 18, background: a.color, color: '#fff', borderRadius: 9, fontSize: '0.65rem', fontWeight: 800, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '0 4px' }}>
                  {a.badge}
                </span>
              )}
            </button>
          ))}
        </div>
      </motion.div>
    </div>
  );
}
