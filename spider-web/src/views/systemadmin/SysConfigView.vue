<template>
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
          <el-button v-show="editingConfigId === row.id" type="success" link @click="handleSaveConfig(row)">保存</el-button>
          <el-button v-show="editingConfigId === row.id" type="info" link @click="cancelEditConfig">取消</el-button>
          <el-button v-show="editingConfigId !== row.id" type="primary" link @click="startEditConfig(row)">编辑</el-button>
          <el-button v-show="editingConfigId !== row.id" type="danger" link @click="handleDeleteConfig(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

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
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSystemConfigs, addSystemConfig, updateSystemConfig, deleteSystemConfig } from '@/api/admin'
import DictTag from '@/components/DictTag.vue'

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

onMounted(() => { loadConfigs() })
</script>

<style scoped lang="scss">
.card-header { display: flex; align-items: center; justify-content: space-between; }
</style>
