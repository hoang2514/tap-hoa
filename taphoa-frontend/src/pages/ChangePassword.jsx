import React, { useMemo, useState } from 'react';
import Message from '../components/Message.jsx';
import { createApi } from '../lib/api.js';
import { useAuth } from '../lib/auth.jsx';

export default function ChangePassword() {
  const auth = useAuth();
  const api = useMemo(
    () =>
      createApi({
        getToken: () => auth.token,
        onUnauthorized: () => auth.logout()
      }),
    [auth]
  );

  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [msg, setMsg] = useState('');
  const [err, setErr] = useState('');

  async function handleSubmit(e) {
    e.preventDefault();
    setErr('');
    setMsg('');
    setLoading(true);
    try {
      const res = await api.post('/user/changePassword', {
        email: auth.email,
        oldPassword,
        newPassword
      });
      setMsg((res && res.message) || 'Đổi mật khẩu thành công');
      setOldPassword('');
      setNewPassword('');
    } catch (e2) {
      setErr(e2.message || 'Đổi mật khẩu thất bại');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="card" style={{ maxWidth: 520 }}>
      <h2 style={{ marginTop: 0 }}>Đổi mật khẩu</h2>
      <div className="muted small" style={{ marginBottom: 10 }}>
        Tài khoản: <b>{auth.email}</b>
      </div>
      <Message type="success" text={msg} />
      <Message type="error" text={err} />

      <form onSubmit={handleSubmit}>
        <label className="label">Mật khẩu cũ</label>
        <input
          className="input"
          type="password"
          value={oldPassword}
          onChange={(e) => setOldPassword(e.target.value)}
          required
        />

        <div style={{ height: 10 }} />
        <label className="label">Mật khẩu mới</label>
        <input
          className="input"
          type="password"
          value={newPassword}
          onChange={(e) => setNewPassword(e.target.value)}
          required
        />

        <div style={{ height: 14 }} />
        <button className="btn primary" type="submit" disabled={loading}>
          {loading ? 'Đang xử lý...' : 'Đổi mật khẩu'}
        </button>
      </form>
    </div>
  );
}
