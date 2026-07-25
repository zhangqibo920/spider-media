<template>
  <div class="profile-page">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <template #header><span>个人信息</span></template>
          <div class="profile-info">
            <el-avatar :size="80" :src="userInfo?.avatar">
              {{ userInfo?.nickName?.charAt(0) || 'U' }}
            </el-avatar>
            <h3>{{ userInfo?.nickName || userInfo?.userName }}</h3>
            <DictTag dict-type="sys_user_role" :value="userInfo?.role || 'USER'" size="small" />
          </div>
          <el-descriptions :column="1" border style="margin-top: 20px">
            <el-descriptions-item label="用户名">{{ userInfo?.userName }}</el-descriptions-item>
            <el-descriptions-item label="昵称">{{ userInfo?.nickName }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ userInfo?.email || '-' }}</el-descriptions-item>
            <el-descriptions-item label="手机">{{ userInfo?.phonenumber || '-' }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <DictTag dict-type="sys_user_status" :value="userInfo?.status || '0'" size="small" />
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card>
          <template #header><span>编辑信息</span></template>
          <el-form ref="formRef" :model="form" label-width="80px" style="max-width: 500px">
            <el-form-item label="昵称">
              <el-input v-model="form.nickName" placeholder="请输入昵称" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="手机">
              <el-input v-model="form.phonenumber" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSave" :loading="saving">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card style="margin-top: 20px">
          <template #header><span>修改密码</span></template>
          <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px" style="max-width: 500px">
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleChangePassword" :loading="changingPassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { updateUser } from '@/api/admin'
import { resetPassword } from '@/api/admin'
import DictTag from '@/components/DictTag.vue'

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
  newPassword: '',
  confirmPassword: '',
})

const validateConfirmPassword = (_rule: any, value: string, callback: Function) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
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
    await updateUser({
      userId: userInfo.value.userId,
      nickName: form.nickName,
      email: form.email,
      phonenumber: form.phonenumber,
    })
    await userStore.fetchUserInfo()
    ElMessage.success('保存成功')
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleChangePassword = async () => {
  const valid = await passwordFormRef.value?.validate().catch(() => false)
  if (!valid || !userInfo.value) return

  changingPassword.value = true
  try {
    await resetPassword(userInfo.value.userId, passwordForm.newPassword)
    ElMessage.success('密码修改成功')
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    passwordFormRef.value?.resetFields()
  } catch {
    ElMessage.error('密码修改失败')
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
