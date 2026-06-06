import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../components/Navbar';
import api from '../api';

function Badge({ value }) {
  return <span className={`badge badge-${value.toLowerCase()}`}>{value}</span>;
}

const emptyForm = { name: '', description: '', status: 'PENDING', deadline: '' };

export default function Projects() {
  const [data, setData] = useState({ content: [], totalPages: 0, number: 0 });
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [modal, setModal] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  function load(p = 0) {
    setLoading(true);
    api.get(`/api/projects?page=${p}&size=10&sort=createdAt,desc`)
      .then(({ data: d }) => { setData(d); setPage(p); })
      .finally(() => setLoading(false));
  }

  useEffect(() => { load(); }, []);

  function handle(e) { setForm({ ...form, [e.target.name]: e.target.value }); }

  async function save(e) {
    e.preventDefault();
    setError('');
    setSaving(true);
    try {
      await api.post('/api/projects', form);
      setModal(false);
      setForm(emptyForm);
      load(0);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create project.');
    } finally {
      setSaving(false);
    }
  }

  return (
    <>
      <Navbar />
      <div className="page">
        <div className="page-header">
          <h1>Projects</h1>
          <button className="btn-primary" onClick={() => { setModal(true); setForm(emptyForm); setError(''); }}>
            + New Project
          </button>
        </div>

        <div className="card">
          {loading ? (
            <p className="empty">Loading...</p>
          ) : data.content.length === 0 ? (
            <p className="empty">No projects yet.</p>
          ) : (
            <>
              <table>
                <thead>
                  <tr><th>Name</th><th>Status</th><th>Deadline</th><th></th></tr>
                </thead>
                <tbody>
                  {data.content.map(p => (
                    <tr key={p.id}>
                      <td>{p.name}</td>
                      <td><Badge value={p.status} /></td>
                      <td>{p.deadline || '—'}</td>
                      <td><Link to={`/projects/${p.id}`}><button className="btn-sm btn-primary">View</button></Link></td>
                    </tr>
                  ))}
                </tbody>
              </table>
              {data.totalPages > 1 && (
                <div className="pagination">
                  <button disabled={page === 0} onClick={() => load(page - 1)}>Previous</button>
                  <span className="page-info">Page {page + 1} of {data.totalPages}</span>
                  <button disabled={page + 1 >= data.totalPages} onClick={() => load(page + 1)}>Next</button>
                </div>
              )}
            </>
          )}
        </div>
      </div>

      {modal && (
        <div className="modal-overlay" onClick={() => setModal(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <h2>New Project</h2>
            <form onSubmit={save}>
              <div className="form-group">
                <label>Name</label>
                <input name="name" value={form.name} onChange={handle} required />
              </div>
              <div className="form-group">
                <label>Description</label>
                <textarea name="description" value={form.description} onChange={handle} rows={3} />
              </div>
              <div className="form-group">
                <label>Status</label>
                <select name="status" value={form.status} onChange={handle}>
                  <option value="PENDING">Pending</option>
                  <option value="IN_PROGRESS">In Progress</option>
                  <option value="COMPLETED">Completed</option>
                </select>
              </div>
              <div className="form-group">
                <label>Deadline</label>
                <input name="deadline" type="date" value={form.deadline} onChange={handle} />
              </div>
              {error && <p className="error-msg">{error}</p>}
              <div className="modal-actions">
                <button type="button" onClick={() => setModal(false)}>Cancel</button>
                <button className="btn-primary" type="submit" disabled={saving}>
                  {saving ? 'Saving...' : 'Create'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </>
  );
}
