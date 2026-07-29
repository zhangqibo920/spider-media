<template>
  <div class="profile-page">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <template #header><span>{{ t('profile.title') }}</span></template>
          <div class="profile-info">
            <el-avatar :size="80" :src="userInfo?.avatar">
              {{ userInfo?.nickName?.charAt(0) || 'U' }}
            </el-avatar>
            <h3>{{ userInfo?.nickName || userInfo?.userName }}</h3>
            <DictTag dict-type="sys_user_role" :value="userStore.getRole()" size="small" />
          </div>
          <el-descriptions :column="1" border style="margin-top: 20px">
            <el-descriptions-item :label="t('sysAdmin.username')">{{ userInfo?.userName }}</el-descriptions-item>
            <el-descriptions-item :label="t('profile.nickname')">{{ userInfo?.nickName }}</el-descriptions-item>
            <el-descriptions-item :label="t('profile.email')">{{ userInfo?.email || '-' }}</el-descriptions-item>
            <el-descriptions-item :label="t('profile.phone')">{{ userInfo?.phonenumber || '-' }}</el-descriptions-item>
            <el-descriptions-item :label="t('common.status')">
              <DictTag dict-type="sys_user_status" :value="userInfo?.status || '0'" size="small" />
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card>
          <template #header><span>{{ t('profile.editInfo') }}</span></template>
          <el-form ref="formRef" :model="form" label-width="80px" style="max-width: 500px">
            <el-form-item :label="t('profile.nickname')">
              <el-input v-model="form.nickName" :placeholder="t('profile.nicknamePlaceholder')" />
            </el-form-item>
            <el-form-item :label="t('profile.email')">
              <el-input v-model="form.email" :placeholder="t('profile.emailPlaceholder')" />
            </el-form-item>
            <el-form-item :label="t('profile.phone')">
              <el-input v-model="form.phonenumber" :placeholder="t('profile.phonePlaceholder')" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSave" :loading="saving">{{ t('profile.saveChanges') }}</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card style="margin-top: 20px">
          <template #header><span>{{ t('profile.changePassword') }}</span></template>
          <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px" style="max-width: 500px">
            <el-form-item :label="t('profile.oldPassword')" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" :placeholder="t('profile.oldPasswordPlaceholder')" show-password />
            </el-form-item>
            <el-form-item :label="t('profile.newPassword')" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" :placeholder="t('profile.newPasswordPlaceholder')" show-password />
            </el-form-item>
            <el-form-item :label="t('profile.confirmPassword')" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" :placeholder="t('profile.confirmPasswordPlaceholder')" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleChangePassword" :loading="changingPassword">{{ t('profile.changePassword') }}</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { updateProfile, changePassword } from '@/api/profile'

const { t } = useI18n()
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)

const formRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()
const saving = ref(false)
const changingPassword = ref(false)

const form = reactive({
  nickName: '',
  email: '',
  phonenumber: '',
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const validateConfirmPassword = (_rule: any, value: string, callback: Function) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error(t('profile.passwordMismatch')))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: t('profile.oldPasswordRequired'), trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: t('profile.newPasswordRequired'), trigger: 'blur' },
    { min: 6, message: t('profile.passwordMinLength'), trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: t('profile.confirmPasswordRequired'), trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

onMounted(() => {
  if (userInfo.value) {
    form.nickName = userInfo.value.nickName || ''
    form.email = userInfo.value.email || ''
    form.phonenumber = userInfo.value.phonenumber || ''
  }
})

const handleSave = async () => {
  if (!userInfo.value) return
  saving.value = true
  try {
    await updateProfile({
      nickName: form.nickName,
      email: form.email,
      phonenumber: form.phonenumber,
    })
    await userStore.fetchUserInfo()
    ElMessage.success(t('profile.saveSuccess'))
  } catch {
    ElMessage.error(t('common.saveFailed'))
  } finally {
    saving.value = false
  }
}

const handleChangePassword = async () => {
  const valid = await passwordFormRef.value?.validate().catch(() => false)
  if (!valid || !userInfo.value) return

  changingPassword.value = true
  try {
    await changePassword(passwordForm.oldPassword, passwordForm.newPassword)
    ElMessage.success(t('profile.changePwdSuccess'))
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    passwordFormRef.value?.resetFields()
  } catch {
    ElMessage.error(t('profile.changePwdFailed'))
  } finally {
    changingPassword.value = false
  }
}
</script>

<style scoped lang="scss">
.profile-page {
  .profile-info {
    text-align: center;
    padding: 20px 0;

    h3 {
      margin: 12px 0 8px;
      color: #303133;
    }
  }
}
</style>
