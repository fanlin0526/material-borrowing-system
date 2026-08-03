<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span class="card-title">系统配置</span>
          <el-button type="primary" @click="saveConfig">保存配置</el-button>
        </div>
      </template>
      <el-form label-width="180px" label-position="left" v-loading="loading">
        <el-form-item label="单次借用最长天数">
          <el-input-number v-model="config.borrow_max_days" :min="1" :max="90" />
          <span class="form-tip">学生一次最多借多少天</span>
        </el-form-item>
        <el-form-item label="单次最多借用数量">
          <el-input-number v-model="config.borrow_max_items" :min="1" :max="50" />
          <span class="form-tip">一次最多借几件物资</span>
        </el-form-item>
        <el-form-item label="预约最多提前天数">
          <el-input-number v-model="config.reserve_advance_days" :min="1" :max="90" />
          <span class="form-tip">最多提前多少天预约设备</span>
        </el-form-item>
        <el-form-item label="预约最短时长（小时）">
          <el-input-number v-model="config.reserve_min_hours" :min="1" :max="24" :step="0.5" :precision="1" />
          <span class="form-tip">每次预约最少占用多长时间</span>
        </el-form-item>
        <el-form-item label="逾期每日违约金">
          <el-input-number v-model="config.overdue_fine_per_day" :min="0" :max="100" />
          <span class="form-tip">元/天，0=不开启违约金</span>
        </el-form-item>
        <el-form-item label="预约未取自动取消">
          <el-input-number v-model="config.auto_cancel_hours" :min="0" :max="72" />
          <span class="form-tip">小时，预约后超时未取自动取消，0=不开启</span>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const config = reactive({
  borrow_max_days: 14,
  borrow_max_items: 5,
  reserve_advance_days: 7,
  reserve_min_hours: 2,
  overdue_fine_per_day: 0,
  auto_cancel_hours: 24,
})

onMounted(async () => {
  loading.value = true
  try {
    const res = await fetch('/api/admin/config')
    const json = await res.json()
    if (json.code === 200 && json.data) {
      Object.keys(config).forEach(k => {
        if (json.data[k] !== undefined) config[k] = Number(json.data[k])
      })
    }
  } catch { /* 加载失败用默认值 */ }
  finally { loading.value = false }
})

async function saveConfig() {
  loading.value = true
  try {
    const items = Object.entries(config).map(([k, v]) => ({
      configKey: k, configValue: String(v)
    }))
    const res = await fetch('/api/admin/config', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(items)
    })
    const json = await res.json()
    ElMessage[json.code === 200 ? 'success' : 'error'](json.message || json.data || '保存失败')
  } catch { ElMessage.error('保存失败') }
  finally { loading.value = false }
}
</script>

<style scoped>
.card-title { font-size: 15px; font-weight: 600; color: #303133; }
.form-tip { margin-left: 12px; font-size: 13px; color: #909399; }
</style>
