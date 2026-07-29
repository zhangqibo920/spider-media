<template>
  <div class="menu-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ $t('sysAdmin.menuManagement') }}</span>
          <el-button type="primary" @click="openAddDialog(null)">
            <el-icon><Plus /></el-icon> {{ $t('sysAdmin.addMenu') }}
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
        <el-table-column prop="menuName" :label="$t('sysAdmin.menuName')" width="200">
          <template #default="{ row }">
            <el-icon v-if="row.icon" style="margin-right: 6px"><component :is="row.icon" /></el-icon>
            {{ row.menuName }}
          </template>
        </el-table-column>
        <el-table-column prop="icon" :label="$t('sysAdmin.icon')" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.icon" size="small">{{ row.icon }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" :label="$t('sysAdmin.routePath')" width="180" />
        <el-table-column prop="component" :label="$t('sysAdmin.componentPath')" width="200" />
        <el-table-column prop="sortOrder" :label="$t('sysAdmin.sortOrder')" width="70" />
        <el-table-column prop="menuType" :label="$t('sysAdmin.menuType')" width="80">
          <template #default="{ row }">
            <DictTag dict-type="sys_menu_type" :value="row.menuType || 'C'" size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="visible" :label="$t('sysAdmin.visible')" width="70">
          <template #default="{ row }">
            <DictTag dict-type="sys_visible" :value="row.visible || '0'" size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="perms" :label="$t('sysAdmin.perms')" width="180" />
        <el-table-column prop="status" :label="$t('common.status')" width="70">
          <template #default="{ row }">
            <DictTag dict-type="sys_normal_status" :value="row.status || '0'" size="small" />
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.operation')" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openAddDialog(row)">{{ $t('sysAdmin.addChildMenu') }}</el-button>
            <el-button type="warning" link @click="openEditDialog(row)">{{ $t('common.edit') }}</el-button>
            <el-button type="danger" link @click="handleDelete(row)">{{ $t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? $t('sysAdmin.editMenu') : $t('sysAdmin.addMenu')" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" :label-width="100">
        <el-form-item :label="$t('sysAdmin.parentMenu')">
          <el-tree-select
            v-model="form.parentId"
            :data="menuTreeSelect"
            :props="{ label: 'menuName', value: 'menuId', children: 'children' }"
            :placeholder="t('sysAdmin.parentMenuPlaceholder')"
            check-strictly
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item :label="$t('sysAdmin.menuName')" prop="menuName">
          <el-input v-model="form.menuName" :placeholder="t('sysAdmin.menuNamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('sysAdmin.routePath')" prop="path">
          <el-input v-model="form.path" :placeholder="t('sysAdmin.routePathPlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('sysAdmin.componentPath')">
          <el-input v-model="form.component" :placeholder="t('sysAdmin.componentPathPlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('sysAdmin.icon')">
          <el-select v-model="form.icon" clearable filterable :placeholder="t('sysAdmin.iconPlaceholder')" style="width: 100%">
            <el-option v-for="icon in elementIcons" :key="icon" :label="icon" :value="icon">
              <el-icon style="vertical-align: middle; margin-right: 6px"><component :is="icon" /></el-icon>
              <span>{{ icon }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item :label="$t('sysAdmin.menuType')">
              <el-select v-model="form.menuType" :placeholder="t('sysAdmin.pleaseSelect')">
                <el-option :label="t('sysAdmin.menuTypeDir')" value="M" />
                <el-option :label="t('sysAdmin.menuTypeMenu')" value="C" />
                <el-option :label="t('sysAdmin.menuTypeButton')" value="F" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('sysAdmin.sortOrder')">
              <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item :label="$t('sysAdmin.visible')">
              <el-radio-group v-model="form.visible">
                <el-radio value="0">{{ $t('sysAdmin.show') }}</el-radio>
                <el-radio value="1">{{ $t('sysAdmin.hide') }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('common.status')">
              <el-radio-group v-model="form.status">
                <el-radio value="0">{{ $t('common.normal') }}</el-radio>
                <el-radio value="1">{{ $t('common.disabled') }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item :label="$t('sysAdmin.perms')">
          <el-input v-model="form.perms" :placeholder="t('sysAdmin.permsPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
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
  menuName: [{ required: true, message: t('sysAdmin.menuNameRequired'), trigger: 'blur' }],
  path: [{ required: true, message: t('sysAdmin.pathRequired'), trigger: 'blur' }],
}

const menuTreeSelect = computed(() => {
  return menuList.value.length > 0 ? [{ menuId: 0, menuName: t('sysAdmin.topLevelMenu'), children: menuList.value }] : []
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
      ElMessage.success(t('common.updateSuccess'))
    } else {
      await addMenu({ ...form })
      ElMessage.success(t('common.saveSuccess'))
    }
    dialogVisible.value = false
    loadMenus()
  } catch {
    ElMessage.error(isEdit.value ? t('common.updateFailed') : t('common.saveFailed'))
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(t('sysAdmin.confirmDeleteMenu', { name: row.menuName }), t('sysAdmin.hint'), { type: 'warning' })
    await deleteMenu(row.menuId)
    ElMessage.success(t('common.deleteSuccess'))
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
