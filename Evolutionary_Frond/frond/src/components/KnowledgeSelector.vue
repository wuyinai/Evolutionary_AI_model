<template>
  <div class="knowledge-selector">
    <div class="selector-header" @click="toggleDropdown">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path>
        <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path>
      </svg>
      <span class="selector-title">知识库</span>
      <span v-if="selectedDocuments.length > 0" class="selected-count">
        {{ selectedDocuments.length }}
      </span>
      <svg class="dropdown-arrow" :class="{ rotated: showDropdown }" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <polyline points="6 9 12 15 18 9"></polyline>
      </svg>
    </div>

    <div v-if="showDropdown" class="dropdown-menu">
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <span>加载中...</span>
      </div>

      <div v-else-if="documents.length === 0" class="empty-state">
        <span>暂无可用的知识库</span>
        <router-link to="/knowledge-document" class="upload-link">去上传文档</router-link>
      </div>

      <div v-else class="document-list">
        <div
          v-for="doc in documents"
          :key="doc.id"
          class="document-item"
          :class="{ selected: isSelected(doc.id) }"
          @click="toggleDocument(doc)"
        >
          <div class="document-checkbox">
            <svg v-if="isSelected(doc.id)" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
              <polyline points="20 6 9 17 4 12"></polyline>
            </svg>
          </div>
          <div class="document-info">
            <div class="document-name">{{ doc.documentName }}</div>
            <div class="document-meta">
              <span class="file-type">{{ doc.fileType?.toUpperCase() }}</span>
              <span class="chunk-count">{{ doc.chunkCount }} 个分块</span>
            </div>
          </div>
        </div>
      </div>

      <div v-if="documents.length > 0" class="dropdown-footer">
        <button class="clear-btn" @click="clearSelection" :disabled="selectedDocuments.length === 0">
          清空选择
        </button>
        <button class="confirm-btn" @click="confirmSelection">
          确定
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'

interface KnowledgeDocument {
  id: string
  documentName: string
  fileType: string
  chunkCount: number
  status: string
}

const props = defineProps<{
  disabled?: boolean
}>()

const emit = defineEmits<{
  change: [documentIds: string[]]
}>()

const showDropdown = ref(false)
const loading = ref(false)
const documents = ref<KnowledgeDocument[]>([])
const selectedDocuments = ref<KnowledgeDocument[]>([])

const toggleDropdown = () => {
  if (!props.disabled) {
    showDropdown.value = !showDropdown.value
  }
}

const isSelected = (docId: string) => {
  return selectedDocuments.value.some(doc => doc.id === docId)
}

const toggleDocument = (doc: KnowledgeDocument) => {
  const index = selectedDocuments.value.findIndex(d => d.id === doc.id)
  if (index > -1) {
    selectedDocuments.value.splice(index, 1)
  } else {
    selectedDocuments.value.push(doc)
  }
}

const clearSelection = () => {
  selectedDocuments.value = []
}

const confirmSelection = () => {
  showDropdown.value = false
  emit('change', selectedDocuments.value.map(doc => doc.id))
}

const loadDocuments = async () => {
  loading.value = true
  try {
    const response = await request.get('/knowledge/document/list')
    if (response.code === 200) {
      // 只显示已完成的文档
      documents.value = response.data.filter((doc: KnowledgeDocument) => doc.status === 'COMPLETED')
    }
  } catch (error) {
    console.error('加载知识库列表失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDocuments()
})
</script>

<style scoped>
.knowledge-selector {
  position: relative;
  width: 100%;
}

.selector-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  background-color: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.2s ease-out;
}

.selector-header:hover {
  border-color: var(--color-primary);
}

.selector-title {
  flex: 1;
  font-size: var(--font-size-sm);
  color: var(--color-text);
}

.selected-count {
  background-color: var(--color-primary);
  color: white;
  font-size: var(--font-size-xs);
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 18px;
  text-align: center;
}

.dropdown-arrow {
  transition: transform 0.2s ease-out;
}

.dropdown-arrow.rotated {
  transform: rotate(180deg);
}

.dropdown-menu {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  background-color: white;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  z-index: 100;
  max-height: 300px;
  display: flex;
  flex-direction: column;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-xl);
  gap: var(--spacing-sm);
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.upload-link {
  color: var(--color-primary);
  text-decoration: none;
  margin-top: var(--spacing-sm);
}

.upload-link:hover {
  text-decoration: underline;
}

.document-list {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-sm);
}

.document-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background-color 0.2s ease-out;
}

.document-item:hover {
  background-color: var(--color-background-soft);
}

.document-item.selected {
  background-color: var(--color-primary-light);
}

.document-checkbox {
  width: 18px;
  height: 18px;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease-out;
}

.document-item.selected .document-checkbox {
  border-color: var(--color-primary);
  background-color: var(--color-primary);
  color: white;
}

.document-info {
  flex: 1;
  min-width: 0;
}

.document-name {
  font-size: var(--font-size-sm);
  color: var(--color-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.document-meta {
  display: flex;
  gap: var(--spacing-sm);
  margin-top: 2px;
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
}

.file-type {
  background-color: var(--color-background-soft);
  padding: 1px 4px;
  border-radius: 2px;
}

.dropdown-footer {
  display: flex;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm);
  border-top: 1px solid var(--color-border);
}

.clear-btn,
.confirm-btn {
  flex: 1;
  padding: var(--spacing-sm);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-sm);
  cursor: pointer;
  transition: all 0.2s ease-out;
}

.clear-btn {
  border: 1px solid var(--color-border);
  background-color: white;
  color: var(--color-text-secondary);
}

.clear-btn:hover:not(:disabled) {
  background-color: var(--color-background-soft);
}

.clear-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.confirm-btn {
  border: none;
  background-color: var(--color-primary);
  color: white;
}

.confirm-btn:hover {
  background-color: var(--color-primary-dark);
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid var(--color-border);
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
