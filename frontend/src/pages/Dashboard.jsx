import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../components/Navbar';
import api from '../api';

function Badge({ value }) {
  return <span className={`badge badge-${value.toLowerCase()}`}>{value}</span>;
}

export default function Dashboard() {
  const [stats, setStats] = useState(null);
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      api.get('/api/dashboard/stats'),
      api.get('/api/projects?size=5&sort=createdAt,desc'),
    ])
      .then(([statsRes, projectsRes]) => {
        setStats(statsRes.data);
        setProjects(projectsRes.data.content || []);
      })
      .finally(() => setLoading(false));
  }, []);

  return (
    <>
      <Navbar />
      <div className="page">
        <div className="page-header">
          <h1>Dashboard</h1>
          <Link to="/projects"><button className="btn-primary">View All Projects</button></Link>
        </div>

        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-label">Total Projects</div>
            <div className="stat-value">{loading ? '—' : stats?.totalProjects ?? 0}</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">In Progress</div>
            <div className="stat-value">{loading ? '—' : stats?.inProgressProjects ?? 0}</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Completed</div>
            <div className="stat-value">{loading ? '—' : stats?.completedProjects ?? 0}</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Unpaid Invoices</div>
            <div className="stat-value">{loading ? '—' : (stats?.unpaidInvoices ?? 0) + (stats?.overdueInvoices ?? 0)}</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Total Revenue</div>
            <div className="stat-value">${loading ? '—' : Number(stats?.totalRevenue ?? 0).toFixed(2)}</div>
          </div>
        </div>

        <div className="card">
          <div className="page-header" style={{ marginBottom: 16 }}>
            <h1 style={{ fontSize: 16 }}>Recent Projects</h1>
          </div>
          {loading ? (
            <p className="empty">Loading...</p>
          ) : projects.length === 0 ? (
            <p className="empty">No projects yet. <Link to="/projects" style={{ color: '#2c3e50', fontWeight: 600 }}>Create one.</Link></p>
          ) : (
            <table>
              <thead>
                <tr><th>Name</th><th>Status</th><th>Deadline</th><th></th></tr>
              </thead>
              <tbody>
                {projects.map(p => (
                  <tr key={p.id}>
                    <td>{p.name}</td>
                    <td><Badge value={p.status} /></td>
                    <td>{p.deadline || '—'}</td>
                    <td><Link to={`/projects/${p.id}`}><button className="btn-sm btn-primary">View</button></Link></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </>
  );
}
