# Frontend React - Hệ thống bán hàng Tạp Hóa

## Yêu cầu
- Node.js 18+ (khuyến nghị)
- Backend Spring Boot chạy sẵn (mặc định `http://localhost:8080`)

## Cấu hình API
Tạo file `.env` (cùng cấp với `package.json`) nếu backend không chạy cổng 8080:

```
VITE_API_BASE=http://localhost:8080
```

## Cài đặt & chạy
```bash
npm install
npm run dev
```

Mở: `http://localhost:5173`

## Ghi chú nghiệp vụ
- JWT lưu trong localStorage (`taphoa_token`).
- Nếu token hết hạn: tự đăng xuất và redirect về `/login`.
- Thanh toán VNPay: frontend **chỉ submit HTML form** đến backend `/bill/submitOrder`.
- Backend tự render kết quả VNPay tại `/bill/vnpay-payment`.
