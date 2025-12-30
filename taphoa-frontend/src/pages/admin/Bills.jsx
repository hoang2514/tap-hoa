import React, { useEffect, useMemo, useState } from 'react'
import AdminNav from '../../components/AdminNav.jsx'
import Message from '../../components/Message.jsx'
import Loader from '../../components/Loader.jsx'
import { createApi } from '../../lib/api.js'
import { useAuth } from '../../lib/auth.jsx'

  const getStatusText = (status) => {
    switch (status) {
      case 'AWAITING_PAYMENT': return 'Đang chờ thanh toán'
      case 'PAYMENT_FAILED': return 'Thanh toán thất bại'
      case 'PREPARING_SHIPMENT': return 'Đang chuẩn bị giao hàng'
      case 'SHIPPING': return 'Đang giao hàng'
      case 'DELIVERED': return 'Đã giao thành công'
      case 'CANCELLED': return 'Đơn hàng bị hủy'
      default: return status
    }
  }

export default function Bills() {
  const auth = useAuth()

  const api = useMemo(
    () =>
      createApi({
        getToken: () => auth.token,
        onUnauthorized: () => auth.logout()
      }),
    [auth]
  )

  const [bills, setBills] = useState([])
  const [loading, setLoading] = useState(false)
  const [msg, setMsg] = useState('')
  const [err, setErr] = useState('')

  async function load() {
    setLoading(true)
    setErr('')
    try {
      const data = await api.get('/bill/getBills')
      setBills(Array.isArray(data) ? data : [])
    } catch (e) {
      setErr(e.message || 'Không thể tải hóa đơn')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  async function handleDownloadPdf(uuid) {
    setErr('')
    setMsg('')
    try {
      const res = await fetch(`${api.baseUrl}/bill/getPdf`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${auth.token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ uuid })
      })

      if (!res.ok) {
        throw new Error('Không thể tải PDF')
      }

      const blob = await res.blob()
      downloadBlob(blob, `${uuid}.pdf`)
      setMsg('Đã tải PDF thành công')
      setTimeout(() => setMsg(''), 1200)
    } catch (e) {
      setErr(e.message || 'Không thể tải PDF')
    }
  }

  async function handleUpdateStatus(id, newStatus) {
    setErr('')
    setMsg('')
    try {
      await api.post(`/bill/updateOrderStatus/${id}`, { status: newStatus })
      setMsg('Đã cập nhật trạng thái')
      await load()
    } catch (e) {
      setErr(e.message || 'Không thể cập nhật trạng thái')
    }
  }

  async function handleDelete(id) {
    if (!window.confirm('Bạn chắc chắn muốn xoá hóa đơn này?')) return
    setErr('')
    setMsg('')
    try {
      await api.post(`/bill/delete/${id}`)
      setMsg('Đã xoá hóa đơn')
      await load()
    } catch (e) {
      setErr(e.message || 'Không thể xoá hóa đơn')
    }
  }

  return (
    <div>
      <AdminNav />
      <h2 style={{ marginTop: 0 }}>Quản lý hóa đơn</h2>

      <Message type="success" text={msg} />
      <Message type="error" text={err} />

      <div className="card">
        <div className="row" style={{ justifyContent: 'space-between', alignItems: 'center' }}>
          <div className="muted small">Tổng: {bills.length} hóa đơn</div>
          <button className="btn" onClick={load} disabled={loading}>
            Tải lại
          </button>
        </div>

        <div className="hr" />
        {loading && <Loader />}

        <table className="table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Mã</th>
              <th>Khách hàng</th>
              <th>Tổng tiền</th>
              <th>Thanh toán</th>
              <th>Trạng thái</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {bills.map((b) => (
              <tr key={b.id}>
                <td>{b.id}</td>
                <td>{b.uuid}</td>
                <td>
                  <div style={{ fontWeight: 700 }}>{b.name}</div>
                  <div className="muted small">
                    {b.email} • {b.contactNumber}
                  </div>
                </td>
                <td>{Number(b.total).toLocaleString('vi-VN')} ₫</td>
                <td>{b.paymentMethod}</td>
                <td>{getStatusText(b.status)}</td>
                <td style={{ width: 320 }}>
                  <div className="row" style={{ flexWrap: 'nowrap' }}>
                    <button className="btn" onClick={() => handleDownloadPdf(b.uuid)}>
                      Tải PDF
                    </button>
                    {b.status === 'PREPARING_SHIPMENT' && (
                      <button className="btn primary" onClick={() => handleUpdateStatus(b.id, 'SHIPPING')}>
                        Giao hàng
                      </button>
                    )}
                    {b.status === 'SHIPPING' && (
                      <button className="btn success" onClick={() => handleUpdateStatus(b.id, 'DELIVERED')}>
                        Đã giao
                      </button>
                    )}
                    <button className="btn danger" onClick={() => handleDelete(b.id)}>
                      Xoá
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {!loading && bills.length === 0 && (
          <div className="muted">Chưa có hóa đơn.</div>
        )}
      </div>
    </div>
  )
}
