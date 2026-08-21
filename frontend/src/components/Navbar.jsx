import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

function Navbar() {
  const { user, logout } = useAuth();

  return (
    <nav className="navbar">
      <span className="navbar-brand">SecureAuth</span>
      <Link to="/">Home</Link>

      {user ? (
        <>
          <Link to="/dashboard">Dashboard</Link>
          <Link to="/profile">Profile</Link>
          {user.role === 'ADMIN' && <Link to="/admin">Admin</Link>}
          <div className="navbar-spacer">
            <span className={`badge ${user.role === 'ADMIN' ? 'badge-admin' : 'badge-user'}`}>
              {user.role}
            </span>
            <span className="navbar-email">{user.email}</span>
            <button className="btn btn-secondary" onClick={logout}>Logout</button>
          </div>
        </>
      ) : (
        <div className="navbar-spacer">
          <Link to="/login">Login</Link>
          <Link to="/signup">Signup</Link>
        </div>
      )}
    </nav>
  );
}

export default Navbar;