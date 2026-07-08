<template>
  <div class="sys-prompt-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <svg class="header-icon" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path>
          <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path>
        </svg>
        <h1 class="page-title">默认提示词管理</h1>
      </div>
      <div class="header-actions">
        <input
          v-model="searchKeyword"
          class="search-input"
          placeholder="搜索提示词..."
          type="text"
        />
        <button v-if="hasPermission('sys:prompt:upload')" class="btn btn-primary" @click="openUploadModal">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
            <polyline points="17 8 12 3 7 8"></polyline>
            <line x1="12" y1="3" x2="12" y2="15"></line>
          </svg>
          <span>上传文档</span>
        </button>
        <button v-if="hasPermission('sys:prompt:add')" class="btn btn-secondary" @click="openCreateTextModal">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="12" y1="5" x2="12" y2="19"></line>
            <line x1="5" y1="12" x2="19" y2="12"></line>
          </svg>
          <span>创建文本提示词</span>
        </button>
      </div>
    </div>

    <!-- 信息提示Banner -->
    <div class="info-banner">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10"></circle>
        <line x1="12" y1="16" x2="12" y2="12"></line>
        <line x1="12" y1="8" x2="12.01" y2="8"></line>
      </svg>
      <div class="banner-content">
        <p class="banner-title">系统默认提示词</p>
        <p class="banner-text">管理系统级默认提示词，用于约束智能体规范行为。支持文档型和文本型两种提示词类型。</p>
      </div>
    </div>

    <!-- 提示词列表 -->
    <div class="prompt-list-container">
      <div v-if="sysPromptStore.loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="filteredPrompts.length === 0" class="empty-state">
        <svg class="empty-icon" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path>
          <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path>
        </svg>
        <p class="empty-title">暂无默认提示词</p>
        <p class="empty-text">点击上方按钮上传文档或创建文本提示词</p>
      </div>

      <div v-else class="prompt-list">
        <div
          v-for="prompt in filteredPrompts"
          :key="prompt.id"
          class="prompt-card"
        >
          <!-- 卡片头部 -->
          <div class="card-header">
            <div class="prompt-info">
              <h3 class="prompt-name">{{ prompt.promptName }}</h3>
              <p class="prompt-code">{{ prompt.promptCode }}</p>
              <div class="prompt-meta">
                <span class="type-badge" :class="prompt.promptType.toLowerCase()">
                  {{ prompt.promptType === 'DOCUMENT' ? '文档型' : '文本型' }}
                </span>
                <span class="status-badge" :class="prompt.isEnabled === 1 ? 'enabled' : 'disabled'">
                  {{ prompt.isEnabled === 1 ? '已启用' : '已禁用' }}
                </span>
                <span v-if="prompt.isDefault === 1" class="default-badge">默认</span>
              </div>
            </div>
            <div class="card-actions">
              <!-- 文档预览 -->
              <button
                v-if="prompt.promptType === 'DOCUMENT' && hasPermission('sys:prompt:preview')"
                class="action-btn"
                @click="previewDocument(prompt.id)"
                title="文档预览"
              >
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                  <circle cx="12" cy="12" r="3"></circle>
                </svg>
              </button>
              <!-- 编辑 -->
              <button
                v-if="hasPermission('sys:prompt:edit')"
                class="action-btn"
                @click="openEditModal(prompt)"
                title="编辑"
              >
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                </svg>
              </button>
              <!-- 删除 -->
              <button
                v-if="hasPermission('sys:prompt:delete')"
                class="action-btn danger"
                @click="confirmDelete(prompt)"
                title="删除"
              >
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6"></polyline>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                </svg>
              </button>
            </div>
          </div>

          <!-- 卡片内容 -->
          <div class="card-content">
            <p class="prompt-description">{{ prompt.promptDescription || '暂无描述' }}</p>
            <div class="prompt-footer">
              <!-- 启用开关 -->
              <div class="toggle-wrapper">
                <label class="status-toggle">
                  <input
                    type="checkbox"
                    :checked="prompt.isEnabled === 1"
                    @change="toggleEnabled(prompt)"
                    class="toggle-input"
                  />
                  <span class="toggle-slider"></span>
                </label>
                <span class="toggle-label">{{ prompt.isEnabled === 1 ? '启用' : '禁用' }}</span>
              </div>
              <!-- 默认提示词开关 -->
              <div class="toggle-wrapper">
                <label class="default-toggle">
                  <input
                    type="checkbox"
                    :checked="prompt.isDefault === 1"
                    @change="toggleDefault(prompt)"
                    class="toggle-input"
                  />
                  <span class="toggle-slider"></span>
                </label>
                <span class="toggle-label">{{ prompt.isDefault === 1 ? '默认提示词' : '设为默认' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 上传文档Modal -->
    <div v-if="uploadModalVisible" class="modal-overlay" @click="closeUploadModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>上传文档型提示词</h2>
          <button class="close-btn" @click="closeUploadModal">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>

        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">提示词名称 *</label>
            <input v-model="uploadForm.promptName" type="text" class="form-input" placeholder="请输入提示词名称" />
          </div>

          <div class="form-group">
            <label class="form-label">提示词编码 *</label>
            <input v-model="uploadForm.promptCode" type="text" class="form-input" placeholder="请输入唯一编码（如：general_assistant）" />
          </div>

          <div class="form-group">
            <label class="form-label">描述</label>
            <textarea v-model="uploadForm.promptDescription" class="form-textarea" placeholder="请输入描述" rows="3"></textarea>
          </div>

          <div class="form-group">
            <label class="form-label">文档文件 *</label>
            <div class="upload-area">
              <input
                ref="uploadFileInput"
                type="file"
                @change="handleFileChange"
                accept=".pdf,.docx,.doc,.txt"
                hidden
              />
              <div class="upload-box" @click="triggerUploadFileInput">
                <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                  <polyline points="17 8 12 3 7 8"></polyline>
                  <line x1="12" y1="3" x2="12" y2="15"></line>
                </svg>
                <p class="upload-text">点击上传文档</p>
                <p class="upload-hint">支持 PDF、Word、TXT 格式</p>
              </div>
              <div v-if="uploadFileList.length > 0" class="file-list">
                <div v-for="(file, index) in uploadFileList" :key="index" class="file-item">
                  <span class="file-name">{{ file.name }}</span>
                  <button class="remove-btn" @click="handleRemoveFile(index)">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <line x1="18" y1="6" x2="6" y2="18"></line>
                      <line x1="6" y1="6" x2="18" y2="18"></line>
                    </svg>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeUploadModal">取消</button>
          <button class="btn btn-primary" @click="handleUpload" :disabled="!uploadForm.promptName || !uploadForm.promptCode || uploadFileList.length === 0">
            上传
          </button>
        </div>
      </div>
    </div>

    <!-- 创建文本提示词Modal -->
    <div v-if="createTextModalVisible" class="modal-overlay" @click="closeCreateTextModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>创建文本型提示词</h2>
          <button class="close-btn" @click="closeCreateTextModal">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>

        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">提示词名称 *</label>
            <input v-model="createTextForm.promptName" type="text" class="form-input" placeholder="请输入提示词名称" />
          </div>

          <div class="form-group">
            <label class="form-label">提示词编码 *</label>
            <input v-model="createTextForm.promptCode" type="text" class="form-input" placeholder="请输入唯一编码" />
          </div>

          <div class="form-group">
            <label class="form-label">描述</label>
            <textarea v-model="createTextForm.promptDescription" class="form-textarea" placeholder="请输入描述" rows="3"></textarea>
          </div>

          <div class="form-group">
            <label class="form-label">提示词内容 *</label>
            <textarea v-model="createTextForm.textContent" class="form-textarea" placeholder="请输入提示词内容" rows="6"></textarea>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeCreateTextModal">取消</button>
          <button class="btn btn-primary" @click="handleCreateText" :disabled="!createTextForm.promptName || !createTextForm.promptCode || !createTextForm.textContent">
            创建
          </button>
        </div>
      </div>
    </div>

    <!-- 编辑Modal -->
    <div v-if="editModalVisible" class="modal-overlay" @click="closeEditModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>编辑提示词</h2>
          <button class="close-btn" @click="closeEditModal">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>

        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">提示词名称 *</label>
            <input v-model="editForm.promptName" type="text" class="form-input" />
          </div>

          <div class="form-group">
            <label class="form-label">提示词编码 *</label>
            <input v-model="editForm.promptCode" type="text" class="form-input" />
          </div>

          <div class="form-group">
            <label class="form-label">描述</label>
            <textarea v-model="editForm.promptDescription" class="form-textarea" rows="3"></textarea>
          </div>

          <div v-if="editForm.promptType === 'TEXT'" class="form-group">
            <label class="form-label">提示词内容 *</label>
            <textarea v-model="editForm.textContent" class="form-textarea" rows="6"></textarea>
          </div>

          <div class="form-group">
            <label class="form-label">排序号</label>
            <input v-model.number="editForm.sortOrder" type="number" class="form-input" min="0" />
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeEditModal">取消</button>
          <button class="btn btn-primary" @click="handleEdit" :disabled="!editForm.promptName || !editForm.promptCode">
            保存
          </button>
        </div>
      </div>
    </div>

    <!-- 文档预览Modal -->
    <div v-if="previewModalVisible" class="modal-overlay" @click="closePreviewModal">
      <div class="modal-content modal-large" @click.stop>
        <div class="modal-header">
          <h2>文档预览</h2>
          <button class="close-btn" @click="closePreviewModal">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>

        <div class="modal-body">
          <iframe
            v-if="previewUrl"
            :src="previewUrl"
            class="preview-iframe"
            frameborder="0"
          ></iframe>
          <div v-else class="preview-empty">
            <p>加载预览内容...</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 删除确认Modal -->
    <div v-if="deleteModalVisible" class="modal-overlay" @click="closeDeleteModal">
      <div class="modal-content modal-small" @click.stop>
        <div class="modal-header">
          <h2>确认删除</h2>
          <button class="close-btn" @click="closeDeleteModal">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>

        <div class="modal-body">
          <p class="confirm-text">确定要删除提示词 <strong>{{ deleteTarget?.promptName }}</strong> 吗？</p>
          <p class="warning-text">此操作不可恢复，文档文件将一并删除。</p>
        </div>

        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeDeleteModal">取消</button>
          <button class="btn btn-danger" @click="handleDelete">删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useSysPromptStore } from '@/stores/sysPrompt'
import { usePermission } from '@/composables/usePermission'
import { useToast } from '@/composables/useToast'
import type { SysPrompt } from '@/types/sysPrompt'

const sysPromptStore = useSysPromptStore()
const { hasPermission, loadPermissions } = usePermission()
const toast = useToast()

// 搜索关键词
const searchKeyword = ref('')

// 过滤后的提示词列表
const filteredPrompts = computed(() => {
  if (!searchKeyword.value) return sysPromptStore.prompts
  const keyword = searchKeyword.value.toLowerCase()
  return sysPromptStore.prompts.filter(prompt =>
    prompt.promptName.toLowerCase().includes(keyword) ||
    prompt.promptCode?.toLowerCase().includes(keyword) ||
    prompt.promptDescription?.toLowerCase().includes(keyword)
  )
})

// Modal显示状态
const uploadModalVisible = ref(false)
const createTextModalVisible = ref(false)
const editModalVisible = ref(false)
const previewModalVisible = ref(false)
const deleteModalVisible = ref(false)

// 表单数据
const uploadForm = ref({
  promptName: '',
  promptCode: '',
  promptDescription: ''
})
const uploadFileList = ref<File[]>([])
const uploadFileInput = ref<HTMLInputElement | null>(null)

const createTextForm = ref({
  promptName: '',
  promptCode: '',
  promptDescription: '',
  textContent: ''
})
const editForm = ref<SysPrompt | null>(null)
const previewUrl = ref('')
const deleteTarget = ref<SysPrompt | null>(null)

// 打开上传Modal
const openUploadModal = () => {
  uploadModalVisible.value = true
  uploadForm.value = {
    promptName: '',
    promptCode: '',
    promptDescription: ''
  }
  uploadFileList.value = []
}

// 关闭上传Modal
const closeUploadModal = () => {
  uploadModalVisible.value = false
}

// 触发文件选择
const triggerUploadFileInput = () => {
  uploadFileInput.value?.click()
}

// 处理文件选择
const handleFileChange = (event: Event) => {
  const input = event.target as HTMLInputElement
  if (input.files) {
    const newFile = Array.from(input.files)[0]
    uploadFileList.value = [newFile]
  }
  // 重置input，以便可以再次选择相同的文件
  if (uploadFileInput.value) {
    uploadFileInput.value.value = ''
  }
}

// 移除文件
const handleRemoveFile = (index: number) => {
  uploadFileList.value.splice(index, 1)
}

// 执行上传
const handleUpload = async () => {
  if (!uploadForm.value.promptName || !uploadForm.value.promptCode) {
    toast.showError('请填写提示词名称和编码')
    return
  }
  if (uploadFileList.value.length === 0) {
    toast.showError('请选择文档文件')
    return
  }

  try {
    await sysPromptStore.uploadDocumentPrompt({
      promptName: uploadForm.value.promptName,
      promptCode: uploadForm.value.promptCode,
      promptDescription: uploadForm.value.promptDescription,
      file: uploadFileList.value[0]
    })
    toast.showSuccess('文档上传成功')
    closeUploadModal()
  } catch (error: any) {
    toast.showError(error.message || '上传失败')
  }
}

// 打开创建文本提示词Modal
const openCreateTextModal = () => {
  createTextModalVisible.value = true
  createTextForm.value = {
    promptName: '',
    promptCode: '',
    promptDescription: '',
    textContent: ''
  }
}

// 关闭创建文本提示词Modal
const closeCreateTextModal = () => {
  createTextModalVisible.value = false
}

// 执行创建文本提示词
const handleCreateText = async () => {
  if (!createTextForm.value.promptName || !createTextForm.value.promptCode || !createTextForm.value.textContent) {
    toast.showError('请填写完整信息')
    return
  }

  try {
    await sysPromptStore.createTextPrompt({
      promptName: createTextForm.value.promptName,
      promptCode: createTextForm.value.promptCode,
      promptDescription: createTextForm.value.promptDescription,
      promptType: 'TEXT',
      textContent: createTextForm.value.textContent
    })
    toast.showSuccess('文本提示词创建成功')
    closeCreateTextModal()
  } catch (error: any) {
    toast.showError(error.message || '创建失败')
  }
}

// 打开编辑Modal
const openEditModal = (prompt: SysPrompt) => {
  editModalVisible.value = true
  editForm.value = { ...prompt }
}

// 关闭编辑Modal
const closeEditModal = () => {
  editModalVisible.value = false
  editForm.value = null
}

// 执行编辑
const handleEdit = async () => {
  if (!editForm.value) return

  try {
    await sysPromptStore.updatePrompt(editForm.value.id, editForm.value)
    toast.showSuccess('更新成功')
    closeEditModal()
  } catch (error: any) {
    toast.showError(error.message || '更新失败')
  }
}

// 文档预览
const previewDocument = async (promptId: number) => {
  try {
    const url = await sysPromptStore.getPreviewUrl(promptId)
    previewUrl.value = url
    previewModalVisible.value = true
  } catch (error: any) {
    toast.showError(error.message || '获取预览URL失败')
  }
}

// 关闭预览Modal
const closePreviewModal = () => {
  previewModalVisible.value = false
  previewUrl.value = ''
}

// 确认删除
const confirmDelete = (prompt: SysPrompt) => {
  deleteTarget.value = prompt
  deleteModalVisible.value = true
}

// 关闭删除Modal
const closeDeleteModal = () => {
  deleteModalVisible.value = false
  deleteTarget.value = null
}

// 执行删除
const handleDelete = async () => {
  if (!deleteTarget.value) return

  try {
    await sysPromptStore.deletePrompt(deleteTarget.value.id)
    toast.showSuccess('删除成功')
    closeDeleteModal()
  } catch (error: any) {
    toast.showError(error.message || '删除失败')
  }
}

// 切换启用状态
const toggleEnabled = async (prompt: SysPrompt) => {
  try {
    const newStatus = prompt.isEnabled === 1 ? 0 : 1
    await sysPromptStore.updateEnabledStatus(prompt.id, newStatus)
    toast.showSuccess(newStatus === 1 ? '已启用' : '已禁用')
  } catch (error: any) {
    toast.showError(error.message || '操作失败')
  }
}

// 设置默认提示词
const toggleDefault = async (prompt: SysPrompt) => {
  try {
    const newDefault = prompt.isDefault === 1 ? 0 : 1
    await sysPromptStore.setDefaultPrompt(prompt.id, newDefault)
    toast.showSuccess(newDefault === 1 ? '已设为默认提示词' : '已取消默认')
  } catch (error: any) {
    toast.showError(error.message || '操作失败')
  }
}

// 初始化加载
onMounted(async () => {
  await loadPermissions()
  await sysPromptStore.loadPrompts()
})
</script>

<style scoped>
/* 主容器 */
.sys-prompt-view {
  width: 100%;
  min-height: 100vh;
  padding: 24px;
  background-color: #fafafa;
}

/* 页面头部 */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 24px;
  border-bottom: 1px solid #e8e8e8;
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  color: #1890ff;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #262626;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-input {
  width: 300px;
  padding: 10px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.search-input:focus {
  outline: none;
  border-color: #1890ff;
}

/* 信息横幅 */
.info-banner {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background-color: #e6f7ff;
  border: 1px solid #91d5ff;
  border-radius: 8px;
  margin-bottom: 24px;
}

.info-banner svg {
  color: #1890ff;
  flex-shrink: 0;
}

.banner-content {
  flex: 1;
}

.banner-title {
  font-size: 14px;
  font-weight: 600;
  color: #0050b3;
  margin: 0 0 4px 0;
}

.banner-text {
  font-size: 13px;
  color: #595959;
  margin: 0;
  line-height: 1.5;
}

/* 提示词列表容器 */
.prompt-list-container {
  background-color: #ffffff;
  border-radius: 8px;
  padding: 24px;
  min-height: 400px;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px;
  gap: 16px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #e8e8e8;
  border-top-color: #1890ff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-state p {
  font-size: 14px;
  color: #8c8c8c;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px;
  gap: 16px;
}

.empty-icon {
  color: #bfbfbf;
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: #595959;
  margin: 0;
}

.empty-text {
  font-size: 14px;
  color: #8c8c8c;
  margin: 0;
}

/* 提示词列表 */
.prompt-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

/* 提示词卡片 */
.prompt-card {
  background-color: #ffffff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.2s;
}

.prompt-card:hover {
  border-color: #1890ff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

/* 卡片头部 */
.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 20px;
  border-bottom: 1px solid #e8e8e8;
}

.prompt-info {
  flex: 1;
  min-width: 0;
}

.prompt-name {
  font-size: 18px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.prompt-code {
  font-size: 12px;
  color: #8c8c8c;
  margin: 0 0 8px 0;
}

.prompt-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.type-badge,
.status-badge,
.default-badge {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.type-badge.document {
  background-color: #f0f5ff;
  color: #2f54eb;
}

.type-badge.text {
  background-color: #fcffe6;
  color: #52c41a;
}

.status-badge.enabled {
  background-color: #f6ffed;
  color: #52c41a;
}

.status-badge.disabled {
  background-color: #fff1f0;
  color: #f5222d;
}

.default-badge {
  background-color: #fff7e6;
  color: #fa8c16;
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 操作按钮 */
.action-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 4px;
  background-color: transparent;
  color: #8c8c8c;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.action-btn:hover {
  background-color: #f5f5f5;
  color: #1890ff;
}

.action-btn.danger:hover {
  background-color: #fff1f0;
  color: #f5222d;
}

/* 卡片内容 */
.card-content {
  padding: 16px 20px;
}

.prompt-description {
  font-size: 14px;
  color: #595959;
  line-height: 1.5;
  margin: 0 0 16px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.prompt-footer {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

/* 开关样式 */
.toggle-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-toggle,
.default-toggle {
  position: relative;
  display: inline-block;
  width: 44px;
  height: 22px;
}

.toggle-input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: 0.3s;
  border-radius: 22px;
}

.toggle-slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 2px;
  bottom: 2px;
  background-color: white;
  transition: 0.3s;
  border-radius: 50%;
}

.toggle-input:checked + .toggle-slider {
  background-color: #1890ff;
}

.toggle-input:checked + .toggle-slider:before {
  transform: translateX(22px);
}

.toggle-label {
  font-size: 14px;
  color: #595959;
}

/* 模态框 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background-color: #ffffff;
  border-radius: 8px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.modal-large {
  max-width: 800px;
}

.modal-small {
  max-width: 400px;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  border-bottom: 1px solid #e8e8e8;
}

.modal-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #262626;
  margin: 0;
}

.close-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 4px;
  background-color: transparent;
  color: #8c8c8c;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.close-btn:hover {
  background-color: #f5f5f5;
  color: #262626;
}

.modal-body {
  padding: 20px;
}

.modal-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #e8e8e8;
}

/* 表单 */
.form-group {
  margin-bottom: 20px;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #262626;
  margin-bottom: 8px;
}

.form-input,
.form-textarea,
.form-select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.2s;
  font-family: inherit;
}

.form-input:focus,
.form-textarea:focus,
.form-select:focus {
  outline: none;
  border-color: #1890ff;
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

/* 上传区域 */
.upload-area {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.upload-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  border: 2px dashed #d9d9d9;
  border-radius: 4px;
  background-color: #fafafa;
  cursor: pointer;
  transition: all 0.2s;
}

.upload-box:hover {
  border-color: #1890ff;
  background-color: #e6f7ff;
}

.upload-box svg {
  color: #8c8c8c;
  margin-bottom: 12px;
}

.upload-text {
  font-size: 14px;
  font-weight: 500;
  color: #262626;
  margin: 0 0 4px 0;
}

.upload-hint {
  font-size: 12px;
  color: #8c8c8c;
  margin: 0;
}

.file-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.file-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background-color: #ffffff;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
}

.file-name {
  font-size: 14px;
  color: #262626;
}

.remove-btn {
  width: 24px;
  height: 24px;
  border: none;
  border-radius: 4px;
  background-color: transparent;
  color: #8c8c8c;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.remove-btn:hover {
  background-color: #fff1f0;
  color: #f5222d;
}

/* 预览iframe */
.preview-iframe {
  width: 100%;
  height: 600px;
  border: none;
}

.preview-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
}

.preview-empty p {
  font-size: 14px;
  color: #8c8c8c;
  margin: 0;
}

/* 确认文本 */
.confirm-text {
  font-size: 14px;
  color: #262626;
  margin: 0 0 8px 0;
  line-height: 1.5;
}

.warning-text {
  font-size: 14px;
  color: #f5222d;
  margin: 0;
}

/* 按钮样式 */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary {
  background-color: #1890ff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: #40a9ff;
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-secondary {
  background-color: #ffffff;
  border: 1px solid #d9d9d9;
  color: #595959;
}

.btn-secondary:hover {
  border-color: #1890ff;
  color: #1890ff;
}

.btn-danger {
  background-color: #f5222d;
  color: white;
}

.btn-danger:hover {
  background-color: #ff4d4f;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sys-prompt-view {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .header-actions {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
  }

  .search-input {
    width: 100%;
  }

  .header-actions .btn {
    width: 100%;
  }

  .prompt-list {
    grid-template-columns: 1fr;
  }

  .modal-content {
    width: 95%;
    max-height: 85vh;
  }

  .modal-large {
    max-width: 95%;
  }

  .modal-small {
    max-width: 95%;
  }
}
</style>