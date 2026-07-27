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
  background: #f0f9ff;
  border-right: 1px solid rgba(0, 0, 0, 0.06);
  transition: width 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  box-shadow: none;

  .logo {
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-bottom: 1px solid #f0f0f0;

    .logo-text {
      font-size: 20px;
      font-weight: 700;
      letter-spacing: 2px;
      background: linear-gradient(135deg, #1677ff, #0958d9);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    .logo-icon {
      font-size: 26px;
      font-weight: 700;
      background: linear-gradient(135deg, #1677ff, #0958d9);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
  }

  .sidebar-menu {
    border-right: none;
    background: transparent;

    &:not(.el-menu--collapse) {
      width: 220px;
    }

    :deep(.el-menu-item) {
      height: 44px;
      line-height: 44px;
      margin: 2px 8px;
      border-radius: 8px;
      color: #5b6871;
      font-size: 14px;
      transition: all 0.2s ease;

      &:hover {
        background: #f0f5ff;
        color: #1677ff;

        .el-icon { color: #1677ff; }
      }

      &.is-active {
        background: #fff;
        color: #1677ff;
        font-weight: 500;

        .el-icon { color: #1677ff; }
      }

      .el-icon {
        font-size: 18px;
        color: #8c8c8c;
        transition: color 0.2s ease;
      }
    }

    :deep(.el-sub-menu) {
      .el-sub-menu__title {
        height: 44px;
        line-height: 44px;
        margin: 2px 8px;
        border-radius: 8px;
        color: #5b6871;
        font-size: 14px;
        transition: all 0.2s ease;

        &:hover {
          background: #f0f5ff;
          color: #1677ff;

          .el-icon { color: #1677ff; }
        }

        .el-icon {
          font-size: 18px;
          color: #8c8c8c;
          transition: color 0.2s ease;
        }
      }

      .el-menu {
        background: transparent;

        .el-menu-item {
          padding-left: 52px !important;
          height: 40px;
          line-height: 40px;
          margin: 1px 8px;
          font-size: 13px;
          border-radius: 6px;

          &.is-active {
            background: #fff;
            font-weight: 500;
          }
        }
      }
    }
  }
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  box-shadow: none;
  padding: 0 24px;

  .header-left {
    .collapse-btn {
      font-size: 18px;
      cursor: pointer;
      color: #8c8c8c;
      transition: all 0.2s ease;

      &:hover {
        color: #1677ff;
      }
    }
  }

  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      cursor: pointer;
      padding: 4px 10px;
      border-radius: 8px;
      transition: all 0.2s ease;

      &:hover {
        background: #f5f5f5;
      }

      .username {
        margin-left: 8px;
        font-size: 14px;
        color: #262626;
      }
    }
  }
}

.main-content {
  background-color: #f5f5f5;
  padding: 20px;
  min-height: 0;
}
</style>
