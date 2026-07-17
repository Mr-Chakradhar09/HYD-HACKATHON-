import { useState, useEffect } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import {
  Package, LayoutDashboard, Warehouse, ShoppingBag,
  ClipboardList, ArrowLeftRight, GitPullRequestArrow, BookMarked, RefreshCw,
  BarChart3, Users, LogOut, ChevronRight, Sun, Moon, Menu, X, Shield
} from 'lucide-react';

const navItems = [
  { path: '/',             icon: LayoutDashboard, label: 'Dashboard',     exact: true },
  { path: '/warehouses',   icon: Warehouse,        label: 'Warehouses'    },
  { path: '/products',     icon: ShoppingBag,      label: 'Products'      },
  { path: '/inventory',    icon: ClipboardList,    label: 'Inventory'     },
  { path: '/movements',    icon: ArrowLeftRight,   label: 'Movements'     },
  { path: '/transfers',    icon: GitPullRequestArrow, label: 'Transfers'  },
  { path: '/reservations', icon: BookMarked,       label: 'Reservations'  },
  { path: '/replenishment',icon: RefreshCw,        label: 'Replenishment' },
  { path: '/reports',      icon: BarChart3,        label: 'Reports',       reportsHiddenForOperator: true },
  { path: '/employees',    icon: Users,            label: 'Employees',    adminOnly: true },
];

function NavItem({ item, collapsed, onClick }) {
  const { isAdmin, isOperator } = useAuth();
  if (item.adminOnly && !isAdmin()) return null;
  if (item.reportsHiddenForOperator && isOperator()) return null;
  return (
    <NavLink to={item.path} end={item.exact} onClick={onClick}
      style={({ isActive }) => ({
        display: 'flex', alignItems: 'center', gap: 11,
        padding: collapsed ? '10px 13px' : '10px 14px',
        borderRadius: 9, textDecoration: 'none', transition: 'all 0.18s',
        marginBottom: 2, justifyContent: collapsed ? 'center' : 'flex-start',
        background: isActive ? 'rgba(99,102,241,0.12)' : 'transparent',
        color: isActive ? 'var(--primary)' : 'var(--text-muted)',
        borderLeft: isActive ? '2px solid var(--primary)' : '2px solid transparent',
        fontWeight: isActive ? 600 : 500, fontSize: '0.86rem',
      })}
      title={collapsed ? item.label : undefined}
    >
      <item.icon size={18} style={{ flexShrink: 0 }} />
      {!collapsed && <span>{item.label}</span>}
    </NavLink>
  );
}

export default function Layout({ children }) {
  const { user, logout, isAdmin } = useAuth();
  const { theme, toggle: toggleTheme } = useTheme();
  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const [isMobile, setIsMobile] = useState(window.innerWidth < 768);
  const navigate = useNavigate();
  const isDark = theme === 'dark';

  useEffect(() => {
    const handleResize = () => setIsMobile(window.innerWidth < 768);
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  useEffect(() => {
    if (isMobile) setCollapsed(true);
  }, [isMobile]);

  const handleLogout = () => { logout(); navigate('/login'); };
  const W = collapsed ? 68 : 232;

  const sidebarStyle = {
    width: W, background: 'var(--bg-surface)',
    borderRight: '1px solid var(--border-subtle)',
    display: 'flex', flexDirection: 'column',
    position: isMobile ? 'fixed' : 'fixed', top: 0, left: 0, bottom: 0,
    zIndex: 100, overflow: 'hidden', flexShrink: 0,
    transition: 'width 0.22s cubic-bezier(0.4,0,0.2,1), transform 0.22s cubic-bezier(0.4,0,0.2,1)',
    boxShadow: isDark ? 'none' : '2px 0 12px rgba(0,0,0,0.06)',
    transform: isMobile && !mobileOpen ? 'translateX(-100%)' : 'translateX(0)',
  };

  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      {/* Mobile overlay */}
      <AnimatePresence>
        {mobileOpen && (
          <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
            onClick={() => setMobileOpen(false)}
            style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)', backdropFilter: 'blur(4px)', zIndex: 99 }}
          />
        )}
      </AnimatePresence>

      {/* ── Sidebar ── */}
      <aside style={sidebarStyle}>
        {/* Logo */}
        <div style={{ padding: collapsed ? '18px 0' : '18px 18px', borderBottom: '1px solid var(--border-subtle)', display: 'flex', alignItems: 'center', gap: 11, justifyContent: collapsed ? 'center' : 'flex-start', height: 66, flexShrink: 0 }}>
          <div style={{ width: 34, height: 34, background: 'linear-gradient(135deg, #6366f1, #4f46e5)', borderRadius: 9, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0, boxShadow: '0 4px 12px rgba(99,102,241,0.35)' }}>
            <Package size={18} color="#fff" />
          </div>
          {!collapsed && (
            <div style={{ overflow: 'hidden' }}>
              <div style={{ fontWeight: 800, fontSize: '0.95rem', letterSpacing: '-0.03em', color: 'var(--text-primary)', whiteSpace: 'nowrap' }}>InventoryOS</div>
              <div style={{ fontSize: '0.62rem', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.1em' }}>Management Platform</div>
            </div>
          )}
        </div>

        {/* Collapse toggle */}
        <button onClick={() => setCollapsed(c => !c)}
          style={{ position: 'absolute', right: -11, top: 22, width: 22, height: 22, borderRadius: '50%', background: 'var(--bg-card)', border: '1px solid var(--border)', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)', zIndex: 10 }}
        >
          {collapsed ? <ChevronRight size={11} /> : <X size={11} />}
        </button>

        {/* Nav */}
        <nav style={{ flex: 1, padding: '12px 10px', overflowY: 'auto', overflowX: 'hidden' }}>
          {!collapsed && <div style={{ fontSize: '0.65rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.1em', padding: '0 4px', marginBottom: 8 }}>Menu</div>}
          {navItems.map(item => <NavItem key={item.path} item={item} collapsed={collapsed} onClick={() => setMobileOpen(false)} />)}
        </nav>

        {/* Bottom area */}
        <div style={{ padding: collapsed ? '12px 8px' : '12px 14px', borderTop: '1px solid var(--border-subtle)', display: 'flex', flexDirection: 'column', gap: 6 }}>
          {/* Theme toggle */}
          <button onClick={toggleTheme}
            style={{ display: 'flex', alignItems: 'center', gap: 10, padding: collapsed ? '9px 0' : '9px 10px', borderRadius: 9, background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)', fontFamily: 'Inter, sans-serif', fontSize: '0.84rem', fontWeight: 500, width: '100%', justifyContent: collapsed ? 'center' : 'flex-start', transition: 'color 0.2s' }}
          >
            {isDark ? <Sun size={17} /> : <Moon size={17} />}
            {!collapsed && <span>{isDark ? 'Light Mode' : 'Dark Mode'}</span>}
          </button>

          {/* User */}
          <div style={{ display: 'flex', alignItems: 'center', gap: 9, padding: collapsed ? '6px 0' : '6px 6px', justifyContent: collapsed ? 'center' : 'flex-start' }}>
            <div style={{ width: 32, height: 32, borderRadius: 9, background: 'linear-gradient(135deg, rgba(99,102,241,0.3), rgba(79,70,229,0.3))', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.82rem', fontWeight: 700, color: 'var(--primary)', flexShrink: 0 }}>
              {(user?.fullName?.charAt(0) || user?.employeeCode?.charAt(user?.employeeCode?.length - 1) || 'U')}
            </div>
            {!collapsed && (
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontSize: '0.82rem', fontWeight: 600, color: 'var(--text-primary)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{user?.fullName || user?.employeeCode || 'User'}</div>
                <div style={{ fontSize: '0.68rem', color: 'var(--text-muted)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{(user?.roles?.[0] || '').replace(/_/g, ' ')}</div>
              </div>
            )}
            {!collapsed && (
              <button onClick={handleLogout} title="Sign out" style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)', padding: 4, display: 'flex', borderRadius: 6, transition: 'color 0.2s' }}>
                <LogOut size={15} />
              </button>
            )}
          </div>
        </div>
      </aside>

      {/* ── Main ── */}
      <div style={{ flex: 1, marginLeft: isMobile ? 0 : W, transition: 'margin-left 0.22s cubic-bezier(0.4,0,0.2,1)', display: 'flex', flexDirection: 'column', minHeight: '100vh', minWidth: 0, overflow: 'hidden' }}>
        {/* Top bar */}
        <header style={{
          height: 66, background: 'var(--bg-surface)', borderBottom: '1px solid var(--border-subtle)',
          display: 'flex', alignItems: 'center', justifyContent: 'space-between',
          padding: isMobile ? '0 14px' : '0 24px', position: 'sticky', top: 0, zIndex: 50,
          boxShadow: isDark ? 'none' : '0 1px 8px rgba(0,0,0,0.06)',
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            {isMobile && (
              <button onClick={() => setMobileOpen(true)}
                style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-primary)', padding: 6, display: 'flex', borderRadius: 8 }}>
                <Menu size={22} />
              </button>
            )}
            {!isMobile && (
              <span style={{ fontSize: '0.82rem', color: 'var(--text-muted)' }}>
                {new Date().toLocaleDateString('en-IN', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })}
              </span>
            )}
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            {isAdmin() && (
              <div style={{ display: 'flex', alignItems: 'center', gap: 5, background: 'rgba(99,102,241,0.1)', border: '1px solid rgba(99,102,241,0.2)', padding: '4px 11px', borderRadius: 20, fontSize: '0.71rem', fontWeight: 700, color: 'var(--primary)' }}>
                <Shield size={11} /> ADMIN
              </div>
            )}
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, background: 'var(--bg-card)', border: '1px solid var(--border-subtle)', padding: '6px 13px', borderRadius: 10 }}>
              <div style={{ width: 26, height: 26, borderRadius: 7, background: 'linear-gradient(135deg, rgba(99,102,241,0.3), rgba(79,70,229,0.3))', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.78rem', fontWeight: 700, color: 'var(--primary)' }}>
                {user?.fullName?.charAt(0) || user?.employeeCode?.charAt(user?.employeeCode?.length - 1) || 'U'}
              </div>
              <span style={{ fontSize: '0.84rem', fontWeight: 600, color: 'var(--text-primary)' }}>{user?.fullName || user?.employeeCode || 'User'}</span>
            </div>
          </div>
        </header>

        {/* Page content */}
        <main style={{ flex: 1, padding: isMobile ? '14px' : '24px', maxWidth: 1440, width: '100%', margin: '0 auto', boxSizing: 'border-box', overflow: 'hidden' }}>
          <motion.div key={window.location.pathname} initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.25 }}>
            {children}
          </motion.div>
        </main>
      </div>
    </div>
  );
}
