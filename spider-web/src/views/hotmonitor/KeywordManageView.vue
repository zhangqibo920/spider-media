<template>
  <div class="keyword-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>监控关键词</span>
          <el-button type="primary" @click="showDialog = true">
            <el-icon><Plus /></el-icon> 添加关键词
          </el-button>
        </div>
      </template>
      <el-table :data="keywords" v-loading="loading" stripe>
        <el-table-column prop="keyword" label="关键词" min-width="160" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-switch :model-value="row.status === '0'" @change="handleToggle(row)" size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="intervalMin" label="间隔(分钟)" width="100" />
        <el-table-column label="站内通知" width="90">
          <template #default="{ row }">
            <el-tag :type="row.notifySite === '1' ? 'success' : 'info'" size="small">
              {{ row.notifySite === '1' ? '开启' : '关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="邮件通知" width="240">
          <template #default="{ row }">
            <el-tag :type="row.notifyEmail === '1' ? 'success' : 'info'" size="small">
              {{ row.notifyEmail === '1' ? '开启' : '关闭' }}
            </el-tag>
            <span v-if="row.notifyEmail === '1' && row.notifyEmailAddr" style="margin-left:6px;font-size:12px;color:#666">
              {{ row.notifyEmailAddr }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="lastFetchTime" label="上次抓取" width="160" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除此关键词？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showDialog" :title="isEdit ? '编辑关键词' : '添加关键词'" width="500px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="关键词" prop="keyword">
          <el-input v-model="form.keyword" placeholder="输入要监控的关键词" />
        </el-form-item>
        <el-form-item label="抓取间隔" prop="intervalMin">
          <el-input-number v-model="form.intervalMin" :min="5" :max="1440" /> 分钟
        </el-form-item>
        <el-form-item label="站内通知">
          <el-switch v-model="form.notifySite" :active-value="'1'" :inactive-value="'0'" />
        </el-form-item>
        <el-form-item label="邮件通知">
          <el-switch v-model="form.notifyEmail" :active-value="'1'" :inactive-value="'0'" />
        </el-form-item>
        <el-form-item label="邮箱地址" v-if="form.notifyEmail === '1'">
          <el-input v-model="form.notifyEmailAddr" placeholder="接收通知的邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getKeywords, createKeyword, updateKeyword, deleteKeyword, toggleKeywordStatus } from '@/api/hotmonitor'

const loading = ref(false)
const saving = ref(false)
const showDialog = ref(false)
const isEdit = ref(false)
const keywords = ref<any[]>([])
const formRef = ref<any>(null)

const form = reactive({
  id: 0,
  keyword: '',
  intervalMin: 30,
  notifySite: '1',
  notifyEmail: '0',
  notifyEmailAddr: '',
})

const rules = {
  keyword: [{ required: true, message: '请输入关键词', trigger: 'blur' }],
  intervalMin: [{ required: true, message: '请设置抓取间隔', trigger: 'blur' }],
}

const loadKeywords = async () => {
  loading.value = true
  try {
    const res = await getKeywords()
    keywords.value = res.data
  } finally {
    loading.value = false
  }
}

const handleEdit = (row: any) => {
  isEdit.value = true
  form.id = row.id
  form.keyword = row.keyword
  form.intervalMin = row.intervalMin
  form.notifySite = row.notifySite
  form.notifyEmail = row.notifyEmail
  form.notifyEmailAddr = row.notifyEmailAddr || ''
  showDialog.value = true
}

const handleSave = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (isEdit.value) {
      await updateKeyword(form)
      ElMessage.success('更新成功')
    } else {
      await createKeyword(form)
      ElMessage.success('添加成功')
    }
    showDialog.value = false
    loadKeywords()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    saving.value = false
  }
}

const handleDelete = async (id: number) => {
  try {
    await deleteKeyword(id)
    ElMessage.success('删除成功')
    loadKeywords()
  } catch {
    ElMessage.error('删除失败')
  }
}

const handleToggle = async (row: any) => {
  const newStatus = row.status === '0' ? '1' : '0'
  try {
    await toggleKeywordStatus(row.id, newStatus)
    row.status = newStatus
    ElMessage.success(newStatus === '0' ? '已激活' : '已暂停')
  } catch {
    ElMessage.error('操作失败')
  }
}

const resetForm = () => {
  isEdit.value = false
  form.id = 0
  form.keyword = ''
  form.intervalMin = 30
  form.notifySite = '1'
  form.notifyEmail = '0'
  form.notifyEmailAddr = ''
}

onMounted(() => { loadKeywords() })
</script>

<style scoped lang="scss">
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
