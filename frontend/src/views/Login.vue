#独立全屏登录/注册页，不带任何导航#
<template>
  <div class="login-container">
    <!-- 左侧品牌区 -->
    <div class="login-left">
      <div class="brand-content">
        <div class="brand-logo">
          <span class="logo-emoji">📦</span>
          <span class="logo-title">基于SpringBoot+Vue的实验物资借用系统的设计与实现</span>
        </div>
        <p class="brand-tagline">Experimental Material Borrowing Management System</p>

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
          <span>📦</span>
        </div>

        <h2 class="form-title" :class="{ 'title-alone': !isRegister }">{{ isRegister ? '注册账号' : '欢迎登录' }}</h2>
        <p v-if="isRegister" class="form-subtitle">注册固定为学生账号，借用 / 领用 / 预约需教师审批</p>

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

          <el-form-item v-if="isRegister" prop="department">
            <el-input
              v-model="form.department"
              placeholder="所属学院 / 部门（用于统计与审批归属）"
              :prefix-icon="UserFilled"
              clearable
            />
          </el-form-item>

          <el-form-item v-if="isRegister" prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="handleSubmit"
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

        <div v-if="showForgot" class="forgot-tip">
          <el-alert type="info" :closable="false" show-icon>
            系统暂不支持自助找回密码，请联系管理员重置。
          </el-alert>
        </div>
      </div>
    </div>

    <!-- 首次登录（管理员重置过密码）强制改密：不可关闭 -->
    <el-dialog v-model="forceChange" title="首次登录需修改密码" width="440px"
      :close-on-click-modal="false" :close-on-press-escape="false" :show-close="false">
      <el-alert type="warning" :closable="false" show-icon style="margin-bottom: 14px">
        管理员已重置您的密码。为保障账号安全，请先设置新密码（至少 6 位）后再进入系统。
      </el-alert>
      <el-form label-width="90px">
        <el-form-item label="原密码">
          <el-input v-model="forceForm.oldPassword" type="password" show-password placeholder="请输入管理员给的密码" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="forceForm.newPassword" type="password" show-password placeholder="至少 6 位" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="forceForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" style="width: 100%" @click="submitForceChange" :loading="forceSaving">
          保存并进入系统
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, UserFilled } from '@element-plus/icons-vue'
import { authApi } from '../api'
import { authStore } from '../stores/auth'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const isRegister = ref(false)
const rememberMe = ref(false)
const showForgot = ref(false)
// 首次登录强制改密（管理员重置密码的账号）
const forceChange = ref(false)
const forceSaving = ref(false)
const forceForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const form = reactive({
  username: '',
  password: '',
  realName: '',
  department: '',
  confirmPassword: ''
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
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.password) callback(new Error('两次输入的密码不一致'))
        else callback()
      },
      trigger: 'blur'
    }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  department: [
    { required: true, message: '请输入所属学院或部门', trigger: 'blur' }
  ]
}

const toggleMode = () => {
  isRegister.value = !isRegister.value
  form.realName = ''
  form.department = ''
  form.confirmPassword = ''
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
      // 确认密码仅用于前端校验；导师由管理员在用户管理中指定
      res = await authApi.register({
        username: form.username,
        password: form.password,
        realName: form.realName,
        department: form.department
      })
      ElMessage.success('注册成功，正在登录...')
    } else {
      res = await authApi.login({ username: form.username, password: form.password })
      ElMessage.success('登录成功')
    }

    // 通过 authStore 存储用户信息（角色以服务端返回为准）
    const userInfo = {
      id: res.id,
      username: res.username,
      realName: res.realName,
      role: res.role,
      phone: res.phone,
      avatar: res.avatar,
    }
    authStore.setUser(userInfo, res.token)
    if (rememberMe.value) {
      localStorage.setItem('rememberUser', form.username)
    } else {
      localStorage.removeItem('rememberUser')
    }

    // 管理员重置过密码 → 先强制改密再进入系统
    if (res.needChangePassword) {
      forceForm.oldPassword = form.password
      forceForm.newPassword = ''
      forceForm.confirmPassword = ''
      forceChange.value = true
      return
    }

    // 跳转
    router.push('/dashboard')
  } catch (err) {
    ElMessage.error(err.message || '操作失败')
  } finally {
    loading.value = false
  }
}

async function submitForceChange() {
  if (!forceForm.oldPassword) { ElMessage.warning('请输入原密码'); return }
  if (!forceForm.newPassword || forceForm.newPassword.length < 6) {
    ElMessage.warning('新密码至少 6 位'); return
  }
  if (forceForm.newPassword !== forceForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致'); return
  }
  forceSaving.value = true
  try {
    await authApi.updatePassword({
      oldPassword: forceForm.oldPassword,
      newPassword: forceForm.newPassword
    })
    ElMessage.success('密码已更新')
    forceChange.value = false
    router.push('/dashboard')
  } catch (e) {
    ElMessage.error(e.message || '修改失败')
  } finally {
    forceSaving.value = false
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
  gap: 10px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.logo-emoji { font-size: 32px; }
.logo-title {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  line-height: 1.5;
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

.form-title.title-alone {
  margin-bottom: 28px;
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
