<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-title">{{ $t('dashboard.targetAccounts') }}</div>
              <div class="stat-value">{{ stats.targetAccounts }}</div>
            </div>
            <el-icon class="stat-icon" :size="40"><User /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-title">{{ $t('dashboard.collectedArticles') }}</div>
              <div class="stat-value">{{ stats.articles }}</div>
            </div>
            <el-icon class="stat-icon" :size="40"><Document /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-title">{{ $t('dashboard.aiGenerated') }}</div>
              <div class="stat-value">{{ stats.aiGenerated }}</div>
            </div>
            <el-icon class="stat-icon" :size="40"><MagicStick /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-title">{{ $t('dashboard.published') }}</div>
              <div class="stat-value">{{ stats.published }}</div>
            </div>
            <el-icon class="stat-icon" :size="40"><Promotion /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
          <el-card>
          <template #header>
            <span>{{ $t('dashboard.quickActions') }}</span>
          </template>
          <el-space wrap>
            <el-button type="primary" @click="$router.push('/collection')">
              <el-icon><Download /></el-icon> {{ $t('dashboard.dataCollection') }}
            </el-button>
            <el-button type="success" @click="$router.push('/ai-creation')">
              <el-icon><MagicStick /></el-icon> {{ $t('dashboard.aiCreation') }}
            </el-button>
            <el-button type="warning" @click="$router.push('/publish')">
              <el-icon><Promotion /></el-icon> {{ $t('dashboard.contentPublish') }}
            </el-button>
            <el-button @click="$router.push('/scheduler')">
              <el-icon><Timer /></el-icon> {{ $t('dashboard.taskSchedule') }}
            </el-button>
          </el-space>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>{{ $t('dashboard.systemStatus') }}</span>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item :label="$t('dashboard.systemVersion')">V1.0.0</el-descriptions-item>
            <el-descriptions-item :label="$t('dashboard.runningStatus')">
              <el-tag type="success">{{ $t('dashboard.running') }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('dashboard.aiModel')">{{ $t('dashboard.aiModelDetail') }}</el-descriptions-item>
            <el-descriptions-item :label="$t('dashboard.hotTopics')">{{ stats.hotTopics }} {{ $t('dashboard.articleUnit') }}</el-descriptions-item>
            <el-descriptions-item :label="$t('dashboard.publishAccounts')">{{ stats.publishAccounts }} {{ $t('dashboard.accountUnit') }}</el-descriptions-item>
            <el-descriptions-item :label="$t('dashboard.scheduledTasks')">{{ stats.scheduledTasks }} {{ $t('dashboard.taskUnit') }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { getDashboardStats } from '@/api/dashboard'

const { t } = useI18n()
const stats = ref({
  targetAccounts: 0,
  articles: 0,
  aiGenerated: 0,
  published: 0,
  hotTopics: 0,
  publishAccounts: 0,
  scheduledTasks: 0,
})

onMounted(async () => {
  try {
    const res = await getDashboardStats()
    stats.value = res.data
  } catch {
    ElMessage.error(t('common.operationFailed'))
  }
})
</script>

<style scoped lang="scss">
.dashboard {
  .stat-card {
    .stat-content {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .stat-info {
        .stat-title {
          font-size: 14px;
          color: #909399;
        }

        .stat-value {
          font-size: 32px;
          font-weight: bold;
          color: #303133;
          margin-top: 8px;
        }
      }

      .stat-icon {
        color: #409eff;
        opacity: 0.5;
      }
    }
  }
}
</style>
