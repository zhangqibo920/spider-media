import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, register, getCurrentUser } from '@/api/auth'
import { getRouters } from '@/api/menu'
import type { User, Role, Menu } from '@/types'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<User | null>(null)
  const roles = ref<Role[]>([])
  const menuTree = ref<Menu[]>([])
  const routesLoaded = ref(false)

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
      userInfo.value = res.data.user
      roles.value = res.data.roles || []
    } catch {
      logout()
    }
  }

  async function fetchMenuTree() {
    try {
      const res = await getRouters()
      menuTree.value = res.data || []
      routesLoaded.value = true
    } catch {
      menuTree.value = []
      routesLoaded.value = true
    }
  }

  /** Get user's first role key (for backward compatibility) */
  function getRole(): string {
    if (roles.value.length > 0) {
      return roles.value[0].roleKey
    }
    return userInfo.value?.role || 'USER'
  }

  function hasRole(roleKey: string): boolean {
    return roles.value.some(r => r.roleKey === roleKey)
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    roles.value = []
    menuTree.value = []
    routesLoaded.value = false
    localStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    roles,
    menuTree,
    routesLoaded,
    handleLogin,
    handleRegister,
    fetchUserInfo,
    fetchMenuTree,
    getRole,
    hasRole,
    logout,
  }
})
