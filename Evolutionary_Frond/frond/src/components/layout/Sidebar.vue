<template>
  <div class="sidebar">
    <div class="sidebar-header">
      <button class="btn btn-secondary btn-full new-chat-btn" @click="createNewConversation">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="12" y1="5" x2="12" y2="19"></line>
          <line x1="5" y1="12" x2="19" y2="12"></line>
        </svg>
        <span>开启新对话</span>
      </button>
    </div>

    <div class="sidebar-content">
      <div v-for="group in groupedConversations" :key="group.label" class="conversation-group">
        <div class="group-label text-sm text-secondary">{{ group.label }}</div>
        <div class="conversation-list">
          <div
            v-for="conversation in group.conversations"
            :key="conversation.id"
            class="conversation-item"
            :class="{ active: currentConversation?.id === conversation.id }"
            @click="selectConversation(conversation.id)"
          >
            <div class="conversation-title text-sm">{{ conversation.title }}</div>
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
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useConversationStore } from '@/stores/conversation'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const conversationStore = useConversationStore()
const userStore = useUserStore()

const groupedConversations = computed(() => conversationStore.groupedConversations)
const currentConversation = computed(() => conversationStore.currentConversation)

const userEmail = computed(() => {
  return userStore.userInfo?.email || '195******65@163.com'
})

const createNewConversation = () => {
  conversationStore.createConversation()
}

const selectConversation = (id: string) => {
  conversationStore.selectConversation(id)
}

const deleteConversation = (id: string) => {
  conversationStore.deleteConversation(id)
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

.conversation-group {
  margin-bottom: var(--spacing-lg);
}

.group-label {
  padding: var(--spacing-sm) var(--spacing-md);
  font-weight: 500;
  color: var(--color-text-secondary);
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
}

.conversation-item:hover {
  background-color: var(--color-background-soft);
}

.conversation-item.active {
  background-color: var(--color-primary-light);
}

.conversation-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--color-text);
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
</style>
