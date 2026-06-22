<template>
  <el-container class="layout-container">
    <!-- 左侧导航 -->
    <el-aside width="220px" class="layout-aside">
      <div class="logo">
        <el-icon :size="24"><Monitor /></el-icon>
        <span class="logo-text">杭州大厦AI客服</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#1d1e2c"
        text-color="#a3a6b4"
        active-text-color="#409eff"
        class="sidebar-menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/knowledge">
          <el-icon><Reading /></el-icon>
          <span>知识库管理</span>
        </el-menu-item>
        <el-menu-item index="/chat">
          <el-icon><ChatDotRound /></el-icon>
          <span>AI对话</span>
        </el-menu-item>
        <el-menu-item index="/agent-workbench">
          <el-icon><Headset /></el-icon>
          <span>专属客服工作台</span>
        </el-menu-item>
        <el-menu-item index="/cs">
          <el-icon><Service /></el-icon>
          <span>在线客服</span>
        </el-menu-item>
        <el-menu-item index="/reports">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据报表</span>
        </el-menu-item>
        <el-menu-item index="/settings">
          <el-icon><Setting /></el-icon>
          <span>系统设置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部栏 -->
      <el-header class="layout-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="pageTitle">{{ pageTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-button type="warning" size="small" @click="handleReset" :loading="resetLoading">
            <el-icon><RefreshLeft /></el-icon>
            重置演示数据
          </el-button>
          <el-dropdown>
            <span class="user-info">
              <el-avatar :size="32" src="">
                <el-icon :size="18"><UserFilled /></el-icon>
              </el-avatar>
              <span class="username">管理员</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人信息</el-dropdown-item>
                <el-dropdown-item divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

const route = useRoute()
const resetLoading = ref(false)

const activeMenu = computed(() => {
  const path = route.path
  if (path === '/') return '/dashboard'
  return path
})

const pageTitle = computed(() => {
  return route.meta.title || ''
})

const handleReset = async () => {
  try {
    await ElMessageBox.confirm('确定要重置所有演示数据吗？此操作不可撤销。', '确认重置', {
      confirmButtonText: '确定重置',
      cancelButtonText: '取消',
      type: 'warning'
    })
    resetLoading.value = true
    await api.post('/v1/demo/reset')
    ElMessage.success('演示数据已重置')
  } catch (err) {
    if (err !== 'cancel') {
      console.error('Reset failed:', err)
    }
  } finally {
    resetLoading.value = false
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.layout-aside {
  background-color: #1d1e2c;
  overflow-y: auto;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #fff;
  border-bottom: 1px solid rgba(255,255,255,0.08);
  font-size: 16px;
  font-weight: 600;
}

.logo .el-icon {
  color: #409eff;
}

.sidebar-menu {
  border-right: none;
}

.sidebar-menu .el-menu-item {
  margin: 4px 8px;
  border-radius: 6px;
  height: 44px;
}

.layout-header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.header-left {
  flex: 1;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  font-size: 14px;
  color: #303133;
}

.layout-main {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
