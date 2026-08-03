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
        <el-col :span="7">
          <el-button type="primary" @click="loadData">
            <el-icon style="margin-right: 4px"><Search /></el-icon>查询
          </el-button>
          <el-button
            type="warning"
            :disabled="checkedIds.length === 0"
            @click="showBatchDialog"
          >
            <el-icon style="margin-right: 4px"><ShoppingCart /></el-icon>
            批量借用（{{ checkedIds.length }}）
          </el-button>
          <el-button v-if="isSuperAdmin" type="success" @click="showAddDialog">
            <el-icon style="margin-right: 4px"><Plus /></el-icon>新增物资
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 物资表格 -->
    <el-card>
      <el-table
        :data="tableData" stripe v-loading="loading" style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa', fontWeight: 600 }"
        @selection-change="handleSelectionChange"
        ref="tableRef"
      >
        <el-table-column type="selection" width="45" :selectable="checkSelectable" />
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
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button v-if="isBorrowable(row)" type="warning" size="small" :disabled="row.availableQuantity <= 0" @click="showBorrowDialog(row)">
              借用
            </el-button>
            <el-button v-else type="success" size="small" :disabled="row.availableQuantity <= 0 || row.deviceStatus !== 'AVAILABLE'" @click="goReserve(row)">
              预约
            </el-button>
            <template v-if="isSuperAdmin">
              <el-button type="primary" size="small" @click="showEditDialog(row)">编辑</el-button>
              <el-popconfirm title="确定删除该物资？" @confirm="handleDelete(row.id)">
                <template #reference>
                  <el-button type="danger" size="small">删除</el-button>
                </template>
              </el-popconfirm>
            </template>
            <el-tag v-else type="info" size="small" effect="plain" style="margin-left: 8px">
              {{ isAdmin ? '仅查看' : '仅查看' }}
            </el-tag>
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

    <!-- 借用弹窗 -->
    <el-dialog v-model="borrowDialog.visible" title="借用物资" width="480px">
      <el-form :model="borrowDialog.form" label-width="100px">
        <el-form-item label="物资名称">
          <el-input :model-value="borrowDialog.material?.name" disabled />
        </el-form-item>
        <el-form-item label="物资类别">
          <el-tag :type="categoryTag(borrowDialog.material?.category)" effect="light">
            {{ categoryLabel(borrowDialog.material?.category) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="可借数量">
          <el-tag type="success" size="large">{{ borrowDialog.material?.availableQuantity }} {{ borrowDialog.material?.unit }}</el-tag>
        </el-form-item>
        <el-form-item label="借用人" required>
          <el-input v-model="borrowDialog.form.borrower" placeholder="请输入借用人姓名" />
        </el-form-item>
        <el-form-item label="借用数量" required>
          <el-input-number v-model="borrowDialog.form.quantity" :min="1" :max="borrowDialog.material?.availableQuantity" style="width: 100%" />
        </el-form-item>
        <el-form-item label="用途说明">
          <el-input v-model="borrowDialog.form.purpose" type="textarea" :rows="2" placeholder="实验项目名称或用途" />
        </el-form-item>
        <el-form-item label="预计归还">
          <el-date-picker v-model="borrowDialog.form.expectedReturnTime" type="datetime" placeholder="选填" format="YYYY-MM-DD HH:mm" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="borrowDialog.visible = false">取消</el-button>
        <el-button type="warning" @click="handleBorrow" :loading="borrowDialog.saving">确认借用</el-button>
      </template>
    </el-dialog>

    <!-- 批量借用弹窗 -->
    <el-dialog v-model="batchDialog.visible" title="批量借用物资" width="780px" :close-on-click-modal="false">
      <!-- 统一填写区 -->
      <el-card shadow="never" style="margin-bottom: 16px; background: #f5f7fa">
        <el-row :gutter="16" style="align-items: center">
          <el-col :span="8">
            <span style="color: #606266; font-size: 14px; margin-right: 8px">借用人：</span>
            <el-input
              v-model="batchDialog.borrower"
              placeholder="请输入借用人姓名"
              style="width: 160px"
              @input="syncBorrower"
            />
          </el-col>
          <el-col :span="8">
            <span style="color: #606266; font-size: 14px; margin-right: 8px">用途：</span>
            <el-input
              v-model="batchDialog.purpose"
              placeholder="统一用途（可单独改）"
              style="width: 160px"
              @input="syncPurpose"
            />
          </el-col>
          <el-col :span="8">
            <span style="color: #606266; font-size: 14px; margin-right: 8px">预计归还：</span>
            <el-date-picker
              v-model="batchDialog.expectedReturnTime"
              type="datetime"
              placeholder="选填"
              format="YYYY-MM-DD HH:mm"
              style="width: 180px"
              @change="syncReturnTime"
            />
          </el-col>
        </el-row>
      </el-card>

      <!-- 已选物资列表 -->
      <el-table :data="batchDialog.items" stripe max-height="360" style="width: 100%">
        <el-table-column prop="name" label="物资名称" min-width="130" />
        <el-table-column label="类别" width="90">
          <template #default="{ row }">
            <el-tag :type="categoryTag(row.category)" size="small" effect="light">
              {{ categoryLabel(row.category) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="可借" width="80" align="center">
          <template #default="{ row }">
            <span :style="{ color: row.availableQuantity <= 0 ? '#F56C6C' : '#67C23A', fontWeight: 'bold' }">
              {{ row.availableQuantity }}
            </span>
            <span style="color: #909399"> / {{ row.unit }}</span>
          </template>
        </el-table-column>
        <el-table-column label="借用数量" width="130" align="center">
          <template #default="{ row }">
            <el-input-number
              v-model="row.quantity"
              :min="1"
              :max="row.availableQuantity"
              size="small"
              controls-position="right"
              style="width: 100px"
            />
          </template>
        </el-table-column>
        <el-table-column label="用途说明" min-width="150">
          <template #default="{ row }">
            <el-input v-model="row.purpose" size="small" placeholder="单独填写用途" />
          </template>
        </el-table-column>
        <el-table-column width="50" align="center">
          <template #default="{ $index }">
            <el-button type="danger" size="small" :icon="Delete" circle text @click="removeBatchItem($index)" />
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 16px; text-align: center; color: #909399; font-size: 13px">
        共 <b style="color: #409EFF">{{ batchDialog.items.length }}</b> 种物资，
        合计借用 <b style="color: #E6A23C">{{ totalBatchQty }}</b> 件
      </div>

      <template #footer>
        <el-button @click="batchDialog.visible = false">取消</el-button>
        <el-button type="warning" @click="handleBatchBorrow" :loading="batchDialog.saving">
          确认全部借用
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus, ShoppingCart, Delete } from '@element-plus/icons-vue'
import { materialApi, borrowApi, categoryApi } from '../api'
import { useRouter } from 'vue-router'
const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const tableRef = ref()
const search = reactive({ keyword: '', category: '' })
const filterBorrowable = ref(false)
const checkedIds = ref([])
const checkedItems = ref([])
const categories = ref([])

// 当前登录用户
const currentUser = computed(() => {
  try { return JSON.parse(localStorage.getItem('user') || '{}') } catch { return {} }
})
const isAdmin = computed(() => currentUser.value.role === 'SUPER_ADMIN' || currentUser.value.role === 'TEACHER')
const isSuperAdmin = computed(() => currentUser.value.role === 'SUPER_ADMIN')

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

const batchDialog = reactive({
  visible: false, saving: false,
  borrower: '', purpose: '', expectedReturnTime: null,
  items: []
})

const totalBatchQty = computed(() => {
  return batchDialog.items.reduce((sum, item) => sum + (item.quantity || 0), 0)
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
function checkSelectable(row) {
  return row.availableQuantity > 0
}
// 可归还型（设备/玻璃器皿）不可直接借用，需走预约；消耗型可直接借用
function isBorrowable(row) {
  const cat = categories.value.find(c => c.code === row.category)
  return cat ? !cat.returnable : true
}
// 跳转到预约管理页并预选设备
function goReserve(row) {
  router.push({ path: '/reservation', query: { materialId: row.id, materialName: row.name } })
}

function handleSelectionChange(selection) {
  checkedItems.value = selection
  checkedIds.value = selection.map(s => s.id)
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
  if (!borrowDialog.form.borrower) { ElMessage.warning('请输入借用人'); return }
  if (borrowDialog.form.quantity < 1) { ElMessage.warning('借用数量至少为1'); return }

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
    ElMessage.success('申请已提交，等待审批')
    borrowDialog.visible = false
    loadData()
  }).catch(e => ElMessage.error(e.message))
    .finally(() => borrowDialog.saving = false)
}

// ==== 批量借用 ====
function showBatchDialog() {
  if (checkedItems.value.length === 0) {
    ElMessage.warning('请先勾选需要借用的物资')
    return
  }
  batchDialog.visible = true
  batchDialog.borrower = currentUser.value.realName || ''
  batchDialog.purpose = ''
  batchDialog.expectedReturnTime = null
  batchDialog.items = checkedItems.value.map(item => ({
    ...item,
    quantity: 1,
    purpose: ''
  }))
}

function syncBorrower() {
  // 借用人统一填写后不需要同步到每行（借用人统一即可）
}

function syncPurpose() {
  batchDialog.items.forEach(item => {
    item.purpose = batchDialog.purpose
  })
}

function syncReturnTime() {
  // 预计归还时间在提交时统一处理
}

function removeBatchItem(index) {
  batchDialog.items.splice(index, 1)
  if (batchDialog.items.length === 0) {
    batchDialog.visible = false
    ElMessage.info('已清空批量借用列表')
  }
}

function handleBatchBorrow() {
  if (!batchDialog.borrower) { ElMessage.warning('请输入借用人姓名'); return }
  if (batchDialog.items.length === 0) { ElMessage.warning('借用列表为空'); return }

  // 校验每个物资
  for (const item of batchDialog.items) {
    if (!item.quantity || item.quantity < 1) {
      ElMessage.warning('「' + item.name + '」借用数量至少为1')
      return
    }
    if (item.quantity > item.availableQuantity) {
      ElMessage.warning('「' + item.name + '」库存不足，最多可借 ' + item.availableQuantity)
      return
    }
  }

  // 构建请求
  const requests = batchDialog.items.map(item => ({
    materialId: item.id,
    materialName: item.name,
    borrower: batchDialog.borrower,
    quantity: item.quantity,
    purpose: item.purpose || batchDialog.purpose,
    expectedReturnTime: batchDialog.expectedReturnTime
      ? new Date(batchDialog.expectedReturnTime).toISOString().replace('Z', '')
      : null
  }))

  batchDialog.saving = true
  borrowApi.batchBorrow(requests).then(() => {
    ElMessage.success(`批量借用成功，共 ${requests.length} 种物资`)
    batchDialog.visible = false
    // 清空选中
    if (tableRef.value) tableRef.value.clearSelection()
    checkedIds.value = []
    checkedItems.value = []
    loadData()
  }).catch(e => ElMessage.error(e.message))
    .finally(() => batchDialog.saving = false)
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
