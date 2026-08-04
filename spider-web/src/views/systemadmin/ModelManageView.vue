<template>
  <div class="model-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ $t('sysAdmin.modelManagement') }}</span>
          <el-button type="primary" @click="openAddDialog">
            <el-icon><Plus /></el-icon> {{ $t('sysAdmin.addModel') }}
          </el-button>
        </div>
      </template>

      <el-table :data="models" v-loading="loading" stripe>
        <el-table-column prop="modelName" :label="$t('sysAdmin.modelName')" width="160" />
        <el-table-column prop="modelKey" :label="$t('sysAdmin.modelKey')" width="140">
          <template #default="{ row }">
            <el-tag size="small">{{ row.modelKey }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="provider" :label="$t('sysAdmin.provider')" width="100">
          <template #default="{ row }">
            <el-tag :type="getProviderType(row.provider)" size="small">{{ row.provider }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="apiKey" :label="$t('sysAdmin.apiKey')" width="200">
          <template #default="{ row }">
            <span v-if="row.apiKey">{{ maskKey(row.apiKey) }}</span>
            <span v-else class="text-muted">{{ $t('sysAdmin.notConfigured') }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" :label="$t('common.status')" width="80">
          <template #default="{ row }">
            <el-switch
              :model-value="row.enabled === 'Y'"
              @change="(val: string | number | boolean) => handleToggle(row, val as boolean)"
              :loading="row._toggling"
            />
          </template>
        </el-table-column>
        <el-table-column prop="testStatus" :label="$t('sysAdmin.testStatus')" width="100">
          <template #default="{ row }">
            <el-tag :type="getTestStatusType(row.testStatus)" size="small">
              {{ getTestStatusLabel(row.testStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="testMessage" :label="$t('sysAdmin.testResult')" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.testMessage" :class="row.testStatus === 'SUCCESS' ? 'text-success' : 'text-danger'">
              {{ row.testMessage }}
            </span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.operation')" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEditDialog(row)">{{ $t('common.edit') }}</el-button>
            <el-button type="success" link @click="handleTest(row)" :loading="row._testing">
              {{ row._testing ? $t('sysAdmin.testing') : $t('sysAdmin.testModel') }}
            </el-button>
            <el-popconfirm :title="$t('sysAdmin.confirmDeleteModel')" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link>{{ $t('common.delete') }}</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="showDialog" :title="editingId ? $t('sysAdmin.editModel') : $t('sysAdmin.addModel')" width="600px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="$t('sysAdmin.modelKey')" prop="modelKey">
          <el-input v-model="form.modelKey" :placeholder="$t('sysAdmin.modelKeyPlaceholder')" :disabled="!!editingId" />
        </el-form-item>
        <el-form-item :label="$t('sysAdmin.modelName')" prop="modelName">
          <el-input v-model="form.modelName" :placeholder="$t('sysAdmin.modelNamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('sysAdmin.provider')" prop="provider">
          <el-select v-model="form.provider" :placeholder="$t('sysAdmin.providerPlaceholder')">
            <el-option label="DeepSeek" value="deepseek" />
            <el-option :label="t('sysAdmin.providerZhipu')" value="zhipu" />
            <el-option label="OpenAI" value="openai" />
            <el-option :label="$t('sysAdmin.other')" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('sysAdmin.apiKey')" prop="apiKey">
          <el-input v-model="form.apiKey" type="password" :placeholder="$t('sysAdmin.apiKeyPlaceholder')" show-password />
        </el-form-item>
        <el-form-item :label="$t('sysAdmin.apiUrl')" prop="baseUrl">
          <el-input v-model="form.baseUrl" :placeholder="$t('sysAdmin.apiUrlPlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('sysAdmin.sortOrder')">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item :label="$t('common.enabled')">
          <el-switch v-model="form.enabled" active-value="Y" inactive-value="N" />
        </el-form-item>
        <el-form-item :label="$t('common.remark')">
          <el-input v-model="form.remark" type="textarea" :rows="2" :placeholder="$t('sysAdmin.remarkPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import type { FormInstance } from 'element-plus'
import {
  getAiModelList, addAiModel, updateAiModel, deleteAiModel,
  toggleAiModel, testAiModel
} from '@/api/aiModel'

const { t } = useI18n()

const loading = ref(false)
const models = ref<any[]>([])
const showDialog = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const form = reactive({
  modelKey: '',
  modelName: '',
  provider: 'deepseek',
  apiKey: '',
  baseUrl: '',
  sortOrder: 0,
  enabled: 'N',
  remark: '',
})

const rules = {
  modelKey: [{ required: true, message: t('sysAdmin.modelKeyRequired'), trigger: 'blur' }],
  modelName: [{ required: true, message: t('sysAdmin.modelNameRequired'), trigger: 'blur' }],
  provider: [{ required: true, message: t('sysAdmin.providerRequired'), trigger: 'change' }],
  apiKey: [{ required: true, message: t('sysAdmin.apiKeyRequired'), trigger: 'blur' }],
  baseUrl: [{ required: true, message: t('sysAdmin.apiUrlRequired'), trigger: 'blur' }],
}

const loadModels = async () => {
  loading.value = true
  try {
    const res = await getAiModelList()
    models.value = (res.data || []).map((m: any) => ({ ...m, _testing: false, _toggling: false }))
  } finally {
    loading.value = false
  }
}

const maskKey = (key: string) => {
  if (!key) return ''
  if (key.length <= 8) return '****'
  return key.substring(0, 4) + '****' + key.substring(key.length - 4)
}

const getProviderType = (provider: string) => {
  const map: Record<string, string> = { deepseek: 'primary', zhipu: 'success', openai: 'warning', other: 'info' }
  return (map[provider] || 'info') as any
}

const getTestStatusType = (status: string) => {
  const map: Record<string, string> = { SUCCESS: 'success', FAILED: 'danger', TESTING: 'warning', UNTESTED: 'info' }
  return (map[status] || 'info') as any
}

const getTestStatusLabel = (status: string) => {
  const map: Record<string, string> = { SUCCESS: t('common.success'), FAILED: t('common.failed'), TESTING: t('sysAdmin.testing'), UNTESTED: t('sysAdmin.untested') }
  return map[status] || t('sysAdmin.unknown')
}

const openAddDialog = () => {
  editingId.value = null
  Object.assign(form, { modelKey: '', modelName: '', provider: 'deepseek', apiKey: '', baseUrl: '', sortOrder: 0, enabled: 'N', remark: '' })
  showDialog.value = true
}

const openEditDialog = (row: any) => {
  editingId.value = row.id
  Object.assign(form, {
    modelKey: row.modelKey, modelName: row.modelName, provider: row.provider,
    apiKey: row.apiKey, baseUrl: row.baseUrl, sortOrder: row.sortOrder,
    enabled: row.enabled, remark: row.remark || ''
  })
  showDialog.value = true
}

const resetForm = () => {
  editingId.value = null
  formRef.value?.resetFields()
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    if (editingId.value) {
      await updateAiModel({ id: editingId.value, ...form })
      ElMessage.success(t('common.updateSuccess'))
    } else {
      await addAiModel(form)
      ElMessage.success(t('sysAdmin.addSuccess'))
    }
    showDialog.value = false
    loadModels()
  } catch { ElMessage.error(t('common.operationFailed')) }
}

const handleToggle = async (row: any, enabled: boolean) => {
  row._toggling = true
  try {
    await toggleAiModel(row.id, enabled ? 'Y' : 'N')
    row.enabled = enabled ? 'Y' : 'N'
    ElMessage.success(enabled ? t('sysAdmin.enabledSuccess') : t('sysAdmin.disabledSuccess'))
  } catch { ElMessage.error(t('common.operationFailed')) } finally { row._toggling = false }
}

const handleTest = async (row: any) => {
  row._testing = true
  row.testStatus = 'TESTING'
  row.testMessage = t('sysAdmin.testingMessage')
  try {
    const res = await testAiModel(row.id)
    const result = res.data?.result || ''
    if (result.startsWith(t('sysAdmin.testSuccessPrefix'))) {
      row.testStatus = 'SUCCESS'
      row.testMessage = result
      ElMessage.success(t('sysAdmin.testPassed'))
    } else {
      row.testStatus = 'FAILED'
      row.testMessage = result
      ElMessage.error(t('sysAdmin.testFailed'))
    }
    loadModels()
  } catch {
    row.testStatus = 'FAILED'
    row.testMessage = t('sysAdmin.testRequestFailed')
    ElMessage.error(t('sysAdmin.testRequestFailed'))
  } finally { row._testing = false }
}

const handleDelete = async (id: number) => {
  try {
    await deleteAiModel(id)
    ElMessage.success(t('common.deleteSuccess'))
    loadModels()
  } catch { ElMessage.error(t('common.deleteFailed')) }
}

onMounted(() => { loadModels() })
</script>

<style scoped lang="scss">
.card-header { display: flex; justify-content: space-between; align-items: center; }
.text-muted { color: #909399; }
.text-success { color: #67c23a; }
.text-danger { color: #f56c6c; }
</style>
