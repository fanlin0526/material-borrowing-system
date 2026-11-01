#右上角齿轮图标的下拉菜单「用户管理」#
<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span class="card-title">用户管理</span>
          <div style="display: flex; gap: 12px">
            <el-input v-model="keyword" placeholder="搜索姓名/用户名" clearable style="width: 220px" @input="loadUsers" />
            <el-button type="primary" @click="showAddDialog">新增用户</el-button>
          </div>
        </div>
      </template>
      <el-table :data="users" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" width="130" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="roleTag(row.role)" size="small">{{ roleLabel(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="department" label="学院/部门" width="120">
          <template #default="{ row }">{{ row.department || '-' }}</template>
        </el-table-column>
        <el-table-column prop="labName" label="实验室" width="150">
          <template #default="{ row }">{{ row.labName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="mentorName" label="导师" width="90">
          <template #default="{ row }">{{ row.mentorName || '-' }}</template>
        </el-table-column>
        <el-table-column label="信用分" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="creditTag(row.creditScore)" size="small">
              {{ row.creditScore != null ? row.creditScore : 100 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.enabled === false ? 'info' : 'success'" size="small">
              {{ row.enabled === false ? '已停用' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="330" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="resetPwd(row)">重置密码</el-button>
            <el-button size="small" :type="row.enabled === false ? 'success' : 'info'" plain @click="toggleStatus(row)">
              {{ row.enabled === false ? '启用' : '停用' }}
            </el-button>
            <el-button size="small" type="danger" @click="deleteUser(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑用户' : '新增用户'" width="500px">
      <el-form :model="dialog.form" label-width="80px">
        <el-form-item label="用户名" required>
          <el-input v-model="dialog.form.username" :disabled="dialog.isEdit" placeholder="登录用户名" />
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model="dialog.form.realName" placeholder="真实姓名" />
        </el-form-item>
        <el-form-item label="角色" required>
          <el-select v-model="dialog.form.role" style="width: 100%">
            <el-option label="超级管理员" value="SUPER_ADMIN" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="学生" value="STUDENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="dialog.form.phone" placeholder="选填" />
        </el-form-item>
        <el-form-item label="学院/部门">
          <el-input v-model="dialog.form.department" placeholder="如：软件学院 / 实验中心（用于按院系统计）" />
        </el-form-item>
        <el-form-item label="实验室">
          <el-input v-model="dialog.form.labName" placeholder="如：电子与嵌入式实验室" />
        </el-form-item>
        <el-form-item label="导师">
          <el-input v-model="dialog.form.mentorName" placeholder="该生的借用申请先由导师审批（实验室负责人）" />
        </el-form-item>
        <el-form-item label="信用分">
          <el-input-number v-model="dialog.form.creditScore" :min="0" :max="100" :controls="false" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="!dialog.isEdit" label="密码">
          <el-input v-model="dialog.form.password" placeholder="默认 123456" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitUser">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { authFetch } from '../api'

const loading = ref(false)
const users = ref([])
const keyword = ref('')

const dialog = reactive({
  visible: false, isEdit: false, editId: null,
  form: {
    username: '', realName: '', role: 'STUDENT', phone: '', password: '123456',
    department: '', labName: '', mentorName: '', creditScore: 100
  }
})

function creditTag(score) {
  const s = score != null ? score : 100
  if (s >= 90) return 'success'
  if (s >= 60) return 'warning'
  return 'danger'
}
function roleLabel(r) {
  const map = { SUPER_ADMIN: '超级管理员', TEACHER: '教师', STUDENT: '学生' }
  return map[r] || r
}
function roleTag(r) {
  const map = { SUPER_ADMIN: 'danger', TEACHER: 'warning', STUDENT: 'info' }
  return map[r] || 'info'
}
function formatTime(t) {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN')
}

async function loadUsers() {
  loading.value = true
  try {
    const res = await authFetch('/api/admin/users?keyword=' + encodeURIComponent(keyword.value))
    const json = await res.json()
    users.value = json.code === 200 ? json.data : []
  } catch { users.value = [] }
  finally { loading.value = false }
}

function showAddDialog() {
  dialog.isEdit = false; dialog.editId = null
  dialog.form = {
    username: '', realName: '', role: 'STUDENT', phone: '', password: '123456',
    department: '', labName: '', mentorName: '', creditScore: 100
  }
  dialog.visible = true
}

function showEditDialog(row) {
  dialog.isEdit = true; dialog.editId = row.id
  dialog.form = {
    username: row.username, realName: row.realName, role: row.role, phone: row.phone || '',
    department: row.department || '', labName: row.labName || '',
    mentorName: row.mentorName || '', creditScore: row.creditScore != null ? row.creditScore : 100
  }
  dialog.visible = true
}

async function submitUser() {
  const { username, realName, role, phone, password, department, labName, mentorName, creditScore } = dialog.form
  if (!username || !realName) { ElMessage.warning('请填写用户名和姓名'); return }
  const common = { realName, role, phone, department, labName, mentorName, creditScore }
  try {
    let res
    if (dialog.isEdit) {
      res = await authFetch('/api/admin/users/' + dialog.editId, {
        method: 'PUT', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(common)
      })
    } else {
      res = await authFetch('/api/admin/users', {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password, ...common })
      })
    }
    const json = await res.json()
    if (json.code === 200) {
      ElMessage.success(dialog.isEdit ? '更新成功' : '创建成功')
      dialog.visible = false
      loadUsers()
    } else {
      ElMessage.error(json.message)
    }
  } catch { ElMessage.error('操作失败') }
}

/** 停用 / 启用账号：停用后不能登录与提交新业务，历史记录保留（论文口径） */
async function toggleStatus(row) {
  const target = row.enabled === false
  try {
    await ElMessageBox.confirm(
      '确认' + (target ? '启用' : '停用') + '账号「' + row.realName + '」？'
        + (target ? '' : '停用后该账号无法登录与提交借用/预约，历史记录保留。'),
      target ? '启用账号' : '停用账号', { type: 'warning' })
    const res = await authFetch('/api/admin/users/' + row.id + '/status', {
      method: 'PUT', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ enabled: target })
    })
    const json = await res.json()
    if (json.code === 200) {
      ElMessage.success(target ? '已启用' : '已停用')
      loadUsers()
    } else {
      ElMessage.error(json.message)
    }
  } catch { /* 取消 */ }
}

async function resetPwd(row) {
  try {
    await ElMessageBox.confirm('确认重置「' + row.realName + '」的密码为 123456？', '重置密码', { type: 'warning' })
    const res = await authFetch('/api/admin/users/' + row.id + '/reset-password', { method: 'PUT' })
    const json = await res.json()
    ElMessage[json.code === 200 ? 'success' : 'error'](json.message || json.data)
  } catch { /* 取消 */ }
}

async function deleteUser(row) {
  try {
    await ElMessageBox.confirm(
      '确认删除用户「' + row.realName + '」？此操作不可撤销。'
        + '若该用户还有未完结的借用记录，系统会提示改为「停用」（保留历史记录可追溯）。',
      '删除确认', { type: 'warning' })
    const res = await authFetch('/api/admin/users/' + row.id, { method: 'DELETE' })
    const json = await res.json()
    if (json.code === 200) {
      ElMessage.success('已删除')
      loadUsers()
    } else {
      ElMessage.error(json.message)
    }
  } catch { /* 取消 */ }
}

onMounted(() => loadUsers())
</script>

<style scoped>
.card-title { font-size: 15px; font-weight: 600; color: #303133; }
</style>
