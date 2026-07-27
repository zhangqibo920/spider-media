<template>
  <el-card>
    <template #header><span>用户管理</span></template>
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
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { getUsers, updateUser, deleteUser, resetPassword } from '@/api/admin'
import { getRoles } from '@/api/role'
import DictTag from '@/components/DictTag.vue'

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
  if (value !== passwordForm.newPassword) { callback(new Error('两次输入的密码不一致')) }
  else { callback() }
}
const passwordRules = {
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' }, { validator: validateConfirmPassword, trigger: 'blur' }],
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
  try { await resetPassword(passwordForm.userId, passwordForm.newPassword); ElMessage.success('密码重置成功'); showChangePasswordDialog.value = false } catch { ElMessage.error('密码重置失败') } finally { changingPassword.value = false }
}

const startEditUser = (row: any) => {
  Object.assign(editUserForm, { userId: row.userId, userName: row.userName, nickName: row.nickName, email: row.email, role: row.role, status: row.status })
  showEditUserDialog.value = true
}
const handleSaveUser = async () => {
  try { await updateUser(editUserForm); ElMessage.success('保存成功'); showEditUserDialog.value = false; loadUsers() } catch { ElMessage.error('保存失败') }
}
const handleDeleteUser = async (row: any) => {
  try { await ElMessageBox.confirm('确定删除该用户吗？', '提示', { type: 'warning' }); await deleteUser(row.userId); ElMessage.success('删除成功'); loadUsers() } catch {}
}

onMounted(() => { loadUsers(); loadRoleOptions() })
</script>

<style scoped lang="scss">
.card-header { display: flex; align-items: center; justify-content: space-between; }
</style>
