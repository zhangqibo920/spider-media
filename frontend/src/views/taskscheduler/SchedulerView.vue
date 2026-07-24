<template>
  <div class="task-scheduler">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>定时任务管理</span>
          <el-button type="primary" @click="showCreateDialog = true">
            <el-icon><Plus /></el-icon> 创建任务
          </el-button>
        </div>
      </template>

      <el-table :data="tasks" v-loading="loading" stripe>
        <el-table-column prop="taskName" label="任务名称" />
        <el-table-column prop="taskType" label="任务类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ row.taskType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="cronExpression" label="Cron表达式" width="150" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '运行中' : '已停止' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="runCount" label="执行次数" width="80" />
        <el-table-column prop="failCount" label="失败次数" width="80" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="success" link @click="handleEnable(row.id)">启用</el-button>
            <el-button v-if="row.status === 1" type="warning" link @click="handleDisable(row.id)">停用</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Create Task Dialog -->
    <el-dialog v-model="showCreateDialog" title="创建定时任务" width="500px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="任务名称" prop="taskName">
          <el-input v-model="form.taskName" placeholder="输入任务名称" />
        </el-form-item>
        <el-form-item label="任务类型" prop="taskType">
          <el-select v-model="form.taskType" placeholder="选择类型">
            <el-option label="热点抓取" value="HOT_TOPIC" />
            <el-option label="AI创作" value="AI_CREATION" />
            <el-option label="内容发布" value="PUBLISH" />
            <el-option label="数据采集" value="DATA_COLLECT" />
          </el-select>
        </el-form-item>
        <el-form-item label="Cron表达式" prop="cronExpression">
          <el-input v-model="form.cronExpression" placeholder="例: 0 0/30 * * * ?" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { getScheduledTasks, createScheduledTask, enableTask, disableTask } from '@/api/scheduler'
import type { ScheduledTask } from '@/types'

const loading = ref(false)
const showCreateDialog = ref(false)
const tasks = ref<ScheduledTask[]>([])
const formRef = ref<FormInstance>()

const form = reactive({
  taskName: '',
  taskType: '',
  cronExpression: '',
})

const rules = {
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  taskType: [{ required: true, message: '请选择任务类型', trigger: 'change' }],
  cronExpression: [{ required: true, message: '请输入Cron表达式', trigger: 'blur' }],
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
    ElMessage.success('创建成功')
    showCreateDialog.value = false
    loadTasks()
  } catch {
    ElMessage.error('创建失败')
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
    ElMessage.success('已启用')
    loadTasks()
  } catch {
    ElMessage.error('操作失败')
  }
}

const handleDisable = async (id: number) => {
  try {
    await disableTask(id)
    ElMessage.success('已停用')
    loadTasks()
  } catch {
    ElMessage.error('操作失败')
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
