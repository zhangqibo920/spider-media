<template>
  <div class="task-scheduler">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ $t('scheduler.title') }}</span>
          <el-button type="primary" @click="showCreateDialog = true">
            <el-icon><Plus /></el-icon> {{ $t('scheduler.createTask') }}
          </el-button>
        </div>
      </template>

      <el-table :data="tasks" v-loading="loading" stripe>
        <el-table-column prop="taskName" :label="$t('scheduler.taskName')" />
        <el-table-column prop="taskType" :label="$t('scheduler.taskType')" width="120">
          <template #default="{ row }">
            <el-tag>{{ row.taskType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="cronExpression" :label="$t('scheduler.cronExpression')" width="150" />
        <el-table-column prop="status" :label="$t('common.status')" width="80">
          <template #default="{ row }">
            <DictTag dict-type="ts_task_status" :value="row.status" />
          </template>
        </el-table-column>
        <el-table-column prop="runCount" :label="$t('scheduler.executeCount')" width="80" />
        <el-table-column prop="failCount" :label="$t('scheduler.failCount')" width="80" />
        <el-table-column :label="$t('common.operation')" width="150">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="success" link @click="handleEnable(row.id)">{{ $t('scheduler.enable') }}</el-button>
            <el-button v-if="row.status === 1" type="warning" link @click="handleDisable(row.id)">{{ $t('scheduler.disable') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showCreateDialog" :title="$t('scheduler.createScheduledTask')" width="500px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="$t('scheduler.taskName')" prop="taskName">
          <el-input v-model="form.taskName" :placeholder="$t('scheduler.taskNamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('scheduler.taskType')" prop="taskType">
          <el-select v-model="form.taskType" :placeholder="$t('scheduler.selectType')">
            <el-option :label="$t('scheduler.taskTypeHotTopic')" value="HOT_TOPIC" />
            <el-option :label="$t('scheduler.taskTypeAICreation')" value="AI_CREATION" />
            <el-option :label="$t('scheduler.taskTypePublish')" value="PUBLISH" />
            <el-option :label="$t('scheduler.taskTypeDataCollect')" value="DATA_COLLECT" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('scheduler.cronExpression')" prop="cronExpression">
          <el-input v-model="form.cronExpression" :placeholder="$t('scheduler.cronPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleCreate">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { getScheduledTasks, createScheduledTask, enableTask, disableTask } from '@/api/scheduler'

const { t } = useI18n()

const loading = ref(false)
const showCreateDialog = ref(false)
const tasks = ref<any[]>([])
const formRef = ref<FormInstance>()

const form = reactive({
  taskName: '',
  taskType: '',
  cronExpression: '',
})

const rules = {
  taskName: [{ required: true, message: t('scheduler.taskNameRequired'), trigger: 'blur' }],
  taskType: [{ required: true, message: t('scheduler.taskTypeRequired'), trigger: 'change' }],
  cronExpression: [{ required: true, message: t('scheduler.cronRequired'), trigger: 'blur' }],
}

const loadTasks = async () => {
  loading.value = true
  try {
    const res = await getScheduledTasks()
    tasks.value = res.data.list
  } finally {
    loading.value = false
  }
}

const handleCreate = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    await createScheduledTask(form)
    ElMessage.success(t('scheduler.createSuccess'))
    showCreateDialog.value = false
    loadTasks()
  } catch {
    ElMessage.error(t('scheduler.createFailed'))
  }
}

const resetForm = () => {
  form.taskName = ''
  form.taskType = ''
  form.cronExpression = ''
}

const handleEnable = async (id: number) => {
  try {
    await enableTask(id)
    ElMessage.success(t('scheduler.enableSuccess'))
    loadTasks()
  } catch {
    ElMessage.error(t('common.operationFailed'))
  }
}

const handleDisable = async (id: number) => {
  try {
    await disableTask(id)
    ElMessage.success(t('scheduler.disableSuccess'))
    loadTasks()
  } catch {
    ElMessage.error(t('common.operationFailed'))
  }
}

onMounted(() => {
  loadTasks()
})
</script>

<style scoped lang="scss">
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
