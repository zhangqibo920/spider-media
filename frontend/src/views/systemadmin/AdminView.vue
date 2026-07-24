<template>
  <div class="system-admin">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="系统配置" name="config">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>系统配置</span>
              <el-button type="primary" @click="showAddDialog = true">
                <el-icon><Plus /></el-icon> 添加配置
              </el-button>
            </div>
          </template>
          <el-table :data="configs" v-loading="loadingConfigs" stripe>
            <el-table-column prop="configName" label="配置名称" width="180" />
            <el-table-column prop="configKey" label="配置键" width="200" />
            <el-table-column prop="configValue" label="配置值">
              <template #default="{ row }">
                <el-input
                  v-show="editingId === row.id"
                  v-model="editingValue"
                  size="small"
                  @keyup.enter="handleSaveConfig(row)"
                />
                <span v-show="editingId !== row.id" @click="startEdit(row)" style="cursor: pointer">
                  {{ row.configValue || '-' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="configType" label="类型" width="80">
              <template #default="{ row }">
                <el-tag :type="row.configType === 'Y' ? 'danger' : 'info'" size="small">
                  {{ row.configType === 'Y' ? '内置' : '自定义' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button v-show="editingId === row.id" type="success" link @click="handleSaveConfig(row)">
                  保存
                </el-button>
                <el-button v-show="editingId === row.id" type="info" link @click="cancelEdit">
                  取消
                </el-button>
                <el-button v-show="editingId !== row.id" type="primary" link @click="startEdit(row)">编辑</el-button>
                <el-button v-show="editingId !== row.id" type="danger" link @click="handleDeleteConfig(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="操作日志" name="logs">
        <el-card>
          <el-table :data="logs" v-loading="loadingLogs" stripe>
            <el-table-column prop="username" label="用户" width="100" />
            <el-table-column prop="module" label="模块" width="100" />
            <el-table-column prop="action" label="操作" width="100" />
            <el-table-column prop="description" label="描述" show-overflow-tooltip />
            <el-table-column prop="ip" label="IP" width="120" />
            <el-table-column prop="createTime" label="时间" width="180" />
          </el-table>
          <el-pagination
            v-model:current-page="logPage"
            :page-size="20"
            :total="logTotal"
            layout="total, prev, pager, next"
            @current-change="loadLogs"
            style="margin-top: 16px; justify-content: flex-end"
          />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="showAddDialog" title="添加配置" width="480px">
      <el-form :model="addForm" label-width="80px">
        <el-form-item label="配置名称">
          <el-input v-model="addForm.configName" placeholder="请输入配置名称" />
        </el-form-item>
        <el-form-item label="配置键">
          <el-input v-model="addForm.configKey" placeholder="请输入配置键" />
        </el-form-item>
        <el-form-item label="配置值">
          <el-input v-model="addForm.configValue" type="textarea" :rows="3" placeholder="请输入配置值" />
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="addForm.configType">
            <el-radio value="N">自定义</el-radio>
            <el-radio value="Y">内置</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="addForm.remark" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddConfig">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getSystemConfigs, addSystemConfig, updateSystemConfig, deleteSystemConfig, getOperationLogs } from '@/api/admin'

const activeTab = ref('config')
const loadingConfigs = ref(false)
const loadingLogs = ref(false)
const configs = ref<any[]>([])
const logs = ref<any[]>([])
const logPage = ref(1)
const logTotal = ref(0)

const editingId = ref<number | null>(null)
const editingValue = ref('')

const showAddDialog = ref(false)
const addForm = reactive({
  configName: '',
  configKey: '',
  configValue: '',
  configType: 'N',
  remark: '',
})

const loadConfigs = async () => {
  loadingConfigs.value = true
  try {
    const res = await getSystemConfigs('')
    configs.value = res.data
  } finally {
    loadingConfigs.value = false
  }
}

const loadLogs = async () => {
  loadingLogs.value = true
  try {
    const res = await getOperationLogs(logPage.value)
    logs.value = res.data.list
    logTotal.value = res.data.total
  } finally {
    loadingLogs.value = false
  }
}

const startEdit = (row: any) => {
  editingId.value = row.id
  editingValue.value = row.configValue || ''
}

const cancelEdit = () => {
  editingId.value = null
  editingValue.value = ''
}

const handleSaveConfig = async (row: any) => {
  try {
    await updateSystemConfig({ id: row.id, configValue: editingValue.value })
    ElMessage.success('保存成功')
    cancelEdit()
    loadConfigs()
  } catch {
    ElMessage.error('保存失败')
  }
}

const handleAddConfig = async () => {
  if (!addForm.configKey) {
    ElMessage.warning('请输入配置键')
    return
  }
  try {
    await addSystemConfig(addForm)
    ElMessage.success('添加成功')
    showAddDialog.value = false
    addForm.configName = ''
    addForm.configKey = ''
    addForm.configValue = ''
    addForm.configType = 'N'
    addForm.remark = ''
    loadConfigs()
  } catch {
    ElMessage.error('添加失败')
  }
}

const handleDeleteConfig = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定删除该配置吗？', '提示', { type: 'warning' })
    await deleteSystemConfig(row.id)
    ElMessage.success('删除成功')
    loadConfigs()
  } catch {
    // cancelled
  }
}

onMounted(() => {
  loadConfigs()
  loadLogs()
})
</script>

<style scoped lang="scss">
.system-admin {
  :deep(.el-tabs__content) {
    padding: 0;
  }
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
