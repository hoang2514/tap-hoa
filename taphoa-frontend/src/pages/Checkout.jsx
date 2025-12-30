import React, { useMemo, useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import Message from '../components/Message.jsx'
import Loader from '../components/Loader.jsx'
import { useCart } from '../lib/cart.jsx'
import { useAuth } from '../lib/auth.jsx'
import { createApi } from '../lib/api.js'

function formatMoney(v) {
  const n = Number(v) || 0
  try {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n)
  } catch {
    return String(n)
  }
}

function buildProductDetails(items) {
  return JSON.stringify(
    items.map((it) => {
      const price = Number(it.price) || 0
      const quantity = Number(it.quantity) || 0
      return {
          id: it.productId,
        name: String(it.productName || ''),
        category: String(it.categoryName || ''),
        quantity: String(quantity),
        price: price,              
        total: price * quantity    
      }
    })
  )
}

export default function Checkout() {
  const cart = useCart()
  const auth = useAuth()
  const nav = useNavigate()

  const api = useMemo(
    () =>
      createApi({
        getToken: () => auth.token,
        onUnauthorized: () => auth.logout()
      }),
    [auth]
  )


  const [paymentMethod, setPaymentMethod] = useState('CASH')

  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const [orderUuid, setOrderUuid] = useState('')
  const [orderStatus, setOrderStatus] = useState('')
  const [orderPlaced, setOrderPlaced] = useState(false)

  const vnpayFormRef = useRef(null)

  const totalAmount = Math.round(Number(cart.total) || 0)
  const productDetails = useMemo(() => buildProductDetails(cart.items), [cart.items])

  if (!cart.items.length) {
    return (
      <div className="card">
        <h2>Thanh toán</h2>
        <div className="muted">Giỏ hàng trống.</div>
        <button className="btn" onClick={() => nav('/')}>Về trang chủ</button>
      </div>
    )
  }

  async function handlePlaceOrder(e) {
    e.preventDefault()
    setError('')
    setSuccess('')
    setLoading(true)
    setOrderUuid('')
    setOrderStatus('')

    try {
      const payload = {
        name: auth.user?.name || 'Khách hàng',
        contactNumber: auth.user?.phone || '0000000000',
        email: auth.email,
        paymentMethod,
        productDetails,
        totalAmount: String(totalAmount)
      }

      const res = await api.post('/bill/generateReport', payload)
      const obj = typeof res === 'string' ? JSON.parse(res) : res

      if (!obj?.uuid) {
        throw new Error(
          'Backend không trả uuid. Nếu backend đang báo 500, hãy kiểm tra thư mục STORE_LOCATION: C:\\Users\\Administrator\\Documents'
        )
      }

      setOrderUuid(obj.uuid)

      // Chờ RabbitMQ xử lý đơn hàng
      await new Promise(resolve => setTimeout(resolve, 3000));

      // Chờ đến khi đơn hàng được xử lý
      let status = 'CONFIRMING';
      let attempts = 0;
      while (status === 'CONFIRMING' && attempts < 120) { // 60 seconds
        await new Promise(resolve => setTimeout(resolve, 500));
        try {
          const statusRes = await api.get(`/bill/status/${obj.uuid}`);
          status = statusRes.status;
        } catch (e) {
          console.error('Error polling status', e);
        }
        attempts++;
      }
      setOrderStatus(status)

      if (status === 'PREPARING_SHIPMENT') {
        setSuccess('Đặt hàng thành công.')
        await cart.refresh()
        setOrderPlaced(true)
      } else if (status === 'AWAITING_PAYMENT') {
        setSuccess('Đặt hàng thành công. Vui lòng thanh toán.')
        setOrderPlaced(true)
      } else {
        throw new Error('Đơn hàng không thể đặt. Có thể hết hàng hoặc lỗi hệ thống.')
      }
    } catch (err) {
      setError(err?.message || 'Không thể đặt hàng')
    } finally {
      setLoading(false)
    }
  }

  async function handlePayment(e) {
    e.preventDefault()
    if (!orderUuid || orderStatus !== 'AWAITING_PAYMENT') return

    const form = vnpayFormRef.current
    form.elements.amount.value = String(totalAmount)
    form.elements.orderInfo.value = orderUuid
    form.submit()
  }

  return (
    <div>
      <div className="row" style={{ justifyContent: 'space-between' }}>
        <h2>Thanh toán</h2>
        <Link className="btn" to="/cart">Quay lại giỏ hàng</Link>
      </div>

      <Message type="success" text={success} />
      <Message type="error" text={error} />

      <div className="grid" style={{ gridTemplateColumns: '2fr 1fr', gap: 14 }}>
        <div className="card">
          <h3>Thông tin khách hàng</h3>

          <form onSubmit={handlePlaceOrder}>


            <div style={{ height: 10 }} />
            <label className="label">Email</label>
            <input className="input" value={auth.email || ''} disabled />

            <div style={{ height: 10 }} />
            <label className="label">Phương thức thanh toán</label>
            <select className="input" value={paymentMethod} onChange={(e) => setPaymentMethod(e.target.value)} disabled={orderPlaced}>
              <option value="CASH">Tiền mặt</option>
              <option value="VNPAY">VNPay</option>
            </select>

            <div style={{ height: 14 }} />
            {!orderPlaced && (
              <button className="btn primary" disabled={loading}>
                {loading ? 'Đang xử lý...' : 'Đặt hàng'}
              </button>
            )}

            {paymentMethod === 'VNPAY' && orderStatus === 'AWAITING_PAYMENT' && (
              <div style={{ marginTop: 10 }}>
                <button className="btn primary" onClick={handlePayment}>
                  Thanh toán VNPay
                </button>
              </div>
            )}

            {loading && <Loader />}
          </form>

          <form
            ref={vnpayFormRef}
            method="POST"
            action={`${api.baseUrl}/bill/submitOrder`}
            style={{ display: 'none' }}
          >
            <input name="amount" />
            <input name="orderInfo" />
          </form>
        </div>

        <div className="card">
          <h3>Tóm tắt đơn hàng</h3>
          {cart.items.map((it) => (
            <div key={it.cartItemId} style={{ marginBottom: 8 }}>
              <div style={{ fontWeight: 700 }}>{it.productName}</div>
              <div className="muted small">
                {formatMoney(it.price)} x {it.quantity}
              </div>
            </div>
          ))}
          <div className="hr" />
          <div className="row" style={{ justifyContent: 'space-between' }}>
            <b>Tổng tiền</b>
            <b>{formatMoney(totalAmount)}</b>
          </div>
        </div>
      </div>
    </div>
  )
}