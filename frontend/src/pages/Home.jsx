import { Link } from 'react-router-dom';
import { ShieldCheck, KeyRound, UserCog, FileClock, Lock, RefreshCw } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

const features = [
  { icon: KeyRound, title: 'JWT Authentication', text: 'Short-lived access tokens with automatic refresh token rotation.' },
  { icon: UserCog, title: 'Role-Based Access', text: 'Fine-grained authorization for USER and ADMIN roles.' },
  { icon: Lock, title: 'Brute-Force Protection', text: 'Accounts lock automatically after repeated failed login attempts.' },
  { icon: ShieldCheck, title: 'BCrypt Hashing', text: 'Passwords are never stored or logged in plain text.' },
  { icon: RefreshCw, title: 'Password Recovery', text: 'Secure, single-use reset links delivered by email.' },
  { icon: FileClock, title: 'Security Audit Log', text: 'Every login, failure, and account change is tracked.' },
];

function Home() {
  const { user } = useAuth();

  return (
    <>
      <div className="hero">
        <h1>SecureAuth</h1>
        <p>
          A secure, reusable authentication and authorization module - built with Spring Boot,
          PostgreSQL, and modern security practices.
        </p>
        <div className="cta-group">
          {user ? (
            <Link to="/dashboard" className="btn btn-primary">Go to Dashboard</Link>
          ) : (
            <>
              <Link to="/signup" className="btn btn-primary">Get Started</Link>
              <Link to="/login" className="btn btn-secondary">Login</Link>
            </>
          )}
        </div>
      </div>

      <div className="feature-grid">
        {features.map(({ icon: Icon, title, text }) => (
          <div className="feature-card" key={title}>
            <Icon size={22} />
            <h3>{title}</h3>
            <p>{text}</p>
          </div>
        ))}
      </div>
    </>
  );
}

export default Home;