import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/chat'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/Index.vue')
  },
  {
    path: '/splash',
    name: 'Splash',
    component: () => import('../views/splash/Index.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/register/Index.vue')
  },
  {
    path: '/chat',
    name: 'Chat',
    component: () => import('../views/chat/Index.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/contacts',
    name: 'Contacts',
    component: () => import('../views/contacts/Index.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/profile/Index.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/search',
    name: 'Search',
    component: () => import('../views/search/Index.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/search/message',
    name: 'SearchMessage',
    component: () => import('../views/search/Message.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/group/:id',
    name: 'GroupDetail',
    component: () => import('../views/group/Index.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/group/create',
    name: 'GroupCreate',
    component: () => import('../views/group/Create.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('../views/settings/Index.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/admin/Index.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/office',
    name: 'Office',
    component: () => import('../views/office/Index.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/knowledge',
    name: 'Knowledge',
    component: () => import('../views/knowledge/Index.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/knowledge/edit',
    name: 'KnowledgeEdit',
    component: () => import('../views/knowledge/Editor.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('vela_token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
