<template>
  <div>
    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom: 20px">
      <el-col :span="4" v-for="(item, i) in categoryCards" :key="i">
        <el-card shadow="hover" class="cat-card" :body-style="{ padding: '16px' }" @click="filterByCategory(item.key)">
          <div class="cat-icon" :style="{ background: item.bg }">{{ item.icon }}</div>
          <div class="cat-info">
            <div class="cat-num" :style="{ color: item.color }">{{ item.count }}</div>
            <div class="cat-label">{{ item.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索/操作栏 -->
    <el-card style="margin-bottom: 20px">
      <el-row :gutter="16" style="align-items: center">
        <el-col :span="5">
          <el-input v-model="search.keyword" placeholder="搜索物资名称/编号" clearable @clear="loadData" @keyup.enter="loadData">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="search.category" placeholder="全部类别" clearable @change="loadData" style="width: 100%">
            <el-option v-for="cat in categories" :key="cat.code" :label="cat.name" :value="cat.code" />
          </el-select>
        </el-col>
        <el-col :span="3">
          <el-switch
            v-model="filterBorrowable"
            active-text="仅可借"
            inactive-text="全部"
            @change="loadData"
          />
        </el-col>
        <el-col :span="10">
          <el-button type="primary" @click="loadData">
            <el-icon style="margin-right: 4px"><Search /></el-icon>查询
          </el-button>
          <el-button type="success" plain @click="handleExport">
            <el-icon style="margin-right: 4px"><Download /></el-icon>导出Excel
          </el-button>
          <el-button v-if="isSuperAdmin" type="warning" plain @click="triggerImport">
            <el-icon style="margin-right: 4px"><Upload /></el-icon>导入Excel
          </el-button>
          <el-button v-if="isSuperAdmin" type="success" @click="showAddDialog">
            <el-icon style="margin-right: 4px"><Plus /></el-icon>新增物资
          </el-button>
          <input ref="fileInput" type="file" accept=".xlsx,.xls" style="display: none" @change="handleImport" />
        </el-col>
      </el-row>
    </el-card>

    <!-- 物资表格 -->
    <el-card>
      <el-table
        :data="tableData" stripe v-loading="loading" style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa', fontWeight: 600 }"
      >
        <el-table-column prop="code" label="编号" width="100" />
        <el-table-column prop="name" label="物资名称" min-width="130" />
        <el-table-column label="类别" width="100">
          <template #default="{ row }">
            <el-tag :type="categoryTag(row.category)" size="small" effect="light">
              {{ categoryLabel(row.category) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="specification" label="规格型号" min-width="130" show-overflow-tooltip />
        <el-table-column prop="unit" label="单位" width="60" align="center" />
        <el-table-column prop="totalQuantity" label="总量" width="70" align="center" />
        <el-table-column label="可借数量" width="90" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.availableQuantity === 0 ? 'danger' : (isLowStock(row) ? 'warning' : 'success')"
              size="small" effect="dark"
            >
              {{ row.availableQuantity }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="存放位置" width="130" show-overflow-tooltip />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button v-if="isBorrowable(row)" type="warning" size="small" :disabled="row.availableQuantity <= 0" @click="showBorrowDialog(row)">
              {{ isConsumable(row) ? '领用' : '借用' }}
            </el-button>
            <el-button v-else type="success" size="small" :disabled="row.availableQuantity <= 0 || row.deviceStatus !== 'AVAILABLE'" @click="goReserve(row)">
              预约
            </el-button>
            <el-button type="info" size="small" plain @click="showLogs(row)">流水</el-button>
            <template v-if="isSuperAdmin">
              <el-button type="primary" size="small" @click="showEditDialog(row)">编辑</el-button>
              <el-popconfirm title="确定删除该物资？" @confirm="handleDelete(row.id)">
                <template #reference>
                  <el-button type="danger" size="small">删除</el-button>
                </template>
              </el-popconfirm>
            </template>
            <el-tag v-else type="info" size="small" effect="plain" style="margin-left: 8px">仅查看</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑物资' : '新增物资'" width="600px">
      <el-form :model="dialog.form" label-width="100px">
        <el-form-item label="物资名称" required>
          <el-input v-model="dialog.form.name" placeholder="如：数字示波器、U盘、去离子水" />
        </el-form-item>
        <el-form-item label="物资类别" required>
          <el-select v-model="dialog.form.category" placeholder="请选择类别" style="width: 100%">
            <el-option v-for="cat in categories" :key="cat.code" :label="cat.name" :value="cat.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="规格型号">
          <el-input v-model="dialog.form.specification" placeholder="如：DS1054Z 50MHz" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="单位">
              <el-input v-model="dialog.form.unit" placeholder="台/个/卷/升" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="总数量" required>
              <el-input-number v-model="dialog.form.totalQuantity" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="预警阈值">
              <el-input-number v-model="dialog.form.warningThreshold" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="存放位置">
          <el-input v-model="dialog.form.location" placeholder="如：A102实验室" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="dialog.form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="dialog.saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 领用/借用弹窗 -->
    <el-dialog v-model="borrowDialog.visible" :title="isConsumable(borrowDialog.material) ? '领用物资' : '借用物资'" width="480px">
      <el-form :model="borrowDialog.form" label-width="100px">
        <el-form-item label="物资名称">
          <el-input :model-value="borrowDialog.material?.name" disabled />
        </el-form-item>
        <el-form-item label="物资类别">
          <el-tag :type="categoryTag(borrowDialog.material?.category)" effect="light">
            {{ categoryLabel(borrowDialog.material?.category) }}
          </el-tag>
        </el-form-item>
        <el-form-item :label="isConsumable(borrowDialog.material) ? '可领数量' : '可借数量'">
          <el-tag type="success" size="large">{{ borrowDialog.material?.availableQuantity }} {{ borrowDialog.material?.unit }}</el-tag>
        </el-form-item>
        <el-form-item :label="isConsumable(borrowDialog.material) ? '领用人' : '借用人'" required>
          <el-input v-model="borrowDialog.form.borrower" :placeholder="'请输入' + (isConsumable(borrowDialog.material) ? '领用人' : '借用人') + '姓名'" />
        </el-form-item>
        <el-form-item :label="isConsumable(borrowDialog.material) ? '领用数量' : '借用数量'" required>
          <el-input-number v-model="borrowDialog.form.quantity" :min="1" :max="borrowDialog.material?.availableQuantity" style="width: 100%" />
        </el-form-item>
        <el-form-item label="用途说明">
          <el-input v-model="borrowDialog.form.purpose" type="textarea" :rows="2" placeholder="实验项目名称或用途" />
        </el-form-item>
        <el-form-item v-if="!isConsumable(borrowDialog.material)" label="预计归还">
          <el-date-picker v-model="borrowDialog.form.expectedReturnTime" type="datetime" placeholder="选填" format="YYYY-MM-DD HH:mm" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="borrowDialog.visible = false">取消</el-button>
        <el-button type="warning" @click="handleBorrow" :loading="borrowDialog.saving">
          {{ isConsumable(borrowDialog.material) ? '确认领用' : '确认借用' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 库存流水弹窗 -->
    <el-dialog v-model="logDialog.visible" :title="'库存流水 — ' + (logDialog.material?.name || '')" width="760px">
      <el-table :data="logDialog.list" stripe v-loading="logDialog.loading" max-height="420" style="width: 100%">
        <el-table-column label="变动类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="logTypeTag(row.changeType)" size="small" effect="dark">{{ logTypeLabel(row.changeType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="变动数量" width="90" align="center">
          <template #default="{ row }">
            <span :style="{ color: row.changeQuantity > 0 ? '#67C23A' : '#F56C6C', fontWeight: 'bold' }">
              {{ row.changeQuantity > 0 ? '+' : '' }}{{ row.changeQuantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="变动前" prop="beforeQuantity" width="70" align="center" />
        <el-table-column label="变动后" prop="afterQuantity" width="70" align="center" />
        <el-table-column prop="operator" label="操作人" width="90" />
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column label="时间" width="150">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
      </el-table>
      <div style="display: flex; justify-content: flex-end; margin-top: 12px">
        <el-pagination
          v-model:current-page="logDialog.page"
          :page-size="logDialog.size"
          :total="logDialog.total"
          layout="total, prev, pager, next"
          @current-change="loadLogs"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Download, Upload } from '@element-plus/icons-vue'
import { materialApi, borrowApi, categoryApi, excelApi } from '../api'
import { authStore } from '../stores/auth'
import { useRouter } from 'vue-router'
const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const fileInput = ref()
const search = reactive({ keyword: '', category: '' })
const filterBorrowable = ref(false)
const categories = ref([])

// 当前登录用户 — 以 authStore 为准
const currentUser = computed(() => authStore.userInfo || {})
const isAdmin = computed(() => authStore.isAdmin)
const isSuperAdmin = computed(() => authStore.isSuperAdmin)

// 颜色调色板
const catColors = ['#409EFF','#67C23A','#00C9A7','#E6A23C','#9B59B6','#F56C6C','#2196F3','#FF9800','#795548','#607D8B']
const catBgs = ['#ecf5ff','#f0f9eb','#e8f8f5','#fdf6ec','#f5f0ff','#fef0f0','#e3f2fd','#fff3e0','#efebe9','#eceff1']
const catIcons = ['🔬','📦','💧','⚗️','🧪','🔧','📡','🛠️','📋','📊']

const categoryCards = computed(() => {
  const cards = categories.value.map((cat, i) => ({
    label: cat.name,
    key: cat.code,
    count: 0,
    icon: catIcons[i % catIcons.length],
    color: catColors[i % catColors.length],
    bg: catBgs[i % catBgs.length],
  }))
  cards.push({ label: '借用中', key: '', count: 0, icon: '🔄', color: '#F56C6C', bg: '#fef0f0' })
  return cards
})

const dialog = reactive({
  visible: false, isEdit: false, saving: false,
  form: { name: '', category: 'DEVICE', specification: '', unit: '', totalQuantity: 1, warningThreshold: 3, location: '', remark: '' }
})

const borrowDialog = reactive({
  visible: false, saving: false,
  material: null,
  form: { borrower: '', quantity: 1, purpose: '', expectedReturnTime: null }
})

// 库存流水弹窗
const logDialog = reactive({
  visible: false, loading: false,
  material: null, list: [], page: 1, size: 8, total: 0
})

function categoryLabel(catCode) {
  const cat = categories.value.find(c => c.code === catCode)
  return cat ? cat.name : catCode
}
function categoryTag(catCode) {
  // 根据分类索引返回 tag 样式
  const idx = categories.value.findIndex(c => c.code === catCode)
  const types = ['', 'success', 'info', 'warning', 'danger']
  return idx >= 0 && idx < types.length ? types[idx] : ''
}
function isLowStock(row) {
  const threshold = row.warningThreshold || 3
  return row.availableQuantity <= threshold
}
// 消耗型物资（耗材/水/试剂）：领用后直接核销，无需归还
function isConsumable(row) {
  const cat = categories.value.find(c => c.code === row.category)
  return cat ? !cat.returnable : true
}
// 可归还型（设备/玻璃器皿）不可直接借用，需走预约；消耗型可直接领用
function isBorrowable(row) {
  const cat = categories.value.find(c => c.code === row.category)
  return cat ? !cat.returnable : true
}
// 跳转到预约管理页并预选设备
function goReserve(row) {
  router.push({ path: '/reservation', query: { materialId: row.id, materialName: row.name } })
}

function loadData() {
  loading.value = true
  const params = { ...search }
  Promise.all([
    materialApi.list(params),
    materialApi.stats(),
    categoryApi.list()
  ]).then(([data, s, cats]) => {
    categories.value = cats || []
    // 仅可借筛选在前端过滤
    if (filterBorrowable.value) {
      tableData.value = data.filter(d => d.availableQuantity > 0)
    } else {
      tableData.value = data
    }
    // 填充分类卡片计数
    if (s.categories) {
      const cards = categoryCards.value
      for (let i = 0; i < cards.length - 1; i++) {
        const card = cards[i]
        const info = s.categories[card.key]
        card.count = info ? info.count : 0
      }
      cards[cards.length - 1].count = s.borrowedCount
    }
  }).catch(e => ElMessage.error('加载失败：' + e.message))
    .finally(() => loading.value = false)
}

function filterByCategory(key) {
  search.category = key
  loadData()
}

// ==== 单项借用 ====
function showBorrowDialog(row) {
  borrowDialog.material = row
  borrowDialog.form = {
    borrower: currentUser.value.realName || '',
    quantity: 1,
    purpose: '',
    expectedReturnTime: null
  }
  borrowDialog.visible = true
}

function handleBorrow() {
  const isConsume = isConsumable(borrowDialog.material)
  if (!borrowDialog.form.borrower) { ElMessage.warning(isConsume ? '请输入领用人' : '请输入借用人'); return }
  if (borrowDialog.form.quantity < 1) { ElMessage.warning('数量至少为1'); return }

  borrowDialog.saving = true
  borrowApi.borrow({
    materialId: borrowDialog.material.id,
    borrower: borrowDialog.form.borrower,
    quantity: borrowDialog.form.quantity,
    purpose: borrowDialog.form.purpose,
    expectedReturnTime: borrowDialog.form.expectedReturnTime
      ? new Date(borrowDialog.form.expectedReturnTime).toISOString().replace('Z', '')
      : null
  }).then(() => {
    ElMessage.success(isConsume ? '领用申请已提交，等待审批' : '借用申请已提交，等待审批')
    borrowDialog.visible = false
    loadData()
  }).catch(e => ElMessage.error(e.message))
    .finally(() => borrowDialog.saving = false)
}

// ==== 库存流水 ====
function logTypeLabel(t) {
  const map = { IN: '入库', OUT_BORROW: '借出', RETURN: '归还', CONSUME: '领用', ADJUST: '调整' }
  return map[t] || t
}
function logTypeTag(t) {
  const map = { IN: 'success', OUT_BORROW: 'warning', RETURN: 'success', CONSUME: 'info', ADJUST: 'danger' }
  return map[t] || 'info'
}
function formatTime(t) {
  if (!t) return '-'
  const d = new Date(t)
  const p = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
}
function showLogs(row) {
  logDialog.material = row
  logDialog.page = 1
  logDialog.list = []
  logDialog.total = 0
  logDialog.visible = true
  loadLogs()
}
function loadLogs() {
  if (!logDialog.material) return
  logDialog.loading = true
  materialApi.logs(logDialog.material.id, { page: logDialog.page, size: logDialog.size })
    .then(data => {
      logDialog.list = data.content || []
      logDialog.total = data.totalElements || 0
    })
    .catch(e => ElMessage.error('流水加载失败：' + e.message))
    .finally(() => logDialog.loading = false)
}

// ==== Excel 导入导出 ====
function handleExport() {
  excelApi.exportMaterials({ keyword: search.keyword, category: search.category })
    .then(() => ElMessage.success('导出成功'))
    .catch(e => ElMessage.error(e.message))
}
function triggerImport() {
  fileInput.value.click()
}
function handleImport(e) {
  const file = e.target.files && e.target.files[0]
  if (!file) return
  excelApi.importMaterials(file)
    .then(msg => {
      // 后端返回 "导入完成：成功 N 条，失败 M 条\n第X行：..."
      ElMessageBox.alert(msg, '导入结果', { confirmButtonText: '知道了' })
        .then(() => loadData())
        .catch(() => {})
    })
    .catch(err => ElMessage.error(err.message))
    .finally(() => { e.target.value = '' })
}

// ==== CRUD ====
function showAddDialog() {
  dialog.isEdit = false
  dialog.visible = true
  const defaultCat = categories.value.length > 0 ? categories.value[0].code : ''
  dialog.form = { name: '', category: defaultCat, specification: '', unit: '', totalQuantity: 1, warningThreshold: 3, location: '', remark: '' }
}

function showEditDialog(row) {
  dialog.isEdit = true
  dialog.visible = true
  dialog.form = { ...row }
}

function handleSave() {
  if (!dialog.form.name) { ElMessage.warning('请输入物资名称'); return }
  dialog.saving = true
  const api = dialog.isEdit
    ? materialApi.update(dialog.form.id, dialog.form)
    : materialApi.add(dialog.form)
  api.then(() => {
    ElMessage.success(dialog.isEdit ? '修改成功' : '新增成功')
    dialog.visible = false
    loadData()
  }).catch(e => ElMessage.error(e.message))
    .finally(() => dialog.saving = false)
}

function handleDelete(id) {
  materialApi.delete(id).then(() => {
    ElMessage.success('删除成功')
    loadData()
  }).catch(e => ElMessage.error(e.message))
}

onMounted(loadData)
</script>

<style scoped>
.cat-card { cursor: pointer; transition: transform 0.2s; border-radius: 10px; }
.cat-card:hover { transform: translateY(-3px); }
.cat-card .cat-icon {
  width: 44px; height: 44px;
  border-radius: 10px;
  display: inline-flex; align-items: center; justify-content: center;
  font-size: 22px;
  vertical-align: middle;
}
.cat-card .cat-info { display: inline-block; vertical-align: middle; margin-left: 10px; }
.cat-num { font-size: 22px; font-weight: 700; line-height: 1.2; }
.cat-label { font-size: 12px; color: #909399; }
</style>
