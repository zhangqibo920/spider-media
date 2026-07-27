<template>
  <div class="system-admin">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="系统配置" name="config">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>系统配置</span>
              <el-button type="primary" @click="showAddConfigDialog = true">
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
                  v-show="editingConfigId === row.id"
                  v-model="editingConfigValue"
                  size="small"
                  @keyup.enter="handleSaveConfig(row)"
                />
                <span v-show="editingConfigId !== row.id" @click="startEditConfig(row)" style="cursor: pointer">
                  {{ row.configValue || '-' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="configType" label="类型" width="80">
              <template #default="{ row }">
                <DictTag dict-type="sys_config_type" :value="row.configType" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button v-show="editingConfigId === row.id" type="success" link @click="handleSaveConfig(row)">
                  保存
                </el-button>
                <el-button v-show="editingConfigId === row.id" type="info" link @click="cancelEditConfig">
                  取消
                </el-button>
                <el-button v-show="editingConfigId !== row.id" type="primary" link @click="startEditConfig(row)">编辑</el-button>
                <el-button v-show="editingConfigId !== row.id" type="danger" link @click="handleDeleteConfig(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="用户管理" name="users">
        <el-card>
          <template #header>
            <span>用户管理</span>
          </template>
          <el-table :data="users" v-loading="loadingUsers" stripe>
            <el-table-column prop="userId" label="ID" width="60" />
            <el-table-column prop="userName" label="用户名" width="120" />
            <el-table-column prop="nickName" label="昵称" width="120" />
            <el-table-column prop="email" label="邮箱" />
            <el-table-column prop="role" label="角色" width="100">
              <template #default="{ row }">
                <DictTag dict-type="sys_user_role" :value="row.role" size="small" />
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <DictTag dict-type="sys_user_status" :value="row.status" size="small" />
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button type="primary" link @click="startEditUser(row)">编辑</el-button>
                <el-button type="warning" link @click="openChangePassword(row)">改密</el-button>
                <el-button type="danger" link @click="handleDeleteUser(row)" :disabled="row.userId === 1">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="操作日志" name="logs">
        <el-card>
          <el-table :data="logs" v-loading="loadingLogs" stripe>
            <el-table-column type="index" label="#" width="50" :index="(index: number) => (logPage - 1) * logSize + index + 1" />
            <el-table-column prop="username" label="用户" width="100" />
            <el-table-column prop="module" label="模块" width="100" />
            <el-table-column prop="action" label="操作" width="100" />
            <el-table-column prop="description" label="描述" show-overflow-tooltip />
            <el-table-column prop="ip" label="IP" width="120" />
            <el-table-column prop="createTime" label="时间" width="180" />
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
      </el-tab-pane>

      <el-tab-pane label="模型管理" name="models">
        <ModelManageView />
      </el-tab-pane>

      <el-tab-pane label="字典管理" name="dict">
        <DictManageView />
      </el-tab-pane>

      <el-tab-pane label="菜单管理" name="menus">
        <MenuManageView />
      </el-tab-pane>

      <el-tab-pane label="角色管理" name="roles">
        <RoleManageView />
      </el-tab-pane>
    </el-tabs>

    <!-- 添加配置对话框 -->
    <el-dialog v-model="showAddConfigDialog" title="添加配置" width="480px">
      <el-form :model="addConfigForm" label-width="80px">
        <el-form-item label="配置名称">
          <el-input v-model="addConfigForm.configName" placeholder="请输入配置名称" />
        </el-form-item>
        <el-form-item label="配置键">
          <el-input v-model="addConfigForm.configKey" placeholder="请输入配置键" />
        </el-form-item>
        <el-form-item label="配置值">
          <el-input v-model="addConfigForm.configValue" type="textarea" :rows="3" placeholder="请输入配置值" />
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="addConfigForm.configType">
            <el-radio value="N">自定义</el-radio>
            <el-radio value="Y">内置</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddConfigDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddConfig">确定</el-button>
      </template>
    </el-dialog>

    <!-- 编辑用户对话框 -->
    <el-dialog v-model="showEditUserDialog" title="编辑用户" width="480px">
      <el-form :model="editUserForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="editUserForm.userName" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="editUserForm.nickName" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editUserForm.email" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editUserForm.role">
            <el-option v-for="opt in roleOptions" :key="opt.roleKey" :label="opt.roleName" :value="opt.roleKey" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="editUserForm.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditUserDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveUser">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog v-model="showChangePasswordDialog" title="重置密码" width="480px" @closed="resetPasswordForm">
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
        <el-form-item label="当前用户">
          <el-input :model-value="passwordForm.userName" disabled />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showChangePasswordDialog = false">取消</el-button>
        <el-button type="primary" :loading="changingPassword" @click="handleChangePassword">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import {
  getSystemConfigs, addSystemConfig, updateSystemConfig, deleteSystemConfig,
  getUsers, updateUser, deleteUser, getOperationLogs, resetPassword
} from '@/api/admin'
import { getRoles } from '@/api/role'
import DictTag from '@/components/DictTag.vue'
import ModelManageView from './ModelManageView.vue'
import DictManageView from './DictManageView.vue'
import MenuManageView from './MenuManageView.vue'
import RoleManageView from './RoleManageView.vue'

const activeTab = ref('config')
const loadingConfigs = ref(false)
const loadingUsers = ref(false)
const loadingLogs = ref(false)
const configs = ref<any[]>([])
const users = ref<any[]>([])
const logs = ref<any[]>([])
const logPage = ref(1)
const logSize = ref(10)
const logTotal = ref(0)

const editingConfigId = ref<number | null>(null)
const editingConfigValue = ref('')
const showAddConfigDialog = ref(false)
const addConfigForm = reactive({ configName: '', configKey: '', configValue: '', configType: 'N' })

const showEditUserDialog = ref(false)
const editUserForm = reactive({ userId: 0, userName: '', nickName: '', email: '', role: 'USER', status: '0' })

const roleOptions = ref<any[]>([])

const loadRoleOptions = async () => {
  try {
    const res = await getRoles()
    roleOptions.value = (res.data || []).filter((r: any) => r.status === '0')
  } catch {}
}

// ========== 修改密码 ==========
const showChangePasswordDialog = ref(false)
const changingPassword = ref(false)
const passwordFormRef = ref<FormInstance>()
const passwordForm = reactive({
  userId: 0,
  userName: '',
  newPassword: '',
  confirmPassword: '',
})

const validateConfirmPassword = (_rule: any, value: string, callback: Function) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

const openChangePassword = (row: any) => {
  passwordForm.userId = row.userId
  passwordForm.userName = row.userName
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  showChangePasswordDialog.value = true
}

const resetPasswordForm = () => {
  passwordForm.userId = 0
  passwordForm.userName = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

const handleChangePassword = async () => {
  const valid = await passwordFormRef.value?.validate().catch(() => false)
  if (!valid) return

  changingPassword.value = true
  try {
    await resetPassword(passwordForm.userId, passwordForm.newPassword)
    ElMessage.success('密码重置成功')
    showChangePasswordDialog.value = false
  } catch {
    ElMessage.error('密码重置失败')
  } finally {
    changingPassword.value = false
  }
}

// ========== 配置管理 ==========
const loadConfigs = async () => {
  loadingConfigs.value = true
  try {
    const res = await getSystemConfigs('')
    configs.value = res.data
  } finally { loadingConfigs.value = false }
}

const loadUsers = async () => {
  loadingUsers.value = true
  try {
    const res = await getUsers()
    users.value = res.data
  } finally { loadingUsers.value = false }
}

const loadLogs = async () => {
  loadingLogs.value = true
  try {
    const res = await getOperationLogs(logPage.value, logSize.value)
    logs.value = res.data.list
    logTotal.value = res.data.total
  } finally { loadingLogs.value = false }
}

const handleLogSizeChange = () => {
  logPage.value = 1
  loadLogs()
}

const startEditConfig = (row: any) => { editingConfigId.value = row.id; editingConfigValue.value = row.configValue || '' }
const cancelEditConfig = () => { editingConfigId.value = null; editingConfigValue.value = '' }
const handleSaveConfig = async (row: any) => {
  try {
    await updateSystemConfig({ id: row.id, configValue: editingConfigValue.value })
    ElMessage.success('保存成功')
    cancelEditConfig()
    loadConfigs()
  } catch { ElMessage.error('保存失败') }
}
const handleAddConfig = async () => {
  if (!addConfigForm.configKey) { ElMessage.warning('请输入配置键'); return }
  try {
    await addSystemConfig(addConfigForm)
    ElMessage.success('添加成功')
    showAddConfigDialog.value = false
    addConfigForm.configName = ''; addConfigForm.configKey = ''; addConfigForm.configValue = ''; addConfigForm.configType = 'N'
    loadConfigs()
  } catch { ElMessage.error('添加失败') }
}
const handleDeleteConfig = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定删除该配置吗？', '提示', { type: 'warning' })
    await deleteSystemConfig(row.id)
    ElMessage.success('删除成功')
    loadConfigs()
  } catch {}
}

// ========== 用户管理 ==========
const startEditUser = (row: any) => {
  Object.assign(editUserForm, { userId: row.userId, userName: row.userName, nickName: row.nickName, email: row.email, role: row.role, status: row.status })
  showEditUserDialog.value = true
}
const handleSaveUser = async () => {
  try {
    await updateUser(editUserForm)
    ElMessage.success('保存成功')
    showEditUserDialog.value = false
    loadUsers()
  } catch { ElMessage.error('保存失败') }
}
const handleDeleteUser = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定删除该用户吗？', '提示', { type: 'warning' })
    await deleteUser(row.userId)
    ElMessage.success('删除成功')
    loadUsers()
  } catch {}
}

onMounted(() => { loadConfigs(); loadUsers(); loadLogs(); loadRoleOptions() })
</script>

<style scoped lang="scss">
.system-admin { :deep(.el-tabs__content) { padding: 0; } }
.card-header { display: flex; align-items: center; justify-content: space-between; }
</style>
