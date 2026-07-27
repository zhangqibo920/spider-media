import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw, Component } from 'vue-router'
import { defineAsyncComponent } from 'vue'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { useUserStore } from '@/stores/user'
import type { Menu } from '@/types'

declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    icon?: string
    requiresAuth?: boolean
    roles?: string[]
    hidden?: boolean
  }
}

const componentMap: Record<string, Component> = {
  'dashboard/DashboardView': defineAsyncComponent(() => import('@/views/dashboard/DashboardView.vue')),
  'datacollection/CollectionView': defineAsyncComponent(() => import('@/views/datacollection/CollectionView.vue')),
  'aicreation/AiCreationView': defineAsyncComponent(() => import('@/views/aicreation/AiCreationView.vue')),
  'contentpublish/PublishView': defineAsyncComponent(() => import('@/views/contentpublish/PublishView.vue')),
  'taskscheduler/SchedulerView': defineAsyncComponent(() => import('@/views/taskscheduler/SchedulerView.vue')),
  'systemadmin/SysConfigView': defineAsyncComponent(() => import('@/views/systemadmin/SysConfigView.vue')),
  'systemadmin/AdminUsersView': defineAsyncComponent(() => import('@/views/systemadmin/AdminUsersView.vue')),
  'systemadmin/AdminLogsView': defineAsyncComponent(() => import('@/views/systemadmin/AdminLogsView.vue')),
  'systemadmin/ModelManageView': defineAsyncComponent(() => import('@/views/systemadmin/ModelManageView.vue')),
  'systemadmin/DictManageView': defineAsyncComponent(() => import('@/views/systemadmin/DictManageView.vue')),
  'systemadmin/MenuManageView': defineAsyncComponent(() => import('@/views/systemadmin/MenuManageView.vue')),
  'systemadmin/RoleManageView': defineAsyncComponent(() => import('@/views/systemadmin/RoleManageView.vue')),
  'userauth/ProfileView': defineAsyncComponent(() => import('@/views/userauth/ProfileView.vue')),
}

function generateRoutes(menus: Menu[]): RouteRecordRaw[] {
  const routes: RouteRecordRaw[] = []
  for (const menu of menus) {
    if (menu.menuType === 'F') continue
    const routePath = menu.path.startsWith('/') ? menu.path.substring(1) : menu.path
    const routeName = routePath.replace(/[\/-]/g, '_') || 'root'
    const route: RouteRecordRaw = {
      path: routePath,
      name: routeName,
      meta: {
        title: menu.menuName,
        icon: menu.icon || undefined,
        hidden: menu.visible === '1',
      },
    }
    if (menu.menuType === 'M') {
      const childRoutes = generateRoutes(menu.children || [])
      route.children = childRoutes
      if (childRoutes.length > 0) {
        route.redirect = childRoutes[0].path
      }
    } else if (menu.component && componentMap[menu.component]) {
      route.component = componentMap[menu.component]
      if (menu.children && menu.children.length > 0) {
        route.children = generateRoutes(menu.children)
      }
    }
    routes.push(route)
  }
  return routes
}

export function buildDynamicRoutes(menus: Menu[]): RouteRecordRaw[] {
  return generateRoutes(menus)
}

const DYNAMIC_ROUTE_NAMES = new Set<string>()

export function clearDynamicRoutes(router: ReturnType<typeof createRouter>) {
  for (const name of DYNAMIC_ROUTE_NAMES) {
    if (router.hasRoute(name)) {
      router.removeRoute(name)
    }
  }
  DYNAMIC_ROUTE_NAMES.clear()
}

export function addDynamicRoutes(router: ReturnType<typeof createRouter>, menus: Menu[]) {
  clearDynamicRoutes(router)
  const routes = buildDynamicRoutes(menus)
  for (const route of routes) {
    router.addRoute('Layout', route)
    if (route.name) {
      DYNAMIC_ROUTE_NAMES.add(route.name as string)
    }
  }
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/userauth/LoginView.vue'),
      meta: { title: '登录', requiresAuth: false },
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/userauth/RegisterView.vue'),
      meta: { title: '注册', requiresAuth: false },
    },
    {
      path: '/',
      name: 'Layout',
      component: () => import('@/components/layout/MainLayout.vue'),
      redirect: '/dashboard',
      meta: { requiresAuth: true },
      children: [],
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/error/NotFound.vue'),
      meta: { title: '页面不存在' },
    },
  ],
})

router.beforeEach(async (to, _from, next) => {
  NProgress.start()
  document.title = `${to.meta.title || 'SpiderMedia'} - SpiderMedia`

  const userStore = useUserStore()
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth !== false)

  if (!requiresAuth) {
    NProgress.done()
    next()
    return
  }

  if (!userStore.token) {
    NProgress.done()
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  if (!userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
    } catch {
      NProgress.done()
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }
  }

  if (!userStore.routesLoaded) {
    await userStore.fetchMenuTree()
    if (userStore.menuTree.length > 0) {
      addDynamicRoutes(router, userStore.menuTree)
    }
    // 路由已动态添加，重新导航到目标路径，让 Vue Router 重新计算 to.matched
    next({ path: to.fullPath, replace: true })
    return
  }

  if (to.path === '/') {
    next({ path: '/dashboard', replace: true })
    return
  }

  next()
})

router.afterEach(() => {
  NProgress.done()
})

export default router
