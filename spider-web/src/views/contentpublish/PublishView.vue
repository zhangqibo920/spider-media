<template>
  <div class="content-publish">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>{{ $t('publish.title') }}</span>
              <el-button type="primary" size="small" @click="showAddAccountDialog = true">
                <el-icon><Plus /></el-icon> {{ $t('publish.addAccount') }}
              </el-button>
            </div>
          </template>
          <el-table :data="accounts" v-loading="loadingAccounts">
            <el-table-column prop="platform" :label="$t('publish.platform')" width="100">
              <template #default="{ row }"><el-tag>{{ row.platform }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="accountName" :label="$t('publish.accountName')" />
            <el-table-column prop="status" :label="$t('common.status')" width="80">
              <template #default="{ row }">
                <DictTag dict-type="pb_account_status" :value="row.status" size="small" />
              </template>
            </el-table-column>
            <el-table-column :label="$t('common.operation')" width="80">
              <template #default="{ row }">
                <el-popconfirm :title="$t('common.confirmDelete')" @confirm="handleDeleteAccount(row.id)">
                  <template #reference><el-button type="danger" link>{{ $t('common.delete') }}</el-button></template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>{{ $t('publish.taskTitle') }}</span>
              <el-button type="primary" size="small" @click="showCreateTaskDialog = true">
                <el-icon><Plus /></el-icon> {{ $t('publish.createTask') }}
              </el-button>
            </div>
          </template>
          <el-table :data="tasks" v-loading="loadingTasks">
            <el-table-column prop="title" :label="$t('publish.articleTitle')" show-overflow-tooltip />
            <el-table-column prop="platform" :label="$t('publish.platform')" width="80">
              <template #default="{ row }"><el-tag size="small">{{ row.platform }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="status" :label="$t('common.status')" width="80">
              <template #default="{ row }">
                <DictTag dict-type="pb_publish_status" :value="row.status" size="small" />
              </template>
            </el-table-column>
            <el-table-column :label="$t('common.operation')" width="150">
              <template #default="{ row }">
                <el-button v-if="row.status === 0" type="success" link @click="handlePublish(row.id)">{{ $t('publish.publishNow') }}</el-button>
                <el-button v-if="row.status === 0" type="primary" link @click="openScheduleDialog(row.id)">{{ $t('publish.schedule') }}</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="showAddAccountDialog" :title="$t('publish.addAccount')" width="500px" @closed="resetAccountForm">
      <el-form ref="accountFormRef" :model="accountForm" :rules="accountRules" label-width="80px">
        <el-form-item :label="$t('publish.platform')" prop="platform">
          <el-select v-model="accountForm.platform" :placeholder="$t('publish.selectPlatform')">
            <el-option v-for="p in platformDict" :key="p.dictValue" :label="p.dictLabel" :value="p.dictValue" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('publish.accountName')" prop="accountName">
          <el-input v-model="accountForm.accountName" :placeholder="$t('publish.accountNamePlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddAccountDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleAddAccount">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showCreateTaskDialog" :title="$t('publish.createTask')" width="600px" @closed="resetTaskForm">
      <el-form ref="taskFormRef" :model="taskForm" :rules="taskRules" label-width="80px">
        <el-form-item :label="$t('publish.account')" prop="platformAccountId">
          <el-select v-model="taskForm.platformAccountId" :placeholder="$t('publish.selectAccount')">
            <el-option v-for="acc in accounts" :key="acc.id" :label="acc.accountName" :value="acc.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('publish.articleTitle')" prop="title">
          <el-input v-model="taskForm.title" :placeholder="$t('publish.articleTitlePlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('publish.content')" prop="content">
          <el-input v-model="taskForm.content" type="textarea" :rows="6" :placeholder="$t('publish.articleContentPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateTaskDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleCreateTask">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showScheduleDialog" :title="$t('publish.schedulePublish')" width="400px">
      <el-form label-width="80px">
        <el-form-item :label="$t('publish.publishTime')">
          <el-date-picker v-model="scheduleTime" type="datetime" :placeholder="$t('publish.selectPublishTime')" style="width: 100%" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useI18n } from 'vue-i18n'
import {
  getPlatformAccounts, addPlatformAccount, deletePlatformAccount,
  getPublishTasks, createPublishTask, publishNow, schedulePublish
} from '@/api/publish'
import { useDict } from '@/composables/useDict'
import DictTag from '@/components/DictTag.vue'

const { t } = useI18n()

const { dict: platformDict } = useDict('publish_platform')

const loadingAccounts = ref(false)
const loadingTasks = ref(false)
const showAddAccountDialog = ref(false)
const showCreateTaskDialog = ref(false)
const showScheduleDialog = ref(false)
const accounts = ref<any[]>([])
const tasks = ref<any[]>([])
const accountFormRef = ref<FormInstance>()
const taskFormRef = ref<FormInstance>()
const scheduleTaskId = ref(0)
const scheduleTime = ref<Date | null>(null)

const accountForm = reactive({ platform: '', accountName: '' })
const accountRules = {
  platform: [{ required: true, message: t('publish.validateSelectPlatform'), trigger: 'change' }],
  accountName: [{ required: true, message: t('publish.validateAccountName'), trigger: 'blur' }],
}
const taskForm = reactive({ platformAccountId: null as number | null, title: '', content: '' })
const taskRules = {
  platformAccountId: [{ required: true, message: t('publish.validateSelectAccount'), trigger: 'change' }],
  title: [{ required: true, message: t('publish.validateTitle'), trigger: 'blur' }],
  content: [{ required: true, message: t('publish.validateContent'), trigger: 'blur' }],
}

const loadAccounts = async () => {
  loadingAccounts.value = true
  try { const res = await getPlatformAccounts(); accounts.value = res.data } finally { loadingAccounts.value = false }
}
const loadTasks = async () => {
  loadingTasks.value = true
  try { const res = await getPublishTasks(); tasks.value = res.data.list } finally { loadingTasks.value = false }
}

const handleAddAccount = async () => {
  const valid = await accountFormRef.value?.validate().catch(() => false)
  if (!valid) return
  try { await addPlatformAccount(accountForm); ElMessage.success(t('publish.addSuccess')); showAddAccountDialog.value = false; loadAccounts() } catch { ElMessage.error(t('publish.addFailed')) }
}
const handleDeleteAccount = async (id: number) => {
  try { await deletePlatformAccount(id); ElMessage.success(t('common.deleteSuccess')); loadAccounts() } catch { ElMessage.error(t('common.deleteFailed')) }
}
const handleCreateTask = async () => {
  const valid = await taskFormRef.value?.validate().catch(() => false)
  if (!valid) return
  try { await createPublishTask(taskForm); ElMessage.success(t('publish.createSuccess')); showCreateTaskDialog.value = false; loadTasks() } catch { ElMessage.error(t('publish.createFailed')) }
}
const handlePublish = async (id: number) => {
  try {
    await publishNow(id)
    ElMessage.success(t('publish.publishTriggered'))
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
  } catch { ElMessage.error(t('publish.publishFailed')) }
}
const openScheduleDialog = (id: number) => { scheduleTaskId.value = id; scheduleTime.value = null; showScheduleDialog.value = true }
const handleSchedule = async () => {
  if (!scheduleTime.value) { ElMessage.warning(t('publish.selectPublishTimeError')); return }
  try {
    await schedulePublish(scheduleTaskId.value, scheduleTime.value.toISOString())
    ElMessage.success(t('publish.scheduleSuccess'))
    showScheduleDialog.value = false
    loadTasks()
  } catch { ElMessage.error(t('publish.scheduleFailed')) }
}

const resetAccountForm = () => { accountForm.platform = ''; accountForm.accountName = '' }
const resetTaskForm = () => { taskForm.platformAccountId = null; taskForm.title = ''; taskForm.content = '' }
onMounted(() => { loadAccounts(); loadTasks() })
</script>

<style scoped lang="scss">
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
