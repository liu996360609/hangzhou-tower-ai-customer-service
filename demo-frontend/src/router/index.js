import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/MainLayout.vue'

const routes = [
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/Dashboard.vue'), meta: { title: '首页' } },
      { path: 'knowledge', name: 'Knowledge', component: () => import('@/views/Knowledge.vue'), meta: { title: '知识库管理' } },
      { path: 'chat', name: 'Chat', component: () => import('@/views/Chat.vue'), meta: { title: 'AI对话' } },
      { path: 'cs', name: 'CustomerService', component: () => import('@/views/CustomerService.vue'), meta: { title: '在线客服' } },
      { path: 'agent-workbench', name: 'AgentWorkbench', component: () => import('@/views/AgentWorkbench.vue'), meta: { title: '专属客服工作台' } },
      { path: 'reports', name: 'Reports', component: () => import('@/views/Reports.vue'), meta: { title: '数据报表' } },
      { path: 'settings', name: 'Settings', component: () => import('@/views/Settings.vue'), meta: { title: '系统设置' } },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
