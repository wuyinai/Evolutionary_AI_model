<template>
  <div class="knowledge-document-management">
    <!-- Header Section -->
    <div class="page-header">
      <div class="header-content">
        <svg class="header-icon" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
          <polyline points="14 2 14 8 20 8"></polyline>
          <line x1="16" y1="13" x2="8" y2="13"></line>
          <line x1="16" y1="17" x2="8" y2="17"></line>
          <polyline points="10 9 9 9 8 9"></polyline>
        </svg>
        <h1 class="page-title">知识库文档管理</h1>
      </div>
      <div class="header-actions">
        <button class="btn btn-primary" @click="openUploadModal">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
            <polyline points="17 8 12 3 7 8"></polyline>
            <line x1="12" y1="3" x2="12" y2="15"></line>
          </svg>
          <span>上传文档</span>
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
        <p class="banner-title">文档向量化流程</p>
        <p class="banner-text">上传文档 → 文档解析 → 文本分块 → 向量化 → 存入向量数据库。支持PDF、Word、TXT格式。</p>
      </div>
    </div>

    <!-- Document List -->
    <div class="document-list-container">
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="documents.length === 0" class="empty-state">
        <svg class="empty-icon" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
          <polyline points="14 2 14 8 20 8"></polyline>
        </svg>
        <p class="empty-title">暂无文档</p>
        <p class="empty-text">点击上方按钮上传文档</p>
      </div>

      <div v-else class="document-list">
        <div
          v-for="doc in documents"
          :key="doc.id"
          class="document-card"
        >
          <!-- Card Header -->
          <div class="card-header">
            <div class="document-info">
              <h3 class="document-name">{{ doc.documentName }}</h3>
              <div class="document-meta">
                <span class="file-type-badge" :class="doc.fileType">{{ doc.fileType.toUpperCase() }}</span>
                <span class="file-size">{{ formatFileSize(doc.fileSize) }}</span>
                <span class="status-badge" :class="doc.status.toLowerCase()">{{ getStatusText(doc.status) }}</span>
              </div>
            </div>
            <div class="card-actions">
              <button class="action-btn" @click="viewDocument(doc)" title="查看详情">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                  <circle cx="12" cy="12" r="3"></circle>
                </svg>
              </button>
              <button v-if="doc.status === 'FAILED'" class="action-btn" @click="reprocessDocument(doc.id)" title="重新处理">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="23 4 23 10 17 10"></polyline>
                  <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"></path>
                </svg>
              </button>
              <button class="action-btn danger" @click="deleteDocument(doc.id)" title="删除">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6"></polyline>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                </svg>
              </button>
            </div>
          </div>

          <!-- Card Body -->
          <div class="card-body">
            <div class="info-row">
              <label class="info-label">分块数量:</label>
              <span class="info-value">{{ doc.chunkCount || 0 }}</span>
            </div>
            <div class="info-row">
              <label class="info-label">上传时间:</label>
              <span class="info-value">{{ formatDateTime(doc.createTime) }}</span>
            </div>
            <div v-if="doc.errorMessage" class="info-row error">
              <label class="info-label">错误信息:</label>
              <span class="info-value">{{ doc.errorMessage }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Upload Modal -->
    <div v-if="showUploadModal" class="modal-overlay" @click="closeUploadModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>上传文档</h2>
          <button class="close-btn" @click="closeUploadModal">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>

        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">选择向量模型</label>
            <select v-model="selectedEmbeddingModelId" class="form-select">
              <option value="">请选择向量模型</option>
              <option v-for="model in embeddingModels" :key="model.id" :value="model.id">
                {{ model.configName }} ({{ model.modelName }})
              </option>
            </select>
            <p v-if="embeddingModels.length === 0" class="form-tip warning">
              暂无可用的向量模型，请先<router-link to="/model-config">配置向量模型</router-link>
            </p>
          </div>

          <div class="form-group">
            <label class="form-label">选择密级标签 <span class="required">*</span></label>
            <select v-model="selectedSecurityLabelId" class="form-select">
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
              <div v-if="!selectedFile" class="upload-placeholder">
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
                <span>{{ selectedFile.name }}</span>
                <button class="remove-file-btn" @click.stop="removeFile">
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
          <button
            class="btn btn-primary"
            @click="uploadDocument"
            :disabled="!selectedFile || !selectedEmbeddingModelId || !selectedSecurityLabelId || uploading"
          >
            <span v-if="uploading">上传中...</span>
            <span v-else>上传</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Document Detail Modal -->
    <div v-if="showDetailModal" class="modal-overlay" @click="closeDetailModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>文档详情</h2>
          <button class="close-btn" @click="closeDetailModal">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>

        <div class="modal-body" v-if="selectedDocument">
          <div class="detail-row">
            <label>文档名称:</label>
            <span>{{ selectedDocument.documentName }}</span>
          </div>
          <div class="detail-row">
            <label>文件类型:</label>
            <span>{{ selectedDocument.fileType.toUpperCase() }}</span>
          </div>
          <div class="detail-row">
            <label>文件大小:</label>
            <span>{{ formatFileSize(selectedDocument.fileSize) }}</span>
          </div>
          <div class="detail-row">
            <label>处理状态:</label>
            <span class="status-badge" :class="selectedDocument.status.toLowerCase()">{{ getStatusText(selectedDocument.status) }}</span>
          </div>
          <div class="detail-row">
            <label>分块数量:</label>
            <span>{{ selectedDocument.chunkCount || 0 }}</span>
          </div>
          <div class="detail-row">
            <label>上传时间:</label>
            <span>{{ formatDateTime(selectedDocument.createTime) }}</span>
          </div>
          <div v-if="selectedDocument.errorMessage" class="detail-row error">
            <label>错误信息:</label>
            <span>{{ selectedDocument.errorMessage }}</span>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeDetailModal">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { useToast } from '@/composables/useToast'

interface KnowledgeDocument {
  id: string
  documentName: string
  userId: string
  fileType: string
  fileSize: number
  storagePath: string
  embeddingModelId: string
  status: string
  chunkCount: number
  errorMessage: string
  createTime: string
  updateTime: string
  delFlag: number
}

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

const documents = ref<KnowledgeDocument[]>([])
const embeddingModels = ref<AiModelConfig[]>([])
const securityLabels = ref<SecurityLabel[]>([])
const loading = ref(false)
const showUploadModal = ref(false)
const showDetailModal = ref(false)
const selectedFile = ref<File | null>(null)
const selectedEmbeddingModelId = ref('')
const selectedSecurityLabelId = ref('')
const selectedDocument = ref<KnowledgeDocument | null>(null)
const uploading = ref(false)
const fileInput = ref<HTMLInputElement>()
const toast = useToast()

// 加载文档列表（仅独立文档，不属于任何知识库）
const loadDocuments = async () => {
  loading.value = true
  try {
    const response = await request.get('/knowledge/document/standalone')
    if (response.code === 200) {
      documents.value = response.data
    }
  } catch (error) {
    console.error('加载文档列表失败:', error)
    toast.showError('加载文档列表失败')
  } finally {
    loading.value = false
  }
}

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

// 打开上传弹窗
const openUploadModal = () => {
  showUploadModal.value = true
  selectedFile.value = null
  selectedEmbeddingModelId.value = ''
  selectedSecurityLabelId.value = ''
}

// 关闭上传弹窗
const closeUploadModal = () => {
  showUploadModal.value = false
}

// 触发文件选择
const triggerFileInput = () => {
  fileInput.value?.click()
}

// 处理文件选择
const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files.length > 0) {
    selectedFile.value = target.files[0]
  }
}

// 处理文件拖放
const handleFileDrop = (event: DragEvent) => {
  if (event.dataTransfer && event.dataTransfer.files.length > 0) {
    selectedFile.value = event.dataTransfer.files[0]
  }
}

// 移除文件
const removeFile = () => {
  selectedFile.value = null
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

// 上传文档
const uploadDocument = async () => {
  if (!selectedFile.value || !selectedEmbeddingModelId.value || !selectedSecurityLabelId.value) {
    toast.showError('请选择向量模型和密级标签')
    return
  }

  uploading.value = true
  const formData = new FormData()
  formData.append('file', selectedFile.value)
  formData.append('embeddingModelId', selectedEmbeddingModelId.value)
  formData.append('securityLabelId', selectedSecurityLabelId.value)

  try {
    const response = await request.post('/knowledge/document/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    if (response.code === 200) {
      toast.showSuccess('文档上传成功')
      closeUploadModal()
      loadDocuments()
    } else {
      toast.showError('文档上传失败: ' + response.message)
    }
  } catch (error) {
    console.error('文档上传失败:', error)
    toast.showError('文档上传失败')
  } finally {
    uploading.value = false
  }
}

// 查看文档详情
const viewDocument = async (doc: KnowledgeDocument) => {
  try {
    const response = await request.get(`/knowledge/document/status/${doc.id}`)
    if (response.code === 200) {
      selectedDocument.value = response.data
      showDetailModal.value = true
    }
  } catch (error) {
    console.error('获取文档详情失败:', error)
    toast.showError('获取文档详情失败')
  }
}

// 关闭详情弹窗
const closeDetailModal = () => {
  showDetailModal.value = false
  selectedDocument.value = null
}

// 重新处理文档
const reprocessDocument = async (documentId: string) => {
  if (!confirm('确定要重新处理该文档吗？')) {
    return
  }

  try {
    const response = await request.post(`/knowledge/document/reprocess/${documentId}`)
    if (response.code === 200) {
      toast.showSuccess('文档重新处理成功')
      loadDocuments()
    } else {
      toast.showError('文档重新处理失败: ' + response.message)
    }
  } catch (error) {
    console.error('文档重新处理失败:', error)
    toast.showError('文档重新处理失败')
  }
}

// 删除文档
const deleteDocument = async (documentId: string) => {
  if (!confirm('确定要删除该文档吗？此操作不可恢复。')) {
    return
  }

  try {
    const response = await request.delete(`/knowledge/document/${documentId}`)
    if (response.code === 200) {
      toast.showSuccess('文档删除成功')
      loadDocuments()
    } else {
      toast.showError('文档删除失败: ' + response.message)
    }
  } catch (error) {
    console.error('文档删除失败:', error)
    toast.showError('文档删除失败')
  }
}

// 格式化文件大小
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
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
  loadDocuments()
  loadEmbeddingModels()
  loadSecurityLabels()
})
</script>

<style scoped>
.knowledge-document-management {
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

.document-list-container {
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

.document-list {
  display: grid;
  gap: 16px;
}

.document-card {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  transition: all 0.2s;
}

.document-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.document-info {
  flex: 1;
}

.document-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.document-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.file-type-badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.file-type-badge.pdf {
  background: #fee2e2;
  color: #dc2626;
}

.file-type-badge.docx,
.file-type-badge.doc {
  background: #dbeafe;
  color: #2563eb;
}

.file-type-badge.txt {
  background: #f3f4f6;
  color: #6b7280;
}

.file-size {
  font-size: 12px;
  color: #6b7280;
}

.status-badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.pending {
  background: #fef3c7;
  color: #d97706;
}

.status-badge.processing {
  background: #dbeafe;
  color: #2563eb;
}

.status-badge.completed {
  background: #d1fae5;
  color: #059669;
}

.status-badge.failed {
  background: #fee2e2;
  color: #dc2626;
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

.info-row.error .info-value {
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

.form-select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

.form-tip {
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
}

.form-tip.warning {
  color: #d97706;
}

.form-tip.warning a {
  color: #10b981;
  text-decoration: underline;
}

.required {
  color: #dc2626;
  font-weight: bold;
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

.detail-row {
  display: flex;
  margin-bottom: 12px;
  font-size: 14px;
}

.detail-row label {
  min-width: 100px;
  color: #6b7280;
}

.detail-row span {
  color: #1f2937;
}

.detail-row.error span {
  color: #dc2626;
}
</style>
