import React, { useEffect, useMemo, useState } from 'react';
import AdminNav from '../../components/AdminNav.jsx';
import Message from '../../components/Message.jsx';
import Loader from '../../components/Loader.jsx';
import { createApi } from '../../lib/api.js';
import { useAuth } from '../../lib/auth.jsx';
import { uploadImageToCloudinary } from "../../lib/cloudinary.js";

export default function Products() {
  const auth = useAuth();
  const api = useMemo(
    () =>
      createApi({
        getToken: () => auth.token,
        onUnauthorized: () => auth.logout()
      }),
    [auth]
  );

  const [categories, setCategories] = useState([]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [msg, setMsg] = useState('');
  const [err, setErr] = useState('');
  const [imageFile, setImageFile] = useState(null);

  const [editingId, setEditingId] = useState(null);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [price, setPrice] = useState('');
  const [categoryId, setCategoryId] = useState('');

  async function loadAll() {
    setLoading(true);
    setErr('');
    try {
      const [cats, prods] = await Promise.all([api.get('/category/get'), api.get('/product/get')]);
      setCategories(Array.isArray(cats) ? cats : []);
      setProducts(Array.isArray(prods) ? prods : []);
    } catch (e) {
      setErr(e.message || 'Không thể tải dữ liệu');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadAll();

  }, []);

  function resetForm() {
    setEditingId(null);
    setName('');
    setDescription('');
    setPrice('');
    setCategoryId('');
  }

  async function submit(e) {
    e.preventDefault();
    setErr('');
    setMsg('');
    if (!name || !price || !categoryId) {
      setErr('Vui lòng nhập đầy đủ thông tin');
      return;
    }

    try {
      let imageUrl = null;

      if (imageFile) {
        imageUrl = await uploadImageToCloudinary(imageFile);
      }

      const payload = {
        name,
        description,
        price: String(price),
        categoryId: String(categoryId),
        imageUrl
      };

      console.log("SUBMIT PAYLOAD", payload);

      let res;
      if (editingId) {
        res = await api.post('/product/update', { ...payload, id: String(editingId) });
      } else {
        res = await api.post('/product/add', payload);
      }
      setMsg((res && res.message) || 'Đã lưu sản phẩm');
      resetForm();
      await loadAll();
    } catch (e2) {
      setErr(e2.message || 'Không thể lưu sản phẩm');
    }
  }

  return (
    <div>
      <AdminNav />
      <h2 style={{ marginTop: 0 }}>Quản lý sản phẩm</h2>
      <Message type="success" text={msg} />
      <Message type="error" text={err} />

      <div className="grid" style={{ gridTemplateColumns: '1fr 2fr' }}>
        <div className="card">
          <h3 style={{ marginTop: 0 }}>{editingId ? 'Sửa sản phẩm' : 'Thêm sản phẩm'}</h3>
          <form onSubmit={submit}>
            <label className="label">Tên sản phẩm</label>
            <input className="input" value={name} onChange={(e) => setName(e.target.value)} required />

            <div style={{ height: 10 }} />
            <label className="label">Mô tả</label>
            <textarea
              className="input"
              rows={4}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              required
            />

            <div style={{ height: 10 }} />
            <label className="label">Ảnh sản phẩm</label>
            <input
              type="file"
              accept="image/*"
              className="input"
              onChange={(e) => setImageFile(e.target.files[0])}
            />

            <div style={{ height: 10 }} />
            <label className="label">Giá</label>
            <input
              className="input"
              type="number"
              min="0"
              step="1"
              value={price}
              onChange={(e) => setPrice(e.target.value)}
              required
            />

            <div style={{ height: 10 }} />
            <label className="label">Danh mục</label>
            <select className="input" value={categoryId} onChange={(e) => setCategoryId(e.target.value)} required>
              <option value="">Chọn danh mục</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>

            <div style={{ height: 12 }} />
            <div className="row">
              <button className="btn primary" type="submit">
                {editingId ? 'Cập nhật' : 'Thêm'}
              </button>
              {editingId ? (
                <button type="button" className="btn" onClick={resetForm}>
                  Huỷ
                </button>
              ) : null}
            </div>
          </form>
        </div>

        <div className="card">
          <div className="row" style={{ justifyContent: 'space-between', alignItems: 'center' }}>
            <h3 style={{ marginTop: 0 }}>Danh sách sản phẩm</h3>
            <button className="btn" onClick={loadAll} disabled={loading}>
              Tải lại
            </button>
          </div>

          {loading ? <Loader /> : null}

          <table className="table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Ảnh</th>
                <th>Tên</th>
                <th>Danh mục</th>
                <th>Giá</th>

              </tr>
            </thead>
            <tbody>
              {products.map((p) => (
                <tr key={p.id}>
                  <td>{p.id}</td>
                  <td>
                    {p.imageUrl ? (
                      <img
                        src={p.imageUrl}
                        alt={p.name}
                        style={{
                          width: 60,
                          height: 60,
                          objectFit: 'cover',
                          borderRadius: 6
                        }}
                      />
                    ) : (
                      <span className="muted">No image</span>
                    )}
                  </td>
                  <td>
                    <div style={{ fontWeight: 700 }}>{p.name}</div>
                    <div className="muted small">{p.description}</div>
                  </td>
                  <td>{p.categoryName}</td>
                  <td>{Number(p.price).toLocaleString('vi-VN')} ₫</td>
                  <td style={{ width: 220 }}>
                    <div className="row" style={{ flexWrap: 'nowrap' }}>
                      <button
                        className="btn"
                        onClick={() => {
                          setEditingId(p.id);
                          setName(p.name || '');
                          setDescription(p.description || '');
                          setPrice(String(p.price || ''));
                          setCategoryId(String(p.categoryId || ''));
                        }}
                      >
                        Sửa
                      </button>
                      <button
                        className="btn danger"
                        onClick={async () => {
                          if (!window.confirm('Bạn chắc chắn muốn xoá sản phẩm này?')) return;
                          setErr('');
                          setMsg('');
                          try {
                            const res = await api.post(`/product/delete/${p.id}`);
                            setMsg((res && res.message) || 'Đã xoá sản phẩm');
                            await loadAll();
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

          {!loading && products.length === 0 ? <div className="muted">Chưa có sản phẩm.</div> : null}
        </div>
      </div>
    </div>
  );
}
