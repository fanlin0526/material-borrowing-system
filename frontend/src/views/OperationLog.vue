<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span class="card-title">操作日志</span>
          <el-button @click="loadLogs" :loading="loading">刷新</el-button>
        </div>
      </template>
      <el-table :data="logs" stripe v-loading="loading" style="width: 100%" max-height="600">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="realName" label="操作人" width="100" />
        <el-table-column prop="action" label="操作类型" width="120">
          <template #default="{ row }">
            <el-tag :type="actionTag(row.action)" size="small">{{ actionLabel(row.action) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="detail" label="操作描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="targetType" label="目标类型" width="100" />
        <el-table-column prop="createTime" label="操作时间" width="180">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
      </el-table>
      <div v-if="logs.length === 0 && !loading" style="text-align: center; padding: 40px; color: #909399">
        暂无操作日志
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const loading = ref(false)
const logs = ref([])

function actionLabel(a) {
  const map = {
    LOGIN: '登录', LOGOUT: '登出', BORROW: '借用', RETURN: '归还',
    RESERVE: '预约', CANCEL: '取消预约', MATERIAL_ADD: '新增物资',
    MATERIAL_EDIT: '编辑物资', MATERIAL_DELETE: '删除物资',
    USER_EDIT: '编辑用户', CONFIG_EDIT: '修改配置'
  }
  return map[a] || a
}
function actionTag(a) {
  const map = {
    LOGIN: 'info', LOGOUT: 'info', BORROW: 'success', RETURN: 'warning',
    RESERVE: 'primary', CANCEL: 'danger', MATERIAL_ADD: 'success',
    MATERIAL_EDIT: 'warning', MATERIAL_DELETE: 'danger',
    USER_EDIT: 'warning', CONFIG_EDIT: 'warning'
  }
  return map[a] || 'info'
}
function formatTime(t) {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN')
}

async function loadLogs() {
  loading.value = true
  try {
    const res = await fetch('/api/admin/logs')
    const json = await res.json()
    logs.value = json.code === 200 ? json.data : []
  } catch { logs.value = [] }
  finally { loading.value = false }
}

onMounted(() => loadLogs())
</script>

<style scoped>
.card-title { font-size: 15px; font-weight: 600; color: #303133; }
</style>
