import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

function Home() {
  const { user } = useAuth();

  return (
    <div className="page">
      <div className="card">
        <h1>SecureAuth</h1>
        <p>A secure, reusable authentication and authorization module.</p>
        {user ? (
          <Link to="/dashboard">Go to Dashboard →</Link>
        ) : (
          <p className="helper-text">
            <Link to="/login">Login</Link> or <Link to="/signup">Sign up</Link> to get started.
          </p>
        )}
      </div>
    </div>
  );
}

export default Home;