<template>
  <!-- 登录页：全屏无侧边栏 -->
  <router-view v-if="route.meta.public" />

  <!-- 管理员 → 顶部导航布局 -->
  <AdminLayout v-else-if="isAdmin" />

  <!-- 普通用户 → 侧边栏布局 -->
  <el-container v-else style="min-height: 100vh">
    <!-- 侧边栏 -->
    <el-aside width="210px" style="background: #1d1e2c">
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
import { computed, ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { DataBoard, Box, Tickets, TrendCharts, Timer, Calendar, ArrowDown, User, Switch, Setting, Document, Checked } from '@element-plus/icons-vue'
import AdminLayout from './layouts/AdminLayout.vue'
import { authStore } from './stores/auth'

const route = useRoute()
const router = useRouter()
const infoDialog = ref(false)
const pendingCount = ref(0)

const titleMap = {
  '/dashboard': '数据概览',
  '/material': '物资管理',
  '/borrow': '借用记录',
  '/warning': '库存预警',
  '/report': '统计报表',
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

// 待审批数量轮询
function loadPendingCount() {
  fetch('/api/borrow/pending').then(r => r.json()).then(j => {
    pendingCount.value = j.code === 200 && Array.isArray(j.data) ? j.data.length : 0
  }).catch(() => {})
}
onMounted(loadPendingCount)
setInterval(loadPendingCount, 60000)
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

.side-menu { border-right: none !important; }
.side-menu .el-menu-item { height: 50px; line-height: 50px; }
.side-menu .el-menu-item.is-active { background: #409EFF !important; }

.side-footer {
  position: absolute;
  bottom: 16px;
  left: 0;
  right: 0;
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
