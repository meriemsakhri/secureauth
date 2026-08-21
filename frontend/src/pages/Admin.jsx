import { useEffect, useState } from 'react';
import * as authApi from '../api/authApi';

function Admin() {
  const [users, setUsers] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    authApi.getAllUsers()
      .then((response) => setUsers(response.data))
      .catch(() => setError('Could not load users.'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p>Loading users...</p>;

  return (
    <div>
      <h1>Admin — All Users</h1>
      {error && <p style={{ color: 'red' }}>{error}</p>}
      {!error && (
        <table border="1" cellPadding="8">
          <thead>
            <tr>
              <th>Email</th>
              <th>Enabled</th>
              <th>Failed Attempts</th>
            </tr>
          </thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.id}>
                <td>{u.email}</td>
                <td>{u.enabled ? 'Yes' : 'No'}</td>
                <td>{u.failedAttempts}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

export default Admin;