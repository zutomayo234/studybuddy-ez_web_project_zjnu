import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'login', component: () => import('../views/Login.vue'), meta: { title: '登录' } },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'home',
        component: () => import('../views/Home.vue'),
        meta: { title: '首页概览' }
      },
      {
        path: 'users',
        name: 'users',
        component: () => import('../views/UserManage.vue'),
        meta: { title: '用户管理', perm: 'user:list' }
      },
      {
        path: 'roles',
        name: 'roles',
        component: () => import('../views/RoleManage.vue'),
        meta: { title: '角色权限', perm: 'role:list' }
      },
      {
        path: 'chat',
        name: 'chat',
        component: () => import('../views/Chat.vue'),
        meta: { title: '学习搭子 · 智能问答', perm: 'ai:chat' }
      },
      {
        path: 'kb',
        name: 'kb',
        component: () => import('../views/KnowledgeBase.vue'),
        meta: { title: '知识库管理', perm: 'kb:manage' }
      },
      {
        path: 'logs',
        name: 'logs',
        component: () => import('../views/OperationLog.vue'),
        meta: { title: '操作日志', perm: 'log:list' }
      },
      {
        path: 'profile',
        name: 'profile',
        component: () => import('../views/Profile.vue'),
        meta: { title: '个人中心' }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/home' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

function currentPermissions() {
  try {
    return JSON.parse(localStorage.getItem('sb_permissions') || '[]')
  } catch (e) {
    return []
  }
}

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 学习搭子` : '学习搭子 StudyBuddy'
  const token = localStorage.getItem('sb_token')
  if (to.path !== '/login' && !token) {
    next('/login')
    return
  }
  if (to.path === '/login' && token) {
    next('/home')
    return
  }

  if (to.meta.perm && !currentPermissions().includes(to.meta.perm)) {
    next('/home')
    return
  }
  next()
})

export default router
