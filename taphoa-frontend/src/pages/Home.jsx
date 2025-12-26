import React, { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import Loader from '../components/Loader.jsx';
import Message from '../components/Message.jsx';
import { createApi } from '../lib/api.js';
import { useAuth } from '../lib/auth.jsx';
import { useCart } from '../lib/cart.jsx';

export default function Home() {
  const auth = useAuth();
  const cart = useCart();
  const api = useMemo(
    () =>
      createApi({
        getToken: () => auth.token,
        onUnauthorized: () => auth.logout()
      }),
    [auth]
  );

  const [categories, setCategories] = useState([]);
  const [products, setProducts] = useState([]);
  const [categoryId, setCategoryId] = useState('');
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState('');
  const [msg, setMsg] = useState('');

  async function load() {
    setLoading(true);
    setErr('');
    try {
      const [cats, prods] = await Promise.all([api.get('/category/get'), api.get('/product/get')]);
      setCategories(Array.isArray(cats) ? cats : []);
      setProducts(Array.isArray(prods) ? prods : []);
    } catch (e) {
      setErr(e.message || 'Không thể tải dữ liệu');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const filtered = categoryId
    ? products.filter((p) => String(p.categoryId) === String(categoryId))
    : products;

  return (
    <div>
      <h2 style={{ marginTop: 0 }}>Danh sách sản phẩm</h2>
      <Message type="success" text={msg} />
      <Message type="error" text={err} />

      <div className="card" style={{ marginBottom: 12 }}>
        <div className="row" style={{ alignItems: 'center', justifyContent: 'space-between' }}>
          <div>
            <div className="label">Lọc theo danh mục</div>
            <select className="input" value={categoryId} onChange={(e) => setCategoryId(e.target.value)}>
              <option value="">Tất cả danh mục</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </div>
          <div style={{ alignSelf: 'end' }}>
            <button className="btn" onClick={load} disabled={loading}>Tải lại</button>
          </div>
        </div>
      </div>

      {loading ? <Loader /> : null}

      <div className="grid">
        {filtered.map((p) => (
          <div className="card" key={p.id}>
            {p.imageUrl ? (
              <img
                src={p.imageUrl}
                alt={p.name}
                style={{
                  width: '100%',
                  height: 160,
                  objectFit: 'cover',
                  borderRadius: 6,
                  marginBottom: 8
                }}
              />
            ) : null}
            <div style={{ fontWeight: 700, marginBottom: 6 }}>{p.name}</div>
            <div className="muted small" style={{ marginBottom: 8 }}>
              {p.categoryName ? `Danh mục: ${p.categoryName}` : ''}
            </div>
            <div style={{ marginBottom: 10 }}>{p.description}</div>
            <div className="row" style={{ alignItems: 'center', justifyContent: 'space-between' }}>
              <div style={{ fontWeight: 700 }}>{Number(p.price).toLocaleString('vi-VN')} ₫</div>
              <div className="row">
                <Link className="btn" to={`/product/${p.id}`}>Chi tiết</Link>
                <button
                  className="btn primary"
                  onClick={async () => {
                    try {
                      await cart.add(p.id, 1);
                      setMsg('Đã thêm vào giỏ hàng');
                      setTimeout(() => setMsg(''), 1200);
                    } catch (e) {
                      setErr(e.message || 'Không thể thêm vào giỏ hàng');
                    }
                  }}
                >
                  Thêm giỏ
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>

      {!loading && filtered.length === 0 ? <div className="muted">Không có sản phẩm.</div> : null}
    </div>
  );
}
