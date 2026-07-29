<template>
  <div class="role-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ $t('sysAdmin.roleManagement') }}</span>
          <el-button type="primary" @click="openAddDialog">
            <el-icon><Plus /></el-icon> {{ $t('sysAdmin.addRole') }}
          </el-button>
        </div>
      </template>
      <el-table :data="roleList" v-loading="loading" stripe>
        <el-table-column prop="roleId" label="ID" width="70" />
        <el-table-column prop="roleName" :label="$t('sysAdmin.roleName')" width="160" />
        <el-table-column prop="roleKey" :label="$t('sysAdmin.roleKey')" width="140">
          <template #default="{ row }">
            <el-tag>{{ row.roleKey }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('common.status')" width="80">
          <template #default="{ row }">
            <DictTag dict-type="sys_normal_status" :value="row.status" size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="$t('common.createTime')" width="180" />
        <el-table-column :label="$t('common.operation')" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openMenuAssign(row)">{{ $t('sysAdmin.assignMenu') }}</el-button>
            <el-button type="warning" link @click="openEditDialog(row)">{{ $t('common.edit') }}</el-button>
            <el-button type="danger" link @click="handleDelete(row)">{{ $t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="formDialog" :title="isEdit ? $t('sysAdmin.editRole') : $t('sysAdmin.addRole')" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" :label-width="100">
        <el-form-item :label="$t('sysAdmin.roleName')" prop="roleName">
          <el-input v-model="form.roleName" :placeholder="t('sysAdmin.roleNamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('sysAdmin.roleKey')" prop="roleKey">
          <el-input v-model="form.roleKey" :placeholder="t('sysAdmin.roleKeyPlaceholder')" :disabled="isEdit" />
        </el-form-item>
        <el-form-item :label="$t('common.status')">
          <el-radio-group v-model="form.status">
            <el-radio value="0">{{ $t('common.normal') }}</el-radio>
            <el-radio value="1">{{ $t('common.disabled') }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="menuDialog" :title="$t('sysAdmin.menuPermission')" width="500px">
      <el-tree
        ref="menuTreeRef"
        :data="allMenus"
        show-checkbox
        node-key="menuId"
        :props="{ label: 'menuName', children: 'children' }"
        default-expand-all
        check-strictly
      />
      <template #footer>
        <el-button @click="menuDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="savingMenus" @click="handleSaveMenus">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { getRoles, addRole, updateRole, deleteRole, getRoleMenuIds, updateRoleMenus } from '@/api/role'
import { getMenus } from '@/api/admin'
import DictTag from '@/components/DictTag.vue'

const loading = ref(false)
const submitting = ref(false)
const savingMenus = ref(false)
const formDialog = ref(false)
const menuDialog = ref(false)
const isEdit = ref(false)
const roleList = ref<any[]>([])
const allMenus = ref<any[]>([])
const currentRoleId = ref(0)
const menuTreeRef = ref()
const formRef = ref()

const defaultForm = { roleId: 0, roleName: '', roleKey: '', status: '0' }
const form = reactive({ ...defaultForm })

const rules = {
  roleName: [{ required: true, message: t('sysAdmin.roleNameRequired'), trigger: 'blur' }],
  roleKey: [{ required: true, message: t('sysAdmin.roleKeyRequired'), trigger: 'blur' }],
}

const loadRoles = async () => {
  loading.value = true
  try {
    const res = await getRoles()
    roleList.value = res.data || []
  } finally {
    loading.value = false
  }
}

const loadAllMenus = async () => {
  try {
    const res = await getMenus()
    allMenus.value = res.data || []
  } catch {}
}

const openAddDialog = () => {
  isEdit.value = false
  Object.assign(form, { ...defaultForm })
  formDialog.value = true
}

const openEditDialog = (row: any) => {
  isEdit.value = true
  Object.assign(form, { roleId: row.roleId, roleName: row.roleName, roleKey: row.roleKey, status: row.status })
  formDialog.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateRole({ ...form })
      ElMessage.success(t('common.updateSuccess'))
    } else {
      await addRole({ ...form })
      ElMessage.success(t('common.saveSuccess'))
    }
    formDialog.value = false
    loadRoles()
  } catch {
    ElMessage.error(isEdit.value ? t('common.updateFailed') : t('common.saveFailed'))
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(t('sysAdmin.confirmDeleteRole', { name: row.roleName }), t('sysAdmin.hint'), { type: 'warning' })
    await deleteRole(row.roleId)
    ElMessage.success(t('common.deleteSuccess'))
    loadRoles()
  } catch {}
}

const openMenuAssign = async (row: any) => {
  currentRoleId.value = row.roleId
  await loadAllMenus()
  menuDialog.value = true
  await nextTick()
  try {
    const res = await getRoleMenuIds(row.roleId)
    const checkedIds = res.data || []
    menuTreeRef.value?.setCheckedKeys(checkedIds)
  } catch {}
}

const handleSaveMenus = async () => {
  const checkedKeys = menuTreeRef.value?.getCheckedKeys() || []
  const halfCheckedKeys = menuTreeRef.value?.getHalfCheckedKeys() || []
  savingMenus.value = true
  try {
    await updateRoleMenus(currentRoleId.value, [...checkedKeys, ...halfCheckedKeys])
    ElMessage.success(t('sysAdmin.menuAssignSuccess'))
    menuDialog.value = false
  } catch {
    ElMessage.error(t('common.saveFailed'))
  } finally {
    savingMenus.value = false
  }
}

onMounted(() => { loadRoles() })
</script>

<style scoped lang="scss">
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
