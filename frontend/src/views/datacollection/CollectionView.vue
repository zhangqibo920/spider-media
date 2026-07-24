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
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '监控中' : '已暂停' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleCollect(row.id)">采集</el-button>
            <el-button type="primary" link @click="handleViewArticles(row.id)">查看文章</el-button>
            <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Add Account Dialog -->
    <el-dialog v-model="showAddDialog" title="添加对标账号" width="500px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="平台" prop="platform">
          <el-select v-model="form.platform" placeholder="选择平台">
            <el-option label="微信公众号" value="wechat" />
            <el-option label="百家号" value="baijia" />
            <el-option label="头条号" value="toutiao" />
            <el-option label="小红书" value="xiaohongshu" />
            <el-option label="抖音" value="douyin" />
            <el-option label="知乎" value="zhihu" />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { getTargetAccounts, addTargetAccount, deleteTargetAccount, triggerCollect } from '@/api/collection'
import type { TargetAccount } from '@/types'

const loading = ref(false)
const showAddDialog = ref(false)
const accounts = ref<TargetAccount[]>([])
const formRef = ref<FormInstance>()

const form = reactive({
  platform: '',
  accountName: '',
  accountId: '',
  accountUrl: '',
  groupName: '',
})

const rules = {
  platform: [{ required: true, message: '请选择平台', trigger: 'change' }],
  accountName: [{ required: true, message: '请输入账号名称', trigger: 'blur' }],
}

const loadAccounts = async () => {
  loading.value = true
  try {
    const res = await getTargetAccounts()
    accounts.value = res.data
  } finally {
    loading.value = false
  }
}

const handleAdd = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  try {
    await addTargetAccount(form)
    ElMessage.success('添加成功')
    showAddDialog.value = false
    loadAccounts()
  } catch {
    ElMessage.error('添加失败')
  }
}

const handleDelete = async (id: number) => {
  try {
    await deleteTargetAccount(id)
    ElMessage.success('删除成功')
    loadAccounts()
  } catch {
    ElMessage.error('删除失败')
  }
}

const handleCollect = async (id: number) => {
  try {
    await triggerCollect(id)
    ElMessage.success('采集任务已触发')
  } catch {
    ElMessage.error('触发失败')
  }
}

const handleViewArticles = (id: number) => {
  ElMessage.info('文章查看功能开发中')
}

const resetForm = () => {
  form.platform = ''
  form.accountName = ''
  form.accountId = ''
  form.accountUrl = ''
  form.groupName = ''
}

onMounted(() => {
  loadAccounts()
})
</script>

<style scoped lang="scss">
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
