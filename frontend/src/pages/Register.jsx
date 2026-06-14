import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api';

export default function Register() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: '', email: '', password: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  function handle(e) {
    setError('');
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  async function submit(e) {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      await api.post('/api/auth/register', form);
      navigate('/login', { state: { message: 'Account created. Please sign in.' } });
    } catch (err) {
      const status = err.response?.status;
      if (status === 409) {
        setError('An account with that email already exists.');
      } else if (status === 429) {
        setError('Too many attempts. Please wait a moment.');
      } else {
        setError('Registration failed. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1>Create account</h1>
        <p>Get started with your client portal</p>
        <form onSubmit={submit}>
          <div className="form-group">
            <label htmlFor="name">Full Name</label>
            <input id="name" name="name" value={form.name} onChange={handle} required autoComplete="name" />
          </div>
          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input id="email" name="email" type="email" pattern="[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}" value={form.email} onChange={handle} required autoComplete="email" />
          </div>
          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input id="password" name="password" type="password" value={form.password} onChange={handle} required minLength={8} autoComplete="new-password" />
          </div>
          {error && <p className="form-error">{error}</p>}
          <button className="btn-primary" type="submit" disabled={loading}>
            {loading ? 'Creating...' : 'Create account'}
          </button>
        </form>
        <p className="auth-footer">
          Already have an account? <Link to="/login">Sign in</Link>
        </p>
      </div>
    </div>
  );
}
