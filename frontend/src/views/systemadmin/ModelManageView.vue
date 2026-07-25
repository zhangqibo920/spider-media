<template>
  <div class="model-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>AI 模型管理</span>
          <el-button type="primary" @click="openAddDialog">
            <el-icon><Plus /></el-icon> 添加模型
          </el-button>
        </div>
      </template>

      <el-table :data="models" v-loading="loading" stripe>
        <el-table-column prop="modelName" label="模型名称" width="160" />
        <el-table-column prop="modelKey" label="模型标识" width="140">
          <template #default="{ row }">
            <el-tag size="small">{{ row.modelKey }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="provider" label="提供方" width="100">
          <template #default="{ row }">
            <el-tag :type="getProviderType(row.provider)" size="small">{{ row.provider }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="apiKey" label="API密钥" width="200">
          <template #default="{ row }">
            <span v-if="row.apiKey">{{ maskKey(row.apiKey) }}</span>
            <span v-else class="text-muted">未配置</span>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              :model-value="row.enabled === 'Y'"
              @change="(val: boolean) => handleToggle(row, val)"
              :loading="row._toggling"
            />
          </template>
        </el-table-column>
        <el-table-column prop="testStatus" label="测试状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getTestStatusType(row.testStatus)" size="small">
              {{ getTestStatusLabel(row.testStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="testMessage" label="测试结果" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.testMessage" :class="row.testStatus === 'SUCCESS' ? 'text-success' : 'text-danger'">
              {{ row.testMessage }}
            </span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEditDialog(row)">编辑</el-button>
            <el-button type="success" link @click="handleTest(row)" :loading="row._testing">
              {{ row._testing ? '测试中' : '测试' }}
            </el-button>
            <el-popconfirm title="确定删除该模型？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="showDialog" :title="editingId ? '编辑模型' : '添加模型'" width="600px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="模型标识" prop="modelKey">
          <el-input v-model="form.modelKey" placeholder="如 deepseek-chat、glm-4" :disabled="!!editingId" />
        </el-form-item>
        <el-form-item label="模型名称" prop="modelName">
          <el-input v-model="form.modelName" placeholder="如 DeepSeek Chat、智谱 GLM-4" />
        </el-form-item>
        <el-form-item label="提供方" prop="provider">
          <el-select v-model="form.provider" placeholder="选择提供方">
            <el-option label="DeepSeek" value="deepseek" />
            <el-option label="智谱" value="zhipu" />
            <el-option label="OpenAI" value="openai" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="API密钥" prop="apiKey">
          <el-input v-model="form.apiKey" type="password" placeholder="输入API密钥" show-password />
        </el-form-item>
        <el-form-item label="API地址" prop="baseUrl">
          <el-input v-model="form.baseUrl" placeholder="如 https://api.deepseek.com" />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" active-value="Y" inactive-value="N" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="模型描述信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import {
  getAiModelList, addAiModel, updateAiModel, deleteAiModel,
  toggleAiModel, testAiModel
} from '@/api/aiModel'

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
  modelKey: [{ required: true, message: '请输入模型标识', trigger: 'blur' }],
  modelName: [{ required: true, message: '请输入模型名称', trigger: 'blur' }],
  provider: [{ required: true, message: '请选择提供方', trigger: 'change' }],
  apiKey: [{ required: true, message: '请输入API密钥', trigger: 'blur' }],
  baseUrl: [{ required: true, message: '请输入API地址', trigger: 'blur' }],
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
  const map: Record<string, string> = { SUCCESS: '通过', FAILED: '失败', TESTING: '测试中', UNTESTED: '未测试' }
  return map[status] || '未知'
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
      ElMessage.success('更新成功')
    } else {
      await addAiModel(form)
      ElMessage.success('添加成功')
    }
    showDialog.value = false
    loadModels()
  } catch { ElMessage.error('操作失败') }
}

const handleToggle = async (row: any, enabled: boolean) => {
  row._toggling = true
  try {
    await toggleAiModel(row.id, enabled ? 'Y' : 'N')
    row.enabled = enabled ? 'Y' : 'N'
    ElMessage.success(enabled ? '已启用' : '已禁用')
  } catch { ElMessage.error('操作失败') } finally { row._toggling = false }
}

const handleTest = async (row: any) => {
  row._testing = true
  row.testStatus = 'TESTING'
  row.testMessage = '正在测试...'
  try {
    const res = await testAiModel(row.id)
    const result = res.data?.result || ''
    if (result.startsWith('连接成功')) {
      row.testStatus = 'SUCCESS'
      row.testMessage = result
      ElMessage.success('模型测试通过')
    } else {
      row.testStatus = 'FAILED'
      row.testMessage = result
      ElMessage.error('模型测试失败')
    }
    // Reload to get updated test status from server
    loadModels()
  } catch {
    row.testStatus = 'FAILED'
    row.testMessage = '测试请求失败'
    ElMessage.error('测试请求失败')
  } finally { row._testing = false }
}

const handleDelete = async (id: number) => {
  try {
    await deleteAiModel(id)
    ElMessage.success('删除成功')
    loadModels()
  } catch { ElMessage.error('删除失败') }
}

onMounted(() => { loadModels() })
</script>

<style scoped lang="scss">
.card-header { display: flex; justify-content: space-between; align-items: center; }
.text-muted { color: #909399; }
.text-success { color: #67c23a; }
.text-danger { color: #f56c6c; }
</style>
