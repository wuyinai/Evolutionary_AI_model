<template>
  <div class="skill-management">
    <!-- Header Section -->
    <div class="page-header">
      <div class="header-content">
        <svg class="header-icon" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"></path>
        </svg>
        <h1 class="page-title">技能仓库</h1>
      </div>
      <div class="header-actions">
        <button class="btn btn-primary" @click="triggerUpload">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
            <polyline points="17 8 12 3 7 8"></polyline>
            <line x1="12" y1="3" x2="12" y2="15"></line>
          </svg>
          <span>上传技能包</span>
        </button>
        <input
          type="file"
          ref="fileInput"
          @change="handleFileSelect"
          accept=".zip"
          style="display: none"
        />
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
        <p class="banner-title">技能包规范</p>
        <p class="banner-text">上传ZIP格式的技能包，根目录必须包含SKILL.md文件（包含name、description等YAML头信息）。技能包将被解压存储，可随时启用/禁用。</p>
      </div>
    </div>

    <!-- Upload Drop Zone -->
    <div
      class="upload-drop-zone"
      @dragover.prevent="handleDragOver"
      @dragleave.prevent="handleDragLeave"
      @drop.prevent="handleFileDrop"
      :class="{ dragging: isDragging }"
    >
      <div class="drop-zone-content">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
          <polyline points="17 8 12 3 7 8"></polyline>
          <line x1="12" y1="3" x2="12" y2="15"></line>
        </svg>
        <p class="drop-zone-title">拖拽ZIP文件到此处上传</p>
        <p class="drop-zone-text">或点击上方按钮选择文件</p>
      </div>
    </div>

    <!-- Skill List -->
    <div class="skill-list-container">
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="skills.length === 0" class="empty-state">
        <svg class="empty-icon" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"></path>
        </svg>
        <p class="empty-title">暂无技能包</p>
        <p class="empty-text">上传ZIP格式的技能包开始使用</p>
      </div>

      <div v-else class="skill-list">
        <div
          v-for="skill in skills"
          :key="skill.id"
          class="skill-card"
        >
          <!-- Card Header -->
          <div class="card-header">
            <div class="skill-info">
              <h3 class="skill-name">{{ skill.displayName || skill.name }}</h3>
              <div class="skill-meta">
                <span class="version-badge" v-if="skill.version">v{{ skill.version }}</span>
                <span class="author-badge" v-if="skill.author">{{ skill.author }}</span>
              </div>
            </div>
            <div class="card-actions">
              <label class="toggle-switch">
                <input
                  type="checkbox"
                  :checked="skill.enabled"
                  @change="toggleSkillStatus(skill.id, !skill.enabled)"
                />
                <span class="toggle-slider"></span>
              </label>
              <button class="action-btn danger" @click="deleteSkill(skill.id)" title="删除">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6"></polyline>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                </svg>
              </button>
            </div>
          </div>

          <!-- Card Body -->
          <div class="card-body">
            <p class="skill-description">{{ skill.description }}</p>
            <div class="info-row">
              <label class="info-label">技能标识:</label>
              <span class="info-value">{{ skill.name }}</span>
            </div>
            <div class="info-row">
              <label class="info-label">状态:</label>
              <span class="status-badge" :class="skill.enabled ? 'enabled' : 'disabled'">
                {{ skill.enabled ? '已启用' : '已禁用' }}
              </span>
            </div>
            <div class="info-row">
              <label class="info-label">创建时间:</label>
              <span class="info-value">{{ formatDateTime(skill.createTime) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Upload Progress Modal -->
    <div v-if="uploading" class="modal-overlay">
      <div class="modal-content">
        <div class="modal-header">
          <h2>上传技能包</h2>
        </div>
        <div class="modal-body">
          <div class="upload-progress">
            <div class="loading-spinner"></div>
            <p>正在上传并处理技能包...</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useSkillStore } from '@/stores/skill'
import { useToast } from '@/composables/useToast'
import type { UserSkill } from '@/types/skill'

const skillStore = useSkillStore()
const toast = useToast()

const skills = computed(() => skillStore.skills)
const loading = computed(() => skillStore.loading)

const fileInput = ref<HTMLInputElement>()
const uploading = ref(false)
const isDragging = ref(false)

// 触发文件上传
const triggerUpload = () => {
  fileInput.value?.click()
}

// 处理拖拽悬停
const handleDragOver = () => {
  isDragging.value = true
}

// 处理拖拽离开
const handleDragLeave = () => {
  isDragging.value = false
}

// 处理文件选择
const handleFileSelect = async (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files.length > 0) {
    const file = target.files[0]
    if (!file.name.endsWith('.zip')) {
      toast.showWarning('只支持ZIP格式的技能包')
      return
    }

    // 校验文件名：不允许MinIO不支持的特殊字符
    const fileName = file.name.replace('.zip', '')
    if (!isValidFileName(fileName)) {
      toast.showWarning('文件名包含MinIO不支持的特殊字符（如空格、连字符、中文等），请使用字母、数字、下划线或连字符')
      return
    }
    
    await uploadSkillPackage(file)
    // 清空input
    if (fileInput.value) {
      fileInput.value.value = ''
    }
  }
}

// 处理文件拖放
const handleFileDrop = async (event: DragEvent) => {
  isDragging.value = false
  if (event.dataTransfer && event.dataTransfer.files.length > 0) {
    const file = event.dataTransfer.files[0]
    if (!file.name.endsWith('.zip')) {
      toast.showWarning('只支持ZIP格式的技能包')
      return
    }

    // 校验文件名：不允许MinIO不支持的特殊字符
    const fileName = file.name.replace('.zip', '')
    if (!isValidFileName(fileName)) {
      toast.showWarning('文件名包含MinIO不支持的特殊字符（如空格、连字符、中文等），请使用字母、数字、下划线或连字符')
      return
    }
    
    await uploadSkillPackage(file)
  }
}

// 校验文件名是否合法（MinIO不支持特殊字符）
const isValidFileName = (fileName: string): boolean => {
  // 只允许字母、数字、下划线、连字符
  const validPattern = /^[a-zA-Z0-9_-]+$/
  return validPattern.test(fileName)
}

// 上传技能包
const uploadSkillPackage = async (file: File) => {
  uploading.value = true
  try {
    const response = await skillStore.uploadSkillPackage(file)
    if (response.code === 200) {
      toast.showSuccess('技能包上传成功')
    } else {
      toast.showError('技能包上传失败: ' + response.message)
    }
  } catch (error) {
    console.error('上传技能包失败:', error)
    toast.showError('上传技能包失败')
  } finally {
    uploading.value = false
  }
}

// 切换技能状态
const toggleSkillStatus = async (skillId: string, enabled: boolean) => {
  try {
    await skillStore.toggleSkillStatus(skillId, enabled)
  } catch (error) {
    console.error('更新技能状态失败:', error)
    toast.showError('更新技能状态失败')
  }
}

// 删除技能
const deleteSkill = async (skillId: string) => {
  if (!confirm('确定要删除该技能包吗？此操作不可恢复。')) {
    return
  }

  try {
    await skillStore.removeSkill(skillId)
    toast.showSuccess('技能包删除成功')
  } catch (error) {
    console.error('删除技能失败:', error)
    toast.showError('删除技能失败')
  }
}

// 格式化日期时间
const formatDateTime = (dateTime: string): string => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  skillStore.loadSkills()
})
</script>

<style scoped>
.skill-management {
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
  color: #8b5cf6;
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
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.3);
}

.info-banner {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: #f3e8ff;
  border: 1px solid #8b5cf6;
  border-radius: 8px;
  margin-bottom: 24px;
}

.info-banner svg {
  color: #8b5cf6;
  flex-shrink: 0;
}

.banner-content {
  flex: 1;
}

.banner-title {
  font-weight: 600;
  color: #5b21b6;
  margin: 0 0 4px 0;
}

.banner-text {
  color: #6b21a8;
  margin: 0;
  font-size: 14px;
}

.upload-drop-zone {
  border: 2px dashed #d1d5db;
  border-radius: 12px;
  padding: 48px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 24px;
  background: white;
}

.upload-drop-zone:hover {
  border-color: #8b5cf6;
  background: #faf5ff;
}

.upload-drop-zone.dragging {
  border-color: #8b5cf6;
  background: #f3e8ff;
}

.drop-zone-content {
  color: #6b7280;
}

.drop-zone-content svg {
  color: #d1d5db;
  margin-bottom: 16px;
}

.drop-zone-title {
  font-size: 16px;
  font-weight: 600;
  color: #374151;
  margin: 0 0 8px 0;
}

.drop-zone-text {
  font-size: 14px;
  margin: 0;
}

.skill-list-container {
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
  border-top-color: #8b5cf6;
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

.skill-list {
  display: grid;
  gap: 16px;
}

.skill-card {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  transition: all 0.2s;
}

.skill-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.skill-info {
  flex: 1;
}

.skill-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.skill-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.version-badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  background: #dbeafe;
  color: #2563eb;
}

.author-badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  background: #f3f4f6;
  color: #6b7280;
}

.card-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.toggle-switch {
  position: relative;
  display: inline-block;
  width: 44px;
  height: 24px;
}

.toggle-switch input {
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
  background-color: #d1d5db;
  transition: 0.4s;
  border-radius: 24px;
}

.toggle-slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: 0.4s;
  border-radius: 50%;
}

input:checked + .toggle-slider {
  background-color: #8b5cf6;
}

input:checked + .toggle-slider:before {
  transform: translateX(20px);
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

.skill-description {
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

.status-badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.enabled {
  background: #d1fae5;
  color: #059669;
}

.status-badge.disabled {
  background: #f3f4f6;
  color: #6b7280;
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
  max-width: 400px;
}

.modal-header {
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.modal-body {
  padding: 24px;
}

.upload-progress {
  text-align: center;
}

.upload-progress p {
  margin-top: 16px;
  color: #6b7280;
}
</style>