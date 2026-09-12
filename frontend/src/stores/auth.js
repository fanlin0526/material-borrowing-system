import { reactive, computed } from 'vue'
import { authApi } from '../api'

/**
 * 认证状态管理 — 响应式单例
 *
 * 安全原则：用户角色以服务端 /api/auth/info 返回为准，
 * localStorage 仅用于持久化 token（供 API 拦截器携带），不作为权限判断依据。
 */
class AuthStore {
  constructor() {
    this.state = reactive({
      user: null,       // { id, username, realName, role, phone, avatar }
      token: null,
      loaded: false,     // 是否已完成服务端验证
    })
  }

  // ---- 计算属性 ----
  get userInfo()   { return this.state.user }
  get isLoggedIn() { return !!this.state.token && !!this.state.user }
  get isAdmin()    { return this.state.user?.role === 'SUPER_ADMIN' || this.state.user?.role === 'TEACHER' }
  get isSuperAdmin() { return this.state.user?.role === 'SUPER_ADMIN' }
  get role()       { return this.state.user?.role || '' }

  // ---- 初始化：先读 localStorage 快速渲染，再调用服务端获取真实角色 ----
  async init() {
    const token = localStorage.getItem('token')
    if (!token) {
      this.state.loaded = true
      return
    }
    this.state.token = token

    // 先从 localStorage 恢复用户信息（快速渲染，不等网络）
    try {
      const cached = localStorage.getItem('user')
      if (cached) this.state.user = JSON.parse(cached)
    } catch { /* ignore */ }

    // 再从服务端获取真实角色（覆盖可能的伪造数据）
    try {
      const info = await authApi.info()
      if (info) {
        this.state.user = {
          id: info.id,
          username: info.username,
          realName: info.realName,
          role: info.role,        // ← 以服务端为准
          phone: info.phone,
          avatar: info.avatar,
        }
      }
    } catch {
      // Token 无效，清空状态
      this.clear()
    } finally {
      this.state.loaded = true
    }
  }

  // ---- 登录成功后调用 ----
  setUser(info, token) {
    this.state.token = token
    this.state.user = {
      id: info.id,
      username: info.username,
      realName: info.realName,
      role: info.role,
      phone: info.phone,
      avatar: info.avatar,
    }
    localStorage.setItem('token', token)
    localStorage.setItem('user', JSON.stringify(this.state.user)) // 仅用于跨标签页同步
  }

  // ---- 退出 ----
  clear() {
    this.state.user = null
    this.state.token = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    this.state.loaded = true
  }
}

// 单例导出
export const authStore = new AuthStore()
