<template>
  <div class="login-container">
    <!-- 左侧品牌区 -->
    <div class="login-left">
      <div class="brand-content">
        <div class="brand-logo">
          <span class="logo-emoji">⏱️</span>
          <span class="logo-title">实验室设备预约调度系统</span>
        </div>
        <p class="brand-tagline">Laboratory Equipment Scheduling System</p>

        <!-- SVG 装饰插图 -->
        <div class="illustration">
          <svg viewBox="0 0 400 300" xmlns="http://www.w3.org/2000/svg">
            <!-- 烧杯 -->
            <g transform="translate(50, 60)">
              <path d="M20 10 L20 80 Q20 110 50 110 Q80 110 80 80 L80 10 Z" fill="none" stroke="rgba(255,255,255,0.6)" stroke-width="3"/>
              <path d="M22 60 L78 60" stroke="rgba(255,255,255,0.3)" stroke-width="2"/>
              <path d="M25 80 Q50 75 75 80 L75 90 Q50 95 25 90 Z" fill="rgba(64,158,255,0.4)"/>
              <circle cx="45" cy="85" r="3" fill="rgba(255,255,255,0.5)"/>
              <circle cx="60" cy="82" r="2" fill="rgba(255,255,255,0.4)"/>
            </g>
            <!-- 试管架 -->
            <g transform="translate(160, 50)">
              <rect x="0" y="100" width="80" height="12" rx="3" fill="rgba(255,255,255,0.3)"/>
              <rect x="5" y="10" width="14" height="95" rx="7" fill="none" stroke="rgba(255,255,255,0.5)" stroke-width="2.5"/>
              <rect x="33" y="10" width="14" height="95" rx="7" fill="none" stroke="rgba(255,255,255,0.5)" stroke-width="2.5"/>
              <rect x="61" y="10" width="14" height="95" rx="7" fill="none" stroke="rgba(255,255,255,0.5)" stroke-width="2.5"/>
              <rect x="7" y="70" width="10" height="35" rx="5" fill="rgba(103,194,58,0.5)"/>
              <rect x="35" y="55" width="10" height="50" rx="5" fill="rgba(230,162,60,0.5)"/>
              <rect x="63" y="75" width="10" height="30" rx="5" fill="rgba(64,158,255,0.5)"/>
            </g>
            <!-- 分子结构 -->
            <g transform="translate(280, 80)" opacity="0.7">
              <circle cx="30" cy="30" r="10" fill="rgba(255,255,255,0.3)"/>
              <circle cx="70" cy="50" r="8" fill="rgba(64,158,255,0.4)"/>
              <circle cx="50" cy="80" r="9" fill="rgba(255,255,255,0.25)"/>
              <circle cx="90" cy="20" r="7" fill="rgba(103,194,58,0.4)"/>
              <line x1="30" y1="30" x2="70" y2="50" stroke="rgba(255,255,255,0.4)" stroke-width="2"/>
              <line x1="70" y1="50" x2="50" y2="80" stroke="rgba(255,255,255,0.4)" stroke-width="2"/>
              <line x1="70" y1="50" x2="90" y2="20" stroke="rgba(255,255,255,0.4)" stroke-width="2"/>
              <line x1="30" y1="30" x2="50" y2="80" stroke="rgba(255,255,255,0.3)" stroke-width="2"/>
            </g>
            <!-- 底部波浪 -->
            <path d="M0 280 Q100 260 200 280 T400 280 L400 300 L0 300 Z" fill="rgba(255,255,255,0.08)"/>
          </svg>
        </div>

        <div class="features">
          <div class="feature-item">
            <span class="feature-icon">📅</span>
            <span>时段预约调度</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">✅</span>
            <span>多级审批流程</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">📊</span>
            <span>可视化数据报表</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">⚡</span>
            <span>物资快速借还</span>
          </div>
        </div>
      </div>

      <!-- 背景装饰圆 -->
      <div class="bg-circle bg-circle-1"></div>
      <div class="bg-circle bg-circle-2"></div>
      <div class="bg-circle bg-circle-3"></div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="login-right">
      <div class="form-wrapper">
        <!-- 头像 -->
        <div class="avatar-circle">
          <span>⏱️</span>
        </div>

        <h2 class="form-title">{{ isRegister ? '注册账号' : '欢迎登录' }}</h2>
        <p class="form-subtitle">{{ isRegister ? '创建一个新账号开始使用' : '基于 SpringBoot + Vue 的实验室设备预约调度系统' }}</p>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large" @submit.prevent="handleSubmit">
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="handleSubmit"
            />
          </el-form-item>

          <el-form-item v-if="isRegister" prop="realName">
            <el-input
              v-model="form.realName"
              placeholder="请输入真实姓名"
              :prefix-icon="UserFilled"
              clearable
            />
          </el-form-item>

          <div v-if="!isRegister" class="form-options">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <el-link type="primary" :underline="false" @click="showForgot = !showForgot">忘记密码？</el-link>
          </div>

          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleSubmit"
          >
            {{ isRegister ? '注 册' : '登 录' }}
          </el-button>

          <div class="toggle-mode">
            <span>{{ isRegister ? '已有账号？' : '还没有账号？' }}</span>
            <el-link type="primary" :underline="false" @click="toggleMode">
              {{ isRegister ? '去登录' : '注册新账号' }}
            </el-link>
          </div>
        </el-form>

        <!-- 演示账号 -->
        <div v-if="!isRegister" class="demo-accounts">
          <el-divider content-position="center">
            <span style="font-size: 12px; color: #909399">演示账号（密码均为 123456）</span>
          </el-divider>
          <div class="demo-section">
            <div class="demo-section-title">👑 超级管理员</div>
            <div class="demo-tags">
              <el-tag
                v-for="acc in demoAccounts.filter(a => a.role === 'SUPER_ADMIN')"
                :key="acc.username"
                class="demo-tag demo-tag-super"
                type="danger"
                effect="dark"
                @click="quickFill(acc)"
              >
                {{ acc.label }}
              </el-tag>
            </div>
          </div>
          <div class="demo-section">
            <div class="demo-section-title">🔑 教师</div>
            <div class="demo-tags">
              <el-tag
                v-for="acc in demoAccounts.filter(a => a.role === 'TEACHER')"
                :key="acc.username"
                class="demo-tag"
                type="warning"
                effect="dark"
                @click="quickFill(acc)"
              >
                {{ acc.label }}
              </el-tag>
            </div>
          </div>
          <div class="demo-section">
            <div class="demo-section-title">👤 学生</div>
            <div class="demo-tags">
              <el-tag
                v-for="acc in demoAccounts.filter(a => a.role === 'STUDENT')"
                :key="acc.username"
                class="demo-tag"
                type="info"
                effect="plain"
                @click="quickFill(acc)"
              >
                {{ acc.label }}
              </el-tag>
            </div>
          </div>
        </div>

        <div v-if="showForgot" class="forgot-tip">
          <el-alert type="info" :closable="false" show-icon>
            演示系统暂不支持找回密码，请使用演示账号登录。
          </el-alert>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, UserFilled } from '@element-plus/icons-vue'
import { authApi } from '../api'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const isRegister = ref(false)
const rememberMe = ref(false)
const showForgot = ref(false)

const form = reactive({
  username: '',
  password: '',
  realName: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, message: '用户名至少3个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ],
  realName: [
    { required: false }
  ]
}

const demoAccounts = [
  { username: 'superadmin', label: '超级管理员', role: 'SUPER_ADMIN' },
  { username: 'teacher', label: '王老师', role: 'TEACHER' },
  { username: 'zhangming', label: '张明', role: 'STUDENT' },
  { username: 'lihua', label: '李华', role: 'STUDENT' },
  { username: 'wangfang', label: '王芳', role: 'STUDENT' },
]

const quickFill = (acc) => {
  form.username = acc.username
  form.password = '123456'
  ElMessage.success('已填充账号：' + acc.label)
}

const toggleMode = () => {
  isRegister.value = !isRegister.value
  form.realName = ''
  showForgot.value = false
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    let res
    if (isRegister.value) {
      res = await authApi.register({ ...form })
      ElMessage.success('注册成功，正在登录...')
    } else {
      res = await authApi.login({ username: form.username, password: form.password })
      ElMessage.success('登录成功')
    }

    // 存储用户信息
    const userInfo = {
      id: res.id,
      username: res.username,
      realName: res.realName,
      role: res.role,
      token: res.token
    }
    localStorage.setItem('user', JSON.stringify(userInfo))
    localStorage.setItem('token', res.token)  // JWT 单独存储
    if (rememberMe.value) {
      localStorage.setItem('rememberUser', form.username)
    } else {
      localStorage.removeItem('rememberUser')
    }

    // 触发 storage 事件让 App.vue 同步用户状态
    window.dispatchEvent(new Event('storage'))

    // 跳转
    router.push('/dashboard')
  } catch (err) {
    ElMessage.error(err.message || '操作失败')
  } finally {
    loading.value = false
  }
}

// 恢复记住的用户名
const savedUser = localStorage.getItem('rememberUser')
if (savedUser) {
  form.username = savedUser
  rememberMe.value = true
}
</script>

<style scoped>
.login-container {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
}

/* ====== 左侧品牌区 ====== */
.login-left {
  flex: 1;
  position: relative;
  background: linear-gradient(135deg, #1d1e2c 0%, #2d2e4c 50%, #1a3a5c 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.brand-content {
  position: relative;
  z-index: 2;
  text-align: center;
  padding: 40px;
  max-width: 480px;
}

.brand-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 12px;
}
.logo-emoji { font-size: 40px; }
.logo-title {
  font-size: 26px;
  font-weight: 700;
  color: #fff;
  white-space: nowrap;
}

.brand-tagline {
  color: rgba(255,255,255,0.5);
  font-size: 14px;
  letter-spacing: 2px;
  margin-bottom: 30px;
}

.illustration {
  margin: 20px auto;
  max-width: 400px;
}
.illustration svg {
  width: 100%;
  height: auto;
}

.features {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 24px;
  margin-top: 20px;
  text-align: left;
}
.feature-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(255,255,255,0.8);
  font-size: 14px;
}
.feature-icon { font-size: 20px; }

/* 背景装饰 */
.bg-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.08;
}
.bg-circle-1 {
  width: 400px; height: 400px;
  background: #409EFF;
  top: -100px; right: -80px;
}
.bg-circle-2 {
  width: 300px; height: 300px;
  background: #67C23A;
  bottom: -60px; left: -60px;
}
.bg-circle-3 {
  width: 200px; height: 200px;
  background: #E6A23C;
  top: 40%; right: 20%;
}

/* ====== 右侧表单区 ====== */
.login-right {
  width: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  flex-shrink: 0;
}

.form-wrapper {
  width: 100%;
  max-width: 360px;
  padding: 40px;
}

.avatar-circle {
  width: 76px;
  height: 76px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1d1e2c 0%, #409EFF 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  font-size: 34px;
  box-shadow: 0 10px 30px rgba(64,158,255,0.24);
  transition: transform 0.3s;
}
.avatar-circle:hover {
  transform: scale(1.05);
}

.form-title {
  text-align: center;
  font-size: 26px;
  font-weight: 700;
  color: #1d1e2c;
  margin-bottom: 6px;
  background: linear-gradient(135deg, #1d1e2c 0%, #1662ab 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.form-subtitle {
  text-align: center;
  font-size: 14px;
  color: #909399;
  margin-bottom: 32px;
}

.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.login-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #409EFF 0%, #1d1e2c 100%);
  border: none;
  border-radius: 8px;
  box-shadow: 0 4px 15px rgba(64,158,255,0.35);
  transition: all 0.3s;
}
.login-btn:hover {
  opacity: 0.92;
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(64,158,255,0.45);
}

.toggle-mode {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #909399;
}

.demo-accounts {
  margin-top: 24px;
}
.demo-section {
  margin-bottom: 12px;
}
.demo-section-title {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
  padding-left: 4px;
}
.demo-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}
.demo-tag {
  cursor: pointer;
  transition: all 0.2s;
}
.demo-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.demo-tag-admin {
  font-weight: 600;
  padding: 0 16px !important;
  height: 32px !important;
  line-height: 30px !important;
}
.demo-role-badge {
  font-size: 14px;
  margin-right: 2px;
}

.forgot-tip {
  margin-top: 16px;
}

/* ====== 响应式 ====== */
@media (max-width: 900px) {
  .login-left { display: none; }
  .login-right {
    width: 100%;
    background: linear-gradient(180deg, #1d1e2c 0%, #2d2e4c 100%);
  }
  .form-wrapper {
    background: #fff;
    border-radius: 16px;
    box-shadow: 0 20px 60px rgba(0,0,0,0.3);
    margin: 20px;
  }
}
</style>
