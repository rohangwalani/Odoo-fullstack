import axios from 'axios';
import toast from 'react-hot-toast';

const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8000/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
});

/* ── Request Interceptor ─────────────────────────────────── */
axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

/* ── Response Interceptor (global error handling) ───────── */
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (!error.response) {
      // Network / timeout error
      toast.error('Network error — please check your connection', {
        id: 'network-error',
        className: 'custom-toast',
      });
      return Promise.reject(error);
    }

    const { status, data } = error.response;
    const serverMessage = data?.message || data?.detail || data?.error;

    switch (status) {
      case 400:
        toast.error(serverMessage || 'Bad request — please check your input.', {
          id: `err-400`,
          className: 'custom-toast',
        });
        break;

      case 401:
        toast.error(serverMessage || 'Unauthorized — please log in again.', {
          id: `err-401`,
          className: 'custom-toast',
        });
        // Optionally clear token and redirect
        localStorage.removeItem('authToken');
        break;

      case 402:
        toast.error(serverMessage || 'Payment required — upgrade your plan.', {
          id: `err-402`,
          className: 'custom-toast',
        });
        break;

      case 403:
        toast.error(serverMessage || 'Forbidden — you do not have permission.', {
          id: `err-403`,
          className: 'custom-toast',
        });
        break;

      case 404:
        toast.error(serverMessage || 'Resource not found.', {
          id: `err-404`,
          className: 'custom-toast',
        });
        break;

      case 409:
        toast.error(serverMessage || 'Conflict — this resource already exists.', {
          id: `err-409`,
          className: 'custom-toast',
        });
        break;

      case 422:
        toast.error(serverMessage || 'Validation failed — check your input.', {
          id: `err-422`,
          className: 'custom-toast',
        });
        break;

      case 500:
        toast.error(serverMessage || 'Server error — please try again later.', {
          id: `err-500`,
          className: 'custom-toast',
        });
        break;

      default:
        toast.error(serverMessage || `Error ${status} — something went wrong.`, {
          id: `err-${status}`,
          className: 'custom-toast',
        });
    }

    return Promise.reject(error);
  }
);

export default axiosInstance;
