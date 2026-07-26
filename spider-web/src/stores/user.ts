import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, register, getCurrentUser } from '@/api/auth'
import type { User } from '@/types'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<User | null>(null)

  /**
   * 用户登录（携带验证码）
   *
   * @param username    用户名
   * @param password    密码
   * @param captchaId   验证码唯一标识
   * @param captchaCode 用户输入的验证码
   */
  async function handleLogin(
    username: string,
    password: string,
    captchaId: string,
    captchaCode: string,
  ) {
    const res = await login(username, password, captchaId, captchaCode)
    token.value = res.data.token
    localStorage.setItem('token', res.data.token)
    await fetchUserInfo()
  }

  async function handleRegister(username: string, password: string) {
    await register(username, password)
  }

  async function fetchUserInfo() {
    try {
      const res = await getCurrentUser()
      userInfo.value = res.data
    } catch {
      logout()
    }
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    handleLogin,
    handleRegister,
    fetchUserInfo,
    logout,
  }
})
