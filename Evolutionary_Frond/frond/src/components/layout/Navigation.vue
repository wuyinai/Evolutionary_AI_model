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
      <template v-for="item in displayMenuList" :key="item.path">
        <!-- 有子菜单的项（目录/分组） -->
        <div v-if="item.children && item.children.length > 0" class="nav-group">
          <div
            class="nav-item"
            :class="{ active: isGroupActive(item) }"
            @click="toggleGroup(item.path)"
          >
            <div v-if="item.icon" class="nav-icon">
              <feather-icon :type="item.icon" :size="20" />
            </div>
            <span class="nav-label">{{ item.permissionName }}</span>
            <svg
              class="expand-arrow"
              :class="{ expanded: expandedGroups[item.path] }"
              width="12"
              height="12"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <polyline points="6 9 12 15 18 9"></polyline>
            </svg>
          </div>
          <!-- 子菜单 -->
          <div v-if="expandedGroups[item.path]" class="nav-submenu">
            <router-link
              v-for="child in item.children"
              :key="child.path"
              :to="child.path"
              class="nav-sub-item"
              :class="{ active: isActive(child.path) }"
              @click="closeMobileMenu"
            >
              <div v-if="child.icon" class="nav-icon">
                <feather-icon :type="child.icon" :size="18" />
              </div>
              <span class="nav-label">{{ child.permissionName }}</span>
            </router-link>
          </div>
        </div>
        <!-- 无子菜单的项（单个菜单） -->
        <router-link
          v-else
          :to="item.path"
          class="nav-item"
          :class="{ active: isActive(item.path) }"
          @click="closeMobileMenu"
        >
          <div v-if="item.icon" class="nav-icon">
            <feather-icon :type="item.icon" :size="20" />
          </div>
          <span class="nav-label">{{ item.permissionName }}</span>
        </router-link>
      </template>
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
import { computed, ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUserMenuTree, type SysPermission } from '@/utils/sysPermissionApi'
import FeatherIcon from '@/components/FeatherIcon.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isMobileMenuOpen = ref(false)
const expandedGroups = reactive<Record<string, boolean>>({})

// 用户信息
const userName = computed(() => userStore.userInfo?.username || '用户')
const userEmail = computed(() => userStore.userInfo?.email || 'user@example.com')

// 构建树形菜单：将后端返回的扁平列表转为树形结构
function buildMenuTree(permissions: SysPermission[]): SysPermission[] {
  const map = new Map<string, SysPermission>()
  const roots: SysPermission[] = []

  // 先按 id 建立映射
  permissions.forEach(p => {
    map.set(p.id, { ...p, children: [] as SysPermission[] })
  })

  // 构建树
  permissions.forEach(p => {
    const node = map.get(p.id)!
    if (p.parentId === '0' || !map.has(p.parentId)) {
      roots.push(node)
    } else {
      const parent = map.get(p.parentId)
      if (parent) {
        if (!parent.children) {
          parent.children = []
        }
        ;(parent.children as SysPermission[]).push(node)
      }
    }
  })

  return roots
}

// 动态菜单列表（树形结构）
const menuTree = ref<SysPermission[]>([])
const displayMenuList = computed(() => menuTree.value)

// 加载菜单
async function loadMenus() {
  try {
    const response = await getUserMenuTree()
    if (response.code === 200 && response.data) {
      const tree = buildMenuTree(response.data)
      menuTree.value = tree
      // 缓存到 store
      userStore.userMenus = response.data
    }
  } catch (error) {
    console.error('加载菜单失败:', error)
  }
}

onMounted(() => {
  loadMenus()
})

// 判断当前路由是否激活
const isActive = (path?: string) => {
  if (!path) return false
  return route.path === path || route.path.startsWith(path + '/')
}

// 判断分组是否激活
const isGroupActive = (item: SysPermission) => {
  if (item.children && (item.children as SysPermission[]).length > 0) {
    return (item.children as SysPermission[]).some((child: SysPermission) => isActive(child.path))
  }
  return isActive(item.path)
}

// 切换分组展开/折叠
const toggleGroup = (path?: string) => {
  if (!path) return
  expandedGroups[path] = !expandedGroups[path]
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

.expand-arrow {
  margin-left: auto;
  transition: transform 0.2s ease-out;
  color: var(--color-text-tertiary);
}

.expand-arrow.expanded {
  transform: rotate(180deg);
}

/* 导航分组 */
.nav-group {
  margin-bottom: 4px;
}

/* 子菜单 */
.nav-submenu {
  margin-left: 12px;
  margin-top: 4px;
}

.nav-sub-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  margin-bottom: 2px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.2s ease-out;
  color: var(--color-text-secondary);
  text-decoration: none;
  font-size: 14px;
}

.nav-sub-item:hover {
  background-color: var(--color-background-soft);
  color: var(--color-text);
}

.nav-sub-item.active {
  background-color: var(--color-primary-light);
  color: var(--color-primary);
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
