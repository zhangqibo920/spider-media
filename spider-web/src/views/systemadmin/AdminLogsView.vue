<template>
  <el-card>
    <el-table :data="logs" v-loading="loadingLogs" stripe>
      <el-table-column type="index" label="#" width="50" :index="(index: number) => (logPage - 1) * logSize + index + 1" />
      <el-table-column prop="username" :label="$t('sysAdmin.username')" width="100" />
      <el-table-column prop="module" :label="$t('sysAdmin.module')" width="100" />
      <el-table-column prop="action" :label="$t('common.operation')" width="100" />
      <el-table-column prop="description" :label="$t('sysAdmin.description')" show-overflow-tooltip />
      <el-table-column prop="ip" label="IP" width="120" />
      <el-table-column prop="createTime" :label="$t('common.createTime')" width="180" />
    </el-table>
    <el-pagination
      v-model:current-page="logPage"
      v-model:page-size="logSize"
      :page-sizes="[10, 20, 50, 100]"
      :total="logTotal"
      layout="total, sizes, prev, pager, next"
      @current-change="loadLogs"
      @size-change="handleLogSizeChange"
      style="margin-top: 16px; justify-content: flex-end"
    />
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { getOperationLogs } from '@/api/admin'

const { t } = useI18n()

const loadingLogs = ref(false)
const logs = ref<any[]>([])
const logPage = ref(1)
const logSize = ref(10)
const logTotal = ref(0)

const loadLogs = async () => {
  loadingLogs.value = true
  try {
    const res = await getOperationLogs(logPage.value, logSize.value)
    logs.value = res.data.list
    logTotal.value = res.data.total
  } catch {
    ElMessage.error(t('common.operationFailed'))
    loadingLogs.value = false
  } finally { loadingLogs.value = false }
}

const handleLogSizeChange = () => { logPage.value = 1; loadLogs() }

onMounted(() => { loadLogs() })
</script>
