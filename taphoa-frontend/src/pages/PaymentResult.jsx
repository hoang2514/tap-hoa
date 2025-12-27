import React from 'react';

export default function PaymentResult() {
  return (
    <div className="card">
      <h2 style={{ marginTop: 0 }}>Kết quả thanh toán</h2>
      <p>
        Trang kết quả VNPay được <b>backend</b> render tại đường dẫn <b>/bill/vnpay-payment</b> (không phải SPA).
      </p>
      <p className="muted">
        Nếu bạn đang đứng ở đây, hãy kiểm tra luồng thanh toán: Frontend chỉ submit form đến <b>/bill/submitOrder</b>,
        sau đó VNPay sẽ redirect về backend.
      </p>
    </div>
  );
}
