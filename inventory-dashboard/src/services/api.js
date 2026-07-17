import axios from 'axios';

const api = axios.create({
  baseURL: '/api/v1',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('authToken');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

let isLoginRequest = false;

api.interceptors.request.use((config) => {
  isLoginRequest = config.url === '/auth/login';
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && !isLoginRequest && window.location.pathname !== '/login') {
      localStorage.removeItem('authToken');
      localStorage.removeItem('authUser');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// ─── Auth ────────────────────────────────────────
export const authApi = {
  login: (employeeCode, password) =>
    api.post('/auth/login', { employeeCode, password }),
  register: (data) => api.post('/auth/register', data),
  me: () => api.get('/auth/me'),
  getEmployees: (params) => api.get('/auth/employees', { params }),
  createEmployee: (data) => api.post('/auth/employees', data),
  updateEmployee: (code, data) => api.put(`/auth/employees/${code}`, data),
  deactivateEmployee: (code) => api.put(`/auth/employees/${code}/deactivate`),
  activateEmployee: (code) => api.put(`/auth/employees/${code}/activate`),
  deleteEmployee: (code) => api.delete(`/auth/employees/${code}`),
};

// ─── Warehouses ──────────────────────────────────
export const warehouseApi = {
  getAll: (params) => api.get('/warehouses', { params }),
  getById: (id) => api.get(`/warehouses/${id}`),
  create: (data) => api.post('/warehouses', data),
  update: (id, data) => api.put(`/warehouses/${id}`, data),
  activate: (id) => api.patch(`/warehouses/${id}/activate`),
  deactivate: (id) => api.patch(`/warehouses/${id}/deactivate`),
  delete: (id) => api.delete(`/warehouses/${id}`),
  search: (params) => api.get('/warehouses/search', { params }),
  getAssignments: (id, params) => api.get(`/warehouses/${id}/assignments`, { params }),
  assignEmployee: (id, data) => api.post(`/warehouses/${id}/assignments`, data),
  getUnassignedEmployees: () => api.get('/warehouses/unassigned-employees'),
  getMyAssignment: () => api.get('/warehouses/my-assignment'),
};

// ─── Products ────────────────────────────────────
export const productApi = {
  getAll: (params) => api.get('/products', { params }),
  getById: (id) => api.get(`/products/${id}`),
  create: (data) => api.post('/products', data),
  update: (id, data) => api.put(`/products/${id}`, data),
  deactivate: (id) => api.patch(`/products/${id}/status`),
  activate: (id) => api.patch(`/products/${id}/status`),
};

// ─── Categories ──────────────────────────────────
export const categoryApi = {
  getAll: (params) => api.get('/categories', { params }),
  getById: (id) => api.get(`/categories/${id}`),
  create: (data) => api.post('/categories', data),
  update: (id, data) => api.put(`/categories/${id}`, data),
};

// ─── Inventory ───────────────────────────────────
export const inventoryApi = {
  getAll: (params) => api.get('/inventories', { params }),
  getById: (id) => api.get(`/inventories/${id}`),
  create: (data) => api.post('/inventories', data),
  deactivate: (id) => api.patch(`/inventories/${id}/status`),
  search: (params) => api.get('/inventories/search', { params }),
};

// ─── Adjustments ─────────────────────────────────
// Backend only has POST - no list endpoint
export const adjustmentApi = {
  create: (data) => api.post('/adjustments', data),
};

// ─── Reservations ────────────────────────────────
// Backend has: POST /reservations, PATCH /{id}/cancel, PATCH /{id}/release
// NO GET list endpoint exists in the backend
export const reservationApi = {
  create: (data) => api.post('/reservations', data),
  cancel: (id) => api.patch(`/reservations/${id}/cancel`),
  release: (id) => api.patch(`/reservations/${id}/release`),
};

// ─── Movements ───────────────────────────────────
export const movementApi = {
  getAll: (params) => api.get('/movements', { params }),
  getById: (id) => api.get(`/movements/${id}`),
  create: (data) => api.post('/movements', data),
  search: (params) => api.get('/movements/search', { params }),
};

// ─── Replenishment ───────────────────────────────
// PATCH /{id}/status?status=APPROVED|REJECTED
export const replenishmentApi = {
  getAll: (params) => api.get('/replenishment', { params }),
  create: (data) => api.post('/replenishment', data),
  updateStatus: (id, status) => api.patch(`/replenishment/${id}/status`, null, { params: { status } }),
  approve: (id) => api.patch(`/replenishment/${id}/status`, null, { params: { status: 'APPROVED' } }),
  reject: (id) => api.patch(`/replenishment/${id}/status`, null, { params: { status: 'REJECTED' } }),
};

// ─── Transfer Requests ──────────────────────────
export const transferRequestApi = {
  create: (data) => api.post('/transfer-requests', data),
  getAll: (params) => api.get('/transfer-requests', { params }),
  getPending: (params) => api.get('/transfer-requests/pending', { params }),
  getMy: (params) => api.get('/transfer-requests/my', { params }),
  getById: (id) => api.get(`/transfer-requests/${id}`),
  approve: (id, data) => api.patch(`/transfer-requests/${id}/approve`, data),
  reject: (id, data) => api.patch(`/transfer-requests/${id}/reject`, data),
  cancel: (id) => api.patch(`/transfer-requests/${id}/cancel`),
  dispatch: (id, data) => api.patch(`/transfer-requests/${id}/dispatch`, data),
  receive: (id, data) => api.patch(`/transfer-requests/${id}/receive`, data),
  getForDispatch: (warehouseId, params) => api.get('/transfer-requests/for-dispatch', { params: { ...params, warehouseId } }),
  getForReceipt: (warehouseId, params) => api.get('/transfer-requests/for-receipt', { params: { ...params, warehouseId } }),
  getStats: () => api.get('/transfer-requests/stats'),
};

export default api;
