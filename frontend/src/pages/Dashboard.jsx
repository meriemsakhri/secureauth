import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import * as authApi from '../api/authApi';

function Dashboard() {
  const { user, logout } = useAuth();
  const [me, setMe] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    authApi.getCurrentUser()
      .then((response) => setMe(response.data))
      .catch(() => setError('Could not load your profile.'));
  }, []);

  return (
    <div className="page">
      <div className="card">
        <h1>Dashboard</h1>
        <p>
          Welcome back, <strong>{user?.email}</strong>
        </p>
        <p>
          Role:{' '}
          <span className={`badge ${user?.role === 'ADMIN' ? 'badge-admin' : 'badge-user'}`}>
            {user?.role}
          </span>
        </p>

        {error && <p className="alert-error">{error}</p>}
        {me && (
          <div style={{ marginTop: '1.5rem', paddingTop: '1.5rem', borderTop: '1px solid var(--color-border)' }}>
            <h2>Live data from /api/users/me</h2>
            <p>Email: {me.email}</p>
            <p>Authorities: {me.authorities?.map((a) => a.authority).join(', ')}</p>
          </div>
        )}

        <button className="btn btn-secondary" onClick={logout} style={{ marginTop: '1rem' }}>
          Logout
        </button>
      </div>
    </div>
  );
}

export default Dashboard;