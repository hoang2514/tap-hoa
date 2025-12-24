import React from 'react';

export default function Message({ type = 'info', text }) {
  if (!text) return null;
  const bg = type === 'error' ? '#fee2e2' : type === 'success' ? '#dcfce7' : '#e0f2fe';
  const border = type === 'error' ? '#fecaca' : type === 'success' ? '#bbf7d0' : '#bae6fd';

  return (
    <div style={{ background: bg, border: `1px solid ${border}`, padding: 10, borderRadius: 8, marginBottom: 12 }}>
      {text}
    </div>
  );
}
