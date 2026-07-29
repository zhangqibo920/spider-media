<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>SpiderMedia</h2>
          <p>Spider Media</p>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            :placeholder="$t('login.username')"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            :placeholder="$t('login.password')"
            :prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item prop="captchaCode">
          <div class="captcha-row">
            <el-input
              v-model="form.captchaCode"
              :placeholder="$t('login.captcha')"
              :prefix-icon="Key"
              size="large"
              maxlength="4"
              @keyup.enter="handleLogin"
            />
            <div class="captcha-img" @click="refreshCaptcha" title="点击刷新验证码">
              <img v-if="captchaImg" :src="captchaImg" alt="验证码" />
              <div v-else class="captcha-loading">{{ $t('login.loading') }}</div>
            </div>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="handleLogin" style="width: 100%">
            {{ $t('login.login') }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        {{ $t('login.noAccount') }}<router-link to="/register">{{ $t('login.registerNow') }}</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Lock, Key } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getCaptcha } from '@/api/auth'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const { t } = useI18n()

const formRef = ref<FormInstance>()
const loading = ref(false)
const captchaImg = ref('')
const captchaId = ref('')

const form = reactive({
  username: '',
  password: '',
  captchaCode: '',
})

const rules = {
  username: [{ required: true, message: t('login.username'), trigger: 'blur' }],
  password: [{ required: true, message: t('login.password'), trigger: 'blur' }],
  captchaCode: [
    { required: true, message: t('login.captcha'), trigger: 'blur' },
    { min: 4, max: 4, message: '验证码为 4 位字符', trigger: 'blur' },
  ],
}

async function refreshCaptcha() {
  try {
    const res = await getCaptcha()
    captchaId.value = res.data.captchaId
    captchaImg.value = res.data.img
    form.captchaCode = ''
  } catch {
    ElMessage.error(t('login.captchaFailed'))
  }
}

const handleLogin = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.handleLogin(form.username, form.password, captchaId.value, form.captchaCode)
    ElMessage.success(t('login.loginSuccess'))
    const redirect = (route.query.redirect as string) || '/dashboard'
    router.push(redirect)
  } catch {
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  refreshCaptcha()
})
</script>

<style scoped lang="scss">
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #e0f2fe 0%, #f0f9ff 100%);

  .login-card {
    width: 400px;
    border: none;
    border-radius: 12px;
    box-shadow: 0 4px 24px rgba(30, 64, 175, 0.1);

    .card-header {
      text-align: center;

      h2 {
        margin: 0;
        font-size: 26px;
        font-weight: 700;
        color: #1e40af;
      }

      p {
        margin: 8px 0 0;
        color: #94a3b8;
        font-size: 14px;
      }
    }
  }

  .captcha-row {
    display: flex;
    width: 100%;
    gap: 8px;
    align-items: center;

    .captcha-img {
      flex-shrink: 0;
      width: 120px;
      height: 40px;
      cursor: pointer;
      border-radius: 4px;
      overflow: hidden;
      background: #f5f5f5;
      display: flex;
      align-items: center;
      justify-content: center;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .captcha-loading {
        color: #94a3b8;
        font-size: 12px;
      }

      &:hover {
        opacity: 0.85;
      }
    }
  }

  .login-footer {
    text-align: center;
    font-size: 14px;
    color: #94a3b8;

    a {
      color: #1e40af;
      text-decoration: none;

      &:hover {
        text-decoration: underline;
      }
    }
  }
}
</style>
