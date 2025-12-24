import React from 'react';

export default function Loader({ text = 'Đang tải...' }) {
  return (
    <div className="card" style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
      <div
        style={{
          width: 18,
          height: 18,
          border: '2px solid #e7e9ee',
          borderTopColor: '#111',
          borderRadius: '50%',
          animation: 'spin 1s linear infinite'
        }}
      />
      <div>{text}</div>
      <style>{`@keyframes spin{to{transform:rotate(360deg)}}`}</style>
    </div>
  );
}
