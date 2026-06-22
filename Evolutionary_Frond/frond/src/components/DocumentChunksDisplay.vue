<template>
  <div class="document-chunks-container">
    <div v-if="chunks && chunks.length > 0" class="chunks-section">
      <div class="chunks-header" @click="toggleExpand">
        <div class="header-left">
          <svg class="document-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
            <polyline points="14 2 14 8 20 8"></polyline>
            <line x1="16" y1="13" x2="8" y2="13"></line>
            <line x1="16" y1="17" x2="8" y2="17"></line>
            <polyline points="10 9 9 9 8 9"></polyline>
          </svg>
          <span class="header-title">知识库引用 ({{ chunks.length }})</span>
        </div>
        <svg
          class="expand-icon"
          :class="{ expanded: isExpanded }"
          width="16"
          height="16"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
        >
          <polyline points="6 9 12 15 18 9"></polyline>
        </svg>
      </div>

      <transition name="slide">
        <div v-if="isExpanded" class="chunks-list">
          <div
            v-for="(chunk, index) in chunks"
            :key="chunk.chunkId || index"
            class="chunk-item"
          >
            <div class="chunk-header" @click="toggleChunk(index)">
              <div class="chunk-info">
                <span class="chunk-index">#{{ index + 1 }}</span>
                <span class="chunk-source">{{ chunk.documentName || '未知文档' }}</span>
              </div>
              <svg
                class="chunk-expand-icon"
                :class="{ expanded: expandedChunks.includes(index) }"
                width="14"
                height="14"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <polyline points="6 9 12 15 18 9"></polyline>
              </svg>
            </div>

            <transition name="slide">
              <div v-if="expandedChunks.includes(index)" class="chunk-content">
                <div class="content-text">{{ chunk.content }}</div>
                <div class="chunk-meta">
                  <span v-if="chunk.chunkIndex" class="chunk-position">
                    文档位置: 第 {{ chunk.chunkIndex }} 段
                  </span>
                  <span v-if="chunk.similarityScore" class="similarity-score">
                    相似度: {{ (chunk.similarityScore * 100).toFixed(1) }}%
                  </span>
                </div>
              </div>
            </transition>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, defineProps, withDefaults } from 'vue'

interface DocumentChunk {
  chunkId?: string
  content: string
  documentId?: number
  documentName?: string
  chunkIndex?: number
  similarityScore?: number
  summary?: string
}

interface Props {
  chunks?: DocumentChunk[]
}

withDefaults(defineProps<Props>(), {
  chunks: () => [],
})

const isExpanded = ref(false)
const expandedChunks = ref<number[]>([])

const toggleExpand = () => {
  isExpanded.value = !isExpanded.value
}

const toggleChunk = (index: number) => {
  const idx = expandedChunks.value.indexOf(index)
  if (idx > -1) {
    expandedChunks.value.splice(idx, 1)
  } else {
    expandedChunks.value.push(index)
  }
}
</script>

<style scoped>
.document-chunks-container {
  margin-top: var(--spacing-lg);
  width: 100%;
}

.chunks-section {
  border: 2px solid var(--color-primary-light);
  border-radius: var(--radius-md);
  background-color: #f8f9fa;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.chunks-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md) var(--spacing-lg);
  background-color: var(--color-primary-light);
  cursor: pointer;
  transition: background-color var(--transition-fast);
  border-bottom: 1px solid var(--color-primary);
}

.chunks-header:hover {
  background-color: var(--color-primary-lighter);
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.document-icon {
  color: var(--color-primary);
  flex-shrink: 0;
}

.header-title {
  font-size: var(--font-size-base);
  font-weight: 600;
  color: var(--color-text);
}

.expand-icon {
  color: var(--color-text-secondary);
  transition: transform var(--transition-fast);
  flex-shrink: 0;
}

.expand-icon.expanded {
  transform: rotate(180deg);
}

.chunks-list {
  padding: var(--spacing-md);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.chunk-item {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background-color: #ffffff;
  overflow: hidden;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.chunk-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-sm) var(--spacing-md);
  background-color: #f1f3f5;
  cursor: pointer;
  transition: background-color var(--transition-fast);
  border-bottom: 1px solid var(--color-border);
}

.chunk-header:hover {
  background-color: #e9ecef;
}

.chunk-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.chunk-index {
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: var(--color-primary);
  background-color: var(--color-primary-light);
  padding: 4px 8px;
  border-radius: var(--radius-sm);
  flex-shrink: 0;
}

.chunk-source {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  font-weight: 500;
}

.chunk-expand-icon {
  color: var(--color-text-tertiary);
  transition: transform var(--transition-fast);
  flex-shrink: 0;
}

.chunk-expand-icon.expanded {
  transform: rotate(180deg);
}

.chunk-content {
  padding: var(--spacing-md);
  background-color: #ffffff;
}

.content-text {
  font-size: var(--font-size-sm);
  line-height: 1.8;
  color: var(--color-text);
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 300px;
  overflow-y: auto;
  padding: var(--spacing-sm);
  background-color: #f8f9fa;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
}

.chunk-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: var(--spacing-sm);
  padding-top: var(--spacing-sm);
  border-top: 1px solid var(--color-border);
}

.chunk-position {
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
  font-weight: 500;
}

.similarity-score {
  font-size: var(--font-size-xs);
  color: var(--color-primary);
  font-weight: 600;
  background-color: var(--color-primary-light);
  padding: 2px 8px;
  border-radius: var(--radius-sm);
}

/* 过渡动画 */
.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
  max-height: 1000px;
}

.slide-enter-from,
.slide-leave-to {
  max-height: 0;
  opacity: 0;
  overflow: hidden;
}

/* 滚动条样式 */
.content-text::-webkit-scrollbar {
  width: 6px;
}

.content-text::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.content-text::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.content-text::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
