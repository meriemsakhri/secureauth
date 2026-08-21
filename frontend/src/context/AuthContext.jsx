import { createContext, useContext, useState } from 'react';
import * as authApi from '../api/authApi';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const email = localStorage.getItem('email');
    const role = localStorage.getItem('role');
    return email && role ? { email, role } : null;
  });

  const login = async (email, password) => {
    const response = await authApi.login(email, password);
    const { accessToken, refreshToken, role } = response.data;

    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
    localStorage.setItem('email', email);
    localStorage.setItem('role', role);

    setUser({ email, role });
  };

  const logout = async () => {
    const refreshToken = localStorage.getItem('refreshToken');
    try {
      if (refreshToken) {
        await authApi.logout(refreshToken);
      }
    } finally {
      localStorage.clear();
      setUser(null);
    }
  };

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}