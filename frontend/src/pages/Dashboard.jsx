import { Link } from 'react-router-dom';
import { UserCircle, ShieldCheck, KeyRound } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

function initials(email) {
  return email ? email.charAt(0).toUpperCase() : '?';
}

function Dashboard() {
  const { user } = useAuth();

  return (
    <div className="page-wide">
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '2rem' }}>
        <div className="avatar avatar-lg">{initials(user?.email)}</div>
        <div>
          <h1 style={{ marginBottom: '0.15rem' }}>Welcome back</h1>
          <p style={{ margin: 0 }}>{user?.email}</p>
        </div>
        <span
          className={`badge ${user?.role === 'ADMIN' ? 'badge-admin' : 'badge-user'}`}
          style={{ marginLeft: 'auto' }}
        >
          {user?.role}
        </span>
      </div>

      <div className="feature-grid" style={{ margin: 0 }}>
        <Link to="/profile" className="feature-card" style={{ display: 'block', color: 'inherit' }}>
          <UserCircle size={22} />
          <h3>Your Profile</h3>
          <p>View your account details and change your password.</p>
        </Link>

        <div className="feature-card">
          <ShieldCheck size={22} />
          <h3>Account Security</h3>
          <p>Your session is protected by short-lived JWT access tokens.</p>
        </div>

        {user?.role === 'ADMIN' && (
          <Link to="/admin" className="feature-card" style={{ display: 'block', color: 'inherit' }}>
            <KeyRound size={22} />
            <h3>Admin Panel</h3>
            <p>Manage all user accounts and permissions.</p>
          </Link>
        )}
      </div>
    </div>
  );
}

export default Dashboard;