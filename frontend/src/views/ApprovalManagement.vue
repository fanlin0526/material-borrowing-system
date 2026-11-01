#顶部 Tab「审批管理」，带待审批数量红角标#
<template>
  <div>
    <el-card style="margin-bottom: 20px" v-loading="loading">
      <template #header><span class="card-title">待审批预约申请（审批人不能是预约人本人）</span></template>
      <el-table :data="reservationPending" stripe empty-text="暂无待审批预约申请" style="width: 100%">
        <el-table-column prop="realName" label="预约人" width="90" />
        <el-table-column prop="materialName" label="物资" width="140" />
        <el-table-column label="预约时间段" min-width="240">
          <template #default="{ row }">{{ formatRange(row.startTime, row.endTime) }}</template>
        </el-table-column>
        <el-table-column prop="purpose" label="用途" min-width="140" show-overflow-tooltip />
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="row.type === 'RECURRING' ? 'warning' : 'info'">
              {{ row.type === 'RECURRING' ? '周期' : '单次' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="120">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="success" @click="approveReservation(row)">通过</el-button>
            <el-button size="small" type="danger" @click="rejectReservation(row)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card style="margin-bottom: 20px" v-loading="loading">
      <template #header><span class="card-title">待审批借用申请（导师环节 → 实验室管理员终审）</span></template>
      <el-alert v-if="mentorPendingCount > 0" type="warning" :closable="false" show-icon style="margin-bottom: 12px">
        其中 {{ mentorPendingCount }} 条处于「待导师」环节：申请人的导师（实验室负责人）先审批，通过后自动转入终审；
        若导师不在，管理员也可直接处理，避免流程卡住。
      </el-alert>
      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 12px">
        审批人不能是申请人本人：高值/稀缺仪器标记了「借用需审批」后，教师本人借用也要走审批，
        但系统会拦住自审，请由另一位教师或管理员处理。
      </el-alert>
      <el-table :data="borrowPending" stripe empty-text="暂无待审批借用申请" style="width: 100%">
        <el-table-column prop="borrower" label="申请人" width="90" />
        <el-table-column label="申请单" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.requestNo" type="info" size="small" effect="plain">
              批量 {{ row.requestNo.slice(-6) }}
            </el-tag>
            <span v-else>单项</span>
          </template>
        </el-table-column>
        <el-table-column prop="materialName" label="物资" width="140" />
        <el-table-column prop="borrowQuantity" label="数量" width="60" />
        <el-table-column label="环节" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.approvalStage === 'MENTOR' ? 'warning' : 'primary'" effect="light">
              {{ row.approvalStage === 'MENTOR' ? '待导师' : '待终审' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="预估费用" width="90" align="center">
          <template #default="{ row }">
            {{ (row.estimatedFee || 0) > 0 ? '¥' + row.estimatedFee.toFixed(2) : '免费' }}
          </template>
        </el-table-column>
        <el-table-column prop="purpose" label="用途" min-width="150" show-overflow-tooltip />
        <el-table-column label="预计归还" width="120">
          <template #default="{ row }">{{ row.expectedReturnTime ? formatTime(row.expectedReturnTime) : '-' }}</template>
        </el-table-column>
        <el-table-column label="申请时间" width="120">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="success" @click="approveBorrow(row)">
              {{ row.requestNo ? '整单通过' : (row.approvalStage === 'MENTOR' ? '导师通过' : '终审通过') }}
            </el-button>
            <el-button size="small" type="warning" plain @click="returnForEdit(row)">退回修改</el-button>
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
import { authFetch } from '../api'
import { authStore } from '../stores/auth'

const loading = ref(false)
const allPending = ref([])
const reservationPending = ref([])

const borrowPending = computed(() => allPending.value.filter(r => r.status === 'PENDING'))
const extendPending = computed(() => allPending.value.filter(r => r.status === 'PENDING_EXTEND'))
/** 待导师环节的申请数（用于提示多级审批的第一环节） */
const mentorPendingCount = computed(() =>
  borrowPending.value.filter(r => r.approvalStage === 'MENTOR').length)

const currentUser = computed(() => authStore.userInfo || {})

function formatTime(t) {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN')
}
function formatRange(s, e) {
  if (!s || !e) return '-'
  const d = new Date(s)
  const p = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} `
    + `${p(d.getHours())}:${p(d.getMinutes())} ~ `
    + `${p(new Date(e).getHours())}:${p(new Date(e).getMinutes())}`
}

async function loadPending() {
  loading.value = true
  try {
    const res = await authFetch('/api/borrow/pending')
    const json = await res.json()
    allPending.value = json.code === 200 ? json.data : []
  } catch { allPending.value = [] }
  try {
    const res2 = await authFetch('/api/reservation/pending')
    const json2 = await res2.json()
    reservationPending.value = json2.code === 200 ? json2.data : []
  } catch { reservationPending.value = [] }
  finally { loading.value = false }
}

const approverName = () => currentUser.value.realName || '管理员'

/** 统一调用后端并返回 Result 对象（业务失败时后端仍是 HTTP 200 + code!=200） */
async function callApi(url, method, body) {
  const res = await authFetch(url, {
    method,
    headers: { 'Content-Type': 'application/json' },
    body: body ? JSON.stringify(body) : undefined
  })
  return res.json()
}

/**
 * 弹窗收集拒绝原因：返回 null 表示用户点了「取消」，返回 '' 表示确定但未填原因。
 * 之前用 `.catch(() => null)` 再解构 value，取消时会直接抛 TypeError 被外层静默吞掉，
 * 行为看着对、实际是靠异常兜着，这里显式区分。
 */
async function askRejectReason(title, placeholder) {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因', title, {
      confirmButtonText: '确定拒绝',
      cancelButtonText: '取消',
      inputPlaceholder: placeholder
    })
    return value == null ? '' : value
  } catch {
    return null
  }
}

/** 统一处理返回结果：成功提示并刷新待办，失败把服务端原因透出来 */
function reportResult(j, okText) {
  if (j && j.code === 200) {
    ElMessage.success(okText)
    loadPending()
    return true
  }
  ElMessage.error((j && j.message) || '操作失败')
  return false
}

// ====== 预约审批（通过后自动生成借用单，形成 预约→审批→借用→归还 闭环） ======
async function approveReservation(row) {
  try {
    const j = await callApi('/api/reservation/approve/' + row.id, 'PUT', { approver: approverName() })
    const rec = j && j.data ? j.data.borrowRecord : null
    reportResult(j, '已通过「' + row.realName + '」的预约'
      + (rec ? '，已生成借用单 #' + rec.id + '，可在「借用记录」中归还' : ''))
  } catch { ElMessage.error('操作失败') }
}

async function rejectReservation(row) {
  const reason = await askRejectReason('拒绝预约申请', '选填（会通知预约人）')
  if (reason === null) return
  try {
    const j = await callApi('/api/reservation/reject/' + row.id, 'PUT',
      { approver: approverName(), reason })
    reportResult(j, '已拒绝该预约申请')
  } catch { ElMessage.error('操作失败') }
}

async function approveBorrow(row) {
  try {
    const url = row.requestNo
      ? '/api/borrow/batch/approve/' + row.requestNo
      : '/api/borrow/approve/' + row.id
    const j = await callApi(url, 'PUT', { approver: approverName() })
    // 导师环节：只推进环节，不扣库存、不借出
    if (row.approvalStage === 'MENTOR') {
      reportResult(j, '已通过「' + row.borrower + '」的导师审批环节，转实验室管理员终审'
        + (row.requestNo ? '（整张批量申请）' : ''))
      return
    }
    const consumed = j && j.data && j.data.status === 'CONSUMED'
    reportResult(j, '已通过「' + row.borrower + '」的' + (consumed ? '领用申请' : '借用申请')
      + (row.requestNo ? '（整张批量申请）' : ''))
  } catch { ElMessage.error('操作失败') }
}

/** 退回修改：与「拒绝」不同，申请不作废，申请人改完可重新提交 */
async function returnForEdit(row) {
  const reason = await askRejectReason('退回修改', '请说明需要修改的地方（会通知申请人）')
  if (reason === null) return
  try {
    const url = row.requestNo
      ? '/api/borrow/batch/return-edit/' + row.requestNo
      : '/api/borrow/return-edit/' + row.id
    const j = await callApi(url, 'PUT', { reason })
    reportResult(j, '已退回「' + row.borrower + '」修改后重新提交' + (row.requestNo ? '（整张批量申请）' : ''))
  } catch { ElMessage.error('操作失败') }
}

async function rejectBorrow(row) {
  const reason = await askRejectReason('拒绝借用申请', '选填（会通知申请人）')
  if (reason === null) return
  try {
    const url = row.requestNo
      ? '/api/borrow/batch/reject/' + row.requestNo
      : '/api/borrow/reject/' + row.id
    const j = await callApi(url, 'PUT', { approver: approverName(), reason })
    reportResult(j, '已拒绝「' + row.borrower + '」的申请' + (row.requestNo ? '（整张批量申请）' : ''))
  } catch { ElMessage.error('操作失败') }
}

async function approveExtend(row) {
  try {
    const j = await callApi('/api/borrow/extend/approve/' + row.id, 'PUT', { approver: approverName() })
    reportResult(j, '已批准「' + row.borrower + '」的延期申请')
  } catch { ElMessage.error('操作失败') }
}

async function rejectExtend(row) {
  const reason = await askRejectReason('拒绝延期申请', '选填（会通知申请人）')
  if (reason === null) return
  try {
    const j = await callApi('/api/borrow/extend/reject/' + row.id, 'PUT',
      { approver: approverName(), reason })
    reportResult(j, '已拒绝「' + row.borrower + '」的延期申请')
  } catch { ElMessage.error('操作失败') }
}

onMounted(() => loadPending())
</script>

<style scoped>
.card-title { font-size: 15px; font-weight: 600; color: #303133; }
</style>
