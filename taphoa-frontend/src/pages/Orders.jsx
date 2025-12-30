import React, { useState, useEffect, useMemo } from 'react';
import { createApi } from '../lib/api.js';
import { useAuth } from '../lib/auth.jsx';

export default function Orders() {
  const auth = useAuth();
  const api = useMemo(
    () =>
      createApi({
        getToken: () => auth.token,
        onUnauthorized: () => auth.logout()
      }),
    [auth]
  );

  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    try {
      const response = await api.get('/bill/getUserBills');
      setOrders(Array.isArray(response) ? response : []);
    } catch (error) {
      console.error('Error fetching orders:', error);
      setOrders([]);
    } finally {
      setLoading(false);
    }
  };

  const cancelOrder = async (id) => {
    if (!window.confirm('Bạn có chắc muốn hủy đơn hàng này?')) return;
    try {
      await api.post(`/bill/cancelOrder/${id}`);
      fetchOrders();
    } catch (error) {
      alert('Không thể hủy đơn hàng: ' + (error.response?.data?.message || error.message));
    }
  };

  const getStatusInfo = (status) => {
    switch (status) {
      case 'AWAITING_PAYMENT': return { text: 'Chờ thanh toán', color: '#f59e0b', bg: '#fef3c7' };
      case 'PAYMENT_FAILED': return { text: 'Thanh toán lỗi', color: '#ef4444', bg: '#fee2e2' };
      case 'PREPARING_SHIPMENT': return { text: 'Đang chờ giao hàng', color: '#3b82f6', bg: '#dbeafe' };
      case 'SHIPPING': return { text: 'Đang giao hàng', color: '#6366f1', bg: '#e0e7ff' };
      case 'DELIVERED': return { text: 'Đã giao thành công', color: '#10b981', bg: '#d1fae5' };
      case 'CANCELLED': return { text: 'Đã hủy', color: '#6b7280', bg: '#f3f4f6' };
      default: return { text: status, color: '#374151', bg: '#f3f4f6' };
    }
  };

  const parseProducts = (productDetail) => {
    try {
      return JSON.parse(productDetail);
    } catch (e) {
      return [];
    }
  };

  const canCancel = (status) => {
    return !['DELIVERED', 'CANCELLED', 'PAYMENT_FAILED'].includes(status);
  };

  if (loading) return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', backgroundColor: '#f9fafb' }}>
      <div style={{ fontSize: '1.1rem', color: '#6b7280' }}>Đang tải đơn hàng...</div>
    </div>
  );

  return (
    <div style={{ minHeight: '100vh', backgroundColor: '#f3f4f6', padding: '40px 20px' }}>
      <div style={{ maxWidth: '800px', margin: '0 auto' }}>
        <header style={{ marginBottom: '32px' }}>
          <h1 style={{ fontSize: '28px', fontWeight: '800', color: '#111827', margin: 0 }}>Đơn hàng của tôi</h1>
        </header>

        {orders.length === 0 ? (
          <div style={{ backgroundColor: 'white', padding: '60px', borderRadius: '16px', textAlign: 'center', boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
            <p style={{ color: '#9ca3af', fontSize: '18px' }}>Bạn chưa có đơn hàng nào.</p>
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
            {orders.map((order) => {
              const products = parseProducts(order.productDetail);
              const status = getStatusInfo(order.status);
              
              return (
                <div key={order.id} style={{ backgroundColor: 'white', borderRadius: '16px', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1)', overflow: 'hidden', border: '1px solid #e5e7eb' }}>
                  <div style={{ padding: '16px 24px', backgroundColor: '#fafafa', borderBottom: '1px solid #f3f4f6', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                      <span style={{ fontWeight: 'bold', color: '#2563eb', backgroundColor: '#eff6ff', padding: '4px 10px', borderRadius: '6px', fontSize: '14px' }}>
                        #{order.id}
                      </span>
                      <span style={{ fontSize: '14px', color: '#9ca3af' }}>
                        {order.createdDate ? new Date(order.createdDate).toLocaleDateString('vi-VN') : 'N/A'}
                      </span>
                    </div>
                    <span style={{ fontSize: '12px', fontWeight: 'bold', textTransform: 'uppercase', padding: '6px 12px', borderRadius: '99px', backgroundColor: status.bg, color: status.color, border: `1px solid ${status.color}20` }}>
                      {status.text}
                    </span>
                  </div>

                  <div style={{ padding: '24px' }}>
                    {products.map((p, idx) => (
                      <div key={idx} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: idx === products.length - 1 ? 0 : '16px', paddingBottom: idx === products.length - 1 ? 0 : '16px', borderBottom: idx === products.length - 1 ? 'none' : '1px solid #f9fafb' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                          <div style={{ width: '48px', height: '48px', backgroundColor: '#f3f4f6', borderRadius: '8px', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path><polyline points="3.27 6.96 12 12.01 20.73 6.96"></polyline><line x1="12" y1="22.08" x2="12" y2="12"></line></svg>
                          </div>
                          <div>
                            <h4 style={{ margin: 0, fontWeight: '600', color: '#1f2937', fontSize: '15px' }}>{p.name}</h4>
                            <p style={{ margin: '4px 0 0 0', fontSize: '13px', color: '#6b7280' }}>Số lượng: <strong>{p.quantity}</strong></p>
                          </div>
                        </div>
                        <div style={{ textAlign: 'right' }}>
                          <div style={{ fontWeight: '600', color: '#111827' }}>{Number(p.price).toLocaleString('vi-VN')} ₫</div>
                          <div style={{ fontSize: '12px', color: '#9ca3af' }}>Tổng: {Number(p.total).toLocaleString('vi-VN')} ₫</div>
                        </div>
                      </div>
                    ))}
                  </div>

                  <div style={{ padding: '20px 24px', backgroundColor: '#fcfcfc', borderTop: '1px solid #f3f4f6', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div style={{ fontSize: '13px', color: '#6b7280' }}>
                      Phương thức thanh toán: <span style={{ fontWeight: '500', color: '#374151' }}>{order.paymentMethod}</span>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
                      <div style={{ textAlign: 'right' }}>
                        <span style={{ fontSize: '12px', color: '#9ca3af', display: 'block', textTransform: 'uppercase', letterSpacing: '0.5px' }}>Tổng cộng</span>
                        <span style={{ fontSize: '20px', fontWeight: '800', color: '#ef4444' }}>{Number(order.total).toLocaleString('vi-VN')} ₫</span>
                      </div>
                      {canCancel(order.status) && (
                        <button
                          onClick={() => cancelOrder(order.id)}
                          style={{ backgroundColor: 'white', color: '#ef4444', border: '1px solid #fecaca', padding: '8px 16px', borderRadius: '8px', fontSize: '14px', fontWeight: '600', cursor: 'pointer', transition: 'all 0.2s' }}
                          onMouseOver={(e) => e.target.style.backgroundColor = '#fef2f2'}
                          onMouseOut={(e) => e.target.style.backgroundColor = 'white'}
                        >
                          Hủy
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}