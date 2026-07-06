import axios from 'axios';

const API_BASE = '/api/v1';

const api = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' }
});

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const authApi = {
  login: (data) => api.post('/auth/login', data),
};

export const userApi = {
  getAll: () => api.get('/users'),
  getById: (id) => api.get(`/users/${id}`),
  create: (data) => api.post('/users', data),
  update: (id, data) => api.put(`/users/${id}`, data),
  activate: (id) => api.patch(`/users/${id}/activate`),
  deactivate: (id) => api.patch(`/users/${id}/deactivate`),
  getByWarehouse: (id) => api.get(`/users/by-warehouse/${id}`),
};

export const productApi = {
  getAll: (search) => api.get('/products', { params: { search } }),
  getById: (id) => api.get(`/products/${id}`),
  create: (data) => api.post('/products', data),
  update: (id, data) => api.put(`/products/${id}`, data),
  delete: (id) => api.delete(`/products/${id}`),
};

export const warehouseApi = {
  getAll: () => api.get('/warehouses'),
  getById: (id) => api.get(`/warehouses/${id}`),
  create: (data) => api.post('/warehouses', data),
  update: (id, data) => api.put(`/warehouses/${id}`, data),
};

export const inventoryApi = {
  getAll: (warehouseId) => api.get('/inventory', { params: { warehouseId } }),
  getByProduct: (id) => api.get(`/inventory/product/${id}`),
  inbound: (data) => api.post('/inventory/inbound', data),
  outbound: (data) => api.post('/inventory/outbound', data),
  transfer: (data) => api.post('/inventory/transfer', data),
  adjustment: (data) => api.post('/inventory/adjustment', data),
};

export const purchaseRequestApi = {
  getAll: (warehouseId) => api.get('/purchase-requests', { params: { warehouseId } }),
  create: (data) => api.post('/purchase-requests', data),
  approve: (id) => api.put(`/purchase-requests/${id}/approve`),
};

export const reportApi = {
  inventory: () => api.get('/reports/inventory'),
  movement: () => api.get('/reports/movement'),
  warehouse: () => api.get('/reports/warehouse'),
};

export default api;
