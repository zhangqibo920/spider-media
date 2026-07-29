<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <h2>{{ $t('register.title') }}</h2>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            :placeholder="$t('register.username')"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            :placeholder="$t('register.password')"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            :placeholder="$t('register.confirmPassword')"
            :prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleRegister"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="handleRegister" style="width: 100%">
            {{ $t('register.register') }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        {{ $t('register.hasAccount') }}<router-link to="/login">{{ $t('register.loginNow') }}</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const userStore = useUserStore()
const { t } = useI18n()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
})

const validateConfirmPassword = (_rule: any, value: string, callback: Function) => {
  if (value !== form.password) {
    callback(new Error(t('register.passwordMismatch')))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, message: t('register.username'), trigger: 'blur' }],
  password: [{ required: true, message: t('register.password'), trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: t('register.confirmPassword'), trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

const handleRegister = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.handleRegister(form.username, form.password)
    ElMessage.success(t('register.registerSuccess'))
    router.push('/login')
  } catch (error) {
    ElMessage.error(t('register.registerFailed'))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #e0f2fe 0%, #f0f9ff 100%);

  .register-card {
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
    }
  }

  .register-footer {
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
