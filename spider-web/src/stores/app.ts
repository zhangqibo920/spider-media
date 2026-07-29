import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

const SIDEBAR_COLLAPSED_KEY = 'app:sidebarCollapsed'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(localStorage.getItem(SIDEBAR_COLLAPSED_KEY) === 'true')

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  watch(sidebarCollapsed, (collapsed) => {
    localStorage.setItem(SIDEBAR_COLLAPSED_KEY, String(collapsed))
  })

  return {
    sidebarCollapsed,
    toggleSidebar,
  }
})
