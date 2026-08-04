<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="480px"
    @closed="handleClose"
    :close-on-click-modal="false"
  >
    <!-- 扫码登录步骤 -->
    <div v-if="currentStep === 'scan'" class="qr-code-content">
      <div class="qr-code-wrapper">
        <div class="qr-code-box">
          <img v-if="qrCodeUrl" :src="qrCodeUrl" class="qr-code-image" />
          <div v-else class="qr-code-loading">
            <el-icon class="is-loading" :size="32"><Loading /></el-icon>
            <span>{{ t('common.loading') }}</span>
          </div>
        </div>
      </div>

      <div class="scan-instructions">
        <p class="instruction-title">{{ t('account.scanInstructions', { platform: platformLabel }) }}</p>
        <ol class="instruction-steps">
          <li v-if="platform === 'toutiao'">
            {{ t('account.scanStep1', { platform: '今日头条' }) }}
          </li>
          <li v-else-if="platform === 'baijiahao'">
            {{ t('account.scanStep1', { platform: '百度' }) }}
          </li>
          <li>{{ t('account.scanStep3') }}</li>
          <li>{{ t('account.scanStep4') }}</li>
        </ol>
      </div>

      <div class="scan-status" :class="statusClass">
        <el-icon v-if="status === 'WAITING'" class="is-loading"><Loading /></el-icon>
        <el-icon v-else-if="status === 'SCANNED'"><Iphone /></el-icon>
        <span>{{ statusText }}</span>
      </div>

      <div class="dialog-footer">
        <el-button @click="handleClose">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="refreshQrCode" :disabled="status === 'WAITING'">
          {{ t('account.refreshQrCode') }}
        </el-button>
      </div>
    </div>

    <!-- Cookie绑定步骤（扫码失败后的备选方案） -->
    <div v-else-if="currentStep === 'bind'" class="bind-content">
      <el-alert
        :title="bindAlertTitle"
        type="info"
        show-icon
        :closable="false"
        style="margin-bottom: 16px"
      />

      <el-form :model="bindForm" label-width="100px">
        <el-form-item :label="$t('account.accountName')">
          <el-input v-model="bindForm.accountName" :placeholder="$t('account.accountNamePlaceholder')" />
        </el-form-item>
        <el-form-item label="Cookie">
          <el-input
            v-model="bindForm.cookie"
            type="textarea"
            :rows="6"
            :placeholder="$t('account.cookiePlaceholder')"
          />
        </el-form-item>
      </el-form>

      <div class="cookie-steps">
        <p class="steps-title">{{ t('account.getCookieSteps') }}：</p>
        <ol v-if="platform === 'toutiao'" class="instruction-steps">
          <li>{{ t('account.cookieStep1', { site: 'mp.toutiao.com' }) }}</li>
          <li>{{ t('account.cookieStep2', { platform: t('account.platformToutiao') }) }}</li>
          <li>{{ t('account.cookieStep3') }}</li>
          <li>{{ t('account.cookieStep4') }}</li>
          <li>{{ t('account.cookieStep5') }}</li>
          <li>{{ t('account.cookieStep6') }}</li>
        </ol>
        <ol v-else class="instruction-steps">
          <li>{{ t('account.cookieStep1', { site: 'baijiahao.baidu.com' }) }}</li>
          <li>{{ t('account.cookieStep2', { platform: t('account.platformBaijiahao') }) }}</li>
          <li>{{ t('account.cookieStep3') }}</li>
          <li>{{ t('account.cookieStep4') }}</li>
          <li>{{ t('account.cookieStep5') }}</li>
          <li>{{ t('account.cookieStep6') }}</li>
        </ol>
      </div>

      <div class="dialog-footer">
        <el-button @click="currentStep = 'scan'">{{ t('account.backToScan') }}</el-button>
        <el-button @click="handleClose">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleBindCookie" :loading="binding">
          {{ t('account.bindAccount') }}
        </el-button>
      </div>
    </div>

    <!-- 绑定成功 -->
    <div v-else-if="currentStep === 'success'" class="success-content">
      <el-result
        icon="success"
        :title="$t('account.bindSuccess')"
        :sub-title="$t('account.bindSuccessDetail', { platform: platformLabel })"
      >
        <template #extra>
          <el-button type="primary" @click="handleClose">{{ $t('common.confirm') }}</el-button>
        </template>
      </el-result>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { getToutiaoQrCode, getBaijiahaoQrCode, bindToutiaoCookie, bindBaijiahaoCookie, pollToutiaoStatus, pollBaijiahaoStatus } from '@/api/publish'

const { t } = useI18n()

const props = defineProps<{
  modelValue: boolean
  platform: 'toutiao' | 'baijiahao'
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const currentStep = ref<'scan' | 'bind' | 'success'>('scan')
const qrCodeUrl = ref('')
const qrCodeToken = ref('')
const status = ref<'WAITING' | 'SCANNED' | 'CONFIRMED' | 'EXPIRED' | 'FAILED'>('WAITING')
const binding = ref(false)
let pollTimer: ReturnType<typeof setInterval> | null = null

const bindForm = ref({
  accountName: '',
  cookie: ''
})

const platformLabel = computed(() => {
  return props.platform === 'toutiao' ? t('account.platformToutiao') : t('account.platformBaijiahao')
})

const dialogTitle = computed(() => {
  return t('account.bindAccount', { platform: platformLabel.value })
})

const bindAlertTitle = computed(() => {
  return t('account.bindAlert', { platform: platformLabel.value })
})

const statusText = computed(() => {
  const statusMap: Record<string, string> = {
    WAITING: t('account.waitingScan'),
    SCANNED: t('account.scanned'),
    CONFIRMED: t('account.scanSuccess'),
    EXPIRED: t('account.qrCodeExpired'),
    FAILED: t('account.scanFailed')
  }
  return statusMap[status.value] || ''
})

const statusClass = computed(() => {
  return `status-${status.value.toLowerCase()}`
})

// 获取二维码
const fetchQrCode = async () => {
  try {
    const res = props.platform === 'toutiao'
      ? await getToutiaoQrCode()
      : await getBaijiahaoQrCode()

    qrCodeUrl.value = res.data.qrCodeUrl
    qrCodeToken.value = res.data.token
    status.value = 'WAITING'

    // 开始轮询状态
    startPolling()
  } catch {
    ElMessage.error(t('account.getQrCodeFailed'))
  }
}

// 开始轮询扫码状态
const startPolling = () => {
  stopPolling()
  pollTimer = setInterval(async () => {
    try {
      const res = props.platform === 'toutiao'
        ? await pollToutiaoStatus(qrCodeToken.value)
        : await pollBaijiahaoStatus(qrCodeToken.value)

      const newStatus = res.data?.status
      if (newStatus) {
        status.value = newStatus
        if (newStatus === 'CONFIRMED') {
          stopPolling()
          currentStep.value = 'success'
          emit('success')
        } else if (newStatus === 'EXPIRED' || newStatus === 'FAILED') {
          stopPolling()
        }
      }
    } catch {
      // 轮询出错时继续尝试
    }
  }, 3000)
}

// 停止轮询
const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

// 刷新二维码
const refreshQrCode = () => {
  fetchQrCode()
}

// 切换到绑定步骤
const switchToBindStep = () => {
  stopPolling()
  currentStep.value = 'bind'
}

// 绑定Cookie
const handleBindCookie = async () => {
  if (!bindForm.value.cookie.trim()) {
    ElMessage.warning('请输入Cookie')
    return
  }

  binding.value = true
  try {
    const res = props.platform === 'toutiao'
      ? await bindToutiaoCookie({
          cookie: bindForm.value.cookie,
          accountName: bindForm.value.accountName
        })
      : await bindBaijiahaoCookie({
          cookie: bindForm.value.cookie,
          accountName: bindForm.value.accountName
        })

    if (res.data.success) {
      currentStep.value = 'success'
      emit('success')
    } else {
      ElMessage.error(res.data.message || '绑定失败')
    }
  } catch {
    ElMessage.error('绑定失败，请重试')
  } finally {
    binding.value = false
  }
}

// 关闭弹窗
const handleClose = () => {
  stopPolling()
  currentStep.value = 'scan'
  qrCodeUrl.value = ''
  qrCodeToken.value = ''
  status.value = 'WAITING'
  bindForm.value = { accountName: '', cookie: '' }
  visible.value = false
}

// 监听visible变化
watch(visible, (val) => {
  if (val) {
    currentStep.value = 'scan'
    fetchQrCode()
  } else {
    handleClose()
  }
})

// 暴露切换到绑定步骤的方法
defineExpose({ switchToBindStep })
</script>

<style scoped lang="scss">
.qr-code-content {
  text-align: center;
}

.qr-code-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.qr-code-box {
  width: 200px;
  height: 200px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
}

.qr-code-image {
  width: 180px;
  height: 180px;
}

.qr-code-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #909399;
}

.scan-instructions {
  margin-bottom: 16px;

  .instruction-title {
    font-size: 15px;
    font-weight: 500;
    margin-bottom: 12px;
  }

  .instruction-steps {
    text-align: left;
    display: inline-block;
    color: #606266;
    line-height: 1.8;
    font-size: 14px;

    li {
      margin-bottom: 4px;
    }
  }
}

.scan-status {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 16px;
  font-size: 14px;

  &.status-waiting {
    background: #e6f7ff;
    color: #1890ff;
  }

  &.status-scanned {
    background: #fff7e6;
    color: #fa8c16;
  }

  &.status-confirmed {
    background: #f6ffed;
    color: #52c41a;
  }

  &.status-expired,
  &.status-failed {
    background: #fff2f0;
    color: #ff4d4f;
  }
}

.bind-content {
  .cookie-steps {
    margin-top: 16px;
    padding: 12px;
    background: #f5f7fa;
    border-radius: 8px;

    .steps-title {
      font-weight: 500;
      margin-bottom: 8px;
    }

    .instruction-steps {
      text-align: left;
      display: inline-block;
      color: #606266;
      line-height: 1.8;
      font-size: 13px;

      li {
        margin-bottom: 4px;
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}
</style>
