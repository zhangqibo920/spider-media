<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="520px"
    @closed="handleClose"
    :close-on-click-modal="false"
  >
    <!-- 步骤一：打开登录页面 -->
    <div v-if="currentStep === 1" class="step-content">
      <el-steps :active="currentStep" finish-status="success" align-center style="margin-bottom: 24px">
        <el-step :title="$t('account.stepOpenLogin')" :description="$t('account.stepOpenLoginDesc')" />
        <el-step :title="$t('account.stepScanLogin')" :description="$t('account.stepScanLoginDesc')" />
        <el-step :title="$t('account.stepCopyCookie')" :description="$t('account.stepCopyCookieDesc')" />
        <el-step :title="$t('account.stepBindAccount')" :description="$t('account.stepBindAccountDesc')" />
      </el-steps>

      <div class="login-page-preview">
        <div class="preview-header">
          <el-icon :size="24" :style="{ color: platformColor }"><Platform /></el-icon>
          <span>{{ platformLabel }}{{ $t('account.creationPlatform') }}</span>
        </div>
        <div class="preview-url">{{ platformLoginUrl }}</div>
      </div>

      <div class="action-buttons">
        <el-button type="primary" size="large" @click="openLoginPage">
          <el-icon><Link /></el-icon>
          {{ $t('account.openPlatformLogin', { platform: platformLabel }) }}
        </el-button>
      </div>

      <el-alert
        :title="$t('account.completeScanInNewWindow')"
        type="info"
        show-icon
        :closable="false"
        style="margin-top: 16px"
      />
    </div>

    <!-- 步骤二：等待扫码（用户自行操作） -->
    <div v-else-if="currentStep === 2" class="step-content">
      <el-steps :active="currentStep" finish-status="success" align-center style="margin-bottom: 24px">
        <el-step :title="$t('account.stepOpenLogin')" />
        <el-step :title="$t('account.stepScanLogin')" :description="$t('account.stepScanLoginDesc')" />
        <el-step :title="$t('account.stepCopyCookie')" :description="$t('account.stepCopyCookieDesc')" />
        <el-step :title="$t('account.stepBindAccount')" :description="$t('account.stepBindAccountDesc')" />
      </el-steps>

      <div class="waiting-tip">
        <el-icon class="is-loading" :size="48" color="#409eff"><Loading /></el-icon>
        <p>{{ $t('account.completeScanInNewWindow') }}</p>
        <p class="sub-tip">{{ $t('account.clickAfterLogin') }}</p>
      </div>

      <div class="action-buttons">
        <el-button @click="currentStep = 1">{{ $t('common.prev') }}</el-button>
        <el-button type="primary" @click="currentStep = 3">
          {{ $t('account.loginCompleteNext') }}
        </el-button>
      </div>
    </div>

    <!-- 步骤三：获取Cookie -->
    <div v-else-if="currentStep === 3" class="step-content">
      <el-steps :active="currentStep" finish-status="success" align-center style="margin-bottom: 24px">
        <el-step :title="$t('account.stepOpenLogin')" />
        <el-step :title="$t('account.stepScanLogin')" />
        <el-step :title="$t('account.stepCopyCookie')" :description="$t('account.stepCopyCookieDesc')" />
        <el-step :title="$t('account.stepBindAccount')" :description="$t('account.stepBindAccountDesc')" />
      </el-steps>

      <div class="cookie-steps">
        <el-alert
          :title="$t('account.followStepsToGetCookie')"
          type="warning"
          show-icon
          :closable="false"
          style="margin-bottom: 16px"
        />

        <div class="steps-list">
          <div class="step-item" v-for="(step, index) in cookieSteps" :key="index">
            <div class="step-number">{{ index + 1 }}</div>
            <div class="step-text" v-html="step"></div>
          </div>
        </div>

        <div class="copy-buttons">
          <el-button type="success" @click="copyBookmarklet">
            <el-icon><DocumentCopy /></el-icon>
            {{ $t('account.copyOneClickScript') }}
          </el-button>
          <el-tooltip :content="$t('account.bookmarkletTooltip')" placement="top">
            <el-icon><QuestionFilled /></el-icon>
          </el-tooltip>
        </div>
      </div>

      <div class="action-buttons">
        <el-button @click="currentStep = 2">{{ $t('common.prev') }}</el-button>
        <el-button type="primary" @click="currentStep = 4">
          {{ $t('account.cookieObtainedNext') }}
        </el-button>
      </div>
    </div>

    <!-- 步骤四：绑定账号 -->
    <div v-else-if="currentStep === 4" class="step-content">
      <el-steps :active="currentStep" finish-status="success" align-center style="margin-bottom: 24px">
        <el-step :title="$t('account.stepOpenLogin')" />
        <el-step :title="$t('account.stepScanLogin')" />
        <el-step :title="$t('account.stepCopyCookie')" />
        <el-step :title="$t('account.stepBindAccount')" :description="$t('account.stepBindAccountDesc')" />
      </el-steps>

      <el-form :model="bindForm" label-width="100px">
        <el-form-item :label="$t('account.accountName')">
          <el-input v-model="bindForm.accountName" :placeholder="$t('account.accountNamePlaceholder')" />
        </el-form-item>
        <el-form-item label="Cookie" prop="cookie">
          <el-input
            v-model="bindForm.cookie"
            type="textarea"
            :rows="6"
            :placeholder="$t('account.cookiePlaceholder')"
          />
        </el-form-item>
      </el-form>

      <div class="action-buttons">
        <el-button @click="currentStep = 3">{{ $t('common.prev') }}</el-button>
        <el-button type="primary" @click="handleBindCookie" :loading="binding">
          {{ $t('account.bindAccount') }}
        </el-button>
      </div>
    </div>

    <!-- 绑定成功 -->
    <div v-else-if="currentStep === 5" class="step-content">
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
import { bindToutiaoCookie, bindBaijiahaoCookie } from '@/api/publish'

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

const currentStep = ref(1)
const binding = ref(false)

const bindForm = ref({
  accountName: '',
  cookie: ''
})

const platformLabel = computed(() => {
  return props.platform === 'toutiao' ? t('account.platformToutiao') : t('account.platformBaijiahao')
})

const platformColor = computed(() => {
  return props.platform === 'toutiao' ? '#F85959' : '#2932E1'
})

const platformLoginUrl = computed(() => {
  return props.platform === 'toutiao'
    ? 'https://mp.toutiao.com'
    : 'https://baijiahao.baidu.com'
})

const dialogTitle = computed(() => {
  return t('account.bindAccountTitle', { platform: platformLabel.value })
})

const cookieSteps = computed(() => {
  if (props.platform === 'toutiao') {
    return [
      t('account.cookieStepsToutiao[0]'),
      t('account.cookieStepsToutiao[1]'),
      t('account.cookieStepsToutiao[2]'),
      t('account.cookieStepsToutiao[3]'),
      t('account.cookieStepsToutiao[4]')
    ]
  } else {
    return [
      t('account.cookieStepsBaijiahao[0]'),
      t('account.cookieStepsBaijiahao[1]'),
      t('account.cookieStepsBaijiahao[2]'),
      t('account.cookieStepsBaijiahao[3]'),
      t('account.cookieStepsBaijiahao[4]')
    ]
  }
})

// 一键获取Cookie的书签脚本
const bookmarkletCode = computed(() => {
  return `javascript:void(function(){var cookie=document.cookie;var ta=document.createElement('textarea');ta.value=cookie;document.body.appendChild(ta);ta.select();document.execCommand('copy');document.body.removeChild(ta);alert('Cookie已复制到剪贴板！\\n长度: '+cookie.length+'字符');}())`
})

// 打开登录页面
const openLoginPage = () => {
  window.open(platformLoginUrl.value, '_blank')
  currentStep.value = 2
}

// 复制书签脚本
const copyBookmarklet = () => {
  navigator.clipboard.writeText(bookmarkletCode.value).then(() => {
    ElMessage.success(t('account.scriptCopied'))
  }).catch(() => {
    ElMessage.error(t('account.copyFailed'))
  })
}

// 绑定Cookie
const handleBindCookie = async () => {
  if (!bindForm.value.cookie.trim()) {
    ElMessage.warning(t('account.validateCookie'))
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
      currentStep.value = 5
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
  currentStep.value = 1
  bindForm.value = { accountName: '', cookie: '' }
  visible.value = false
}

// 监听visible变化
watch(visible, (val) => {
  if (val) {
    currentStep.value = 1
  } else {
    handleClose()
  }
})
</script>

<style scoped lang="scss">
.step-content {
  min-height: 300px;
}

.login-page-preview {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;

  .preview-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 500;
    margin-bottom: 8px;
  }

  .preview-url {
    color: #909399;
    font-size: 13px;
    font-family: monospace;
  }
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 20px;
}

.waiting-tip {
  text-align: center;
  padding: 30px 0;

  p {
    margin-top: 16px;
    font-size: 16px;
    color: #303133;

    &.sub-tip {
      font-size: 14px;
      color: #909399;
    }
  }
}

.cookie-steps {
  .steps-list {
    background: #f5f7fa;
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 16px;

    .step-item {
      display: flex;
      align-items: flex-start;
      gap: 12px;
      margin-bottom: 12px;

      &:last-child {
        margin-bottom: 0;
      }

      .step-number {
        width: 24px;
        height: 24px;
        border-radius: 50%;
        background: #409eff;
        color: #fff;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 12px;
        font-weight: 500;
        flex-shrink: 0;
      }

      .step-text {
        line-height: 24px;
        font-size: 14px;
        color: #303133;
      }
    }
  }

  .copy-buttons {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}
</style>
