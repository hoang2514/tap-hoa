import React, { useEffect, useMemo, useState } from 'react';
import AdminNav from '../../components/AdminNav.jsx';
import Message from '../../components/Message.jsx';
import Loader from '../../components/Loader.jsx';
import { createApi } from '../../lib/api.js';
import { useAuth } from '../../lib/auth.jsx';

export default function Categories() {
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
  const [name, setName] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(false);
  const [msg, setMsg] = useState('');
  const [err, setErr] = useState('');

  async function load() {
    setLoading(true);
    setErr('');
    try {
      const data = await api.get('/category/get');
      setItems(Array.isArray(data) ? data : []);
    } catch (e) {
      setErr(e.message || 'Không thể tải danh mục');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();

  }, []);

  async function submit(e) {
    e.preventDefault();
    setErr('');
    setMsg('');
    try {
      if (editingId) {
        const res = await api.post('/category/update', { id: String(editingId), name });
        setMsg((res && res.message) || 'Đã cập nhật danh mục');
      } else {
        const res = await api.post('/category/add', { name });
        setMsg((res && res.message) || 'Đã thêm danh mục');
      }
      setName('');
      setEditingId(null);
      await load();
    } catch (e2) {
      setErr(e2.message || 'Không thể lưu');
    }
  }

  return (
    <div>
      <AdminNav />
      <h2 style={{ marginTop: 0 }}>Quản lý danh mục</h2>
      <Message type="success" text={msg} />
      <Message type="error" text={err} />

      <div className="grid" style={{ gridTemplateColumns: '1fr 2fr' }}>
        <div className="card">
          <h3 style={{ marginTop: 0 }}>{editingId ? 'Sửa danh mục' : 'Thêm danh mục'}</h3>
          <form onSubmit={submit}>
            <label className="label">Tên danh mục</label>
            <input className="input" value={name} onChange={(e) => setName(e.target.value)} required />

            <div style={{ height: 12 }} />
            <div className="row">
              <button className="btn primary" type="submit">
                {editingId ? 'Cập nhật' : 'Thêm'}
              </button>
              {editingId ? (
                <button
                  type="button"
                  className="btn"
                  onClick={() => {
                    setEditingId(null);
                    setName('');
                  }}
                >
                  Huỷ
                </button>
              ) : null}
            </div>
          </form>
        </div>

        <div className="card">
          <div className="row" style={{ justifyContent: 'space-between', alignItems: 'center' }}>
            <h3 style={{ marginTop: 0 }}>Danh sách danh mục</h3>
            <button className="btn" onClick={load} disabled={loading}>
              Tải lại
            </button>
          </div>

          {loading ? <Loader /> : null}

          <table className="table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Tên</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {items.map((c) => (
                <tr key={c.id}>
                  <td>{c.id}</td>
                  <td>{c.name}</td>
                  <td style={{ width: 220 }}>
                    <div className="row" style={{ flexWrap: 'nowrap' }}>
                      <button
                        className="btn"
                        onClick={() => {
                          setEditingId(c.id);
                          setName(c.name || '');
                        }}
                      >
                        Sửa
                      </button>
                      <button
                        className="btn danger"
                        onClick={async () => {
                          if (!window.confirm('Bạn chắc chắn muốn xoá danh mục này?')) return;
                          setErr('');
                          setMsg('');
                          try {
                            const res = await api.post(`/category/delete/${c.id}`);
                            setMsg((res && res.message) || 'Đã xoá danh mục');
                            await load();
                          } catch (e3) {
                            setErr(e3.message || 'Không thể xoá');
                          }
                        }}
                      >
                        Xoá
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {!loading && items.length === 0 ? <div className="muted">Chưa có danh mục.</div> : null}
        </div>
      </div>
    </div>
  );
}
