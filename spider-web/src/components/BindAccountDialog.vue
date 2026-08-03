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
        <el-step title="打开登录页" description="跳转到平台登录页面" />
        <el-step title="扫码登录" description="使用APP扫描二维码" />
        <el-step title="复制Cookie" description="获取登录凭证" />
        <el-step title="绑定账号" description="完成账号绑定" />
      </el-steps>

      <div class="login-page-preview">
        <div class="preview-header">
          <el-icon :size="24" :style="{ color: platformColor }"><Platform /></el-icon>
          <span>{{ platformLabel }}创作平台</span>
        </div>
        <div class="preview-url">{{ platformLoginUrl }}</div>
      </div>

      <div class="action-buttons">
        <el-button type="primary" size="large" @click="openLoginPage">
          <el-icon><Link /></el-icon>
          打开{{ platformLabel }}登录页面
        </el-button>
      </div>

      <el-alert
        title="请在新窗口中完成扫码登录"
        type="info"
        show-icon
        :closable="false"
        style="margin-top: 16px"
      />
    </div>

    <!-- 步骤二：等待扫码（用户自行操作） -->
    <div v-else-if="currentStep === 2" class="step-content">
      <el-steps :active="currentStep" finish-status="success" align-center style="margin-bottom: 24px">
        <el-step title="打开登录页" />
        <el-step title="扫码登录" description="使用APP扫描二维码" />
        <el-step title="复制Cookie" description="获取登录凭证" />
        <el-step title="绑定账号" description="完成账号绑定" />
      </el-steps>

      <div class="waiting-tip">
        <el-icon class="is-loading" :size="48" color="#409eff"><Loading /></el-icon>
        <p>请在新打开的窗口中完成扫码登录</p>
        <p class="sub-tip">登录成功后，点击下方按钮继续</p>
      </div>

      <div class="action-buttons">
        <el-button @click="currentStep = 1">返回上一步</el-button>
        <el-button type="primary" @click="currentStep = 3">
          登录完成，下一步
        </el-button>
      </div>
    </div>

    <!-- 步骤三：获取Cookie -->
    <div v-else-if="currentStep === 3" class="step-content">
      <el-steps :active="currentStep" finish-status="success" align-center style="margin-bottom: 24px">
        <el-step title="打开登录页" />
        <el-step title="扫码登录" />
        <el-step title="复制Cookie" description="获取登录凭证" />
        <el-step title="绑定账号" description="完成账号绑定" />
      </el-steps>

      <div class="cookie-steps">
        <el-alert
          title="请按照以下步骤获取Cookie"
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
            复制一键获取脚本
          </el-button>
          <el-tooltip content="将此脚本添加到浏览器书签栏，登录后点击即可自动复制Cookie" placement="top">
            <el-icon><QuestionFilled /></el-icon>
          </el-tooltip>
        </div>
      </div>

      <div class="action-buttons">
        <el-button @click="currentStep = 2">返回上一步</el-button>
        <el-button type="primary" @click="currentStep = 4">
          已获取Cookie，下一步
        </el-button>
      </div>
    </div>

    <!-- 步骤四：绑定账号 -->
    <div v-else-if="currentStep === 4" class="step-content">
      <el-steps :active="currentStep" finish-status="success" align-center style="margin-bottom: 24px">
        <el-step title="打开登录页" />
        <el-step title="扫码登录" />
        <el-step title="复制Cookie" />
        <el-step title="绑定账号" description="完成账号绑定" />
      </el-steps>

      <el-form :model="bindForm" label-width="100px">
        <el-form-item label="账号名称">
          <el-input v-model="bindForm.accountName" :placeholder="`请输入${platformLabel}账号名称`" />
        </el-form-item>
        <el-form-item label="Cookie" prop="cookie">
          <el-input
            v-model="bindForm.cookie"
            type="textarea"
            :rows="6"
            placeholder="请粘贴从浏览器获取的Cookie"
          />
        </el-form-item>
      </el-form>

      <div class="action-buttons">
        <el-button @click="currentStep = 3">返回上一步</el-button>
        <el-button type="primary" @click="handleBindCookie" :loading="binding">
          绑定账号
        </el-button>
      </div>
    </div>

    <!-- 绑定成功 -->
    <div v-else-if="currentStep === 5" class="step-content">
      <el-result
        icon="success"
        title="绑定成功"
        :sub-title="`已成功绑定${platformLabel}账号`"
      >
        <template #extra>
          <el-button type="primary" @click="handleClose">完成</el-button>
        </template>
      </el-result>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { bindToutiaoCookie, bindBaijiahaoCookie } from '@/api/publish'

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
  return props.platform === 'toutiao' ? '头条号' : '百家号'
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
  return `绑定${platformLabel.value}账号`
})

const cookieSteps = computed(() => {
  if (props.platform === 'toutiao') {
    return [
      '在已登录的头条号页面，按 <strong>F12</strong> 打开开发者工具',
      '切换到 <strong>Application</strong>（应用程序）面板',
      '在左侧找到 <strong>Cookies</strong> → 点击 <strong>https://mp.toutiao.com</strong>',
      '点击上方 <strong>cookies.com</strong> 或按 <strong>Ctrl+A</strong> 全选',
      '按 <strong>Ctrl+C</strong> 复制，然后粘贴到下方输入框'
    ]
  } else {
    return [
      '在已登录的百家号页面，按 <strong>F12</strong> 打开开发者工具',
      '切换到 <strong>Application</strong>（应用程序）面板',
      '在左侧找到 <strong>Cookies</strong> → 点击 <strong>https://baijiahao.baidu.com</strong>',
      '点击上方表格区域，按 <strong>Ctrl+A</strong> 全选',
      '按 <strong>Ctrl+C</strong> 复制，然后粘贴到下方输入框'
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
    ElMessage.success('一键获取脚本已复制，请添加到浏览器书签栏')
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制')
  })
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
