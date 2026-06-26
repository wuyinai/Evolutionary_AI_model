<template>
  <div class="role-expert-view">
    <!-- Header Section -->
    <div class="page-header">
      <div class="header-content">
        <svg class="header-icon" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
          <circle cx="9" cy="7" r="4"></circle>
          <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
          <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
        </svg>
        <h1 class="page-title">AI角色专家</h1>
      </div>
      <div class="header-actions">
        <button class="btn btn-primary" @click="openCreateModal">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="12" y1="5" x2="12" y2="19"></line>
            <line x1="5" y1="12" x2="19" y2="12"></line>
          </svg>
          <span>创建角色</span>
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
        <p class="banner-title">AI角色系统</p>
        <p class="banner-text">创建AI角色 → 上传系统提示词文档 → 构建角色提示词。切换角色可动态改变AI的行为和身份。</p>
      </div>
    </div>

    <!-- Role List -->
    <div class="role-list-container">
      <div v-if="aiRoleStore.loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="aiRoleStore.roles.length === 0" class="empty-state">
        <svg class="empty-icon" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
          <circle cx="9" cy="7" r="4"></circle>
          <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
          <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
        </svg>
        <p class="empty-title">暂无AI角色</p>
        <p class="empty-text">点击上方按钮创建AI角色</p>
      </div>

      <div v-else class="role-list">
        <div
          v-for="role in aiRoleStore.roles"
          :key="role.id"
          class="role-card"
          :class="{ active: aiRoleStore.currentRole?.id === role.id }"
          @click="selectRole(role)"
        >
          <!-- Card Header -->
          <div class="card-header">
            <div class="role-info">
              <h3 class="role-name">{{ role.roleName }}</h3>
              <div class="role-meta">
                <span class="status-badge" :class="getStatusClass(role.status)">{{ getStatusText(role.status) }}</span>
                <span class="doc-count">{{ role.documents?.length || 0 }} 个文档</span>
              </div>
            </div>
            <div class="card-actions">
              <button class="action-btn" @click.stop="openUploadModal(role)" title="上传文档">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                  <polyline points="17 8 12 3 7 8"></polyline>
                  <line x1="12" y1="3" x2="12" y2="15"></line>
                </svg>
              </button>
              <button class="action-btn" @click.stop="buildPrompt(role.id)" title="构建提示词">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
                </svg>
              </button>
              <button class="action-btn" @click.stop="openEditModal(role)" title="编辑">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                </svg>
              </button>
              <button class="action-btn danger" @click.stop="confirmDeleteRole(role.id)" title="删除">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6"></polyline>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                </svg>
              </button>
            </div>
          </div>

          <!-- Card Body -->
          <div class="card-body">
            <p v-if="role.description" class="role-description">{{ role.description }}</p>
            <div class="info-row">
              <label class="info-label">角色代码:</label>
              <span class="info-value">{{ role.roleCode }}</span>
            </div>
            <div class="info-row">
              <label class="info-label">创建时间:</label>
              <span class="info-value">{{ formatDateTime(role.createTime) }}</span>
            </div>
          </div>

          <!-- Documents Section -->
          <div v-if="aiRoleStore.currentRole?.id === role.id && aiRoleStore.currentDocuments.length > 0" class="documents-section">
            <h4 class="documents-title">文档列表</h4>
            <div class="document-list-mini">
              <div v-for="doc in aiRoleStore.currentDocuments" :key="doc.id" class="document-item-mini">
                <span class="doc-name-mini">{{ doc.documentName }}</span>
                <span class="status-badge-mini" :class="getDocStatusClass(doc.status)">{{ getDocStatusText(doc.status) }}</span>
                <button class="action-btn-mini" @click.stop="previewDoc(doc.id)" title="预览">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                    <circle cx="12" cy="12" r="3"></circle>
                  </svg>
                </button>
                <button class="action-btn-mini danger" @click.stop="confirmDeleteDocument(doc.id)" title="删除">
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

    <!-- Create/Edit Role Modal -->
    <div v-if="showRoleModal" class="modal-overlay" @click="closeRoleModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>{{ isEditing ? '编辑角色' : '创建角色' }}</h2>
          <button class="close-btn" @click="closeRoleModal">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>

        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">角色名称 *</label>
            <input v-model="roleForm.roleName" type="text" class="form-input" placeholder="请输入角色名称" />
          </div>

          <div class="form-group">
            <label class="form-label">角色代码 *</label>
            <input v-model="roleForm.roleCode" type="text" class="form-input" placeholder="请输入角色唯一标识（如：assistant）" />
          </div>

          <div class="form-group">
            <label class="form-label">角色描述</label>
            <textarea v-model="roleForm.description" class="form-textarea" placeholder="请输入角色描述" rows="3"></textarea>
          </div>

          <div class="form-group">
            <label class="form-label">系统提示词（纯文本）</label>
            <textarea v-model="roleForm.systemPrompt" class="form-textarea" placeholder="请输入系统提示词内容（可选）" rows="5"></textarea>
          </div>

          <div class="form-group">
            <label class="form-label">系统提示词模板</label>
            <textarea v-model="roleForm.systemPromptTemplate" class="form-textarea" placeholder="支持变量替换：{role_name}、{documents}" rows="5"></textarea>
            <p class="form-hint">示例：你是{role_name}，拥有以下知识：{documents}</p>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeRoleModal">取消</button>
          <button class="btn btn-primary" @click="submitRoleForm" :disabled="!roleForm.roleName || !roleForm.roleCode">
            {{ isEditing ? '保存' : '创建' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Upload Document Modal -->
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
          <div class="upload-area">
            <input
              ref="fileInput"
              type="file"
              @change="handleFileChange"
              accept=".pdf,.doc,.docx,.txt,.md"
              multiple
              hidden
            />
            <div class="upload-box" @click="triggerFileInput">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                <polyline points="17 8 12 3 7 8"></polyline>
                <line x1="12" y1="3" x2="12" y2="15"></line>
              </svg>
              <p class="upload-text">点击上传文档</p>
              <p class="upload-hint">支持 PDF、Word、TXT、Markdown 格式</p>
            </div>
            <div v-if="uploadFiles.length > 0" class="file-list">
              <div v-for="(file, index) in uploadFiles" :key="index" class="file-item">
                <span class="file-name">{{ file.name }}</span>
                <button class="remove-btn" @click="removeFile(index)">
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
          <button class="btn btn-primary" @click="uploadDocuments" :disabled="uploadFiles.length === 0 || aiRoleStore.uploading">
            {{ aiRoleStore.uploading ? '上传中...' : '上传' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Preview Document Modal -->
    <div v-if="showPreviewModal" class="modal-overlay" @click="closePreviewModal">
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
          <div v-if="aiRoleStore.previewContent" class="preview-content">
            <div class="preview-header">
              <h3>{{ aiRoleStore.previewContent.documentName }}</h3>
            </div>
            <div class="preview-text">
              <pre>{{ aiRoleStore.previewContent.content }}</pre>
            </div>
          </div>
          <div v-else class="preview-empty">
            <p>加载预览内容...</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Built Prompt Modal -->
    <div v-if="showPromptModal" class="modal-overlay" @click="closePromptModal">
      <div class="modal-content modal-large" @click.stop>
        <div class="modal-header">
          <h2>构建的系统提示词</h2>
          <button class="close-btn" @click="closePromptModal">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>

        <div class="modal-body">
          <div v-if="aiRoleStore.builtSystemPrompt" class="prompt-content">
            <div class="prompt-text">
              <pre>{{ aiRoleStore.builtSystemPrompt }}</pre>
            </div>
          </div>
          <div v-else class="prompt-empty">
            <p>构建提示词失败，请检查角色配置</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <div v-if="showDeleteConfirm" class="modal-overlay" @click="closeDeleteConfirm">
      <div class="modal-content modal-small" @click.stop>
        <div class="modal-header">
          <h2>确认删除</h2>
          <button class="close-btn" @click="closeDeleteConfirm">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>

        <div class="modal-body">
          <p class="confirm-text">{{ deleteTarget === 'role' ? '确定要删除这个角色吗？相关的文档也会一起删除。' : '确定要删除这个文档吗？' }}</p>
        </div>

        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeDeleteConfirm">取消</button>
          <button class="btn btn-danger" @click="executeDelete">删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAiRoleStore } from '@/stores/aiRole'
import type { AiRole, CreateAiRoleDTO, UpdateAiRoleDTO } from '@/types/aiRole'

const aiRoleStore = useAiRoleStore()

// Modal状态
const showRoleModal = ref(false)
const showUploadModal = ref(false)
const showPreviewModal = ref(false)
const showPromptModal = ref(false)
const showDeleteConfirm = ref(false)

// 编辑状态
const isEditing = ref(false)

// 表单数据
const roleForm = ref<CreateAiRoleDTO>({
  roleName: '',
  roleCode: '',
  description: '',
  systemPrompt: '',
  systemPromptTemplate: ''
})

// 上传相关
const uploadFiles = ref<File[]>([])
const fileInput = ref<HTMLInputElement | null>(null)
const currentUploadRole = ref<AiRole | null>(null)

// 删除相关
const deleteTarget = ref<'role' | 'document'>('role')
const deleteId = ref<string>('')

// 生命周期
onMounted(() => {
  loadRoles()
})

// 加载角色列表
const loadRoles = async () => {
  try {
    const response = await aiRoleStore.loadRoles()
    console.log('角色列表加载结果:', response)
    console.log('角色数据:', aiRoleStore.roles)
    console.log('加载状态:', aiRoleStore.loading)
  } catch (error) {
    console.error('加载角色列表失败:', error)
    showErrorMessage('加载角色列表失败，请刷新页面重试')
  }
}

// 选择角色
const selectRole = async (role: AiRole) => {
  if (aiRoleStore.currentRole?.id === role.id) {
    // 取消选择
    aiRoleStore.currentRole = null
    aiRoleStore.currentDocuments = []
  } else {
    // 选择角色并加载文档
    aiRoleStore.currentRole = role
    try {
      await aiRoleStore.loadRoleDocuments(role.id)
    } catch (error) {
      console.error('加载文档列表失败:', error)
    }
  }
}

// 打开创建模态框
const openCreateModal = () => {
  isEditing.value = false
  roleForm.value = {
    roleName: '',
    roleCode: '',
    description: '',
    systemPrompt: '',
    systemPromptTemplate: ''
  }
  showRoleModal.value = true
}

// 打开编辑模态框
const openEditModal = (role: AiRole) => {
  isEditing.value = true
  roleForm.value = {
    roleName: role.roleName,
    roleCode: role.roleCode,
    description: role.description || '',
    systemPrompt: role.systemPrompt || '',
    systemPromptTemplate: role.systemPromptTemplate || ''
  }
  // 保存编辑的角色ID（需要在roleForm中添加id字段，这里临时处理）
  showRoleModal.value = true
}

// 关闭角色模态框
const closeRoleModal = () => {
  showRoleModal.value = false
}

// 提交角色表单
const submitRoleForm = async () => {
  try {
    let response
    if (isEditing.value && aiRoleStore.currentRole) {
      const updateData: UpdateAiRoleDTO = {
        id: aiRoleStore.currentRole.id,
        roleName: roleForm.value.roleName,
        description: roleForm.value.description,
        systemPrompt: roleForm.value.systemPrompt,
        systemPromptTemplate: roleForm.value.systemPromptTemplate
      }
      response = await aiRoleStore.updateRole(updateData)
    } else {
      response = await aiRoleStore.createRole(roleForm.value)
    }

    // 检查响应结果
    if (response && response.code === 200) {
      // 成功：关闭模态框并显示提示
      closeRoleModal()
      showSuccessMessage(isEditing.value ? '角色更新成功' : '角色创建成功')
    } else {
      // 失败：显示错误信息，保持模态框打开
      showErrorMessage(response?.message || '操作失败，请重试')
    }
  } catch (error) {
    console.error('操作失败:', error)
    showErrorMessage('操作失败，请检查网络连接')
  }
}

// 显示成功消息
const showSuccessMessage = (message: string) => {
  alert(message) // 可以替换为更优雅的toast组件
}

// 显示错误消息
const showErrorMessage = (message: string) => {
  alert(message) // 可以替换为更优雅的toast组件
}

// 打开上传模态框
const openUploadModal = (role: AiRole) => {
  currentUploadRole.value = role
  uploadFiles.value = []
  showUploadModal.value = true
}

// 关闭上传模态框
const closeUploadModal = () => {
  showUploadModal.value = false
  uploadFiles.value = []
  currentUploadRole.value = null
}

// 触发文件选择
const triggerFileInput = () => {
  fileInput.value?.click()
}

// 处理文件选择
const handleFileChange = (event: Event) => {
  const input = event.target as HTMLInputElement
  if (input.files) {
    const newFiles = Array.from(input.files)
    uploadFiles.value = [...uploadFiles.value, ...newFiles]
  }
  // 重置input，以便可以再次选择相同的文件
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

// 移除文件
const removeFile = (index: number) => {
  uploadFiles.value.splice(index, 1)
}

// 上传文档
const uploadDocuments = async () => {
  if (!currentUploadRole.value) return

  try {
    let successCount = 0
    let failCount = 0

    for (const file of uploadFiles.value) {
      const response = await aiRoleStore.uploadDocument(file, currentUploadRole.value.id)
      if (response && response.code === 200) {
        successCount++
      } else {
        failCount++
      }
    }

    // 关闭模态框并显示结果
    closeUploadModal()

    if (failCount === 0) {
      showSuccessMessage(`成功上传 ${successCount} 个文档`)
    } else {
      showErrorMessage(`上传完成：成功 ${successCount} 个，失败 ${failCount} 个`)
    }
  } catch (error) {
    console.error('上传失败:', error)
    showErrorMessage('上传失败，请检查网络连接')
  }
}

// 构建系统提示词
const buildPrompt = async (roleId: string) => {
  try {
    const response = await aiRoleStore.buildPrompt(roleId)

    // 检查响应结果
    if (response && response.code === 200 && aiRoleStore.builtSystemPrompt) {
      showPromptModal.value = true
    } else {
      showErrorMessage(response?.message || '构建提示词失败，请检查角色配置')
    }
  } catch (error) {
    console.error('构建提示词失败:', error)
    showErrorMessage('构建提示词失败，请检查网络连接')
  }
}

// 关闭提示词模态框
const closePromptModal = () => {
  showPromptModal.value = false
  aiRoleStore.clearBuiltPrompt()
}

// 预览文档
const previewDoc = async (documentId: string) => {
  try {
    const response = await aiRoleStore.previewDoc(documentId)

    // 检查响应结果
    if (response && response.code === 200 && aiRoleStore.previewContent) {
      showPreviewModal.value = true
    } else {
      showErrorMessage(response?.message || '文档预览失败')
    }
  } catch (error) {
    console.error('预览失败:', error)
    showErrorMessage('文档预览失败，请检查网络连接')
  }
}

// 关闭预览模态框
const closePreviewModal = () => {
  showPreviewModal.value = false
  aiRoleStore.clearPreview()
}

// 确认删除角色
const confirmDeleteRole = (roleId: string) => {
  deleteTarget.value = 'role'
  deleteId.value = roleId
  showDeleteConfirm.value = true
}

// 确认删除文档
const confirmDeleteDocument = (documentId: string) => {
  deleteTarget.value = 'document'
  deleteId.value = documentId
  showDeleteConfirm.value = true
}

// 关闭删除确认
const closeDeleteConfirm = () => {
  showDeleteConfirm.value = false
  deleteId.value = ''
}

// 执行删除
const executeDelete = async () => {
  try {
    let response
    if (deleteTarget.value === 'role') {
      response = await aiRoleStore.deleteRole(deleteId.value)
    } else {
      response = await aiRoleStore.deleteDocument(deleteId.value)
    }

    // 检查响应结果
    if (response && response.code === 200) {
      closeDeleteConfirm()
      showSuccessMessage(deleteTarget.value === 'role' ? '角色删除成功' : '文档删除成功')
    } else {
      showErrorMessage(response?.message || '删除失败，请重试')
    }
  } catch (error) {
    console.error('删除失败:', error)
    showErrorMessage('删除失败，请检查网络连接')
  }
}

// 格式化日期时间
const formatDateTime = (dateTime: string): string => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取状态文本（文档状态）
const getDocStatusText = (status: number | string): string => {
  const statusMap: Record<string, string> = {
    '0': '待处理',
    '1': '处理中',
    '2': '已完成',
    '3': '失败',
    'PENDING': '待处理',
    'PROCESSING': '处理中',
    'COMPLETED': '已完成',
    'FAILED': '失败'
  }
  return statusMap[String(status)] || String(status)
}

// 获取角色状态文本
const getStatusText = (status: number | string): string => {
  // 后端返回：0-禁用，1-启用
  if (typeof status === 'number') {
    return status === 1 ? '活跃' : '停用'
  }
  return status === 'ACTIVE' ? '活跃' : '停用'
}

// 获取角色状态CSS类名
const getStatusClass = (status: number | string): string => {
  // 后端返回：0-禁用，1-启用
  if (typeof status === 'number') {
    return status === 1 ? 'active' : 'inactive'
  }
  return status === 'ACTIVE' ? 'active' : 'inactive'
}

// 获取文档状态CSS类名
const getDocStatusClass = (status: number | string): string => {
  const statusStr = String(status)
  const classMap: Record<string, string> = {
    '0': 'pending',
    '1': 'processing',
    '2': 'completed',
    '3': 'failed',
    'PENDING': 'pending',
    'PROCESSING': 'processing',
    'COMPLETED': 'completed',
    'FAILED': 'failed'
  }
  return classMap[statusStr] || 'pending'
}
</script>

<style scoped>
/* 主容器 */
.role-expert-view {
  width: 100%;
  height: 100vh;
  overflow-y: auto;
  padding: 24px;
  background-color: #fafafa;
}

/* 页面头部 */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--color-border);
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  color: var(--color-primary);
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: var(--color-text);
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* 信息横幅 */
.info-banner {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background-color: #e3f2fd;
  border: 1px solid #90caf9;
  border-radius: var(--radius-md);
  margin-bottom: 24px;
}

.info-banner svg {
  color: #1976d2;
  flex-shrink: 0;
}

.banner-content {
  flex: 1;
}

.banner-title {
  font-size: 14px;
  font-weight: 600;
  color: #1976d2;
  margin: 0 0 4px 0;
}

.banner-text {
  font-size: 13px;
  color: #424242;
  margin: 0;
  line-height: 1.5;
}

/* 角色列表容器 */
.role-list-container {
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
  border: 3px solid var(--color-border);
  border-top-color: var(--color-primary);
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
  color: var(--color-text-secondary);
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
  color: #d1d5db;
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text);
  margin: 0;
}

.empty-text {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0;
}

/* 角色列表 */
.role-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 20px;
}

/* 角色卡片 */
.role-card {
  background-color: #ffffff;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: all 0.2s;
  cursor: pointer;
}

.role-card:hover {
  border-color: var(--color-primary);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.role-card.active {
  border-color: var(--color-primary);
  border-width: 2px;
  background-color: #f0f7ff;
}

/* 卡片头部 */
.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 20px;
  border-bottom: 1px solid var(--color-border);
}

.role-info {
  flex: 1;
  min-width: 0;
}

.role-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text);
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.role-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.status-badge {
  padding: 4px 10px;
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 500;
}

.status-badge.active {
  background-color: #c8e6c9;
  color: #2e7d32;
}

.status-badge.inactive {
  background-color: #ffcdd2;
  color: #c62828;
}

.doc-count {
  font-size: 12px;
  color: var(--color-text-secondary);
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
  border-radius: var(--radius-sm);
  background-color: transparent;
  color: var(--color-text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.action-btn:hover {
  background-color: var(--color-background-soft);
  color: var(--color-primary);
}

.action-btn.danger:hover {
  background-color: #ffebee;
  color: #c62828;
}

/* 卡片主体 */
.card-body {
  padding: 16px 20px;
}

.role-description {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0 0 12px 0;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-label {
  font-size: 13px;
  color: var(--color-text-tertiary);
  margin-right: 8px;
  flex-shrink: 0;
}

.info-value {
  font-size: 13px;
  color: var(--color-text-secondary);
}

/* 文档区域 */
.documents-section {
  padding: 16px 20px;
  background-color: #f8f9fa;
  border-top: 1px solid var(--color-border);
}

.documents-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
  margin: 0 0 12px 0;
}

.document-list-mini {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.document-item-mini {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background-color: #ffffff;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
}

.doc-name-mini {
  flex: 1;
  font-size: 13px;
  color: var(--color-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-badge-mini {
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  font-size: 11px;
  font-weight: 500;
}

.status-badge-mini.pending {
  background-color: #fff3cd;
  color: #856404;
}

.status-badge-mini.processing {
  background-color: #cce5ff;
  color: #004085;
}

.status-badge-mini.completed {
  background-color: #d4edda;
  color: #155724;
}

.status-badge-mini.failed {
  background-color: #f8d7da;
  color: #721c24;
}

.action-btn-mini {
  width: 24px;
  height: 24px;
  border: none;
  border-radius: var(--radius-sm);
  background-color: transparent;
  color: var(--color-text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.action-btn-mini:hover {
  background-color: var(--color-background-soft);
  color: var(--color-primary);
}

.action-btn-mini.danger:hover {
  background-color: #ffebee;
  color: #c62828;
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
  border-radius: var(--radius-lg);
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
  border-bottom: 1px solid var(--color-border);
}

.modal-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text);
  margin: 0;
}

.close-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: var(--radius-sm);
  background-color: transparent;
  color: var(--color-text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.close-btn:hover {
  background-color: var(--color-background-soft);
  color: var(--color-text);
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
  border-top: 1px solid var(--color-border);
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
  color: var(--color-text);
  margin-bottom: 8px;
}

.form-input,
.form-textarea,
.form-select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 14px;
  transition: border-color 0.2s;
  font-family: inherit;
}

.form-input:focus,
.form-textarea:focus,
.form-select:focus {
  outline: none;
  border-color: var(--color-primary);
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.form-hint {
  font-size: 12px;
  color: var(--color-text-tertiary);
  margin: 4px 0 0 0;
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
  border: 2px dashed var(--color-border);
  border-radius: var(--radius-md);
  background-color: #fafafa;
  cursor: pointer;
  transition: all 0.2s;
}

.upload-box:hover {
  border-color: var(--color-primary);
  background-color: #f0f7ff;
}

.upload-box svg {
  color: var(--color-text-secondary);
  margin-bottom: 12px;
}

.upload-text {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
  margin: 0 0 4px 0;
}

.upload-hint {
  font-size: 12px;
  color: var(--color-text-tertiary);
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
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
}

.file-name {
  font-size: 14px;
  color: var(--color-text);
}

.remove-btn {
  width: 24px;
  height: 24px;
  border: none;
  border-radius: var(--radius-sm);
  background-color: transparent;
  color: var(--color-text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.remove-btn:hover {
  background-color: #ffebee;
  color: #c62828;
}

/* 预览内容 */
.preview-content {
  height: 100%;
}

.preview-header {
  padding-bottom: 12px;
  border-bottom: 1px solid var(--color-border);
}

.preview-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text);
  margin: 0;
}

.preview-text {
  height: calc(100% - 60px);
  overflow-y: auto;
}

.preview-text pre {
  margin: 0;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: var(--radius-md);
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: 'Courier New', Courier, monospace;
}

.preview-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
}

.preview-empty p {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0;
}

/* 提示词内容 */
.prompt-content {
  height: 100%;
}

.prompt-text {
  height: 100%;
  overflow-y: auto;
}

.prompt-text pre {
  margin: 0;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: var(--radius-md);
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: 'Courier New', Courier, monospace;
}

.prompt-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
}

.prompt-empty p {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0;
}

/* 确认文本 */
.confirm-text {
  font-size: 14px;
  color: var(--color-text);
  margin: 0;
  line-height: 1.5;
}

/* 按钮样式 */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 20px;
  border: none;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary {
  background-color: var(--color-primary);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: #0066cc;
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-secondary {
  background-color: #ffffff;
  border: 1px solid var(--color-border);
  color: var(--color-text-secondary);
}

.btn-secondary:hover {
  background-color: var(--color-background-soft);
  border-color: var(--color-text-secondary);
}

.btn-danger {
  background-color: #dc3545;
  color: white;
}

.btn-danger:hover {
  background-color: #c82333;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .role-expert-view {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .header-actions {
    width: 100%;
  }

  .header-actions .btn {
    width: 100%;
  }

  .role-list {
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