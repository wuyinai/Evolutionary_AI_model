<template>
  <div class="chat-sidebar">
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
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useConversationStore } from '@/stores/conversation'

const router = useRouter()
const conversationStore = useConversationStore()

const groupedConversations = computed(() => conversationStore.groupedConversations)
const currentConversation = computed(() => conversationStore.currentConversation)
const isLoadingConversations = computed(() => conversationStore.isLoadingConversations)
const isLoadingMessages = computed(() => conversationStore.isLoadingMessages)

// Load conversations when component mounts
onMounted(async () => {
  await conversationStore.loadConversations()
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