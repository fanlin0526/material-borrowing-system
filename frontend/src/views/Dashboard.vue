#顶部 Tab「数据大盘」#
<template>
  <div v-loading="loading">
    <!-- ===== 角色横幅 ===== -->
    <div class="role-banner" :class="roleBannerClass">
      <div class="banner-left">
        <span class="banner-icon">{{ roleBanner.icon }}</span>
        <div>
          <div class="banner-title">{{ roleBanner.title }}</div>
          <div class="banner-sub">{{ roleBanner.desc }}</div>
        </div>
      </div>
      <div class="banner-actions">
        <el-button
          v-for="act in roleBanner.actions" :key="act.path"
          :type="roleBanner.btnType"
          size="small"
          @click="$router.push(act.path)"
        >{{ act.label }}</el-button>
      </div>
    </div>

    <!-- ===== 统计卡片 6 个 ===== -->
    <el-row :gutter="20" style="margin-bottom: 24px">
      <el-col :span="4" v-for="(card, i) in cards" :key="i">
        <el-card
          shadow="hover" class="stat-card"
          :class="{ 'stat-card-clickable': card.to }"
          :body-style="{ padding: '20px' }"
          @click="card.to && $router.push(card.to)"
        >
          <div class="stat-card-inner">
            <div class="stat-icon" :style="{ background: card.bg }">
              <span style="font-size: 24px">{{ card.icon }}</span>
            </div>
            <div class="stat-info">
              <div class="stat-num" :style="{ color: card.color }">{{ card.value }}</div>
              <div class="stat-label">{{ card.label }}<span v-if="card.to" class="stat-more">›</span></div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ===== Row 1：分类分布 + 设备使用率排名 ===== -->
    <el-row :gutter="20" style="margin-bottom: 24px">
      <el-col :span="9">
        <el-card shadow="hover">
          <template #header><span class="card-title">物料分类分布</span></template>
          <div ref="pieRef" style="height: 320px"></div>
        </el-card>
      </el-col>
      <el-col :span="15">
        <el-card shadow="hover">
          <template #header><span class="card-title">{{ chartTitles.device }}</span></template>
          <div ref="deviceUsageRef" style="height: 320px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ===== Row 2：月度对比 + 健康度仪表盘 + 分类消耗 ===== -->
    <el-row :gutter="20" style="margin-bottom: 24px">
      <el-col :span="9">
        <el-card shadow="hover">
          <template #header><span class="card-title">{{ chartTitles.monthly }}</span></template>
          <div ref="monthlyRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="7">
        <el-card shadow="hover">
          <template #header>
            <span class="card-title">库存充足率</span>
            <el-tooltip placement="top" content="口径：可借数量高于自身预警阈值的物资占比（替代原先自造的「库存健康度」；设备完好率与机时利用率见「统计报表」）">
              <el-icon style="margin-left: 4px; color: #909399; vertical-align: middle"><QuestionFilled /></el-icon>
            </el-tooltip>
          </template>
          <div ref="healthRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header><span class="card-title">分类消耗比例</span></template>
          <div ref="consumeRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ===== Row 3：近7天趋势 + 最近记录 ===== -->
    <el-row :gutter="20" style="margin-bottom: 24px">
      <el-col :span="9">
        <el-card shadow="hover">
          <template #header><span class="card-title">{{ chartTitles.trend }}</span></template>
          <div ref="trendRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="15">
        <el-card shadow="hover">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span class="card-title">{{ chartTitles.recent }}</span>
              <el-button type="primary" link @click="$router.push('/borrow')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="recentRecords" size="small" style="width: 100%" :max-height="300"
                    :empty-text="isStaffScope ? '暂无借用记录' : '您还没有借用记录'">
            <el-table-column prop="materialName" label="物资名称" min-width="120" show-overflow-tooltip />
            <el-table-column label="类别" width="90">
              <template #default="{ row }">
                <el-tag :type="categoryTag(row.materialCategory)" size="small" effect="light">
                  {{ categoryLabel(row.materialCategory) }}
                </el-tag>
              </template>
            </el-table-column>
            <!-- 学生口径下所有记录都是本人，借用人列无信息量 -->
            <el-table-column v-if="isStaffScope" prop="borrower" label="借用人" width="80" />
            <el-table-column prop="borrowQuantity" label="数量" width="60" align="center" />
            <el-table-column label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="statusTag(row.status)" size="small">
                  {{ statusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="借用时间" width="140">
              <template #default="{ row }">
                {{ formatTime(row.borrowTime) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- ===== Row 4：热门借用物资 TOP5 + 借用时段热力分布 ===== -->
    <el-row :gutter="20" style="margin-bottom: 24px">
      <el-col :span="9">
        <el-card shadow="hover">
          <template #header><span class="card-title">{{ chartTitles.hot }}</span></template>
          <div ref="hotRef" style="height: 320px"></div>
        </el-card>
      </el-col>
      <el-col :span="15">
        <el-card shadow="hover">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span class="card-title">{{ chartTitles.heat }}</span>
              <!-- 后端只统计 8-18 点内的借用，窗口必须写出来，否则会被误读成"全部时段" -->
              <span style="font-size: 12px; color: #909399">最近 30 天 · 开放时段 8:00-18:00</span>
            </div>
          </template>
          <div ref="heatRef" style="height: 320px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick, computed } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { QuestionFilled } from '@element-plus/icons-vue'
import { dashboardApi, categoryApi } from '../api'
import { authStore } from '../stores/auth'

// 当前用户 — 以 authStore 为准
const currentUser = computed(() => authStore.userInfo || {})
const roleClass = computed(() => {
  if (authStore.isSuperAdmin) return 'super'
  if (authStore.isAdmin) return 'teacher'
  return 'student'
})

// 角色专属横幅
const roleBanner = computed(() => {
  if (currentUser.value.role === 'SUPER_ADMIN') {
    return {
      icon: '🛡️', title: '超级管理员面板',
      desc: '系统全局管理 · 用户权限 · 分类配置 · 数据审计',
      btnType: 'danger', actions: []
    }
  }
  if (currentUser.value.role === 'TEACHER') {
    return {
      icon: '📚', title: '教师工作台',
      desc: '物资审批 · 借用管理 · 教学实验支持',
      btnType: 'warning', actions: []
    }
  }
  return {
    icon: '🧑‍🔬', title: '学生工作台',
    desc: '实验物资借用 · 预约设备 · 查看记录',
    btnType: 'primary', actions: []
  }
})
const roleBannerClass = computed(() => 'banner-' + roleClass.value)

const loading = ref(false)
const recentRecords = ref([])
const categories = ref([])

/**
 * 借用类指标的口径随角色变化：教师/超管看全量，学生只看本人。
 * 口径由服务端决定（DashboardController.scopeRecords），前端只负责把标题写对 ——
 * 否则学生会把「借用中」这种字样误读成全校数字。
 * 物资类指标（物资总数/库存预警/库存健康）是全局的，不随角色变。
 */
const isStaffScope = computed(() => authStore.isAdmin)

const chartTitles = computed(() => ({
  device: isStaffScope.value ? '设备使用率排名' : '我的常用设备',
  monthly: isStaffScope.value ? '借用月度对比' : '我的借用月度对比',
  trend: isStaffScope.value ? '近7天借用趋势' : '我近7天借用趋势',
  recent: isStaffScope.value ? '最近借用记录' : '我的最近记录',
  hot: isStaffScope.value ? '热门借用物资 TOP5' : '我的常用物资 TOP5',
  heat: isStaffScope.value ? '借用时段热力分布' : '我的借用时段分布',
}))

const cardValues = reactive({
  totalMaterials: 0, borrowingCount: 0, totalRecords: 0,
  lowStockCount: 0, healthScore: '0%', thisMonth: 0
})

const cards = computed(() => [
  { label: '物资总数', value: cardValues.totalMaterials, icon: '📦', color: '#409EFF', bg: '#ecf5ff' },
  { label: isStaffScope.value ? '借用中' : '我借用中', value: cardValues.borrowingCount, icon: '🔄', color: '#E6A23C', bg: '#fdf6ec' },
  { label: isStaffScope.value ? '借用记录' : '我的记录', value: cardValues.totalRecords, icon: '📋', color: '#67C23A', bg: '#f0f9eb' },
  // 带 to 的卡片可下钻；「库存预警」跳物资列表的预警视图（数据源即 /material/low-stock）
  { label: '库存预警', value: cardValues.lowStockCount, icon: '⚠️', color: '#F56C6C', bg: '#fef0f0',
    to: { path: '/material', query: { lowStock: '1' } } },
  { label: '库存充足率', value: cardValues.healthScore, icon: '💚', color: '#00C9A7', bg: '#e8f8f5' },
  { label: isStaffScope.value ? '本月借用' : '我本月借用', value: cardValues.thisMonth, icon: '📈', color: '#9B59B6', bg: '#f5f0ff' },
])

// 最近记录表里的状态列：原实现只有"借用中/已归还"两态，
// 学生看到自己的待审批、已损坏、已逾期记录会被错误显示成"已归还"
const STATUS_LABEL = {
  PENDING: '待审批', APPROVED: '已通过', REJECTED: '已拒绝', PENDING_EXTEND: '待审批延期',
  BORROWED: '借用中', CONSUMED: '已领用', RETURNED: '已归还', OVERDUE: '已逾期', DAMAGED: '已损坏'
}
const STATUS_TAG = {
  PENDING: 'info', APPROVED: 'warning', REJECTED: 'danger', PENDING_EXTEND: 'info',
  BORROWED: 'warning', CONSUMED: 'success', RETURNED: 'success', OVERDUE: 'danger', DAMAGED: 'danger'
}
function statusLabel(s) { return STATUS_LABEL[s] || s }
function statusTag(s) { return STATUS_TAG[s] || 'info' }

const pieRef = ref(null)
const trendRef = ref(null)
const deviceUsageRef = ref(null)
const monthlyRef = ref(null)
const healthRef = ref(null)
const consumeRef = ref(null)
const hotRef = ref(null)
const heatRef = ref(null)

/**
 * ECharts 实例登记表。
 * 同一 DOM 重复 echarts.init 会报「already initialized」，组件卸载后不 dispose
 * 又会把 canvas 一直留在内存里，所以统一从这里取实例，并在卸载时集中释放。
 */
const chartInstances = new Set()

function chartOf(el) {
  if (!el) return null
  let inst = echarts.getInstanceByDom(el)
  if (!inst) {
    inst = echarts.init(el)
    chartInstances.add(inst)
  }
  return inst
}

/** 窗口尺寸变化时重绘（侧栏/筛选面板伸缩同样会改动画布宽度） */
function resizeCharts() {
  chartInstances.forEach(c => { if (!c.isDisposed()) c.resize() })
}

const categoryColors = {}

function categoryLabel(cat) {
  const c = categories.value.find(c => c.code === cat)
  return c ? c.name : cat
}

function categoryTag(cat) {
  const idx = categories.value.findIndex(c => c.code === cat)
  const types = ['', 'success', 'info', 'warning', 'danger']
  return idx >= 0 && idx < types.length ? types[idx] : ''
}

function formatTime(t) {
  if (!t) return '-'
  const d = new Date(t)
  return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

/**
 * ECharts 空数据占位。
 * 借用类图表按角色分口径后，学生（尤其新建账号）可能一条记录都没有，
 * 这些卡片会渲染成一块空白，容易被误认为页面坏了。
 */
function renderEmpty(el, text = '暂无数据', subtext = '') {
  if (!el) return
  const chart = chartOf(el)
  if (!chart) return
  chart.clear()
  chart.setOption({
    title: {
      text, subtext,
      left: 'center', top: 'middle',
      textStyle: { color: '#C0C4CC', fontSize: 14, fontWeight: 'normal' },
      subtextStyle: { color: '#DCDFE6', fontSize: 12 }
    }
  })
}

function loadData() {
  loading.value = true
  // 先加载分类数据
  categoryApi.list().then(cats => {
    categories.value = cats || []
    // 构建中文名→颜色映射
    const colorPalette = ['#409EFF','#67C23A','#00C9A7','#E6A23C','#9B59B6','#F56C6C','#2196F3','#FF9800']
    categories.value.forEach((c, i) => {
      categoryColors[c.name] = colorPalette[i % colorPalette.length]
    })
  }).catch(() => {})

  // 先加载基础数据（旧接口，一定可用）
  dashboardApi.overview().then(overview => {
    cardValues.totalMaterials = overview.cards.totalMaterials
    cardValues.borrowingCount = overview.cards.borrowingCount
    cardValues.totalRecords = overview.cards.totalRecords
    cardValues.lowStockCount = overview.cards.lowStockCount
    recentRecords.value = overview.recentRecords

    nextTick(() => {
      initPieChart(overview.categoryPie)
      initTrendChart(overview.borrowTrend)
      initHotChart(overview.topBorrowed)
    })

    // 再尝试加载扩展数据（新接口，不可用时降级）
    return dashboardApi.detail().then(detail => {
      cardValues.healthScore = (detail.health && detail.health.score != null) ? detail.health.score + '%' : '0%'
      cardValues.thisMonth = (detail.monthlyComparison && detail.monthlyComparison.thisMonth != null)
        ? detail.monthlyComparison.thisMonth : 0
      nextTick(() => {
        initDeviceUsageChart(detail.deviceUsage)
        initMonthlyChart(detail.monthlyComparison)
        initHealthChart(detail.health)
        initConsumeChart(detail.consumeRate)
        initHeatChart(detail.heatmap)
      })
    }).catch(() => {
      // detail 接口不可用，降级显示
      cardValues.healthScore = '--'
      cardValues.thisMonth = '--'
    })
  }).catch(e => ElMessage.error('加载失败：' + e.message))
    .finally(() => loading.value = false)
}

function initPieChart(pieData) {
  if (!pieRef.value) return
  const chart = chartOf(pieRef.value)
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, left: 'center', icon: 'circle' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 16, fontWeight: 'bold' } },
      data: pieData.map(item => ({
        ...item,
        itemStyle: { color: categoryColors[item.name] || '#909399' }
      }))
    }]
  })
}

function initTrendChart(trend) {
  if (!trendRef.value) return
  const chart = chartOf(trendRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 30, bottom: 30 },
    xAxis: { type: 'category', data: trend.dates, axisLine: { lineStyle: { color: '#ddd' } } },
    yAxis: { type: 'value', minInterval: 1, splitLine: { lineStyle: { color: '#f0f0f0' } } },
    series: [{
      type: 'bar',
      data: trend.counts,
      barWidth: '45%',
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#409EFF' },
          { offset: 1, color: '#a0cfff' }
        ])
      }
    }]
  })
}

// ===== 新增：设备使用率排名（横向柱状图） =====
function initDeviceUsageChart(data) {
  if (!deviceUsageRef.value) return
  if (!data || data.length === 0) { renderEmpty(deviceUsageRef.value, '暂无借用记录'); return }
  const chart = chartOf(deviceUsageRef.value)
  const top8 = data.slice(0, 8)
  const names = top8.map(d => d.name).reverse()
  const times = top8.map(d => d.times).reverse()
  const maxVal = Math.max(...times, 1)
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' },
      formatter: p => `${p[0].name}<br/>借用次数：<b>${p[0].value}</b> 次` },
    grid: { left: 80, right: 50, top: 20, bottom: 30 },
    xAxis: { type: 'value', max: maxVal + 2, minInterval: 1, splitLine: { lineStyle: { color: '#f0f0f0' } } },
    yAxis: { type: 'category', data: names, axisLine: { lineStyle: { color: '#ddd' } },
      axisLabel: { width: 70, overflow: 'truncate' } },
    series: [{
      type: 'bar',
      data: times,
      barWidth: '55%',
      itemStyle: {
        borderRadius: [0, 6, 6, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#409EFF' },
          { offset: 1, color: '#79bbff' }
        ])
      },
      label: { show: true, position: 'right', formatter: '{c} 次', color: '#606266' }
    }]
  })
}

// ===== 新增：月度对比（双柱对比图） =====
function initMonthlyChart(data) {
  if (!monthlyRef.value || !data) return
  const chart = chartOf(monthlyRef.value)
  const labels = [data.lastMonthLabel || '上月', data.thisMonthLabel || '本月']
  const values = [data.lastMonth || 0, data.thisMonth || 0]
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 30, top: 40, bottom: 40 },
    xAxis: { type: 'category', data: labels, axisLine: { lineStyle: { color: '#ddd' } } },
    yAxis: { type: 'value', minInterval: 1, splitLine: { lineStyle: { color: '#f0f0f0' } } },
    series: [{
      type: 'bar',
      data: [
        { value: values[0], itemStyle: { color: '#c0c4cc' } },
        { value: values[1], itemStyle: { color: '#409EFF' } },
      ],
      barWidth: '50%',
      itemStyle: { borderRadius: [6, 6, 0, 0] },
      label: { show: true, position: 'top', fontSize: 14, fontWeight: 'bold' }
    }]
  })
}

// ===== 新增：库存健康度仪表盘 =====
function initHealthChart(data) {
  if (!healthRef.value || !data) return
  const chart = chartOf(healthRef.value)
  const score = data.score || 0
  chart.setOption({
    series: [
      {
        type: 'gauge',
        startAngle: 210,
        endAngle: -30,
        center: ['50%', '55%'],
        radius: '90%',
        min: 0,
        max: 100,
        splitNumber: 10,
        axisLine: {
          show: true,
          lineStyle: {
            width: 18,
            color: [
              [0.4, '#F56C6C'],
              [0.7, '#E6A23C'],
              [0.9, '#67C23A'],
              [1, '#409EFF']
            ]
          }
        },
        pointer: {
          icon: 'path://M12.8,0.7l12,40.1H0.7L12.8,0.7z',
          length: '70%',
          width: 8,
          itemStyle: { color: 'auto' }
        },
        axisTick: { length: 8, lineStyle: { color: 'auto', width: 2 } },
        splitLine: { length: 20, lineStyle: { color: 'auto', width: 4 } },
        axisLabel: { distance: -40, color: '#909399', fontSize: 10 },
        detail: {
          valueAnimation: true,
          formatter: '{value}%',
          color: '#303133',
          fontSize: 24,
          fontWeight: 'bold',
          offsetCenter: [0, '65%']
        },
        title: { offsetCenter: [0, '90%'], fontSize: 12, color: '#909399' },
        data: [{ value: score, name: '库存健康度' }]
      }
    ]
  })
}

// ===== 新增：分类消耗比例（环形图） =====
function initConsumeChart(data) {
  if (!consumeRef.value || !data) return
  // 环形图只画已消耗的类别，全部满库存时会是空环，需要占位
  const used = data.filter(d => d.consumed > 0)
  if (used.length === 0) { renderEmpty(consumeRef.value, '暂无消耗记录'); return }
  const chart = chartOf(consumeRef.value)
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}件 ({d}%)' },
    legend: { bottom: 0, left: 'center', icon: 'circle', textStyle: { fontSize: 11 } },
    series: [{
      type: 'pie',
      radius: ['45%', '75%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: used.map(d => ({
        name: d.name,
        value: d.consumed,
        itemStyle: { color: categoryColors[d.name] || '#909399' }
      }))
    }]
  })
}

// ===== 热门借用物资 TOP5（横向柱状图） =====
// 与「设备使用率排名」的区别：那个只看 DEVICE 类，这里覆盖全部类别，
// 玻璃器皿、耗材这类高频物资只有此处能体现。
function initHotChart(data) {
  if (!hotRef.value) return
  if (!data || data.length === 0) { renderEmpty(hotRef.value, '暂无借用记录'); return }
  const chart = chartOf(hotRef.value)
  const top = data.slice(0, 5)
  const names = top.map(d => d.name).reverse()
  const counts = top.map(d => d.count).reverse()
  const maxVal = Math.max(...counts, 1)
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' },
      formatter: p => `${p[0].name}<br/>借用次数：<b>${p[0].value}</b> 次` },
    grid: { left: 90, right: 50, top: 20, bottom: 30 },
    xAxis: { type: 'value', max: maxVal + 2, minInterval: 1, splitLine: { lineStyle: { color: '#f0f0f0' } } },
    yAxis: { type: 'category', data: names, axisLine: { lineStyle: { color: '#ddd' } },
      axisLabel: { width: 80, overflow: 'truncate' } },
    series: [{
      type: 'bar',
      data: counts,
      barWidth: '55%',
      itemStyle: {
        borderRadius: [0, 6, 6, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#9B59B6' },
          { offset: 1, color: '#c39bd3' }
        ])
      },
      label: { show: true, position: 'right', formatter: '{c} 次', color: '#606266' }
    }]
  })
}

// ===== 借用时段热力分布（星期 × 时段，后端仅统计最近 30 天） =====
const WEEK_LABELS = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
const HEAT_HOURS = [8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]

function initHeatChart(data) {
  if (!heatRef.value || !data) return
  // 后端返回 周一~周日 → 小时 → 次数 的嵌套对象，转成 ECharts 需要的 [x, y, value]
  const points = []
  let total = 0
  WEEK_LABELS.forEach((w, di) => {
    const hourMap = data[w] || {}
    HEAT_HOURS.forEach((h, hi) => {
      const v = hourMap[h] || hourMap[String(h)] || 0
      total += v
      points.push([hi, di, v])
    })
  })
  if (total === 0) {
    renderEmpty(heatRef.value, '近 30 天暂无借用记录', '仅统计 8:00-18:00 时段内的借用')
    return
  }

  const chart = chartOf(heatRef.value)
  const maxVal = Math.max(...points.map(p => p[2]), 1)
  chart.setOption({
    tooltip: {
      position: 'top',
      formatter: p => `${WEEK_LABELS[p.value[1]]} ${HEAT_HOURS[p.value[0]]}:00-${HEAT_HOURS[p.value[0]] + 1}:00<br/>借用 <b>${p.value[2]}</b> 次`
    },
    grid: { left: 55, right: 20, top: 20, bottom: 62 },
    xAxis: {
      type: 'category',
      data: HEAT_HOURS.map(h => h + ':00'),
      splitArea: { show: true },
      axisLabel: { fontSize: 10, color: '#909399' }
    },
    yAxis: {
      type: 'category',
      data: WEEK_LABELS,
      splitArea: { show: true },
      axisLabel: { fontSize: 11, color: '#909399' }
    },
    visualMap: {
      min: 0, max: maxVal, calculable: true,
      orient: 'horizontal', left: 'center', bottom: 0,
      itemWidth: 12, itemHeight: 90,
      inRange: { color: ['#ecf5ff', '#a0cfff', '#409EFF', '#1a56db'] },
      textStyle: { fontSize: 10, color: '#909399' }
    },
    series: [{
      type: 'heatmap',
      data: points,
      label: { show: false },
      itemStyle: { borderRadius: 3, borderColor: '#fff', borderWidth: 2 },
      emphasis: { itemStyle: { shadowBlur: 8, shadowColor: 'rgba(0,0,0,0.2)' } }
    }]
  })
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', resizeCharts)
})

// 离开页面时释放所有图表实例，避免 canvas 常驻内存
onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  chartInstances.forEach(c => { if (!c.isDisposed()) c.dispose() })
  chartInstances.clear()
})
</script>

<style scoped>
.stat-card { border-radius: 10px; }
.stat-card-inner { display: flex; align-items: center; gap: 14px; }
/* 可下钻卡片：给出手型光标与悬停描边，避免"碰巧能点"的误导感 */
.stat-card-clickable { cursor: pointer; transition: border-color .2s, transform .2s; }
.stat-card-clickable:hover { border-color: #F56C6C; transform: translateY(-2px); }
.stat-more { color: #F56C6C; font-weight: 700; margin-left: 2px; }
.stat-icon {
  width: 52px; height: 52px;
  border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.stat-num { font-size: 26px; font-weight: 700; line-height: 1.2; }
.stat-label { font-size: 13px; color: #909399; margin-top: 2px; }
.card-title { font-size: 15px; font-weight: 600; color: #303133; }

/* ===== 角色横幅 ===== */
.role-banner {
  display: flex; justify-content: space-between; align-items: center;
  padding: 18px 24px; border-radius: 12px; margin-bottom: 24px;
  color: #fff; flex-wrap: wrap; gap: 12px;
}
.banner-left { display: flex; align-items: center; gap: 14px; }
.banner-icon { font-size: 36px; line-height: 1; }
.banner-title { font-size: 20px; font-weight: 700; }
.banner-sub { font-size: 13px; opacity: 0.85; margin-top: 2px; }
.banner-actions { display: flex; gap: 8px; flex-wrap: wrap; }

/* 超管：深红金属渐变 */
.banner-super {
  background: linear-gradient(135deg, #1a0a0a 0%, #8B0000 40%, #C62828 100%);
  box-shadow: 0 4px 20px rgba(198,40,40,0.3);
}
.banner-super .banner-actions :deep(.el-button--danger) {
  background: rgba(255,255,255,0.15); border-color: rgba(255,255,255,0.25); color: #fff;
}
.banner-super .banner-actions :deep(.el-button--danger:hover) {
  background: rgba(255,255,255,0.25);
}

/* 教师：深绿渐变 */
.banner-teacher {
  background: linear-gradient(135deg, #0a1a0a 0%, #2E7D32 40%, #43A047 100%);
  box-shadow: 0 4px 20px rgba(46,125,50,0.3);
}
.banner-teacher .banner-actions :deep(.el-button--warning) {
  background: rgba(255,255,255,0.15); border-color: rgba(255,255,255,0.25); color: #fff;
}
.banner-teacher .banner-actions :deep(.el-button--warning:hover) {
  background: rgba(255,255,255,0.25);
}

/* 学生：蓝色渐变 */
.banner-student {
  background: linear-gradient(135deg, #0a1538 0%, #1a56db 40%, #3B82F6 100%);
  box-shadow: 0 4px 20px rgba(59,130,246,0.3);
}
.banner-student .banner-actions :deep(.el-button--primary) {
  background: rgba(255,255,255,0.15); border-color: rgba(255,255,255,0.25); color: #fff;
}
.banner-student .banner-actions :deep(.el-button--primary:hover) {
  background: rgba(255,255,255,0.25);
}
</style>
