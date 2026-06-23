import { createRouter, createWebHistory } from 'vue-router'
import { isAuthenticated } from '@/utils/auth'
import MainLayout from '@/components/layout/MainLayout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: MainLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          redirect: '/chat',
        },
        {
          path: 'chat',
          name: 'chat',
          component: () => import('../views/ChatView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'provider-config',
          name: 'providerConfig',
          component: () => import('../views/ProviderConfigManagement.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'model-config',
          name: 'modelConfig',
          component: () => import('../views/ModelConfigManagement.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'agent',
          name: 'agent',
          component: () => import('../views/AgentView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'knowledge-document',
          name: 'knowledgeDocument',
          component: () => import('../views/KnowledgeDocumentManagement.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'knowledge-base',
          name: 'knowledgeBase',
          component: () => import('../views/KnowledgeBaseManagement.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'skills',
          name: 'skills',
          component: () => import('../views/SkillManagement.vue'),
          meta: { requiresAuth: true },
        },
      ],
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: { requiresAuth: false },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue'),
      meta: { requiresAuth: false },
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/AboutView.vue'),
      meta: { requiresAuth: false },
    },
  ],
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  const authenticated = isAuthenticated()

  if (to.meta.requiresAuth && !authenticated) {
    // 需要登录但未登录，跳转到登录页
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && authenticated) {
    // 已登录访问登录/注册页，跳转到主页
    next('/')
  } else {
    next()
  }
})

export default router
