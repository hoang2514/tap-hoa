import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../lib/auth.jsx';

export default function ProtectedRoute({ children, requireRole }) {
  const auth = useAuth();
  const loc = useLocation();

  if (!auth.isAuthenticated) {
    // chưa đăng nhập -> luôn về /login
    return <Navigate to="/login" replace state={{ from: loc.pathname }} />;
  }

  if (requireRole && auth.role !== requireRole) {
    // Sai role -> đưa về trang chủ
    return <Navigate to="/" replace />;
  }

  return children;
}
