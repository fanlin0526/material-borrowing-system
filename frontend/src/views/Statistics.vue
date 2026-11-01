#顶部 Tab「统计报表」#
<template>
  <div v-loading="loading">
    <!-- 运营指标（对标大型仪器共享平台的考核口径：机时利用率、设备完好率、有偿使用费用） -->
    <el-row :gutter="20" style="margin-bottom: 24px">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="metric-num" style="color: #E6A23C">¥{{ fee.totalFee.toFixed(2) }}</div>
          <div class="metric-label">费用合计（借用 ¥{{ fee.borrowFee.toFixed(2) }} / 预约 ¥{{ fee.reserveFee.toFixed(2) }}）</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="metric-num" style="color: #67C23A">{{ intactRate }}%</div>
          <div class="metric-label">设备完好率（{{ intactDevice }}/{{ totalDevice }} 台非报废设备可用）</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="metric-num" style="color: #409EFF">{{ avgRate }}%</div>
          <div class="metric-label">平均机时利用率（近 {{ windowDays }} 天）</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="metric-num" style="color: #9B59B6">{{ utilization.length }}</div>
          <div class="metric-label">纳入机时统计的设备数</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 部门维度与机时利用率 -->
    <el-row :gutter="20" style="margin-bottom: 24px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span class="card-title">各部门借用 / 预约量</span></template>
          <div ref="deptRef" style="height: 320px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span class="card-title">设备机时利用率 TOP（近 {{ windowDays }} 天）</span></template>
          <div ref="utilRef" style="height: 320px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 分类统计 -->
    <el-row :gutter="20" style="margin-bottom: 24px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span class="card-title">各分类物资数量分布</span></template>
          <div ref="catBarRef" style="height: 320px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span class="card-title">各分类库存容量对比</span></template>
          <div ref="catPieRef" style="height: 320px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 借用分析 -->
    <el-row :gutter="20" style="margin-bottom: 24px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span class="card-title">各分类借用频次</span></template>
          <div ref="borrowCatRef" style="height: 320px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span class="card-title">借用人排行 TOP5</span></template>
          <div ref="borrowerRef" style="height: 320px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 分类明细表 -->
    <el-card shadow="hover">
      <template #header><span class="card-title">分类物资统计明细</span></template>
      <el-table :data="categoryDetail" stripe :header-cell-style="{ background: '#f5f7fa', fontWeight: 600 }">
        <el-table-column label="分类" width="120">
          <template #default="{ row }">
            <el-tag :type="categoryTag(row.key)" effect="light">{{ row.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="count" label="种类数" width="100" align="center" />
        <el-table-column prop="totalQty" label="总库存量" width="100" align="center" />
        <el-table-column prop="availableQty" label="可借数量" width="100" align="center">
          <template #default="{ row }">
            <span style="color: #67C23A; font-weight: bold">{{ row.availableQty }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="borrowedQty" label="借出数量" width="100" align="center">
          <template #default="{ row }">
            <span style="color: #E6A23C; font-weight: bold">{{ row.borrowedQty }}</span>
          </template>
        </el-table-column>
        <el-table-column label="库存使用率" min-width="200">
          <template #default="{ row }">
            <el-progress
              :percentage="row.totalQty > 0 ? Math.round(row.borrowedQty / row.totalQty * 100) : 0"
              :color="getProgressColor(row.borrowedQty / (row.totalQty || 1))"
              :text-inside="true" :stroke-width="16"
            />
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { materialApi, borrowApi, categoryApi, analyticsApi } from '../api'

const loading = ref(false)
const catBarRef = ref(null)
const catPieRef = ref(null)
const borrowCatRef = ref(null)
const borrowerRef = ref(null)
const categoryDetail = ref([])
const categories = ref([])
const categoryMap = ref([])
// 运营指标（费用 / 完好率 / 机时利用率 / 部门维度）
const deptRef = ref(null)
const utilRef = ref(null)
const fee = reactive({ borrowFee: 0, reserveFee: 0, totalFee: 0 })
const utilization = ref([])
const intactRate = ref(0)
const intactDevice = ref(0)
const totalDevice = ref(0)
const windowDays = ref(30)
const avgRate = computed(() => {
  if (utilization.value.length === 0) return 0
  const sum = utilization.value.reduce((s, r) => s + (Number(r.rate) || 0), 0)
  return Math.round(sum / utilization.value.length * 10) / 10
})

// 颜色调色板
const catColors = ['#409EFF','#67C23A','#00C9A7','#E6A23C','#9B59B6','#F56C6C','#2196F3','#FF9800','#795548','#607D8B']

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

function resizeCharts() {
  chartInstances.forEach(c => { if (!c.isDisposed()) c.resize() })
}

function categoryTag(key) {
  const idx = categories.value.findIndex(c => c.code === key)
  const types = ['', 'success', 'info', 'warning', 'danger']
  return idx >= 0 && idx < types.length ? types[idx] : ''
}

function getProgressColor(ratio) {
  if (ratio >= 0.7) return '#F56C6C'
  if (ratio >= 0.4) return '#E6A23C'
  return '#67C23A'
}

/** 运营指标：部门维度、费用、设备完好率、机时利用率（教师/超管专属接口，失败时保持默认值） */
function loadAnalytics() {
  analyticsApi.overview().then(data => {
    if (!data) return
    fee.borrowFee = data.borrowFee || 0
    fee.reserveFee = data.reserveFee || 0
    fee.totalFee = data.totalFee || 0
    utilization.value = data.utilization || []
    intactRate.value = data.intactRate || 0
    intactDevice.value = data.intactDevice || 0
    totalDevice.value = data.totalDevice || 0
    windowDays.value = data.windowDays || 30
    nextTick(() => {
      renderDeptChart(data.byDepartment || [])
      renderUtilChart(utilization.value)
    })
  }).catch(() => { /* 无权限或接口不可用时保持默认值 */ })
}

function renderEmptyChart(el, text) {
  const chart = chartOf(el)
  if (!chart) return
  chart.clear()
  chart.setOption({
    title: { text, left: 'center', top: 'middle',
      textStyle: { color: '#C0C4CC', fontSize: 14, fontWeight: 'normal' } }
  })
}

function renderDeptChart(rows) {
  if (!deptRef.value) return
  if (rows.length === 0) { renderEmptyChart(deptRef.value, '暂无数据'); return }
  chartOf(deptRef.value).setOption({
    tooltip: { trigger: 'axis' },
    legend: { bottom: 0 },
    grid: { left: 50, right: 20, top: 30, bottom: 50 },
    xAxis: { type: 'category', data: rows.map(r => r.department), axisLabel: { interval: 0 } },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      { name: '借用', type: 'bar', stack: 'total', data: rows.map(r => r.borrowCount),
        itemStyle: { color: '#409EFF' } },
      { name: '预约', type: 'bar', stack: 'total', data: rows.map(r => r.reserveCount),
        itemStyle: { color: '#67C23A', borderRadius: [6, 6, 0, 0] } }
    ]
  })
}

function renderUtilChart(rows) {
  if (!utilRef.value) return
  if (rows.length === 0) { renderEmptyChart(utilRef.value, '近 30 天暂无已通过的预约'); return }
  const top = rows.slice(0, 8).reverse()
  chartOf(utilRef.value).setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' },
      formatter: p => p[0].name + '<br/>机时利用率：<b>' + p[0].value + '%</b>' },
    grid: { left: 100, right: 60, top: 20, bottom: 30 },
    xAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
    yAxis: { type: 'category', data: top.map(r => r.name), axisLabel: { width: 90, overflow: 'truncate' } },
    series: [{
      type: 'bar',
      data: top.map(r => r.rate),
      barWidth: '55%',
      itemStyle: { borderRadius: [0, 6, 6, 0], color: '#409EFF' },
      label: { show: true, position: 'right', formatter: '{c}%', color: '#606266' }
    }]
  })
}

function loadData() {
  loading.value = true
  loadAnalytics()
  Promise.all([
    materialApi.list({}),
    borrowApi.list({ size: 10000 }), // /api/borrow 是分页接口，取全量记录用于统计
    categoryApi.list()
  ]).then(([materials, borrowPage, cats]) => {
    const records = (borrowPage && borrowPage.content) || []
    categories.value = cats || []
    categoryMap.value = categories.value.map((c, i) => ({
      key: c.code,
      label: c.name,
      color: catColors[i % catColors.length]
    }))

    // 分类统计
    const detail = categoryMap.value.map(c => {
      const items = materials.filter(m => m.category === c.key)
      return {
        ...c,
        count: items.length,
        totalQty: items.reduce((s, m) => s + m.totalQuantity, 0),
        availableQty: items.reduce((s, m) => s + m.availableQuantity, 0),
        borrowedQty: items.reduce((s, m) => s + (m.totalQuantity - m.availableQuantity), 0),
      }
    })
    categoryDetail.value = detail

    // 各分类借用频次
    const borrowByCat = categoryMap.value.map(c => ({
      name: c.label,
      value: records.filter(r => r.materialCategory === c.key).length,
      itemStyle: { color: c.color }
    }))

    // 借用人排行
    const borrowerFreq = {}
    records.forEach(r => {
      borrowerFreq[r.borrower] = (borrowerFreq[r.borrower] || 0) + 1
    })
    const topBorrowers = Object.entries(borrowerFreq)
      .sort((a, b) => b[1] - a[1])
      .slice(0, 5)
      .map(([name, count]) => ({ name, count }))

    nextTick(() => {
      // 分类数量柱状图
      chartOf(catBarRef.value).setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: 50, right: 20, top: 30, bottom: 40 },
        xAxis: { type: 'category', data: detail.map(d => d.label), axisLabel: { interval: 0 } },
        yAxis: { type: 'value', minInterval: 1 },
        series: [{
          type: 'bar',
          data: detail.map(d => ({ value: d.count, itemStyle: { color: d.color } })),
          barWidth: '50%',
          itemStyle: { borderRadius: [6, 6, 0, 0] },
          label: { show: true, position: 'top' }
        }]
      })

      // 库存容量饼图
      chartOf(catPieRef.value).setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: { bottom: 0 },
        series: [{
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['50%', '45%'],
          data: detail.map(d => ({ name: d.label, value: d.totalQty, itemStyle: { color: d.color } })),
          itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
          label: { formatter: '{b}\n{c}' }
        }]
      })

      // 借用频次柱状图
      chartOf(borrowCatRef.value).setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: 50, right: 20, top: 30, bottom: 40 },
        xAxis: { type: 'category', data: borrowByCat.map(d => d.name), axisLabel: { interval: 0 } },
        yAxis: { type: 'value', minInterval: 1 },
        series: [{
          type: 'bar',
          data: borrowByCat,
          barWidth: '50%',
          itemStyle: { borderRadius: [6, 6, 0, 0] },
          label: { show: true, position: 'top' }
        }]
      })

      // 借用人排行
      const names = topBorrowers.map(b => b.name).reverse()
      const counts = topBorrowers.map(b => b.count).reverse()
      chartOf(borrowerRef.value).setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: 70, right: 40, top: 20, bottom: 30 },
        xAxis: { type: 'value', minInterval: 1 },
        yAxis: { type: 'category', data: names },
        series: [{
          type: 'bar',
          data: counts,
          barWidth: '50%',
          itemStyle: { borderRadius: [0, 6, 6, 0], color: '#409EFF' },
          label: { show: true, position: 'right', formatter: '{c} 次' }
        }]
      })
    })
  }).catch(e => ElMessage.error('加载失败：' + e.message))
    .finally(() => loading.value = false)
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
.card-title { font-size: 15px; font-weight: 600; color: #303133; }
.metric-num { font-size: 26px; font-weight: 700; line-height: 1.2; }
.metric-label { font-size: 13px; color: #909399; margin-top: 6px; line-height: 1.5; }
</style>
