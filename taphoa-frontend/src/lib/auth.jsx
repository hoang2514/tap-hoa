import React, { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { storage } from './storage.js';
import { decodeJwt, getEmailFromToken, getRoleFromToken, isTokenExpired } from './jwt.js';

const TOKEN_KEY = 'taphoa_token';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => storage.get(TOKEN_KEY));
  const [role, setRole] = useState(() => (token ? getRoleFromToken(token) : null));
  const [email, setEmail] = useState(() => (token ? getEmailFromToken(token) : null));

  function logout() {
    storage.remove(TOKEN_KEY);
    setToken(null);
    setRole(null);
    setEmail(null);
  }

  function login(newToken) {
    storage.set(TOKEN_KEY, newToken);
    setToken(newToken);
    setRole(getRoleFromToken(newToken));
    setEmail(getEmailFromToken(newToken));
  }

  useEffect(() => {
    if (!token) return;
    if (isTokenExpired(token)) {
      logout();
      return;
    }

    const t = setInterval(() => {
      const current = storage.get(TOKEN_KEY);
      if (current && isTokenExpired(current)) {
        logout();
      }
    }, 30000);

    return () => clearInterval(t);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token]);

  const value = useMemo(
    () => ({
      token,
      role,
      email,
      isAuthenticated: Boolean(token) && !isTokenExpired(token),
      login,
      logout,
      decode: () => (token ? decodeJwt(token) : null)
    }),
    [token, role, email]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth phải được dùng trong AuthProvider');
  return ctx;
}
