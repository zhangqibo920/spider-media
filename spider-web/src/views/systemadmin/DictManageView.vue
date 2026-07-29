<template>
  <div class="dict-manage">
    <el-row :gutter="20">
      <!-- Left: Dict Type List -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>{{ $t('sysAdmin.dictType') }}</span>
              <el-button type="primary" size="small" @click="openAddTypeDialog">
                <el-icon><Plus /></el-icon> {{ $t('common.add') }}
              </el-button>
            </div>
          </template>
          <el-table :data="dictTypes" v-loading="loadingTypes" stripe highlight-current-row
                    @current-change="handleTypeSelect" size="small">
            <el-table-column prop="dictName" :label="$t('sysAdmin.dictName')" />
            <el-table-column prop="dictType" :label="$t('sysAdmin.dictType')" width="160">
              <template #default="{ row }">
                <el-tag size="small">{{ row.dictType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column :label="$t('common.operation')" width="120">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="openEditTypeDialog(row)">{{ $t('common.edit') }}</el-button>
                <el-popconfirm :title="$t('sysAdmin.confirmDeleteDictType')" @confirm="handleDeleteType(row.id)">
                  <template #reference>
                    <el-button type="danger" link size="small">{{ $t('common.delete') }}</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- Right: Dict Data List -->
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>{{ $t('sysAdmin.dictData') }} {{ currentType ? `- ${currentType.dictName}` : '' }}</span>
              <el-button type="primary" size="small" @click="openAddDataDialog" :disabled="!currentType">
                <el-icon><Plus /></el-icon> {{ $t('common.add') }}
              </el-button>
            </div>
          </template>
          <el-table :data="dictDataList" v-loading="loadingData" stripe size="small">
            <el-table-column prop="dictSort" :label="$t('sysAdmin.sortOrder')" width="60" />
            <el-table-column prop="dictLabel" :label="$t('sysAdmin.dictLabel')" width="120" />
            <el-table-column prop="dictValue" :label="$t('sysAdmin.dictValue')" width="120">
              <template #default="{ row }">
                <el-tag size="small">{{ row.dictValue }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="cssClass" :label="$t('sysAdmin.cssClass')" width="100">
              <template #default="{ row }">
                <el-tag :type="row.cssClass || 'info'" size="small">{{ row.cssClass || 'info' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" :label="$t('common.status')" width="80">
              <template #default="{ row }">
                <DictTag dict-type="sys_user_status" :value="row.status" size="small" />
              </template>
            </el-table-column>
            <el-table-column prop="remark" :label="$t('common.remark')" show-overflow-tooltip />
            <el-table-column :label="$t('common.operation')" width="140">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="openEditDataDialog(row)">{{ $t('common.edit') }}</el-button>
                <el-popconfirm :title="$t('sysAdmin.confirmDeleteDictData')" @confirm="handleDeleteData(row.id)">
                  <template #reference>
                    <el-button type="danger" link size="small">{{ $t('common.delete') }}</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- Add/Edit Dict Type Dialog -->
    <el-dialog v-model="showTypeDialog" :title="editingTypeId ? $t('sysAdmin.editDictType') : $t('sysAdmin.addDictType')" width="480px">
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" :label-width="100">
        <el-form-item :label="$t('sysAdmin.dictName')" prop="dictName">
          <el-input v-model="typeForm.dictName" :placeholder="t('sysAdmin.dictNamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('sysAdmin.dictType')" prop="dictType">
          <el-input v-model="typeForm.dictType" :placeholder="t('sysAdmin.dictTypePlaceholder')" :disabled="!!editingTypeId" />
        </el-form-item>
        <el-form-item :label="$t('common.remark')">
          <el-input v-model="typeForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTypeDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSaveType">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <!-- Add/Edit Dict Data Dialog -->
    <el-dialog v-model="showDataDialog" :title="editingDataId ? $t('sysAdmin.editDictData') : $t('sysAdmin.addDictData')" width="520px">
      <el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" :label-width="100">
        <el-form-item :label="$t('sysAdmin.dictType')">
          <el-input :model-value="currentType?.dictType" disabled />
        </el-form-item>
        <el-form-item :label="$t('sysAdmin.dictLabel')" prop="dictLabel">
          <el-input v-model="dataForm.dictLabel" :placeholder="t('sysAdmin.dictLabelPlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('sysAdmin.dictValue')" prop="dictValue">
          <el-input v-model="dataForm.dictValue" :placeholder="t('sysAdmin.dictValuePlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('sysAdmin.sortOrder')">
          <el-input-number v-model="dataForm.dictSort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item :label="$t('sysAdmin.cssClass')">
          <el-select v-model="dataForm.cssClass" :placeholder="t('sysAdmin.cssClassPlaceholder')">
            <el-option :label="t('sysAdmin.cssStyleSuccess')" value="success" />
            <el-option :label="t('sysAdmin.cssStyleWarning')" value="warning" />
            <el-option :label="t('sysAdmin.cssStyleDanger')" value="danger" />
            <el-option :label="t('sysAdmin.cssStyleInfo')" value="info" />
            <el-option :label="t('sysAdmin.cssStylePrimary')" value="primary" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('common.status')">
          <el-radio-group v-model="dataForm.status">
            <el-radio value="0">{{ $t('common.normal') }}</el-radio>
            <el-radio value="1">{{ $t('common.disabled') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('common.remark')">
          <el-input v-model="dataForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDataDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSaveData">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import type { FormInstance } from 'element-plus'
import {
  getDictTypeList, addDictType, updateDictType, deleteDictType,
  getDictDataByType, addDictData, updateDictData, deleteDictData
} from '@/api/dictManage'
import DictTag from '@/components/DictTag.vue'
import { reloadDict } from '@/composables/useDict'

// ========== Dict Type ==========
const loadingTypes = ref(false)
const dictTypes = ref<any[]>([])
const currentType = ref<any>(null)
const showTypeDialog = ref(false)
const editingTypeId = ref<number | null>(null)
const typeFormRef = ref<FormInstance>()
const typeForm = reactive({ dictName: '', dictType: '', remark: '' })
const typeRules = {
  dictName: [{ required: true, message: t('sysAdmin.dictNameRequired'), trigger: 'blur' }],
  dictType: [{ required: true, message: t('sysAdmin.dictTypeRequired'), trigger: 'blur' }],
}

// ========== Dict Data ==========
const loadingData = ref(false)
const dictDataList = ref<any[]>([])
const showDataDialog = ref(false)
const editingDataId = ref<number | null>(null)
const dataFormRef = ref<FormInstance>()
const dataForm = reactive({ dictLabel: '', dictValue: '', dictSort: 0, cssClass: 'info', status: '0', remark: '' })
const dataRules = {
  dictLabel: [{ required: true, message: t('sysAdmin.dictLabelRequired'), trigger: 'blur' }],
  dictValue: [{ required: true, message: t('sysAdmin.dictValueRequired'), trigger: 'blur' }],
}

const loadDictTypes = async () => {
  loadingTypes.value = true
  try {
    const res = await getDictTypeList()
    dictTypes.value = res.data || []
  } finally { loadingTypes.value = false }
}

const loadDictData = async () => {
  if (!currentType.value) { dictDataList.value = []; return }
  loadingData.value = true
  try {
    const res = await getDictDataByType(currentType.value.dictType)
    dictDataList.value = res.data || []
  } finally { loadingData.value = false }
}

const handleTypeSelect = (row: any) => {
  currentType.value = row
  loadDictData()
}

// ========== Dict Type CRUD ==========
const openAddTypeDialog = () => {
  editingTypeId.value = null
  Object.assign(typeForm, { dictName: '', dictType: '', remark: '' })
  showTypeDialog.value = true
}

const openEditTypeDialog = (row: any) => {
  editingTypeId.value = row.id
  Object.assign(typeForm, { dictName: row.dictName, dictType: row.dictType, remark: row.remark || '' })
  showTypeDialog.value = true
}

const handleSaveType = async () => {
  const valid = await typeFormRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    if (editingTypeId.value) {
      await updateDictType({ id: editingTypeId.value, ...typeForm })
      ElMessage.success(t('common.updateSuccess'))
    } else {
      await addDictType(typeForm)
      ElMessage.success(t('common.saveSuccess'))
    }
    showTypeDialog.value = false
    loadDictTypes()
  } catch { ElMessage.error(t('common.operationFailed')) }
}

const handleDeleteType = async (id: number) => {
  try {
    await deleteDictType(id)
    ElMessage.success(t('common.deleteSuccess'))
    if (currentType.value?.id === id) { currentType.value = null; dictDataList.value = [] }
    loadDictTypes()
  } catch { ElMessage.error(t('common.deleteFailed')) }
}

// ========== Dict Data CRUD ==========
const openAddDataDialog = () => {
  editingDataId.value = null
  Object.assign(dataForm, { dictLabel: '', dictValue: '', dictSort: dictDataList.value.length, cssClass: 'info', status: '0', remark: '' })
  showDataDialog.value = true
}

const openEditDataDialog = (row: any) => {
  editingDataId.value = row.id
  Object.assign(dataForm, {
    dictLabel: row.dictLabel, dictValue: row.dictValue, dictSort: row.dictSort,
    cssClass: row.cssClass || 'info', status: row.status, remark: row.remark || ''
  })
  showDataDialog.value = true
}

const handleSaveData = async () => {
  const valid = await dataFormRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    const payload = { ...dataForm, dictType: currentType.value.dictType }
    if (editingDataId.value) {
      await updateDictData({ id: editingDataId.value, ...payload })
      ElMessage.success(t('common.updateSuccess'))
    } else {
      await addDictData(payload)
      ElMessage.success(t('common.saveSuccess'))
    }
    showDataDialog.value = false
    loadDictData()
    // Reload dict cache so other pages see updated labels
    reloadDict(currentType.value.dictType)
  } catch { ElMessage.error(t('common.operationFailed')) }
}

const handleDeleteData = async (id: number) => {
  try {
    await deleteDictData(id)
    ElMessage.success(t('common.deleteSuccess'))
    loadDictData()
    // Reload dict cache so other pages see updated labels
    reloadDict(currentType.value.dictType)
  } catch { ElMessage.error(t('common.deleteFailed')) }
}

onMounted(() => { loadDictTypes() })
</script>

<style scoped lang="scss">
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
