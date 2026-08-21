import { Routes, Route } from 'react-router-dom';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Signup from './pages/Signup';

function App() {
  return (
    <Routes>
      <Route path="/" element={<div>Home (placeholder)</div>} />
      <Route path="/login" element={<Login />} />
      <Route path="/signup" element={<Signup />} />
      <Route path="/forgot-password" element={<div>Forgot Password (placeholder)</div>} />
      <Route path="/dashboard" element={<Dashboard />} />
    </Routes>
  );
}

export default App;