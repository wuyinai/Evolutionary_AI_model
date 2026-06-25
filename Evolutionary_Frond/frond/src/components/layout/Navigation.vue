<template>
  <div class="navigation" :class="{ open: isMobileMenuOpen }">
    <!-- Logo区域 -->
    <div class="navigation-header">
      <div class="logo-container">
        <svg class="logo-icon" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"></polygon>
        </svg>
        <span class="logo-text">Evolutionary AI</span>
      </div>
    </div>

    <!-- 导航菜单 -->
    <nav class="navigation-menu">
      <router-link
        v-for="item in menuItems"
        :key="item.path"
        :to="item.path"
        class="nav-item"
        :class="{ active: isActive(item.path) }"
        @click="closeMobileMenu"
      >
        <div class="nav-icon">
          <component :is="item.icon" />
        </div>
        <span class="nav-label">{{ item.label }}</span>
      </router-link>
    </nav>

    <!-- 用户信息区域 -->
    <div class="navigation-footer">
      <div class="user-profile">
        <div class="user-avatar">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
            <circle cx="12" cy="7" r="4"></circle>
          </svg>
        </div>
        <div class="user-info">
          <div class="user-name">{{ userName }}</div>
          <div class="user-email">{{ userEmail }}</div>
        </div>
        <button class="logout-btn" @click="handleLogout" title="退出登录">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
            <polyline points="16 17 21 12 16 7"></polyline>
            <line x1="21" y1="12" x2="9" y2="12"></line>
          </svg>
        </button>
      </div>
    </div>

    <!-- 移动端折叠按钮 -->
    <button class="mobile-toggle-btn" @click="toggleMobileMenu">
      <svg v-if="!isMobileMenuOpen" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <line x1="3" y1="12" x2="21" y2="12"></line>
        <line x1="3" y1="6" x2="21" y2="6"></line>
        <line x1="3" y1="18" x2="21" y2="18"></line>
      </svg>
      <svg v-else width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <line x1="18" y1="6" x2="6" y2="18"></line>
        <line x1="6" y1="6" x2="18" y2="18"></line>
      </svg>
    </button>

    <!-- 移动端遮罩层 -->
    <div v-if="isMobileMenuOpen" class="mobile-overlay" @click="closeMobileMenu"></div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isMobileMenuOpen = ref(false)

// 用户信息
const userName = computed(() => userStore.userInfo?.username || '用户')
const userEmail = computed(() => userStore.userInfo?.email || 'user@example.com')

// 导航菜单项
const menuItems = [
  {
    path: '/chat',
    label: 'AI对话',
    icon: h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
      h('path', { d: 'M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z' })
    ])
  },
  {
    path: '/knowledge-base',
    label: '知识库管理',
    icon: h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
      h('path', { d: 'M4 19.5A2.5 2.5 0 0 1 6.5 17H20' }),
      h('path', { d: 'M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z' })
    ])
  },
  {
    path: '/knowledge-document',
    label: '文档管理',
    icon: h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
      h('path', { d: 'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z' }),
      h('polyline', { points: '14 2 14 8 20 8' }),
      h('line', { x1: 16, y1: 13, x2: 8, y2: 13 }),
      h('line', { x1: 16, y1: 17, x2: 8, y2: 17 }),
      h('polyline', { points: '10 9 9 9 8 9' })
    ])
  },
  {
    path: '/skills',
    label: 'Skills仓库',
    icon: h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
      h('path', { d: 'M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z' })
    ])
  },
  {
    path: '/role-expert',
    label: '角色专家',
    icon: h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
      h('path', { d: 'M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2' }),
      h('circle', { cx: 9, cy: 7, r: 4 }),
      h('path', { d: 'M23 21v-2a4 4 0 0 0-3-3.87' }),
      h('path', { d: 'M16 3.13a4 4 0 0 1 0 7.75' })
    ])
  },
  {
    path: '/provider-config',
    label: '供应商配置',
    icon: h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
      h('path', { d: 'M12 2L2 7l10 5 10-5-10-5z' }),
      h('path', { d: 'M2 17l10 5 10-5' }),
      h('path', { d: 'M2 12l10 5 10-5' })
    ])
  },
  {
    path: '/model-config',
    label: '模型配置',
    icon: h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
      h('polygon', { points: '12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2' })
    ])
  },
  {
    path: '/agent',
    label: 'Agent助手',
    icon: h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
      h('rect', { x: 3, y: 11, width: 18, height: 10, rx: 2 }),
      h('circle', { cx: 12, cy: 5, r: 2 }),
      h('path', { d: 'M12 7v4' }),
      h('line', { x1: 8, y1: 16, x2: 8, y2: 16 }),
      h('line', { x1: 16, y1: 16, x2: 16, y2: 16 }),
      h('line', { x1: 9, y1: 20, x2: 15, y2: 20 })
    ])
  },
  {
    path: '/ai-hotspot-monitor',
    label: 'AI热点监控',
    icon: h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
      h('rect', { x: 3, y: 3, width: 18, height: 18, rx: 2, ry: 2 }),
      h('line', { x1: 12, y1: 8, x2: 12, y2: 16 }),
      h('line', { x1: 8, y1: 12, x2: 16, y2: 12 })
    ])
  },
  {
    path: '/reserved-3',
    label: '预留3',
    icon: h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
      h('rect', { x: 3, y: 3, width: 18, height: 18, rx: 2, ry: 2 }),
      h('line', { x1: 12, y1: 8, x2: 12, y2: 16 }),
      h('line', { x1: 8, y1: 12, x2: 16, y2: 12 })
    ])
  },
  {
    path: '/reserved-4',
    label: '预留4',
    icon: h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
      h('rect', { x: 3, y: 3, width: 18, height: 18, rx: 2, ry: 2 }),
      h('line', { x1: 12, y1: 8, x2: 12, y2: 16 }),
      h('line', { x1: 8, y1: 12, x2: 16, y2: 12 })
    ])
  }
]

// 判断当前路由是否激活
const isActive = (path: string) => {
  return route.path === path || route.path.startsWith(path + '/')
}

// 切换移动端菜单
const toggleMobileMenu = () => {
  isMobileMenuOpen.value = !isMobileMenuOpen.value
}

// 关闭移动端菜单
const closeMobileMenu = () => {
  isMobileMenuOpen.value = false
}

// 退出登录
const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.navigation {
  width: 240px;
  height: 100vh;
  background-color: #ffffff;
  border-right: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0;
  top: 0;
  z-index: 100;
  transition: transform 0.3s ease-out;
}

/* Logo区域 */
.navigation-header {
  padding: 24px 20px;
  border-bottom: 1px solid var(--color-border);
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  color: var(--color-primary);
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text);
}

/* 导航菜单 */
.navigation-menu {
  flex: 1;
  padding: 16px 12px;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  margin-bottom: 4px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.2s ease-out;
  color: var(--color-text-secondary);
  text-decoration: none;
  position: relative;
}

.nav-item:hover {
  background-color: var(--color-background-soft);
  color: var(--color-text);
}

.nav-item.active {
  background-color: var(--color-primary-light);
  color: var(--color-primary);
  border-left: 3px solid var(--color-primary);
  padding-left: 13px;
}

.nav-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-label {
  font-size: 15px;
  font-weight: 500;
}

/* 用户信息区域 */
.navigation-footer {
  padding: 20px;
  border-top: 1px solid var(--color-border);
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: var(--color-background-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-secondary);
  border: 2px solid var(--color-border);
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-email {
  font-size: 12px;
  color: var(--color-text-tertiary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.logout-btn {
  padding: 8px;
  border-radius: var(--radius-sm);
  color: var(--color-text-secondary);
  transition: all 0.2s ease-out;
  cursor: pointer;
}

.logout-btn:hover {
  background-color: var(--color-background-soft);
  color: var(--color-text);
}

/* 移动端折叠按钮 */
.mobile-toggle-btn {
  display: none;
  position: fixed;
  top: 20px;
  left: 20px;
  width: 44px;
  height: 44px;
  background-color: #ffffff;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease-out;
  box-shadow: var(--shadow-md);
  z-index: 200;
}

.mobile-toggle-btn:hover {
  background-color: var(--color-background-soft);
  border-color: var(--color-primary);
}

/* 移动端遮罩层 */
.mobile-overlay {
  display: none;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.3);
  z-index: 90;
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .navigation {
    transform: translateX(-100%);
    width: 280px;
  }

  .navigation.open {
    transform: translateX(0);
  }

  .mobile-toggle-btn {
    display: flex;
  }

  .mobile-overlay {
    display: block;
  }

  .nav-label {
    display: block;
  }

  .user-info {
    display: block;
  }
}

@media (max-width: 480px) {
  .navigation {
    width: 100%;
  }

  .logo-text {
    font-size: 16px;
  }
}
</style>