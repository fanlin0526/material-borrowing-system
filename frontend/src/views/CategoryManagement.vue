#齿轮下拉「分类管理」#
<template>
  <div>
    <el-card style="margin-bottom: 20px">
      <el-row style="align-items: center">
        <el-col :span="12">
          <h3 style="margin: 0">分类管理</h3>
        </el-col>
        <el-col :span="12" style="text-align: right">
          <el-button v-if="isSuperAdmin" type="primary" @click="showAddDialog">
            <el-icon style="margin-right: 4px"><Plus /></el-icon>新增分类
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card>
      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa', fontWeight: 600 }">
        <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
        <el-table-column prop="code" label="分类编码" width="150" />
        <el-table-column prop="name" label="分类名称" min-width="130" />
        <el-table-column label="可归还" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.returnable ? 'success' : 'info'" size="small" effect="light">
              {{ row.returnable ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <template v-if="isSuperAdmin">
              <el-button type="primary" size="small" @click="showEditDialog(row)">编辑</el-button>
              <el-popconfirm title="确定删除该分类？删除前请确保该分类下无物资。" @confirm="handleDelete(row.id)">
                <template #reference>
                  <el-button type="danger" size="small">删除</el-button>
                </template>
              </el-popconfirm>
            </template>
            <el-tag v-else type="info" size="small" effect="plain">仅查看</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑分类' : '新增分类'" width="480px">
      <el-form :model="dialog.form" label-width="80px">
        <el-form-item label="分类编码" required>
          <el-input v-model="dialog.form.code" placeholder="如：DEVICE、TOOL" />
        </el-form-item>
        <el-form-item label="分类名称" required>
          <el-input v-model="dialog.form.name" placeholder="如：实验设备、五金工具" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="dialog.form.description" type="textarea" :rows="2" placeholder="该分类的说明" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="dialog.form.sortOrder" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="可归还">
              <el-switch v-model="dialog.form.returnable" active-text="是" inactive-text="否" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="dialog.saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { categoryApi } from '../api'
import { authStore } from '../stores/auth'

// 分类是物资归类的基础口径，维护操作仅超级管理员可用（服务端同样有校验）
const isSuperAdmin = computed(() => authStore.isSuperAdmin)

const loading = ref(false)
const tableData = ref([])

const dialog = reactive({
  visible: false, isEdit: false, saving: false,
  form: { code: '', name: '', description: '', sortOrder: 0, returnable: false }
})

function loadData() {
  loading.value = true
  categoryApi.list().then(data => {
    tableData.value = data || []
  }).catch(e => ElMessage.error('加载失败：' + e.message))
    .finally(() => loading.value = false)
}

function showAddDialog() {
  dialog.isEdit = false
  dialog.visible = true
  dialog.form = { code: '', name: '', description: '', sortOrder: 0, returnable: false }
}

function showEditDialog(row) {
  dialog.isEdit = true
  dialog.visible = true
  dialog.form = { ...row }
}

function handleSave() {
  if (!dialog.form.code) { ElMessage.warning('请输入分类编码'); return }
  if (!dialog.form.name) { ElMessage.warning('请输入分类名称'); return }
  dialog.saving = true
  const api = dialog.isEdit
    ? categoryApi.update(dialog.form.id, dialog.form)
    : categoryApi.add(dialog.form)
  api.then(() => {
    ElMessage.success(dialog.isEdit ? '修改成功' : '新增成功')
    dialog.visible = false
    loadData()
  }).catch(e => ElMessage.error(e.message))
    .finally(() => dialog.saving = false)
}

function handleDelete(id) {
  categoryApi.delete(id).then(() => {
    ElMessage.success('删除成功')
    loadData()
  }).catch(e => ElMessage.error(e.message))
}

onMounted(loadData)
</script>
