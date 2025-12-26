import React, { useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import Loader from '../components/Loader.jsx';
import Message from '../components/Message.jsx';
import { createApi } from '../lib/api.js';
import { useAuth } from '../lib/auth.jsx';
import { useCart } from '../lib/cart.jsx';

export default function ProductDetail() {
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

  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState('');
  const [msg, setMsg] = useState('');
  const [qty, setQty] = useState(1);


  async function load() {
    setLoading(true);
    setErr('');
    try {
      const data = await api.get(`/product/getById/${id}`);
      setProduct(data);
    } catch (e) {
      setErr(e.message || 'Không thể tải sản phẩm');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  return (
    <div className="card">
      <div className="row" style={{ justifyContent: 'space-between', alignItems: 'center' }}>
        <h2 style={{ margin: 0 }}>Chi tiết sản phẩm</h2>
        <Link className="btn" to="/">Quay lại</Link>
      </div>
      <div className="hr" />

      <Message type="success" text={msg} />
      <Message type="error" text={err} />

      {loading ? <Loader /> : null}

      {product ? (
        <>
          {product.imageUrl ? (
            <img
              src={product.imageUrl}
              alt={product.name}
              style={{
                width: '100%',
                maxWidth: 320,
                height: 'auto',
                marginBottom: 16,
                borderRadius: 8,
                objectFit: 'cover'
              }}
              // onError={(e) => {
              //   e.target.style.display = 'none';
              // }}
            />
          ) : "null"}
          <div style={{ fontSize: 20, fontWeight: 800, marginBottom: 6 }}>{product.name}</div>
          <div className="muted small" style={{ marginBottom: 10 }}>
            {product.categoryName ? `Danh mục: ${product.categoryName}` : ''}
          </div>
          <div style={{ marginBottom: 10 }}>{product.description}</div>
          <div style={{ fontWeight: 800, marginBottom: 14 }}>{Number(product.price).toLocaleString('vi-VN')} ₫</div>

          <div className="row" style={{ alignItems: 'center', marginBottom: 12 }}>
            <span style={{ marginRight: 10 }}>Số lượng:</span>

            <button
              className="btn"
              onClick={() => setQty(q => Math.max(1, q - 1))}
            >
              -
            </button>

            <input
              className="input"
              type="number"
              min={1}
              value={qty}
              onChange={(e) =>
                setQty(Math.max(1, Number(e.target.value) || 1))
              }
              style={{ width: 80, textAlign: 'center', margin: '0 8px' }}
            />

            <button
              className="btn"
              onClick={() => setQty(q => q + 1)}
            >
              +
            </button>
          </div>


          <button
            className="btn primary"
            onClick={async () => {
              try {
                await cart.add(product.id, qty);


                setMsg('Đã thêm vào giỏ hàng');
                setTimeout(() => setMsg(''), 1200);
              } catch (e) {
                setErr(e.message || 'Không thể thêm vào giỏ hàng');
              }
            }}
          >
            Thêm vào giỏ hàng
          </button>
        </>
      ) : null}
    </div>
  );
}
