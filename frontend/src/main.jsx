import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App.jsx';
import { AuthProvider } from './context/AuthContext';
import { Toaster } from 'react-hot-toast';
import './styles/global.css';

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <AuthProvider>
      <Toaster 
        position="top-right" 
        toastOptions={{
          style: {
            background: 'rgba(4,42,43,0.9)',
            color: '#E2E8C0',
            border: '1px solid rgba(206,160,126,0.3)',
          }
        }}
      />
      <App />
    </AuthProvider>
  </React.StrictMode>
);
