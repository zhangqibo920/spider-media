<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-title">对标账号</div>
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
              <div class="stat-title">采集文章</div>
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
              <div class="stat-title">AI生成</div>
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
              <div class="stat-title">已发布</div>
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
            <span>快捷操作</span>
          </template>
          <el-space wrap>
            <el-button type="primary" @click="$router.push('/collection')">
              <el-icon><Download /></el-icon> 数据采集
            </el-button>
            <el-button type="success" @click="$router.push('/ai-creation')">
              <el-icon><MagicStick /></el-icon> AI创作
            </el-button>
            <el-button type="warning" @click="$router.push('/publish')">
              <el-icon><Promotion /></el-icon> 内容发布
            </el-button>
            <el-button @click="$router.push('/scheduler')">
              <el-icon><Timer /></el-icon> 任务调度
            </el-button>
          </el-space>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>系统状态</span>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="系统版本">V1.0.0</el-descriptions-item>
            <el-descriptions-item label="运行状态">
              <el-tag type="success">正常运行</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="AI模型">DeepSeek / 智谱</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const stats = ref({
  targetAccounts: 0,
  articles: 0,
  aiGenerated: 0,
  published: 0,
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
