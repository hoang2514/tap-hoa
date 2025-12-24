import React, { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import Message from '../components/Message.jsx';
import { createApi } from '../lib/api.js';

export default function ForgotPassword() {
  const api = useMemo(() => createApi({ getToken: () => null }), []);
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [msg, setMsg] = useState('');
  const [err, setErr] = useState('');

  async function handleSubmit(e) {
    e.preventDefault();
    setErr('');
    setMsg('');
    setLoading(true);
    try {
      const res = await api.post('/user/forgotPassword', { email });
      setMsg((res && res.message) || 'Vui lòng kiểm tra email của bạn.');
    } catch (e2) {
      setErr(e2.message || 'Không thể gửi yêu cầu');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="container" style={{ maxWidth: 420 }}>
      <div className="card">
        <h2 style={{ marginTop: 0 }}>Quên mật khẩu</h2>
        <Message type="success" text={msg} />
        <Message type="error" text={err} />

        <form onSubmit={handleSubmit}>
          <label className="label">Email</label>
          <input className="input" value={email} onChange={(e) => setEmail(e.target.value)} required />

          <div style={{ height: 14 }} />
          <button className="btn primary" type="submit" disabled={loading} style={{ width: '100%' }}>
            {loading ? 'Đang gửi...' : 'Gửi mật khẩu qua email'}
          </button>
        </form>

        <div className="hr" />
        <Link to="/login">Quay lại đăng nhập</Link>
      </div>
    </div>
  );
}
