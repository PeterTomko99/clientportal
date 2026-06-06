import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import api from '../api';

function Badge({ value }) {
  return <span className={`badge badge-${value.toLowerCase()}`}>{value}</span>;
}

const emptyInvoice = { amount: '', dueDate: '', status: 'UNPAID' };

export default function ProjectDetail() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [project, setProject] = useState(null);
  const [invoices, setInvoices] = useState([]);
  const [files, setFiles] = useState([]);
  const [tab, setTab] = useState('invoices');
  const [loading, setLoading] = useState(true);

  const [invoiceModal, setInvoiceModal] = useState(false);
  const [invoiceForm, setInvoiceForm] = useState(emptyInvoice);
  const [savingInvoice, setSavingInvoice] = useState(false);
  const [invoiceError, setInvoiceError] = useState('');

  const [editModal, setEditModal] = useState(false);
  const [editForm, setEditForm] = useState({});
  const [savingEdit, setSavingEdit] = useState(false);

  const [uploading, setUploading] = useState(false);
  const [uploadError, setUploadError] = useState('');

  useEffect(() => { loadAll(); }, [id]);

  async function loadAll() {
    setLoading(true);
    try {
      const [pRes, iRes, fRes] = await Promise.all([
        api.get(`/api/projects/${id}`),
        api.get(`/api/projects/${id}/invoices?size=50`),
        api.get(`/api/projects/${id}/files`),
      ]);
      setProject(pRes.data);
      setInvoices(iRes.data.content || []);
      setFiles(fRes.data);
    } finally {
      setLoading(false);
    }
  }

  function handleInvoice(e) { setInvoiceForm({ ...invoiceForm, [e.target.name]: e.target.value }); }
  function handleEdit(e)    { setEditForm({ ...editForm, [e.target.name]: e.target.value }); }

  async function saveInvoice(e) {
    e.preventDefault();
    setInvoiceError('');
    setSavingInvoice(true);
    try {
      await api.post(`/api/projects/${id}/invoices`, invoiceForm);
      setInvoiceModal(false);
      setInvoiceForm(emptyInvoice);
      const { data } = await api.get(`/api/projects/${id}/invoices?size=50`);
      setInvoices(data.content || []);
    } catch (err) {
      setInvoiceError(err.response?.data?.message || 'Failed to create invoice.');
    } finally {
      setSavingInvoice(false);
    }
  }

  async function deleteInvoice(invoiceId) {
    if (!confirm('Delete this invoice?')) return;
    await api.delete(`/api/projects/${id}/invoices/${invoiceId}`);
    setInvoices(invoices.filter(i => i.id !== invoiceId));
  }

  function downloadPdf(invoiceId) {
    const token = localStorage.getItem('token');
    const url = `${import.meta.env.VITE_API_URL || ''}/api/projects/${id}/invoices/${invoiceId}/pdf`;
    const a = document.createElement('a');
    a.href = url;
    a.download = `invoice-${invoiceId}.pdf`;
    // use fetch to attach auth header
    fetch(url, { headers: { Authorization: `Bearer ${token}` } })
      .then(r => r.blob())
      .then(blob => {
        a.href = URL.createObjectURL(blob);
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
      });
  }

  function openEdit() {
    setEditForm({ name: project.name, description: project.description || '', status: project.status, deadline: project.deadline || '' });
    setEditModal(true);
  }

  async function saveEdit(e) {
    e.preventDefault();
    setSavingEdit(true);
    try {
      const { data } = await api.put(`/api/projects/${id}`, editForm);
      setProject(data);
      setEditModal(false);
    } finally {
      setSavingEdit(false);
    }
  }

  async function deleteProject() {
    if (!confirm('Delete this project and all its data?')) return;
    await api.delete(`/api/projects/${id}`);
    navigate('/projects');
  }

  async function uploadFile(e) {
    const file = e.target.files[0];
    if (!file) return;
    setUploadError('');
    setUploading(true);
    const form = new FormData();
    form.append('file', file);
    try {
      await api.post(`/api/projects/${id}/files`, form);
      const { data } = await api.get(`/api/projects/${id}/files`);
      setFiles(data);
    } catch (err) {
      setUploadError(err.response?.data?.message || 'Upload failed.');
    } finally {
      setUploading(false);
      e.target.value = '';
    }
  }

  async function deleteFile(fileId) {
    if (!confirm('Delete this file?')) return;
    await api.delete(`/api/projects/${id}/files/${fileId}`);
    setFiles(files.filter(f => f.id !== fileId));
  }

  function downloadFile(fileId, fileName) {
    const token = localStorage.getItem('token');
    const url = `${import.meta.env.VITE_API_URL || ''}/api/projects/${id}/files/${fileId}/download`;
    fetch(url, { headers: { Authorization: `Bearer ${token}` } })
      .then(r => r.blob())
      .then(blob => {
        const a = document.createElement('a');
        a.href = URL.createObjectURL(blob);
        a.download = fileName;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
      });
  }

  if (loading) return <><Navbar /><div className="page"><p className="empty">Loading...</p></div></>;
  if (!project) return <><Navbar /><div className="page"><p className="empty">Project not found.</p></div></>;

  return (
    <>
      <Navbar />
      <div className="page">
        <div className="page-header">
          <div>
            <h1>{project.name}</h1>
            <div style={{ marginTop: 6, display: 'flex', gap: 12, alignItems: 'center' }}>
              <Badge value={project.status} />
              {project.deadline && <span style={{ fontSize: 13, color: '#888' }}>Due {project.deadline}</span>}
            </div>
          </div>
          <div style={{ display: 'flex', gap: 8 }}>
            <button className="btn-sm btn-primary" onClick={openEdit}>Edit</button>
            <button className="btn-sm btn-danger" onClick={deleteProject}>Delete</button>
          </div>
        </div>

        {project.description && (
          <div className="card" style={{ marginBottom: 24 }}>
            <p style={{ fontSize: 14, color: '#555', lineHeight: 1.6 }}>{project.description}</p>
          </div>
        )}

        <div className="tabs">
          <div className={`tab ${tab === 'invoices' ? 'active' : ''}`} onClick={() => setTab('invoices')}>
            Invoices ({invoices.length})
          </div>
          <div className={`tab ${tab === 'files' ? 'active' : ''}`} onClick={() => setTab('files')}>
            Files ({files.length})
          </div>
        </div>

        {tab === 'invoices' && (
          <div className="card">
            <div className="page-header" style={{ marginBottom: 16 }}>
              <h1 style={{ fontSize: 16 }}>Invoices</h1>
              <button className="btn-sm btn-primary" onClick={() => { setInvoiceModal(true); setInvoiceForm(emptyInvoice); setInvoiceError(''); }}>
                + Add Invoice
              </button>
            </div>
            {invoices.length === 0 ? (
              <p className="empty">No invoices yet.</p>
            ) : (
              <table>
                <thead>
                  <tr><th>Amount</th><th>Due Date</th><th>Status</th><th></th></tr>
                </thead>
                <tbody>
                  {invoices.map(inv => (
                    <tr key={inv.id}>
                      <td>${Number(inv.amount).toFixed(2)}</td>
                      <td>{inv.dueDate}</td>
                      <td><Badge value={inv.status} /></td>
                      <td>
                        <div style={{ display: 'flex', gap: 6 }}>
                          <button className="btn-sm btn-primary" onClick={() => downloadPdf(inv.id)}>PDF</button>
                          <button className="btn-sm btn-danger" onClick={() => deleteInvoice(inv.id)}>Delete</button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}

        {tab === 'files' && (
          <div className="card">
            <div className="page-header" style={{ marginBottom: 16 }}>
              <h1 style={{ fontSize: 16 }}>Files</h1>
              <label className="btn-sm btn-primary" style={{ cursor: 'pointer' }}>
                {uploading ? 'Uploading...' : '+ Upload File'}
                <input type="file" style={{ display: 'none' }} onChange={uploadFile} disabled={uploading} />
              </label>
            </div>
            {uploadError && <p className="error-msg" style={{ marginBottom: 12 }}>{uploadError}</p>}
            {files.length === 0 ? (
              <p className="empty">No files uploaded yet.</p>
            ) : (
              <table>
                <thead>
                  <tr><th>File Name</th><th>Uploaded</th><th></th></tr>
                </thead>
                <tbody>
                  {files.map(f => (
                    <tr key={f.id}>
                      <td>{f.fileName}</td>
                      <td>{f.uploadedAt ? new Date(f.uploadedAt).toLocaleDateString() : '—'}</td>
                      <td>
                        <div style={{ display: 'flex', gap: 6 }}>
                          <button className="btn-sm btn-primary" onClick={() => downloadFile(f.id, f.fileName)}>Download</button>
                          <button className="btn-sm btn-danger" onClick={() => deleteFile(f.id)}>Delete</button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}
      </div>

      {invoiceModal && (
        <div className="modal-overlay" onClick={() => setInvoiceModal(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <h2>New Invoice</h2>
            <form onSubmit={saveInvoice}>
              <div className="form-group">
                <label>Amount ($)</label>
                <input name="amount" type="number" step="0.01" min="0" value={invoiceForm.amount} onChange={handleInvoice} required />
              </div>
              <div className="form-group">
                <label>Due Date</label>
                <input name="dueDate" type="date" value={invoiceForm.dueDate} onChange={handleInvoice} required />
              </div>
              <div className="form-group">
                <label>Status</label>
                <select name="status" value={invoiceForm.status} onChange={handleInvoice}>
                  <option value="UNPAID">Unpaid</option>
                  <option value="PAID">Paid</option>
                </select>
              </div>
              {invoiceError && <p className="error-msg">{invoiceError}</p>}
              <div className="modal-actions">
                <button type="button" onClick={() => setInvoiceModal(false)}>Cancel</button>
                <button className="btn-primary" type="submit" disabled={savingInvoice}>
                  {savingInvoice ? 'Saving...' : 'Create'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {editModal && (
        <div className="modal-overlay" onClick={() => setEditModal(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <h2>Edit Project</h2>
            <form onSubmit={saveEdit}>
              <div className="form-group">
                <label>Name</label>
                <input name="name" value={editForm.name} onChange={handleEdit} required />
              </div>
              <div className="form-group">
                <label>Description</label>
                <textarea name="description" value={editForm.description} onChange={handleEdit} rows={3} />
              </div>
              <div className="form-group">
                <label>Status</label>
                <select name="status" value={editForm.status} onChange={handleEdit}>
                  <option value="PENDING">Pending</option>
                  <option value="IN_PROGRESS">In Progress</option>
                  <option value="COMPLETED">Completed</option>
                </select>
              </div>
              <div className="form-group">
                <label>Deadline</label>
                <input name="deadline" type="date" value={editForm.deadline} onChange={handleEdit} />
              </div>
              <div className="modal-actions">
                <button type="button" onClick={() => setEditModal(false)}>Cancel</button>
                <button className="btn-primary" type="submit" disabled={savingEdit}>
                  {savingEdit ? 'Saving...' : 'Save'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </>
  );
}
