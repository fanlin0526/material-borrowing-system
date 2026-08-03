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
        <el-card shadow="hover" class="stat-card" :body-style="{ padding: '20px' }">
          <div class="stat-card-inner">
            <div class="stat-icon" :style="{ background: card.bg }">
              <span style="font-size: 24px">{{ card.icon }}</span>
            </div>
            <div class="stat-info">
              <div class="stat-num" :style="{ color: card.color }">{{ card.value }}</div>
              <div class="stat-label">{{ card.label }}</div>
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
          <template #header><span class="card-title">设备使用率排名</span></template>
          <div ref="deviceUsageRef" style="height: 320px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ===== Row 2：月度对比 + 健康度仪表盘 + 分类消耗 ===== -->
    <el-row :gutter="20" style="margin-bottom: 24px">
      <el-col :span="9">
        <el-card shadow="hover">
          <template #header><span class="card-title">借用月度对比</span></template>
          <div ref="monthlyRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="7">
        <el-card shadow="hover">
          <template #header><span class="card-title">库存健康度</span></template>
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
          <template #header><span class="card-title">近7天借用趋势</span></template>
          <div ref="trendRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="15">
        <el-card shadow="hover">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span class="card-title">最近借用记录</span>
              <el-button type="primary" link @click="$router.push('/borrow')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="recentRecords" size="small" style="width: 100%" :max-height="300">
            <el-table-column prop="materialName" label="物资名称" min-width="120" show-overflow-tooltip />
            <el-table-column label="类别" width="90">
              <template #default="{ row }">
                <el-tag :type="categoryTag(row.materialCategory)" size="small" effect="light">
                  {{ categoryLabel(row.materialCategory) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="borrower" label="借用人" width="80" />
            <el-table-column prop="borrowQuantity" label="数量" width="60" align="center" />
            <el-table-column label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 'BORROWED' ? 'warning' : 'success'" size="small">
                  {{ row.status === 'BORROWED' ? '借用中' : '已归还' }}
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
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { dashboardApi, categoryApi } from '../api'

// 当前用户
const currentUser = computed(() => {
  try { return JSON.parse(localStorage.getItem('user') || '{}') } catch { return {} }
})
const roleClass = computed(() => {
  if (currentUser.value.role === 'SUPER_ADMIN') return 'super'
  if (currentUser.value.role === 'TEACHER') return 'teacher'
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

const cards = ref([
  { label: '物资总数', value: 0, icon: '📦', color: '#409EFF', bg: '#ecf5ff' },
  { label: '借用中', value: 0, icon: '🔄', color: '#E6A23C', bg: '#fdf6ec' },
  { label: '借用记录', value: 0, icon: '📋', color: '#67C23A', bg: '#f0f9eb' },
  { label: '库存预警', value: 0, icon: '⚠️', color: '#F56C6C', bg: '#fef0f0' },
  { label: '库存健康', value: '0%', icon: '💚', color: '#00C9A7', bg: '#e8f8f5' },
  { label: '本月借用', value: 0, icon: '📈', color: '#9B59B6', bg: '#f5f0ff' },
])

const pieRef = ref(null)
const trendRef = ref(null)
const deviceUsageRef = ref(null)
const monthlyRef = ref(null)
const healthRef = ref(null)
const consumeRef = ref(null)

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
    cards.value[0].value = overview.cards.totalMaterials
    cards.value[1].value = overview.cards.borrowingCount
    cards.value[2].value = overview.cards.totalRecords
    cards.value[3].value = overview.cards.lowStockCount
    recentRecords.value = overview.recentRecords

    nextTick(() => {
      initPieChart(overview.categoryPie)
      initTrendChart(overview.borrowTrend)
    })

    // 再尝试加载扩展数据（新接口，不可用时降级）
    return dashboardApi.detail().then(detail => {
      cards.value[4].value = (detail.health && detail.health.score != null) ? detail.health.score + '%' : '0%'
      cards.value[5].value = (detail.monthlyComparison && detail.monthlyComparison.thisMonth != null)
        ? detail.monthlyComparison.thisMonth : 0
      nextTick(() => {
        initDeviceUsageChart(detail.deviceUsage)
        initMonthlyChart(detail.monthlyComparison)
        initHealthChart(detail.health)
        initConsumeChart(detail.consumeRate)
      })
    }).catch(() => {
      // detail 接口不可用，降级显示
      cards.value[4].value = '--'
      cards.value[5].value = '--'
    })
  }).catch(e => ElMessage.error('加载失败：' + e.message))
    .finally(() => loading.value = false)
}

function initPieChart(pieData) {
  if (!pieRef.value) return
  const chart = echarts.init(pieRef.value)
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
  const chart = echarts.init(trendRef.value)
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
  if (!deviceUsageRef.value || !data || data.length === 0) return
  const chart = echarts.init(deviceUsageRef.value)
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
  const chart = echarts.init(monthlyRef.value)
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
  const chart = echarts.init(healthRef.value)
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
  const chart = echarts.init(consumeRef.value)
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
      data: data.filter(d => d.consumed > 0).map(d => ({
        name: d.name,
        value: d.consumed,
        itemStyle: { color: categoryColors[d.name] || '#909399' }
      }))
    }]
  })
}

onMounted(loadData)
</script>

<style scoped>
.stat-card { border-radius: 10px; }
.stat-card-inner { display: flex; align-items: center; gap: 14px; }
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
