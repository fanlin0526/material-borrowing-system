<template>
  <div>
    <!-- 统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #fdf6ec">🔄</div>
          <div class="stat-info">
            <div class="stat-num" style="color: #E6A23C">{{ borrowStats.borrowingCount }}</div>
            <div class="stat-label">借用中</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #fef0f0">⏰</div>
          <div class="stat-info">
            <div class="stat-num" style="color: #F56C6C">{{ overdueCount }}</div>
            <div class="stat-label">已逾期</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #f0f9eb">✅</div>
          <div class="stat-info">
            <div class="stat-num" style="color: #67C23A">{{ borrowStats.returnedToday }}</div>
            <div class="stat-label">今日归还</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #ecf5ff">📋</div>
          <div class="stat-info">
            <div class="stat-num" style="color: #409EFF">{{ borrowStats.totalRecords || tableData.length }}</div>
            <div class="stat-label">记录总数</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 状态标签页 + 搜索 -->
    <el-card style="margin-bottom: 20px">
      <el-alert v-if="!isAdmin" type="info" :closable="false" show-icon style="margin-bottom: 12px">
        您当前为普通用户，仅可查看自己的借用记录
      </el-alert>
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="待审批" name="PENDING" />
        <el-tab-pane label="借用中" name="BORROWED" />
        <el-tab-pane v-if="isAdmin" label="已逾期" name="OVERDUE" />
        <el-tab-pane label="已领用" name="CONSUMED" />
        <el-tab-pane label="已归还" name="RETURNED" />
        <el-tab-pane v-if="isAdmin" label="已损坏" name="DAMAGED" />
      </el-tabs>
      <el-row :gutter="20" style="align-items: center">
        <el-col :span="8">
          <el-input v-model="search.keyword" placeholder="搜索借用人/物资名称" clearable @clear="loadData" @keyup.enter="loadData">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-col>
        <el-col :span="12" style="text-align: right">
          <el-button type="success" plain @click="handleExport">
            <el-icon style="margin-right: 4px"><Download /></el-icon>导出Excel
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 记录表格 -->
    <el-card>
      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa', fontWeight: 600 }"
        :row-class-name="rowClassName">
        <el-table-column prop="materialName" label="物资名称" min-width="120" show-overflow-tooltip />
        <el-table-column label="类别" width="100">
          <template #default="{ row }">
            <el-tag :type="categoryTag(row.materialCategory)" size="small" effect="light">
              {{ categoryLabel(row.materialCategory) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="borrower" label="借用人" width="90" />
        <el-table-column prop="borrowQuantity" label="数量" width="60" align="center" />
        <el-table-column label="借用时间" width="160">
          <template #default="{ row }">{{ formatTime(row.borrowTime) }}</template>
        </el-table-column>
        <el-table-column label="预计归还" width="130">
          <template #default="{ row }">
            <span :class="{ 'overdue-text': isOverdue(row) }">
              {{ row.expectedReturnTime ? formatDate(row.expectedReturnTime) : '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="purpose" label="用途" min-width="130" show-overflow-tooltip />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small" effect="dark">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="归还时间" width="160">
          <template #default="{ row }">{{ row.actualReturnTime ? formatTime(row.actualReturnTime) : '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'BORROWED' || row.status === 'OVERDUE'"
              type="success" size="small" @click="showReturnDialog(row)">归还</el-button>
            <el-button v-if="row.status === 'BORROWED' || row.status === 'OVERDUE'"
              type="warning" size="small" plain @click="showExtendDialog(row)">延期</el-button>
            <el-tooltip v-if="row.isDamaged && row.damageDescription"
              :content="'损坏说明：' + row.damageDescription" placement="top">
              <el-tag type="danger" size="small" effect="plain">⚠损坏</el-tag>
            </el-tooltip>
            <el-button v-if="row.returnRemark" type="info" size="small" plain @click="showRemark(row)">备注</el-button>
          </template>
        </el-table-column>
      </el-table>
      <!-- 分页 -->
      <div style="display: flex; justify-content: flex-end; margin-top: 16px">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next, jumper"
          @current-change="onPageChange"
        />
      </div>
    </el-card>

    <!-- 归还弹窗 -->
    <el-dialog v-model="returnDialog.visible" title="归还物资" width="480px">
      <el-form :model="returnDialog.form" label-width="100px">
        <el-form-item label="物资">
          <el-input :model-value="returnDialog.record?.materialName" disabled />
        </el-form-item>
        <el-form-item label="借用人">
          <el-input :model-value="returnDialog.record?.borrower" disabled />
        </el-form-item>
        <el-form-item label="借用数量">
          <el-tag type="warning">{{ returnDialog.record?.borrowQuantity }}</el-tag>
        </el-form-item>
        <el-form-item v-if="returnDialog.record?.expectedReturnTime" label="是否逾期">
          <el-tag v-if="isOverdue(returnDialog.record)" type="danger" size="small" effect="dark">
            已逾期 {{ getOverdueDays(returnDialog.record) }} 天
          </el-tag>
          <el-tag v-else type="success" size="small" effect="dark">未逾期</el-tag>
        </el-form-item>
        <el-form-item label="归还数量">
          <el-input-number v-model="returnDialog.form.returnQuantity" :min="1" :max="returnDialog.record?.borrowQuantity" style="width: 100%" />
        </el-form-item>
        <el-form-item label="归还备注">
          <el-input v-model="returnDialog.form.returnRemark" type="textarea" :rows="2" placeholder="归还情况说明" />
        </el-form-item>
        <el-divider content-position="left" style="margin: 12px 0; font-size: 13px; color: #909399">损坏登记（选填）</el-divider>
        <el-form-item label="是否损坏">
          <el-switch v-model="returnDialog.form.isDamaged" active-text="是" inactive-text="否" />
        </el-form-item>
        <el-form-item v-if="returnDialog.form.isDamaged" label="损坏描述">
          <el-input v-model="returnDialog.form.damageDescription" type="textarea" :rows="2" placeholder="描述损坏情况..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="returnDialog.visible = false">取消</el-button>
        <el-button type="success" @click="handleReturn" :loading="returnDialog.saving">确认归还</el-button>
      </template>
    </el-dialog>

    <!-- 延期弹窗 -->
    <el-dialog v-model="extendDialog.visible" title="申请延期" width="400px">
      <el-form :model="extendDialog.form" label-width="80px">
        <el-form-item label="物资">
          <el-input :model-value="extendDialog.record?.materialName" disabled />
        </el-form-item>
        <el-form-item label="延期天数" required>
          <el-input-number v-model="extendDialog.form.days" :min="1" :max="30" style="width: 100%" />
        </el-form-item>
        <el-form-item label="延期原因">
          <el-input v-model="extendDialog.form.reason" type="textarea" :rows="2" placeholder="说明延期原因..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="extendDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="handleExtend" :loading="extendDialog.saving">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Download } from '@element-plus/icons-vue'
import { borrowApi, categoryApi, excelApi } from '../api'
import { authStore } from '../stores/auth'

const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const activeTab = ref('all')
const borrowStats = reactive({ borrowingCount: 0, returnedToday: 0, totalRecords: 0 })
const search = reactive({ status: '', keyword: '' })
const categories = ref([])

const currentUser = computed(() => authStore.userInfo || {})
const isAdmin = computed(() => authStore.isAdmin)

const overdueCount = computed(() =>
  tableData.value.filter(r => r.status === 'OVERDUE' || isOverdue(r)).length
)

const returnDialog = reactive({
  visible: false, saving: false,
  record: null,
  form: { returnQuantity: 1, returnRemark: '', isDamaged: false, damageDescription: '' }
})

function categoryLabel(cat) {
  const c = categories.value.find(c => c.code === cat)
  return c ? c.name : cat
}
function categoryTag(cat) {
  const idx = categories.value.findIndex(c => c.code === cat)
  const types = ['', 'success', 'info', 'warning', 'danger']
  return idx >= 0 && idx < types.length ? types[idx] : ''
}
function statusLabel(s) {
  const map = { PENDING: '待审批', APPROVED: '已通过', REJECTED: '已拒绝', PENDING_EXTEND: '待审批延期',
    BORROWED: '借用中', CONSUMED: '已领用', RETURNED: '已归还', OVERDUE: '已逾期', DAMAGED: '已损坏' }
  return map[s] || s
}
function statusTag(s) {
  const map = { PENDING: 'info', APPROVED: 'warning', REJECTED: 'danger', PENDING_EXTEND: 'info',
    BORROWED: 'warning', CONSUMED: 'success', RETURNED: 'success', OVERDUE: 'danger', DAMAGED: 'danger' }
  return map[s] || 'info'
}
function rowClassName({ row }) {
  if (row.status === 'OVERDUE') return 'row-overdue'
  if (row.status === 'DAMAGED') return 'row-damaged'
  return ''
}

function formatTime(t) {
  if (!t) return '-'
  const d = new Date(t)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}
function formatDate(t) {
  if (!t) return '-'
  const d = new Date(t)
  return `${d.getMonth() + 1}/${d.getDate()}`
}

function isOverdue(row) {
  if ((row.status !== 'BORROWED' && row.status !== 'OVERDUE') || !row.expectedReturnTime) return false
  return new Date(row.expectedReturnTime) < new Date()
}
function getOverdueDays(row) {
  if (!row.expectedReturnTime) return 0
  return Math.floor((new Date() - new Date(row.expectedReturnTime)) / 86400000)
}

function onTabChange(name) {
  search.status = name === 'all' ? '' : name
  currentPage.value = 1
  loadData()
}

function onPageChange(page) {
  currentPage.value = page
  loadData()
}

function loadData() {
  loading.value = true
  const params = { status: search.status, keyword: search.keyword, page: currentPage.value, size: pageSize.value }
  if (!isAdmin.value && currentUser.value.realName) {
    params.keyword = currentUser.value.realName
  }
  borrowApi.list(params).then(data => {
    // 适配分页响应格式 { content, totalElements, totalPages, page, size }
    if (data && data.content) {
      tableData.value = data.content
      total.value = data.totalElements
    } else {
      tableData.value = Array.isArray(data) ? data : []
      total.value = tableData.value.length
    }
  }).catch(e => ElMessage.error('加载失败：' + e.message))
    .finally(() => loading.value = false)

  borrowApi.stats().then(s => {
    Object.assign(borrowStats, s)
  })
}

function showReturnDialog(row) {
  returnDialog.record = row
  returnDialog.form = {
    returnQuantity: row.borrowQuantity,
    returnRemark: '',
    isDamaged: false,
    damageDescription: ''
  }
  returnDialog.visible = true
}

function handleReturn() {
  returnDialog.saving = true
  borrowApi.return(returnDialog.record.id, {
    returnQuantity: returnDialog.form.returnQuantity,
    returnRemark: returnDialog.form.returnRemark,
    isDamaged: returnDialog.form.isDamaged,
    damageDescription: returnDialog.form.damageDescription || null,
  }).then(data => {
    let msg = returnDialog.form.isDamaged ? '归还成功（已登记损坏）' : '归还成功'
    // data 现在是 { record, overdueDays, fineAmount }
    if (data && data.overdueDays > 0) {
      msg += '，逾期 ' + data.overdueDays + ' 天'
    }
    if (data && data.fineAmount > 0) {
      msg += '，罚款 ¥' + data.fineAmount
    }
    ElMessage.success(msg)
    returnDialog.visible = false
    loadData()
  }).catch(e => ElMessage.error(e.message))
    .finally(() => returnDialog.saving = false)
}

// 延期弹窗
const extendDialog = reactive({
  visible: false, saving: false, record: null,
  form: { days: 7, reason: '' }
})
function showExtendDialog(row) {
  extendDialog.record = row
  extendDialog.form = { days: 7, reason: '' }
  extendDialog.visible = true
}
function handleExtend() {
  extendDialog.saving = true
  fetch('/api/borrow/extend/' + extendDialog.record.id, {
    method: 'PUT', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      borrower: currentUser.value.realName,
      days: extendDialog.form.days,
      reason: extendDialog.form.reason
    })
  }).then(r => r.json()).then(j => {
    ElMessage.success(j.code === 200 ? '延期申请已提交' : (j.message || '提交失败'))
    extendDialog.visible = false
    loadData()
  }).catch(() => ElMessage.error('操作失败'))
    .finally(() => extendDialog.saving = false)
}

function showRemark(row) {
  ElMessage.info(row.returnRemark)
}

function handleExport() {
  excelApi.exportBorrows({ status: search.status, keyword: search.keyword })
    .then(() => ElMessage.success('导出成功'))
    .catch(e => ElMessage.error(e.message))
}

onMounted(() => {
  loadData()
  categoryApi.list().then(cats => { categories.value = cats || [] }).catch(() => {})
})
</script>

<style scoped>
.stat-card { display: flex; align-items: center; gap: 16px; border-radius: 10px; }
.stat-icon { width: 52px; height: 52px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 24px; }
.stat-num { font-size: 28px; font-weight: 700; line-height: 1.2; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }
.overdue-text { color: #F56C6C; font-weight: 600; }
:deep(.row-overdue) { background: #fef0f0 !important; }
:deep(.row-damaged) { background: #fdf6ec !important; }
</style>
