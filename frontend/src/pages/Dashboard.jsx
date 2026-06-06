import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../components/Navbar';
import api from '../api';

function Badge({ value }) {
  return <span className={`badge badge-${value.toLowerCase()}`}>{value}</span>;
}

export default function Dashboard() {
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get('/api/projects?size=5&sort=createdAt,desc')
      .then(({ data }) => setProjects(data.content || []))
      .finally(() => setLoading(false));
  }, []);

  const total = projects.length;
  const completed = projects.filter(p => p.status === 'COMPLETED').length;
  const inProgress = projects.filter(p => p.status === 'IN_PROGRESS').length;

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
            <div className="stat-label">Recent Projects</div>
            <div className="stat-value">{total}</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">In Progress</div>
            <div className="stat-value">{inProgress}</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Completed</div>
            <div className="stat-value">{completed}</div>
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
