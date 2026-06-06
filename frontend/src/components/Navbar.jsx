import { Link, useNavigate } from 'react-router-dom';
import { isAdmin } from '../auth';

export default function Navbar() {
  const navigate = useNavigate();

  function logout() {
    localStorage.removeItem('token');
    navigate('/login');
  }

  return (
    <nav>
      <span className="brand">Client Portal</span>
      <div className="nav-links">
        <Link to="/">Dashboard</Link>
        <Link to="/projects">Projects</Link>
        {isAdmin() && <Link to="/admin">Admin</Link>}
        <button onClick={logout}>Logout</button>
      </div>
    </nav>
  );
}
