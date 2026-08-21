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
    <div>
      <h1>Dashboard</h1>
      <p>Welcome, {user?.email}</p>
      <p>Role: {user?.role}</p>

      {error && <p style={{ color: 'red' }}>{error}</p>}
      {me && (
        <div>
          <h3>Data from /api/users/me:</h3>
          <p>Email: {me.email}</p>
          <p>Authorities: {me.authorities?.map((a) => a.authority).join(', ')}</p>
        </div>
      )}

      <button onClick={logout}>Logout</button>
    </div>
  );
}

export default Dashboard;