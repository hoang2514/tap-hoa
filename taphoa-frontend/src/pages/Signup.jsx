import React, { useMemo, useState, useEffect } from 'react';
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
  const [otp, setOtp] = useState('');

  const [loading, setLoading] = useState(false);
  const [verifying, setVerifying] = useState(false);
  const [resending, setResending] = useState(false);
  const [msg, setMsg] = useState('');
  const [err, setErr] = useState('');
  const [showOtpForm, setShowOtpForm] = useState(false);
  const [timeLeft, setTimeLeft] = useState(30);
  const [canResend, setCanResend] = useState(false);

  useEffect(() => {
    if (showOtpForm && timeLeft > 0) {
      const timer = setInterval(() => {
        setTimeLeft((prev) => {
          if (prev <= 1) {
            setCanResend(true);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);

      return () => clearInterval(timer);
    }
  }, [showOtpForm, timeLeft]);

  async function handleSubmit(e) {
    e.preventDefault();
    setErr('');
    setMsg('');
    setLoading(true);
    try {
      const res = await api.post('/user/signup', { name, contactNumber, email, password });

      setMsg((res && res.message) || 'OTP đã được gửi đến email của bạn.');
      setShowOtpForm(true);
      setTimeLeft(30);
      setCanResend(false);
    } catch (e2) {
      setErr(e2.message || 'Đăng ký thất bại');
    } finally {
      setLoading(false);
    }
  }

  async function handleVerifyOTP(e) {
    e.preventDefault();
    setErr('');
    setMsg('');
    setVerifying(true);
    try {
      const res = await api.post('/user/verifyOTP', { email, otp });

      setMsg((res && res.message) || 'Đăng ký thành công.');
      setTimeout(() => nav('/login', { replace: true }), 2000);
    } catch (e2) {
      setErr(e2.message || 'Mã OTP không đúng hoặc đã hết hạn');
    } finally {
      setVerifying(false);
    }
  }

  async function handleResendOTP() {
    setErr('');
    setMsg('');
    setResending(true);
    try {
      const res = await api.post('/user/resendOTP', { email });

      setMsg((res && res.message) || 'Mã OTP mới đã được gửi.');
      setTimeLeft(30);
      setCanResend(false);
      setOtp('');
    } catch (e2) {
      setErr(e2.message || 'Không thể gửi lại mã OTP');
    } finally {
      setResending(false);
    }
  }

  if (showOtpForm) {
    return (
      <div className="container" style={{ maxWidth: 460 }}>
        <div className="card">
          <h2 style={{ marginTop: 0 }}>Xác thực OTP</h2>
          <Message type="success" text={msg} />
          <Message type="error" text={err} />

          <form onSubmit={handleVerifyOTP}>
            <label className="label">Nhập mã OTP</label>
            <input
              className="input"
              value={otp}
              onChange={(e) => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
              placeholder="Nhập 6 chữ số"
              maxLength={6}
              required
            />

            <div style={{ height: 10 }} />
            {timeLeft > 0 ? (
              <div style={{ textAlign: 'center', color: '#666', fontSize: '14px' }}>
                Mã OTP còn hiệu lực trong: <strong>{timeLeft}</strong> giây
              </div>
            ) : (
              <div style={{ textAlign: 'center' }}>
                <button
                  type="button"
                  onClick={handleResendOTP}
                  disabled={resending}
                  className="btn"
                  style={{
                    background: 'transparent',
                    border: 'none',
                    color: '#007bff',
                    textDecoration: 'underline',
                    cursor: resending ? 'not-allowed' : 'pointer',
                    padding: 0
                  }}
                >
                  {resending ? 'Đang gửi...' : 'Gửi lại mã?'}
                </button>
              </div>
            )}

            <div style={{ height: 14 }} />
            <button className="btn primary" type="submit" disabled={verifying} style={{ width: '100%' }}>
              {verifying ? 'Đang xác thực...' : 'Xác thực'}
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