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
            Quản lý cửa hàng
          </div>
        ) : null}
        <Outlet />
      </div>
      <footer className="footer">
  <div className="inner">
    <div>
      <h4>Về chúng tôi</h4>
      <p>Giới thiệu cửa hàng</p>
      <p>Danh sách cửa hàng</p>
      <p>Quản lý chất lượng</p>
    </div>

    <div>
      <h4>Hỗ trợ khách hàng</h4>
      <p>Hướng dẫn mua hàng</p>
      <p>Chính sách đổi trả</p>
      <p>Liên hệ hỗ trợ</p>
    </div>

    <div>
      <h4>Chăm sóc khách hàng</h4>
      <p>Dịch vụ giao hàng</p>
      <p>Chăm sóc sau bán</p>
      <p>Phản hồi khách hàng</p>
    </div>

    <div className="copyright">
      © 2025 Tạp hóa. Tất cả quyền được bảo lưu.
    </div>
  </div>
</footer>

    </>
  );
}
