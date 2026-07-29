<template>
  <div class="hot-feed">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ t('hotFeed.title') }}</span>
          <div class="filter-bar">
            <el-input v-model="keywordFilter" :placeholder="t('hotFeed.searchTopic')" clearable style="width: 180px" size="small" @clear="loadTopics" @keyup.enter="loadTopics" />
            <el-select v-model="sourceFilter" :placeholder="t('hotFeed.source')" clearable style="width: 120px" size="small" @change="loadTopics">
              <el-option :label="t('keyword.allSources')" value="" />
              <el-option v-for="s in sources" :key="s" :label="s" :value="s" />
            </el-select>
            <el-select v-model="sortBy" style="width: 120px" size="small" @change="loadTopics">
              <el-option :label="t('hotFeed.sortByHot')" value="hot_score" />
              <el-option :label="t('hotFeed.sortByRelevance')" value="relevance" />
              <el-option :label="t('hotFeed.sortByTime')" value="time" />
            </el-select>
            <el-button size="small" @click="loadTopics" :icon="Search">{{ t('hotFeed.filter') }}</el-button>
          </div>
        </div>
      </template>
      <el-table :data="topics" v-loading="loading" stripe max-height="600">
        <el-table-column prop="title" :label="t('hotFeed.topic')" min-width="250" show-overflow-tooltip />
        <el-table-column prop="source" :label="t('hotFeed.source')" width="90">
          <template #default="{ row }">
            <el-tag size="small">{{ row.source || row.platform }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('hotFeed.hotScore')" width="100" sortable="custom" prop="hotScore">
          <template #default="{ row }">
            {{ formatHot(row.hotScore) }}
          </template>
        </el-table-column>
        <el-table-column :label="t('hotFeed.aiScore')" width="80">
          <template #default="{ row }">
            <el-rate :model-value="row.aiScore || 0" disabled size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="relevance" :label="t('hotFeed.relevance')" width="80">
          <template #default="{ row }">
            <el-tag :type="relevanceType(row.relevance)" size="small">
              {{ row.relevance ?? '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="aiSummary" :label="t('hotFeed.summary')" min-width="200" show-overflow-tooltip />
        <el-table-column :label="t('common.operation')" width="100">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleGenerate(row.id)">{{ t('hotFeed.generateArticle') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getHotTopics } from '@/api/hotmonitor'
import { generateArticle } from '@/api/ai'

const { t } = useI18n()

const loading = ref(false)
const topics = ref<any[]>([])
const keywordFilter = ref('')
const sourceFilter = ref('')
const sortBy = ref('hot_score')
const sources = ['weibo', 'douyin', 'zhihu', 'toutiao', 'baidu', 'bilibili', 'hackernews', 'github']

const formatHot = (v: number | null) => {
  if (v == null) return '-'
  if (v >= 100000000) return (v / 100000000).toFixed(1) + t('hotFeed.hundredMillion')
  if (v >= 10000) return (v / 10000).toFixed(v % 10000 === 0 ? 0 : 1) + t('aiCreation.tenThousand')
  return v.toLocaleString()
}

const relevanceType = (v: number | null) => {
  if (v == null) return 'info'
  if (v >= 70) return 'success'
  if (v >= 40) return 'warning'
  return 'danger'
}

const loadTopics = async () => {
  loading.value = true
  try {
    const params: any = { sortBy: sortBy.value, sortOrder: 'desc' }
    if (keywordFilter.value) params.keyword = keywordFilter.value
    if (sourceFilter.value) params.source = sourceFilter.value
    const res = await getHotTopics(params)
    topics.value = res.data
  } finally {
    loading.value = false
  }
}

const handleGenerate = async (topicId: number) => {
  try {
    await generateArticle(topicId)
    ElMessage.success(t('aiCreation.generateTriggered'))
  } catch {
    ElMessage.error(t('aiCreation.generateFailed'))
  }
}

onMounted(() => { loadTopics() })
</script>

<style scoped lang="scss">
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}
.filter-bar {
  display: flex;
  gap: 8px;
  align-items: center;
}
</style>
