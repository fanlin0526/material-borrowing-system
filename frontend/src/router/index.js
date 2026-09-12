import { createRouter, createWebHistory } from 'vue-router'
import { authStore } from '../stores/auth'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue'), meta: { public: true } },
  { path: '/', redirect: '/dashboard' },
  { path: '/dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue') },
  { path: '/material', name: 'Material', component: () => import('../views/MaterialList.vue') },
  { path: '/borrow', name: 'Borrow', component: () => import('../views/BorrowRecord.vue') },
  { path: '/reservation', name: 'Reservation', component: () => import('../views/Reservation.vue') },
  // 教师/管理员专属
  { path: '/report', name: 'Report', component: () => import('../views/Statistics.vue'),
    meta: { roles: ['SUPER_ADMIN', 'TEACHER'] } },
  // 教师/管理员专属 - 审批
  { path: '/approval', name: 'Approval', component: () => import('../views/ApprovalManagement.vue'),
    meta: { roles: ['SUPER_ADMIN', 'TEACHER'] } },
  // 超级管理员专属
  { path: '/users', name: 'UserManagement', component: () => import('../views/UserManagement.vue'),
    meta: { roles: ['SUPER_ADMIN'] } },
  { path: '/config', name: 'SystemConfig', component: () => import('../views/SystemConfig.vue'),
    meta: { roles: ['SUPER_ADMIN'] } },
  { path: '/logs', name: 'OperationLog', component: () => import('../views/OperationLog.vue'),
    meta: { roles: ['SUPER_ADMIN'] } },
  { path: '/categories', name: 'CategoryManagement', component: () => import('../views/CategoryManagement.vue'),
    meta: { roles: ['SUPER_ADMIN'] } },
  // 教师/管理员专属 - 采购入库
  { path: '/stock-in', name: 'StockIn', component: () => import('../views/StockIn.vue'),
    meta: { roles: ['SUPER_ADMIN', 'TEACHER'] } },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局路由守卫 — 角色以服务端验证结果为准
router.beforeEach((to, from, next) => {
  if (!to.meta.public && !authStore.isLoggedIn) {
    next('/login')
    return
  }
  if (to.meta.roles) {
    if (!to.meta.roles.includes(authStore.role)) {
      next('/dashboard')
      return
    }
  }
  next()
})

export default router
