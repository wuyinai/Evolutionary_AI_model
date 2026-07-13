<template>
  <div class="knowledge-base-management">
    <!-- Header Section -->
    <div class="page-header">
      <div class="header-content">
        <svg class="header-icon" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path>
          <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path>
        </svg>
        <h1 class="page-title">知识库管理</h1>
      </div>
      <div class="header-actions">
        <button class="btn btn-primary" @click="openCreateModal">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="12" y1="5" x2="12" y2="19"></line>
            <line x1="5" y1="12" x2="19" y2="12"></line>
          </svg>
          <span>创建知识库</span>
        </button>
      </div>
    </div>

    <!-- Info Banner -->
    <div class="info-banner">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10"></circle>
        <line x1="12" y1="16" x2="12" y2="12"></line>
        <line x1="12" y1="8" x2="12.01" y2="8"></line>
      </svg>
      <div class="banner-content">
        <p class="banner-title">知识库架构</p>
        <p class="banner-text">创建知识库 → 上传多个文档 → 文档解析分块 → 向量化存储。每个知识库可包含多个相关文档。</p>
      </div>
    </div>

    <!-- Knowledge Base List -->
    <div class="kb-list-container">
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="knowledgeBases.length === 0" class="empty-state">
        <svg class="empty-icon" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path>
          <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path>
        </svg>
        <p class="empty-title">暂无知识库</p>
        <p class="empty-text">点击上方按钮创建知识库</p>
      </div>

      <div v-else class="kb-list">
        <div
          v-for="kb in knowledgeBases"
          :key="kb.id"
          class="kb-card"
          :class="{ active: selectedKB?.id === kb.id }"
          @click="selectKnowledgeBase(kb)"
        >
          <!-- Card Header -->
          <div class="card-header">
            <div class="kb-info">
              <h3 class="kb-name">{{ kb.name }}</h3>
              <div class="kb-meta">
                <span class="status-badge" :class="kb.status.toLowerCase()">{{ kb.status === 'ACTIVE' ? '活跃' : '停用' }}</span>
                <span class="doc-count">{{ kb.documentCount }} 个文档</span>
                <span class="chunk-count">{{ kb.chunkCount }} 个分块</span>
              </div>
            </div>
            <div class="card-actions">
              <button class="action-btn" @click.stop="openUploadModal(kb)" title="上传文档">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                  <polyline points="17 8 12 3 7 8"></polyline>
                  <line x1="12" y1="3" x2="12" y2="15"></line>
                </svg>
              </button>
              <button class="action-btn" @click.stop="openEditModal(kb)" title="编辑">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                </svg>
              </button>
              <button class="action-btn danger" @click.stop="deleteKnowledgeBase(kb.id)" title="删除">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6"></polyline>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                </svg>
              </button>
            </div>
          </div>

          <!-- Card Body -->
          <div class="card-body">
            <p v-if="kb.description" class="kb-description">{{ kb.description }}</p>
            <div class="info-row">
              <label class="info-label">创建时间:</label>
              <span class="info-value">{{ formatDateTime(kb.createTime) }}</span>
            </div>
          </div>

          <!-- Documents Section (shown when selected) -->
          <div v-if="selectedKB?.id === kb.id && documents.length > 0" class="documents-section">
            <h4 class="documents-title">文档列表</h4>
            <div class="document-list-mini">
              <div v-for="doc in documents" :key="doc.id" class="document-item-mini">
                <span class="doc-name-mini">{{ doc.documentName }}</span>
                <span class="status-badge-mini" :class="doc.status.toLowerCase()">{{ getStatusText(doc.status) }}</span>
                <button class="action-btn-mini danger" @click.stop="deleteDocument(doc.id, kb.id)" title="删除">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <line x1="18" y1="6" x2="6" y2="18"></line>
                    <line x1="6" y1="6" x2="18" y2="18"></line>
                  </svg>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Create/Edit Knowledge Base Modal -->
    <div v-if="showKBModal" class="modal-overlay" @click="closeKBModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>{{ isEditing ? '编辑知识库' : '创建知识库' }}</h2>
          <button class="close-btn" @click="closeKBModal">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>

        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">知识库名称 *</label>
            <input v-model="kbForm.name" type="text" class="form-input" placeholder="请输入知识库名称" />
          </div>

          <div class="form-group">
            <label class="form-label">描述</label>
            <textarea v-model="kbForm.description" class="form-textarea" placeholder="请输入知识库描述" rows="3"></textarea>
          </div>
          <div class="form-group">
            <label class="form-label">密级标签 <span class="required">*</span></label>
            <select v-model="kbForm.securityLabelId" class="form-select">
              <option value="">请选择密级标签</option>
              <option v-for="label in securityLabels" :key="label.id" :value="label.id">
                {{ label.labelName }} ({{ label.labelCode }}) - {{ label.description }}
              </option>
            </select>
            <p v-if="securityLabels.length === 0" class="form-tip warning">
              暂无密级标签配置，请联系管理员添加
            </p>
          </div>
          <div class="form-group">
            <label class="form-label">默认向量模型</label>
            <select v-model="kbForm.embeddingModelId" class="form-select">
              <option value="">请选择向量模型（可选）</option>
              <option v-for="model in embeddingModels" :key="model.id" :value="model.id">
                {{ model.configName }} ({{ model.modelName }})
              </option>
            </select>
            <p class="form-tip">选择默认向量模型后，上传文档时将自动使用该模型</p>
          </div>


        </div>

        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeKBModal">取消</button>
          <button class="btn btn-primary" @click="submitKBForm" :disabled="!kbForm.name || !kbForm.securityLabelId || submitting">
            <span v-if="submitting">处理中...</span>
            <span v-else>{{ isEditing ? '保存' : '创建' }}</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Upload Document Modal -->
    <div v-if="showUploadModal" class="modal-overlay" @click="closeUploadModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>上传文档到知识库</h2>
          <button class="close-btn" @click="closeUploadModal">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>

        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">目标知识库</label>
            <p class="kb-name-display">{{ uploadTargetKB?.name }}</p>
          </div>

          <div class="form-group">
            <label class="form-label">向量模型</label>
            <select v-model="uploadEmbeddingModelId" class="form-select">
              <option value="">使用知识库默认模型</option>
              <option v-for="model in embeddingModels" :key="model.id" :value="model.id">
                {{ model.configName }} ({{ model.modelName }})
              </option>
            </select>
          </div>

          <div class="form-group">
            <label class="form-label">选择密级标签 <span class="required">*</span></label>
            <select v-model="uploadSecurityLabelId" class="form-select">
              <option value="">请选择密级标签</option>
              <option v-for="label in securityLabels" :key="label.id" :value="label.id">
                {{ label.labelName }} ({{ label.labelCode }}) - {{ label.description }}
              </option>
            </select>
            <p v-if="securityLabels.length === 0" class="form-tip warning">
              暂无密级标签配置，请联系管理员添加
            </p>
          </div>

          <div class="form-group">
            <label class="form-label">选择文件</label>
            <div class="file-upload-area" @click="triggerFileInput" @dragover.prevent @drop.prevent="handleFileDrop">
              <input
                type="file"
                ref="fileInput"
                @change="handleFileSelect"
                accept=".pdf,.docx,.doc,.txt"
                style="display: none"
              />
              <div v-if="!uploadFile" class="upload-placeholder">
                <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                  <polyline points="17 8 12 3 7 8"></polyline>
                  <line x1="12" y1="3" x2="12" y2="15"></line>
                </svg>
                <p>点击或拖拽文件到此处上传</p>
                <p class="file-types">支持 PDF、Word、TXT 格式</p>
              </div>
              <div v-else class="selected-file">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                  <polyline points="14 2 14 8 20 8"></polyline>
                </svg>
                <span>{{ uploadFile.name }}</span>
                <button class="remove-file-btn" @click.stop="removeUploadFile">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <line x1="18" y1="6" x2="6" y2="18"></line>
                    <line x1="6" y1="6" x2="18" y2="18"></line>
                  </svg>
                </button>
              </div>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeUploadModal">取消</button>
          <button class="btn btn-primary" @click="uploadDocument" :disabled="!uploadFile || !uploadSecurityLabelId || uploading" >
            <span v-if="uploading">上传中...</span>
            <span v-else>上传</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useKnowledgeStore } from '@/stores/knowledge'
import request from '@/utils/request'
import type { KnowledgeBase, KnowledgeDocument } from '@/types/knowledge'
import { useToast } from '@/composables/useToast'

interface AiModelConfig {
  id: string
  configName: string
  modelName: string
  modelType: string
}

interface SecurityLabel {
  id: string
  labelName: string
  labelCode: string
  labelLevel: number
  description: string
}

const knowledgeStore = useKnowledgeStore()
const toast = useToast()

const knowledgeBases = computed(() => knowledgeStore.knowledgeBases)
const documents = computed(() => knowledgeStore.currentDocuments)
const loading = computed(() => knowledgeStore.loading)

const selectedKB = ref<KnowledgeBase | null>(null)
const embeddingModels = ref<AiModelConfig[]>([])
const securityLabels = ref<SecurityLabel[]>([])

// Modal states
const showKBModal = ref(false)
const showUploadModal = ref(false)
const isEditing = ref(false)
const submitting = ref(false)
const uploading = ref(false)

// Form data
const kbForm = ref({
  id: '',
  name: '',
  description: '',
  embeddingModelId: '',
  securityLabelId: ''
})

const uploadTargetKB = ref<KnowledgeBase | null>(null)
const uploadFile = ref<File | null>(null)
const uploadEmbeddingModelId = ref('')
const uploadSecurityLabelId = ref('')
const fileInput = ref<HTMLInputElement>()

// 加载向量模型列表
const loadEmbeddingModels = async () => {
  try {
    const response = await request.get('/ai/config/list/EMBEDDING')
    if (response.code === 200) {
      embeddingModels.value = response.data
    }
  } catch (error) {
    console.error('加载向量模型列表失败:', error)
  }
}

// 加载密级标签列表
const loadSecurityLabels = async () => {
  try {
    const response = await request.get('/system/security-label/list')
    if (response.code === 200) {
      securityLabels.value = response.data
    }
  } catch (error) {
    console.error('加载密级标签列表失败:', error)
  }
}

// 选择知识库
const selectKnowledgeBase = async (kb: KnowledgeBase) => {
  selectedKB.value = kb
  await knowledgeStore.loadKnowledgeBaseDocuments(kb.id)
}

// 打开创建弹窗
const openCreateModal = () => {
  isEditing.value = false
  kbForm.value = { id: '', name: '', description: '', embeddingModelId: '', securityLabelId: '' }
  showKBModal.value = true
}

// 打开编辑弹窗
const openEditModal = (kb: KnowledgeBase) => {
  isEditing.value = true
  kbForm.value = {
    id: kb.id,
    name: kb.name,
    description: kb.description || '',
    embeddingModelId: kb.embeddingModelId || ''
  }
  showKBModal.value = true
}

// 关闭知识库弹窗
const closeKBModal = () => {
  showKBModal.value = false
}

// 提交知识库表单
const submitKBForm = async () => {
  if (!kbForm.value.name) return

  submitting.value = true
  try {
    if (isEditing.value) {
      await knowledgeStore.updateKB({
        id: kbForm.value.id,
        name: kbForm.value.name,
        description: kbForm.value.description,
        embeddingModelId: kbForm.value.embeddingModelId
      })
      toast.showSuccess('知识库更新成功')
    } else {
      await knowledgeStore.createKB({
        name: kbForm.value.name,
        description: kbForm.value.description,
        embeddingModelId: kbForm.value.embeddingModelId,
        securityLabelId: kbForm.value.securityLabelId
      })
      toast.showSuccess('知识库创建成功')
    }
    closeKBModal()
  } catch (error) {
    toast.showError(isEditing.value ? '知识库更新失败' : '知识库创建失败')
  } finally {
    submitting.value = false
  }
}

// 删除知识库
const deleteKnowledgeBase = async (id: string) => {
  if (!confirm('确定要删除该知识库吗？此操作将删除知识库下的所有文档，不可恢复。')) {
    return
  }

  try {
    await knowledgeStore.deleteKB(id)
    if (selectedKB.value?.id === id) {
      selectedKB.value = null
    }
    toast.showSuccess('知识库删除成功')
  } catch (error) {
    toast.showError('知识库删除失败')
  }
}

// 打开上传弹窗
const openUploadModal = (kb: KnowledgeBase) => {
  uploadTargetKB.value = kb
  uploadFile.value = null
  uploadEmbeddingModelId.value = kb.embeddingModelId || ''
  uploadSecurityLabelId.value = ''
  showUploadModal.value = true
}

// 关闭上传弹窗
const closeUploadModal = () => {
  showUploadModal.value = false
  uploadTargetKB.value = null
  uploadFile.value = null
}

// 触发文件选择
const triggerFileInput = () => {
  fileInput.value?.click()
}

// 处理文件选择
const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files.length > 0) {
    uploadFile.value = target.files[0]
  }
}

// 处理文件拖放
const handleFileDrop = (event: DragEvent) => {
  if (event.dataTransfer && event.dataTransfer.files.length > 0) {
    uploadFile.value = event.dataTransfer.files[0]
  }
}

// 移除上传文件
const removeUploadFile = () => {
  uploadFile.value = null
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

// 上传文档
const uploadDocument = async () => {
  if (!uploadFile.value || !uploadTargetKB.value || !uploadSecurityLabelId.value) {
    toast.showError('请选择密级标签')
    return
  }

  uploading.value = true
  try {
    await knowledgeStore.uploadDocument(
      uploadFile.value,
      uploadTargetKB.value.id,
      uploadEmbeddingModelId.value || undefined,
      uploadSecurityLabelId.value
    )
    toast.showSuccess('文档上传成功')
    closeUploadModal()
    // 刷新文档列表
    if (selectedKB.value?.id === uploadTargetKB.value.id) {
      await knowledgeStore.loadKnowledgeBaseDocuments(uploadTargetKB.value.id)
    }
  } catch (error) {
    toast.showError('文档上传失败')
  } finally {
    uploading.value = false
  }
}

// 删除文档
const deleteDocument = async (documentId: string, knowledgeBaseId: string) => {
  if (!confirm('确定要删除该文档吗？')) {
    return
  }

  try {
    await knowledgeStore.removeDocument(documentId, knowledgeBaseId)
    toast.showSuccess('文档删除成功')
  } catch (error) {
    toast.showError('文档删除失败')
  }
}

// 格式化日期时间
const formatDateTime = (dateTime: string): string => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN')
}

// 获取状态文本
const getStatusText = (status: string): string => {
  const statusMap: Record<string, string> = {
    'PENDING': '待处理',
    'PROCESSING': '处理中',
    'COMPLETED': '已完成',
    'FAILED': '失败'
  }
  return statusMap[status] || status
}

onMounted(() => {
  knowledgeStore.loadKnowledgeBases()
  loadEmbeddingModels()
  loadSecurityLabels()
})
</script>

<style scoped>
.knowledge-base-management {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  color: #10b981;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.btn-secondary {
  background: white;
  color: #6b7280;
  border: 1px solid #e5e7eb;
}

.btn-secondary:hover {
  background: #f9fafb;
}

.info-banner {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: #ecfdf5;
  border: 1px solid #10b981;
  border-radius: 8px;
  margin-bottom: 24px;
}

.info-banner svg {
  color: #10b981;
  flex-shrink: 0;
}

.banner-content {
  flex: 1;
}

.banner-title {
  font-weight: 600;
  color: #065f46;
  margin: 0 0 4px 0;
}

.banner-text {
  color: #047857;
  margin: 0;
  font-size: 14px;
}

.kb-list-container {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.loading-state,
.empty-state {
  text-align: center;
  padding: 48px;
  color: #6b7280;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #e5e7eb;
  border-top-color: #10b981;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-icon {
  color: #d1d5db;
  margin-bottom: 16px;
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: #374151;
  margin: 0 0 8px 0;
}

.empty-text {
  font-size: 14px;
  margin: 0;
}

.kb-list {
  display: grid;
  gap: 16px;
}

.kb-card {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  transition: all 0.2s;
  cursor: pointer;
}

.kb-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.kb-card.active {
  border-color: #10b981;
  background: #f0fdf4;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.kb-info {
  flex: 1;
}

.kb-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.kb-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.status-badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.active {
  background: #d1fae5;
  color: #059669;
}

.status-badge.inactive {
  background: #f3f4f6;
  color: #6b7280;
}

.doc-count,
.chunk-count {
  font-size: 12px;
  color: #6b7280;
}

.card-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: 1px solid #e5e7eb;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:hover {
  background: #f9fafb;
}

.action-btn.danger:hover {
  background: #fee2e2;
  border-color: #dc2626;
  color: #dc2626;
}

.card-body {
  display: grid;
  gap: 8px;
}

.kb-description {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 8px 0;
}

.info-row {
  display: flex;
  font-size: 14px;
}

.info-label {
  color: #6b7280;
  min-width: 100px;
}

.info-value {
  color: #1f2937;
}

.documents-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}

.documents-title {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin: 0 0 12px 0;
}

.document-list-mini {
  display: grid;
  gap: 8px;
}

.document-item-mini {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f9fafb;
  border-radius: 4px;
}

.doc-name-mini {
  flex: 1;
  font-size: 13px;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-badge-mini {
  padding: 1px 6px;
  border-radius: 3px;
  font-size: 11px;
  font-weight: 500;
}

.status-badge-mini.pending {
  background: #fef3c7;
  color: #d97706;
}

.status-badge-mini.processing {
  background: #dbeafe;
  color: #2563eb;
}

.status-badge-mini.completed {
  background: #d1fae5;
  color: #059669;
}

.status-badge-mini.failed {
  background: #fee2e2;
  color: #dc2626;
}

.action-btn-mini {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border: none;
  background: transparent;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn-mini.danger:hover {
  background: #fee2e2;
  color: #dc2626;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.close-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #6b7280;
  padding: 4px;
}

.close-btn:hover {
  color: #1f2937;
}

.modal-body {
  padding: 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.form-input,
.form-select,
.form-textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: #10b981;
}

.form-textarea {
  resize: vertical;
}

.form-tip {
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
}

.form-tip.warning {
  color: #d97706;
}

.required {
  color: #dc2626;
  font-weight: bold;
}

.kb-name-display {
  font-size: 14px;
  font-weight: 600;
  color: #10b981;
  margin: 0;
}

.file-upload-area {
  border: 2px dashed #d1d5db;
  border-radius: 8px;
  padding: 32px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
}

.file-upload-area:hover {
  border-color: #10b981;
  background: #f9fafb;
}

.upload-placeholder {
  color: #6b7280;
}

.upload-placeholder svg {
  color: #d1d5db;
  margin-bottom: 12px;
}

.upload-placeholder p {
  margin: 4px 0;
}

.file-types {
  font-size: 12px;
  color: #9ca3af;
}

.selected-file {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #10b981;
}

.selected-file svg {
  color: #10b981;
}

.remove-file-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #6b7280;
  padding: 4px;
}

.remove-file-btn:hover {
  color: #dc2626;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #e5e7eb;
}
</style>