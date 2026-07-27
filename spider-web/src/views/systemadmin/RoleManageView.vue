<template>
  <div class="role-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button type="primary" @click="openAddDialog">
            <el-icon><Plus /></el-icon> 添加角色
          </el-button>
        </div>
      </template>
      <el-table :data="roleList" v-loading="loading" stripe>
        <el-table-column prop="roleId" label="ID" width="70" />
        <el-table-column prop="roleName" label="角色名称" width="160" />
        <el-table-column prop="roleKey" label="角色标识" width="140">
          <template #default="{ row }">
            <el-tag>{{ row.roleKey }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <DictTag dict-type="sys_normal_status" :value="row.status" size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openMenuAssign(row)">分配菜单</el-button>
            <el-button type="warning" link @click="openEditDialog(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="formDialog" :title="isEdit ? '编辑角色' : '添加角色'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色标识" prop="roleKey">
          <el-input v-model="form.roleKey" placeholder="如 EDITOR" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="menuDialog" title="分配菜单权限" width="500px">
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
        <el-button @click="menuDialog = false">取消</el-button>
        <el-button type="primary" :loading="savingMenus" @click="handleSaveMenus">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
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
  roleName: [{ required: true, message: '角色名称不能为空', trigger: 'blur' }],
  roleKey: [{ required: true, message: '角色标识不能为空', trigger: 'blur' }],
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
      ElMessage.success('修改成功')
    } else {
      await addRole({ ...form })
      ElMessage.success('添加成功')
    }
    formDialog.value = false
    loadRoles()
  } catch {
    ElMessage.error(isEdit.value ? '修改失败' : '添加失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定删除角色「${row.roleName}」吗？`, '提示', { type: 'warning' })
    await deleteRole(row.roleId)
    ElMessage.success('删除成功')
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
    ElMessage.success('菜单分配成功')
    menuDialog.value = false
  } catch {
    ElMessage.error('保存失败')
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
