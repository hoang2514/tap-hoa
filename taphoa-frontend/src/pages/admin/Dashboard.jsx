import React, { useEffect, useMemo, useState } from 'react';
import AdminNav from '../../components/AdminNav.jsx';
import Loader from '../../components/Loader.jsx';
import Message from '../../components/Message.jsx';
import { createApi } from '../../lib/api.js';
import { useAuth } from '../../lib/auth.jsx';

function getEpochFromBillUuid(uuid) {
  if (!uuid || typeof uuid !== 'string') return null;
  const m = uuid.match(/^BILL-(\d+)$/);
  if (!m) return null;
  const epoch = Number(m[1]);
  return Number.isFinite(epoch) ? epoch : null;
}

function formatDateKey(epoch) {
  const d = new Date(epoch);
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd}`;
}

export default function Dashboard() {
  const auth = useAuth();
  const api = useMemo(
    () =>
      createApi({
        getToken: () => auth.token,
        onUnauthorized: () => auth.logout()
      }),
    [auth]
  );

  const [counts, setCounts] = useState({ category: 0, product: 0, bill: 0 });
  const [bills, setBills] = useState([]);
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState('');

  async function load() {
    setLoading(true);
    setErr('');
    try {
      const [c, bl] = await Promise.all([api.get('/dashboard/details'), api.get('/bill/getBills')]);
      setCounts({
        category: Number(c?.category) || 0,
        product: Number(c?.product) || 0,
        bill: Number(c?.bill) || 0
      });
      setBills(Array.isArray(bl) ? bl : []);
    } catch (e) {
      setErr(e.message || 'Không thể tải dashboard');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();

  }, []);

  const totalRevenue = bills.reduce((sum, b) => sum + (Number(b.total) || 0), 0);

  const revenueByDayMap = bills.reduce((acc, b) => {
    const epoch = getEpochFromBillUuid(b.uuid);
    if (!epoch) return acc;
    const key = formatDateKey(epoch);
    acc[key] = (acc[key] || 0) + (Number(b.total) || 0);
    return acc;
  }, {});

  const revenueByDay = Object.keys(revenueByDayMap)
    .sort()
    .map((day) => ({ day, revenue: revenueByDayMap[day] }));

  const maxRevenue = revenueByDay.reduce((m, x) => Math.max(m, x.revenue), 0);
  const recentBills = bills.slice(0, 5);

  return (
    <div>
      <AdminNav />
      <div className="row" style={{ justifyContent: 'space-between', alignItems: 'center' }}>
        <h2 style={{ marginTop: 0 }}>Dashboard</h2>
        <button className="btn" onClick={load} disabled={loading}>Tải lại</button>
      </div>

      <Message type="error" text={err} />
      {loading ? <Loader /> : null}

      <div className="grid" style={{ marginBottom: 12 }}>
        <div className="card">
          <div className="muted small">Tổng danh mục</div>
          <div style={{ fontSize: 26, fontWeight: 900 }}>{counts.category}</div>
        </div>
        <div className="card">
          <div className="muted small">Tổng sản phẩm</div>
          <div style={{ fontSize: 26, fontWeight: 900 }}>{counts.product}</div>
        </div>
        <div className="card">
          <div className="muted small">Tổng hóa đơn</div>
          <div style={{ fontSize: 26, fontWeight: 900 }}>{counts.bill}</div>
        </div>
        <div className="card">
          <div className="muted small">Tổng doanh thu</div>
          <div style={{ fontSize: 26, fontWeight: 900 }}>{totalRevenue.toLocaleString('vi-VN')} ₫</div>
        </div>
      </div>

      <div className="grid" style={{ gridTemplateColumns: '2fr 1fr' }}>
        <div className="card">
          <h3 style={{ marginTop: 0 }}>Biểu đồ doanh thu theo ngày</h3>

          {revenueByDay.length === 0 ? (
            <div className="muted">Chưa có dữ liệu doanh thu.</div>
          ) : (
            <div
  style={{
    height: 260,
    padding: '16px 20px 12px',
    borderRadius: 12,
    background: '#fafafa',
    border: '1px solid #eef1f6',
    display: 'flex',
    alignItems: 'flex-end',
    gap: 24,
    justifyContent: revenueByDay.length === 1 ? 'center' : 'flex-start'
  }}
>
  {revenueByDay.map((x) => {
    const pct = maxRevenue
      ? Math.round((x.revenue / maxRevenue) * 100)
      : 0

    return (
      <div
        key={x.day}
        style={{
          width: revenueByDay.length === 1 ? 140 : 64,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center'
        }}
      >
        {/* số tiền */}
        <div
          style={{
            fontSize: 12,
            fontWeight: 600,
            color: '#475569',
            marginBottom: 6
          }}
        >
          {x.revenue.toLocaleString('vi-VN')} ₫
        </div>

        {/* cột */}
        <div
          title={`${x.revenue.toLocaleString('vi-VN')} ₫`}
          style={{
            width: '100%',
            height: `${pct}%`,
            minHeight: 28,
            background: 'linear-gradient(180deg, #0f172a, #334155)',
            borderRadius: 10,
            boxShadow: '0 6px 12px rgba(0,0,0,0.08)',
            transition: 'all 0.25s ease'
          }}
        />

        {/* ngày */}
        <div
          style={{
            marginTop: 8,
            fontSize: 12,
            color: '#64748b'
          }}
        >
          {x.day}
        </div>
      </div>
    )
  })}
</div>

          )}
        </div>

        <div className="card">
          <h3 style={{ marginTop: 0 }}>Hóa đơn gần nhất</h3>
          {recentBills.length === 0 ? (
            <div className="muted">Chưa có hóa đơn.</div>
          ) : (
            <div style={{ display: 'grid', gap: 10 }}>
              {recentBills.map((b) => (
                <div key={b.id} style={{ border: '1px solid #eef1f6', borderRadius: 10, padding: 10 }}>
                  <div style={{ fontWeight: 800 }}>{b.uuid}</div>
                  <div className="muted small">{b.name}</div>
                  <div style={{ fontWeight: 700 }}>{Number(b.total).toLocaleString('vi-VN')} ₫</div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
