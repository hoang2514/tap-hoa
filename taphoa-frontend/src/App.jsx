import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';

import Layout from './components/Layout.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';

import Home from './pages/Home.jsx';
import CategoryDetail from './pages/CategoryDetail.jsx';
import ProductDetail from './pages/ProductDetail.jsx';
import Cart from './pages/Cart.jsx';
import Checkout from './pages/Checkout.jsx';
import Login from './pages/Login.jsx';
import Signup from './pages/Signup.jsx';
import ForgotPassword from './pages/ForgotPassword.jsx';
import ChangePassword from './pages/ChangePassword.jsx';
import PaymentResult from './pages/PaymentResult.jsx';
import Orders from './pages/Orders.jsx';
import NotFound from './pages/NotFound.jsx';

import Dashboard from './pages/admin/Dashboard.jsx';
import Categories from './pages/admin/Categories.jsx';
import Products from './pages/admin/Products.jsx';
import Bills from './pages/admin/Bills.jsx';

import { AuthProvider } from './lib/auth.jsx';
import { CartProvider } from './lib/cart.jsx';

export default function App() {
  return (
    <AuthProvider>
      <CartProvider>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<Signup />} />
          <Route path="/forgot-password" element={<ForgotPassword />} />

          {/* Tất cả trang còn lại bắt buộc đăng nhập */}
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <Layout />
              </ProtectedRoute>
            }
          >
            <Route index element={<Home />} />
            <Route path="category/:id" element={<CategoryDetail />} />
            <Route path="product/:id" element={<ProductDetail />} />
            <Route path="cart" element={<Cart />} />
            <Route path="checkout" element={<Checkout />} />
            <Route path="change-password" element={<ChangePassword />} />
            <Route path="orders" element={<Orders />} />

            {/* Trang này chỉ để hiển thị hướng dẫn. Backend VNPay render view riêng */}
            <Route path="payment-result" element={<PaymentResult />} />

            {/* Admin */}
            <Route
              path="admin/dashboard"
              element={
                <ProtectedRoute requireRole="ADMIN">
                  <Dashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="admin/categories"
              element={
                <ProtectedRoute requireRole="ADMIN">
                  <Categories />
                </ProtectedRoute>
              }
            />
            <Route
              path="admin/products"
              element={
                <ProtectedRoute requireRole="ADMIN">
                  <Products />
                </ProtectedRoute>
              }
            />
            <Route
              path="admin/bills"
              element={
                <ProtectedRoute requireRole="ADMIN">
                  <Bills />
                </ProtectedRoute>
              }
            />

            <Route path="not-found" element={<NotFound />} />
            <Route path="*" element={<Navigate to="/not-found" replace />} />
          </Route>

          {/* Khi chưa đăng nhập và gõ URL khác, ProtectedRoute sẽ tự redirect về /login */}
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </CartProvider>
    </AuthProvider>
  );
}