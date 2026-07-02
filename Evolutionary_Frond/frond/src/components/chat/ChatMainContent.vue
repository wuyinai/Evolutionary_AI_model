<template>
  <div class="chat-main-content">
    <!-- 消息区域（可滚动） -->
    <div class="messages-area">
      <div class="content-header">
        <div class="title-section">
          <svg class="title-icon" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"></polygon>
          </svg>
          <h1 class="main-title text-3xl font-bold">
            开始对话
          </h1>
        </div>
      </div>

      <div class="messages-container" ref="messagesContainer">
        <!-- Loading indicator for messages -->
        <div v-if="isLoadingMessages" class="loading-overlay">
          <div class="loading-spinner"></div>
          <span class="text-secondary text-sm">加载消息历史...</span>
        </div>

        <div v-if="messages.length === 0 && !isLoadingMessages" class="empty-state">
          <p class="text-secondary text-lg">开始您的对话吧！</p>
        </div>
        <div v-else-if="messages.length > 0" class="messages-list">
          <div
            v-for="message in messages"
            :key="message.id"
            class="message-item"
            :class="message.role"
          >
            <div class="message-content" v-html="renderMarkdown(message.content)"></div>
            <!-- 流式输出时显示闪烁的光标 -->
            <span v-if="message.isStreaming" class="streaming-cursor">▊</span>
            <!-- 文档块展示组件 -->
            <DocumentChunksDisplay
              v-if="message.role === 'assistant' && message.documentChunks && message.documentChunks.length > 0"
              :chunks="message.documentChunks"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 输入框区域（固定在底部） -->
    <div class="input-section-fixed">
      <div class="input-wrapper">
        <!-- 模型选择器 -->
        <ModelSelector :disabled="isLoading" @change="onModelChange" />

        <!-- 知识库选择器 -->
        <KnowledgeSelector :disabled="isLoading" @change="onKnowledgeChange" />

        <!-- 钉选模型显示 -->
        <div v-if="pinnedModelInfo" class="pinned-model-info">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"></path>
          </svg>
          <span class="pinned-text">钉选: {{ pinnedModelInfo.configName }}</span>
          <button class="unpin-btn" @click="unpinModel" title="取消钉选">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>

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
import { ref, computed, nextTick, watch } from 'vue'
import { useConversationStore } from '@/stores/conversation'
import { useModelConfigStore } from '@/stores/modelConfig'
import { streamChat, type DocumentChunk } from '@/utils/chat'
import ModelSelector from '@/components/ModelSelector.vue'
import KnowledgeSelector, { type KnowledgeSelection } from '@/components/KnowledgeSelector.vue'
import DocumentChunksDisplay from '@/components/DocumentChunksDisplay.vue'
import type { ChatMessageDTO } from '@/types/conversation'
import { marked } from 'marked'

// 配置marked选项
marked.setOptions({
  breaks: true, // GitHub风格换行
  gfm: true,    // 启用GitHub风格Markdown
})

// 渲染Markdown为HTML
const renderMarkdown = (content: string): string => {
  if (!content) return ''
  try {
    return marked.parse(content) as string
  } catch (e) {
    console.error('Markdown渲染失败:', e)
    return content
  }
}

const conversationStore = useConversationStore()
const modelConfigStore = useModelConfigStore()

const messages = computed(() => conversationStore.currentConversation?.messages || [])
const isLoadingMessages = computed(() => conversationStore.isLoadingMessages)

const inputMessage = ref('')
const isLoading = ref(false)
const messagesContainer = ref<HTMLElement | null>(null)

// 当前正在流式输出的消息ID
const streamingMessageId = ref<string | null>(null)

// 当前选中的模型配置ID
const currentConfigId = ref<string | null>(null)

// 钉选的模型信息
const pinnedModelInfo = computed(() => {
  const pinnedConfigId = conversationStore.getPinnedConfigId()
  if (pinnedConfigId) {
    return modelConfigStore.modelConfigs.find((config) => config.id === pinnedConfigId)
  }
  return null
})

// Watch for messages loading completion and scroll to bottom
watch(isLoadingMessages, async (loading) => {
  if (!loading && messages.value.length > 0) {
    await scrollToBottom()
  }
})

// Watch for current conversation changes and scroll to bottom
watch(() => conversationStore.currentConversation?.id, async (newId, oldId) => {
  if (newId && newId !== oldId && messages.value.length > 0) {
    await scrollToBottom()
  }
})

// 模型切换处理
const onModelChange = (configId: string | null) => {
  currentConfigId.value = configId
}

// 当前选中的知识库文档ID列表（文档挂载）
const selectedKnowledgeIds = ref<string[]>([])
// 当前选中的知识库ID列表（知识库挂载）
const selectedKnowledgeBaseIds = ref<string[]>([])

// 知识库切换处理
const onKnowledgeChange = (selection: KnowledgeSelection) => {
  selectedKnowledgeIds.value = selection.documentIds
  selectedKnowledgeBaseIds.value = selection.knowledgeBaseIds
}

// 取消钉选
const unpinModel = () => {
  conversationStore.unpinModel()
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
    .filter((msg) => msg.id !== tempMessageId)
    .slice(-10)
    .map((msg) => ({
      role: msg.role,
      content: msg.content,
    }))

  // 流式内容累积
  let accumulatedContent = ''

  // 文档块信息
  let documentChunks: DocumentChunk[] = []

  try {
    const pinnedConfigId = conversationStore.getPinnedConfigId()
    const configId = pinnedConfigId || currentConfigId.value || modelConfigStore.currentModel?.id || null

    await streamChat(
      {
        conversationId: conversationStore.currentConversation?.id,
        message: userMessage,
        configId: configId || undefined,
        history,
        knowledgeDocumentIds: selectedKnowledgeIds.value.length > 0 ? selectedKnowledgeIds.value : undefined,
        knowledgeBaseIds: selectedKnowledgeBaseIds.value.length > 0 ? selectedKnowledgeBaseIds.value : undefined,
        ragTopK: 3,
        roleId: conversationStore.selectedRoleId || undefined,
      },
      (chunk: string) => {
        accumulatedContent += chunk
        if (streamingMessageId.value && conversationStore.currentConversation) {
          const msgIndex = conversationStore.currentConversation.messages.findIndex(
            (m) => m.id === streamingMessageId.value,
          )
          if (msgIndex !== -1 && conversationStore.currentConversation.messages[msgIndex]) {
            conversationStore.currentConversation.messages[msgIndex].content = accumulatedContent
          }
        }
        scrollToBottom()
      },
      (error: Error) => {
        console.error('流式对话错误:', error)
        if (streamingMessageId.value && conversationStore.currentConversation) {
          const msgIndex = conversationStore.currentConversation.messages.findIndex(
            (m) => m.id === streamingMessageId.value,
          )
          if (msgIndex !== -1 && conversationStore.currentConversation.messages[msgIndex]) {
            conversationStore.currentConversation.messages[msgIndex].content = `错误: ${error.message}`
            conversationStore.currentConversation.messages[msgIndex].isStreaming = false
          }
        }
        isLoading.value = false
        streamingMessageId.value = null
      },
      () => {
        if (streamingMessageId.value && conversationStore.currentConversation) {
          const msgIndex = conversationStore.currentConversation.messages.findIndex(
            (m) => m.id === streamingMessageId.value,
          )
          if (msgIndex !== -1 && conversationStore.currentConversation.messages[msgIndex]) {
            conversationStore.currentConversation.messages[msgIndex].isStreaming = false
            // 设置文档块信息
            if (documentChunks.length > 0) {
              conversationStore.currentConversation.messages[msgIndex].documentChunks = documentChunks
            }
          }
        }
        isLoading.value = false
        streamingMessageId.value = null
        scrollToBottom()
      },
      (chunks: DocumentChunk[]) => {
        // 处理文档块信息
        documentChunks = chunks
        console.log('接收到文档块信息:', chunks.length, '个')
      },
    )
  } catch (error) {
    console.error('发送消息失败:', error)
    if (streamingMessageId.value && conversationStore.currentConversation) {
      const msgIndex = conversationStore.currentConversation.messages.findIndex(
        (m) => m.id === streamingMessageId.value,
      )
      if (msgIndex !== -1 && conversationStore.currentConversation.messages[msgIndex]) {
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
.chat-main-content {
  flex: 1;
  margin-left: 280px;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #ffffff;
  position: relative;
}

/* 消息区域（可滚动） */
.messages-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  padding: var(--spacing-2xl);
  padding-bottom: 0; /* 为固定输入框留出空间 */
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

.messages-container {
  flex: 1;
  overflow-y: auto;
  max-width: 900px;
  margin: 0 auto;
  width: 100%;
  position: relative;
  padding-bottom: var(--spacing-lg);
}

/* Loading overlay */
.loading-overlay {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-md);
  padding: var(--spacing-xl);
  min-height: 200px;
}

.loading-spinner {
  width: 24px;
  height: 24px;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
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

/* Markdown 内容样式 */
.message-content :deep(h1),
.message-content :deep(h2),
.message-content :deep(h3),
.message-content :deep(h4) {
  margin-top: 1em;
  margin-bottom: 0.5em;
  font-weight: 600;
  color: var(--color-text);
}

.message-content :deep(h1) { font-size: 1.5em; }
.message-content :deep(h2) { font-size: 1.3em; }
.message-content :deep(h3) { font-size: 1.15em; }
.message-content :deep(h4) { font-size: 1.05em; }

.message-content :deep(p) {
  margin-bottom: 0.75em;
}

.message-content :deep(ul),
.message-content :deep(ol) {
  margin-bottom: 0.75em;
  padding-left: 1.5em;
}

.message-content :deep(li) {
  margin-bottom: 0.25em;
}

.message-content :deep(strong) {
  font-weight: 600;
}

.message-content :deep(code) {
  background-color: var(--color-background-soft);
  padding: 0.15em 0.4em;
  border-radius: 3px;
  font-size: 0.9em;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
}

.message-content :deep(pre) {
  background-color: var(--color-background-soft);
  padding: 1em;
  border-radius: var(--radius-md);
  overflow-x: auto;
  margin-bottom: 0.75em;
  border: 1px solid var(--color-border);
}

.message-content :deep(pre code) {
  background: none;
  padding: 0;
  border-radius: 0;
  font-size: 0.85em;
  line-height: 1.5;
}

.message-content :deep(blockquote) {
  border-left: 3px solid var(--color-primary);
  padding-left: 1em;
  margin-left: 0;
  margin-bottom: 0.75em;
  color: var(--color-text-secondary);
}

.message-content :deep(hr) {
  border: none;
  border-top: 1px solid var(--color-border);
  margin: 1em 0;
}

.message-content :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin-bottom: 0.75em;
  font-size: 0.9em;
}

.message-content :deep(th),
.message-content :deep(td) {
  border: 1px solid var(--color-border);
  padding: 0.5em 0.75em;
  text-align: left;
}

.message-content :deep(th) {
  background-color: var(--color-background-soft);
  font-weight: 600;
}

.message-content :deep(a) {
  color: var(--color-primary);
  text-decoration: underline;
}

.message-content :deep(img) {
  max-width: 100%;
  border-radius: var(--radius-md);
  margin: 0.5em 0;
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

/* 输入框区域（固定在底部） */
.input-section-fixed {
  position: sticky;
  bottom: 0;
  left: 280px;
  right: 0;
  background-color: #ffffff;
  border-top: 1px solid var(--color-border);
  padding: var(--spacing-lg) var(--spacing-2xl);
  z-index: 10;
}

.input-wrapper {
  max-width: 900px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  align-items: stretch;
}

/* Pinned Model Info */
.pinned-model-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  background-color: var(--color-primary-light);
  border: 1px solid var(--color-primary);
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  color: var(--color-primary);
}

.pinned-model-info svg {
  color: var(--color-warning);
}

.pinned-text {
  flex: 1;
}

.unpin-btn {
  padding: var(--spacing-xs);
  border: none;
  background: transparent;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: color var(--transition-fast);
}

.unpin-btn:hover {
  color: var(--color-text);
}

.input-container {
  flex: 1;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--spacing-md);
  background-color: #ffffff;
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
  transition: all 0.2s ease-out;
  cursor: pointer;
}

.action-btn:hover {
  background-color: var(--color-background-soft);
}

.send-btn {
  padding: var(--spacing-md);
  border-radius: var(--radius-md);
  min-width: 48px;
  height: 48px;
  transition: all 0.2s ease-out;
  cursor: pointer;
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .chat-main-content {
    margin-left: 240px;
  }
  
  .messages-area {
    padding: var(--spacing-md);
  }
  
  .input-section-fixed {
    left: 240px;
    padding: var(--spacing-md);
  }
  
  .message-item {
    max-width: 90%;
  }
  
  .input-actions span {
    display: none;
  }
}

@media (max-width: 480px) {
  .chat-main-content {
    margin-left: 0;
    width: 100%;
  }
  
  .messages-area {
    padding: var(--spacing-sm);
  }
  
  .input-section-fixed {
    left: 0;
    padding: var(--spacing-sm);
  }
  
  .message-item {
    max-width: 100%;
  }
}
</style>