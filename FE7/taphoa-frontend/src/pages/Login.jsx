import React, { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import Message from '../components/Message.jsx';
import { useAuth } from '../lib/auth.jsx';
import { getRoleFromToken } from '../lib/jwt.js';

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080';

export default function Login() {
  const auth = useAuth();
  const nav = useNavigate();
  const loc = useLocation();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const res = await fetch(`${API_BASE}/user/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          email,
          password,
        }),
      });

      if (!res.ok) {
        throw new Error('Login failed');
      }

      const data = await res.json();

      // lưu token
      auth.login(data.token);

      // decode role
      const role = getRoleFromToken(data.token);

      // điều hướng theo role
      const target = role === 'ADMIN' ? '/admin/dashboard' : '/';
      const from = loc.state?.from || null;

      nav(from || target, { replace: true });
    } catch (err) {
      setError('Đăng nhập thất bại');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="container" style={{ maxWidth: 420 }}>
      <div className="card">
        <h2 style={{ marginTop: 0 }}>Đăng nhập</h2>

        <Message type="error" text={error} />

        <form onSubmit={handleSubmit}>
          <label className="label">Email</label>
          <input
            className="input"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />

          <div style={{ height: 10 }} />

          <label className="label">Mật khẩu</label>
          <input
            className="input"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />

          <div style={{ height: 14 }} />

          <button
            className="btn primary"
            type="submit"
            disabled={loading}
            style={{ width: '100%' }}
          >
            {loading ? 'Đang đăng nhập...' : 'Đăng nhập'}
          </button>
        </form>

        <div className="hr" />

        <div className="row" style={{ justifyContent: 'space-between' }}>
          <Link to="/signup">Đăng ký</Link>
          <Link to="/forgot-password">Quên mật khẩu</Link>
        </div>
      </div>
    </div>
  );
}
//