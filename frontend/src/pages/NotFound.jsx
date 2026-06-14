import { Link } from 'react-router-dom';

export default function NotFound() {
  return (
    <div className="auth-page">
      <div className="auth-card" style={{ textAlign: 'center' }}>
        <h1 style={{ fontSize: 64, margin: '0 0 8px' }}>404</h1>
        <p style={{ marginBottom: 24 }}>This page does not exist.</p>
        <Link to="/"><button className="btn-primary">Go to Dashboard</button></Link>
      </div>
    </div>
  );
}
