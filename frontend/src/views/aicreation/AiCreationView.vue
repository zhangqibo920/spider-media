<template>
  <div class="ai-creation">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>全网热点</span>
              <el-select v-model="selectedPlatform" placeholder="选择平台" size="small" style="width: 120px">
                <el-option label="抖音" value="douyin" />
                <el-option label="微博" value="weibo" />
                <el-option label="网易" value="netease" />
              </el-select>
              <el-button type="primary" size="small" @click="handleFetchHot" :loading="fetching">
                抓取热点
              </el-button>
            </div>
          </template>

          <el-table :data="hotTopics" v-loading="loadingTopics" max-height="400">
            <el-table-column prop="title" label="热点话题" show-overflow-tooltip />
            <el-table-column prop="hotScore" label="热度" width="80" />
            <el-table-column prop="platform" label="平台" width="80">
              <template #default="{ row }">
                <el-tag size="small">{{ row.platform }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleGenerate(row.id)">生成文章</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <span>AI生成文章</span>
          </template>

          <el-table :data="articles" v-loading="loadingArticles" max-height="400">
            <el-table-column prop="title" label="标题" show-overflow-tooltip />
            <el-table-column prop="modelUsed" label="模型" width="100" />
            <el-table-column prop="wordCount" label="字数" width="80" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 'COMPLETED' ? 'success' : 'warning'" size="small">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getHotTopics, fetchHotTopics, generateArticle, getGeneratedArticles } from '@/api/ai'
import type { HotTopic, GeneratedArticle } from '@/types'

const selectedPlatform = ref('douyin')
const loadingTopics = ref(false)
const loadingArticles = ref(false)
const fetching = ref(false)
const hotTopics = ref<HotTopic[]>([])
const articles = ref<GeneratedArticle[]>([])

const loadHotTopics = async () => {
  loadingTopics.value = true
  try {
    const res = await getHotTopics()
    hotTopics.value = res.data
  } finally {
    loadingTopics.value = false
  }
}

const loadArticles = async () => {
  loadingArticles.value = true
  try {
    const res = await getGeneratedArticles()
    articles.value = res.data.list
  } finally {
    loadingArticles.value = false
  }
}

const handleFetchHot = async () => {
  fetching.value = true
  try {
    await fetchHotTopics(selectedPlatform.value)
    ElMessage.success('热点抓取任务已触发')
    loadHotTopics()
  } catch {
    ElMessage.error('抓取失败')
  } finally {
    fetching.value = false
  }
}

const handleGenerate = async (topicId: number) => {
  try {
    await generateArticle(topicId)
    ElMessage.success('AI创作任务已触发')
    loadArticles()
  } catch {
    ElMessage.error('生成失败')
  }
}

onMounted(() => {
  loadHotTopics()
  loadArticles()
})
</script>

<style scoped lang="scss">
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}
</style>
