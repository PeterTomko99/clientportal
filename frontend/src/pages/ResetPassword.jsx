import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import api from '../api';

export default function ResetPassword() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const navigate = useNavigate();

  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  if (!token) {
    return (
      <div className="auth-page">
        <div className="auth-card" style={{ textAlign: 'center' }}>
          <h1>Invalid link</h1>
          <p>This password reset link is invalid or has expired.</p>
          <Link to="/forgot-password"><button className="btn-primary" style={{ marginTop: 16 }}>Request a new link</button></Link>
        </div>
      </div>
    );
  }

  async function submit(e) {
    e.preventDefault();
    if (password !== confirm) {
      setError('Passwords do not match.');
      return;
    }
    setLoading(true);
    setError('');
    try {
      await api.post('/api/auth/reset-password', { token, newPassword: password });
      navigate('/login', { state: { message: 'Password updated. Please sign in.' } });
    } catch (err) {
      if (err.response?.status === 400) {
        setError('This reset link has expired or already been used.');
      } else {
        setError('Something went wrong. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1>Set new password</h1>
        <p>Choose a new password for your account.</p>
        <form onSubmit={submit}>
          <div className="form-group">
            <label htmlFor="password">New Password</label>
            <input
              id="password"
              name="password"
              type="password"
              value={password}
              onChange={e => { setError(''); setPassword(e.target.value); }}
              required
              minLength={8}
              autoComplete="new-password"
            />
          </div>
          <div className="form-group">
            <label htmlFor="confirm">Confirm Password</label>
            <input
              id="confirm"
              name="confirm"
              type="password"
              value={confirm}
              onChange={e => { setError(''); setConfirm(e.target.value); }}
              required
              minLength={8}
              autoComplete="new-password"
            />
          </div>
          {error && <p className="form-error">{error}</p>}
          <button className="btn-primary" type="submit" disabled={loading}>
            {loading ? 'Updating...' : 'Update password'}
          </button>
        </form>
      </div>
    </div>
  );
}
