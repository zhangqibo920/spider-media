import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

/**
 * 应用全局状态 Store
 *
 * <p>管理侧边栏折叠状态等 UI 偏好，状态会持久化到 localStorage，
 * 用户刷新页面或重新进入时保持上一次的偏好。</p>
 */
const SIDEBAR_COLLAPSED_KEY = 'app:sidebarCollapsed'

export const useAppStore = defineStore('app', () => {
  // 初始化时从 localStorage 读取，确保刷新后保持上一次的折叠状态
  const sidebarCollapsed = ref(localStorage.getItem(SIDEBAR_COLLAPSED_KEY) === 'true')
  const activeMenu = ref('')

  /**
   * 切换侧边栏折叠状态
   * <p>状态变化会通过 watch 自动同步到 localStorage。</p>
   */
  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function setActiveMenu(menu: string) {
    activeMenu.value = menu
  }

  // 监听侧边栏状态变化，持久化到 localStorage
  watch(sidebarCollapsed, (collapsed) => {
    localStorage.setItem(SIDEBAR_COLLAPSED_KEY, String(collapsed))
  })

  return {
    sidebarCollapsed,
    activeMenu,
    toggleSidebar,
    setActiveMenu,
  }
})
