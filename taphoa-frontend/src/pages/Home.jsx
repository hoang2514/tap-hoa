import React, { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import Loader from '../components/Loader.jsx';
import Message from '../components/Message.jsx';
import { createApi } from '../lib/api.js';
import { useAuth } from '../lib/auth.jsx';

export default function Home() {
  const auth = useAuth();
  const api = useMemo(
    () =>
      createApi({
        getToken: () => auth.token,
        onUnauthorized: () => auth.logout()
      }),
    [auth]
  );

  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState('');

  async function load() {
    setLoading(true);
    setErr('');
    try {
      const cats = await api.get('/category/get');
      setCategories(Array.isArray(cats) ? cats : []);
    } catch (e) {
      setErr(e.message || 'Không thể tải danh mục');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  return (
    <div>
      <img src="/landing.jpg" alt="Landing" style={{ width: '100%', height: 'auto', marginBottom: 20 }} />
      <h2 style={{ marginTop: 0 }}>Danh mục sản phẩm</h2>
      <Message type="error" text={err} />

      <div style={{ marginBottom: 12, display: 'flex', justifyContent: 'flex-end' }}>
        <button className="btn" onClick={load} disabled={loading}>Tải lại</button>
      </div>

      {loading ? <Loader /> : null}

      <div className="grid">
        {categories.map((c) => (
          <div className="card" key={c.id}>
            <div style={{ padding: 20, textAlign: 'center' }}>
              <h3 style={{ margin: '0 0 10px 0' }}>{c.name}</h3>
              <Link className="btn primary" style={{ display: 'block', width: '100%' }} to={`/category/${c.id}`}>
                Xem sản phẩm
              </Link>
            </div>
          </div>
        ))}
      </div>

      {!loading && categories.length === 0 ? <div className="muted">Không có danh mục nào.</div> : null}
    </div>
  );
}