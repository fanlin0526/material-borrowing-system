#顶部 Tab「借用记录」#
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
        您当前为普通用户，仅可查看自己的借用记录（含上方统计）
      </el-alert>
      <!-- 信用分对学生是"能不能借"的硬约束，必须可见，否则被拒时会莫名其妙 -->
      <el-alert v-if="creditScore !== null" type="warning" :closable="false" show-icon style="margin-bottom: 12px">
        <template #title>
          我的信用分：<b>{{ creditScore }}</b> 分 —— 逾期归还会按天扣分，按时归还加分；
          低于系统设定的最低分（默认 60）时将暂时无法提交借用与预约。
        </template>
      </el-alert>
      <el-alert v-if="myTraining.length" type="success" :closable="false" show-icon style="margin-bottom: 12px">
        <template #title>
          我的上机资质：{{ myTraining.map(t => t.materialName + (t.validUntil ? '（至 ' + t.validUntil + '）' : '（长期）')).join('、') }}
        </template>
      </el-alert>
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="待审批" name="PENDING" />
        <el-tab-pane label="借用中" name="BORROWED" />
        <!-- 延期申请提交后状态会转成 PENDING_EXTEND，原先没有对应页签，
             记录会从「借用中」直接消失，申请人看不到审批结果 -->
        <el-tab-pane label="待审批延期" name="PENDING_EXTEND" />
        <!-- 退回修改：教师发现申请信息有误时退回，申请人改完可重新提交 -->
        <el-tab-pane label="退回修改" name="RETURNED_FOR_EDIT" />
        <!-- 逾期记录本人也要能归还/申请延期，故不再只给管理员看 -->
        <el-tab-pane label="已逾期" name="OVERDUE" />
        <el-tab-pane label="已领用" name="CONSUMED" />
        <el-tab-pane label="已归还" name="RETURNED" />
        <!-- 损坏是本人归还时勾选的，本人必须能看到登记结果与维修进度；
             该状态行无归还/延期按钮，放开只是「可见」，不带来任何写权限 -->
        <el-tab-pane label="已损坏" name="DAMAGED" />
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
          <!-- 教师/超管默认看全量，需要时可只看本人记录 -->
          <el-switch v-if="isAdmin" v-model="onlyMine" active-text="只看我的"
            style="margin-right: 16px" @change="onOnlyMineChange" />
          <el-button v-if="isAdmin" type="success" plain @click="handleExport">
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
        <el-table-column label="数量" width="110" align="center">
          <template #default="{ row }">
            {{ row.borrowQuantity }}
            <span v-if="row.returnedQuantity > 0 && row.status !== 'CONSUMED'"
              style="color: #909399; font-size: 12px">（已还{{ row.returnedQuantity }}）</span>
          </template>
        </el-table-column>
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
            <el-tooltip v-if="row.rejectReason && ['REJECTED', 'RETURNED_FOR_EDIT'].includes(row.status)"
              :content="row.rejectReason" placement="top">
              <el-icon style="margin-left: 4px; color: #E6A23C; vertical-align: middle"><WarningFilled /></el-icon>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="审批环节" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'PENDING'" size="small"
              :type="row.approvalStage === 'MENTOR' ? 'warning' : 'primary'" effect="light">
              {{ row.approvalStage === 'MENTOR' ? '待导师' : '待终审' }}
            </el-tag>
            <span v-else style="color: #c0c4cc">-</span>
          </template>
        </el-table-column>
        <el-table-column label="预估费用" width="100" align="center">
          <template #default="{ row }">
            <span v-if="(row.estimatedFee || 0) > 0" style="color: #E6A23C; font-weight: 600">
              ¥{{ row.estimatedFee.toFixed(2) }}
            </span>
            <span v-else style="color: #909399">免费</span>
          </template>
        </el-table-column>
        <el-table-column label="归还时间" width="160">
          <template #default="{ row }">{{ row.actualReturnTime ? formatTime(row.actualReturnTime) : '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" plain @click="showProgress(row)">进度</el-button>
            <el-button v-if="row.status === 'BORROWED' || row.status === 'OVERDUE'"
              type="success" size="small" @click="showReturnDialog(row)">归还</el-button>
            <el-button v-if="row.status === 'BORROWED' || row.status === 'OVERDUE'"
              type="warning" size="small" plain @click="showExtendDialog(row)">延期</el-button>
            <el-button v-if="row.status === 'RETURNED_FOR_EDIT'" type="primary" size="small"
              @click="showResubmitDialog(row)">修改并重新提交</el-button>
            <!-- 撤回：学生误提交、临时取消实验时不必等教师拒绝 -->
            <el-button v-if="row.status === 'PENDING' && (isMine(row) || isAdmin)" type="info" size="small" plain
              @click="cancelRequest(row)">撤回申请</el-button>
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
          <span v-if="remainQuantity(returnDialog.record) !== (returnDialog.record?.borrowQuantity || 0)"
            style="margin-left: 10px; color: #909399; font-size: 13px">
            已归还 {{ returnDialog.record?.returnedQuantity || 0 }}，
            <b style="color: #E6A23C">未归还 {{ remainQuantity(returnDialog.record) }}</b>
          </span>
        </el-form-item>
        <el-form-item v-if="returnDialog.record?.expectedReturnTime" label="是否逾期">
          <el-tag v-if="isOverdue(returnDialog.record)" type="danger" size="small" effect="dark">
            已逾期 {{ getOverdueDays(returnDialog.record) }} 天
          </el-tag>
          <el-tag v-else type="success" size="small" effect="dark">未逾期</el-tag>
        </el-form-item>
        <el-form-item label="归还数量">
          <el-input-number v-model="returnDialog.form.returnQuantity" :min="1"
            :max="remainQuantity(returnDialog.record)" style="width: 100%" />
          <div style="color: #909399; font-size: 12px; line-height: 1.5; margin-top: 4px">
            可部分归还：少于未归还数量时本条记录保持「借用中」，剩余数量可再次归还。
          </div>
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

    <!-- 退回修改后重新提交弹窗 -->
    <el-dialog v-model="resubmitDialog.visible" title="修改并重新提交" width="470px">
      <el-form :model="resubmitDialog.form" label-width="100px">
        <el-form-item label="物资">
          <el-input :model-value="resubmitDialog.record?.materialName" disabled />
        </el-form-item>
        <el-form-item v-if="resubmitDialog.record?.rejectReason" label="退回原因">
          <el-tag type="danger" effect="plain">{{ resubmitDialog.record.rejectReason }}</el-tag>
        </el-form-item>
        <el-form-item label="借用数量" required>
          <el-input-number v-model="resubmitDialog.form.quantity" :min="1" :controls="false" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="resubmitDialog.record?.expectedReturnTime" label="预计归还" required>
          <el-date-picker v-model="resubmitDialog.form.expectedReturnTime" type="datetime"
            format="YYYY-MM-DD HH:mm" style="width: 100%" />
        </el-form-item>
        <el-form-item label="用途说明">
          <el-input v-model="resubmitDialog.form.purpose" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="第二领用人">
          <el-input v-model="resubmitDialog.form.secondBorrower" placeholder="危险化学品须双人领用（其他物资可留空）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resubmitDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="handleResubmit" :loading="resubmitDialog.saving">重新提交</el-button>
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

    <!-- 审批进度追踪弹窗 -->
    <el-dialog v-model="progressDialog.visible" title="审批进度追踪" width="580px">
      <div v-if="progressDialog.record">
        <el-descriptions :column="2" size="small" border style="margin-bottom: 18px">
          <el-descriptions-item label="物资">{{ progressDialog.record.materialName }}</el-descriptions-item>
          <el-descriptions-item label="借用人">{{ progressDialog.record.borrower }}</el-descriptions-item>
          <el-descriptions-item label="数量">{{ progressDialog.record.borrowQuantity }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="statusTag(progressDialog.record.status)" size="small" effect="dark">
              {{ statusLabel(progressDialog.record.status) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <el-timeline>
          <el-timeline-item
            v-for="(node, idx) in progressNodes(progressDialog.record)"
            :key="idx"
            :timestamp="node.time"
            :type="node.type"
            :hollow="node.hollow"
            placement="top"
          >
            <div style="font-weight: 600">{{ node.title }}</div>
            <div v-if="node.desc" style="font-size: 12px; color: #909399; margin-top: 2px">{{ node.desc }}</div>
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button @click="progressDialog.visible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Download, WarningFilled } from '@element-plus/icons-vue'
import { borrowApi, categoryApi, excelApi, authFetch, trainingApi } from '../api'
import { authStore } from '../stores/auth'
import { toLocalDateTimeString } from '../utils/date'

const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const activeTab = ref('all')
const borrowStats = reactive({ borrowingCount: 0, overdueCount: null, returnedToday: 0, totalRecords: 0 })
const search = reactive({ status: '', keyword: '' })
const categories = ref([])
// 教师/超管：只看我的记录；所有人：我的信用分
const onlyMine = ref(false)
const myTraining = ref([])
const creditScore = computed(() =>
  currentUser.value && currentUser.value.creditScore != null ? currentUser.value.creditScore : null)

function isMine(row) {
  return !!row && !!currentUser.value
    && (row.borrowerUsername === currentUser.value.username
      || !row.borrowerUsername && row.borrower === currentUser.value.realName)
}
function onOnlyMineChange() {
  currentPage.value = 1
  loadData()
}

/** 撤回待审批的借用申请 */
function cancelRequest(row) {
  ElMessageBox.confirm(
    '确认撤回对「' + row.materialName + '」的借用申请？撤回后该申请作废，如需再借可重新提交。',
    '撤回申请', { type: 'warning' })
    .then(() => row.requestNo ? borrowApi.cancelBatch(row.requestNo) : borrowApi.cancel(row.id))
    .then(() => { ElMessage.success('已撤回申请'); loadData() })
    .catch(() => {})
}

const isAdmin = computed(() => authStore.isAdmin)
const currentUser = computed(() => authStore.userInfo || {})

/** 逾期数：优先用后端全量统计（原先按当前页 tableData 计算，翻页就变，口径不准） */
const overdueCount = computed(() =>
  typeof borrowStats.overdueCount === 'number'
    ? borrowStats.overdueCount
    : tableData.value.filter(r => r.status === 'OVERDUE' || isOverdue(r)).length
)

/** 未归还数量 = 借用数量 - 已归还数量（部分归还支持） */
function remainQuantity(row) {
  if (!row) return 0
  const total = row.borrowQuantity || 0
  const returned = row.returnedQuantity || 0
  return Math.max(0, total - returned)
}

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
    RETURNED_FOR_EDIT: '退回修改', CANCELLED: '已撤回',
    BORROWED: '借用中', CONSUMED: '已领用', RETURNED: '已归还', OVERDUE: '已逾期', DAMAGED: '已损坏' }
  return map[s] || s
}
function statusTag(s) {
  const map = { PENDING: 'info', APPROVED: 'warning', REJECTED: 'danger', PENDING_EXTEND: 'info',
    RETURNED_FOR_EDIT: 'warning', CANCELLED: 'info',
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
  // 归属过滤已收口到服务端（BorrowController.list 会强制覆盖 borrower），
  // 前端不再传本人姓名 —— 传了也会被忽略，留着只会让人误以为这里才是权限边界
  const params = { status: search.status, keyword: search.keyword, page: currentPage.value, size: pageSize.value }
  // 教师/超管可切换「只看我的」；学生由服务端强制只返回本人记录
  if (onlyMine.value) params.mine = true
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
    returnQuantity: remainQuantity(row) || row.borrowQuantity,
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
    let msg
    if (returnDialog.form.isDamaged) {
      msg = '归还成功（已登记损坏，物资转维修中）'
    } else if (data && data.finished === false) {
      // 部分归还：记录保持借用中，剩余数量可再次归还
      msg = '已归还 ' + (data.returnedQuantity || 0) + ' 件，剩余 ' + (data.remainQuantity || 0) + ' 件未归还'
    } else {
      msg = '归还成功'
    }
    // data: { record, overdueDays, fineAmount, returnedQuantity, remainQuantity, finished }
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

// 退回修改后重新提交
const resubmitDialog = reactive({
  visible: false, saving: false, record: null,
  form: { quantity: 1, expectedReturnTime: null, purpose: '', secondBorrower: '' }
})
function showResubmitDialog(row) {
  resubmitDialog.record = row
  resubmitDialog.form = {
    quantity: row.borrowQuantity || 1,
    expectedReturnTime: row.expectedReturnTime ? new Date(row.expectedReturnTime) : null,
    purpose: row.purpose || '',
    secondBorrower: row.secondBorrower || ''
  }
  resubmitDialog.visible = true
}
function handleResubmit() {
  const f = resubmitDialog.form
  if (!f.quantity || f.quantity < 1) { ElMessage.warning('借用数量至少为 1'); return }
  resubmitDialog.saving = true
  borrowApi.resubmit(resubmitDialog.record.id, {
    quantity: f.quantity,
    purpose: f.purpose,
    secondBorrower: f.secondBorrower || null,
    expectedReturnTime: f.expectedReturnTime
      ? toLocalDateTimeString(f.expectedReturnTime) : null
  }).then(() => {
    ElMessage.success('已重新提交，等待审批')
    resubmitDialog.visible = false
    loadData()
  }).catch(e => ElMessage.error(e.message))
    .finally(() => resubmitDialog.saving = false)
}
function showExtendDialog(row) {
  extendDialog.record = row
  extendDialog.form = { days: 7, reason: '' }
  extendDialog.visible = true
}
function handleExtend() {
  extendDialog.saving = true
  authFetch('/api/borrow/extend/' + extendDialog.record.id, {
    method: 'PUT', headers: { 'Content-Type': 'application/json' },
    // 不再传 borrower：延期申请人由服务端从登录态取，请求体里的身份字段一律无效
    body: JSON.stringify({
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

// ==================== 审批进度追踪 ====================
const progressDialog = reactive({ visible: false, record: null })

function showProgress(row) {
  progressDialog.record = row
  progressDialog.visible = true
}

/** 把一条借用记录的各个时间点串成流程节点：提交 → 审批 → 借出/领用 → 延期 → 归还 */
function progressNodes(r) {
  const nodes = []
  nodes.push({
    title: '提交申请',
    time: formatTime(r.borrowTime),
    desc: '借用人：' + r.borrower + '，数量：' + r.borrowQuantity + (r.purpose ? '，用途：' + r.purpose : ''),
    type: 'primary', hollow: false
  })

  // 多级审批：导师环节通过后先记一条，再到管理员终审
  if (r.mentorApproveTime) {
    nodes.push({
      title: '导师审批通过',
      time: formatTime(r.mentorApproveTime),
      desc: '导师：' + (r.mentorApprover || '-') + '，已转实验室管理员终审',
      type: 'success', hollow: false
    })
  }

  // 退回修改：申请人可改完重新提交，流程在此暂停
  if (r.status === 'RETURNED_FOR_EDIT') {
    nodes.push({
      title: '退回修改',
      time: formatTime(r.approveTime),
      desc: '审批人：' + (r.approver || '-')
        + (r.rejectReason ? '，原因：' + r.rejectReason : '')
        + '；请在「借用记录」中修改后重新提交',
      type: 'warning', hollow: false
    })
    return nodes
  }

  if (r.approveTime) {
    const rejected = r.status === 'REJECTED'
    // 教师/管理员自助借用：审批人就是借用人本人且免审批通道，时间轴上要说明白，
    // 否则会被读成「自己审批自己」
    const selfService = !rejected && r.approver && r.approver === r.borrower
    nodes.push({
      title: rejected ? '审批未通过' : (selfService ? '免审批自助出库' : '审批通过'),
      time: formatTime(r.approveTime),
      desc: (selfService ? '教师/管理员自助借用，无需审批' : '审批人：' + (r.approver || '-'))
        + (rejected && r.rejectReason ? '，原因：' + r.rejectReason : ''),
      type: rejected ? 'danger' : 'success', hollow: false
    })
  } else {
    nodes.push({
      title: '等待审批', time: '进行中',
      desc: r.approvalStage === 'MENTOR' ? '申请已提交，等待导师（实验室负责人）审批' : '申请已提交，等待教师审批',
      type: 'warning', hollow: true
    })
  }

  if (r.status === 'REJECTED') return nodes

  // 消耗型物资：审批通过即核销领用，流程结束
  if (r.status === 'CONSUMED') {
    nodes.push({ title: '领用完成', time: formatTime(r.approveTime), desc: '消耗型物资已核销，流程结束', type: 'success', hollow: false })
    return nodes
  }

  // 延期环节
  if (r.extensionDays) {
    nodes.push({ title: '申请延期 ' + r.extensionDays + ' 天', time: '待审批', desc: r.extensionReason || '', type: 'warning', hollow: true })
  } else if (r.extensionApproved) {
    nodes.push({ title: '延期申请已批准', time: '-', desc: '归还日期已顺延至 ' + formatDate(r.expectedReturnTime), type: 'success', hollow: false })
  }

  // 借用中 / 逾期
  if (['BORROWED', 'OVERDUE', 'PENDING_EXTEND'].includes(r.status)) {
    nodes.push({
      title: r.status === 'OVERDUE' ? '已逾期' : '借用中',
      time: '预计归还 ' + formatDate(r.expectedReturnTime),
      desc: r.status === 'OVERDUE' ? '已超过预计归还时间，请尽快归还' : '',
      type: r.status === 'OVERDUE' ? 'danger' : 'primary', hollow: false
    })
  }

  // 归还环节
  if (r.actualReturnTime) {
    const damaged = r.status === 'DAMAGED'
    let desc = ''
    if (damaged && r.damageDescription) desc += '损坏说明：' + r.damageDescription + '；'
    if (r.overdueDays > 0) desc += '逾期 ' + r.overdueDays + ' 天；'
    if (r.fineAmount > 0) desc += '罚款 ¥' + r.fineAmount
    nodes.push({
      title: damaged ? '归还（登记损坏）' : '已归还',
      time: formatTime(r.actualReturnTime),
      desc: desc || '完好归还',
      type: damaged ? 'danger' : 'success', hollow: false
    })
    if (damaged) {
      nodes.push({ title: '损坏处理', time: '进行中', desc: '物资已转「维修中」，修复后由管理员在物资管理中改回「可用」', type: 'warning', hollow: true })
    }
  }
  return nodes
}

function handleExport() {
  excelApi.exportBorrows({ status: search.status, keyword: search.keyword })
    .then(() => ElMessage.success('导出成功'))
    .catch(e => ElMessage.error(e.message))
}

onMounted(() => {
  loadData()
  categoryApi.list().then(cats => { categories.value = cats || [] }).catch(() => {})
  trainingApi.mine().then(list => { myTraining.value = list || [] }).catch(() => {})
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
