import React, { useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Message from '../components/Message.jsx';
import { createApi } from '../lib/api.js';

export default function Signup() {
  const api = useMemo(() => createApi({ getToken: () => null }), []);
  const nav = useNavigate();

  const [name, setName] = useState('');
  const [contactNumber, setContactNumber] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const [loading, setLoading] = useState(false);
  const [msg, setMsg] = useState('');
  const [err, setErr] = useState('');

  async function handleSubmit(e) {
    e.preventDefault();
    setErr('');
    setMsg('');
    setLoading(true);
    try {
      const res = await api.post('/user/signup', { name, contactNumber, email, password });

      setMsg((res && res.message) || 'Đăng ký thành công. Vui lòng chờ admin duyệt (nếu có).');
      setTimeout(() => nav('/login', { replace: true }), 500);
    } catch (e2) {
      setErr(e2.message || 'Đăng ký thất bại');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="container" style={{ maxWidth: 460 }}>
      <div className="card">
        <h2 style={{ marginTop: 0 }}>Đăng ký</h2>
        <Message type="success" text={msg} />
        <Message type="error" text={err} />

        <form onSubmit={handleSubmit}>
          <label className="label">Họ và tên</label>
          <input className="input" value={name} onChange={(e) => setName(e.target.value)} required />

          <div style={{ height: 10 }} />
          <label className="label">Số điện thoại</label>
          <input className="input" value={contactNumber} onChange={(e) => setContactNumber(e.target.value)} required />

          <div style={{ height: 10 }} />
          <label className="label">Email</label>
          <input className="input" value={email} onChange={(e) => setEmail(e.target.value)} required />

          <div style={{ height: 10 }} />
          <label className="label">Mật khẩu</label>
          <input className="input" type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />

          <div style={{ height: 14 }} />
          <button className="btn primary" type="submit" disabled={loading} style={{ width: '100%' }}>
            {loading ? 'Đang đăng ký...' : 'Đăng ký'}
          </button>
        </form>

        <div className="hr" />
        <div className="row" style={{ justifyContent: 'space-between' }}>
          <span className="muted">Đã có tài khoản?</span>
          <Link to="/login">Đăng nhập</Link>
        </div>
      </div>
    </div>
  );
}
