import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import Navbar from '../components/Navbar';
import api from '../api';
import { isAdmin } from '../auth';

function Badge({ value }) {
  return <span className={`badge badge-${value.toLowerCase()}`}>{value}</span>;
}

function Pagination({ data, onPage }) {
  if (data.totalPages <= 1) return null;
  return (
    <div className="pagination">
      <button disabled={data.number === 0} onClick={() => onPage(data.number - 1)}>Previous</button>
      <span className="page-info">Page {data.number + 1} of {data.totalPages}</span>
      <button disabled={data.number + 1 >= data.totalPages} onClick={() => onPage(data.number + 1)}>Next</button>
    </div>
  );
}

const empty = { content: [], totalPages: 0, number: 0 };

export default function Admin() {
  const navigate = useNavigate();
  const [tab, setTab] = useState('users');
  const [users, setUsers] = useState(empty);
  const [projects, setProjects] = useState(empty);
  const [invoices, setInvoices] = useState(empty);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isAdmin()) {
      navigate('/');
      return;
    }
    Promise.all([
      api.get('/api/admin/users?size=20&sort=createdAt,desc'),
      api.get('/api/admin/projects?size=20&sort=createdAt,desc'),
      api.get('/api/admin/invoices?size=20&sort=createdAt,desc'),
    ])
      .then(([u, p, i]) => {
        setUsers(u.data);
        setProjects(p.data);
        setInvoices(i.data);
      })
      .catch(() => toast.error('Failed to load admin data.'))
      .finally(() => setLoading(false));
  }, []);

  async function loadPage(type, page) {
    try {
      const { data } = await api.get(`/api/admin/${type}?page=${page}&size=20&sort=createdAt,desc`);
      if (type === 'users') { setUsers(data); }
      else if (type === 'projects') { setProjects(data); }
      else { setInvoices(data); }
    } catch {
      toast.error('Failed to load page.');
    }
  }

  return (
    <>
      <Navbar />
      <div className="page">
        <div className="page-header">
          <h1>Admin</h1>
        </div>

        <div className="tabs">
          <div className={`tab ${tab === 'users' ? 'active' : ''}`} onClick={() => setTab('users')}>
            Users
          </div>
          <div className={`tab ${tab === 'projects' ? 'active' : ''}`} onClick={() => setTab('projects')}>
            Projects
          </div>
          <div className={`tab ${tab === 'invoices' ? 'active' : ''}`} onClick={() => setTab('invoices')}>
            Invoices
          </div>
        </div>

        {loading ? (
          <div className="card"><p className="empty">Loading...</p></div>
        ) : (
          <>
            {tab === 'users' && (
              <div className="card">
                {users.content.length === 0 ? (
                  <p className="empty">No users.</p>
                ) : (
                  <>
                    <table>
                      <thead>
                        <tr><th>Name</th><th>Email</th><th>Role</th><th>Joined</th></tr>
                      </thead>
                      <tbody>
                        {users.content.map(u => (
                          <tr key={u.id}>
                            <td>{u.name}</td>
                            <td>{u.email}</td>
                            <td><Badge value={u.role} /></td>
                            <td>{u.createdAt ? new Date(u.createdAt).toLocaleDateString() : '—'}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                    <Pagination data={users} onPage={(p) => loadPage('users', p)} />
                  </>
                )}
              </div>
            )}

            {tab === 'projects' && (
              <div className="card">
                {projects.content.length === 0 ? (
                  <p className="empty">No projects.</p>
                ) : (
                  <>
                    <table>
                      <thead>
                        <tr><th>Name</th><th>Status</th><th>Deadline</th><th>Created</th></tr>
                      </thead>
                      <tbody>
                        {projects.content.map(p => (
                          <tr key={p.id}>
                            <td>{p.name}</td>
                            <td><Badge value={p.status} /></td>
                            <td>{p.deadline || '—'}</td>
                            <td>{p.createdAt ? new Date(p.createdAt).toLocaleDateString() : '—'}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                    <Pagination data={projects} onPage={(p) => loadPage('projects', p)} />
                  </>
                )}
              </div>
            )}

            {tab === 'invoices' && (
              <div className="card">
                {invoices.content.length === 0 ? (
                  <p className="empty">No invoices.</p>
                ) : (
                  <>
                    <table>
                      <thead>
                        <tr><th>ID</th><th>Project</th><th>Amount</th><th>Due Date</th><th>Status</th></tr>
                      </thead>
                      <tbody>
                        {invoices.content.map(i => (
                          <tr key={i.id}>
                            <td>#{i.id}</td>
                            <td>{i.projectName || `Project #${i.projectId}`}</td>
                            <td>${Number(i.amount).toFixed(2)}</td>
                            <td>{i.dueDate}</td>
                            <td><Badge value={i.status} /></td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                    <Pagination data={invoices} onPage={(p) => loadPage('invoices', p)} />
                  </>
                )}
              </div>
            )}
          </>
        )}
      </div>
    </>
  );
}
