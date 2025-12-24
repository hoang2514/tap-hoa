import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../lib/auth.jsx';
import { useCart } from '../lib/cart.jsx';

export default function Navbar() {
  const auth = useAuth();
  const cart = useCart();
  const nav = useNavigate();

  const count = cart.items.reduce((s, it) => s + (Number(it.quantity) || 0), 0);

  return (
    <div className="navbar">
      <div className="inner">
        <div className="row" style={{ alignItems: 'center' }}>
          <Link to={auth.isAuthenticated ? '/' : '/login'} style={{ fontWeight: 700 }}>
            Tạp Hóa
          </Link>
          {auth.isAuthenticated ? (
            <span className="badge">{auth.role}</span>
          ) : null}
        </div>

        {!auth.isAuthenticated ? (
          <div className="row">
            <Link className="btn" to="/login">Đăng nhập</Link>
            <Link className="btn primary" to="/signup">Đăng ký</Link>
          </div>
        ) : (
          <div className="row" style={{ alignItems: 'center' }}>
            {auth.role === 'ADMIN' ? (
              <>
                <Link className="btn" to="/admin/dashboard">Quản trị</Link>
                <Link className="btn" to="/change-password">Đổi mật khẩu</Link>
              </>
            ) : (
              <>
                <Link className="btn" to="/">Trang chủ</Link>
                <Link className="btn" to="/cart">Giỏ hàng ({count})</Link>
                <Link className="btn" to="/change-password">Đổi mật khẩu</Link>
              </>
            )}

            <button
              className="btn"
              onClick={() => {
                auth.logout();
                nav('/login', { replace: true });
              }}
            >
              Đăng xuất
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
