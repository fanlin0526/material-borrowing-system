<template>
  <div v-loading="loading">
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
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { materialApi, borrowApi, categoryApi } from '../api'

const loading = ref(false)
const catBarRef = ref(null)
const catPieRef = ref(null)
const borrowCatRef = ref(null)
const borrowerRef = ref(null)
const categoryDetail = ref([])
const categories = ref([])
const categoryMap = ref([])

// 颜色调色板
const catColors = ['#409EFF','#67C23A','#00C9A7','#E6A23C','#9B59B6','#F56C6C','#2196F3','#FF9800','#795548','#607D8B']

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

function loadData() {
  loading.value = true
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
      echarts.init(catBarRef.value).setOption({
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
      echarts.init(catPieRef.value).setOption({
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
      echarts.init(borrowCatRef.value).setOption({
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
      echarts.init(borrowerRef.value).setOption({
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

onMounted(loadData)
</script>

<style scoped>
.card-title { font-size: 15px; font-weight: 600; color: #303133; }
</style>
