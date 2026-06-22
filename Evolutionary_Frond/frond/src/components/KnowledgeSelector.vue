<template>
  <div class="knowledge-selector">
    <div class="selector-header" @click="toggleDropdown">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path>
        <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path>
      </svg>
      <span class="selector-title">知识库</span>
      <span v-if="totalSelectedCount > 0" class="selected-count">
        {{ totalSelectedCount }}
      </span>
      <svg class="dropdown-arrow" :class="{ rotated: showDropdown }" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <polyline points="6 9 12 15 18 9"></polyline>
      </svg>
    </div>

    <div v-if="showDropdown" class="dropdown-menu">
      <!-- Tabs -->
      <div class="tabs">
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'document' }"
          @click="activeTab = 'document'"
        >
          文档挂载
          <span v-if="selectedDocuments.length > 0" class="tab-count">{{ selectedDocuments.length }}</span>
        </button>
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'knowledgeBase' }"
          @click="activeTab = 'knowledgeBase'"
        >
          知识库挂载
          <span v-if="selectedKnowledgeBases.length > 0" class="tab-count">{{ selectedKnowledgeBases.length }}</span>
        </button>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <span>加载中...</span>
      </div>

      <!-- Document Tab -->
      <div v-else-if="activeTab === 'document'" class="tab-content">
        <div v-if="documents.length === 0" class="empty-state">
          <span>暂无可用的文档</span>
          <router-link to="/knowledge-document" class="upload-link">去上传文档</router-link>
        </div>
        <div v-else class="item-list">
          <div
            v-for="doc in documents"
            :key="doc.id"
            class="item-row"
            :class="{ selected: isDocumentSelected(doc.id) }"
            @click="toggleDocument(doc)"
          >
            <div class="item-checkbox">
              <svg v-if="isDocumentSelected(doc.id)" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
            </div>
            <div class="item-info">
              <div class="item-name">{{ doc.documentName }}</div>
              <div class="item-meta">
                <span class="meta-tag">{{ doc.fileType?.toUpperCase() }}</span>
                <span class="meta-text">{{ doc.chunkCount }} 个分块</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Knowledge Base Tab -->
      <div v-else-if="activeTab === 'knowledgeBase'" class="tab-content">
        <div v-if="knowledgeBases.length === 0" class="empty-state">
          <span>暂无知识库</span>
          <router-link to="/knowledge-base" class="upload-link">去创建知识库</router-link>
        </div>
        <div v-else class="item-list">
          <div
            v-for="kb in knowledgeBases"
            :key="kb.id"
            class="item-row"
            :class="{ selected: isKnowledgeBaseSelected(kb.id) }"
            @click="toggleKnowledgeBase(kb)"
          >
            <div class="item-checkbox">
              <svg v-if="isKnowledgeBaseSelected(kb.id)" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
            </div>
            <div class="item-info">
              <div class="item-name">{{ kb.name }}</div>
              <div class="item-meta">
                <span class="meta-text">{{ kb.documentCount }} 个文档</span>
                <span class="meta-text">{{ kb.chunkCount }} 个分块</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Footer -->
      <div v-if="!loading && (documents.length > 0 || knowledgeBases.length > 0)" class="dropdown-footer">
        <button class="clear-btn" @click="clearSelection" :disabled="totalSelectedCount === 0">
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
import { ref, computed, onMounted, watch } from 'vue'
import request from '@/utils/request'

interface KnowledgeDocument {
  id: string
  documentName: string
  fileType: string
  chunkCount: number
  status: string
}

interface KnowledgeBase {
  id: string
  name: string
  documentCount: number
  chunkCount: number
  status: string
}

export interface KnowledgeSelection {
  documentIds: string[]
  knowledgeBaseIds: string[]
}

const props = defineProps<{
  disabled?: boolean
}>()

const emit = defineEmits<{
  change: [selection: KnowledgeSelection]
}>()

const showDropdown = ref(false)
const loading = ref(false)
const activeTab = ref<'document' | 'knowledgeBase'>('document')

const documents = ref<KnowledgeDocument[]>([])
const knowledgeBases = ref<KnowledgeBase[]>([])
const selectedDocuments = ref<KnowledgeDocument[]>([])
const selectedKnowledgeBases = ref<KnowledgeBase[]>([])

const totalSelectedCount = computed(() => selectedDocuments.value.length + selectedKnowledgeBases.value.length)

const toggleDropdown = () => {
  if (!props.disabled) {
    showDropdown.value = !showDropdown.value
  }
}

// Document tab
const isDocumentSelected = (docId: string) => {
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

// Knowledge base tab
const isKnowledgeBaseSelected = (kbId: string) => {
  return selectedKnowledgeBases.value.some(kb => kb.id === kbId)
}

const toggleKnowledgeBase = (kb: KnowledgeBase) => {
  const index = selectedKnowledgeBases.value.findIndex(k => k.id === kb.id)
  if (index > -1) {
    selectedKnowledgeBases.value.splice(index, 1)
  } else {
    selectedKnowledgeBases.value.push(kb)
  }
}

const clearSelection = () => {
  selectedDocuments.value = []
  selectedKnowledgeBases.value = []
}

const confirmSelection = () => {
  showDropdown.value = false
  emit('change', {
    documentIds: selectedDocuments.value.map(doc => doc.id),
    knowledgeBaseIds: selectedKnowledgeBases.value.map(kb => kb.id),
  })
}

const loadDocuments = async () => {
  loading.value = true
  try {
    const response = await request.get('/knowledge/document/standalone')
    if (response.code === 200) {
      documents.value = response.data.filter((doc: KnowledgeDocument) => doc.status === 'COMPLETED')
    }
  } catch (error) {
    console.error('加载文档列表失败:', error)
  } finally {
    loading.value = false
  }
}

const loadKnowledgeBases = async () => {
  loading.value = true
  try {
    const response = await request.get('/knowledge/base/list')
    if (response.code === 200) {
      knowledgeBases.value = (response.data || []).filter((kb: KnowledgeBase) => kb.status === 'ACTIVE')
    }
  } catch (error) {
    console.error('加载知识库列表失败:', error)
  } finally {
    loading.value = false
  }
}

// Switch tab and load corresponding data
watch(activeTab, (tab) => {
  if (tab === 'document' && documents.value.length === 0) {
    loadDocuments()
  } else if (tab === 'knowledgeBase' && knowledgeBases.value.length === 0) {
    loadKnowledgeBases()
  }
})

onMounted(() => {
  loadDocuments()
  loadKnowledgeBases()
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
  max-height: 350px;
  display: flex;
  flex-direction: column;
}

.tabs {
  display: flex;
  border-bottom: 1px solid var(--color-border);
  flex-shrink: 0;
}

.tab-btn {
  flex: 1;
  padding: var(--spacing-sm) var(--spacing-md);
  border: none;
  background: none;
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s ease-out;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.tab-btn:hover {
  color: var(--color-text);
}

.tab-btn.active {
  color: var(--color-primary);
  border-bottom-color: var(--color-primary);
}

.tab-count {
  background-color: var(--color-primary-light);
  color: var(--color-primary);
  font-size: var(--font-size-xs);
  padding: 0px 5px;
  border-radius: 8px;
}

.tab-content {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
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

.item-list {
  padding: var(--spacing-sm);
}

.item-row {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background-color 0.2s ease-out;
}

.item-row:hover {
  background-color: var(--color-background-soft);
}

.item-row.selected {
  background-color: var(--color-primary-light);
}

.item-checkbox {
  width: 18px;
  height: 18px;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.2s ease-out;
}

.item-row.selected .item-checkbox {
  border-color: var(--color-primary);
  background-color: var(--color-primary);
  color: white;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: var(--font-size-sm);
  color: var(--color-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-meta {
  display: flex;
  gap: var(--spacing-sm);
  margin-top: 2px;
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
}

.meta-tag {
  background-color: var(--color-background-soft);
  padding: 1px 4px;
  border-radius: 2px;
}

.meta-text {
  color: var(--color-text-tertiary);
}

.dropdown-footer {
  display: flex;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm);
  border-top: 1px solid var(--color-border);
  flex-shrink: 0;
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
