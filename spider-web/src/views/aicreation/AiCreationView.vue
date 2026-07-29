<template>
  <div class="ai-creation">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>{{ $t('aiCreation.hotTopics') }}</span>
              <el-select v-model="selectedPlatform" :placeholder="$t('aiCreation.selectPlatform')" size="small" style="width: 120px">
                <el-option v-for="p in platformDict" :key="p.dictValue" :label="p.dictLabel" :value="p.dictValue" />
              </el-select>
              <el-button type="primary" size="small" @click="handleFetchHot" :loading="fetching">
                {{ $t('aiCreation.fetchHot') }}
              </el-button>
            </div>
          </template>
          <el-table :data="hotTopics" v-loading="loadingTopics" max-height="400">
            <el-table-column prop="title" :label="$t('aiCreation.hotTopic')" show-overflow-tooltip />
             <el-table-column :label="$t('aiCreation.hotScore')" width="100">
                <template #default="{ row }">
                  {{ row.hotScore >= 10000 ? (row.hotScore / 10000).toFixed((row.hotScore % 10000) === 0 ? 0 : 1) + $t('aiCreation.tenThousand') : row.hotScore?.toLocaleString() ?? '-' }}
                </template>
              </el-table-column>
            <el-table-column prop="platform" :label="$t('collection.platform')" width="80">
              <template #default="{ row }"><el-tag size="small">{{ row.platform }}</el-tag></template>
            </el-table-column>
            <el-table-column :label="$t('common.operation')" width="80">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleGenerate(row.id)">{{ $t('hotFeed.generateArticle') }}</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header><span>{{ $t('aiCreation.aiArticles') }}</span></template>
          <el-table :data="articles" v-loading="loadingArticles" max-height="400">
            <el-table-column prop="title" :label="$t('aiCreation.articleTitle')" show-overflow-tooltip />
            <el-table-column prop="modelUsed" :label="$t('aiCreation.model')" width="100" />
            <el-table-column prop="wordCount" :label="$t('aiCreation.wordCount')" width="80" />
            <el-table-column prop="status" :label="$t('common.status')" width="80">
              <template #default="{ row }">
                <DictTag dict-type="ac_article_status" :value="row.status" size="small" />
              </template>
            </el-table-column>
            <el-table-column :label="$t('common.operation')" width="80">
              <template #default="{ row }">
                <el-button type="primary" link @click="showArticleDetail(row)">{{ $t('aiCreation.viewArticle') }}</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="showDetailDialog" :title="$t('aiCreation.articleDetail')" width="700px" top="5vh">
      <div v-if="currentArticle">
        <h3>{{ currentArticle.title }}</h3>
        <el-descriptions :column="2" border style="margin: 16px 0">
          <el-descriptions-item :label="$t('aiCreation.model')">{{ currentArticle.modelUsed }}</el-descriptions-item>
          <el-descriptions-item :label="$t('aiCreation.wordCount')">{{ currentArticle.wordCount }}</el-descriptions-item>
          <el-descriptions-item :label="$t('common.status')">
            <DictTag dict-type="ac_article_status" :value="currentArticle.status" size="small" />
          </el-descriptions-item>
          <el-descriptions-item :label="$t('common.createTime')">{{ currentArticle.createTime }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="currentArticle.summary" style="margin-bottom: 12px">
          <strong>{{ $t('aiCreation.summary') }}：</strong>{{ currentArticle.summary }}
        </div>
        <el-divider />
        <div class="article-content" style="max-height: 400px; overflow-y: auto">{{ currentArticle.content || $t('aiCreation.noContent') }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { getHotTopics, fetchHotTopics, generateArticle, getGeneratedArticles } from '@/api/ai'
import { useDict } from '@/composables/useDict'

const { t } = useI18n()

const { dict: platformDict } = useDict('hot_topic_platform')

const selectedPlatform = ref('douyin')
const loadingTopics = ref(false)
const loadingArticles = ref(false)
const fetching = ref(false)
const hotTopics = ref<any[]>([])
const articles = ref<any[]>([])
const showDetailDialog = ref(false)
const currentArticle = ref<any>(null)

const loadHotTopics = async () => {
  loadingTopics.value = true
  try { const res = await getHotTopics(); hotTopics.value = res.data } finally { loadingTopics.value = false }
}
const loadArticles = async () => {
  loadingArticles.value = true
  try { const res = await getGeneratedArticles(); articles.value = res.data.list } finally { loadingArticles.value = false }
}
const handleFetchHot = async () => {
  fetching.value = true
  try {
    await fetchHotTopics(selectedPlatform.value)
    ElMessage.success(t('aiCreation.fetchHotTriggered'))
    let attempts = 0
    const maxAttempts = 10
    const poll = async () => {
      await new Promise(resolve => setTimeout(resolve, 2000))
      await loadHotTopics()
      attempts++
      if (hotTopics.value.length === 0 && attempts < maxAttempts) {
        poll()
      }
    }
    poll()
  } catch { ElMessage.error(t('aiCreation.fetchHotFailed')) } finally { fetching.value = false }
}
const handleGenerate = async (topicId: number) => {
  try { await generateArticle(topicId); ElMessage.success(t('aiCreation.generateTriggered')); loadArticles() } catch { ElMessage.error(t('aiCreation.generateFailed')) }
}
const showArticleDetail = (article: any) => { currentArticle.value = article; showDetailDialog.value = true }

onMounted(() => { loadHotTopics(); loadArticles() })
</script>

<style scoped lang="scss">
.card-header { display: flex; justify-content: space-between; align-items: center; gap: 8px; }
.article-content { line-height: 1.8; color: #303133; white-space: pre-wrap; }
</style>
