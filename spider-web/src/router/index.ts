import { createRouter, createWebHistory } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { useUserStore } from '@/stores/user'

/**
 * 路由 meta 扩展类型
 *
 * - requiresAuth: 是否需要登录（默认 true）
 * - roles: 允许访问的角色列表，为空表示所有登录用户均可访问
 * - title: 页面标题
 * - icon: 侧边栏图标（Element Plus 图标名）
 */
declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    icon?: string
    requiresAuth?: boolean
    roles?: string[]
    hidden?: boolean
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
      component: () => import('@/components/layout/MainLayout.vue'),
      redirect: '/dashboard',
      meta: { requiresAuth: true },
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/dashboard/DashboardView.vue'),
          meta: { title: '工作台', icon: 'Odometer' },
        },
        {
          path: 'collection',
          name: 'DataCollection',
          component: () => import('@/views/datacollection/CollectionView.vue'),
          meta: { title: '数据采集', icon: 'Download' },
        },
        {
          path: 'ai-creation',
          name: 'AiCreation',
          component: () => import('@/views/aicreation/AiCreationView.vue'),
          meta: { title: 'AI创作', icon: 'MagicStick' },
        },
        {
          path: 'publish',
          name: 'ContentPublish',
          component: () => import('@/views/contentpublish/PublishView.vue'),
          meta: { title: '内容发布', icon: 'Promotion' },
        },
        {
          path: 'scheduler',
          name: 'TaskScheduler',
          component: () => import('@/views/taskscheduler/SchedulerView.vue'),
          meta: { title: '任务调度', icon: 'Timer' },
        },
        {
          path: 'admin',
          name: 'SystemAdmin',
          component: () => import('@/views/systemadmin/AdminView.vue'),
          // 仅 ADMIN 角色可见可访问
          meta: { title: '系统管理', icon: 'Setting', roles: ['ADMIN'] },
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('@/views/userauth/ProfileView.vue'),
          // 个人中心在侧边栏不显示，仅通过下拉菜单进入
          meta: { title: '个人中心', icon: 'User', hidden: true },
        },
      ],
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/error/NotFound.vue'),
      meta: { title: '页面不存在' },
    },
  ],
})

/**
 * 全局前置守卫
 *
 * 1. 鉴权：未登录访问受保护页面 → 跳转登录页（带 redirect 参数）
 * 2. 角色校验：route.meta.roles 非空时，需用户角色在列表中，否则跳转工作台
 * 3. 角色信息异步加载：token 存在但 userInfo 未加载时，先拉取用户信息再校验
 */
router.beforeEach(async (to, _from, next) => {
  NProgress.start()
  document.title = `${to.meta.title || 'SpiderMedia'} - SpiderMedia`

  const userStore = useUserStore()
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth !== false)

  // 不需要登录的页面直接放行
  if (!requiresAuth) {
    next()
    return
  }

  // 未登录 → 跳转登录页
  if (!userStore.token) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  // 已登录但用户信息未加载（如刷新页面）→ 先拉取用户信息
  if (!userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
    } catch {
      // 拉取失败（token 过期等）→ fetchUserInfo 内部已调用 logout
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }
  }

  // 角色校验：route.meta.roles 非空时，用户角色必须在列表中
  const requiredRoles = to.meta.roles
  if (requiredRoles && requiredRoles.length > 0) {
    const userRole = userStore.userInfo?.role
    if (!userRole || !requiredRoles.includes(userRole)) {
      // 无权访问 → 跳转工作台
      next({ name: 'Dashboard' })
      return
    }
  }

  next()
})

router.afterEach(() => {
  NProgress.done()
})

export default router
