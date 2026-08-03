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
        <el-table-column prop="createTime" label="注册时间" width="180">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="resetPwd(row)">重置密码</el-button>
            <el-button size="small" type="danger" @click="deleteUser(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑用户' : '新增用户'" width="420px">
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

const loading = ref(false)
const users = ref([])
const keyword = ref('')

const dialog = reactive({
  visible: false, isEdit: false, editId: null,
  form: { username: '', realName: '', role: 'STUDENT', phone: '', password: '123456' }
})

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
    const res = await fetch('/api/admin/users?keyword=' + encodeURIComponent(keyword.value))
    const json = await res.json()
    users.value = json.code === 200 ? json.data : []
  } catch { users.value = [] }
  finally { loading.value = false }
}

function showAddDialog() {
  dialog.isEdit = false; dialog.editId = null
  dialog.form = { username: '', realName: '', role: 'STUDENT', phone: '', password: '123456' }
  dialog.visible = true
}

function showEditDialog(row) {
  dialog.isEdit = true; dialog.editId = row.id
  dialog.form = { username: row.username, realName: row.realName, role: row.role, phone: row.phone || '' }
  dialog.visible = true
}

async function submitUser() {
  const { username, realName, role, phone, password } = dialog.form
  if (!username || !realName) { ElMessage.warning('请填写用户名和姓名'); return }
  try {
    let res
    if (dialog.isEdit) {
      res = await fetch('/api/admin/users/' + dialog.editId, {
        method: 'PUT', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ realName, role, phone })
      })
    } else {
      res = await fetch('/api/admin/users', {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, realName, role, phone, password })
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

async function resetPwd(row) {
  try {
    await ElMessageBox.confirm('确认重置「' + row.realName + '」的密码为 123456？', '重置密码', { type: 'warning' })
    const res = await fetch('/api/admin/users/' + row.id + '/reset-password', { method: 'PUT' })
    const json = await res.json()
    ElMessage[json.code === 200 ? 'success' : 'error'](json.message || json.data)
  } catch { /* 取消 */ }
}

async function deleteUser(row) {
  try {
    await ElMessageBox.confirm('确认删除用户「' + row.realName + '」？此操作不可撤销。', '删除确认', { type: 'warning' })
    const res = await fetch('/api/admin/users/' + row.id, { method: 'DELETE' })
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
