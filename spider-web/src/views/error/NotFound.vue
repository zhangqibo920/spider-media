<template>
  <div class="not-found">
    <el-result icon="warning" title="404" :sub-title="$t('notFound.subtitle')">
      <template #extra>
        <el-button type="primary" @click="$router.push('/dashboard')">{{ $t('notFound.backHome') }}</el-button>
      </template>
    </el-result>
  </div>
</template>

<style scoped>
.not-found {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
}
</style>
