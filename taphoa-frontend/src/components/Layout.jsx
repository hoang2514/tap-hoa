import React from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import Navbar from './Navbar.jsx';

export default function Layout() {
  const loc = useLocation();
  const isAdminRoute = loc.pathname.startsWith('/admin');

  return (
    <>
      <Navbar />
      <div className="container">
        {isAdminRoute ? (
          <div className="muted small" style={{ marginBottom: 8 }}>
            Khu vực quản trị
          </div>
        ) : null}
        <Outlet />
      </div>
    </>
  );
}
