function base64UrlDecode(str) {
  try {
    const base64 = str.replace(/-/g, '+').replace(/_/g, '/');
    const padded = base64 + '='.repeat((4 - (base64.length % 4)) % 4);
    const decoded = atob(padded);

    return decodeURIComponent(
      decoded
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
  } catch {
    return null;
  }
}

export function decodeJwt(token) {
  if (!token || typeof token !== 'string') return null;
  const parts = token.split('.');
  if (parts.length !== 3) return null;
  const payload = base64UrlDecode(parts[1]);
  if (!payload) return null;
  try {
    return JSON.parse(payload);
  } catch {
    return null;
  }
}

export function isTokenExpired(token, skewSeconds = 10) {
  const payload = decodeJwt(token);
  if (!payload || !payload.exp) return true;
  const now = Math.floor(Date.now() / 1000);
  return now >= Number(payload.exp) - skewSeconds;
}

export function normalizeRole(role) {
  if (!role) return null;
  const r = String(role).toLowerCase();
  if (r === 'admin' || r === 'role_admin') return 'ADMIN';
  if (r === 'user' || r === 'role_user') return 'USER';
  return String(role).toUpperCase();
}

export function getRoleFromToken(token) {
  const payload = decodeJwt(token);
  if (!payload) return null;
  // JwtUtil có thể để role ở "role" hoặc "authorities" tuỳ cấu hình. Ưu tiên role.
  const raw = payload.role || payload.roles || payload.authority || payload.authorities;
  if (Array.isArray(raw)) {
    return normalizeRole(raw[0]);
  }
  return normalizeRole(raw);
}

export function getEmailFromToken(token) {
  const payload = decodeJwt(token);
  if (!payload) return null;
  return payload.sub || payload.email || null;
}
