<template>
  <el-container style="min-height: 100vh; flex-direction: column" :class="'layout-' + roleClass">
    <!-- 顶部导航栏 -->
    <el-header class="admin-topbar" height="56px" :class="'topbar-' + roleClass">
      <!-- 左侧 LOGO -->
      <div class="topbar-left">
        <span class="topbar-logo">🧪</span>
        <span class="topbar-title">实验物资借用系统</span>
        <el-tag :type="adminBadgeType" size="small" effect="dark" class="admin-badge">{{ adminBadgeText }}</el-tag>
      </div>

      <!-- 中间 Tab 导航 -->
      <nav class="topbar-tabs">
        <div
          v-for="tab in tabs"
          :key="tab.path"
          class="topbar-tab"
          :class="{ active: currentPath === tab.path }"
          @click="$router.push(tab.path)"
        >
          <el-icon><component :is="tab.icon" /></el-icon>
          <span>{{ tab.label }}</span>
          <el-badge
            v-if="tab.badge > 0"
            :value="tab.badge"
            class="tab-badge"
            :type="tab.badgeType || 'danger'"
          />
        </div>
      </nav>

      <!-- 右侧用户区 -->
      <div class="topbar-right">
        <span class="current-date">{{ today }}</span>
        <el-popover placement="bottom" :width="340" trigger="click" @show="loadNotifications">
          <template #reference>
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="notification-bell">
              <el-button :icon="Bell" circle size="small" />
            </el-badge>
          </template>
          <div style="max-height: 360px; overflow-y: auto">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px">
              <b style="font-size: 14px">消息通知</b>
              <el-button v-if="unreadCount > 0" type="primary" link size="small" @click="markAllRead">全部已读</el-button>
            </div>
            <div v-if="notifications.length === 0" style="text-align: center; color: #909399; padding: 20px">
              暂无通知
            </div>
            <div v-for="n in notifications" :key="n.id" class="notif-item" :class="{ unread: !n.isRead }"
              @click="markOneRead(n)">
              <div class="notif-title">
                <el-tag :type="notifTypeTag(n.type)" size="small" effect="dark">{{ notifTypeLabel(n.type) }}</el-tag>
                <span class="notif-time">{{ formatNotifTime(n.createTime) }}</span>
              </div>
              <div class="notif-body">{{ n.content }}</div>
            </div>
          </div>
        </el-popover>
        <!-- 超管工具菜单 -->
        <el-dropdown v-if="isSuperAdmin" trigger="click" @command="handleAdminCommand">
          <el-button :icon="Setting" circle size="small" />
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="users" :icon="User">用户管理</el-dropdown-item>
              <el-dropdown-item command="categories" :icon="Box">分类管理</el-dropdown-item>
              <el-dropdown-item command="config" :icon="Setting">系统配置</el-dropdown-item>
              <el-dropdown-item command="logs" :icon="Document">操作日志</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-dropdown trigger="click" @command="handleCommand">
          <div class="topbar-user">
            <div class="user-avatar">{{ userInitial }}</div>
            <span class="user-name">{{ userName }}</span>
            <el-icon><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="info" :icon="User">
                <span>个人信息</span>
              </el-dropdown-item>
              <el-dropdown-item command="switchAccount" :icon="Switch" divided>
                <span>切换账号</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <!-- 内容区 -->
    <el-main class="admin-main">
      <router-view />
    </el-main>

    <!-- 个人信息弹窗 -->
    <el-dialog v-model="infoDialog" title="个人信息" width="440px" center>
      <div v-if="currentUser" style="text-align: center">
        <div class="info-avatar-large">{{ userInitial }}</div>
        <el-form :model="profileForm" label-width="70px" style="margin-top: 20px; text-align: left">
          <el-form-item label="用户名">
            <el-input :model-value="currentUser.username" disabled />
          </el-form-item>
          <el-form-item label="姓名">
            <el-input v-model="profileForm.realName" placeholder="请输入姓名" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="角色">
            <el-tag :type="adminBadgeType" size="small">{{ adminBadgeText }}</el-tag>
          </el-form-item>
        </el-form>
        <el-button type="primary" style="width: 100%; margin-top: 8px" @click="handleSaveProfile" :loading="profileSaving">保存资料</el-button>

        <el-divider>修改密码</el-divider>
        <el-form :model="passwordForm" label-width="70px" style="text-align: left">
          <el-form-item label="原密码">
            <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="至少6位" />
          </el-form-item>
          <el-form-item label="确认密码">
            <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
          </el-form-item>
        </el-form>
        <el-button type="warning" style="width: 100%" @click="handleChangePassword" :loading="passwordSaving">修改密码</el-button>
      </div>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { computed, ref, reactive, watch, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  DataBoard, Box, Tickets, TrendCharts, Timer, ShoppingCart,
  ArrowDown, User, Switch, Bell, Setting, Document, Checked
} from '@element-plus/icons-vue'
import { notificationApi, authApi, authFetch } from '../api'
import { authStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const infoDialog = ref(false)
const profileSaving = ref(false)
const passwordSaving = ref(false)
const profileForm = reactive({ realName: '', phone: '' })
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const unreadCount = ref(0)
const notifications = ref([])
const pendingCount = ref(0)

const tabs = computed(() => {
  const base = [
    { label: '数据大盘', path: '/dashboard', icon: DataBoard },
    { label: '物资管理', path: '/material', icon: Box },
    { label: '借用记录', path: '/borrow', icon: Tickets },
  ]
  const role = currentUser.value?.role
  if (role === 'SUPER_ADMIN') {
    // 管理员：全部功能
    return [
      ...base,
      { label: '采购入库', path: '/stock-in', icon: ShoppingCart },
      { label: '预约管理', path: '/reservation', icon: Timer },
      { label: '统计报表', path: '/report', icon: TrendCharts },
      { label: '审批管理', path: '/approval', icon: Checked, badge: pendingCount.value, badgeType: 'danger' },
    ]
  }
  if (role === 'TEACHER') {
    // 教师：审批为核心
    return [
      ...base,
      { label: '采购入库', path: '/stock-in', icon: ShoppingCart },
      { label: '预约管理', path: '/reservation', icon: Timer },
      { label: '审批管理', path: '/approval', icon: Checked, badge: pendingCount.value, badgeType: 'danger' },
      { label: '统计报表', path: '/report', icon: TrendCharts },
    ]
  }
  // 学生：借用+预约为核心
  return [
    ...base,
    { label: '预约管理', path: '/reservation', icon: Timer },
  ]
})

const currentPath = computed(() => route.path)

const today = new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })

// 用户信息 — 以 authStore 为准
const currentUser = computed(() => authStore.userInfo)

const userInitial = computed(() => {
  return currentUser.value ? currentUser.value.realName.charAt(0) : '?'
})

const userName = computed(() => {
  return currentUser.value ? currentUser.value.realName : '未登录'
})

const isSuperAdmin = computed(() => currentUser.value?.role === 'SUPER_ADMIN')
const roleClass = computed(() => isSuperAdmin.value ? 'super' : 'teacher')

const adminBadgeText = computed(() => {
  const role = currentUser.value?.role
  if (role === 'SUPER_ADMIN') return '超级管理员'
  if (role === 'TEACHER') return '教师'
  return '管理员'
})

const adminBadgeType = computed(() => {
  const role = currentUser.value?.role
  if (role === 'SUPER_ADMIN') return 'danger'
  if (role === 'TEACHER') return 'warning'
  return 'warning'
})

// ====== 通知 ======
function loadNotifications() {
  const u = currentUser.value
  if (!u || !u.username) return
  notificationApi.list().then(data => {
    notifications.value = Array.isArray(data) ? data.slice(0, 20) : []
  }).catch(() => {})
  notificationApi.unreadCount().then(data => {
    unreadCount.value = data.count || 0
  }).catch(() => {})
}
function markAllRead() {
  const u = currentUser.value
  if (!u || !u.username) return
  notificationApi.readAll().then(() => {
    unreadCount.value = 0
    notifications.value.forEach(n => n.isRead = true)
  }).catch(() => {})
}
function markOneRead(n) {
  if (!n || n.isRead) return
  notificationApi.readOne(n.id).then(() => {
    n.isRead = true
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }).catch(() => {})
}
function notifTypeLabel(t) {
  const map = { OVERDUE: '逾期提醒', DAMAGE: '损坏通知', SYSTEM: '系统通知' }
  return map[t] || t
}
function notifTypeTag(t) {
  const map = { OVERDUE: 'danger', DAMAGE: 'warning', SYSTEM: 'info' }
  return map[t] || 'info'
}
function formatNotifTime(t) {
  if (!t) return ''
  const d = new Date(t)
  return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}
// 每分钟刷新待审批数（借用申请 + 延期申请 + 预约申请）
function loadPendingCount() {
  authFetch('/api/borrow/pending').then(r => r.json()).then(j => {
    return j.code === 200 && Array.isArray(j.data) ? j.data.length : 0
  }).catch(() => 0).then(borrowCnt => {
    return authFetch('/api/reservation/pending').then(r => r.json()).then(j => {
      const resvCnt = j.code === 200 && Array.isArray(j.data) ? j.data.length : 0
      pendingCount.value = borrowCnt + resvCnt
    }).catch(() => { pendingCount.value = borrowCnt })
  })
}
loadPendingCount()
// 轮询句柄统一登记，组件卸载时一并清理：
// 切换账号会重建整个顶栏布局，不清理会不断累积「僵尸」定时器
const timers = [
  setInterval(loadPendingCount, 60000),
  setInterval(() => {
    const u = currentUser.value
    if (u && u.username) {
      notificationApi.unreadCount().then(data => {
        unreadCount.value = data.count || 0
      }).catch(() => {})
    }
  }, 60000)
]
onBeforeUnmount(() => timers.forEach(clearInterval))

const handleCommand = (cmd) => {
  if (cmd === 'info') {
    // 加载最新资料
    profileForm.realName = currentUser.value.realName || ''
    profileForm.phone = currentUser.value.phone || ''
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    infoDialog.value = true
  } else if (cmd === 'switchAccount') {
    authStore.clear()
    ElMessage.success('已切换账号，请重新登录')
    router.push('/login')
  }
}

const handleAdminCommand = (cmd) => {
  router.push('/' + cmd)
}

// ====== 个人信息管理 ======
function handleSaveProfile() {
  if (!profileForm.realName.trim()) {
    ElMessage.warning('姓名不能为空')
    return
  }
  profileSaving.value = true
  authApi.updateProfile({ realName: profileForm.realName, phone: profileForm.phone })
    .then(data => {
      // 更新 authStore 中的用户信息
      if (authStore.state.user) {
        authStore.state.user.realName = data.realName
        authStore.state.user.phone = data.phone
      }
      // 同步 localStorage（用于跨标签页恢复）
      const user = JSON.parse(localStorage.getItem('user') || '{}')
      user.realName = data.realName
      user.phone = data.phone
      localStorage.setItem('user', JSON.stringify(user))
      ElMessage.success('资料已保存')
      infoDialog.value = false
    })
    .catch(e => ElMessage.error(e.message))
    .finally(() => profileSaving.value = false)
}

function handleChangePassword() {
  if (!passwordForm.oldPassword) { ElMessage.warning('请输入原密码'); return }
  if (!passwordForm.newPassword) { ElMessage.warning('请输入新密码'); return }
  if (passwordForm.newPassword.length < 6) { ElMessage.warning('新密码至少6位'); return }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  passwordSaving.value = true
  authApi.updatePassword({
    oldPassword: passwordForm.oldPassword,
    newPassword: passwordForm.newPassword
  }).then(() => {
    ElMessage.success('密码已修改，请重新登录')
    authStore.clear()
    setTimeout(() => router.push('/login'), 1000)
  }).catch(e => ElMessage.error(e.message))
    .finally(() => passwordSaving.value = false)
}
</script>

<style scoped>
/* ====== 顶栏整体 ====== */
.admin-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  z-index: 100;
  padding: 0 24px;
  position: sticky;
  top: 0;
}

/* ====== 左侧 LOGO ====== */
.topbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  margin-right: 32px;
}
.topbar-logo {
  font-size: 26px;
}
.topbar-title {
  font-size: 15px;
  font-weight: 700;
  color: #1d1e2c;
  white-space: nowrap;
}
.admin-badge {
  margin-left: 6px;
  font-weight: 600;
  letter-spacing: 1px;
}

/* ====== 中间 Tab 导航 ====== */
.topbar-tabs {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
  justify-content: center;
  height: 100%;
  min-width: 0;
}
.topbar-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 18px;
  height: 56px;
  cursor: pointer;
  color: #606266;
  font-size: 14px;
  border-bottom: 3px solid transparent;
  transition: all 0.2s;
  position: relative;
  white-space: nowrap;
  flex: 0 0 auto;
}
.topbar-tab:hover {
  color: #409EFF;
  background: rgba(64, 158, 255, 0.04);
}
.topbar-tab.active {
  color: #409EFF;
  font-weight: 600;
  border-bottom-color: #409EFF;
}
.tab-badge {
  margin-left: -2px;
}
.tab-badge :deep(.el-badge__content) {
  transform: scale(0.8);
}

/* ====== 右侧用户区 ====== */
.topbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
  margin-left: 24px;
}
.current-date {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
}
.topbar-user {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 8px;
  transition: background 0.2s;
}
.topbar-user:hover {
  background: #f5f7fa;
}
.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409EFF, #1d1e2c);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
}
.user-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

/* ====== 通知铃铛 ====== */
.notification-bell {
  cursor: pointer;
}
.notif-item {
  padding: 10px 12px;
  border-bottom: 1px solid #f0f0f0;
  cursor: default;
}
.notif-item:last-child { border-bottom: none; }
.notif-item.unread { background: #ecf5ff; }
.notif-item { cursor: pointer; border-radius: 6px; }
.notif-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}
.notif-time { font-size: 11px; color: #c0c4cc; }
.notif-body { font-size: 13px; color: #606266; line-height: 1.5; }

/* ====== 内容区 ====== */
.admin-main {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
  background: #f0f2f5;
  width: 100%;
  flex: 1;
}

/* ====== 个人信息弹窗 ====== */
.info-card {
  text-align: center;
  padding: 10px 0;
}
.info-avatar-large {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409EFF, #1d1e2c);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  font-weight: 600;
  margin: 0 auto 20px;
}
.info-list {
  text-align: left;
  max-width: 280px;
  margin: 0 auto;
}
.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #ebeef5;
}
.info-label { color: #909399; font-size: 14px; }
.info-value { color: #303133; font-size: 14px; font-weight: 500; }

/* ====== 角色视觉主题 ====== */
/* 顶栏顶部色条 */
.topbar-super { border-top: 3px solid #C62828; }
.topbar-teacher { border-top: 3px solid #2E7D32; }

/* 顶栏logo区域色调 */
.topbar-super .topbar-logo { color: #C62828; }
.topbar-teacher .topbar-logo { color: #2E7D32; }

/* 当前激活tab下划线颜色 */
.topbar-super .topbar-tab.active { border-bottom-color: #C62828; color: #C62828; }
.topbar-teacher .topbar-tab.active { border-bottom-color: #2E7D32; color: #2E7D32; }

/* tab悬浮色 */
.topbar-super .topbar-tab:hover { color: #C62828; background: rgba(198,40,40,0.04); }
.topbar-teacher .topbar-tab:hover { color: #2E7D32; background: rgba(46,125,50,0.04); }

/* 内容区背景色微差 */
.layout-super .admin-main { background: #fafafa; }
.layout-teacher .admin-main { background: #f0f5f0; }

/* 中等宽度屏幕下允许导航横向滚动，避免图标和文字挤在一起 */
@media (max-width: 1500px) {
  .topbar-left { margin-right: 16px; }
  .topbar-tabs {
    justify-content: flex-start;
    overflow-x: auto;
    scrollbar-width: thin;
  }
  .current-date { display: none; }
}

/* badge标签色彩 */
.topbar-super .admin-badge { background: #C62828 !important; border-color: #C62828 !important; }
.topbar-teacher .admin-badge { background: #2E7D32 !important; border-color: #2E7D32 !important; }
</style>
