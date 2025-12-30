import React from 'react';
import { NavLink } from 'react-router-dom';

export default function AdminNav() {
  const linkStyle = ({ isActive }) => ({
    padding: '6px 10px',
    borderRadius: 8,
    border: '1px solid #e7e9ee',
    background: isActive ? '#ed1c24' : '#FFFFFF',
    color: isActive ? '#FFFFFF' : '#ed1c24'
  });

  return (
    <div className="row" style={{ marginBottom: 12 }}>
      <NavLink to="/admin/dashboard" style={linkStyle}>Dashboard</NavLink>
      <NavLink to="/admin/categories" style={linkStyle}>Danh mục</NavLink>
      <NavLink to="/admin/products" style={linkStyle}>Sản phẩm</NavLink>
      <NavLink to="/admin/bills" style={linkStyle}>Hóa đơn</NavLink>
    </div>
  );
}
