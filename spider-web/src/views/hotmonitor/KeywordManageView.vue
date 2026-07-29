<template>
  <div class="keyword-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ t('keyword.title') }}</span>
          <el-button type="primary" @click="showDialog = true">
            <el-icon><Plus /></el-icon> {{ t('keyword.addKeyword') }}
          </el-button>
        </div>
      </template>
      <el-table :data="keywords" v-loading="loading" stripe>
        <el-table-column prop="keyword" :label="t('keyword.keyword')" min-width="160" />
        <el-table-column :label="t('common.status')" width="80">
          <template #default="{ row }">
            <el-switch :model-value="row.status === '0'" @change="handleToggle(row)" size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="intervalMin" :label="t('keyword.interval')" width="100" />
        <el-table-column :label="t('keyword.siteNotification')" width="90">
          <template #default="{ row }">
            <el-tag :type="row.notifySite === '1' ? 'success' : 'info'" size="small">
              {{ row.notifySite === '1' ? t('common.enabled') : t('common.disabled') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('keyword.emailNotification')" width="240">
          <template #default="{ row }">
            <el-tag :type="row.notifyEmail === '1' ? 'success' : 'info'" size="small">
              {{ row.notifyEmail === '1' ? t('common.enabled') : t('common.disabled') }}
            </el-tag>
            <span v-if="row.notifyEmail === '1' && row.notifyEmailAddr" style="margin-left:6px;font-size:12px;color:#666">
              {{ row.notifyEmailAddr }}
            </span>
          </template>
        </el-table-column>
        <el-table-column :label="t('keyword.sources')" min-width="180">
          <template #default="{ row }">
            <template v-if="row.sources && row.sources.length > 0">
              <el-tag v-for="s in row.sources.split(',')" :key="s" size="small" style="margin:1px 2px">{{ s }}</el-tag>
            </template>
            <span v-else style="color:#999;font-size:12px">{{ t('keyword.allSources') }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="lastFetchTime" :label="t('keyword.lastFetch')" width="160" />
        <el-table-column :label="t('common.operation')" width="120">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">{{ t('common.edit') }}</el-button>
            <el-popconfirm :title="t('common.confirmDeleteMsg')" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link>{{ t('common.delete') }}</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showDialog" :title="isEdit ? t('keyword.editKeyword') : t('keyword.addKeywordTitle')" width="500px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="t('keyword.keyword')" prop="keyword">
          <el-input v-model="form.keyword" :placeholder="t('keyword.keywordPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('keyword.fetchInterval')" prop="intervalMin">
          <el-input-number v-model="form.intervalMin" :min="5" :max="1440" /> {{ t('keyword.minutes') }}
        </el-form-item>
        <el-form-item :label="t('keyword.siteNotification')">
          <el-switch v-model="form.notifySite" :active-value="'1'" :inactive-value="'0'" />
        </el-form-item>
        <el-form-item :label="t('keyword.emailNotification')">
          <el-switch v-model="form.notifyEmail" :active-value="'1'" :inactive-value="'0'" />
        </el-form-item>
        <el-form-item :label="t('keyword.emailAddr')" v-if="form.notifyEmail === '1'">
          <el-input v-model="form.notifyEmailAddr" :placeholder="t('keyword.emailAddrPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('keyword.sources')">
          <el-select v-model="form.sources" multiple :placeholder="t('keyword.selectSources')" style="width:100%">
            <el-option v-for="s in allSources" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
          <div style="font-size:12px;color:#999;margin-top:4px">{{ t('keyword.sourceHint') }}</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">{{ t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { getKeywords, createKeyword, updateKeyword, deleteKeyword, toggleKeywordStatus } from '@/api/hotmonitor'

const { t } = useI18n()

const loading = ref(false)
const saving = ref(false)
const showDialog = ref(false)
const isEdit = ref(false)
const keywords = ref<any[]>([])
const formRef = ref<any>(null)

const allSources = computed(() => [
  { label: '微博', value: 'weibo' },
  { label: '抖音', value: 'douyin' },
  { label: '知乎', value: 'zhihu' },
  { label: '头条', value: 'toutiao' },
  { label: '百度', value: 'baidu' },
  { label: 'B站', value: 'bilibili' },
  { label: 'HackerNews', value: 'hackernews' },
  { label: 'GitHub', value: 'github' },
])

const form = reactive({
  id: 0,
  keyword: '',
  intervalMin: 30,
  notifySite: '1',
  notifyEmail: '0',
  notifyEmailAddr: '',
  sources: [] as string[],
})

const rules = {
  keyword: [{ required: true, message: t('keyword.keywordRequired'), trigger: 'blur' }],
  intervalMin: [{ required: true, message: t('keyword.intervalRequired'), trigger: 'blur' }],
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
  form.sources = row.sources ? row.sources.split(',') : []
  showDialog.value = true
}

const handleSave = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const payload = { ...form, sources: form.sources.join(',') }
    if (isEdit.value) {
      await updateKeyword(payload)
      ElMessage.success(t('common.updateSuccess'))
    } else {
      await createKeyword(payload)
      ElMessage.success(t('keyword.addSuccess'))
    }
    showDialog.value = false
    loadKeywords()
  } catch {
    ElMessage.error(t('common.operationFailed'))
  } finally {
    saving.value = false
  }
}

const handleDelete = async (id: number) => {
  try {
    await deleteKeyword(id)
    ElMessage.success(t('common.deleteSuccess'))
    loadKeywords()
  } catch {
    ElMessage.error(t('common.deleteFailed'))
  }
}

const handleToggle = async (row: any) => {
  const newStatus = row.status === '0' ? '1' : '0'
  try {
    await toggleKeywordStatus(row.id, newStatus)
    row.status = newStatus
    ElMessage.success(newStatus === '0' ? t('keyword.activated') : t('keyword.paused'))
  } catch {
    ElMessage.error(t('common.operationFailed'))
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
  form.sources = []
}

onMounted(() => { loadKeywords() })
</script>

<style scoped lang="scss">
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
