<template>
  <div class="system-admin">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="系统配置" name="config">
        <el-card>
          <el-table :data="configs" v-loading="loadingConfigs">
            <el-table-column prop="configKey" label="配置项" />
            <el-table-column prop="configValue" label="配置值">
              <template #default="{ row }">
                <el-input
                  v-if="editingKey === row.configKey"
                  v-model="editingValue"
                  size="small"
                  @keyup.enter="handleSaveConfig(row.configKey)"
                />
                <span v-else @click="startEdit(row)">{{ row.configValue }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button v-if="editingKey === row.configKey" type="success" link @click="handleSaveConfig(row.configKey)">
                  保存
                </el-button>
                <el-button v-else type="primary" link @click="startEdit(row)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="操作日志" name="logs">
        <el-card>
          <el-table :data="logs" v-loading="loadingLogs">
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSystemConfigs, updateSystemConfig, getOperationLogs } from '@/api/admin'

const activeTab = ref('config')
const loadingConfigs = ref(false)
const loadingLogs = ref(false)
const configs = ref<any[]>([])
const logs = ref<any[]>([])
const logPage = ref(1)
const logTotal = ref(0)

const editingKey = ref('')
const editingValue = ref('')

const loadConfigs = async () => {
  loadingConfigs.value = true
  try {
    const res = await getSystemConfigs('general')
    configs.value = res.data
  } finally {
    loadingConfigs.value = false
  }
}

const loadLogs = async () => {
  loadingLogs.value = true
  try {
    const res = await getOperationLogs(logPage.value)
    logs.value = res.data.records
    logTotal.value = res.data.total
  } finally {
    loadingLogs.value = false
  }
}

const startEdit = (row: any) => {
  editingKey.value = row.configKey
  editingValue.value = row.configValue
}

const handleSaveConfig = async (key: string) => {
  try {
    await updateSystemConfig(key, editingValue.value)
    ElMessage.success('保存成功')
    editingKey.value = ''
    loadConfigs()
  } catch {
    ElMessage.error('保存失败')
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
</style>
