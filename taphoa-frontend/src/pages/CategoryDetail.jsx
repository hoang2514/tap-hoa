import React, { useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import Loader from '../components/Loader.jsx';
import Message from '../components/Message.jsx';
import { createApi } from '../lib/api.js';
import { useAuth } from '../lib/auth.jsx';
import { useCart } from '../lib/cart.jsx';

export default function CategoryDetail() {
  const { id } = useParams();
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

  const [products, setProducts] = useState([]);
  const [categoryName, setCategoryName] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState('');
  const [msg, setMsg] = useState('');

  async function load() {
    setLoading(true);
    setErr('');
    try {
      const [cats, prods] = await Promise.all([api.get('/category/get'), api.get('/product/get')]);

      const currentCategory = Array.isArray(cats) ? cats.find(c => String(c.id) === String(id)) : null;
      if (currentCategory) {
        setCategoryName(currentCategory.name);
      }

      const allProducts = Array.isArray(prods) ? prods : [];
      const filtered = allProducts.filter((p) => String(p.categoryId) === String(id));
      setProducts(filtered);

    } catch (e) {
      setErr(e.message || 'Không thể tải dữ liệu');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, [id]);

  const filteredProducts = products.filter((p) =>
    p.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div>
        <div className="row" style={{ alignItems: 'center', marginBottom: 20 }}>
            <Link to="/" className="btn">← Quay lại</Link>
            <h2 style={{ margin: '0 0 0 16px' }}>
                {categoryName ? `Sản phẩm: ${categoryName}` : 'Danh sách sản phẩm'}
            </h2>
        </div>

      <div style={{ marginBottom: 20 }}>
        <input
            type="text"
            className="input"
            placeholder="Tìm kiếm sản phẩm..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{ width: '100%', maxWidth: '400px' }}
        />
      </div>

      <Message type="success" text={msg} />
      <Message type="error" text={err} />

      {loading ? <Loader /> : null}

      <div className="grid">
        {filteredProducts.map((p) => (
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
            <div className="small" style={{ marginBottom: 8, color: p.quantity > 0 ? '#28a745' : '#dc3545' }}>
                Còn lại: {p.quantity}
            </div>
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

      {!loading && filteredProducts.length === 0 ? <div className="muted">Không tìm thấy sản phẩm nào.</div> : null}
    </div>
  );
}