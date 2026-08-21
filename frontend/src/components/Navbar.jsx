import { useState, useRef, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

function Navbar() {
  const { user, logout } = useAuth();
  const [open, setOpen] = useState(false);
  const menuRef = useRef(null);

  useEffect(() => {
    function handleClickOutside(event) {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

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
            <button className="btn btn-secondary" onClick={logout}>Logout</button>
            <div className="account-menu" ref={menuRef}>
              <button className="account-trigger" onClick={() => setOpen((v) => !v)}>
                <div className="avatar" style={{ width: 32, height: 32, fontSize: '0.8rem' }}>
                  {user.email.charAt(0).toUpperCase()}
                </div>
              </button>
              {open && (
                <div className="account-dropdown">
                  <p className="account-dropdown-email" style={{ whiteSpace: 'nowrap' }}>{user.email}</p>
                  <span className={`badge ${user.role === 'ADMIN' ? 'badge-admin' : 'badge-user'}`}>
                    {user.role}
                  </span>
                </div>
              )}
            </div>
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