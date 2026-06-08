<template>
  <div class="main-content">
    <div class="content-wrapper">
      <div class="content-header">
        <div class="title-section">
          <svg class="title-icon" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"></polygon>
          </svg>
          <h1 class="main-title text-3xl font-bold">
            {{ currentMode === 'quick' ? '使用快速模式开始对话' : '使用专家模式开始对话' }}
          </h1>
        </div>

        <div class="mode-toggle">
          <button
            class="mode-btn btn-capsule"
            :class="{ active: currentMode === 'quick' }"
            @click="toggleMode('quick')"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"></polygon>
            </svg>
            <span>快速模式</span>
          </button>
          <button
            class="mode-btn btn-capsule"
            :class="{ active: currentMode === 'expert' }"
            @click="toggleMode('expert')"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
            </svg>
            <span>专家模式</span>
          </button>
        </div>
      </div>

      <div class="messages-container" ref="messagesContainer">
        <div v-if="messages.length === 0" class="empty-state">
          <p class="text-secondary text-lg">开始您的对话吧！</p>
        </div>
        <div v-else class="messages-list">
          <div
            v-for="message in messages"
            :key="message.id"
            class="message-item"
            :class="message.role"
          >
            <div class="message-content">
              {{ message.content }}
              <!-- 流式输出时显示闪烁的光标 -->
              <span v-if="message.isStreaming" class="streaming-cursor">▊</span>
            </div>
          </div>
        </div>
      </div>

      <div class="input-section">
        <!-- 模型选择器 -->
        <ModelSelector :disabled="isLoading" @change="onModelChange" />

        <div class="input-container">
          <textarea
            v-model="inputMessage"
            class="message-input"
            placeholder="发送消息"
            rows="1"
            @keydown.enter.exact.prevent="sendMessage"
          ></textarea>
          <div class="input-actions">
            <button class="action-btn text-sm text-secondary">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
              </svg>
              <span>附件</span>
            </button>
            <button class="action-btn text-sm text-secondary">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"></circle>
                <path d="M8 14s1.5 2 4 2 4-2 4-2"></path>
                <line x1="9" y1="9" x2="9.01" y2="9"></line>
                <line x1="15" y1="9" x2="15.01" y2="9"></line>
              </svg>
              <span>表情</span>
            </button>
          </div>
        </div>
        <button class="send-btn btn btn-primary" @click="sendMessage" :disabled="!inputMessage.trim() || isLoading">
          <svg v-if="!isLoading" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="22" y1="2" x2="11" y2="13"></line>
            <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
          </svg>
          <span v-else class="loading-spinner"></span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick } from 'vue'
import { useConversationStore } from '@/stores/conversation'
import { useModelConfigStore } from '@/stores/modelConfig'
import { streamChat } from '@/utils/chat'
import ModelSelector from '@/components/ModelSelector.vue'
import type { ChatMessageDTO } from '@/types/conversation'

const conversationStore = useConversationStore()
const modelConfigStore = useModelConfigStore()

const currentMode = computed(() => conversationStore.currentMode)
const messages = computed(() => conversationStore.currentConversation?.messages || [])

const inputMessage = ref('')
const isLoading = ref(false)
const messagesContainer = ref<HTMLElement | null>(null)

// 当前正在流式输出的消息ID
const streamingMessageId = ref<string | null>(null)

// 当前选中的模型配置ID
const currentConfigId = ref<string | null>(null) // 使用string类型，避免JS精度丢失

const toggleMode = (mode: 'quick' | 'expert') => {
  conversationStore.toggleMode(mode)
}

// 模型切换处理
const handleModelChange = (configId: string | null) => { // 使用string类型
  currentConfigId.value = configId
}

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || isLoading.value) return

  const userMessage = inputMessage.value.trim()
  isLoading.value = true

  // 添加用户消息
  conversationStore.addMessage(userMessage, 'user')
  scrollToBottom()

  // 清空输入框
  inputMessage.value = ''

  // 创建一个临时的AI消息用于流式输出
  const tempMessageId = Date.now().toString(36) + Math.random().toString(36).substr(2)
  conversationStore.addMessage('', 'assistant', tempMessageId, true)
  streamingMessageId.value = tempMessageId
  scrollToBottom()

  // 构建历史消息（排除正在流式输出的消息）
  const history: ChatMessageDTO[] = messages.value
    .filter((msg) => msg.id !== tempMessageId) // 排除临时消息
    .slice(-10)
    .map((msg) => ({
      role: msg.role,
      content: msg.content,
    }))

  // 流式内容累积
  let accumulatedContent = ''

  try {
    // 使用流式对话API，传入configId
    // 获取当前选中的模型配置ID
    const configId = currentConfigId.value || modelConfigStore.currentModel?.id || null
    
    await streamChat(
      {
        conversationId: conversationStore.currentConversation?.id,
        message: userMessage,
        mode: currentMode.value,
        configId: configId,
        history,
      },
      // onMessage: 每次收到新内容块
      (chunk: string) => {
        accumulatedContent += chunk
        // 更新临时消息的内容
        if (streamingMessageId.value && conversationStore.currentConversation) {
          const msgIndex = conversationStore.currentConversation.messages.findIndex(
            (m) => m.id === streamingMessageId.value,
          )
          if (msgIndex !== -1) {
            conversationStore.currentConversation.messages[msgIndex].content = accumulatedContent
          }
        }
        scrollToBottom()
      },
      // onError: 发生错误
      (error: Error) => {
        console.error('流式对话错误:', error)
        // 更新临时消息为错误信息
        if (streamingMessageId.value && conversationStore.currentConversation) {
          const msgIndex = conversationStore.currentConversation.messages.findIndex(
            (m) => m.id === streamingMessageId.value,
          )
          if (msgIndex !== -1) {
            conversationStore.currentConversation.messages[msgIndex].content = `错误: ${error.message}`
            conversationStore.currentConversation.messages[msgIndex].isStreaming = false
          }
        }
        isLoading.value = false
        streamingMessageId.value = null
      },
      // onComplete: 流式完成
      () => {
        // 标记消息完成
        if (streamingMessageId.value && conversationStore.currentConversation) {
          const msgIndex = conversationStore.currentConversation.messages.findIndex(
            (m) => m.id === streamingMessageId.value,
          )
          if (msgIndex !== -1) {
            conversationStore.currentConversation.messages[msgIndex].isStreaming = false
          }
        }
        isLoading.value = false
        streamingMessageId.value = null
        scrollToBottom()
      },
    )
  } catch (error) {
    console.error('发送消息失败:', error)
    // 更新临时消息为错误信息
    if (streamingMessageId.value && conversationStore.currentConversation) {
      const msgIndex = conversationStore.currentConversation.messages.findIndex(
        (m) => m.id === streamingMessageId.value,
      )
      if (msgIndex !== -1) {
        conversationStore.currentConversation.messages[msgIndex].content = '网络错误，请稍后重试'
        conversationStore.currentConversation.messages[msgIndex].isStreaming = false
      }
    }
    isLoading.value = false
    streamingMessageId.value = null
  }
}
</script>

<style scoped>
.main-content {
  flex: 1;
  margin-left: var(--sidebar-width);
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--color-background);
}

.content-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  max-width: 900px;
  margin: 0 auto;
  width: 100%;
  padding: var(--spacing-2xl);
}

.content-header {
  text-align: center;
  margin-bottom: var(--spacing-2xl);
}

.title-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.title-icon {
  color: var(--color-primary);
}

.main-title {
  color: var(--color-text);
}

.mode-toggle {
  display: flex;
  gap: var(--spacing-sm);
  justify-content: center;
}

.mode-btn {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-lg);
  border: 1px solid var(--color-border);
  background-color: var(--color-background);
  color: var(--color-text-secondary);
  transition: all var(--transition-fast);
}

.mode-btn.active {
  background-color: var(--color-primary-light);
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.mode-btn:hover:not(.active) {
  background-color: var(--color-background-soft);
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  margin-bottom: var(--spacing-lg);
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.message-item {
  padding: var(--spacing-md);
  border-radius: var(--radius-md);
  max-width: 80%;
}

.message-item.user {
  background-color: var(--color-primary-light);
  margin-left: auto;
}

.message-item.assistant {
  background-color: var(--color-background-soft);
  margin-right: auto;
}

.message-content {
  color: var(--color-text);
  line-height: 1.6;
  word-break: break-word;
}

/* 流式输出时的闪烁光标 */
.streaming-cursor {
  display: inline-block;
  animation: blink 1s infinite;
  color: var(--color-primary);
  font-weight: bold;
}

@keyframes blink {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0;
  }
}

.input-section {
  display: flex;
  gap: var(--spacing-md);
  align-items: flex-end;
}

.input-container {
  flex: 1;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--spacing-md);
  background-color: var(--color-background);
}

.message-input {
  width: 100%;
  min-height: 24px;
  max-height: 120px;
  resize: none;
  font-size: var(--font-size-base);
  line-height: 1.6;
  color: var(--color-text);
}

.message-input::placeholder {
  color: var(--color-text-tertiary);
}

.input-actions {
  display: flex;
  gap: var(--spacing-md);
  margin-top: var(--spacing-sm);
}

.action-btn {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--radius-sm);
  transition: background-color var(--transition-fast);
}

.action-btn:hover {
  background-color: var(--color-background-soft);
}

.send-btn {
  padding: var(--spacing-md);
  border-radius: var(--radius-md);
  min-width: 48px;
  height: 48px;
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid var(--color-background);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>