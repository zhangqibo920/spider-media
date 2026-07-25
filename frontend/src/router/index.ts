import { createRouter, createWebHistory } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { useUserStore } from '@/stores/user'

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
          meta: { title: '系统管理', icon: 'Setting' },
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('@/views/userauth/ProfileView.vue'),
          meta: { title: '个人中心', icon: 'User' },
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

router.beforeEach((to, _from, next) => {
  NProgress.start()
  document.title = `${to.meta.title || 'SpiderMedia'} - SpiderMedia`

  const userStore = useUserStore()
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth !== false)

  if (requiresAuth && !userStore.token) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})

router.afterEach(() => {
  NProgress.done()
})

export default router
