<template>
  <div class="publish-task">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ $t('publish.taskTitle') }}</span>
          <el-button type="primary" @click="openCreateDialog()">
            <el-icon><Plus /></el-icon> {{ $t('publish.createTask') }}
          </el-button>
        </div>
      </template>

      <el-table :data="tasks" v-loading="loading">
        <el-table-column prop="title" :label="$t('publish.articleTitle')" show-overflow-tooltip />
        <el-table-column prop="platform" :label="$t('publish.platform')" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ getPlatformLabel(row.platform) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="accountName" :label="$t('publish.account')" width="120" />
        <el-table-column prop="status" :label="$t('common.status')" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="$t('common.createTime')" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.operation')" width="260" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              type="success"
              link
              @click="handlePublish(row.id)"
            >
              {{ $t('publish.publishNow') }}
            </el-button>
            <el-button
              v-if="row.status === 0"
              type="primary"
              link
              @click="openScheduleDialog(row.id)"
            >
              {{ $t('publish.schedule') }}
            </el-button>
            <el-button type="primary" link @click="openEditDialog(row)">
              {{ $t('common.edit') }}
            </el-button>
            <el-popconfirm
              :title="$t('common.confirmDelete')"
              @confirm="handleDelete(row.id)"
            >
              <template #reference>
                <el-button type="danger" link>{{ $t('common.delete') }}</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @size-change="loadTasks"
        @current-change="loadTasks"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <!-- 新增/编辑任务弹窗 -->
    <el-dialog
      v-model="showDialog"
      :title="isEditing ? $t('publish.editTask') : $t('publish.createTask')"
      width="650px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="$t('publish.account')" prop="platformAccountId">
          <el-select
            v-model="form.platformAccountId"
            :placeholder="$t('publish.selectAccount')"
            style="width: 100%"
          >
            <el-option-group
              v-for="(accounts, platform) in groupedAccounts"
              :key="platform"
              :label="getPlatformLabel(platform)"
            >
              <el-option
                v-for="acc in accounts"
                :key="acc.id"
                :label="acc.accountName"
                :value="acc.id"
              />
            </el-option-group>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('publish.articleTitle')" prop="title">
          <el-input
            v-model="form.title"
            :placeholder="$t('publish.articleTitlePlaceholder')"
          />
        </el-form-item>
        <el-form-item :label="$t('publish.content')" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="8"
            :placeholder="$t('publish.articleContentPlaceholder')"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ $t('common.confirm') }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 定时发布弹窗 -->
    <el-dialog
      v-model="showScheduleDialog"
      :title="$t('publish.schedulePublish')"
      width="400px"
    >
      <el-form label-width="100px">
        <el-form-item :label="$t('publish.publishTime')">
          <el-date-picker
            v-model="scheduleTime"
            type="datetime"
            :placeholder="$t('publish.selectPublishTime')"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showScheduleDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSchedule">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useI18n } from 'vue-i18n'
import {
  getPlatformAccounts,
  getPublishTasks,
  createPublishTask,
  updatePublishTask,
  deletePublishTask,
  publishNow,
  schedulePublish
} from '@/api/publish'

const { t } = useI18n()

// 平台配置
const platformOptions: Record<string, string> = {
  wechat_mp: '微信公众号',
  toutiao: '头条号',
  baijiahao: '百家号'
}

const tasks = ref<any[]>([])
const accounts = ref<any[]>([])
const loading = ref(false)
const showDialog = ref(false)
const showScheduleDialog = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const scheduleTaskId = ref(0)
const scheduleTime = ref<Date | null>(null)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const formRef = ref<FormInstance>()

const form = reactive({
  platformAccountId: null as number | null,
  title: '',
  content: ''
})

const rules: FormRules = {
  platformAccountId: [{ required: true, message: () => t('publish.validateSelectAccount'), trigger: 'change' }],
  title: [{ required: true, message: () => t('publish.validateTitle'), trigger: 'blur' }],
  content: [{ required: true, message: () => t('publish.validateContent'), trigger: 'blur' }]
}

// 按平台分组的账号
const groupedAccounts = computed(() => {
  const grouped: Record<string, any[]> = {}
  for (const account of accounts.value) {
    if (!grouped[account.platform]) {
      grouped[account.platform] = []
    }
    grouped[account.platform].push(account)
  }
  return grouped
})

// 加载账号列表
const loadAccounts = async () => {
  try {
    const res = await getPlatformAccounts()
    accounts.value = res.data || []
  } catch {
    // ignore
  }
}

// 加载任务列表
const loadTasks = async () => {
  loading.value = true
  try {
    const res = await getPublishTasks(currentPage.value, pageSize.value)
    tasks.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

// 打开新增弹窗
const openCreateDialog = () => {
  isEditing.value = false
  editingId.value = null
  showDialog.value = true
}

// 打开编辑弹窗
const openEditDialog = (task: any) => {
  isEditing.value = true
  editingId.value = task.id
  form.platformAccountId = task.platformAccountId
  form.title = task.title
  form.content = task.content
  showDialog.value = true
}

// 提交表单
const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEditing.value && editingId.value) {
      await updatePublishTask({ id: editingId.value, ...form })
      ElMessage.success(t('common.updateSuccess'))
    } else {
      await createPublishTask(form)
      ElMessage.success(t('publish.createSuccess'))
    }
    showDialog.value = false
    loadTasks()
  } catch {
    ElMessage.error(isEditing.value ? t('common.updateFailed') : t('publish.createFailed'))
  } finally {
    submitting.value = false
  }
}

// 删除任务
const handleDelete = async (id: number) => {
  try {
    await deletePublishTask(id)
    ElMessage.success(t('common.deleteSuccess'))
    loadTasks()
  } catch {
    ElMessage.error(t('common.deleteFailed'))
  }
}

// 发布
const handlePublish = async (id: number) => {
  try {
    await publishNow(id)
    ElMessage.success(t('publish.publishTriggered'))
    // 轮询刷新状态
    let attempts = 0
    const maxAttempts = 10
    const poll = async () => {
      await new Promise(resolve => setTimeout(resolve, 2000))
      await loadTasks()
      attempts++
      if (attempts < maxAttempts) {
        poll()
      }
    }
    poll()
  } catch {
    ElMessage.error(t('publish.publishFailed'))
  }
}

// 打开定时发布弹窗
const openScheduleDialog = (id: number) => {
  scheduleTaskId.value = id
  scheduleTime.value = null
  showScheduleDialog.value = true
}

// 确认定时发布
const handleSchedule = async () => {
  if (!scheduleTime.value) {
    ElMessage.warning(t('publish.selectPublishTimeError'))
    return
  }
  try {
    await schedulePublish(scheduleTaskId.value, scheduleTime.value.toISOString())
    ElMessage.success(t('publish.scheduleSuccess'))
    showScheduleDialog.value = false
    loadTasks()
  } catch {
    ElMessage.error(t('publish.scheduleFailed'))
  }
}

// 重置表单
const resetForm = () => {
  form.platformAccountId = null
  form.title = ''
  form.content = ''
  editingId.value = null
  isEditing.value = false
  formRef.value?.clearValidate()
}

// 工具函数
const getPlatformLabel = (platform: string) => {
  return platformOptions[platform] || platform
}

const getStatusType = (status: number) => {
  const map: Record<number, '' | 'success' | 'warning' | 'danger' | 'info'> = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'danger'
  }
  return map[status] || 'info'
}

const getStatusLabel = (status: number) => {
  const map: Record<number, string> = {
    0: t('publish.statusDraft'),
    1: t('publish.statusPublishing'),
    2: t('publish.statusPublished'),
    3: t('publish.statusFailed')
  }
  return map[status] || ''
}

const formatTime = (time: string) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

onMounted(() => {
  loadAccounts()
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
