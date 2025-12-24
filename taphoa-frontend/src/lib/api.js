const API_BASE = (import.meta.env.VITE_API_BASE || 'http://localhost:8080').replace(/\/$/, '');

function buildUrl(path) {
  if (path.startsWith('http')) return path;
  return `${API_BASE}${path.startsWith('/') ? '' : '/'}${path}`;
}

async function parseResponse(res) {
  const ct = res.headers.get('content-type') || '';
  if (ct.includes('application/json')) {
    return await res.json();
  }
  return await res.text();
}

export function createApi({ getToken, onUnauthorized }) {
  async function request(path, { method = 'GET', body, headers = {}, responseType = 'auto' } = {}) {
    const token = getToken ? getToken() : null;
    const h = { ...headers };
    if (token) {
      h.Authorization = `Bearer ${token}`;
    }

    let reqBody = body;
    if (body && !(body instanceof FormData) && typeof body !== 'string' && responseType !== 'pdf') {
      // Backend nhận Map JSON
      h['Content-Type'] = 'application/json';
      reqBody = JSON.stringify(body);
    }

    const res = await fetch(buildUrl(path), {
      method,
      headers: h,
      body: method === 'GET' || method === 'HEAD' ? undefined : reqBody
    });

    if (res.status === 401) {
      if (onUnauthorized) onUnauthorized();
      throw new Error('UNAUTHORIZED');
    }

    if (!res.ok) {
      const data = await parseResponse(res);
      const msg = typeof data === 'string' ? data : (data && data.message) ? data.message : 'Có lỗi xảy ra';
      const err = new Error(msg);
      err.status = res.status;
      err.data = data;
      throw err;
    }

    if (responseType === 'pdf') {
      const arrayBuffer = await res.arrayBuffer();
      return new Uint8Array(arrayBuffer);
    }

    if (responseType === 'auto') {
      return await parseResponse(res);
    }

    return await res.text();
  }

  return {
    baseUrl: API_BASE,
    get: (path) => request(path),
    post: (path, body, options) => request(path, { method: 'POST', body, ...options }),
    put: (path, body, options) => request(path, { method: 'PUT', body, ...options }),
    del: (path, options) => request(path, { method: 'DELETE', ...options }),
    pdf: (path, body) => request(path, { method: 'POST', body, responseType: 'pdf', headers: { 'Content-Type': 'application/json' } })
  };
}
