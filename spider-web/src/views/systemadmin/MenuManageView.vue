<template>
  <div class="menu-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>菜单管理</span>
          <el-button type="primary" @click="openAddDialog(null)">
            <el-icon><Plus /></el-icon> 添加菜单
          </el-button>
        </div>
      </template>
      <el-table
        :data="menuList"
        v-loading="loading"
        row-key="menuId"
        default-expand-all
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        stripe
      >
        <el-table-column prop="menuName" label="菜单名称" width="200">
          <template #default="{ row }">
            <el-icon v-if="row.icon" style="margin-right: 6px"><component :is="row.icon" /></el-icon>
            {{ row.menuName }}
          </template>
        </el-table-column>
        <el-table-column prop="icon" label="图标" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.icon" size="small">{{ row.icon }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" width="180" />
        <el-table-column prop="component" label="组件路径" width="200" />
        <el-table-column prop="sortOrder" label="排序" width="70" />
        <el-table-column prop="menuType" label="类型" width="80">
          <template #default="{ row }">
            <DictTag dict-type="sys_menu_type" :value="row.menuType || 'C'" size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="visible" label="可见" width="70">
          <template #default="{ row }">
            <DictTag dict-type="sys_visible" :value="row.visible || '0'" size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="perms" label="权限标识" width="180" />
        <el-table-column prop="status" label="状态" width="70">
          <template #default="{ row }">
            <DictTag dict-type="sys_normal_status" :value="row.status || '0'" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openAddDialog(row)">添加子菜单</el-button>
            <el-button type="warning" link @click="openEditDialog(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑菜单' : '添加菜单'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="form.parentId"
            :data="menuTreeSelect"
            :props="{ label: 'menuName', value: 'menuId', children: 'children' }"
            placeholder="选择上级菜单（留空为顶级）"
            check-strictly
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="路由路径" prop="path">
          <el-input v-model="form.path" placeholder="如 /dashboard" />
        </el-form-item>
        <el-form-item label="组件路径">
          <el-input v-model="form.component" placeholder="如 dashboard/DashboardView" />
        </el-form-item>
        <el-form-item label="图标">
          <el-select v-model="form.icon" clearable filterable placeholder="请选择图标" style="width: 100%">
            <el-option v-for="icon in elementIcons" :key="icon" :label="icon" :value="icon">
              <el-icon style="vertical-align: middle; margin-right: 6px"><component :is="icon" /></el-icon>
              <span>{{ icon }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="菜单类型">
              <el-select v-model="form.menuType" placeholder="请选择">
                <el-option label="目录" value="M" />
                <el-option label="菜单" value="C" />
                <el-option label="按钮" value="F" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="显示状态">
              <el-radio-group v-model="form.visible">
                <el-radio value="0">显示</el-radio>
                <el-radio value="1">隐藏</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单状态">
              <el-radio-group v-model="form.status">
                <el-radio value="0">正常</el-radio>
                <el-radio value="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="权限标识">
          <el-input v-model="form.perms" placeholder="如 system:user:list" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMenus, addMenu, updateMenu, deleteMenu } from '@/api/admin'
import { iconMap, iconNames as elementIcons } from '@/utils/icon-map'
import DictTag from '@/components/DictTag.vue'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const menuList = ref<any[]>([])
const formRef = ref()

const defaultForm = {
  menuId: 0,
  parentId: null as number | null,
  menuName: '',
  path: '',
  component: '',
  icon: '',
  menuType: 'C',
  sortOrder: 0,
  visible: '0',
  status: '0',
  perms: '',
}

const form = reactive({ ...defaultForm })

const rules = {
  menuName: [{ required: true, message: '菜单名称不能为空', trigger: 'blur' }],
  path: [{ required: true, message: '路由路径不能为空', trigger: 'blur' }],
}

const menuTreeSelect = computed(() => {
  return menuList.value.length > 0 ? [{ menuId: 0, menuName: '顶级菜单', children: menuList.value }] : []
})

const loadMenus = async () => {
  loading.value = true
  try {
    const res = await getMenus()
    menuList.value = res.data || []
  } finally {
    loading.value = false
  }
}

const openAddDialog = (parent: any) => {
  isEdit.value = false
  Object.assign(form, { ...defaultForm })
  if (parent) {
    form.parentId = parent.menuId
  }
  dialogVisible.value = true
}

const openEditDialog = (row: any) => {
  isEdit.value = true
  Object.assign(form, {
    menuId: row.menuId,
    parentId: row.parentId || null,
    menuName: row.menuName,
    path: row.path,
    component: row.component,
    icon: row.icon,
    menuType: row.menuType,
    sortOrder: row.sortOrder,
    visible: row.visible,
    status: row.status,
    perms: row.perms,
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateMenu({ ...form })
      ElMessage.success('修改成功')
    } else {
      await addMenu({ ...form })
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadMenus()
  } catch {
    ElMessage.error(isEdit.value ? '修改失败' : '添加失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定删除菜单「${row.menuName}」吗？`, '提示', { type: 'warning' })
    await deleteMenu(row.menuId)
    ElMessage.success('删除成功')
    loadMenus()
  } catch {}
}

onMounted(() => { loadMenus() })
</script>

<style scoped lang="scss">
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
