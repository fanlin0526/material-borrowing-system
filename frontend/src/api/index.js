import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截：自动携带 JWT Token
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = 'Bearer ' + token
  }
  return config
})

// 响应拦截
api.interceptors.response.use(
  res => {
    if (res.data.code === 200) return res.data.data
    return Promise.reject(new Error(res.data.message))
  },
  err => {
    if (err.response && err.response.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

// ====== 认证 API ======
export const authApi = {
  login: (data) => api.post('/auth/login', data),
  register: (data) => api.post('/auth/register', data),
  info: () => api.get('/auth/info'),
  updateProfile: (data) => api.put('/auth/profile', data),
  updatePassword: (data) => api.put('/auth/password', data)
}

// ====== 仪表盘 API ======
export const dashboardApi = {
  overview: () => api.get('/dashboard'),
  detail: () => api.get('/dashboard/detail'),
}

// ====== 物资 API ======
export const materialApi = {
  list: (params) => api.get('/material', { params }),
  get: (id) => api.get('/material/' + id),
  add: (data) => api.post('/material', data),
  update: (id, data) => api.put('/material/' + id, data),
  delete: (id) => api.delete('/material/' + id),
  stats: () => api.get('/material/stats'),
  lowStock: () => api.get('/material/low-stock')
}

// ====== 借用 API ======
export const borrowApi = {
  list: (params) => api.get('/borrow', { params }),
  borrow: (data) => api.post('/borrow', data),
  batchBorrow: (data) => api.post('/borrow/batch', data),
  return: (recordId, data) => api.put('/borrow/return/' + recordId, data),
  stats: () => api.get('/borrow/stats')
}

// ====== 通知 API ======
export const notificationApi = {
  list: (username) => api.get('/notification', { params: { username } }),
  unreadCount: (username) => api.get('/notification/unread', { params: { username } }),
  readAll: (username) => api.put('/notification/read-all', null, { params: { username } }),
  readOne: (id) => api.put('/notification/read/' + id),
}

// ====== 预约 API ======
export const reservationApi = {
  create: (data) => api.post('/reservation', data),
  createRecurring: (data) => api.post('/reservation/recurring', data),
  checkConflict: (data) => api.post('/reservation/check-conflict', data),
  weekOverview: (from, to) => api.get('/reservation/week', { params: { from, to } }),
  weekReservations: (materialId, from, to) => api.get('/reservation/week/' + materialId, { params: { from, to } }),
  cancel: (id) => api.put('/reservation/cancel/' + id),
  myReservations: (username) => api.get('/reservation/my', { params: { username } }),
}

// ====== 分类管理 API ======
export const categoryApi = {
  list: () => api.get('/category'),
  get: (id) => api.get('/category/' + id),
  add: (data) => api.post('/category', data),
  update: (id, data) => api.put('/category/' + id, data),
  delete: (id) => api.delete('/category/' + id),
}

// ====== 管理员 API（超管专属） ======
export const adminApi = {
  // 用户管理
  listUsers: (keyword) => api.get('/admin/users', { params: { keyword } }),
  addUser: (data) => api.post('/admin/users', data),
  editUser: (id, data) => api.put('/admin/users/' + id, data),
  resetPassword: (id) => api.put('/admin/users/' + id + '/reset-password'),
  deleteUser: (id) => api.delete('/admin/users/' + id),
  // 系统配置
  getConfig: () => api.get('/admin/config'),
  saveConfig: (data) => api.post('/admin/config', data),
  // 操作日志
  getLogs: () => api.get('/admin/logs'),
}
