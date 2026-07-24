<template>
  <div class="content-publish">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>发布账号管理</span>
              <el-button type="primary" size="small" @click="showAddAccountDialog = true">
                <el-icon><Plus /></el-icon> 添加账号
              </el-button>
            </div>
          </template>
          <el-table :data="accounts" v-loading="loadingAccounts">
            <el-table-column prop="platform" label="平台" width="100">
              <template #default="{ row }"><el-tag>{{ row.platform }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="accountName" label="账号名称" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                  {{ row.status === 1 ? '在线' : '离线' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="{ row }">
                <el-popconfirm title="确定删除？" @confirm="handleDeleteAccount(row.id)">
                  <template #reference><el-button type="danger" link>删除</el-button></template>
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
              <span>发布任务</span>
              <el-button type="primary" size="small" @click="showCreateTaskDialog = true">
                <el-icon><Plus /></el-icon> 创建任务
              </el-button>
            </div>
          </template>
          <el-table :data="tasks" v-loading="loadingTasks">
            <el-table-column prop="title" label="标题" show-overflow-tooltip />
            <el-table-column prop="platform" label="平台" width="80">
              <template #default="{ row }"><el-tag size="small">{{ row.platform }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button v-if="row.status === 0" type="success" link @click="handlePublish(row.id)">发布</el-button>
                <el-button v-if="row.status === 0" type="primary" link @click="openScheduleDialog(row.id)">定时</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="showAddAccountDialog" title="添加发布账号" width="500px" @closed="resetAccountForm">
      <el-form ref="accountFormRef" :model="accountForm" :rules="accountRules" label-width="80px">
        <el-form-item label="平台" prop="platform">
          <el-select v-model="accountForm.platform" placeholder="选择平台">
            <el-option label="微信公众号" value="wechat" />
            <el-option label="今日头条" value="toutiao" />
            <el-option label="百家号" value="baijia" />
            <el-option label="小红书" value="xiaohongshu" />
          </el-select>
        </el-form-item>
        <el-form-item label="账号名称" prop="accountName">
          <el-input v-model="accountForm.accountName" placeholder="输入账号名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddAccountDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddAccount">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showCreateTaskDialog" title="创建发布任务" width="600px" @closed="resetTaskForm">
      <el-form ref="taskFormRef" :model="taskForm" :rules="taskRules" label-width="80px">
        <el-form-item label="账号" prop="platformAccountId">
          <el-select v-model="taskForm.platformAccountId" placeholder="选择发布账号">
            <el-option v-for="acc in accounts" :key="acc.id" :label="acc.accountName" :value="acc.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="taskForm.title" placeholder="文章标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="taskForm.content" type="textarea" :rows="6" placeholder="文章内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateTaskDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreateTask">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showScheduleDialog" title="定时发布" width="400px">
      <el-form label-width="80px">
        <el-form-item label="发布时间">
          <el-date-picker v-model="scheduleTime" type="datetime" placeholder="选择发布时间" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showScheduleDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSchedule">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import {
  getPlatformAccounts, addPlatformAccount, deletePlatformAccount,
  getPublishTasks, createPublishTask, publishNow, schedulePublish
} from '@/api/publish'

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
  platform: [{ required: true, message: '请选择平台', trigger: 'change' }],
  accountName: [{ required: true, message: '请输入账号名称', trigger: 'blur' }],
}
const taskForm = reactive({ platformAccountId: null as number | null, title: '', content: '' })
const taskRules = {
  platformAccountId: [{ required: true, message: '请选择发布账号', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
}

const getStatusType = (s: number) => ({ 0: 'info', 1: 'warning', 2: 'success', 3: 'primary', 4: 'danger' }[s] || 'info') as any
const getStatusText = (s: number) => ({ 0: '草稿', 1: '发布中', 2: '已发布', 3: '定时中', 4: '失败' }[s] || '未知')

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
  try { await addPlatformAccount(accountForm); ElMessage.success('添加成功'); showAddAccountDialog.value = false; loadAccounts() } catch { ElMessage.error('添加失败') }
}
const handleDeleteAccount = async (id: number) => {
  try { await deletePlatformAccount(id); ElMessage.success('删除成功'); loadAccounts() } catch { ElMessage.error('删除失败') }
}
const handleCreateTask = async () => {
  const valid = await taskFormRef.value?.validate().catch(() => false)
  if (!valid) return
  try { await createPublishTask(taskForm); ElMessage.success('创建成功'); showCreateTaskDialog.value = false; loadTasks() } catch { ElMessage.error('创建失败') }
}
const handlePublish = async (id: number) => {
  try { await publishNow(id); ElMessage.success('发布任务已触发'); loadTasks() } catch { ElMessage.error('发布失败') }
}
const openScheduleDialog = (id: number) => { scheduleTaskId.value = id; scheduleTime.value = null; showScheduleDialog.value = true }
const handleSchedule = async () => {
  if (!scheduleTime.value) { ElMessage.warning('请选择发布时间'); return }
  try {
    await schedulePublish(scheduleTaskId.value, scheduleTime.value.toISOString())
    ElMessage.success('定时发布设置成功')
    showScheduleDialog.value = false
    loadTasks()
  } catch { ElMessage.error('设置失败') }
}

const resetAccountForm = () => { accountForm.platform = ''; accountForm.accountName = '' }
const resetTaskForm = () => { taskForm.platformAccountId = null; taskForm.title = ''; taskForm.content = '' }
onMounted(() => { loadAccounts(); loadTasks() })
</script>

<style scoped lang="scss">
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
