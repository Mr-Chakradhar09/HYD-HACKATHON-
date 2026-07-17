import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';
import {
  Package, Eye, EyeOff, LogIn, AlertCircle, Shield
} from 'lucide-react';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ employeeCode: '', password: '' });
  const [showPass, setShowPass] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [testAccounts, setTestAccounts] = useState([]);

  useEffect(() => {
    try {
      const accounts = JSON.parse(localStorage.getItem('testAccounts') || '[]');
      setTestAccounts(accounts);
    } catch(e) {}
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.employeeCode || !form.password) {
      setError('Please fill in all fields');
      return;
    }
    setLoading(true);
    setError('');
    try {
      const user = await login(form.employeeCode, form.password);
      
      // Auto-save successful logins to testAccounts so the quick-button appears
      try {
        const accounts = JSON.parse(localStorage.getItem('testAccounts') || '[]');
        const existingIndex = accounts.findIndex(acc => acc.employeeCode === form.employeeCode.trim());
        let displayRole = 'User';
        if (user.roles && user.roles.length > 0) {
            displayRole = user.roles[0].replace('ROLE_', '').replace(/_/g, ' ');
            displayRole = displayRole.split(' ').map(w => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase()).join(' ');
        }
        const newAcc = { employeeCode: form.employeeCode.trim(), password: form.password, role: displayRole };
        if (existingIndex >= 0) {
            accounts[existingIndex] = newAcc;
        } else {
            accounts.push(newAcc);
        }
        localStorage.setItem('testAccounts', JSON.stringify(accounts));
        setTestAccounts(accounts);
      } catch(e) {}

      const displayName = (user.fullName && user.fullName.trim()) || user.employeeCode || 'User';
      toast.success(`Welcome back, ${displayName}!`);
      navigate('/');
    } catch (err) {
      let msg = 'Invalid credentials. Please try again.';
      const data = err.response?.data;
      const status = err.response?.status;
      if (data) {
        if (status === 400) {
          if (data.errors?.length) {
            const fieldErrors = data.errors;
            if (fieldErrors.some(e => e.field?.includes('employeeCode'))) {
              msg = 'Employee code not found. Please check and try again.';
            } else if (fieldErrors.some(e => e.field?.includes('password'))) {
              msg = 'Invalid password. Please try again.';
            } else {
              msg = data.message || 'Invalid input. Please check your details.';
            }
          } else if (data.message) {
            msg = data.message;
          }
        } else if (status === 401) {
          msg = data.message || 'Invalid employee code or password.';
        } else if (status === 404) {
          msg = 'Employee not found. Please check your employee code.';
        } else if (status >= 500) {
          msg = 'Server error. Please try again later.';
        } else {
          msg = data.message || data.error || msg;
        }
      } else if (err.code === 'ECONNABORTED') {
        msg = 'Request timed out. Please try again.';
      } else if (!err.response) {
        msg = 'Cannot connect to server. Please check your connection.';
      }
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '20px',
      background: 'radial-gradient(ellipse 80% 60% at 50% -10%, rgba(99,102,241,0.18) 0%, transparent 70%), var(--bg-base)',
    }}>
      {/* Background orbs */}
      <div style={{ position: 'fixed', inset: 0, overflow: 'hidden', pointerEvents: 'none', zIndex: 0 }}>
        <div style={{
          position: 'absolute', top: '-20%', right: '-10%',
          width: 600, height: 600, borderRadius: '50%',
          background: 'radial-gradient(circle, rgba(99,102,241,0.12) 0%, transparent 70%)',
        }} />
        <div style={{
          position: 'absolute', bottom: '-20%', left: '-10%',
          width: 500, height: 500, borderRadius: '50%',
          background: 'radial-gradient(circle, rgba(6,182,212,0.1) 0%, transparent 70%)',
        }} />
      </div>

      <motion.div
        initial={{ opacity: 0, y: 30 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5, ease: 'easeOut' }}
        style={{ width: '100%', maxWidth: 440, position: 'relative', zIndex: 1 }}
      >
        {/* Header */}
        <div style={{ textAlign: 'center', marginBottom: 36 }}>
          <motion.div
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            transition={{ delay: 0.2, type: 'spring', stiffness: 200 }}
            style={{
              width: 72, height: 72,
              background: 'linear-gradient(135deg, #6366f1, #4f46e5)',
              borderRadius: 20,
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              margin: '0 auto 20px',
              boxShadow: '0 16px 40px rgba(99,102,241,0.4)',
            }}
          >
            <Package size={36} color="#fff" />
          </motion.div>
          <h1 style={{ fontSize: 'clamp(1.4rem, 4vw, 1.8rem)', fontWeight: 800, letterSpacing: '-0.04em', marginBottom: 8 }}>
            InventoryOS
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
            Unified Inventory Management Platform
          </p>
        </div>

        {/* Card */}
        <div style={{
          background: 'var(--bg-card)',
          border: '1px solid var(--glass-border)',
          borderRadius: 'var(--radius-xl)',
          padding: 'clamp(20px, 5vw, 36px)',
          boxShadow: '0 24px 80px rgba(0,0,0,0.5)',
        }}>
          <h2 style={{ marginBottom: 6, fontSize: '1.25rem' }}>Sign In</h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: 28 }}>
            Enter your employee credentials to access the dashboard
          </p>

          <AnimatePresence>
            {error && (
              <motion.div
                initial={{ opacity: 0, height: 0 }}
                animate={{ opacity: 1, height: 'auto' }}
                exit={{ opacity: 0, height: 0 }}
                className="alert alert-error"
              >
                <AlertCircle size={16} style={{ flexShrink: 0, marginTop: 1 }} />
                <span>{error}</span>
              </motion.div>
            )}
          </AnimatePresence>

          <form onSubmit={handleSubmit}>
            <div style={{ display: 'flex', gap: '8px', marginBottom: '16px', flexWrap: 'wrap' }}>
              <button type="button" className="btn" style={{ flex: '1 1 45%', fontSize: '0.8rem', padding: '6px', color: '#fff', background: 'var(--bg-base)', border: '1px solid var(--border-subtle)' }} onClick={() => setForm({ employeeCode: 'EMP000001', password: 'Admin@123' })}>System Admin</button>
              <button type="button" className="btn" style={{ flex: '1 1 45%', fontSize: '0.8rem', padding: '6px', color: '#fff', background: 'var(--bg-base)', border: '1px solid var(--border-subtle)' }} onClick={() => setForm({ employeeCode: 'EMP000002', password: 'Password@123' })}>Manager</button>
              <button type="button" className="btn" style={{ flex: '1 1 45%', fontSize: '0.8rem', padding: '6px', color: '#fff', background: 'var(--bg-base)', border: '1px solid var(--border-subtle)' }} onClick={() => setForm({ employeeCode: 'EMP000003', password: 'Chakri@123' })}>Procurement Officer</button>
              <button type="button" className="btn" style={{ flex: '1 1 45%', fontSize: '0.8rem', padding: '6px', color: '#fff', background: 'var(--bg-base)', border: '1px solid var(--border-subtle)' }} onClick={() => setForm({ employeeCode: 'EMP000004', password: 'Mohan@123' })}>Warehouse Operator</button>
              {testAccounts.map((acc, idx) => {
                if (acc.employeeCode === 'EMP000001' || acc.employeeCode === 'EMP000002' || acc.employeeCode === 'EMP000003' || acc.employeeCode === 'EMP000004') return null;
                return (
                  <button key={idx} type="button" className="btn" style={{ flex: '1 1 45%', fontSize: '0.8rem', padding: '6px', color: '#fff', background: 'var(--bg-base)', border: '1px solid var(--border-subtle)' }} onClick={() => setForm({ employeeCode: acc.employeeCode, password: acc.password })}>
                    {acc.role}
                  </button>
                );
              })}
            </div>
            <div className="form-group">
              <label className="form-label">Employee Code</label>
              <input
                className="form-control"
                type="text"
                placeholder="e.g. EMP000001"
                value={form.employeeCode}
                onChange={(e) => setForm(f => ({ ...f, employeeCode: e.target.value }))}
                autoFocus
              />
            </div>

            <div className="form-group">
              <label className="form-label">Password</label>
              <div style={{ position: 'relative' }}>
                <input
                  className="form-control"
                  type={showPass ? 'text' : 'password'}
                  placeholder="••••••••"
                  value={form.password}
                  onChange={(e) => setForm(f => ({ ...f, password: e.target.value }))}
                  style={{ paddingRight: 44 }}
                />
                <button
                  type="button"
                  onClick={() => setShowPass(s => !s)}
                  style={{
                    position: 'absolute', right: 12, top: '50%', transform: 'translateY(-50%)',
                    background: 'none', border: 'none', cursor: 'pointer',
                    color: 'var(--text-muted)', display: 'flex',
                  }}
                >
                  {showPass ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
            </div>

            <motion.button
              type="submit"
              className="btn btn-primary w-full"
              style={{ marginTop: 8, height: 46, justifyContent: 'center', fontSize: '0.95rem' }}
              disabled={loading}
              whileTap={{ scale: 0.98 }}
            >
              {loading ? (
                <><span className="spinner" style={{ width: 18, height: 18 }} /> Signing in...</>
              ) : (
                <><LogIn size={18} /> Sign In</>
              )}
            </motion.button>
          </form>

          <div style={{
            marginTop: 24, paddingTop: 20,
            borderTop: '1px solid var(--border-subtle)',
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, color: 'var(--text-muted)', fontSize: '0.8rem' }}>
              <Shield size={14} />
              <span>Secured with JWT authentication</span>
            </div>
          </div>
        </div>

        <p style={{ textAlign: 'center', marginTop: 20, color: 'var(--text-muted)', fontSize: '0.85rem' }}>
          Don't have an account?{' '}
          <Link to="/register" style={{ color: 'var(--primary)', fontWeight: 700, textDecoration: 'none' }}>Register here</Link>
        </p>
        {testAccounts.length > 0 && (
          <p style={{ textAlign: 'center', marginTop: 10, fontSize: '0.75rem' }}>
            <button 
              onClick={() => { localStorage.removeItem('testAccounts'); setTestAccounts([]); }} 
              style={{ background: 'none', border: 'none', color: 'var(--text-muted)', textDecoration: 'underline', cursor: 'pointer' }}
            >
              Clear saved test accounts
            </button>
          </p>
        )}
      </motion.div>
    </div>
  );
}
