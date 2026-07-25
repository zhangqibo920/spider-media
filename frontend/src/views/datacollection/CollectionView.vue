<template>
  <div class="data-collection">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>对标账号管理</span>
          <el-button type="primary" @click="showAddDialog = true">
            <el-icon><Plus /></el-icon> 添加账号
          </el-button>
        </div>
      </template>

      <el-table :data="accounts" v-loading="loading" stripe>
        <el-table-column prop="platform" label="平台" width="100">
          <template #default="{ row }">
            <el-tag>{{ row.platform }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="accountName" label="账号名称" />
        <el-table-column prop="accountId" label="账号ID" />
        <el-table-column prop="groupName" label="分组" width="120" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <DictTag dict-type="dc_account_status" :value="row.status" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleCollect(row.id)">采集</el-button>
            <el-button type="primary" link @click="handleViewArticles(row)">查看文章</el-button>
            <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showAddDialog" title="添加对标账号" width="500px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="平台" prop="platform">
          <el-select v-model="form.platform" placeholder="选择平台">
            <el-option v-for="p in platformDict" :key="p.dictValue" :label="p.dictLabel" :value="p.dictValue" />
          </el-select>
        </el-form-item>
        <el-form-item label="账号名称" prop="accountName">
          <el-input v-model="form.accountName" placeholder="输入账号名称" />
        </el-form-item>
        <el-form-item label="账号ID">
          <el-input v-model="form.accountId" placeholder="输入账号ID" />
        </el-form-item>
        <el-form-item label="账号链接">
          <el-input v-model="form.accountUrl" placeholder="输入主页链接" />
        </el-form-item>
        <el-form-item label="分组">
          <el-input v-model="form.groupName" placeholder="输入分组名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showArticlesDialog" :title="articlesTitle" width="800px" top="5vh">
      <el-table :data="articles" v-loading="loadingArticles" stripe>
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="author" label="作者" width="100" />
        <el-table-column prop="viewCount" label="阅读" width="80" />
        <el-table-column prop="likeCount" label="点赞" width="80" />
        <el-table-column prop="collectedTime" label="采集时间" width="160" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" link @click="showArticleDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="showDetailDialog" title="文章详情" width="700px">
      <div v-if="currentArticle">
        <h3>{{ currentArticle.title }}</h3>
        <el-descriptions :column="2" border style="margin: 16px 0">
          <el-descriptions-item label="平台">{{ currentArticle.platform }}</el-descriptions-item>
          <el-descriptions-item label="作者">{{ currentArticle.author }}</el-descriptions-item>
          <el-descriptions-item label="阅读量">{{ currentArticle.viewCount }}</el-descriptions-item>
          <el-descriptions-item label="点赞量">{{ currentArticle.likeCount }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="currentArticle.url" style="margin-bottom: 12px">
          <el-link type="primary" :href="currentArticle.url" target="_blank">查看原文</el-link>
        </div>
        <el-divider />
        <div class="article-content" style="max-height: 400px; overflow-y: auto">{{ currentArticle.content || '暂无内容' }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { getTargetAccounts, addTargetAccount, deleteTargetAccount, triggerCollect, getCollectedArticles } from '@/api/collection'
import { useDict } from '@/composables/useDict'
import DictTag from '@/components/DictTag.vue'

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

const form = reactive({ platform: '', accountName: '', accountId: '', accountUrl: '', groupName: '' })
const rules = {
  platform: [{ required: true, message: '请选择平台', trigger: 'change' }],
  accountName: [{ required: true, message: '请输入账号名称', trigger: 'blur' }],
}

const loadAccounts = async () => {
  loading.value = true
  try {
    const res = await getTargetAccounts()
    accounts.value = res.data
  } finally { loading.value = false }
}

const handleAdd = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    await addTargetAccount(form)
    ElMessage.success('添加成功')
    showAddDialog.value = false
    loadAccounts()
  } catch { ElMessage.error('添加失败') }
}

const handleDelete = async (id: number) => {
  try {
    await deleteTargetAccount(id)
    ElMessage.success('删除成功')
    loadAccounts()
  } catch { ElMessage.error('删除失败') }
}

const handleCollect = async (id: number) => {
  try {
    await triggerCollect(id)
    ElMessage.success('采集任务已触发，请稍后刷新查看')
  } catch { ElMessage.error('触发失败') }
}

const handleViewArticles = async (row: any) => {
  articlesTitle.value = `${row.accountName} - 采集文章`
  showArticlesDialog.value = true
  loadingArticles.value = true
  try {
    const res = await getCollectedArticles(row.id)
    articles.value = res.data.list || []
  } catch {
    articles.value = []
  } finally { loadingArticles.value = false }
}

const showArticleDetail = (article: any) => {
  currentArticle.value = article
  showDetailDialog.value = true
}

const resetForm = () => { form.platform = ''; form.accountName = ''; form.accountId = ''; form.accountUrl = ''; form.groupName = '' }
onMounted(() => { loadAccounts() })
</script>

<style scoped lang="scss">
.card-header { display: flex; justify-content: space-between; align-items: center; }
.article-content { line-height: 1.8; color: #303133; white-space: pre-wrap; }
</style>
