<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>{{ $t('sysAdmin.configManagement') }}</span>
        <el-button type="primary" @click="showAddConfigDialog = true">
          <el-icon><Plus /></el-icon> {{ $t('sysAdmin.addConfig') }}
        </el-button>
      </div>
    </template>
    <el-table :data="configs" v-loading="loadingConfigs" stripe>
      <el-table-column prop="configName" :label="$t('sysAdmin.configName')" width="180" />
      <el-table-column prop="configKey" :label="$t('sysAdmin.configKey')" width="200" />
      <el-table-column prop="configValue" :label="$t('sysAdmin.configValue')">
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
      <el-table-column prop="configType" :label="$t('sysAdmin.configType')" width="80">
        <template #default="{ row }">
          <DictTag dict-type="sys_config_type" :value="row.configType" size="small" />
        </template>
      </el-table-column>
      <el-table-column :label="$t('common.operation')" width="160">
        <template #default="{ row }">
          <el-button v-show="editingConfigId === row.id" type="success" link @click="handleSaveConfig(row)">{{ $t('common.save') }}</el-button>
          <el-button v-show="editingConfigId === row.id" type="info" link @click="cancelEditConfig">{{ $t('common.cancel') }}</el-button>
          <el-button v-show="editingConfigId !== row.id" type="primary" link @click="startEditConfig(row)">{{ $t('common.edit') }}</el-button>
          <el-button v-show="editingConfigId !== row.id" type="danger" link @click="handleDeleteConfig(row)">{{ $t('common.delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="showAddConfigDialog" :title="$t('sysAdmin.addConfig')" width="480px">
    <el-form :model="addConfigForm" label-width="80px">
      <el-form-item :label="$t('sysAdmin.configName')">
        <el-input v-model="addConfigForm.configName" :placeholder="$t('sysAdmin.configNamePlaceholder')" />
      </el-form-item>
      <el-form-item :label="$t('sysAdmin.configKey')">
        <el-input v-model="addConfigForm.configKey" :placeholder="$t('sysAdmin.configKeyPlaceholder')" />
      </el-form-item>
      <el-form-item :label="$t('sysAdmin.configValue')">
        <el-input v-model="addConfigForm.configValue" type="textarea" :rows="3" :placeholder="$t('sysAdmin.configValuePlaceholder')" />
      </el-form-item>
      <el-form-item :label="$t('sysAdmin.configType')">
        <el-radio-group v-model="addConfigForm.configType">
          <el-radio value="N">{{ $t('sysAdmin.custom') }}</el-radio>
          <el-radio value="Y">{{ $t('sysAdmin.builtin') }}</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showAddConfigDialog = false">{{ $t('common.cancel') }}</el-button>
      <el-button type="primary" @click="handleAddConfig">{{ $t('common.confirm') }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { getSystemConfigs, addSystemConfig, updateSystemConfig, deleteSystemConfig } from '@/api/admin'
import DictTag from '@/components/DictTag.vue'

const { t } = useI18n()

const loadingConfigs = ref(false)
const configs = ref<any[]>([])
const editingConfigId = ref<number | null>(null)
const editingConfigValue = ref('')
const showAddConfigDialog = ref(false)
const addConfigForm = reactive({ configName: '', configKey: '', configValue: '', configType: 'N' })

const loadConfigs = async () => {
  loadingConfigs.value = true
  try {
    const res = await getSystemConfigs('')
    configs.value = res.data
  } finally { loadingConfigs.value = false }
}

const startEditConfig = (row: any) => { editingConfigId.value = row.id; editingConfigValue.value = row.configValue || '' }
const cancelEditConfig = () => { editingConfigId.value = null; editingConfigValue.value = '' }
const handleSaveConfig = async (row: any) => {
  try {
    await updateSystemConfig({ id: row.id, configValue: editingConfigValue.value })
    ElMessage.success(t('common.saveSuccess'))
    cancelEditConfig()
    loadConfigs()
  } catch { ElMessage.error(t('common.saveFailed')) }
}
const handleAddConfig = async () => {
  if (!addConfigForm.configKey) { ElMessage.warning(t('sysAdmin.configKeyRequired')); return }
  try {
    await addSystemConfig(addConfigForm)
    ElMessage.success(t('sysAdmin.addSuccess'))
    showAddConfigDialog.value = false
    addConfigForm.configName = ''; addConfigForm.configKey = ''; addConfigForm.configValue = ''; addConfigForm.configType = 'N'
    loadConfigs()
  } catch { ElMessage.error(t('sysAdmin.addFailed')) }
}
const handleDeleteConfig = async (row: any) => {
  try {
    await ElMessageBox.confirm(t('common.confirmDeleteMsg'), t('sysAdmin.prompt'), { type: 'warning' })
    await deleteSystemConfig(row.id)
    ElMessage.success(t('common.deleteSuccess'))
    loadConfigs()
  } catch {}
}

onMounted(() => { loadConfigs() })
</script>

<style scoped lang="scss">
.card-header { display: flex; align-items: center; justify-content: space-between; }
</style>
