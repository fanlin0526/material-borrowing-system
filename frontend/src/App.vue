<template>
  <!-- 登录页：全屏无侧边栏 -->
  <router-view v-if="route.meta.public" />

  <!-- 管理员 → 顶部导航布局 -->
  <AdminLayout v-else-if="isAdmin" />

  <!-- 普通用户 → 侧边栏布局 -->
  <el-container v-else style="min-height: 100vh">
    <!-- 侧边栏 -->
    <el-aside width="210px" style="background: #1d1e2c; display: flex; flex-direction: column">
      <div class="logo">
        <span class="logo-icon">🧪</span>
        <span class="logo-text">实验室物资系统</span>
      </div>
      <el-menu
        :default-active="route.path"
        router
        class="side-menu"
        background-color="#1d1e2c"
        text-color="#a0a3bd"
        active-text-color="#ffffff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataBoard /></el-icon>
          <span>数据概览</span>
        </el-menu-item>
        <el-menu-item index="/material">
          <el-icon><Box /></el-icon>
          <span>物资管理</span>
        </el-menu-item>
        <el-menu-item index="/borrow">
          <el-icon><Tickets /></el-icon>
          <span>借用记录</span>
        </el-menu-item>
        <el-menu-item index="/reservation">
          <el-icon><Timer /></el-icon>
          <span>预约管理</span>
        </el-menu-item>
        <el-menu-item v-if="isAdmin" index="/report">
          <el-icon><TrendCharts /></el-icon>
          <span>统计报表</span>
        </el-menu-item>
        <el-menu-item v-if="isAdmin" index="/approval">
          <el-icon><Checked /></el-icon>
          <span>审批管理</span>
          <el-badge v-if="pendingCount > 0" :value="pendingCount" style="margin-left: 8px" />
        </el-menu-item>
        <el-menu-item v-if="isSuperAdmin" index="/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item v-if="isSuperAdmin" index="/config">
          <el-icon><Setting /></el-icon>
          <span>系统配置</span>
        </el-menu-item>
        <el-menu-item v-if="isSuperAdmin" index="/logs">
          <el-icon><Document /></el-icon>
          <span>操作日志</span>
        </el-menu-item>
      </el-menu>
      <div class="side-footer">
        <p>实验室物资借用管理系统</p>
        <p class="version">v2.0 · 前后端分离</p>
      </div>
    </el-aside>

    <!-- 主体区域 -->
    <el-container>
      <el-header style="background: #fff; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 1px 4px rgba(0,0,0,0.08); z-index: 10">
        <div class="header-left">
          <span class="page-title">{{ currentTitle }}</span>
        </div>
        <div class="header-right">
          <el-popover placement="bottom" :width="340" trigger="click" @show="loadNotifications">
            <template #reference>
              <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
                <el-button :icon="Bell" circle size="small" />
              </el-badge>
            </template>
            <div style="max-height: 360px; overflow-y: auto">
              <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px">
                <b style="font-size: 14px">消息通知</b>
                <el-button v-if="unreadCount > 0" type="primary" link size="small" @click="markAllRead">全部已读</el-button>
              </div>
              <div v-if="notifications.length === 0" style="text-align: center; color: #909399; padding: 20px">暂无通知</div>
              <div v-for="n in notifications" :key="n.id" class="notif-item" :class="{ unread: !n.isRead }"
                @click="markOneRead(n)">
                <div class="notif-title">
                  <el-tag size="small" effect="dark">{{ notifTypeLabel(n.type) }}</el-tag>
                  <span class="notif-time">{{ formatNotifTime(n.createTime) }}</span>
                </div>
                <div class="notif-body">{{ n.content }}</div>
              </div>
            </div>
          </el-popover>
          <el-tag type="info" effect="plain" size="large">
            <el-icon style="margin-right: 4px; vertical-align: middle"><Calendar /></el-icon>
            {{ today }}
          </el-tag>

          <!-- 用户信息下拉菜单 -->
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <div class="user-avatar">
                {{ currentUser ? currentUser.realName.charAt(0) : '?' }}
              </div>
              <div class="user-detail">
                <span class="user-name">{{ currentUser ? currentUser.realName : '未登录' }}</span>
                <span class="user-role">{{ currentUser ? roleLabel(currentUser.role) : '' }}</span>
              </div>
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
      <el-main style="background: #f0f2f5; padding: 24px">
        <router-view />
      </el-main>
    </el-container>

    <!-- 个人信息弹窗 -->
    <el-dialog v-model="infoDialog" title="个人信息" width="400px">
      <div class="info-card" v-if="currentUser">
        <div class="info-avatar-large">
          {{ currentUser.realName.charAt(0) }}
        </div>
        <div class="info-list">
          <div class="info-row">
            <span class="info-label">用户名</span>
            <span class="info-value">{{ currentUser.username }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">姓名</span>
            <span class="info-value">{{ currentUser.realName }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">角色</span>
            <span class="info-value">
              <el-tag :type="roleTagType(currentUser.role)" size="small">
                {{ roleLabel(currentUser.role) }}
              </el-tag>
            </span>
          </div>
        </div>
      </div>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { computed, ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { DataBoard, Box, Tickets, TrendCharts, Timer, Calendar, ArrowDown, User, Switch, Setting, Document, Checked, Bell } from '@element-plus/icons-vue'
import AdminLayout from './layouts/AdminLayout.vue'
import { authFetch, notificationApi } from './api'
import { authStore } from './stores/auth'

const route = useRoute()
const router = useRouter()
const infoDialog = ref(false)
const pendingCount = ref(0)
const unreadCount = ref(0)
const notifications = ref([])

const titleMap = {
  '/dashboard': '数据概览',
  '/material': '物资管理',
  '/borrow': '借用记录',
  '/reservation': '预约管理',
  '/warning': '库存预警',
  '/report': '统计报表',
  '/approval': '审批管理',
  '/stock-in': '采购入库',
  '/categories': '分类管理',
  '/users': '用户管理',
  '/config': '系统配置',
  '/logs': '操作日志'
}
const currentTitle = computed(() => titleMap[route.path] || '实验室物资借用管理系统')

const today = new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })

// 响应式用户信息 — 以 authStore 为准
const currentUser = computed(() => authStore.userInfo)
const isAdmin = computed(() => authStore.isAdmin)
const isSuperAdmin = computed(() => authStore.isSuperAdmin)

const roleLabel = (role) => {
  const map = { SUPER_ADMIN: '超级管理员', TEACHER: '教师', STUDENT: '学生' }
  return map[role] || role
}

const roleTagType = (role) => {
  const map = { SUPER_ADMIN: 'danger', TEACHER: 'warning', STUDENT: 'info' }
  return map[role] || 'info'
}

const handleCommand = (cmd) => {
  if (cmd === 'info') {
    infoDialog.value = true
  } else if (cmd === 'switchAccount') {
    authStore.clear()
    ElMessage.success('已切换账号，请重新登录')
    router.push('/login')
  }
}

// 待审批数量轮询（借用申请 + 延期申请 + 预约申请；仅管理端需要，学生端直接跳过）
function loadPendingCount() {
  if (!isAdmin.value) return
  authFetch('/api/borrow/pending').then(r => r.json()).then(j => {
    return j.code === 200 && Array.isArray(j.data) ? j.data.length : 0
  }).catch(() => 0).then(borrowCnt => {
    return authFetch('/api/reservation/pending').then(r => r.json()).then(j => {
      const resvCnt = j.code === 200 && Array.isArray(j.data) ? j.data.length : 0
      pendingCount.value = borrowCnt + resvCnt
    }).catch(() => { pendingCount.value = borrowCnt })
  })
}
onMounted(loadPendingCount)
onMounted(() => {
  // 登录页尚未持有 Token 时不请求需要认证的通知接口，避免 401 触发整页刷新
  if (!authStore.isLoggedIn) return
  notificationApi.unreadCount().then(data => { unreadCount.value = data.count || 0 }).catch(() => {})
})
// 轮询句柄登记，卸载时清理，避免切换账号/退出后定时器继续跑
const pendingTimer = setInterval(loadPendingCount, 60000)
onBeforeUnmount(() => clearInterval(pendingTimer))

function loadNotifications() {
  notificationApi.list().then(data => {
    notifications.value = Array.isArray(data) ? data.slice(0, 20) : []
  }).catch(() => {})
  notificationApi.unreadCount().then(data => {
    unreadCount.value = data.count || 0
  }).catch(() => {})
}
function markAllRead() {
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
  return ({ OVERDUE: '逾期提醒', DAMAGE: '损坏通知', SYSTEM: '系统通知' })[t] || t || '通知'
}
function formatNotifTime(t) {
  if (!t) return ''
  const d = new Date(t)
  return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: 'Microsoft YaHei', 'Segoe UI', sans-serif; }

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 20px;
  color: #fff;
  border-bottom: 1px solid rgba(255,255,255,0.08);
}
.logo-icon { font-size: 24px; }
.logo-text { font-size: 16px; font-weight: 600; white-space: nowrap; }

.side-menu { border-right: none !important; flex: 1 1 auto; overflow-y: auto; }
.side-menu .el-menu-item { height: 50px; line-height: 50px; }
.side-menu .el-menu-item.is-active { background: #409EFF !important; }

/* 角标：原先用 position:absolute 但父级 el-aside 没有定位，导致它脱离侧边栏
   飘到右侧内容区（叠在预约表上）。改为 flex 布局自然置底，不再脱离文档流。 */
.side-footer {
  flex-shrink: 0;
  margin-top: auto;
  padding: 16px 0;
  text-align: center;
  color: #6c6f8a;
  font-size: 12px;
  line-height: 1.8;
}
.side-footer .version { font-size: 11px; opacity: 0.7; }

.page-title { font-size: 18px; font-weight: 600; color: #303133; }
.header-right { display: flex; align-items: center; gap: 12px; }

/* 用户信息 */
.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 8px;
  transition: background 0.2s;
}
.user-info:hover { background: #f5f7fa; }

.notif-item {
  padding: 10px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  border-radius: 6px;
}
.notif-item.unread { background: #ecf5ff; }
.notif-item:hover { background: #f5f7fa; }
.notif-title { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.notif-time { color: #909399; font-size: 11px; }
.notif-body { color: #606266; font-size: 13px; line-height: 1.55; }

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409EFF, #2d2e4c);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
}

.user-detail {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
}
.user-name { font-size: 14px; font-weight: 600; color: #303133; }
.user-role { font-size: 12px; color: #909399; }

/* 个人信息弹窗 */
.info-card {
  text-align: center;
  padding: 10px 0;
}
.info-avatar-large {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409EFF, #2d2e4c);
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
</style>
