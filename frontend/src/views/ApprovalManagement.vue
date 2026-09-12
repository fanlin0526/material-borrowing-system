<template>
  <div>
    <el-card style="margin-bottom: 20px" v-loading="loading">
      <template #header><span class="card-title">待审批借用申请</span></template>
      <el-table :data="borrowPending" stripe empty-text="暂无待审批借用申请" style="width: 100%">
        <el-table-column prop="borrower" label="申请人" width="90" />
        <el-table-column prop="materialName" label="物资" width="140" />
        <el-table-column prop="borrowQuantity" label="数量" width="60" />
        <el-table-column prop="purpose" label="用途" min-width="150" show-overflow-tooltip />
        <el-table-column label="预计归还" width="120">
          <template #default="{ row }">{{ row.expectedReturnTime ? formatTime(row.expectedReturnTime) : '-' }}</template>
        </el-table-column>
        <el-table-column label="申请时间" width="120">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="success" @click="approveBorrow(row)">通过</el-button>
            <el-button size="small" type="danger" @click="rejectBorrow(row)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card v-loading="loading">
      <template #header><span class="card-title">待审批延期申请</span></template>
      <el-table :data="extendPending" stripe empty-text="暂无待审批延期申请" style="width: 100%">
        <el-table-column prop="borrower" label="申请人" width="90" />
        <el-table-column prop="materialName" label="物资" width="140" />
        <el-table-column label="延期天数" width="80">
          <template #default="{ row }">{{ row.extensionDays || 7 }}天</template>
        </el-table-column>
        <el-table-column prop="extensionReason" label="延期原因" min-width="160" show-overflow-tooltip />
        <el-table-column label="预计归还" width="120">
          <template #default="{ row }">{{ row.expectedReturnTime ? formatTime(row.expectedReturnTime) : '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="success" @click="approveExtend(row)">通过</el-button>
            <el-button size="small" type="danger" @click="rejectExtend(row)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { authStore } from '../stores/auth'

const loading = ref(false)
const allPending = ref([])

const borrowPending = computed(() => allPending.value.filter(r => r.status === 'PENDING'))
const extendPending = computed(() => allPending.value.filter(r => r.status === 'PENDING_EXTEND'))

const currentUser = computed(() => authStore.userInfo || {})

function formatTime(t) {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN')
}

async function loadPending() {
  loading.value = true
  try {
    const res = await fetch('/api/borrow/pending')
    const json = await res.json()
    allPending.value = json.code === 200 ? json.data : []
  } catch { allPending.value = [] }
  finally { loading.value = false }
}

async function approveBorrow(row) {
  const approver = currentUser.value.realName || '管理员'
  try { await fetch('/api/borrow/approve/' + row.id, {
    method: 'PUT', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ approver }) }).then(r => r.json()).then(j => {
      ElMessage.success('已通过「' + row.borrower + '」的借用申请')
      loadPending()
    })
  } catch { ElMessage.error('操作失败') }
}

async function rejectBorrow(row) {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝借用申请', { confirmButtonText: '确定拒绝', inputPlaceholder: '选填' }).catch(() => null)
    if (reason === null && reason !== '') return // 点了取消
    const approver = currentUser.value.realName || '管理员'
    await fetch('/api/borrow/reject/' + row.id, {
      method: 'PUT', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ approver, reason: reason || '' }) }).then(r => r.json())
    ElMessage.success('已拒绝')
    loadPending()
  } catch { /* 取消 */ }
}

async function approveExtend(row) {
  const approver = currentUser.value.realName || '管理员'
  try { await fetch('/api/borrow/extend/approve/' + row.id, {
    method: 'PUT', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ approver }) }).then(r => r.json())
    ElMessage.success('已批准延期')
    loadPending()
  } catch { ElMessage.error('操作失败') }
}

async function rejectExtend(row) {
  const approver = currentUser.value.realName || '管理员'
  try { await fetch('/api/borrow/extend/reject/' + row.id, {
    method: 'PUT', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ approver }) }).then(r => r.json())
    ElMessage.success('已拒绝延期')
    loadPending()
  } catch { ElMessage.error('操作失败') }
}

onMounted(() => loadPending())
</script>

<style scoped>
.card-title { font-size: 15px; font-weight: 600; color: #303133; }
</style>
