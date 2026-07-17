import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { authApi } from '../services/api';
import toast from 'react-hot-toast';
import { Package, Eye, EyeOff, UserPlus, AlertCircle, Shield, ArrowLeft } from 'lucide-react';

export default function RegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ employeeCode: '', fullName: '', email: '', password: '', confirmPassword: '' });
  const [showPass, setShowPass] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!form.employeeCode || !form.fullName || !form.email || !form.password || !form.confirmPassword) {
      setError('Please fill in all fields');
      return;
    }
    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match');
      return;
    }
    if (form.password.length < 8 || form.password.length > 20) {
      setError('Password must be between 8 and 20 characters');
      return;
    }

    setLoading(true);
    try {
      const res = await authApi.register({
        employeeCode: form.employeeCode.trim(),
        fullName: form.fullName.trim(),
        email: form.email.trim(),
        password: form.password,
      });

      const roles = res?.data?.data?.roles;
      let displayRole = form.fullName.trim() || 'New User';
      if (roles && roles.length > 0) {
        // Format ROLE_WAREHOUSE_OPERATOR to "Warehouse Operator"
        displayRole = roles[0].replace('ROLE_', '').replace(/_/g, ' ');
        displayRole = displayRole.split(' ').map(w => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase()).join(' ');
      }

      // Save to testAccounts in localStorage for quick login during testing
      const testAccounts = JSON.parse(localStorage.getItem('testAccounts') || '[]');
      // Update existing or add new
      const existingIndex = testAccounts.findIndex(acc => acc.employeeCode === form.employeeCode.trim());
      const newAcc = {
        employeeCode: form.employeeCode.trim(),
        password: form.password,
        role: displayRole
      };
      if (existingIndex >= 0) {
        testAccounts[existingIndex] = newAcc;
      } else {
        testAccounts.push(newAcc);
      }
      localStorage.setItem('testAccounts', JSON.stringify(testAccounts));

      toast.success('Registration successful! You can now sign in.');
      navigate('/login');
    } catch (err) {
      let msg = 'Registration failed. Please try again.';
      const data = err.response?.data;
      if (data) {
        if (err.response?.status === 409) {
          msg = data.message || 'An account with this employee code already exists.';
        } else if (err.response?.status === 400 && data.errors) {
          const fieldErrors = data.errors;
          if (fieldErrors.some(e => e.field?.includes('employeeCode'))) {
            msg = 'Invalid employee code format. Use format: EMP000001';
          } else if (fieldErrors.some(e => e.field?.includes('email'))) {
            msg = 'Please enter a valid email address.';
          } else if (fieldErrors.some(e => e.field?.includes('fullName'))) {
            msg = 'Full name must be between 3 and 100 characters.';
          } else if (fieldErrors.some(e => e.field?.includes('password'))) {
            msg = 'Password must be between 8 and 20 characters.';
          } else {
            msg = data.message || 'Invalid input. Please check your details.';
          }
        } else {
          msg = data.message || msg;
        }
      }
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  const set = (field, val) => setForm(f => ({ ...f, [field]: val }));

  return (
    <div style={{
      minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center',
      padding: '20px', background: 'radial-gradient(ellipse 80% 60% at 50% -10%, rgba(99,102,241,0.18) 0%, transparent 70%), var(--bg-base)',
    }}>
      <div style={{ position: 'fixed', inset: 0, overflow: 'hidden', pointerEvents: 'none', zIndex: 0 }}>
        <div style={{ position: 'absolute', top: '-20%', right: '-10%', width: 600, height: 600, borderRadius: '50%', background: 'radial-gradient(circle, rgba(99,102,241,0.12) 0%, transparent 70%)' }} />
        <div style={{ position: 'absolute', bottom: '-20%', left: '-10%', width: 500, height: 500, borderRadius: '50%', background: 'radial-gradient(circle, rgba(6,182,212,0.1) 0%, transparent 70%)' }} />
      </div>

      <motion.div initial={{ opacity: 0, y: 30 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.5, ease: 'easeOut' }}
        style={{ width: '100%', maxWidth: 480, position: 'relative', zIndex: 1 }}>

        <div style={{ textAlign: 'center', marginBottom: 36 }}>
          <motion.div initial={{ scale: 0 }} animate={{ scale: 1 }} transition={{ delay: 0.2, type: 'spring', stiffness: 200 }}
            style={{ width: 72, height: 72, background: 'linear-gradient(135deg, #6366f1, #4f46e5)', borderRadius: 20, display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 20px', boxShadow: '0 16px 40px rgba(99,102,241,0.4)' }}>
            <Package size={36} color="#fff" />
          </motion.div>
          <h1 style={{ fontSize: 'clamp(1.4rem, 4vw, 1.8rem)', fontWeight: 800, letterSpacing: '-0.04em', marginBottom: 8 }}>Create Account</h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>Register as a new employee</p>
        </div>

        <div style={{ background: 'var(--bg-card)', border: '1px solid var(--glass-border)', borderRadius: 'var(--radius-xl)', padding: 'clamp(20px, 5vw, 36px)', boxShadow: '0 24px 80px rgba(0,0,0,0.5)' }}>
          <Link to="/login" style={{ display: 'inline-flex', alignItems: 'center', gap: 6, color: 'var(--text-muted)', fontSize: '0.82rem', textDecoration: 'none', marginBottom: 20, fontWeight: 500 }}>
            <ArrowLeft size={14} /> Back to sign in
          </Link>

          {error && (
            <div className="alert alert-error" style={{ marginBottom: 16 }}>
              <AlertCircle size={16} style={{ flexShrink: 0, marginTop: 1 }} />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label">Employee Code *</label>
              <input className="form-control" type="text" placeholder="e.g. EMP000001" value={form.employeeCode} onChange={e => set('employeeCode', e.target.value)} autoFocus />
            </div>
            <div className="form-group">
              <label className="form-label">Full Name *</label>
              <input className="form-control" type="text" placeholder="John Doe" value={form.fullName} onChange={e => set('fullName', e.target.value)} />
            </div>
            <div className="form-group">
              <label className="form-label">Email *</label>
              <input className="form-control" type="email" placeholder="john@company.com" value={form.email} onChange={e => set('email', e.target.value)} />
            </div>
            <div className="form-group">
              <label className="form-label">Password *</label>
              <div style={{ position: 'relative' }}>
                <input className="form-control" type={showPass ? 'text' : 'password'} placeholder="Min 8 characters" value={form.password} onChange={e => set('password', e.target.value)} style={{ paddingRight: 44 }} />
                <button type="button" onClick={() => setShowPass(s => !s)}
                  style={{ position: 'absolute', right: 12, top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)', display: 'flex' }}>
                  {showPass ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
            </div>
            <div className="form-group">
              <label className="form-label">Confirm Password *</label>
              <input className="form-control" type={showPass ? 'text' : 'password'} placeholder="Re-enter password" value={form.confirmPassword} onChange={e => set('confirmPassword', e.target.value)} />
            </div>

            <motion.button type="submit" className="btn btn-primary w-full"
              style={{ marginTop: 8, height: 46, justifyContent: 'center', fontSize: '0.95rem' }}
              disabled={loading} whileTap={{ scale: 0.98 }}>
              {loading ? (<><span className="spinner" style={{ width: 18, height: 18 }} /> Creating account...</>) : (<><UserPlus size={18} /> Register</>)}
            </motion.button>
          </form>

          <div style={{ marginTop: 24, paddingTop: 20, borderTop: '1px solid var(--border-subtle)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, color: 'var(--text-muted)', fontSize: '0.8rem' }}>
              <Shield size={14} />
              <span>Registration pending manager approval</span>
            </div>
          </div>
        </div>
      </motion.div>
    </div>
  );
}
