<template>
  <div class="hot-feed">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>热点信息流</span>
          <div class="filter-bar">
            <el-input v-model="keywordFilter" placeholder="搜索话题" clearable style="width: 180px" size="small" @clear="loadTopics" @keyup.enter="loadTopics" />
            <el-select v-model="sourceFilter" placeholder="来源" clearable style="width: 120px" size="small" @change="loadTopics">
              <el-option label="全部" value="" />
              <el-option v-for="s in sources" :key="s" :label="s" :value="s" />
            </el-select>
            <el-select v-model="sortBy" style="width: 120px" size="small" @change="loadTopics">
              <el-option label="按热度" value="hot_score" />
              <el-option label="按相关性" value="relevance" />
              <el-option label="按时间" value="time" />
            </el-select>
            <el-button size="small" @click="loadTopics" :icon="Search">筛选</el-button>
          </div>
        </div>
      </template>
      <el-table :data="topics" v-loading="loading" stripe max-height="600">
        <el-table-column prop="title" label="话题" min-width="250" show-overflow-tooltip />
        <el-table-column prop="source" label="来源" width="90">
          <template #default="{ row }">
            <el-tag size="small">{{ row.source || row.platform }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="hotScore" label="热度" width="80" sortable="custom" />
        <el-table-column label="AI评分" width="80">
          <template #default="{ row }">
            <el-rate :model-value="row.aiScore || 0" disabled size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="relevance" label="相关性" width="80">
          <template #default="{ row }">
            <el-tag :type="relevanceType(row.relevance)" size="small">
              {{ row.relevance ?? '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="aiSummary" label="摘要" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleGenerate(row.id)">生成文章</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getHotTopics } from '@/api/hotmonitor'
import { generateArticle } from '@/api/ai'

const loading = ref(false)
const topics = ref<any[]>([])
const keywordFilter = ref('')
const sourceFilter = ref('')
const sortBy = ref('hot_score')
const sources = ['weibo', 'douyin', 'zhihu', 'toutiao', 'baidu', 'bilibili', 'hackernews', 'github']

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
    ElMessage.success('AI创作任务已触发')
  } catch {
    ElMessage.error('生成失败')
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
