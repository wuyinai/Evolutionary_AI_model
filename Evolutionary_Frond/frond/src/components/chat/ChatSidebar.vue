<template>
  <div class="chat-sidebar">
    <div class="sidebar-header">
      <!-- AI角色选择器 -->
      <div class="role-selector">
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
              <span class="text-secondary">暂无角色，请先在"角色专家"中创建</span>
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
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useConversationStore } from '@/stores/conversation'
import { useAiRoleStore } from '@/stores/aiRole'

const router = useRouter()
const conversationStore = useConversationStore()
const aiRoleStore = useAiRoleStore()

const groupedConversations = computed(() => conversationStore.groupedConversations)
const currentConversation = computed(() => conversationStore.currentConversation)
const isLoadingConversations = computed(() => conversationStore.isLoadingConversations)
const isLoadingMessages = computed(() => conversationStore.isLoadingMessages)
const roles = computed(() => aiRoleStore.roles)

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
}

const handleSelectConversation = async (id: string) => {
  await conversationStore.selectConversation(id)
}

const deleteConversation = async (id: string) => {
  try {
    await conversationStore.deleteConversationFromBackend(id)
  } catch (error) {
    console.error('删除会话失败:', error)
    alert('删除会话失败，请稍后重试')
  }
}
</script>

<style scoped>
.chat-sidebar {
  width: 280px;
  height: 100vh;
  background-color: #ffffff;
  border-right: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  position: relative;
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
  transition: all 0.2s ease-out;
}

.new-chat-btn:hover {
  background-color: var(--color-primary-light);
  color: var(--color-primary);
}

.sidebar-content {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-md);
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
  transition: all 0.2s ease-out;
  margin-bottom: var(--spacing-xs);
  position: relative;
}

.conversation-item:hover {
  background-color: var(--color-background-soft);
}

.conversation-item.active {
  background-color: var(--color-primary-light);
  border-left: 3px solid var(--color-primary);
  padding-left: calc(var(--spacing-md) - 3px);
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
  transition: all 0.2s ease-out;
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

/* Responsive adjustments */
@media (max-width: 768px) {
  .chat-sidebar {
    width: 240px;
  }

  .sidebar-header {
    padding: var(--spacing-sm);
  }

  .sidebar-content {
    padding: var(--spacing-sm);
  }
}

@media (max-width: 480px) {
  .chat-sidebar {
    width: 100%;
    position: fixed;
    top: 0;
    left: 0;
    z-index: 50;
    transform: translateX(-100%);
  }

  .chat-sidebar.open {
    transform: translateX(0);
  }
}
</style>
