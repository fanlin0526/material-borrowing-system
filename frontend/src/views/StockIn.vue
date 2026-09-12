<template>
  <div>
    <!-- 操作栏 -->
    <el-card style="margin-bottom: 20px">
      <el-row :gutter="16" style="align-items: center">
        <el-col :span="6">
          <el-input v-model="search.keyword" placeholder="搜索单号/供应商/经办人" clearable @clear="loadData" @keyup.enter="loadData">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </el-col>
        <el-col :span="10">
          <el-button type="primary" @click="loadData">
            <el-icon style="margin-right: 4px"><Search /></el-icon>查询
          </el-button>
          <el-button type="success" @click="showCreateDialog">
            <el-icon style="margin-right: 4px"><Plus /></el-icon>新建入库单
          </el-button>
        </el-col>
        <el-col :span="8" style="text-align: right; color: #909399; font-size: 13px">
          入库后自动增加库存并记录流水
        </el-col>
      </el-row>
    </el-card>

    <!-- 入库单列表 -->
    <el-card>
      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa', fontWeight: 600 }">
        <el-table-column prop="inNo" label="入库单号" width="190" />
        <el-table-column prop="supplier" label="供应商" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.supplier || '-' }}</template>
        </el-table-column>
        <el-table-column prop="operator" label="经办人" width="100" />
        <el-table-column prop="totalQuantity" label="入库数量" width="90" align="center" />
        <el-table-column label="总金额" width="110" align="center">
          <template #default="{ row }">¥{{ row.totalAmount ? row.totalAmount.toFixed(2) : '0.00' }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.remark || '-' }}</template>
        </el-table-column>
        <el-table-column label="入库时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" plain @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="display: flex; justify-content: flex-end; margin-top: 16px">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next, jumper"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 新建入库单弹窗 -->
    <el-dialog v-model="createDialog.visible" title="新建采购入库单" width="900px" :close-on-click-modal="false">
      <el-form label-width="80px">
        <el-row :gutter="20">
          <el-col :span="10">
            <el-form-item label="供应商" required>
              <el-input v-model="createDialog.supplier" placeholder="如：某某试剂公司 / 校内仓库" />
            </el-form-item>
          </el-col>
          <el-col :span="14">
            <el-form-item label="备注">
              <el-input v-model="createDialog.remark" placeholder="选填" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <!-- 明细列表 -->
      <el-card shadow="never" style="background: #f5f7fa; margin-bottom: 12px">
        <div style="font-weight: 600; margin-bottom: 8px; font-size: 14px">
          物资明细（下拉选择已有物资，或直接输入新物资名称）
        </div>
        <div v-for="(item, index) in createDialog.items" :key="index" style="margin-bottom: 8px">
          <el-row :gutter="8" style="align-items: center">
            <el-col :span="7">
              <el-select
                v-model="item.materialId"
                filterable allow-create default-first-option
                placeholder="选择或输入物资名称"
                style="width: 100%"
                @change="onItemSelect(index)"
              >
                <el-option v-for="m in allMaterials" :key="m.id" :label="m.name + (m.specification ? '（' + m.specification + '）' : '')" :value="m.id" />
              </el-select>
            </el-col>
            <el-col :span="4">
              <el-select v-if="isNewItem(item)" v-model="item.category" placeholder="类别" style="width: 100%">
                <el-option v-for="cat in categories" :key="cat.code" :label="cat.name" :value="cat.code" />
              </el-select>
              <el-tag v-else style="width: 100%; text-align: center">{{ categoryLabel(item.category) }}</el-tag>
            </el-col>
            <el-col :span="4">
              <el-input v-model="item.specification" placeholder="规格型号" :disabled="!isNewItem(item)" />
            </el-col>
            <el-col :span="2">
              <el-input v-model="item.unit" placeholder="单位" :disabled="!isNewItem(item)" />
            </el-col>
            <el-col :span="2">
              <el-input-number v-model="item.quantity" :min="1" :max="99999" style="width: 100%" controls-position="right" />
            </el-col>
            <el-col :span="3">
              <el-input-number v-model="item.unitPrice" :min="0" :precision="2" style="width: 100%" controls-position="right" placeholder="单价" />
            </el-col>
            <el-col :span="1" style="text-align: center">
              <el-button type="danger" :icon="Delete" circle text @click="removeItem(index)" />
            </el-col>
          </el-row>
        </div>
        <el-button type="primary" plain size="small" @click="addItem">
          <el-icon style="margin-right: 4px"><Plus /></el-icon>添加一行
        </el-button>
      </el-card>

      <template #footer>
        <el-button @click="createDialog.visible = false">取消</el-button>
        <el-button type="success" @click="handleCreate" :loading="createDialog.saving">提交入库</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialog.visible" :title="'入库单详情 — ' + (detailDialog.order?.inNo || '')" width="720px">
      <el-descriptions v-if="detailDialog.order" :column="3" border size="small" style="margin-bottom: 16px">
        <el-descriptions-item label="供应商">{{ detailDialog.order.supplier || '-' }}</el-descriptions-item>
        <el-descriptions-item label="经办人">{{ detailDialog.order.operator }}</el-descriptions-item>
        <el-descriptions-item label="入库时间">{{ formatTime(detailDialog.order.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="入库数量">{{ detailDialog.order.totalQuantity }}</el-descriptions-item>
        <el-descriptions-item label="总金额">¥{{ detailDialog.order.totalAmount ? detailDialog.order.totalAmount.toFixed(2) : '0.00' }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ detailDialog.order.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="detailDialog.items" stripe size="small" style="width: 100%">
        <el-table-column prop="materialName" label="物资名称" min-width="130" />
        <el-table-column label="类别" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ categoryLabel(row.category) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="specification" label="规格型号" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.specification || '-' }}</template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="60" align="center" />
        <el-table-column prop="quantity" label="数量" width="70" align="center" />
        <el-table-column label="单价" width="90" align="center">
          <template #default="{ row }">¥{{ row.unitPrice ? row.unitPrice.toFixed(2) : '0.00' }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus, Delete } from '@element-plus/icons-vue'
import { stockInApi, materialApi, categoryApi } from '../api'

const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const search = reactive({ keyword: '' })
const categories = ref([])
const allMaterials = ref([])

const createDialog = reactive({
  visible: false, saving: false,
  supplier: '', remark: '',
  items: []
})
const detailDialog = reactive({
  visible: false, order: null, items: []
})

function categoryLabel(code) {
  const cat = categories.value.find(c => c.code === code)
  return cat ? cat.name : code
}
function isNewItem(item) {
  return typeof item.materialId !== 'number'
}
function formatTime(t) {
  if (!t) return '-'
  const d = new Date(t)
  const p = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

function loadData() {
  loading.value = true
  stockInApi.list({ keyword: search.keyword, page: currentPage.value, size: pageSize.value })
    .then(data => {
      tableData.value = data.content || []
      total.value = data.totalElements || 0
    })
    .catch(e => ElMessage.error('加载失败：' + e.message))
    .finally(() => loading.value = false)
}

// ==== 新建入库单 ====
function newItem() {
  return { materialId: null, materialName: '', category: '', specification: '', unit: '', quantity: 1, unitPrice: null }
}
function showCreateDialog() {
  createDialog.supplier = ''
  createDialog.remark = ''
  createDialog.items = [newItem()]
  createDialog.visible = true
  if (allMaterials.value.length === 0) {
    materialApi.list().then(data => { allMaterials.value = data || [] }).catch(() => {})
  }
  if (categories.value.length === 0) {
    categoryApi.list().then(data => { categories.value = data || [] }).catch(() => {})
  }
}
function addItem() {
  createDialog.items.push(newItem())
}
function removeItem(index) {
  if (createDialog.items.length <= 1) {
    ElMessage.warning('至少保留一行明细')
    return
  }
  createDialog.items.splice(index, 1)
}
function onItemSelect(index) {
  const item = createDialog.items[index]
  if (typeof item.materialId === 'number') {
    const m = allMaterials.value.find(x => x.id === item.materialId)
    if (m) {
      item.materialName = m.name
      item.category = m.category
      item.specification = m.specification
      item.unit = m.unit
    }
  } else {
    // 用户输入了新名称：作为新物资建档
    item.materialName = item.materialId
    item.materialId = null
    item.specification = ''
    item.unit = ''
  }
}
function handleCreate() {
  if (!createDialog.supplier.trim()) { ElMessage.warning('请输入供应商'); return }
  if (createDialog.items.length === 0) { ElMessage.warning('请至少添加一行物资明细'); return }
  for (let i = 0; i < createDialog.items.length; i++) {
    const it = createDialog.items[i]
    const name = typeof it.materialId === 'number' ? it.materialName : it.materialId
    if (!name || !String(name).trim()) {
      ElMessage.warning(`第 ${i + 1} 行：物资名称不能为空`); return
    }
    if (!it.quantity || it.quantity < 1) {
      ElMessage.warning(`第 ${i + 1} 行：数量必须大于0`); return
    }
    if (typeof it.materialId !== 'number' && !it.category) {
      ElMessage.warning(`第 ${i + 1} 行：「${name}」是新物资，请选择物资类别`); return
    }
  }

  createDialog.saving = true
  const payload = {
    supplier: createDialog.supplier.trim(),
    remark: createDialog.remark,
    items: createDialog.items.map(it => {
      const isNew = typeof it.materialId !== 'number'
      return {
        materialId: isNew ? null : it.materialId,
        materialName: isNew ? String(it.materialId).trim() : it.materialName,
        category: isNew ? it.category : it.category,
        specification: it.specification,
        unit: it.unit,
        quantity: it.quantity,
        unitPrice: it.unitPrice
      }
    })
  }
  stockInApi.create(payload)
    .then(order => {
      ElMessage.success(`入库成功（单号 ${order.inNo}）`)
      createDialog.visible = false
      loadData()
    })
    .catch(e => ElMessage.error(e.message))
    .finally(() => createDialog.saving = false)
}

// ==== 详情 ====
function showDetail(row) {
  detailDialog.order = null
  detailDialog.items = []
  detailDialog.visible = true
  stockInApi.detail(row.id)
    .then(data => {
      detailDialog.order = data.order
      detailDialog.items = data.items || []
    })
    .catch(e => ElMessage.error('加载失败：' + e.message))
}

onMounted(loadData)
</script>
