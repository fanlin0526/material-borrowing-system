#顶部 Tab「物资管理」#
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

    <!-- 搜索/操作栏：两行自适应布局，避免窄屏按钮互相遮挡 -->
    <el-card class="material-toolbar" style="margin-bottom: 20px">
      <div class="filter-bar">
        <div class="filter-search">
          <el-input v-model="search.keyword" placeholder="名称/编号（可用扫码枪直接扫编号）" clearable @clear="refine" @keyup.enter="refine">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
        <el-select v-model="search.category" placeholder="全部类别" clearable @change="refine" class="category-filter">
          <el-option v-for="cat in categories" :key="cat.code" :label="cat.name" :value="cat.code" />
        </el-select>
        <div class="filter-switches">
          <el-switch v-model="filterBorrowable" active-text="仅可借" inactive-text="全部" @change="refine" />
          <el-switch v-model="filterLowStock" active-text="仅看预警" @change="handleLowStockToggle" />
        </div>
      </div>

      <div class="action-bar">
        <el-button type="primary" @click="loadData">
          <el-icon><Search /></el-icon>查询
        </el-button>
        <el-button type="warning" plain :disabled="selectedRows.length === 0" @click="showBatchBorrowDialog">
          批量借用/领用（{{ selectedRows.length }}）
        </el-button>
        <el-button v-if="isAdmin" type="success" plain @click="handleExport">
          <el-icon><Download /></el-icon>导出Excel
        </el-button>
        <el-dropdown v-if="isSuperAdmin" trigger="click" @command="handleExcelCommand">
          <el-button type="warning" plain>
            <el-icon><Upload /></el-icon>Excel批量建档
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="help">查看导入说明</el-dropdown-item>
              <el-dropdown-item command="template">下载导入模板</el-dropdown-item>
              <el-dropdown-item command="import" divided>导入Excel</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button v-if="isSuperAdmin" type="success" @click="showAddDialog">
          <el-icon><Plus /></el-icon>新增物资
        </el-button>
        <el-button v-if="isAdmin" type="info" plain @click="showStockCheckDialog">
          <el-icon><Checked /></el-icon>库存盘点
        </el-button>
        <input ref="fileInput" type="file" accept=".xlsx,.xls" style="display: none" @change="handleImport" />
      </div>
    </el-card>

    <!-- 物资表格 -->
    <el-card class="material-table-card">
      <el-alert v-if="filterLowStock" type="warning" :closable="false" show-icon style="margin-bottom: 14px">
        <template #title>
          库存预警清单：可借数量低于或等于自身预警阈值的物资，按可借数量升序排列。
          未单独设置阈值的物资按默认阈值 3 判定；补货入库后自动移出本清单。
        </template>
      </el-alert>
      <div class="table-display-bar">
        <span>物资列表</span>
        <div class="table-resize-handle" title="按住上下拖动，调整表格显示高度"
          @mousedown.prevent="startTableResize">
          <span class="resize-dots">↕</span>
          拖动调整表格高度
        </div>
        <el-button link type="primary" @click="resetTableHeight">恢复默认</el-button>
      </div>
      <el-table
        :data="tableData" stripe v-loading="loading" style="width: 100%"
        :height="tableHeight"
        row-key="id" @selection-change="handleSelectionChange"
        :header-cell-style="{ background: '#f5f7fa', fontWeight: 600 }"
        :empty-text="filterLowStock ? '当前没有低于预警阈值的物资' : '暂无物资数据'"
      >
        <el-table-column type="selection" width="46" :selectable="row => canOperate(row) && (isConsumable(row) || canBorrow(row))" />
        <el-table-column prop="code" label="编号" width="100" />
        <el-table-column prop="name" label="物资名称" min-width="180">
          <template #default="{ row }">
            <span>{{ row.name }}</span>
            <el-tag v-if="row.hazardous" type="danger" size="small" effect="plain" style="margin-left: 4px">危化品</el-tag>
            <el-tag v-if="row.needsTraining" type="warning" size="small" effect="plain" style="margin-left: 4px">需培训</el-tag>
            <el-tag v-if="row.approvalRequired" type="primary" size="small" effect="plain" style="margin-left: 4px">需审批</el-tag>
            <el-tag v-if="row.borrowable === false" type="info" size="small" effect="plain" style="margin-left: 4px">仅预约</el-tag>
            <el-tooltip v-if="isMaintenanceExpired(row)" content="检定/维护已到期，暂停预约与借用；完成后请在编辑里更新「下次检定日期」">
              <el-tag type="danger" size="small" effect="dark" style="margin-left: 4px">检定过期</el-tag>
            </el-tooltip>
            <el-tooltip v-if="isExpiringSoon(row)" :content="'有效期至 ' + row.validUntil + '，请及时更换'">
              <el-tag type="warning" size="small" effect="dark" style="margin-left: 4px">临期/过期</el-tag>
            </el-tooltip>
          </template>
        </el-table-column>
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
        <!-- 预警模式下显示判定依据，否则用户看不出这条为什么算预警 -->
        <el-table-column v-if="filterLowStock" label="预警阈值" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="info" size="small" effect="plain">{{ thresholdOf(row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="存放位置" width="130" show-overflow-tooltip />
        <el-table-column label="物资状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="deviceStatusTag(row.deviceStatus)" size="small" effect="dark">
              {{ deviceStatusLabel(row.deviceStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="330" fixed="right">
          <template #default="{ row }">
            <!-- 消耗型（耗材/水/试剂）：领用即核销，无需归还，也不占时段 -->
            <el-button v-if="isConsumable(row)" type="warning" size="small"
              :disabled="!canOperate(row)" @click="showBorrowDialog(row)">
              领用
            </el-button>
            <!-- 可归还型（设备/玻璃器皿）：默认两条通道并列；
                 关闭「允许外借」的物资（大型/固定式仪器）只保留预约通道 -->
            <template v-else>
              <el-button v-if="canBorrow(row)" type="warning" size="small"
                :disabled="!canOperate(row)" @click="showBorrowDialog(row)">
                借用
              </el-button>
              <el-button type="success" size="small" plain :disabled="!canOperate(row)" @click="goReserve(row)">
                预约
              </el-button>
            </template>
            <el-button type="info" size="small" plain @click="showLogs(row)">流水</el-button>
            <el-dropdown style="margin-left: 8px; vertical-align: middle" @command="cmd => handleMoreCommand(cmd, row)">
              <el-button size="small" plain>
                更多<el-icon style="margin-left: 2px"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="training">培训资质</el-dropdown-item>
                  <el-dropdown-item command="attachment">附件资料</el-dropdown-item>
                  <el-dropdown-item command="label">打印标签</el-dropdown-item>
                  <el-dropdown-item v-if="isAdmin" command="status" divided>维护物资状态</el-dropdown-item>
                  <el-dropdown-item v-if="isSuperAdmin" command="edit" divided>编辑物资</el-dropdown-item>
                  <el-dropdown-item v-if="isSuperAdmin" command="delete">删除物资</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-tag v-if="!isAdmin" type="info" size="small" effect="plain" style="margin-left: 8px">仅查看</el-tag>
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
        <el-form-item label="物资状态">
          <el-select v-model="dialog.form.deviceStatus" style="width: 100%">
            <el-option label="可用（可正常借用）" value="AVAILABLE" />
            <el-option label="维修中（不可借用）" value="MAINTENANCE" />
            <el-option label="已报废（不可借用）" value="SCRAPPED" />
          </el-select>
        </el-form-item>
        <el-divider content-position="left" style="margin: 4px 0 12px; font-size: 13px; color: #909399">借用 / 预约规则</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="允许外借">
              <el-switch v-model="dialog.form.borrowable" active-text="可借走" inactive-text="仅预约" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="需培训资质">
              <el-switch v-model="dialog.form.needsTraining" active-text="需授权" inactive-text="不需要" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="借用需审批">
              <el-switch v-model="dialog.form.approvalRequired" active-text="需审批" inactive-text="免审批" />
            </el-form-item>
          </el-col>
        </el-row>
        <div style="margin: -8px 0 10px 100px; font-size: 12px; color: #909399; line-height: 1.5">
          打开「借用需审批」后，<b>教师与管理员本人借用也要走审批</b>，用于高值、稀缺、收费或唯一一台的仪器；
          普通耗材与常规设备保持免审批，避免给师生增加无谓流程；审批人不能是申请人本人。
        </div>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="机时单价">
              <el-input-number v-model="dialog.form.hourlyPrice" :min="0" :precision="2" :controls="false" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="借用单价">
              <el-input-number v-model="dialog.form.unitPrice" :min="0" :precision="2" :controls="false" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <div style="margin: -8px 0 10px 100px; font-size: 12px; color: #909399; line-height: 1.5">
          机时单价 = 元/小时（预约计费）；借用单价 = 元/件·天（借用与领用计费）；填 0 表示不收费。
          关闭「允许外借」的物资（大型/固定式仪器）只保留预约通道。
        </div>
        <el-divider content-position="left" style="margin: 4px 0 12px; font-size: 13px; color: #909399">安全与计量信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="危化品">
              <el-switch v-model="dialog.form.hazardous" active-text="是" inactive-text="否" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="CAS号">
              <el-input v-model="dialog.form.casNo" placeholder="如 64-17-5" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="存放柜位">
              <el-input v-model="dialog.form.cabinetNo" placeholder="如 危化品柜 A-2" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="有效期">
              <el-date-picker v-model="dialog.form.validUntil" type="date" value-format="YYYY-MM-DD"
                placeholder="试剂/耗材有效期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="下次检定">
              <el-date-picker v-model="dialog.form.nextMaintenanceDate" type="date" value-format="YYYY-MM-DD"
                placeholder="下次检定/维护日期" style="width: 100%" />
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

    <!-- 物资状态维护弹窗（教师 / 超管，只改状态不改数量） -->
    <el-dialog v-model="statusDialog.visible" title="物资状态维护" width="440px">
      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 14px">
        此操作只调整物资的使用状态，不会改变任何数量。损坏送修后修好即可在此改回「可用」。
      </el-alert>
      <el-form label-width="100px">
        <el-form-item label="物资名称">
          <el-input :model-value="statusDialog.material?.name" disabled />
        </el-form-item>
        <el-form-item label="物资状态">
          <el-select v-model="statusDialog.form.deviceStatus" style="width: 100%">
            <el-option label="可用（可正常借用）" value="AVAILABLE" />
            <el-option label="维修中（不可借用）" value="MAINTENANCE" />
            <el-option label="已报废（不可借用）" value="SCRAPPED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialog.visible = false">取消</el-button>
        <el-button type="warning" @click="handleStatusSave" :loading="statusDialog.saving">保存状态</el-button>
      </template>
    </el-dialog>

    <!-- 领用/借用弹窗 -->
    <el-dialog v-model="borrowDialog.visible" :title="isConsumable(borrowDialog.material) ? '领用物资' : '直接借用物资'" width="520px">
      <!-- 教师/管理员：免审批，提交即出库 -->
      <el-alert v-if="isAdmin" type="success" :closable="false" show-icon style="margin-bottom: 16px">
        <template #title>
          教师 / 管理员自助{{ isConsumable(borrowDialog.material) ? '领用' : '借用' }}免审批：
          提交后立即扣减库存并生成记录，无需再走审批；{{ isConsumable(borrowDialog.material) ? '领用即核销。' : '归还仍在「借用记录」中操作。' }}
        </template>
      </el-alert>
      <!-- 高值/稀缺仪器：即使是教师本人也要审批，且不能自审 -->
      <el-alert v-if="isAdmin && borrowDialog.material?.approvalRequired" type="warning" :closable="false" show-icon style="margin-bottom: 16px">
        <template #title>
          该物资标记为「借用需审批」（高值/稀缺仪器）：提交后进入审批流程，且
          <b>审批人不能是申请人本人</b>，请由其他教师或管理员审批。
        </template>
      </el-alert>
      <!-- 两条通道的区别说明：避免学生把「借用」当成「预约」的替代品而误用 -->
      <el-alert v-else-if="!isConsumable(borrowDialog.material)" type="info" :closable="false" show-icon style="margin-bottom: 16px">
        <template #title>
          「借用」按天计，审批通过后可直接在「借用记录」中归还，适合急用或需带离实验室的场景；
          若只在实验室内短时使用，建议改用「预约」按时段占用。
        </template>
      </el-alert>
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
        <el-form-item :label="isConsumable(borrowDialog.material) ? '领用人' : '借用人'">
          <el-input v-model="borrowDialog.form.borrower" disabled />
          <span style="color: #909399; font-size: 12px; line-height: 1.6">
            以当前登录账号为准，不可代他人提交（服务端按登录身份记账，防止身份伪造）
          </span>
        </el-form-item>
        <el-form-item :label="isConsumable(borrowDialog.material) ? '领用数量' : '借用数量'" required>
          <el-input-number v-model="borrowDialog.form.quantity" :min="1" :max="borrowDialog.material?.availableQuantity" style="width: 100%" />
        </el-form-item>
        <el-form-item label="用途说明">
          <el-input v-model="borrowDialog.form.purpose" type="textarea" :rows="2" placeholder="实验项目名称或用途" />
        </el-form-item>
        <el-form-item v-if="borrowDialog.material?.hazardous" label="第二领用人" required>
          <el-input v-model="borrowDialog.form.secondBorrower" placeholder="危险化学品须双人领用，请填写另一名领用人姓名" />
        </el-form-item>
        <el-form-item v-if="borrowFee > 0" label="预估费用">
          <el-tag type="warning" size="large">¥{{ borrowFee.toFixed(2) }}</el-tag>
          <span style="margin-left: 8px; color: #909399; font-size: 12px">{{ borrowFeeRule }}</span>
        </el-form-item>
        <el-form-item v-if="!isConsumable(borrowDialog.material)" label="预计归还" required>
          <el-date-picker v-model="borrowDialog.form.expectedReturnTime" type="datetime"
            placeholder="请选择归还日期与时间" format="YYYY-MM-DD HH:mm"
            :disabled-date="disabledReturnDate" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="borrowDialog.visible = false">取消</el-button>
        <el-button type="warning" @click="handleBorrow" :loading="borrowDialog.saving">
          {{ isConsumable(borrowDialog.material) ? '确认领用' : '确认借用' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 一次实验多项物资批量申请 -->
    <el-dialog v-model="batchDialog.visible" title="批量借用 / 领用" width="820px">
      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 14px">
        <template #title>
          同一实验所需物资合并为一张申请单。普通物资的教师 / 管理员提交后直接出库；
          学生申请或批次中含“需审批”物资时，整单进入审批，一次处理全部明细。
        </template>
      </el-alert>
      <el-table :data="batchDialog.rows" size="small" max-height="330" style="width: 100%">
        <el-table-column prop="name" label="物资" min-width="150" show-overflow-tooltip />
        <el-table-column label="类别" width="90">
          <template #default="{ row }">{{ categoryLabel(row.category) }}</template>
        </el-table-column>
        <el-table-column label="可用" width="80" align="center">
          <template #default="{ row }">{{ row.availableQuantity }} {{ row.unit }}</template>
        </el-table-column>
        <el-table-column label="数量" width="130">
          <template #default="{ row }">
            <el-input-number v-model="row.quantity" :min="1" :max="row.availableQuantity" :controls="false" style="width: 100%" />
          </template>
        </el-table-column>
        <el-table-column label="第二领用人" min-width="170">
          <template #default="{ row }">
            <el-input v-if="row.hazardous" v-model="row.secondBorrower" placeholder="危化品必填" />
            <span v-else style="color: #c0c4cc">无需</span>
          </template>
        </el-table-column>
      </el-table>
      <el-form :model="batchDialog.form" label-width="100px" style="margin-top: 14px">
        <el-form-item label="用途说明">
          <el-input v-model="batchDialog.form.purpose" type="textarea" :rows="2" placeholder="本次实验名称或用途" />
        </el-form-item>
        <el-form-item v-if="batchHasReturnable" label="预计归还" required>
          <el-date-picker v-model="batchDialog.form.expectedReturnTime" type="datetime"
            placeholder="批次内含可归还物资，请选择归还时间" format="YYYY-MM-DD HH:mm"
            :disabled-date="disabledReturnDate" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialog.visible = false">取消</el-button>
        <el-button type="warning" @click="submitBatchBorrow" :loading="batchDialog.saving">
          提交 {{ batchDialog.rows.length }} 项物资
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

    <!-- 培训 / 上机资质弹窗 -->
    <el-dialog v-model="trainingDialog.visible" :title="'培训资质 — ' + (trainingDialog.material?.name || '')" width="760px">
      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 12px">
        标记为「需培训」的物资，只有名单内且资质未过期的人才能预约或借用（对标大型仪器共享平台的上机准入规则）。
      </el-alert>
      <el-row v-if="isAdmin" :gutter="12" style="margin-bottom: 12px">
        <el-col :span="7">
          <el-select v-model="trainingDialog.form.username" filterable placeholder="选择被授权人" style="width: 100%">
            <el-option v-for="u in candidates" :key="u.username"
              :label="u.realName + '（' + u.username + '）'" :value="u.username" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-date-picker v-model="trainingDialog.form.trainDate" type="date" value-format="YYYY-MM-DD"
            placeholder="培训日期" style="width: 100%" />
        </el-col>
        <el-col :span="6">
          <el-date-picker v-model="trainingDialog.form.validUntil" type="date" value-format="YYYY-MM-DD"
            placeholder="有效期（空=长期）" style="width: 100%" />
        </el-col>
        <el-col :span="5">
          <el-button type="primary" @click="handleGrantTraining" :loading="trainingDialog.saving">登记授权</el-button>
        </el-col>
      </el-row>
      <el-table :data="trainingDialog.list" stripe size="small" style="width: 100%" empty-text="暂无授权记录">
        <el-table-column prop="realName" label="被授权人" width="100" />
        <el-table-column prop="username" label="账号" width="120" />
        <el-table-column prop="trainer" label="培训/考核人" width="110" />
        <el-table-column label="培训日期" width="110">
          <template #default="{ row }">{{ row.trainDate || '-' }}</template>
        </el-table-column>
        <el-table-column label="有效期" width="120">
          <template #default="{ row }">{{ row.validUntil || '长期有效' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="trainingValid(row) ? 'success' : 'danger'" size="small">
              {{ trainingValid(row) ? '有效' : '已过期' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90">
          <template #default="{ row }">
            <el-button v-if="isAdmin" type="danger" size="small" plain @click="handleRevokeTraining(row)">撤销</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 附件资料弹窗 -->
    <el-dialog v-model="attachmentDialog.visible" :title="'附件资料 — ' + (attachmentDialog.material?.name || '')" width="700px">
      <el-upload v-if="isAdmin" drag :show-file-list="false" :http-request="customUpload" style="margin-bottom: 12px">
        <div class="el-upload__text">把设备照片 / 说明书 / 验收单拖到这里，或<em>点击上传</em>（单个文件不超过 10MB）</div>
      </el-upload>
      <el-table :data="attachmentDialog.list" stripe size="small" style="width: 100%" empty-text="暂无附件">
        <el-table-column prop="fileName" label="文件名" min-width="200" show-overflow-tooltip />
        <el-table-column label="大小" width="100">
          <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column prop="uploader" label="上传人" width="90" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" plain @click="downloadAttachment(row)">下载/预览</el-button>
            <el-button v-if="isAdmin" size="small" type="danger" plain @click="removeAttachment(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 物资标签弹窗（可打印） -->
    <el-dialog v-model="labelDialog.visible" title="打印物资标签" width="460px">
      <div id="material-print-label" class="print-label">
        <div class="label-title">实验物资标签</div>
        <div class="label-code">编号：{{ labelDialog.material?.code }}</div>
        <div class="label-name">{{ labelDialog.material?.name }}</div>
        <div class="label-line">规格：{{ labelDialog.material?.specification || '-' }}</div>
        <div class="label-line">存放：{{ labelDialog.material?.location || '-' }}</div>
        <div class="label-line">单位：{{ labelDialog.material?.unit || '-' }}　类别：{{ categoryLabel(labelDialog.material?.category) }}</div>
        <div class="label-tip">借用/预约请扫码或在系统中输入编号 {{ labelDialog.material?.code }}</div>
      </div>
      <template #footer>
        <el-button @click="labelDialog.visible = false">关闭</el-button>
        <el-button type="primary" @click="printLabel">打印标签</el-button>
      </template>
    </el-dialog>

    <!-- 库存盘点弹窗 -->
    <el-dialog v-model="checkDialog.visible" title="库存盘点" width="860px">
      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 12px">
        录入实盘数量，与账面不一致的会自动调整库存，并写入一条变动类型为「调整」的库存流水；
        盘亏导致可借数量为负时会被拒绝，需先归还相关物资。
      </el-alert>
      <el-input v-model="checkDialog.keyword" placeholder="筛选物资（可选）" clearable style="width: 240px; margin-bottom: 10px" />
      <el-table :data="filteredCheckRows" stripe size="small" height="340" style="width: 100%">
        <el-table-column prop="code" label="编号" width="100" />
        <el-table-column prop="name" label="物资名称" min-width="150" show-overflow-tooltip />
        <el-table-column label="账面总量" width="100" align="center">
          <template #default="{ row }">{{ row.totalQuantity }}</template>
        </el-table-column>
        <el-table-column label="可借数量" width="100" align="center">
          <template #default="{ row }">{{ row.availableQuantity }}</template>
        </el-table-column>
        <el-table-column label="实盘数量" width="150">
          <template #default="{ row }">
            <el-input-number v-model="row.actualQuantity" :min="0" :controls="false" style="width: 100%" />
          </template>
        </el-table-column>
        <el-table-column label="差异" width="90" align="center">
          <template #default="{ row }">
            <span :style="{ fontWeight: 600, color: diffOf(row) === 0 ? '#909399' : (diffOf(row) > 0 ? '#67C23A' : '#F56C6C') }">
              {{ diffOf(row) > 0 ? '+' : '' }}{{ diffOf(row) }}
            </span>
          </template>
        </el-table-column>
      </el-table>
      <el-input v-model="checkDialog.remark" placeholder="盘点备注（如：2026 春季期末盘点）" style="margin-top: 10px" />
      <template #footer>
        <el-button @click="checkDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="handleStockCheck" :loading="checkDialog.saving">提交盘点</el-button>
      </template>
    </el-dialog>

    <!-- Excel 批量建档说明 -->
    <el-dialog v-model="importHelpDialog.visible" title="Excel 批量建档说明" width="760px">
      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 16px">
        <template #title>
          这里的 Excel 导入用于“批量新增物资并建立期初库存”，不是给已有物资补货。
          已有物资增加库存，请到「采购入库」页面创建入库单。
        </template>
      </el-alert>
      <el-table :data="importColumns" size="small" border style="width: 100%">
        <el-table-column prop="column" label="Excel 列" width="150" />
        <el-table-column prop="required" label="是否必填" width="100" align="center" />
        <el-table-column prop="content" label="填写内容" min-width="280" show-overflow-tooltip />
        <el-table-column prop="sample" label="示例" min-width="180" show-overflow-tooltip />
      </el-table>
      <div class="import-help-tips">
        <div>1. 第一行必须是表头，数据从第二行开始；支持 `.xlsx` 和 `.xls`。</div>
        <div>2. 类别可填编码，也可填中文名：实验设备、耗材、实验用水、化学试剂、玻璃器皿。</div>
        <div>3. 数量必须是大于 0 的整数；导入成功后会自动生成物资编号和期初入库流水。</div>
        <div>4. 建议先下载模板，保留表头并删除示例行后再填写自己的数据。</div>
      </div>
      <template #footer>
        <el-button @click="importHelpDialog.visible = false">关闭</el-button>
        <el-button type="primary" @click="handleExcelCommand('template')">下载模板</el-button>
        <el-button type="warning" @click="handleExcelCommand('import')">选择文件导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Download, Upload, ArrowDown, Checked } from '@element-plus/icons-vue'
import { materialApi, borrowApi, categoryApi, excelApi, trainingApi, attachmentApi, authFetch } from '../api'
import { authStore } from '../stores/auth'
import { toLocalDateTimeString } from '../utils/date'
import { useRouter, useRoute } from 'vue-router'
const router = useRouter()
const route = useRoute()

const loading = ref(false)
const tableData = ref([])
const fileInput = ref()
const search = reactive({ keyword: '', category: '' })
const filterBorrowable = ref(false)
// 库存预警模式：数据源换成 /material/low-stock，并额外显示「预警阈值」列
const filterLowStock = ref(false)
// 预警清单缓存：该接口不支持查询参数，故 keyword / 类别变化时本地重筛，不再重复请求
const lowStockList = ref([])
const categories = ref([])

// 表格使用内部滚动，让底部横向滚动条始终可见；高度可通过顶部把手拖动调整。
const defaultTableHeight = () => Math.max(360, Math.min(680, window.innerHeight - 360))
const tableHeight = ref(defaultTableHeight())
let resizeStartY = 0
let resizeStartHeight = 0

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
  form: { name: '', category: 'DEVICE', specification: '', unit: '', totalQuantity: 1, warningThreshold: 3, location: '', remark: '', deviceStatus: 'AVAILABLE' }
})

const borrowDialog = reactive({
  visible: false, saving: false,
  material: null,
  form: { borrower: '', quantity: 1, purpose: '', expectedReturnTime: null }
})

const selectedRows = ref([])
const batchDialog = reactive({
  visible: false, saving: false, rows: [],
  form: { purpose: '', expectedReturnTime: null }
})
const batchHasReturnable = computed(() => batchDialog.rows.some(row => !isConsumable(row)))

// 库存流水弹窗
const logDialog = reactive({
  visible: false, loading: false,
  material: null, list: [], page: 1, size: 8, total: 0
})

// 培训资质 / 附件资料 / 物资标签 / 库存盘点
const trainingDialog = reactive({
  visible: false, saving: false, material: null, list: [],
  form: { username: '', trainDate: '', validUntil: '' }
})
const candidates = ref([])
const attachmentDialog = reactive({ visible: false, material: null, list: [] })
const labelDialog = reactive({ visible: false, material: null })
const checkDialog = reactive({ visible: false, saving: false, keyword: '', remark: '', rows: [] })
const importHelpDialog = reactive({ visible: false })
const importColumns = [
  { column: '物资名称', required: '是', content: '物资的完整名称', sample: '数字示波器' },
  { column: '类别', required: '是', content: '分类编码或中文分类名', sample: '实验设备 / DEVICE' },
  { column: '规格型号', required: '否', content: '品牌、型号或规格', sample: 'DS1054Z 50MHz' },
  { column: '单位', required: '否', content: '计量单位', sample: '台' },
  { column: '数量', required: '是', content: '大于 0 的整数', sample: '2' },
  { column: '存放位置', required: '否', content: '实验室或柜位', sample: 'A102电子实验室' },
  { column: '预警阈值', required: '否', content: '可借数量达到该值时预警', sample: '1' }
]

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
// 预警阈值：未设置时按 3 判定（与后端 MaterialService 同口径）
// 注意用 != null 而非 || —— 阈值 0 是合法值（弹窗里 el-input-number :min="0"），
// 用 || 会把 0 误判成"未设置"，导致前端按 3 判定、后端按 0 判定，两边清单对不上
function thresholdOf(row) {
  return row.warningThreshold != null ? row.warningThreshold : 3
}
function isLowStock(row) {
  return (row.availableQuantity || 0) <= thresholdOf(row)
}
// 物资类别是否可归还（设备/玻璃器皿 = true；耗材/水/试剂 = false）
// 口径以分类表 t_category.returnable 为准，与后端 CategoryService.isReturnable 同源，避免前后端两套标准
function isReturnableCategory(row) {
  // 注意：本函数会在渲染期被调用（弹窗标题等），row 可能为 null（未选择物资时），
  // 早期版本这里直接读 row.category，一旦分类列表加载完成就会抛异常，
  // Vue 会跳过整棵子树的更新 —— 表现为物资列表空白、点「新增物资」没反应。
  if (!row) return false
  const cat = categories.value.find(c => c.code === row.category)
  return cat ? !!cat.returnable : false
}
// 消耗型物资（耗材/水/试剂）：领用后直接核销，无需归还，也不占时段
function isConsumable(row) {
  return !isReturnableCategory(row)
}
// 行内操作前置条件：还有可借数量，且设备状态为「可用」（维修中/已报废不可借）
function canOperate(row) {
  return (row.availableQuantity || 0) > 0 && (row.deviceStatus || 'AVAILABLE') === 'AVAILABLE'
}
// 是否允许带离借用：关闭「允许外借」的物资（大型/固定式仪器）只保留预约通道；历史数据按允许处理
function canBorrow(row) {
  return row.borrowable !== false
}
function handleSelectionChange(rows) {
  selectedRows.value = rows || []
}
function showBatchBorrowDialog() {
  if (!selectedRows.value.length) {
    ElMessage.warning('请先在表格中勾选需要借用的物资')
    return
  }
  batchDialog.rows = selectedRows.value.map(row => ({
    ...row,
    quantity: 1,
    secondBorrower: ''
  }))
  batchDialog.form = { purpose: '', expectedReturnTime: null }
  batchDialog.visible = true
}
function submitBatchBorrow() {
  if (batchDialog.rows.some(row => !row.quantity || row.quantity < 1)) {
    ElMessage.warning('每项物资数量至少为 1')
    return
  }
  if (batchDialog.rows.some(row => row.hazardous && !(row.secondBorrower || '').trim())) {
    ElMessage.warning('危险化学品明细必须填写第二领用人')
    return
  }
  if (batchHasReturnable.value && !batchDialog.form.expectedReturnTime) {
    ElMessage.warning('批次内含可归还物资，请选择预计归还时间')
    return
  }
  batchDialog.saving = true
  const requiresApproval = !isAdmin.value
    || batchDialog.rows.some(row => row.approvalRequired)
  borrowApi.borrowBatch({
    purpose: batchDialog.form.purpose,
    expectedReturnTime: batchDialog.form.expectedReturnTime
      ? toLocalDateTimeString(batchDialog.form.expectedReturnTime)
      : null,
    lines: batchDialog.rows.map(row => ({
      materialId: row.id,
      quantity: row.quantity,
      secondBorrower: row.secondBorrower || null
    }))
  }).then(() => {
    ElMessage.success(requiresApproval
      ? '批量申请已提交，整单进入审批流程'
      : '批量出库完成，库存已同步扣减')
    batchDialog.visible = false
    selectedRows.value = []
    loadData()
  }).catch(e => ElMessage.error(e.message || '批量提交失败'))
    .finally(() => batchDialog.saving = false)
}
/** 检定 / 维护是否已到期（到期后暂停预约与借用） */
function isMaintenanceExpired(row) {
  return !!row.nextMaintenanceDate && new Date(row.nextMaintenanceDate) < new Date(new Date().toDateString())
}
/** 有效期是否已过或 30 天内到期 */
function isExpiringSoon(row) {
  if (!row.validUntil) return false
  const limit = new Date()
  limit.setDate(limit.getDate() + 30)
  return new Date(row.validUntil) <= limit
}
function round2(v) {
  return Math.round((Number(v) || 0) * 100) / 100
}

/** 预估费用：可归还型按「天 × 借用单价」，消耗型按「件 × 借用单价」 */
const borrowFee = computed(() => {
  const m = borrowDialog.material
  if (!m) return 0
  const price = Number(m.unitPrice) || 0
  if (price <= 0) return 0
  if (isConsumable(m)) return round2((Number(borrowDialog.form.quantity) || 0) * price)
  const t = borrowDialog.form.expectedReturnTime
  if (!t) return 0
  const days = Math.max(1, Math.round(
    (new Date(new Date(t).toDateString()) - new Date(new Date().toDateString())) / 86400000))
  return round2(days * price)
})
const borrowFeeRule = computed(() => {
  const m = borrowDialog.material
  if (!m) return ''
  return isConsumable(m)
    ? `${borrowDialog.form.quantity} 件 × ¥${(Number(m.unitPrice) || 0).toFixed(2)}/件`
    : `按天计费，单价 ¥${(Number(m.unitPrice) || 0).toFixed(2)}/天（预估，实际以归还日期结算）`
})
// 预计归还日期：不能早于今天（后端按自然日校验单次借用最长天数）
function disabledReturnDate(time) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return time.getTime() < today.getTime()
}
// 设备状态维护：AVAILABLE-可用 / MAINTENANCE-维修中 / SCRAPPED-已报废
function deviceStatusLabel(s) {
  const map = { AVAILABLE: '可用', MAINTENANCE: '维修中', SCRAPPED: '已报废' }
  return map[s] || (s || '可用')
}
function deviceStatusTag(s) {
  const map = { AVAILABLE: 'success', MAINTENANCE: 'warning', SCRAPPED: 'info' }
  return map[s] || 'success'
}

// 跳转到预约管理页并预选设备
function goReserve(row) {
  router.push({ path: '/reservation', query: { materialId: row.id, materialName: row.name } })
}

// 本地筛选统一收口：保证预警模式与普通模式行为可预期
function applyLocalFilter(list) {
  return (list || []).filter(d => {
    // 「仅可借」筛掉可借为 0 的（预警模式下可选：只看"还有货但已告急"的）
    if (filterBorrowable.value && !((d.availableQuantity || 0) > 0)) return false
    // 预警接口不支持查询参数，故关键字与类别在本地筛
    if (filterLowStock.value) {
      const kw = (search.keyword || '').trim().toLowerCase()
      if (kw && !`${d.name || ''}${d.code || ''}`.toLowerCase().includes(kw)) return false
      if (search.category && d.category !== search.category) return false
    }
    return true
  })
}

// 筛选条件变化：预警清单已全量在手，本地重筛即可，不必重复请求
function refine() {
  if (filterLowStock.value) {
    tableData.value = applyLocalFilter(lowStockList.value)
    return
  }
  loadData()
}

// 切换预警模式：必须重新请求——数据源要从 list 换成 lowStock
function handleLowStockToggle() {
  if (!filterLowStock.value) lowStockList.value = []
  loadData()
}

function loadData() {
  loading.value = true
  const params = { ...search }
  // 预警清单走独立接口（此前 /material/low-stock 前端零调用）；
  // 普通模式仍走 list，关键字/类别交给服务端处理
  const listCall = filterLowStock.value ? materialApi.lowStock() : materialApi.list(params)
  Promise.all([
    listCall,
    materialApi.stats(),
    categoryApi.list()
  ]).then(([data, s, cats]) => {
    categories.value = cats || []
    if (filterLowStock.value) lowStockList.value = data || []
    tableData.value = applyLocalFilter(filterLowStock.value ? lowStockList.value : (data || []))
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
  refine()
}

// ==== 单项借用 ====
// ====== 物资状态维护（教师 / 超管）：只改状态，不改数量 ======
const statusDialog = reactive({
  visible: false, saving: false,
  material: null,
  form: { deviceStatus: 'AVAILABLE' }
})

function showStatusDialog(row) {
  statusDialog.material = row
  statusDialog.form.deviceStatus = row.deviceStatus || 'AVAILABLE'
  statusDialog.visible = true
}

async function handleStatusSave() {
  statusDialog.saving = true
  try {
    await materialApi.updateStatus(statusDialog.material.id, {
      deviceStatus: statusDialog.form.deviceStatus
    })
    ElMessage.success('物资状态已更新')
    statusDialog.visible = false
    loadData()
  } catch (e) {
    ElMessage.error(e.message || '状态更新失败')
  } finally {
    statusDialog.saving = false
  }
}

function showBorrowDialog(row) {
  borrowDialog.material = row
  borrowDialog.form = {
    borrower: currentUser.value.realName || '',
    quantity: 1,
    purpose: '',
    expectedReturnTime: null,
    secondBorrower: ''
  }
  borrowDialog.visible = true
}

function handleBorrow() {
  const isConsume = isConsumable(borrowDialog.material)
  if (borrowDialog.form.quantity < 1) { ElMessage.warning('数量至少为1'); return }
  // 危险化学品双人领用（对标《危险化学品安全管理条例》的双人领取要求）
  if (borrowDialog.material?.hazardous && !(borrowDialog.form.secondBorrower || '').trim()) {
    ElMessage.warning('该物资为危险化学品，须双人领用，请填写第二领用人')
    return
  }
  // 可归还型必须填归还时间：后端按 borrow_max_days 强制校验，这里提前拦住，避免提交后才报错
  if (!isConsume && !borrowDialog.form.expectedReturnTime) {
    ElMessage.warning('请选择预计归还时间'); return
  }

  borrowDialog.saving = true
  // 借用人/领用人以服务端登录态为准（此处传值仅作兜底），无法通过改请求体伪造他人身份
  borrowApi.borrow({
    materialId: borrowDialog.material.id,
    borrower: borrowDialog.form.borrower || currentUser.value.realName,
    quantity: borrowDialog.form.quantity,
    purpose: borrowDialog.form.purpose,
    secondBorrower: borrowDialog.form.secondBorrower || null,
    expectedReturnTime: borrowDialog.form.expectedReturnTime
      ? toLocalDateTimeString(borrowDialog.form.expectedReturnTime)
      : null
  }).then(() => {
    if (isAdmin.value && !borrowDialog.material?.approvalRequired) {
      // 教师/管理员自助通道：提交即出库，不产生待办
      ElMessage.success(isConsume
        ? '已直接领用（免审批），库存已核销'
        : '已直接借出（免审批），归还请在「借用记录」中操作')
    } else {
      ElMessage.success(isConsume
        ? '领用申请已提交，等待其他教师或管理员审批'
        : '借用申请已提交，等待其他教师或管理员审批')
    }
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
function handleExcelCommand(command) {
  if (command === 'help') {
    importHelpDialog.visible = true
  } else if (command === 'template') {
    excelApi.downloadImportTemplate()
      .then(() => ElMessage.success('模板已下载，请按“填写说明”工作表填写'))
      .catch(e => ElMessage.error(e.message || '模板下载失败'))
  } else if (command === 'import') {
    triggerImport()
  }
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

function startTableResize(event) {
  resizeStartY = event.clientY
  resizeStartHeight = tableHeight.value
  document.body.classList.add('is-resizing-material-table')
  window.addEventListener('mousemove', handleTableResize)
  window.addEventListener('mouseup', stopTableResize)
}

function handleTableResize(event) {
  const next = resizeStartHeight + event.clientY - resizeStartY
  tableHeight.value = Math.max(300, Math.min(900, Math.round(next)))
}

function stopTableResize() {
  document.body.classList.remove('is-resizing-material-table')
  window.removeEventListener('mousemove', handleTableResize)
  window.removeEventListener('mouseup', stopTableResize)
}

function resetTableHeight() {
  tableHeight.value = defaultTableHeight()
}

// ==== CRUD ====
function showAddDialog() {
  dialog.isEdit = false
  dialog.visible = true
  const defaultCat = categories.value.length > 0 ? categories.value[0].code : ''
  dialog.form = {
    name: '', category: defaultCat, specification: '', unit: '', totalQuantity: 1,
    warningThreshold: 3, location: '', remark: '', deviceStatus: 'AVAILABLE',
    borrowable: true, needsTraining: false, hourlyPrice: 0, unitPrice: 0,
    approvalRequired: false,
    hazardous: false, casNo: '', cabinetNo: '', validUntil: null, nextMaintenanceDate: null
  }
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

// ==================== 行内「更多」菜单 ====================
function handleMoreCommand(cmd, row) {
  if (cmd === 'training') showTrainingDialog(row)
  else if (cmd === 'attachment') showAttachmentDialog(row)
  else if (cmd === 'label') showLabelDialog(row)
  else if (cmd === 'status') showStatusDialog(row)
  else if (cmd === 'edit') showEditDialog(row)
  else if (cmd === 'delete') {
    ElMessageBox.confirm('确定删除物资「' + row.name + '」？', '删除物资', { type: 'warning' })
      .then(() => handleDelete(row.id))
      .catch(() => {})
  }
}

// ==================== 培训 / 上机资质 ====================
function trainingValid(row) {
  if (!row.validUntil) return true
  return new Date(row.validUntil) >= new Date(new Date().toDateString())
}

function showTrainingDialog(row) {
  trainingDialog.material = row
  trainingDialog.list = []
  trainingDialog.form = { username: '', trainDate: '', validUntil: '' }
  trainingDialog.visible = true
  loadTraining()
  if (isAdmin.value && candidates.value.length === 0) {
    trainingApi.candidates().then(data => { candidates.value = data || [] }).catch(() => {})
  }
}

function loadTraining() {
  if (!trainingDialog.material) return
  trainingApi.listByMaterial(trainingDialog.material.id)
    .then(data => { trainingDialog.list = data || [] })
    .catch(e => ElMessage.error('资质加载失败：' + e.message))
}

function handleGrantTraining() {
  const f = trainingDialog.form
  if (!f.username) { ElMessage.warning('请选择被授权人'); return }
  trainingDialog.saving = true
  trainingApi.grant({
    materialId: trainingDialog.material.id,
    username: f.username,
    trainDate: f.trainDate || null,
    validUntil: f.validUntil || null
  }).then(() => {
    ElMessage.success('已登记培训资质')
    trainingDialog.form = { username: '', trainDate: '', validUntil: '' }
    loadTraining()
    loadData()
  }).catch(e => ElMessage.error(e.message)).finally(() => trainingDialog.saving = false)
}

function handleRevokeTraining(row) {
  ElMessageBox.confirm('确认撤销「' + row.realName + '」对「' + row.materialName + '」的资质？', '撤销资质', { type: 'warning' })
    .then(() => trainingApi.revoke(row.id))
    .then(() => { ElMessage.success('已撤销'); loadTraining() })
    .catch(() => {})
}

// ==================== 附件资料 ====================
function formatSize(bytes) {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(2) + ' MB'
}

function showAttachmentDialog(row) {
  attachmentDialog.material = row
  attachmentDialog.list = []
  attachmentDialog.visible = true
  loadAttachments()
}

function loadAttachments() {
  if (!attachmentDialog.material) return
  attachmentApi.list('MATERIAL', attachmentDialog.material.id)
    .then(data => { attachmentDialog.list = data || [] })
    .catch(e => ElMessage.error('附件加载失败：' + e.message))
}

function customUpload(option) {
  attachmentApi.upload('MATERIAL', attachmentDialog.material.id, option.file)
    .then(() => { ElMessage.success('上传成功'); loadAttachments() })
    .catch(e => ElMessage.error(e.message || '上传失败'))
}

function downloadAttachment(row) {
  authFetch(attachmentApi.downloadUrl(row.id)).then(res => {
    if (!res.ok) throw new Error('下载失败（HTTP ' + res.status + '）')
    return res.blob()
  }).then(blob => {
    const url = URL.createObjectURL(blob)
    window.open(url, '_blank')
    setTimeout(() => URL.revokeObjectURL(url), 60000)
  }).catch(e => ElMessage.error(e.message))
}

function removeAttachment(row) {
  ElMessageBox.confirm('确认删除附件「' + row.fileName + '」？', '删除附件', { type: 'warning' })
    .then(() => attachmentApi.remove(row.id))
    .then(() => { ElMessage.success('已删除'); loadAttachments() })
    .catch(() => {})
}

// ==================== 物资标签（可打印） ====================
function showLabelDialog(row) {
  labelDialog.material = row
  labelDialog.visible = true
}

/**
 * 打印标签：把标签区内容复制到新窗口打印。
 *
 * 说明：条码图形需要第三方条码库（离线环境无法安装依赖），因此系统按「编号」识别 ——
 * 标签上的编号可用学校的标签机打印成条码，扫码枪扫出来的就是编号，
 * 在物资管理页的搜索框里直接扫/输入编号即可定位，无需再手选下拉。
 */
function printLabel() {
  const el = document.getElementById('material-print-label')
  if (!el) return
  const w = window.open('', '_blank', 'width=460,height=420')
  w.document.write('<html><head><title>物资标签</title><style>'
    + 'body{font-family:"Microsoft YaHei",sans-serif;padding:16px}'
    + '.print-label{border:2px solid #303133;border-radius:8px;padding:14px 16px;width:380px}'
    + '.label-title{font-size:13px;color:#909399;letter-spacing:2px}'
    + '.label-code{font-size:15px;font-weight:700;margin:6px 0}'
    + '.label-name{font-size:20px;font-weight:700;margin-bottom:8px}'
    + '.label-line{font-size:13px;color:#303133;line-height:1.7}'
    + '.label-tip{margin-top:10px;font-size:11px;color:#909399;border-top:1px dashed #c0c4cc;padding-top:6px}'
    + '</style></head><body>' + el.outerHTML + '</body></html>')
  w.document.close()
  w.focus()
  setTimeout(() => { w.print() }, 300)
}

// ==================== 库存盘点 ====================
function showStockCheckDialog() {
  checkDialog.keyword = ''
  checkDialog.remark = ''
  checkDialog.rows = []
  checkDialog.visible = true
  materialApi.list({}).then(data => {
    checkDialog.rows = (data || []).map(m => ({ ...m, actualQuantity: m.totalQuantity }))
  }).catch(e => ElMessage.error('物资加载失败：' + e.message))
}

const filteredCheckRows = computed(() => {
  const kw = (checkDialog.keyword || '').trim().toLowerCase()
  if (!kw) return checkDialog.rows
  return checkDialog.rows.filter(r =>
    (r.name || '').toLowerCase().includes(kw) || (r.code || '').toLowerCase().includes(kw))
})

function diffOf(row) {
  return (Number(row.actualQuantity) || 0) - (Number(row.totalQuantity) || 0)
}

function handleStockCheck() {
  const items = checkDialog.rows
    .filter(r => diffOf(r) !== 0)
    .map(r => ({ materialId: r.id, actualQuantity: r.actualQuantity }))
  if (items.length === 0) { ElMessage.warning('所有物资实盘数量与账面一致，无需调整'); return }
  ElMessageBox.confirm('本次盘点有 ' + items.length + ' 项与账面不一致，确认按实盘数量调整库存？', '确认盘点', { type: 'warning' })
    .then(() => {
      checkDialog.saving = true
      return materialApi.stockCheck({ remark: checkDialog.remark, items })
    })
    .then(res => {
      if (!res) return
      ElMessage.success('盘点完成：核对 ' + res.checked + ' 项，调整 ' + res.changed
        + ' 项（盘盈 ' + res.increase + '，盘亏 ' + res.decrease + '）')
      checkDialog.visible = false
      loadData()
    })
    .catch(e => { if (e && e.message) ElMessage.error(e.message) })
    .finally(() => checkDialog.saving = false)
}

onMounted(() => {
  // 从首页「库存预警」卡片下钻而来：/material?lowStock=1
  if (route.query.lowStock === '1') filterLowStock.value = true
  loadData()
})

onBeforeUnmount(stopTableResize)
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

/* 物资标签：与打印窗口中的样式保持一致 */
.print-label {
  border: 2px solid #303133;
  border-radius: 8px;
  padding: 14px 16px;
}
.print-label .label-title { font-size: 13px; color: #909399; letter-spacing: 2px; }
.print-label .label-code { font-size: 15px; font-weight: 700; margin: 6px 0; }
.print-label .label-name { font-size: 20px; font-weight: 700; margin-bottom: 8px; }
.print-label .label-line { font-size: 13px; color: #303133; line-height: 1.7; }
.print-label .label-tip {
  margin-top: 10px; font-size: 11px; color: #909399;
  border-top: 1px dashed #c0c4cc; padding-top: 6px;
}

/* 搜索与操作分两行，按钮不足一行时自动换行，避免图标和文字互相遮挡 */
.material-toolbar :deep(.el-card__body) {
  padding: 16px 18px;
}
.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.filter-search {
  flex: 1 1 320px;
  min-width: 240px;
}
.category-filter {
  width: 170px;
  flex: 0 0 170px;
}
.filter-switches {
  display: flex;
  align-items: center;
  gap: 18px;
  flex-wrap: wrap;
}
.action-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 14px;
}
.action-bar :deep(.el-button + .el-button),
.action-bar :deep(.el-dropdown + .el-button) {
  margin-left: 0;
}

/* 表格内部滚动：横向滚动条固定在表格底部，不再需要滚到整页最底部才能操作 */
.table-display-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 34px;
  margin-bottom: 10px;
  color: #606266;
  font-size: 13px;
}
.table-display-bar > span:first-child {
  font-weight: 600;
  color: #303133;
}
.table-resize-handle {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 28px;
  border-radius: 6px;
  background: linear-gradient(90deg, transparent, #ebeef5 12%, #ebeef5 88%, transparent);
  color: #909399;
  cursor: ns-resize;
  user-select: none;
}
.table-resize-handle:hover {
  color: #409eff;
  background: linear-gradient(90deg, transparent, #ecf5ff 12%, #ecf5ff 88%, transparent);
}
.resize-dots {
  font-size: 17px;
  line-height: 1;
}
.import-help-tips {
  margin-top: 14px;
  padding: 12px 14px;
  border-radius: 8px;
  background: #f5f7fa;
  color: #606266;
  font-size: 13px;
  line-height: 1.9;
}
:global(body.is-resizing-material-table) {
  cursor: ns-resize;
  user-select: none;
}

@media (max-width: 900px) {
  .category-filter {
    flex: 1 1 170px;
    width: auto;
  }
  .action-bar {
    gap: 8px;
  }
}
</style>
