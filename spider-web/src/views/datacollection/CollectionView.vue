<template>
  <div class="data-collection">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ $t('collection.title') }}</span>
          <el-button type="primary" @click="handleAddAccount">
            <el-icon><Plus /></el-icon> {{ $t('collection.addAccount') }}
          </el-button>
        </div>
      </template>

      <el-table :data="accounts" v-loading="loading" stripe>
        <el-table-column prop="platform" :label="$t('collection.platform')" width="100">
          <template #default="{ row }">
            <el-tag>{{ row.platform }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="accountName" :label="$t('collection.accountName')" />
        <el-table-column prop="accountId" :label="$t('collection.accountId')" />
        <el-table-column prop="groupName" :label="$t('collection.group')" width="120" />
        <el-table-column prop="status" :label="$t('common.status')" width="80">
          <template #default="{ row }">
            <DictTag dict-type="dc_account_status" :value="row.status" />
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.operation')" width="270">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleCollect(row.id)">{{ $t('collection.collect') }}</el-button>
            <el-button type="primary" link @click="handleViewArticles(row)">{{ $t('collection.viewArticles') }}</el-button>
            <el-button type="primary" link @click="handleEditAccount(row)">{{ $t('common.edit') }}</el-button>
            <el-popconfirm :title="$t('common.confirmDelete')" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link>{{ $t('common.delete') }}</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showAddDialog" :title="editingAccount ? $t('common.edit') : $t('collection.addAccount')" width="500px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item :label="$t('collection.platform')" prop="platform">
          <el-select v-model="form.platform" :placeholder="$t('collection.selectPlatform')">
            <el-option v-for="p in platformDict" :key="p.dictValue" :label="p.dictLabel" :value="p.dictValue" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('collection.accountName')" prop="accountName">
          <el-input v-model="form.accountName" :placeholder="$t('collection.inputAccountName')" />
        </el-form-item>
        <el-form-item :label="$t('collection.accountId')">
          <el-input v-model="form.accountId" :placeholder="$t('collection.inputAccountId')" />
        </el-form-item>
        <el-form-item :label="$t('collection.accountUrl')">
          <el-input v-model="form.accountUrl" :placeholder="$t('collection.inputAccountUrl')" />
        </el-form-item>
        <el-form-item :label="$t('collection.group')">
          <el-input v-model="form.groupName" :placeholder="$t('collection.inputGroupName')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleAdd">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showArticlesDialog" :title="articlesTitle" width="800px" top="5vh">
      <el-table :data="articles" v-loading="loadingArticles" stripe>
        <el-table-column prop="title" :label="$t('collection.articleTitle')" min-width="200" show-overflow-tooltip />
        <el-table-column prop="author" :label="$t('collection.author')" width="100" />
        <el-table-column prop="viewCount" :label="$t('collection.views')" width="80" />
        <el-table-column prop="likeCount" :label="$t('collection.likes')" width="80" />
        <el-table-column prop="collectedTime" :label="$t('collection.collectedTime')" width="160" />
        <el-table-column :label="$t('common.operation')" width="160">
          <template #default="{ row }">
            <el-button type="primary" link @click="showArticleDetail(row)">{{ $t('collection.view') }}</el-button>
            <el-button type="danger" link @click="handleDeleteArticle(row.id)">{{ $t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="showDetailDialog" :title="$t('collection.articleDetail')" width="700px">
      <div v-if="currentArticle">
        <h3>{{ currentArticle.title }}</h3>
        <el-descriptions :column="2" border style="margin: 16px 0">
          <el-descriptions-item :label="$t('collection.platform')">{{ currentArticle.platform }}</el-descriptions-item>
          <el-descriptions-item :label="$t('collection.author')">{{ currentArticle.author }}</el-descriptions-item>
          <el-descriptions-item :label="$t('collection.viewCount')">{{ currentArticle.viewCount }}</el-descriptions-item>
          <el-descriptions-item :label="$t('collection.likeCount')">{{ currentArticle.likeCount }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="currentArticle.url" style="margin-bottom: 12px">
          <el-link type="primary" :href="currentArticle.url" target="_blank">{{ $t('collection.viewOriginal') }}</el-link>
        </div>
        <el-divider />
        <div class="article-content" style="max-height: 400px; overflow-y: auto">{{ currentArticle.content || $t('collection.noContent') }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { getTargetAccounts, addTargetAccount, updateTargetAccount, deleteTargetAccount, triggerCollect, getCollectedArticles, deleteCollectedArticle } from '@/api/collection'
import { useDict } from '@/composables/useDict'

const { t } = useI18n()

const { dict: platformDict } = useDict('collection_platform')

const loading = ref(false)
const showAddDialog = ref(false)
const accounts = ref<any[]>([])
const formRef = ref<FormInstance>()

const showArticlesDialog = ref(false)
const articlesTitle = ref('')
const articles = ref<any[]>([])
const loadingArticles = ref(false)

const showDetailDialog = ref(false)
const currentArticle = ref<any>(null)

const editingAccount = ref<any>(null)
const selectedAccountId = ref<number | null>(null)

const form = reactive({ platform: '', accountName: '', accountId: '', accountUrl: '', groupName: '' })
const rules = computed(() => ({
  platform: [{ required: true, message: t('collection.pleaseSelectPlatform'), trigger: 'change' }],
  accountName: [{ required: true, message: t('collection.pleaseInputAccountName'), trigger: 'blur' }],
}))

const loadAccounts = async () => {
  loading.value = true
  try {
    const res = await getTargetAccounts()
    accounts.value = res.data
  } finally { loading.value = false }
}

const handleAddAccount = () => {
  editingAccount.value = null
  showAddDialog.value = true
}

const handleEditAccount = (row: any) => {
  editingAccount.value = row
  form.platform = row.platform
  form.accountName = row.accountName
  form.accountId = row.accountId || ''
  form.accountUrl = row.accountUrl || ''
  form.groupName = row.groupName || ''
  showAddDialog.value = true
}

const handleAdd = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    if (editingAccount.value) {
      await updateTargetAccount({ ...form, id: editingAccount.value.id })
      ElMessage.success(t('common.updateSuccess'))
    } else {
      await addTargetAccount(form)
      ElMessage.success(t('collection.addSuccess'))
    }
    showAddDialog.value = false
    editingAccount.value = null
    loadAccounts()
  } catch { ElMessage.error(t('common.operationFailed')) }
}

const handleDelete = async (id: number) => {
  try {
    await deleteTargetAccount(id)
    ElMessage.success(t('collection.deleteSuccess'))
    loadAccounts()
  } catch { ElMessage.error(t('collection.deleteFailed')) }
}

const handleCollect = async (id: number) => {
  try {
    await triggerCollect(id)
    ElMessage.success(t('collection.collectSuccess'))
    let attempts = 0
    const maxAttempts = 10
    const poll = async () => {
      await new Promise(resolve => setTimeout(resolve, 3000))
      await loadAccounts()
      attempts++
      if (attempts < maxAttempts) {
        poll()
      }
    }
    poll()
  } catch { ElMessage.error(t('collection.collectFailed')) }
}

const handleViewArticles = async (row: any) => {
  selectedAccountId.value = row.id
  articlesTitle.value = `${row.accountName} - ${t('collection.collectedArticles')}`
  showArticlesDialog.value = true
  loadingArticles.value = true
  try {
    const res = await getCollectedArticles(row.id)
    articles.value = res.data.list || []
  } catch {
    articles.value = []
  } finally { loadingArticles.value = false }
}

const handleDeleteArticle = async (id: number) => {
  try {
    await ElMessageBox.confirm(t('common.confirmDelete'), t('common.confirm'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning',
    })
    await deleteCollectedArticle(id)
    ElMessage.success(t('common.deleteSuccess'))
    if (selectedAccountId.value) {
      const res = await getCollectedArticles(selectedAccountId.value)
      articles.value = res.data.list || []
    }
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(t('common.deleteFailed'))
    }
  }
}

const showArticleDetail = (article: any) => {
  currentArticle.value = article
  showDetailDialog.value = true
}

const resetForm = () => {
  editingAccount.value = null
  form.platform = ''; form.accountName = ''; form.accountId = ''; form.accountUrl = ''; form.groupName = ''
}
onMounted(() => { loadAccounts() })
</script>

<style scoped lang="scss">
.card-header { display: flex; justify-content: space-between; align-items: center; }
.article-content { line-height: 1.8; color: #303133; white-space: pre-wrap; }
</style>
