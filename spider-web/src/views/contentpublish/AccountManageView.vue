<template>
  <div class="account-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ $t('account.platformAccounts') }}</span>
          <div class="header-actions">
            <el-dropdown @command="handleAddCommand">
              <el-button type="primary">
                <el-icon><Plus /></el-icon> {{ $t('account.addAccount') }}
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="wechat_mp">
                    <el-icon><ChatDotRound /></el-icon> {{ $t('account.platformWechatMp') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="toutiao">
                    <el-icon><Iphone /></el-icon> {{ $t('account.platformToutiao') }}{{ $t('account.addMethodScan') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="baijiahao">
                    <el-icon><Document /></el-icon> {{ $t('account.platformBaijiahao') }}{{ $t('account.addMethodScan') }}
                  </el-dropdown-item>
                  <el-dropdown-item divided command="toutiao_manual">
                    <el-icon><Edit /></el-icon> {{ $t('account.platformToutiao') }}{{ $t('account.addMethodManual') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="baijiahao_manual">
                    <el-icon><Edit /></el-icon> {{ $t('account.platformBaijiahao') }}{{ $t('account.addMethodManual') }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </template>

      <!-- 平台账号卡片展示 -->
      <div class="platform-section" v-for="(accounts, platform) in groupedAccounts" :key="platform">
        <div class="platform-header">
          <el-tag size="large" :type="getPlatformTagType(platform)">
            {{ getPlatformLabel(platform) }}
          </el-tag>
          <span class="account-count">{{ accounts.length }}{{ $t('account.count') }}</span>
        </div>

        <div class="account-cards">
          <el-card
            v-for="account in accounts"
            :key="account.id"
            class="account-card"
            :class="{ 'account-disabled': account.status === '1' }"
          >
            <div class="account-card-content">
              <div class="account-icon">
                <el-avatar :size="48" :style="{ backgroundColor: getPlatformColor(platform) }">
                  <el-icon :size="24"><User /></el-icon>
                </el-avatar>
              </div>
              <div class="account-info">
                <div class="account-name">{{ account.accountName }}</div>
                <div class="account-detail">
                  <span v-if="platform === 'wechat_mp'">
                    AppID: {{ maskSecret(account.appId) }}
                  </span>
                  <span v-else>
                    {{ account.accountId || $t('account.noAccountId') }}
                  </span>
                </div>
                <div class="account-status">
                  <el-tag :type="account.status === '0' ? 'success' : 'danger'" size="small">
                    {{ account.status === '0' ? $t('account.normal') : $t('account.disabled') }}
                  </el-tag>
                  <span v-if="account.tokenExpireTime" class="expire-time">
                    {{ $t('account.tokenExpire') }}: {{ formatTime(account.tokenExpireTime) }}
                  </span>
                  <span v-if="account.lastLoginTime" class="expire-time">
                    {{ $t('account.lastLoginTime') }}: {{ formatTime(account.lastLoginTime) }}
                  </span>
                </div>
              </div>
              <div class="account-actions">
                <el-button type="primary" link @click="openEditDialog(account)">
                  {{ $t('common.edit') }}
                </el-button>
                <el-button type="warning" link @click="handleTestConnection(account)">
                  {{ $t('account.testConnection') }}
                </el-button>
                <el-popconfirm
                  :title="$t('account.confirmDelete')"
                  @confirm="handleDelete(account.id)"
                >
                  <template #reference>
                    <el-button type="danger" link>{{ $t('common.delete') }}</el-button>
                  </template>
                </el-popconfirm>
              </div>
            </div>
          </el-card>
        </div>
      </div>

      <el-empty v-if="accounts.length === 0" :description="$t('account.noAccounts')">
        <el-button type="primary" @click="handleAddCommand('wechat_mp')">
          {{ $t('account.addFirstAccount') }}
        </el-button>
      </el-empty>
    </el-card>

    <!-- 新增/编辑账号弹窗（微信公众号） -->
    <el-dialog
      v-model="showDialog"
      :title="isEditing ? $t('account.editAccount') : $t('account.addAccount')"
      width="550px"
      @closed="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-form-item :label="$t('account.platform')" prop="platform">
          <el-select
            v-model="form.platform"
            :placeholder="$t('account.selectPlatform')"
            :disabled="isEditing"
          >
            <el-option
              v-for="p in platformOptions"
              :key="p.value"
              :label="p.label"
              :value="p.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item :label="$t('account.accountName')" prop="accountName">
          <el-input
            v-model="form.accountName"
            :placeholder="$t('account.accountNamePlaceholder')"
          />
        </el-form-item>

        <!-- 微信公众号字段 -->
        <template v-if="form.platform === 'wechat_mp'">
          <el-form-item label="AppID" prop="appId">
            <el-input
              v-model="form.appId"
              :placeholder="$t('account.validateAppId')"
            />
          </el-form-item>
          <el-form-item label="AppSecret" prop="appSecret">
            <el-input
              v-model="form.appSecret"
              type="password"
              show-password
              :placeholder="$t('account.validateAppSecret')"
            />
          </el-form-item>
        </template>
      </el-form>

      <template #footer>
        <el-button @click="showDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEditing ? $t('common.update') : $t('common.confirm') }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 扫码登录弹窗 -->
    <BindAccountDialog
      v-model="showBindDialog"
      :platform="bindPlatform"
      @success="onBindSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useI18n } from 'vue-i18n'
import BindAccountDialog from '@/components/BindAccountDialog.vue'
import {
  getPlatformAccounts,
  addPlatformAccount,
  updatePlatformAccount,
  deletePlatformAccount,
  testAccountConnection
} from '@/api/publish'
import { formatTime } from '@/utils/date'

const { t } = useI18n()

// 平台选项配置
const platformOptions = computed(() => [
  { value: 'wechat_mp', label: t('account.platformWechatMp') },
  { value: 'toutiao', label: t('account.platformToutiao') },
  { value: 'baijiahao', label: t('account.platformBaijiahao') }
])

// 平台配置
const platformConfig: Record<string, { color: string; tagType: '' | 'success' | 'warning' | 'danger' | 'info' }> = {
  wechat_mp: { color: '#07C160', tagType: 'success' },
  toutiao: { color: '#F85959', tagType: 'danger' },
  baijiahao: { color: '#2932E1', tagType: '' }
}

const accounts = ref<any[]>([])
const loading = ref(false)
const showDialog = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()

// 扫码登录相关
const showBindDialog = ref(false)
const bindPlatform = ref<'toutiao' | 'baijiahao'>('toutiao')

const form = reactive({
  platform: '',
  accountName: '',
  appId: '',
  appSecret: '',
  cookie: ''
})

const rules: FormRules = {
  platform: [{ required: true, message: () => t('account.validateSelectPlatform'), trigger: 'change' }],
  accountName: [{ required: true, message: () => t('account.validateAccountName'), trigger: 'blur' }],
  appId: [{ required: true, message: () => t('account.validateAppId'), trigger: 'blur' }],
  appSecret: [{ required: true, message: () => t('account.validateAppSecret'), trigger: 'blur' }]
}

// 按平台分组
const groupedAccounts = computed(() => {
  const grouped: Record<string, any[]> = {}
  for (const account of accounts.value) {
    if (!grouped[account.platform]) {
      grouped[account.platform] = []
    }
    grouped[account.platform].push(account)
  }
  return grouped
})

// 加载数据
const loadAccounts = async () => {
  loading.value = true
  try {
    const res = await getPlatformAccounts()
    accounts.value = res.data || []
  } finally {
    loading.value = false
  }
}

// 处理新增命令
const handleAddCommand = (command: string) => {
  if (command === 'toutiao') {
    // 头条号扫码登录
    bindPlatform.value = 'toutiao'
    showBindDialog.value = true
  } else if (command === 'baijiahao') {
    // 百家号扫码登录
    bindPlatform.value = 'baijiahao'
    showBindDialog.value = true
  } else if (command === 'toutiao_manual') {
    // 头条号手动绑定
    isEditing.value = false
    editingId.value = null
    form.platform = 'toutiao'
    form.accountName = ''
    showDialog.value = true
  } else if (command === 'baijiahao_manual') {
    // 百家号手动绑定
    isEditing.value = false
    editingId.value = null
    form.platform = 'baijiahao'
    form.accountName = ''
    showDialog.value = true
  } else {
    // 微信公众号
    isEditing.value = false
    editingId.value = null
    form.platform = command
    form.accountName = ''
    showDialog.value = true
  }
}

// 打开编辑弹窗
const openEditDialog = (account: any) => {
  isEditing.value = true
  editingId.value = account.id
  form.platform = account.platform
  form.accountName = account.accountName
  form.appId = account.appId || ''
  form.appSecret = account.appSecret || ''
  form.cookie = account.cookie || ''
  showDialog.value = true
}

// 提交表单
const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data: any = {
      platform: form.platform,
      accountName: form.accountName
    }

    if (form.platform === 'wechat_mp') {
      data.appId = form.appId
      data.appSecret = form.appSecret
    } else {
      data.cookie = form.cookie
    }

    if (isEditing.value && editingId.value) {
      data.id = editingId.value
      await updatePlatformAccount(data)
      ElMessage.success(t('common.updateSuccess'))
    } else {
      await addPlatformAccount(data)
      ElMessage.success(t('account.addSuccess'))
    }

    showDialog.value = false
    loadAccounts()
  } catch {
    ElMessage.error(isEditing.value ? t('common.updateFailed') : t('account.addFailed'))
  } finally {
    submitting.value = false
  }
}

// 删除账号
const handleDelete = async (id: number) => {
  try {
    await deletePlatformAccount(id)
    ElMessage.success(t('common.deleteSuccess'))
    loadAccounts()
  } catch {
    ElMessage.error(t('common.deleteFailed'))
  }
}

// 测试连接
const handleTestConnection = async (account: any) => {
  try {
    const res = await testAccountConnection(account.id)
    if (res.data.success) {
      ElMessage.success(t('account.connectionSuccess'))
    } else {
      ElMessage.warning(res.data.message || t('account.connectionFailed'))
    }
  } catch {
    ElMessage.error(t('account.connectionError'))
  }
}

// 扫码登录成功回调
const onBindSuccess = () => {
  loadAccounts()
}

// 重置表单
const resetForm = () => {
  form.platform = ''
  form.accountName = ''
  form.appId = ''
  form.appSecret = ''
  form.cookie = ''
  editingId.value = null
  isEditing.value = false
  formRef.value?.clearValidate()
}

// 工具函数
const getPlatformLabel = (platform: string) => {
  return platformOptions.value.find(p => p.value === platform)?.label || platform
}

const getPlatformTagType = (platform: string) => {
  return platformConfig[platform]?.tagType || 'info'
}

const getPlatformColor = (platform: string) => {
  return platformConfig[platform]?.color || '#909399'
}

const maskSecret = (str: string) => {
  if (!str) return ''
  return str.substring(0, 6) + '****' + str.substring(str.length - 4)
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

.header-actions {
  display: flex;
  gap: 8px;
}

.platform-section {
  margin-bottom: 24px;

  &:last-child {
    margin-bottom: 0;
  }
}

.platform-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;

  .account-count {
    color: #909399;
    font-size: 14px;
  }
}

.account-cards {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.account-card {
  width: 380px;
  flex-shrink: 0;

  &.account-disabled {
    opacity: 0.6;
  }

  :deep(.el-card__body) {
    padding: 16px;
  }
}

.account-card-content {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.account-info {
  flex: 1;
  min-width: 0;

  .account-name {
    font-size: 16px;
    font-weight: 500;
    margin-bottom: 4px;
  }

  .account-detail {
    color: #909399;
    font-size: 13px;
    margin-bottom: 8px;
  }

  .account-status {
    display: flex;
    align-items: center;
    gap: 8px;

    .expire-time {
      color: #909399;
      font-size: 12px;
    }
  }
}

.account-actions {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
</style>
