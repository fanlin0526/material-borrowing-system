#顶部 Tab「预约管理」#
<template>
  <div>
    <!-- 周切换栏 -->
    <el-card style="margin-bottom: 20px">
      <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px">
        <div style="display: flex; align-items: center; gap: 12px">
          <el-button-group>
            <el-button :icon="ArrowLeft" @click="prevWeek">上一周</el-button>
            <el-button @click="goToday">本周</el-button>
            <el-button :icon="ArrowRight" @click="nextWeek">下一周</el-button>
          </el-button-group>
          <el-date-picker
            v-model="jumpDate"
            type="date"
            placeholder="跳转到指定日期"
            style="width: 160px"
            @change="jumpToDate"
          />
          <span style="font-size: 16px; font-weight: 600; color: #303133">
            {{ weekLabel }}
          </span>
        </div>
        <div style="display: flex; align-items: center; gap: 8px">
          <el-button type="primary" @click="showCreateDialog">
            <el-icon style="margin-right: 4px"><Plus /></el-icon>新建预约
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 流程说明 -->
    <el-alert type="info" :closable="false" show-icon style="margin-bottom: 16px"
      title="预约流程：提交预约申请 → 教师/管理员审批 → 通过后自动生成借用单 → 使用完在「借用记录」中归还" />

    <!-- 周视图表格 -->
    <el-card v-loading="loading">
      <div class="week-grid">
        <!-- 表头：星期 -->
        <div class="grid-header">时间</div>
        <div v-for="d in 7" :key="d" class="grid-header" :class="{ today: isToday(d - 1) }">
          <div class="day-label">{{ weekDays[d - 1] }}</div>
          <div class="date-label">{{ formatDate(getDayDate(d - 1)) }}</div>
        </div>

        <!-- 时间段行 -->
        <template v-for="hour in hours" :key="hour">
          <div class="grid-time">{{ hour }}:00</div>
          <div v-for="d in 7" :key="d" class="grid-cell" @click="showQuickCreate(d - 1, hour)">
            <div v-for="slot in getSlots(d - 1, hour)" :key="slot.id"
              class="slot-bar"
              :class="{ conflict: slot.isMine }"
              :style="slotStyle(slot)"
              @click.stop="showResvDetail(slot)"
            >
              {{ slot.materialName }}{{ slot.status === 'PENDING' ? '（待审批）' : '' }}
            </div>
          </div>
        </template>
      </div>
    </el-card>

    <!-- 新建预约弹窗 -->
    <el-dialog v-model="dialog.visible" title="新建预约" width="560px" :close-on-click-modal="false">
      <el-form :model="dialog.form" label-width="100px">
        <el-form-item label="选择设备" required>
          <el-select v-model="dialog.form.materialId" placeholder="搜索设备" filterable style="width: 100%"
            @change="onDeviceChange">
            <el-option v-for="m in availableDevices" :key="m.id"
              :label="m.name + ' (' + (m.availableQuantity || 0) + (m.unit || '台') + '可用)'"
              :value="m.id" :disabled="m.deviceStatus !== 'AVAILABLE'" />
          </el-select>
        </el-form-item>
        <el-form-item label="预约人" required>
          <el-input v-model="dialog.form.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="日期" required>
          <el-date-picker v-model="dialog.form.date" type="date" placeholder="选择日期" style="width: 100%"
            :disabled-date="disabledDate" @change="checkConflictDebounced" />
        </el-form-item>
        <el-form-item label="时间段" required>
          <el-time-select v-model="dialog.form.startTime" placeholder="开始时间" start="07:00" end="21:00" step="00:30"
            :min-time="minStartTime" style="width: 140px" @change="checkConflictDebounced" />
          <span style="margin: 0 10px; color: #909399">至</span>
          <el-time-select v-model="dialog.form.endTime" placeholder="结束时间" start="07:00" end="21:00" step="00:30"
            :min-time="minEndTime" style="width: 140px" @change="checkConflictDebounced" />
        </el-form-item>
        <el-form-item label="预约模式" required>
          <el-radio-group v-model="dialog.form.mode" @change="resetConflict">
            <el-radio-button label="SINGLE">单次预约</el-radio-button>
            <el-radio-button label="RECURRING">周期预约</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="dialog.form.mode === 'RECURRING'" label="周期类型">
          <el-select v-model="dialog.form.recurringRule" style="width: 100%">
            <el-option label="每周" value="WEEKLY" />
            <el-option label="双周" value="BIWEEKLY" />
            <el-option label="每月" value="MONTHLY" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="dialog.form.mode === 'RECURRING'" label="重复次数">
          <el-input-number v-model="dialog.form.weeks" :min="1" :max="12" style="width: 120px" />
          <span style="margin-left: 8px; color: #909399; font-size: 13px">次</span>
        </el-form-item>
        <el-form-item label="用途说明">
          <el-input v-model="dialog.form.purpose" type="textarea" :rows="2" placeholder="实验项目名称或用途" />
        </el-form-item>

        <!-- 冲突提示：左缩进对齐输入框。注意必须靠外层容器撑宽度——
             el-alert 自带 width:100%，若直接在它身上写 margin-left:100px，
             元素宽度仍是父容器整宽，整体会向右溢出弹窗 100px。 -->
        <div class="dialog-alert">
          <el-alert v-if="conflict.hasConflict" title="时段冲突" type="error" :closable="false" show-icon>
            <template #default>
              <div v-for="(c, i) in conflict.conflicts" :key="i" style="margin-bottom: 4px">
                已被「{{ c.reservedBy }}」占用（{{ formatConflictTime(c) }}）{{ c.status === 'PENDING' ? ' · 待审批' : '' }}
              </div>
            </template>
          </el-alert>
          <el-alert v-if="conflict.checked && !conflict.hasConflict" title="该时段空闲，可预约" type="success"
            :closable="false" show-icon />
          <!-- 在借提示：直接借用通道的占用不落在预约时段上，时段空闲也可能拿不到实物 -->
          <el-alert v-if="conflict.borrowedOutQuantity > 0" type="warning" :closable="false" show-icon>
            <template #title>
              该设备有 {{ conflict.borrowedOutQuantity }} 件已被借出且尚未归还<template
                v-if="conflict.earliestReturnTime">，最早预计 {{ formatReturnTime(conflict.earliestReturnTime) }} 归还</template>。
              时段空闲不代表到时有实物在手，请确认剩余数量够用；急用可改用「借用」直接申请。
            </template>
          </el-alert>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="dialog.saving"
          :disabled="conflict.hasConflict">提交预约申请</el-button>
      </template>
    </el-dialog>

    <!-- 预约详情弹窗 -->
    <el-dialog v-model="detailDialog.visible" title="预约详情" width="420px">
      <div style="text-align: center; margin-bottom: 16px" v-if="detailDialog.record">
        <div :style="{ width:'56px',height:'56px',borderRadius:'50%',background:categoryColor(detailDialog.record.materialCategory||'DEVICE'),margin:'0 auto 10px',display:'flex',alignItems:'center',justifyContent:'center' }">
          <el-icon :size="24" color="#fff"><component :is="deviceIconComp(detailDialog.record.materialCategory||'DEVICE')" /></el-icon>
        </div>
        <b style="font-size: 16px">{{ detailDialog.record.materialName }}</b>
      </div>
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="预约人">{{ detailDialog.record.realName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="日期">{{ detailDialog.record.date || '-' }}</el-descriptions-item>
        <el-descriptions-item label="时间">{{ detailDialog.record.timeRange || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用途">{{ detailDialog.record.purpose || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="resStatusTag(detailDialog.record.status)" size="small">
            {{ resStatusLabel(detailDialog.record.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailDialog.record.approver" label="审批人">
          {{ detailDialog.record.approver }}
        </el-descriptions-item>
        <el-descriptions-item v-if="detailDialog.record.rejectReason"
                              :label="detailDialog.record.status === 'CANCELLED' ? '作废原因' : '拒绝原因'">
          {{ detailDialog.record.rejectReason }}
        </el-descriptions-item>
        <el-descriptions-item v-if="detailDialog.record.borrowRecordId" label="关联借用单">
          已生成借用单 #{{ detailDialog.record.borrowRecordId }}，请在「借用记录」中归还
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialog.visible = false">关闭</el-button>
        <el-button v-if="detailDialog.record && detailDialog.record.status === 'PENDING' && (detailDialog.record.isMine || isAdmin)"
          type="danger" @click="cancelReservation" :loading="detailDialog.cancelling">
          取消预约
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, ArrowLeft, ArrowRight, Monitor, Box, DishDot, MagicStick, Histogram } from '@element-plus/icons-vue'
import { reservationApi, materialApi, categoryApi } from '../api'
import { authStore } from '../stores/auth'

const route = useRoute()

const loading = ref(false)
const materials = ref([])
const weekOffset = ref(0)
const weekReservations = ref([])
const jumpDate = ref(null)

const categories = [
  { label: '实验设备', value: 'DEVICE' },
  { label: '耗材', value: 'CONSUMABLE' },
  { label: '实验用水', value: 'WATER' },
  { label: '化学试剂', value: 'REAGENT' },
  { label: '玻璃器皿', value: 'GLASSWARE' },
]
const deviceFilter = ref('DEVICE')

// 时间行：每行代表半小时槽的整点起点。可预约窗口是 7:00-21:00，
// 故最后一行取 20:00（20:00-21:00），再往后的 21:00 行没有合法结束时间（结束必须晚于开始）
const hours = Array.from({ length: 14 }, (_, i) => i + 7) // 7:00 ~ 20:00
const weekDays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
const colors = ['#409EFF', '#67C23A', '#E6A23C', '#9B59B6', '#00C9A7', '#F56C6C']

// 当前用户 — 以 authStore 为准
const currentUser = computed(() => authStore.userInfo || {})
const isAdmin = computed(() => authStore.isAdmin)

// 本周起始日期
const monday = computed(() => {
  const now = new Date()
  const day = now.getDay() || 7 // 周日=7
  const m = new Date(now)
  m.setDate(now.getDate() - day + 1 + weekOffset.value * 7)
  m.setHours(0, 0, 0, 0)
  return m
})

const weekLabel = computed(() => {
  const m = monday.value
  const s = new Date(m)
  s.setDate(m.getDate() + 6)
  const fmt = d => `${d.getMonth() + 1}/${d.getDate()}`
  return `${fmt(m)} - ${fmt(s)}`
})

function getDayDate(offset) {
  const d = new Date(monday.value)
  d.setDate(d.getDate() + offset)
  return d
}

function formatDate(d) {
  return `${d.getMonth() + 1}/${d.getDate()}`
}

function isToday(offset) {
  const d = getDayDate(offset)
  const now = new Date()
  return d.toDateString() === now.toDateString()
}

function prevWeek() { weekOffset.value--; loadWeek() }
function nextWeek() { weekOffset.value++; loadWeek() }
function goToday() { weekOffset.value = 0; loadWeek() }
function jumpToDate(date) {
  if (!date) return
  const now = new Date()
  const day = now.getDay() || 7
  const thisMonday = new Date(now)
  thisMonday.setDate(now.getDate() - day + 1)
  thisMonday.setHours(0, 0, 0, 0)
  const target = new Date(date)
  target.setHours(0, 0, 0, 0)
  const diffDays = Math.round((target - thisMonday) / 86400000)
  weekOffset.value = Math.floor(diffDays / 7)
  loadWeek()
}

function toLocalISO(d) {
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:00`
}

function loadWeek() {
  loading.value = true
  const from = toLocalISO(monday.value)
  const toDate = new Date(monday.value)
  toDate.setDate(toDate.getDate() + 7)
  const to = toLocalISO(toDate)
  reservationApi.weekOverview(from, to).then(data => {
    weekReservations.value = Array.isArray(data) ? data : []
  }).catch(() => { weekReservations.value = [] })
    .finally(() => loading.value = false)
}

function getSlots(dayOffset, hour) {
  const slots = []
  const dayDate = getDayDate(dayOffset)
  for (const mat of weekReservations.value) {
    for (const r of (mat.reservations || [])) {
      const start = new Date(r.startTime)
      const end = new Date(r.endTime)
      if (start.toDateString() === dayDate.toDateString() && start.getHours() <= hour && end.getHours() > hour) {
        slots.push({ ...r, materialName: mat.materialName, materialCategory: mat.category, isMine: r.username === currentUser.value.username })
      }
    }
  }
  if (slots.length > 1) {
    // 多冲突用红色
    slots.forEach(s => s._conflict = true)
  }
  return slots
}

/** 预约状态：PENDING-待审批，APPROVED-已通过，COMPLETED-已完成，REJECTED/CANCELLED */
function resStatusLabel(s) {
  const map = { PENDING: '待审批', APPROVED: '已通过', COMPLETED: '已完成', REJECTED: '已拒绝', CANCELLED: '已取消' }
  return map[s] || (s || '-')
}
function resStatusTag(s) {
  const map = { PENDING: 'warning', APPROVED: 'success', COMPLETED: 'info', REJECTED: 'danger', CANCELLED: 'info' }
  return map[s] || 'info'
}

function slotStyle(slot) {
  const idx = slot.materialId % colors.length
  // 待审批：浅色 + 虚线边框，与已通过的实色区分
  if (slot.status === 'PENDING') {
    return {
      background: '#fff7e6',
      color: '#b88230',
      border: '1px dashed #e6a23c',
      fontSize: '11px',
      padding: '2px 6px',
      borderRadius: '4px',
      marginBottom: '2px',
      overflow: 'hidden',
      textOverflow: 'ellipsis',
      whiteSpace: 'nowrap',
      cursor: 'pointer'
    }
  }
  return {
    background: slot._conflict ? '#F56C6C' : colors[idx],
    color: '#fff',
    fontSize: '11px',
    padding: '2px 6px',
    borderRadius: '4px',
    marginBottom: '2px',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    whiteSpace: 'nowrap',
    cursor: 'pointer',
    opacity: slot.isMine ? 1 : 0.8
  }
}

// 设备相关 — 按分类筛选，默认只显示实验设备
const deviceMaterials = computed(() =>
  materials.value.filter(m => m.deviceStatus === 'AVAILABLE' && m.category === deviceFilter.value)
)
const availableDevices = computed(() =>
  materials.value.filter(m => ['DEVICE','CONSUMABLE','WATER','REAGENT','GLASSWARE'].includes(m.category))
)

function deviceIconComp(cat) {
  const map = { DEVICE: Monitor, CONSUMABLE: Box, WATER: DishDot, REAGENT: MagicStick, GLASSWARE: Histogram }
  return map[cat] || Box
}
function categoryColor(cat) {
  const map = { DEVICE: '#409EFF', CONSUMABLE: '#67C23A', WATER: '#00C9A7', REAGENT: '#E6A23C', GLASSWARE: '#9B59B6' }
  return map[cat] || '#909399'
}
function catCount(cat) {
  return materials.value.filter(m => m.category === cat && m.deviceStatus === 'AVAILABLE').length
}
function deviceIcon(cat) { return '' }  // 兼容旧调用
function deviceStatusLabel(s) {
  const map = { AVAILABLE: '可用', MAINTENANCE: '维修中', SCRAPPED: '已报废' }
  return map[s] || s || '可用'
}
function deviceStatusTag(s) {
  const map = { AVAILABLE: 'success', MAINTENANCE: 'warning', SCRAPPED: 'danger' }
  return map[s] || 'success'
}

/** 可预约时段常量（与模板里 el-time-select 的 start/end/step 保持一致） */
const SLOT_START = '07:00'
const SLOT_END = '21:00'
const SLOT_STEP = 30

function fmtHM(mins) {
  return String(Math.floor(mins / 60)).padStart(2, '0') + ':' + String(mins % 60).padStart(2, '0')
}

/** 今天是否已过营业结束时间（过了则今天整天不可约） */
function todayExhausted() {
  const now = new Date()
  return now.getHours() * 60 + now.getMinutes() >= 21 * 60
}

/** 过去日期不可选；今天若已过 21:00 也不可选 */
function disabledDate(time) {
  const todayStart = new Date()
  todayStart.setHours(0, 0, 0, 0)
  if (time.getTime() < todayStart.getTime()) return true
  return time.toDateString() === new Date().toDateString() && todayExhausted()
}

// 创建预约弹窗
const dialog = reactive({
  visible: false, saving: false,
  form: { materialId: null, realName: '', date: null, startTime: null, endTime: null,
    mode: 'SINGLE', recurringRule: 'WEEKLY', weeks: 4, purpose: '' }
})
const conflict = reactive({
  checked: false, hasConflict: false, conflicts: [],
  // 该设备被「直接借用」通道借走且尚未归还的数量 / 最早归还时间
  // （这类占用不落在预约时段上，时段冲突检测查不到，但实物已不在库）
  borrowedOutCount: 0, borrowedOutQuantity: 0, earliestReturnTime: null
})

/**
 * 开始时间的可选下限：选「今天」时把当前时刻向上取整到 30 分钟，
 * 已过去的时刻直接不可选 —— 避免第一眼没看清时间就点错。
 */
const minStartTime = computed(() => {
  const d = dialog.form.date
  if (!d) return SLOT_START
  const sel = new Date(d)
  if (sel.toDateString() !== new Date().toDateString()) return SLOT_START
  const now = new Date()
  const mins = Math.ceil((now.getHours() * 60 + now.getMinutes()) / SLOT_STEP) * SLOT_STEP
  if (mins > 21 * 60) return SLOT_END
  return fmtHM(mins)
})

/** 结束时间下限：至少比开始时间晚一个步长 */
const minEndTime = computed(() => {
  const st = dialog.form.startTime
  if (!st) return minStartTime.value
  const [h, m] = st.split(':').map(Number)
  return fmtHM(Math.min(h * 60 + m + SLOT_STEP, 21 * 60))
})

let conflictTimer = null
function checkConflictDebounced() {
  clearTimeout(conflictTimer)
  conflictTimer = setTimeout(checkConflict, 400)
}

function checkConflict() {
  const { materialId, date, startTime, endTime } = dialog.form
  if (!materialId || !date || !startTime || !endTime) {
    conflict.checked = false
    return
  }
  const sHour = parseInt(startTime.split(':')[0])
  const sMin = parseInt(startTime.split(':')[1])
  const eHour = parseInt(endTime.split(':')[0])
  const eMin = parseInt(endTime.split(':')[1])
  if (eHour < sHour || (eHour === sHour && eMin <= sMin)) return

  const d = new Date(date)
  const start = new Date(d.getFullYear(), d.getMonth(), d.getDate(), sHour, sMin)
  const end = new Date(d.getFullYear(), d.getMonth(), d.getDate(), eHour, eMin)

  reservationApi.checkConflict({
    materialId, startTime: toLocalISO(start), endTime: toLocalISO(end)
  }).then(data => {
    conflict.checked = true
    conflict.hasConflict = data.hasConflict
    conflict.conflicts = data.conflicts || []
    conflict.borrowedOutCount = data.borrowedOutCount || 0
    conflict.borrowedOutQuantity = data.borrowedOutQuantity || 0
    conflict.earliestReturnTime = data.earliestReturnTime || null
  }).catch(() => { conflict.checked = false })
}

function resetConflict() {
  conflict.checked = false
  conflict.hasConflict = false
  conflict.conflicts = []
  conflict.borrowedOutCount = 0
  conflict.borrowedOutQuantity = 0
  conflict.earliestReturnTime = null
}

function onDeviceChange() {
  resetConflict()
  checkConflictDebounced()
}

// 预约详情弹窗
const detailDialog = reactive({ visible: false, record: null, cancelling: false })
function showResvDetail(slot) {
  detailDialog.record = {
    ...slot,
    date: slot.startTime ? new Date(slot.startTime).toLocaleDateString('zh-CN') : '-',
    timeRange: slot.startTime && slot.endTime
      ? new Date(slot.startTime).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
        + ' ~ ' + new Date(slot.endTime).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
      : '-',
    materialCategory: slot.materialCategory || 'DEVICE',
  }
  detailDialog.visible = true
}
function cancelReservation() {
  detailDialog.cancelling = true
  reservationApi.cancel(detailDialog.record.id).then(() => {
    ElMessage.success('预约已取消')
    detailDialog.visible = false
    loadWeek()
  }).catch(e => ElMessage.error(e.message))
    .finally(() => detailDialog.cancelling = false)
}

function showCreateDialog() {
  dialog.form = { materialId: null, realName: currentUser.value.realName || '', date: null, startTime: null,
    endTime: null, mode: 'SINGLE', recurringRule: 'WEEKLY', weeks: 4, purpose: '' }
  resetConflict()
  dialog.visible = true
}

function showCreateWithDevice(device) {
  dialog.form.materialId = device.id
  dialog.form.realName = currentUser.value.realName || ''
  dialog.form.date = null
  dialog.form.startTime = null
  dialog.form.endTime = null
  dialog.form.mode = 'SINGLE'
  dialog.form.purpose = ''   // 从物资管理跳转进来时不带上次的用途说明
  resetConflict()
  dialog.visible = true
}

function showQuickCreate(dayOffset, hour) {
  const dayDate = getDayDate(dayOffset)
  // 周视图里点历史格子：直接给提示，避免带着一个已过去的时段进弹窗
  const slotStart = new Date(dayDate.getFullYear(), dayDate.getMonth(), dayDate.getDate(), hour, 0, 0)
  if (slotStart.getTime() <= Date.now()) {
    ElMessage.warning('该时段已经过去，请选择未来的时段')
    return
  }
  dialog.form = {
    materialId: null,
    realName: currentUser.value.realName || '',
    date: new Date(dayDate.getFullYear(), dayDate.getMonth(), dayDate.getDate()),
    startTime: `${String(hour).padStart(2, '0')}:00`,
    // 结束时间不能超出可预约窗口 21:00
    endTime: `${String(Math.min(hour + 1, 21)).padStart(2, '0')}:00`,
    mode: 'SINGLE', recurringRule: 'WEEKLY', weeks: 4, purpose: ''
  }
  resetConflict()
  dialog.visible = true
}

function formatConflictTime(c) {
  const s = new Date(c.startTime), e = new Date(c.endTime)
  return `${s.getMonth() + 1}/${s.getDate()} ${String(s.getHours()).padStart(2, '0')}:${String(s.getMinutes()).padStart(2, '0')} ~ ${String(e.getHours()).padStart(2, '0')}:${String(e.getMinutes()).padStart(2, '0')}`
}

/** 在借设备的预计归还时间（月日 时分） */
function formatReturnTime(t) {
  const d = new Date(t)
  return `${d.getMonth() + 1}月${d.getDate()}日 ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function handleCreate() {
  const { materialId, realName, date, startTime, endTime, mode, recurringRule, weeks, purpose } = dialog.form
  if (!materialId) { ElMessage.warning('请选择设备'); return }
  if (!realName) { ElMessage.warning('请输入预约人姓名'); return }
  if (!date || !startTime || !endTime) { ElMessage.warning('请选择时间段'); return }

  const d = new Date(date)
  const s = new Date(d.getFullYear(), d.getMonth(), d.getDate(), parseInt(startTime.split(':')[0]), parseInt(startTime.split(':')[1]))
  const e = new Date(d.getFullYear(), d.getMonth(), d.getDate(), parseInt(endTime.split(':')[0]), parseInt(endTime.split(':')[1]))

  // 兜底：日期与时刻组件已限制过去时间，这里再拦一道
  if (s.getTime() <= Date.now()) {
    ElMessage.warning('预约开始时间不能早于当前时间，请重新选择')
    return
  }
  if (e.getTime() <= s.getTime()) {
    ElMessage.warning('结束时间必须晚于开始时间')
    return
  }

  const baseData = {
    materialId, username: currentUser.value.username || realName,
    realName, purpose
  }

  dialog.saving = true
  let apiCall
  if (mode === 'RECURRING') {
    apiCall = reservationApi.createRecurring({
      ...baseData,
      firstStart: toLocalISO(s), firstEnd: toLocalISO(e),
      recurringRule, weeks
    })
  } else {
    apiCall = reservationApi.create({
      ...baseData,
      startTime: toLocalISO(s), endTime: toLocalISO(e)
    })
  }

  apiCall.then(() => {
    ElMessage.success(mode === 'RECURRING'
      ? `已提交 ${weeks} 次周期预约申请，等待教师审批`
      : '预约申请已提交，等待教师审批')
    dialog.visible = false
    loadWeek()
  }).catch(e => ElMessage.error(e.message))
    .finally(() => dialog.saving = false)
}

function loadMaterials() {
  materialApi.list({}).then(data => {
    materials.value = data
  }).catch(() => {})
}

onMounted(() => {
  loadMaterials()
  loadWeek()
  // 从物资管理页跳转过来时，自动弹出预约弹窗
  const qMaterialId = route.query.materialId
  if (qMaterialId) {
    // 等物资数据加载完再预选
    setTimeout(() => {
      const m = materials.value.find(d => d.id == qMaterialId)
      if (m) showCreateWithDevice(m)
    }, 800)
  }
})
</script>

<style scoped>
.card-title { font-size: 15px; font-weight: 600; color: #303133; }

/* 新建预约弹窗里的提示块：左缩进与输入框对齐（label-width 100px），
   宽度由本容器撑开，避免 el-alert 的 width:100% 叠加左外边距后溢出弹窗 */
.dialog-alert {
  margin: 0 0 16px 100px;
}
.dialog-alert > .el-alert + .el-alert {
  margin-top: 8px;
}

/* 周视图网格 */
.week-grid {
  display: grid;
  grid-template-columns: 60px repeat(7, 1fr);
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
}
.grid-header {
  text-align: center;
  padding: 10px 4px;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
  border-right: 1px solid #ebeef5;
}
.grid-header.today { background: #ecf5ff; }
.grid-header.today .day-label { color: #409EFF; font-weight: 700; }
.day-label { font-size: 13px; color: #606266; }
.date-label { font-size: 11px; color: #909399; margin-top: 2px; }
.grid-time {
  text-align: center;
  padding: 6px 4px;
  font-size: 12px;
  color: #909399;
  border-right: 1px solid #ebeef5;
  border-bottom: 1px solid #ebeef5;
  background: #fafafa;
}
.grid-cell {
  min-height: 40px;
  border-right: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
  padding: 2px 4px;
  cursor: pointer;
  transition: background 0.15s;
}
.grid-cell:hover { background: #ecf5ff; }
.slot-bar {
  cursor: pointer;
  transition: opacity 0.15s;
}
.slot-bar:hover { opacity: 1 !important; }
.slot-bar.conflict { border: 1px dashed #fff; }

/* 设备卡片 */
.device-card {
  cursor: pointer;
  transition: all 0.2s;
  border-radius: 10px;
  text-align: center;
  margin-bottom: 12px;
}
.device-card:hover { transform: translateY(-3px); box-shadow: 0 6px 20px rgba(0,0,0,0.1); }
.device-card.disabled { opacity: 0.5; cursor: not-allowed; }
.device-icon {
  width: 44px; height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 6px;
}
.device-name { font-size: 13px; font-weight: 600; color: #303133; margin-bottom: 8px; }
</style>
