import React, { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { createApi } from './api.js';
import { useAuth } from './auth.jsx';

const CartContext = createContext(null);

export function CartProvider({ children }) {
  const auth = useAuth();
  const api = useMemo(
    () =>
      createApi({
        getToken: () => auth.token,
        onUnauthorized: () => auth.logout()
      }),
    [auth]
  );

  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  async function refresh() {
    if (!auth.isAuthenticated) {
      setItems([]);
      return;
    }
    setLoading(true);
    setError('');
    try {
      const data = await api.get('/cart');
      setItems(Array.isArray(data) ? data : []);
    } catch (e) {
      setError(e.message || 'Không thể tải giỏ hàng');
    } finally {
      setLoading(false);
    }
  }

  async function add(productId, quantity = 1) {
    setError('');
    await api.post('/cart', { productId, quantity });
    await refresh();
  }

  async function update(cartItemId, quantity) {
    setError('');
    await api.put(`/cart/${cartItemId}`, { quantity });
    await refresh();
  }

  async function remove(cartItemId) {
    setError('');
    await api.del(`/cart/${cartItemId}`);
    await refresh();
  }

  useEffect(() => {
    refresh();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [auth.isAuthenticated]);

  const total = items.reduce((sum, it) => sum + (Number(it.price) || 0) * (Number(it.quantity) || 0), 0);

  const value = useMemo(
    () => ({
      items,
      total,
      loading,
      error,
      refresh,
      add,
      update,
      remove
    }),
    [items, total, loading, error]
  );

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export function useCart() {
  const ctx = useContext(CartContext);
  if (!ctx) throw new Error('useCart phải được dùng trong CartProvider');
  return ctx;
}
