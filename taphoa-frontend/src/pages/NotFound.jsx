import React from 'react';
import { Link } from 'react-router-dom';

export default function NotFound() {
  return (
    <div className="card">
      <h2 style={{ marginTop: 0 }}>Không tìm thấy trang</h2>
      <p className="muted">Đường dẫn không tồn tại.</p>
      <Link className="btn" to="/">Về trang chủ</Link>
    </div>
  );
}
