<template>
  <div class="sidebar">
    <div class="sidebar-header">
      <!-- AI角色选择器 -->
      <div class="role-selector">
        <div class="role-selector-label text-xs text-secondary">AI角色</div>
        <div class="role-dropdown" @click="toggleRoleDropdown">
          <div class="role-selected">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
              <circle cx="12" cy="7" r="4"></circle>
            </svg>
            <span class="role-name text-sm">{{ conversationStore.selectedRoleName }}</span>
            <svg class="dropdown-arrow" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="6 9 12 15 18 9"></polyline>
            </svg>
          </div>
          <div v-if="showRoleDropdown" class="role-dropdown-menu">
            <div
              v-for="role in roles"
              :key="role.id"
              class="role-option"
              :class="{ active: conversationStore.selectedRoleId === role.id }"
              @click.stop="selectRole(role.id, role.roleName)"
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
              <span>{{ role.roleName }}</span>
            </div>
            <div v-if="roles.length === 0" class="role-option disabled">
              <span class="text-secondary">暂无角色</span>
            </div>
          </div>
        </div>
      </div>

      <button class="btn btn-secondary btn-full new-chat-btn" @click="createNewConversation">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="12" y1="5" x2="12" y2="19"></line>
          <line x1="5" y1="12" x2="19" y2="12"></line>
        </svg>
        <span>开启新对话</span>
      </button>
    </div>

    <div class="sidebar-content">
      <!-- Configuration Menu -->
      <div class="config-menu">
        <div class="menu-label text-sm text-secondary">配置管理</div>
        <div class="menu-list">
          <router-link class="menu-item" to="/provider-config" active-class="active">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 2L2 7l10 5 10-5-10-5z"></path>
              <path d="M2 17l10 5 10-5"></path>
              <path d="M2 12l10 5 10-5"></path>
            </svg>
            <span class="text-sm">供应商配置</span>
          </router-link>
          <router-link class="menu-item" to="/model-config" active-class="active">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
            </svg>
            <span class="text-sm">模型配置</span>
          </router-link>
          <router-link class="menu-item" to="/knowledge-document" active-class="active">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
              <polyline points="14 2 14 8 20 8"></polyline>
              <line x1="16" y1="13" x2="8" y2="13"></line>
              <line x1="16" y1="17" x2="8" y2="17"></line>
              <polyline points="10 9 9 9 8 9"></polyline>
            </svg>
            <span class="text-sm">知识库</span>
          </router-link>
        </div>
      </div>

      <!-- Conversations -->
      <div class="conversations-section">
        <div class="section-label text-sm text-secondary">对话历史</div>
        
        <!-- Loading indicator for conversations list -->
        <div v-if="isLoadingConversations" class="loading-indicator">
          <div class="loading-spinner-small"></div>
          <span class="text-sm text-secondary">加载中...</span>
        </div>

        <div v-else>
          <div v-for="group in groupedConversations" :key="group.label" class="conversation-group">
            <div class="group-label text-xs text-tertiary">{{ group.label }}</div>
            <div class="conversation-list">
              <div
                v-for="conversation in group.conversations"
                :key="conversation.id"
                class="conversation-item"
                :class="{ 
                  active: currentConversation?.id === conversation.id,
                  loading: isLoadingMessages && currentConversation?.id === conversation.id
                }"
                @click="handleSelectConversation(conversation.id)"
              >
                <div class="conversation-title text-sm">{{ conversation.title }}</div>
                <!-- Loading indicator for messages -->
                <div v-if="isLoadingMessages && currentConversation?.id === conversation.id" class="message-loading-spinner"></div>
                <button class="delete-btn" @click.stop="deleteConversation(conversation.id)">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <line x1="18" y1="6" x2="6" y2="18"></line>
                    <line x1="6" y1="6" x2="18" y2="18"></line>
                  </svg>
                </button>
              </div>
            </div>
          </div>

          <div v-if="groupedConversations.length === 0" class="empty-state text-center text-secondary text-sm">
            <p>暂无对话记录</p>
          </div>
        </div>
      </div>
    </div>

    <div class="sidebar-footer">
      <div class="user-info">
        <div class="user-avatar">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
            <circle cx="12" cy="7" r="4"></circle>
          </svg>
        </div>
        <div class="user-details">
          <div class="user-email text-sm">{{ userEmail }}</div>
        </div>
        <button class="logout-btn" @click="handleLogout" title="退出登录">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
            <polyline points="16 17 21 12 16 7"></polyline>
            <line x1="21" y1="12" x2="9" y2="12"></line>
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useConversationStore } from '@/stores/conversation'
import { useUserStore } from '@/stores/user'
import { useAiRoleStore } from '@/stores/aiRole'

const router = useRouter()
const conversationStore = useConversationStore()
const userStore = useUserStore()
const aiRoleStore = useAiRoleStore()

const groupedConversations = computed(() => conversationStore.groupedConversations)
const currentConversation = computed(() => conversationStore.currentConversation)
const isLoadingConversations = computed(() => conversationStore.isLoadingConversations)
const isLoadingMessages = computed(() => conversationStore.isLoadingMessages)
const roles = computed(() => aiRoleStore.roles)

const userEmail = computed(() => {
  return userStore.userInfo?.email || '195******65@163.com'
})

const showRoleDropdown = ref(false)

// Load conversations and roles when component mounts
onMounted(async () => {
  await Promise.all([
    conversationStore.loadConversations(),
    aiRoleStore.loadRoles()
  ])

  // 自动选中第一个AI角色作为默认角色
  if (!conversationStore.selectedRoleId && aiRoleStore.roles.length > 0) {
    const firstRole = aiRoleStore.roles[0]
    conversationStore.setSelectedRole(firstRole.id, firstRole.roleName)
  }
})

const toggleRoleDropdown = () => {
  showRoleDropdown.value = !showRoleDropdown.value
}

const selectRole = (roleId: string, roleName: string) => {
  conversationStore.setSelectedRole(roleId, roleName)
  showRoleDropdown.value = false
}

// Close dropdown when clicking outside
const handleClickOutside = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  if (!target.closest('.role-dropdown')) {
    showRoleDropdown.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

const createNewConversation = () => {
  conversationStore.createConversation()
  // Navigate to home page if not already there
  if (router.currentRoute.value.path !== '/') {
    router.push('/')
  }
}

const handleSelectConversation = async (id: string) => {
  // Navigate to home page first
  if (router.currentRoute.value.path !== '/') {
    router.push('/')
  }
  // Then select the conversation (which will load messages)
  await conversationStore.selectConversation(id)
}

const deleteConversation = async (id: string) => {
  try {
    // 调用后端API删除会话
    await conversationStore.deleteConversationFromBackend(id)
  } catch (error) {
    console.error('删除会话失败:', error)
    // 可以在这里添加错误提示，比如使用toast或alert
    alert('删除会话失败，请稍后重试')
  }
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.sidebar {
  width: var(--sidebar-width);
  height: 100vh;
  background-color: var(--color-background);
  border-right: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0;
  top: 0;
}

.sidebar-header {
  padding: var(--spacing-lg);
  border-bottom: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

/* Role Selector */
.role-selector {
  width: 100%;
}

.role-selector-label {
  margin-bottom: 4px;
  padding-left: 2px;
}

.role-dropdown {
  position: relative;
  cursor: pointer;
}

.role-selected {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background-color: var(--color-background-soft);
  transition: all 0.2s ease-out;
}

.role-selected:hover {
  border-color: var(--color-primary);
  background-color: var(--color-primary-light);
}

.role-selected svg {
  color: var(--color-text-secondary);
  flex-shrink: 0;
}

.role-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--color-text);
}

.dropdown-arrow {
  transition: transform 0.2s ease-out;
}

.role-dropdown-menu {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  background-color: #ffffff;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  z-index: 100;
  max-height: 200px;
  overflow-y: auto;
}

.role-option {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  cursor: pointer;
  transition: background-color 0.2s ease-out;
  font-size: var(--font-size-sm);
}

.role-option:hover {
  background-color: var(--color-background-soft);
}

.role-option.active {
  background-color: var(--color-primary-light);
  color: var(--color-primary);
}

.role-option.disabled {
  cursor: default;
  opacity: 0.6;
}

.role-option svg {
  flex-shrink: 0;
  color: var(--color-text-secondary);
}

.role-option.active svg {
  color: var(--color-primary);
}

.new-chat-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md);
  border-radius: var(--radius-md);
  font-weight: 500;
}

.sidebar-content {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-md);
}

/* Configuration Menu */
.config-menu {
  margin-bottom: var(--spacing-lg);
}

.menu-label {
  padding: var(--spacing-sm) var(--spacing-md);
  font-weight: 500;
  color: var(--color-text-secondary);
}

.menu-list {
  margin-top: var(--spacing-xs);
}

.menu-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background-color var(--transition-fast);
  margin-bottom: var(--spacing-xs);
  color: var(--color-text);
  text-decoration: none;
}

.menu-item:hover {
  background-color: var(--color-background-soft);
}

.menu-item.active {
  background-color: var(--color-primary-light);
  color: var(--color-primary);
}

.menu-item svg {
  color: var(--color-text-secondary);
}

.menu-item.active svg {
  color: var(--color-primary);
}

/* Conversations Section */
.conversations-section {
  margin-top: var(--spacing-lg);
}

.section-label {
  padding: var(--spacing-sm) var(--spacing-md);
  font-weight: 500;
  color: var(--color-text-secondary);
}

/* Loading indicator */
.loading-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-xl);
}

.loading-spinner-small {
  width: 16px;
  height: 16px;
  border: 2px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.message-loading-spinner {
  width: 12px;
  height: 12px;
  border: 2px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-right: var(--spacing-xs);
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.conversation-group {
  margin-bottom: var(--spacing-lg);
}

.group-label {
  padding: var(--spacing-xs) var(--spacing-md);
  color: var(--color-text-tertiary);
}

.conversation-list {
  margin-top: var(--spacing-xs);
}

.conversation-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background-color var(--transition-fast);
  margin-bottom: var(--spacing-xs);
  position: relative;
}

.conversation-item:hover {
  background-color: var(--color-background-soft);
}

.conversation-item.active {
  background-color: var(--color-primary-light);
  border-left: 3px solid var(--color-primary);
}

.conversation-item.loading {
  opacity: 0.8;
}

.conversation-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--color-text);
}

.conversation-item.active .conversation-title {
  color: var(--color-primary);
  font-weight: 500;
}

.delete-btn {
  opacity: 0;
  padding: var(--spacing-xs);
  border-radius: var(--radius-sm);
  transition: opacity var(--transition-fast), background-color var(--transition-fast);
}

.conversation-item:hover .delete-btn {
  opacity: 1;
}

.delete-btn:hover {
  background-color: var(--color-background-mute);
}

.empty-state {
  padding: var(--spacing-xl);
  color: var(--color-text-tertiary);
}

.sidebar-footer {
  padding: var(--spacing-lg);
  border-top: 1px solid var(--color-border);
}

.user-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: var(--color-background-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-secondary);
}

.user-details {
  flex: 1;
}

.user-email {
  color: var(--color-text);
  font-weight: 500;
}

.logout-btn {
  padding: var(--spacing-sm);
  border-radius: var(--radius-sm);
  color: var(--color-text-secondary);
  transition: background-color var(--transition-fast), color var(--transition-fast);
}

.logout-btn:hover {
  background-color: var(--color-background-soft);
  color: var(--color-text);
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .sidebar {
    width: 60px;
  }
  
  .sidebar-header {
    padding: var(--spacing-sm);
  }
  
  .new-chat-btn span {
    display: none;
  }
  
  .menu-item span,
  .section-label,
  .group-label,
  .conversation-title,
  .user-email {
    display: none;
  }
  
  .sidebar-content {
    padding: var(--spacing-sm);
  }
  
  .sidebar-footer {
    padding: var(--spacing-sm);
  }
  
  .user-details {
    display: none;
  }
}
</style>