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
      <div class="user-profile" @click="toggleUserDropdown">
        <div class="user-avatar" :class="{ 'dropdown-active': showUserDropdown }">
          <img v-if="userAvatar" :src="userAvatar" alt="头像" class="avatar-img" />
          <svg v-else width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
            <circle cx="12" cy="7" r="4"></circle>
          </svg>
        </div>
        <div class="user-info">
          <div class="user-name">{{ userName || '加载中...' }}</div>
          <div class="user-email">{{ userEmail }}</div>
        </div>
        <svg class="dropdown-indicator" :class="{ 'rotated': showUserDropdown }" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="6 9 12 15 18 9"></polyline>
        </svg>
      </div>

      <!-- 用户下拉菜单 -->
      <div v-if="showUserDropdown" class="user-dropdown-menu">
        <div class="dropdown-item" @click="goToProfile">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
            <circle cx="12" cy="7" r="4"></circle>
          </svg>
          <span>个人主页</span>
        </div>
        <div class="dropdown-item" @click="handleReserved1">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="3"></circle>
            <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"></path>
          </svg>
          <span>个人预留1</span>
        </div>
        <div class="dropdown-item" @click="handleReserved2">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
            <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
          </svg>
          <span>个人预留2</span>
        </div>
        <div class="dropdown-item" @click="handleReserved3">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
            <line x1="3" y1="9" x2="21" y2="9"></line>
            <line x1="9" y1="21" x2="9" y2="9"></line>
          </svg>
          <span>个人预留3</span>
        </div>
        <div class="dropdown-divider"></div>
        <div class="dropdown-item logout-item" @click="handleLogout">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
            <polyline points="16 17 21 12 16 7"></polyline>
            <line x1="21" y1="12" x2="9" y2="12"></line>
          </svg>
          <span>退出登录</span>
        </div>
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
import { get } from '@/utils/request'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isMobileMenuOpen = ref(false)
const expandedGroups = reactive<Record<string, boolean>>({})
const showUserDropdown = ref(false)

// 用户信息
const userName = computed(() => userStore.userInfo?.username || userInfoCache.value?.username || '')
// 备用：从 /profile/user-info 加载的完整用户信息
const userInfoCache = ref<{ username?: string; email?: string } | null>(null)
const userEmail = computed(() => userStore.userInfo?.email || userInfoCache.value?.email || '')
const userAvatar = computed(() => userStore.userInfo?.avatar || userInfoCache.value?.avatar || '')

const loadCurrentUser = async () => {
  if (userStore.userInfo?.username) return
  try {
    const response = await get<any>('/profile/user-info')
    if (response.code === 200 && response.data) {
      userInfoCache.value = response.data
    }
  } catch (error) {
    console.error('加载当前用户信息失败:', error)
  }
}

// 切换用户下拉菜单
const toggleUserDropdown = () => {
  showUserDropdown.value = !showUserDropdown.value
}

// 导航到个人主页
const goToProfile = () => {
  showUserDropdown.value = false
  router.push('/profile')
}

// 预留功能处理
const handleReserved1 = () => {
  showUserDropdown.value = false
  // TODO: 实现预留功能1
  console.log('预留功能1')
}

const handleReserved2 = () => {
  showUserDropdown.value = false
  // TODO: 实现预留功能2
  console.log('预留功能2')
}

const handleReserved3 = () => {
  showUserDropdown.value = false
  // TODO: 实现预留功能3
  console.log('预留功能3')
}

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
  loadCurrentUser()
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
  showUserDropdown.value = false
  userStore.logout()
  router.push('/login')
}

// 点击外部关闭下拉菜单
const handleClickOutside = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  if (!target.closest('.navigation-footer')) {
    showUserDropdown.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})
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
  position: relative;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.2s ease-out;
  position: relative;
}

.user-profile:hover {
  background-color: var(--color-background-soft);
  border-radius: var(--radius-md);
  padding: 8px;
  margin: -8px;
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
  transition: all 0.2s ease-out;
  overflow: hidden;
}

.user-avatar .avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-avatar.dropdown-active {
  border-color: var(--color-primary);
  background-color: var(--color-primary-light);
  color: var(--color-primary);
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

.dropdown-indicator {
  color: var(--color-text-tertiary);
  transition: transform 0.2s ease-out;
}

.dropdown-indicator.rotated {
  transform: rotate(180deg);
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

/* 用户下拉菜单 */
.user-dropdown-menu {
  position: absolute;
  bottom: calc(100% + 8px);
  left: 0;
  right: 0;
  background-color: #ffffff;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  z-index: 1000;
  animation: slideUp 0.2s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.2s ease-out;
  color: var(--color-text);
  font-size: 14px;
}

.dropdown-item:hover {
  background-color: var(--color-background-soft);
}

.dropdown-item:first-child {
  border-radius: var(--radius-md) var(--radius-md) 0 0;
}

.dropdown-item svg {
  color: var(--color-text-secondary);
  flex-shrink: 0;
}

.dropdown-divider {
  height: 1px;
  background-color: var(--color-border);
  margin: 4px 0;
}

.logout-item {
  border-radius: 0 0 var(--radius-md) var(--radius-md);
  color: var(--color-error);
}

.logout-item:hover {
  background-color: #fee;
}

.logout-item svg {
  color: var(--color-error);
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
