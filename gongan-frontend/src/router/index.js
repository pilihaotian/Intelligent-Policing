import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/views/layout/index.vue'),
    redirect: '/system/user',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'system',
        redirect: '/system/user',
        meta: { title: '系统管理' },
        children: [
          {
            path: 'user',
            name: 'UserManage',
            component: () => import('@/views/system/user/index.vue'),
            meta: { title: '用户管理' }
          },
          {
            path: 'role',
            name: 'RoleManage',
            component: () => import('@/views/system/role/index.vue'),
            meta: { title: '角色管理' }
          },
          {
            path: 'org',
            name: 'OrgManage',
            component: () => import('@/views/system/org/index.vue'),
            meta: { title: '机构管理' }
          }
        ]
      },
      {
        path: 'ops-risk',
        redirect: '/ops-risk/document',
        meta: { title: '执法办案' },
        children: [
          {
            path: 'document',
            name: 'LegalDocument',
            component: () => import('@/views/ops-risk/document/index.vue'),
            meta: { title: '案件信息填报' }
          }
        ]
      },
      {
        path: 'ai-assistant',
        redirect: '/ai-assistant/chat',
        meta: { title: '智能助手' },
        children: [
          {
            path: 'kb',
            name: 'KnowledgeBase',
            component: () => import('@/views/ai-assistant/kb/index.vue'),
            meta: { title: '知识库管理' }
          },
          {
            path: 'chat',
            name: 'AiChat',
            component: () => import('@/views/ai-assistant/chat/index.vue'),
            meta: { title: '智能问答' }
          }
        ]
      },
      {
        path: 'smart-nav',
        redirect: '/smart-nav/entry',
        meta: { title: '智能导航' },
        children: [
          {
            path: 'entry',
            name: 'SmartNav',
            component: () => import('@/views/smart-nav/entry/index.vue'),
            meta: { title: '智能导航' }
          }
        ]
      },
      {
        path: 'anti-fraud',
        redirect: '/anti-fraud/customer',
        meta: { title: '刑侦研判' },
        children: [
          {
            path: 'customer',
            name: 'SuspiciousCustomer',
            component: () => import('@/views/anti-fraud/customer/index.vue'),
            meta: { title: '重点人员' }
          },
          {
            path: 'transaction',
            name: 'TransactionList',
            component: () => import('@/views/anti-fraud/transaction/index.vue'),
            meta: { title: '资金流水' }
          },
          {
            path: 'analysis',
            name: 'FraudAnalysis',
            component: () => import('@/views/anti-fraud/analysis/index.vue'),
            meta: { title: '案件分析' }
          }
        ]
      },
      {
        path: 'aml',
        redirect: '/aml/due-diligence',
        meta: { title: '治安管理' },
        children: [
          {
            path: 'due-diligence',
            name: 'DueDiligence',
            component: () => import('@/views/aml/due-diligence/index.vue'),
            meta: { title: '人员核查' }
          },
          {
            path: 'suspicious',
            name: 'AmlSuspicious',
            component: () => import('@/views/aml/suspicious/index.vue'),
            meta: { title: '线索管理' }
          }
        ]
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/anti-fraud/customer'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth !== false && !userStore.token) {
    next('/login')
  } else if (to.path === '/login' && userStore.token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router