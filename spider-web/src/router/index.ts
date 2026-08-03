import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
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

const componentMap: Record<string, () => Promise<any>> = {
  'dashboard/DashboardView': () => import('@/views/dashboard/DashboardView.vue'),
  'datacollection/CollectionView': () => import('@/views/datacollection/CollectionView.vue'),
  'aicreation/AiCreationView': () => import('@/views/aicreation/AiCreationView.vue'),
  'contentpublish/AccountManageView': () => import('@/views/contentpublish/AccountManageView.vue'),
  'contentpublish/PublishTaskView': () => import('@/views/contentpublish/PublishTaskView.vue'),
  'taskscheduler/SchedulerView': () => import('@/views/taskscheduler/SchedulerView.vue'),
  'systemadmin/SysConfigView': () => import('@/views/systemadmin/SysConfigView.vue'),
  'systemadmin/AdminUsersView': () => import('@/views/systemadmin/AdminUsersView.vue'),
  'systemadmin/AdminLogsView': () => import('@/views/systemadmin/AdminLogsView.vue'),
  'systemadmin/ModelManageView': () => import('@/views/systemadmin/ModelManageView.vue'),
  'systemadmin/DictManageView': () => import('@/views/systemadmin/DictManageView.vue'),
  'systemadmin/MenuManageView': () => import('@/views/systemadmin/MenuManageView.vue'),
  'systemadmin/RoleManageView': () => import('@/views/systemadmin/RoleManageView.vue'),
  'hotmonitor/KeywordManageView': () => import('@/views/hotmonitor/KeywordManageView.vue'),
  'hotmonitor/HotFeedView': () => import('@/views/hotmonitor/HotFeedView.vue'),
  'userauth/ProfileView': () => import('@/views/userauth/ProfileView.vue'),
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
    } as RouteRecordRaw
    if (menu.menuType === 'M') {
      const childRoutes = generateRoutes(menu.children || [])
      route.children = childRoutes
      if (childRoutes.length > 0) {
        route.redirect = childRoutes[0].path
      }
    } else if (menu.component && componentMap[menu.component]) {
      route.component = componentMap[menu.component]
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
  const appTitle = to.meta.title || 'SpiderMedia'
  document.title = `${appTitle} - SpiderMedia`

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

  const routeRoles = to.meta.roles
  if (routeRoles && routeRoles.length > 0) {
    const userRoleKeys = userStore.roles.map(r => r.roleKey)
    if (userStore.userInfo?.role) userRoleKeys.push(userStore.userInfo.role)
    const hasRole = routeRoles.some(role => userRoleKeys.includes(role))
    if (!hasRole) {
      NProgress.done()
      next({ name: 'NotFound' })
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
