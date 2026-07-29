<template>
  <el-card>
    <template #header><span>{{ $t('sysAdmin.userManagement') }}</span></template>
    <el-table :data="users" v-loading="loadingUsers" stripe>
      <el-table-column prop="userId" label="ID" width="60" />
      <el-table-column prop="userName" :label="$t('sysAdmin.username')" width="120" />
      <el-table-column prop="nickName" :label="$t('sysAdmin.nickname')" width="120" />
      <el-table-column prop="email" :label="$t('sysAdmin.email')" />
      <el-table-column prop="role" :label="$t('sysAdmin.role')" width="100">
        <template #default="{ row }">
          <DictTag dict-type="sys_user_role" :value="row.role" size="small" />
        </template>
      </el-table-column>
      <el-table-column prop="status" :label="$t('common.status')" width="80">
        <template #default="{ row }">
          <DictTag dict-type="sys_user_status" :value="row.status" size="small" />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" :label="$t('common.createTime')" width="180" />
      <el-table-column :label="$t('common.operation')" width="200">
        <template #default="{ row }">
          <el-button type="primary" link @click="startEditUser(row)">{{ $t('common.edit') }}</el-button>
          <el-button type="warning" link @click="openChangePassword(row)">{{ $t('sysAdmin.changePassword') }}</el-button>
          <el-button type="danger" link @click="handleDeleteUser(row)" :disabled="row.userId === 1">{{ $t('common.delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="showEditUserDialog" :title="$t('sysAdmin.editUser')" width="480px">
    <el-form :model="editUserForm" label-width="80px">
      <el-form-item :label="$t('sysAdmin.username')">
        <el-input v-model="editUserForm.userName" disabled />
      </el-form-item>
      <el-form-item :label="$t('sysAdmin.nickname')">
        <el-input v-model="editUserForm.nickName" />
      </el-form-item>
      <el-form-item :label="$t('sysAdmin.email')">
        <el-input v-model="editUserForm.email" />
      </el-form-item>
      <el-form-item :label="$t('sysAdmin.role')">
        <el-select v-model="editUserForm.role">
          <el-option v-for="opt in roleOptions" :key="opt.roleKey" :label="opt.roleName" :value="opt.roleKey" />
        </el-select>
      </el-form-item>
      <el-form-item :label="$t('common.status')">
        <el-radio-group v-model="editUserForm.status">
          <el-radio value="0">{{ $t('common.normal') }}</el-radio>
          <el-radio value="1">{{ $t('common.disabled') }}</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showEditUserDialog = false">{{ $t('common.cancel') }}</el-button>
      <el-button type="primary" @click="handleSaveUser">{{ $t('common.confirm') }}</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="showChangePasswordDialog" :title="$t('sysAdmin.changePassword')" width="480px" @closed="resetPasswordForm">
    <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
      <el-form-item :label="$t('sysAdmin.currentUser')">
        <el-input :model-value="passwordForm.userName" disabled />
      </el-form-item>
      <el-form-item :label="$t('sysAdmin.newPassword')" prop="newPassword">
        <el-input v-model="passwordForm.newPassword" type="password" :placeholder="$t('sysAdmin.newPasswordPlaceholder')" show-password />
      </el-form-item>
      <el-form-item :label="$t('sysAdmin.confirmPassword')" prop="confirmPassword">
        <el-input v-model="passwordForm.confirmPassword" type="password" :placeholder="$t('sysAdmin.confirmPasswordPlaceholder')" show-password />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showChangePasswordDialog = false">{{ $t('common.cancel') }}</el-button>
      <el-button type="primary" :loading="changingPassword" @click="handleChangePassword">{{ $t('common.confirm') }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import type { FormInstance } from 'element-plus'
import { getUsers, updateUser, deleteUser, resetPassword } from '@/api/admin'
import { getRoles } from '@/api/role'
import DictTag from '@/components/DictTag.vue'

const { t } = useI18n()

const loadingUsers = ref(false)
const users = ref<any[]>([])

const showEditUserDialog = ref(false)
const editUserForm = reactive({ userId: 0, userName: '', nickName: '', email: '', role: 'USER', status: '0' })

const showChangePasswordDialog = ref(false)
const changingPassword = ref(false)
const passwordFormRef = ref<FormInstance>()
const passwordForm = reactive({ userId: 0, userName: '', newPassword: '', confirmPassword: '' })

const roleOptions = ref<any[]>([])

const validateConfirmPassword = (_rule: any, value: string, callback: Function) => {
  if (value !== passwordForm.newPassword) { callback(new Error(t('sysAdmin.passwordMismatch'))) }
  else { callback() }
}
const passwordRules = {
  newPassword: [{ required: true, message: t('sysAdmin.newPasswordRequired'), trigger: 'blur' }, { min: 6, message: t('sysAdmin.passwordMinLength'), trigger: 'blur' }],
  confirmPassword: [{ required: true, message: t('sysAdmin.confirmPasswordRequired'), trigger: 'blur' }, { validator: validateConfirmPassword, trigger: 'blur' }],
}

const loadUsers = async () => {
  loadingUsers.value = true
  try { const res = await getUsers(); users.value = res.data } finally { loadingUsers.value = false }
}

const loadRoleOptions = async () => {
  try { const res = await getRoles(); roleOptions.value = (res.data || []).filter((r: any) => r.status === '0') } catch {}
}

const openChangePassword = (row: any) => {
  Object.assign(passwordForm, { userId: row.userId, userName: row.userName, newPassword: '', confirmPassword: '' })
  showChangePasswordDialog.value = true
}
const resetPasswordForm = () => { Object.assign(passwordForm, { userId: 0, userName: '', newPassword: '', confirmPassword: '' }) }

const handleChangePassword = async () => {
  const valid = await passwordFormRef.value?.validate().catch(() => false)
  if (!valid) return
  changingPassword.value = true
  try { await resetPassword(passwordForm.userId, passwordForm.newPassword); ElMessage.success(t('sysAdmin.passwordResetSuccess')); showChangePasswordDialog.value = false } catch { ElMessage.error(t('sysAdmin.passwordResetFailed')) } finally { changingPassword.value = false }
}

const startEditUser = (row: any) => {
  Object.assign(editUserForm, { userId: row.userId, userName: row.userName, nickName: row.nickName, email: row.email, role: row.role, status: row.status })
  showEditUserDialog.value = true
}
const handleSaveUser = async () => {
  try { await updateUser(editUserForm); ElMessage.success(t('common.saveSuccess')); showEditUserDialog.value = false; loadUsers() } catch { ElMessage.error(t('common.saveFailed')) }
}
const handleDeleteUser = async (row: any) => {
  try { await ElMessageBox.confirm(t('common.confirmDeleteMsg'), t('sysAdmin.prompt'), { type: 'warning' }); await deleteUser(row.userId); ElMessage.success(t('common.deleteSuccess')); loadUsers() } catch {}
}

onMounted(() => { loadUsers(); loadRoleOptions() })
</script>

<style scoped lang="scss">
.card-header { display: flex; align-items: center; justify-content: space-between; }
</style>
