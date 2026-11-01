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
      // 已在登录页时不再重复跳转，避免无效 Token 或未登录请求造成刷新循环
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(err)
  }
)

// ====== 带 JWT 的原生 fetch ======
// 后端 JwtAuthFilter 要求所有 /api/* 请求都带 Authorization 头。
// 需要拿到原始响应（如二进制下载、绕过响应拦截器）时用这个函数，不要再裸写 fetch。
export function authFetch(url, options = {}) {
  const token = localStorage.getItem('token')
  const headers = Object.assign({}, options.headers || {})
  if (token) headers.Authorization = 'Bearer ' + token
  return fetch(url, Object.assign({}, options, { headers }))
}

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
  updateStatus: (id, data) => api.put('/material/' + id + '/status', data),
  delete: (id) => api.delete('/material/' + id),
  stats: () => api.get('/material/stats'),
  lowStock: () => api.get('/material/low-stock'),
  logs: (id, params) => api.get('/material/' + id + '/logs', { params }),
  // 库存盘点：按实盘数量批量校正账面数量，差异写入库存流水（盘点调整）
  stockCheck: (data) => api.post('/material/stock-check', data)
}

// ====== 借用 API ======
export const borrowApi = {
  list: (params) => api.get('/borrow', { params }),
  borrow: (data) => api.post('/borrow', data),
  borrowBatch: (data) => api.post('/borrow/batch', data),
  return: (recordId, data) => api.put('/borrow/return/' + recordId, data),
  stats: () => api.get('/borrow/stats'),
  // 退回修改（教师/管理员）与退回后重新提交（申请人）
  returnForEdit: (recordId, reason) => api.put('/borrow/return-edit/' + recordId, { reason }),
  resubmit: (recordId, data) => api.put('/borrow/resubmit/' + recordId, data),
  // 申请人撤回待审批的申请（误提交、临时取消实验）
  cancel: (recordId) => api.put('/borrow/cancel/' + recordId),
  approveBatch: (requestNo) => api.put('/borrow/batch/approve/' + requestNo),
  rejectBatch: (requestNo, reason) => api.put('/borrow/batch/reject/' + requestNo, { reason }),
  returnBatchForEdit: (requestNo, reason) => api.put('/borrow/batch/return-edit/' + requestNo, { reason }),
  cancelBatch: (requestNo) => api.put('/borrow/batch/cancel/' + requestNo)
}

// ====== 培训 / 上机资质 API ======
export const trainingApi = {
  listByMaterial: (materialId) => api.get('/training', { params: { materialId } }),
  all: () => api.get('/training/all'),
  mine: () => api.get('/training/mine'),
  candidates: () => api.get('/training/candidates'),
  grant: (data) => api.post('/training', data),
  revoke: (id) => api.delete('/training/' + id)
}

// ====== 附件 API ======
export const attachmentApi = {
  list: (refType, refId) => api.get('/attachment', { params: { refType, refId } }),
  upload: (refType, refId, file) => {
    const fd = new FormData()
    fd.append('file', file)
    fd.append('refType', refType)
    fd.append('refId', refId)
    return api.post('/attachment', fd)
  },
  remove: (id) => api.delete('/attachment/' + id),
  // 下载/预览走原生 fetch（带 JWT），后端返回文件流
  downloadUrl: (id) => '/api/attachment/' + id + '/download'
}

// ====== 统计分析 API（教师/超管） ======
export const analyticsApi = {
  overview: () => api.get('/analytics')
}

// ====== 采购入库 API ======
export const stockInApi = {
  list: (params) => api.get('/stock-in', { params }),
  detail: (id) => api.get('/stock-in/' + id),
  create: (data) => api.post('/stock-in', data),
  // 编辑：服务端在同一事务内先回退旧明细的库存与流水，再按新明细重新入库
  update: (id, data) => api.put('/stock-in/' + id, data),
  // 撤销：回退库存、删除明细与对应库存流水
  revoke: (id) => api.delete('/stock-in/' + id)
}

// ====== Excel 导入导出 API ======
// 导出走原生 fetch（blob 下载，绕开 JSON 拦截器）
async function downloadFile(url, fallbackName) {
  const res = await authFetch(url)
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
  downloadImportTemplate: () => downloadFile('/api/export/material-import-template', '物资建档导入模板.xlsx'),
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
  list: () => api.get('/notification'),
  unreadCount: () => api.get('/notification/unread'),
  readAll: () => api.put('/notification/read-all'),
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
  // 预约审批（教师 / 超级管理员）
  pending: () => api.get('/reservation/pending'),
  all: (status) => api.get('/reservation/all', { params: { status } }),
  approve: (id, approver) => api.put('/reservation/approve/' + id, { approver }),
  reject: (id, approver, reason) => api.put('/reservation/reject/' + id, { approver, reason }),
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
  // 停用 / 启用账号（停用后不能登录，历史记录保留）
  updateUserStatus: (id, enabled) => api.put('/admin/users/' + id + '/status', { enabled }),
  // 系统配置
  getConfig: () => api.get('/admin/config'),
  saveConfig: (data) => api.post('/admin/config', data),
  // 操作日志
  getLogs: () => api.get('/admin/logs'),
}
