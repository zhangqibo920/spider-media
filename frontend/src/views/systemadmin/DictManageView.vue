<template>
  <div class="dict-manage">
    <el-row :gutter="20">
      <!-- Left: Dict Type List -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>字典类型</span>
              <el-button type="primary" size="small" @click="openAddTypeDialog">
                <el-icon><Plus /></el-icon> 添加
              </el-button>
            </div>
          </template>
          <el-table :data="dictTypes" v-loading="loadingTypes" stripe highlight-current-row
                    @current-change="handleTypeSelect" size="small">
            <el-table-column prop="dictName" label="字典名称" />
            <el-table-column prop="dictType" label="字典类型" width="160">
              <template #default="{ row }">
                <el-tag size="small">{{ row.dictType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="openEditTypeDialog(row)">编辑</el-button>
                <el-popconfirm title="确定删除？会同时删除其下所有字典数据" @confirm="handleDeleteType(row.id)">
                  <template #reference>
                    <el-button type="danger" link size="small">删除</el-button>
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
              <span>字典数据 {{ currentType ? `- ${currentType.dictName}` : '' }}</span>
              <el-button type="primary" size="small" @click="openAddDataDialog" :disabled="!currentType">
                <el-icon><Plus /></el-icon> 添加
              </el-button>
            </div>
          </template>
          <el-table :data="dictDataList" v-loading="loadingData" stripe size="small">
            <el-table-column prop="dictSort" label="排序" width="60" />
            <el-table-column prop="dictLabel" label="字典标签" width="120" />
            <el-table-column prop="dictValue" label="字典值" width="120">
              <template #default="{ row }">
                <el-tag size="small">{{ row.dictValue }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="cssClass" label="样式" width="100">
              <template #default="{ row }">
                <el-tag :type="row.cssClass || 'info'" size="small">{{ row.cssClass || 'info' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === '0' ? 'success' : 'danger'" size="small">
                  {{ row.status === '0' ? '正常' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" show-overflow-tooltip />
            <el-table-column label="操作" width="140">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="openEditDataDialog(row)">编辑</el-button>
                <el-popconfirm title="确定删除该字典值？" @confirm="handleDeleteData(row.id)">
                  <template #reference>
                    <el-button type="danger" link size="small">删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- Add/Edit Dict Type Dialog -->
    <el-dialog v-model="showTypeDialog" :title="editingTypeId ? '编辑字典类型' : '添加字典类型'" width="480px">
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-width="100px">
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="typeForm.dictName" placeholder="如：用户状态" />
        </el-form-item>
        <el-form-item label="字典类型" prop="dictType">
          <el-input v-model="typeForm.dictType" placeholder="如：sys_user_status" :disabled="!!editingTypeId" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="typeForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTypeDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveType">确定</el-button>
      </template>
    </el-dialog>

    <!-- Add/Edit Dict Data Dialog -->
    <el-dialog v-model="showDataDialog" :title="editingDataId ? '编辑字典数据' : '添加字典数据'" width="520px">
      <el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="100px">
        <el-form-item label="字典类型">
          <el-input :model-value="currentType?.dictType" disabled />
        </el-form-item>
        <el-form-item label="字典标签" prop="dictLabel">
          <el-input v-model="dataForm.dictLabel" placeholder="如：正常、停用" />
        </el-form-item>
        <el-form-item label="字典值" prop="dictValue">
          <el-input v-model="dataForm.dictValue" placeholder="如：0、1、ENABLED" />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="dataForm.dictSort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="样式类型">
          <el-select v-model="dataForm.cssClass" placeholder="选择样式">
            <el-option label="success (绿色)" value="success" />
            <el-option label="warning (橙色)" value="warning" />
            <el-option label="danger (红色)" value="danger" />
            <el-option label="info (灰色)" value="info" />
            <el-option label="primary (蓝色)" value="primary" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="dataForm.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="dataForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDataDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveData">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import {
  getDictTypeList, addDictType, updateDictType, deleteDictType,
  getDictDataByType, addDictData, updateDictData, deleteDictData
} from '@/api/dictManage'

// ========== Dict Type ==========
const loadingTypes = ref(false)
const dictTypes = ref<any[]>([])
const currentType = ref<any>(null)
const showTypeDialog = ref(false)
const editingTypeId = ref<number | null>(null)
const typeFormRef = ref<FormInstance>()
const typeForm = reactive({ dictName: '', dictType: '', remark: '' })
const typeRules = {
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }],
}

// ========== Dict Data ==========
const loadingData = ref(false)
const dictDataList = ref<any[]>([])
const showDataDialog = ref(false)
const editingDataId = ref<number | null>(null)
const dataFormRef = ref<FormInstance>()
const dataForm = reactive({ dictLabel: '', dictValue: '', dictSort: 0, cssClass: 'info', status: '0', remark: '' })
const dataRules = {
  dictLabel: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入字典值', trigger: 'blur' }],
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
      ElMessage.success('更新成功')
    } else {
      await addDictType(typeForm)
      ElMessage.success('添加成功')
    }
    showTypeDialog.value = false
    loadDictTypes()
  } catch { ElMessage.error('操作失败') }
}

const handleDeleteType = async (id: number) => {
  try {
    await deleteDictType(id)
    ElMessage.success('删除成功')
    if (currentType.value?.id === id) { currentType.value = null; dictDataList.value = [] }
    loadDictTypes()
  } catch { ElMessage.error('删除失败') }
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
      ElMessage.success('更新成功')
    } else {
      await addDictData(payload)
      ElMessage.success('添加成功')
    }
    showDataDialog.value = false
    loadDictData()
  } catch { ElMessage.error('操作失败') }
}

const handleDeleteData = async (id: number) => {
  try {
    await deleteDictData(id)
    ElMessage.success('删除成功')
    loadDictData()
  } catch { ElMessage.error('删除失败') }
}

onMounted(() => { loadDictTypes() })
</script>

<style scoped lang="scss">
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
