import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Message from '../components/Message.jsx';
import Loader from '../components/Loader.jsx';
import { useCart } from '../lib/cart.jsx';

export default function Cart() {
  const cart = useCart();
  const nav = useNavigate();
  const [err, setErr] = useState('');
  const [editingQty, setEditingQty] = useState({});

  const isEmpty = cart.items.length === 0;

  return (
    <div>
      <div className="row" style={{ justifyContent: 'space-between', alignItems: 'center' }}>
        <h2 style={{ marginTop: 0 }}>Giỏ hàng</h2>
        <div className="row">
          <button className="btn" onClick={() => cart.refresh()} disabled={cart.loading}>Tải lại</button>
          {!isEmpty ? (
            <button className="btn primary" onClick={() => nav('/checkout')}>Thanh toán</button>
          ) : null}
        </div>
      </div>

      <Message type="error" text={err || cart.error} />
      {cart.loading ? <Loader /> : null}

      {isEmpty ? (
        <div className="card">
          <div className="muted">Giỏ hàng đang trống.</div>
          <div style={{ height: 10 }} />
          <Link className="btn" to="/">Tiếp tục mua hàng</Link>
        </div>
      ) : (
        <div className="card">
          <table className="table">
            <thead>
              <tr>
                <th>Sản phẩm</th>
                <th>Giá</th>
                <th>Số lượng</th>
                <th>Tạm tính</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {cart.items.map((it) => (
                <tr key={it.cartItemId}>
                  <td>
                    <div style={{ fontWeight: 700 }}>{it.productName}</div>
                    <div className="muted small">{it.categoryName}</div>
                  </td>
                  <td>{Number(it.price).toLocaleString('vi-VN')} ₫</td>
                  <td style={{ width: 170 }}>
                    <div className="row" style={{ flexWrap: 'nowrap' }}>
                      <button
                        className="btn"
                        onClick={async () => {
                          setErr('');
                          try {
                            const newQty = Math.max(1, (Number(it.quantity) || 1) - 1);
                            await cart.update(it.cartItemId, newQty);
                            setEditingQty(prev => {
                            const copy = { ...prev };
                            delete copy[it.cartItemId];
                            return copy;
                        });
                          } catch (e) {
                            setErr(e.message || 'Không thể cập nhật');
                          }
                        }}
                      >
                        -
                      </button>
                      <input
  className="input"
  type="number"
  min={1}
  style={{ width: 70, textAlign: 'center' }}
  value={editingQty[it.cartItemId] ?? it.quantity}
  onChange={(e) => {
    const v = Math.max(1, Number(e.target.value) || 1);
    setEditingQty(prev => ({
      ...prev,
      [it.cartItemId]: v
    }));
  }}
  onBlur={async () => {
    const newQty = editingQty[it.cartItemId];
    if (newQty && newQty !== it.quantity) {
      try {
        await cart.update(it.cartItemId, newQty);
        setEditingQty(prev => {
  const copy = { ...prev };
  delete copy[it.cartItemId];
  return copy;
});
      } catch (e) {
        setErr(e.message || 'Không thể cập nhật');
      }
    }
  }}
/>

                      <button
                        className="btn"
                        onClick={async () => {
                          setErr('');
                          try {
                            const newQty = (Number(it.quantity) || 1) + 1;
                            await cart.update(it.cartItemId, newQty);
                            setEditingQty(prev => {
  const copy = { ...prev };
  delete copy[it.cartItemId];
  return copy;
});
                          } catch (e) {
                            setErr(e.message || 'Không thể cập nhật');
                          }
                        }}
                      >
                        +
                      </button>
                    </div>
                  </td>
                  <td>{(Number(it.price) * Number(it.quantity)).toLocaleString('vi-VN')} ₫</td>
                  <td style={{ width: 120 }}>
                    <button
                      className="btn danger"
                      onClick={async () => {
                        if (!window.confirm('Bạn chắc chắn muốn xoá sản phẩm khỏi giỏ hàng?')) return;
                        setErr('');
                        try {
                          await cart.remove(it.cartItemId);
                        } catch (e) {
                          setErr(e.message || 'Không thể xoá');
                        }
                      }}
                    >
                      Xoá
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          <div className="hr" />
          <div className="row" style={{ justifyContent: 'space-between', alignItems: 'center' }}>
            <div style={{ fontWeight: 800 }}>Tổng tiền</div>
            <div style={{ fontWeight: 900 }}>{Number(cart.total).toLocaleString('vi-VN')} ₫</div>
          </div>
        </div>
      )}
    </div>
  );
}
