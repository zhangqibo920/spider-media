<template>
  <el-container class="main-layout">
    <el-aside :width="sidebarCollapsed ? '64px' : '220px'" class="sidebar">
      <div class="logo">
        <span v-if="!sidebarCollapsed" class="logo-text">SpiderMedia</span>
        <span v-else class="logo-icon">S</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="sidebarCollapsed"
        router
        class="sidebar-menu"
      >
        <template v-for="item in menuItems" :key="item.path">
          <el-sub-menu v-if="item.children && item.children.length > 0" :index="item.path">
            <template #title>
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.title }}</span>
            </template>
            <el-menu-item v-for="child in item.children" :key="child.path" :index="child.path">
              <el-icon><component :is="child.icon" /></el-icon>
              <template #title>{{ child.title }}</template>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="item.path">
            <el-icon><component :is="item.icon" /></el-icon>
            <template #title>{{ item.title }}</template>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleSidebar">
            <Fold v-if="!sidebarCollapsed" />
            <Expand v-else />
          </el-icon>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="userInfo?.avatar">
                {{ userInfo?.nickName?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="username">{{ userInfo?.nickName || userInfo?.userName || '用户' }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'

interface MenuItem {
  path: string
  title: string
  icon: string
  children?: MenuItem[]
}

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()
const userStore = useUserStore()

const sidebarCollapsed = computed(() => appStore.sidebarCollapsed)
const activeMenu = computed(() => route.path)
const userInfo = computed(() => userStore.userInfo)

const menuItems = computed<MenuItem[]>(() => {
  function toMenuItems(menus: typeof userStore.menuTree, parentPath = ''): MenuItem[] {
    const items: MenuItem[] = []
    for (const menu of menus) {
      if (menu.visible === '1') continue
      const item: MenuItem = {
        path: menu.path.startsWith('/') ? menu.path : parentPath + '/' + menu.path,
        title: menu.menuName,
        icon: menu.icon || 'Menu',
      }
      if (menu.children && menu.children.length > 0) {
        item.children = toMenuItems(menu.children, item.path)
      }
      items.push(item)
    }
    return items
  }
  return userStore.menuTree.length > 0
    ? toMenuItems(userStore.menuTree)
    : []
})

const toggleSidebar = () => {
  appStore.toggleSidebar()
}

const handleCommand = (command: string) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/profile')
  }
}

onMounted(() => {
  if (userStore.token && !userStore.userInfo) {
    userStore.fetchUserInfo()
  }
  if (userStore.token && !userStore.routesLoaded) {
    userStore.fetchMenuTree()
  }
})
</script>

<style scoped lang="scss">
.main-layout {
  height: 100vh;
}

.sidebar {
  background: linear-gradient(180deg, #f0f9ff 0%, #e8f4fd 100%);
  transition: width 0.3s;
  overflow: hidden;
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.08);

  .logo {
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 16px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.06);
    position: relative;

    .logo-text {
      font-size: 20px;
      font-weight: 700;
      letter-spacing: 2px;
      color: #1e40af;
    }

    .logo-icon {
      font-size: 26px;
      font-weight: 700;
      color: #1e40af;
    }
  }

  .sidebar-menu {
    border-right: none;
    background: transparent;
    padding: 12px 8px;

    &:not(.el-menu--collapse) {
      width: 220px;
    }

    :deep(.el-menu-item) {
      height: 48px;
      line-height: 48px;
      margin: 4px 0;
      border-radius: 8px;
      color: #4b5563;
      font-size: 14px;
      transition: all 0.25s ease;

      &:hover {
        background: #dbeafe;
        color: #1e40af;

        .el-icon {
          color: #1e40af;
        }
      }

      &.is-active {
        background: #dbeafe;
        color: #1e40af;
        font-weight: 600;
        position: relative;

        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 50%;
          transform: translateY(-50%);
          width: 3px;
          height: 24px;
          border-radius: 0 3px 3px 0;
          background: #1e40af;
        }

        .el-icon {
          color: #1e40af;
        }
      }

      .el-icon {
        font-size: 18px;
        transition: color 0.25s ease;
      }
    }
  }
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0 20px;

  .header-left {
    .collapse-btn {
      font-size: 20px;
      cursor: pointer;
      transition: all 0.25s ease;

      &:hover {
        color: #409eff;
        transform: scale(1.1);
      }
    }
  }

  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      cursor: pointer;
      padding: 4px 8px;
      border-radius: 8px;
      transition: background 0.25s ease;

      &:hover {
        background: #f5f7fa;
      }

      .username {
        margin-left: 8px;
        font-size: 14px;
        color: #303133;
      }
    }
  }
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
