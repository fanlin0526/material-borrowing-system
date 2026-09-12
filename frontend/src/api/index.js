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
  lowStock: () => api.get('/material/low-stock'),
  logs: (id, params) => api.get('/material/' + id + '/logs', { params })
}

// ====== 借用 API ======
export const borrowApi = {
  list: (params) => api.get('/borrow', { params }),
  borrow: (data) => api.post('/borrow', data),
  return: (recordId, data) => api.put('/borrow/return/' + recordId, data),
  stats: () => api.get('/borrow/stats')
}

// ====== 采购入库 API ======
export const stockInApi = {
  list: (params) => api.get('/stock-in', { params }),
  detail: (id) => api.get('/stock-in/' + id),
  create: (data) => api.post('/stock-in', data)
}

// ====== Excel 导入导出 API ======
// 导出走原生 fetch（blob 下载，绕开 JSON 拦截器）
async function downloadFile(url, fallbackName) {
  const token = localStorage.getItem('token')
  const res = await fetch(url, { headers: token ? { Authorization: 'Bearer ' + token } : {} })
  if (!res.ok) throw new Error('导出失败（HTTP ' + res.status + '）')
  const blob = await res.blob()
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = fallbackName
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(a.href)
}
const nowStr = () => {
  const d = new Date()
  const p = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}${p(d.getMonth() + 1)}${p(d.getDate())}_${p(d.getHours())}${p(d.getMinutes())}${p(d.getSeconds())}`
}
export const excelApi = {
  exportMaterials: (params) => downloadFile('/api/export/materials' + toQuery(params), `物资清单_${nowStr()}.xlsx`),
  exportBorrows: (params) => downloadFile('/api/export/borrows' + toQuery(params), `借用记录_${nowStr()}.xlsx`),
  importMaterials: (file) => {
    const fd = new FormData()
    fd.append('file', file)
    return api.post('/import/materials', fd)
  }
}
function toQuery(params) {
  if (!params) return ''
  const qs = new URLSearchParams()
  Object.entries(params).forEach(([k, v]) => { if (v) qs.append(k, v) })
  const s = qs.toString()
  return s ? '?' + s : ''
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
