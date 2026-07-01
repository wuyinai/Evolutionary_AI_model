<template>
  <div class="sys-user-view">
    <div class="page-header">
      <h1 class="page-title">用户管理</h1>
      <div class="header-actions">
        <button class="btn btn-primary" @click="openAddModal">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="12" y1="5" x2="12" y2="19"></line>
            <line x1="5" y1="12" x2="19" y2="12"></line>
          </svg>
          <span>添加用户</span>
        </button>
      </div>
    </div>

    <!-- 查询区域 -->
    <div class="search-area">
      <div class="search-row">
        <div class="search-item">
          <label>用户名</label>
          <input type="text" v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </div>
        <div class="search-item">
          <label>真实姓名</label>
          <input type="text" v-model="searchForm.realName" placeholder="请输入真实姓名" clearable />
        </div>
        <div class="search-item">
          <label>手机号</label>
          <input type="text" v-model="searchForm.phone" placeholder="请输入手机号" clearable />
        </div>
        <div class="search-item">
          <label>状态</label>
          <select v-model="searchForm.status">
            <option value="">全部</option>
            <option value="1">正常</option>
            <option value="0">禁用</option>
          </select>
        </div>
        <div class="search-actions">
          <button class="btn btn-primary" @click="handleSearch">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"></circle>
              <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
            </svg>
            <span>查询</span>
          </button>
          <button class="btn btn-secondary" @click="handleReset">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"></path>
              <path d="M3 3v5h5"></path>
            </svg>
            <span>重置</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 用户列表 -->
    <div class="user-table-container">
      <table class="user-table">
        <thead>
          <tr>
            <th>用户名</th>
            <th>真实姓名</th>
            <th>邮箱</th>
            <th>手机号</th>
            <th>性别</th>
            <th>状态</th>
            <th>备注</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="9" class="loading-cell">
              <div class="loading-spinner"></div>
              <span>加载中...</span>
            </td>
          </tr>
          <tr v-else-if="filteredUsers.length === 0">
            <td colspan="9" class="empty-cell">暂无用户数据</td>
          </tr>
          <tr v-else v-for="user in filteredUsers" :key="user.id">
            <td>{{ user.username }}</td>
            <td>{{ user.realName || '-' }}</td>
            <td>{{ user.email || '-' }}</td>
            <td>{{ user.phone || '-' }}</td>
            <td>{{ formatGender(user.gender) }}</td>
            <td>
              <span class="status-tag" :class="user.status === 1 ? 'active' : 'inactive'">
                {{ user.status === 1 ? '正常' : '禁用' }}
              </span>
            </td>
            <td>{{ user.remark || '-' }}</td>
            <td>{{ formatTime(user.createTime) }}</td>
            <td class="action-col">
              <button class="btn-icon" @click="openEditModal(user)" title="编辑">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                </svg>
              </button>
              <button class="btn-icon btn-danger-icon" @click="handleDelete(user.id)" title="删除">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6"></polyline>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                </svg>
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <div class="page-size-select">
        <span>每页</span>
        <select v-model.number="pageSize" @change="handlePageSizeChange">
          <option :value="10">10</option>
          <option :value="20">20</option>
          <option :value="50">50</option>
          <option :value="100">100</option>
        </select>
        <span>条</span>
      </div>
      <span class="page-info">共 {{ total }} 条</span>
      <button class="btn btn-secondary" :disabled="currentPage === 1" @click="handlePageChange(currentPage - 1)">
        上一页
      </button>
      <span class="page-info">第 {{ currentPage }} / {{ totalPages }} 页</span>
      <button class="btn btn-secondary" :disabled="currentPage >= totalPages" @click="handlePageChange(currentPage + 1)">
        下一页
      </button>
      <div class="page-jump">
        <span>跳至</span>
        <input type="number" v-model.number="jumpPage" :min="1" :max="totalPages" @keyup.enter="handleJumpPage" />
        <span>页</span>
        <button class="btn btn-secondary btn-sm" @click="handleJumpPage">跳转</button>
      </div>
    </div>

    <!-- 添加/编辑用户弹窗 -->
    <div v-if="showUserModal" class="modal-overlay" @click="closeUserModal">
      <div class="modal-content user-edit-modal" @click.stop>
        <div class="modal-header">
          <h2>{{ isEdit ? '编辑用户' : '添加用户' }}</h2>
          <button class="modal-close" @click="closeUserModal">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="form-grid">
            <!-- 左侧：头像 -->
            <div class="form-group form-avatar-group">
              <label>头像</label>
              <div class="avatar-upload">
                <div class="avatar-preview">
                  <img v-if="userForm.avatar" :src="userForm.avatar" alt="头像预览" class="avatar-img" />
                  <div v-else class="avatar-placeholder">
                    <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                      <circle cx="12" cy="7" r="4"></circle>
                    </svg>
                    <span>点击上传头像</span>
                  </div>
                </div>
                <input
                  type="file"
                  ref="avatarInputRef"
                  accept="image/jpeg,image/png,image/gif"
                  style="display: none"
                  @change="handleAvatarChange"
                />
                <button type="button" class="btn btn-secondary btn-sm" @click="triggerAvatarUpload">
                  {{ userForm.avatar ? '更换头像' : '选择文件' }}
                </button>
                <button v-if="userForm.avatar" type="button" class="btn btn-text btn-sm" @click="removeAvatar">移除</button>
              </div>
            </div>

            <!-- 右侧：字段区 -->
            <div class="form-fields-grid">
              <div class="form-group">
                <label>用户名 <span class="required">*</span></label>
                <input type="text" v-model="userForm.username" placeholder="请输入用户名（3-20个字符）" :disabled="isEdit" />
              </div>
              <div v-if="!isEdit" class="form-group">
                <label>密码 <span class="required">*</span></label>
                <input type="password" v-model="userForm.password" placeholder="请输入密码（6-20个字符，需包含字母和数字）" />
              </div>
              <div class="form-group">
                <label>真实姓名</label>
                <input type="text" v-model="userForm.realName" placeholder="请输入真实姓名" />
              </div>
              <div class="form-group">
                <label>邮箱</label>
                <input type="email" v-model="userForm.email" placeholder="请输入邮箱" />
              </div>
              <div class="form-group">
                <label>手机号</label>
                <input type="text" v-model="userForm.phone" placeholder="请输入手机号" />
              </div>
              <div class="form-group">
                <label>性别</label>
                <select v-model="userForm.gender">
                  <option :value="0">未知</option>
                  <option :value="1">男</option>
                  <option :value="2">女</option>
                </select>
              </div>
              <div class="form-group">
                <label>状态</label>
                <select v-model="userForm.status">
                  <option :value="1">正常</option>
                  <option :value="0">禁用</option>
                </select>
              </div>
            </div>
          </div>
          <div class="form-group form-group-full">
            <label>备注</label>
            <textarea v-model="userForm.remark" placeholder="请输入备注"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeUserModal">取消</button>
          <button class="btn btn-primary" @click="handleSaveUser" :disabled="savingUser">
            {{ savingUser ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useToast } from '@/composables/useToast'
import {
  getUserList,
  getUserById,
  addUser,
  updateUser,
  deleteUser,
  uploadAvatar,
  type SysUser,
  type UserAddData,
  type UserUpdateData
} from '@/utils/sysUserApi'

const { showSuccess, showError, showWarning } = useToast()

const loading = ref(false)
const users = ref<SysUser[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const jumpPage = ref(1)

const showUserModal = ref(false)
const isEdit = ref(false)
const savingUser = ref(false)
const uploadingAvatar = ref(false)
const avatarInputRef = ref<HTMLInputElement>()
const userForm = ref({
  id: '',
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  avatar: '',
  gender: 0,
  status: 1,
  remark: ''
})

/**
 * 触发头像文件选择
 */
const triggerAvatarUpload = () => {
  avatarInputRef.value?.click()
}

/**
 * 处理头像文件选择
 */
const handleAvatarChange = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  // 校验文件大小（最大2MB）
  if (file.size > 2 * 1024 * 1024) {
    showWarning('头像文件大小不能超过2MB')
    return
  }

  uploadingAvatar.value = true
  try {
    const response = await uploadAvatar(file)
    if (response.code === 200 && response.data) {
      userForm.value.avatar = response.data
      showSuccess('头像上传成功')
    } else {
      showError(response.message || '头像上传失败')
    }
  } catch (error) {
    showError('头像上传失败')
  } finally {
    uploadingAvatar.value = false
    // 清空input，允许重复选择同一文件
    input.value = ''
  }
}

/**
 * 移除头像
 */
const removeAvatar = () => {
  userForm.value.avatar = ''
}

const searchForm = ref({
  username: '',
  realName: '',
  phone: '',
  status: ''
})

const totalPages = computed(() => Math.ceil(total.value / pageSize.value) || 1)

const filteredUsers = computed(() => {
  let result = users.value
  if (searchForm.value.username) {
    result = result.filter(u => u.username.includes(searchForm.value.username))
  }
  if (searchForm.value.realName) {
    result = result.filter(u => u.realName && u.realName.includes(searchForm.value.realName))
  }
  if (searchForm.value.phone) {
    result = result.filter(u => u.phone && u.phone.includes(searchForm.value.phone))
  }
  if (searchForm.value.status !== '') {
    result = result.filter(u => u.status === Number(searchForm.value.status))
  }
  return result
})

onMounted(() => {
  loadUsers()
})

const loadUsers = async () => {
  loading.value = true
  try {
    const response = await getUserList(currentPage.value, pageSize.value)
    if (response.code === 200 && response.data) {
      users.value = response.data.records
      total.value = response.data.total
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handlePageSizeChange = () => {
  currentPage.value = 1
  jumpPage.value = 1
  loadUsers()
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  jumpPage.value = page
  loadUsers()
}

const handleJumpPage = () => {
  if (jumpPage.value >= 1 && jumpPage.value <= totalPages.value) {
    currentPage.value = jumpPage.value
    loadUsers()
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadUsers()
}

const handleReset = () => {
  searchForm.value = {
    username: '',
    realName: '',
    phone: '',
    status: ''
  }
  currentPage.value = 1
  loadUsers()
}

const openAddModal = () => {
  isEdit.value = false
  userForm.value = {
    id: '',
    username: '',
    password: '',
    realName: '',
    email: '',
    phone: '',
    avatar: '',
    gender: 0,
    status: 1,
    remark: ''
  }
  showUserModal.value = true
}

const openEditModal = async (user: SysUser) => {
  isEdit.value = true
  userForm.value = {
    id: user.id,
    username: user.username,
    password: '',
    realName: user.realName || '',
    email: user.email || '',
    phone: user.phone || '',
    avatar: user.avatar || '',
    gender: user.gender || 0,
    status: user.status,
    remark: user.remark || ''
  }
  showUserModal.value = true
}

const handleSaveUser = async () => {
  if (!userForm.value.username) {
    showWarning('请填写用户名')
    return
  }
  if (!isEdit.value && !userForm.value.password) {
    showWarning('请填写密码')
    return
  }

  savingUser.value = true
  try {
    let response
    if (isEdit.value) {
      const updateData: UserUpdateData = {
        id: userForm.value.id,
        realName: userForm.value.realName,
        email: userForm.value.email,
        phone: userForm.value.phone,
        avatar: userForm.value.avatar || undefined,
        gender: userForm.value.gender,
        status: userForm.value.status,
        remark: userForm.value.remark
      }
      response = await updateUser(updateData)
    } else {
      const addData: UserAddData = {
        username: userForm.value.username,
        password: userForm.value.password,
        realName: userForm.value.realName,
        email: userForm.value.email,
        phone: userForm.value.phone,
        avatar: userForm.value.avatar || undefined,
        gender: userForm.value.gender,
        status: userForm.value.status,
        remark: userForm.value.remark
      }
      response = await addUser(addData)
    }

    if (response.code === 200) {
      closeUserModal()
      loadUsers()
      showSuccess(isEdit.value ? '用户修改成功' : '用户添加成功')
    } else {
      showError(response.message || '操作失败')
    }
  } catch (error) {
    console.error('保存用户失败:', error)
    showError('保存失败')
  } finally {
    savingUser.value = false
  }
}

const closeUserModal = () => {
  showUserModal.value = false
}

const handleDelete = async (userId: string) => {
  if (!confirm('确定要删除这个用户吗？')) return
  try {
    const response = await deleteUser(userId)
    if (response.code === 200) {
      loadUsers()
      showSuccess('用户删除成功')
    } else {
      showError(response.message || '删除失败')
    }
  } catch (error) {
    console.error('删除用户失败:', error)
    showError('删除失败')
  }
}

const formatGender = (gender?: number) => {
  if (gender === 1) return '男'
  if (gender === 2) return '女'
  return '未知'
}

const formatTime = (time?: string) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
/* ========== 页面布局 ========== */
.sys-user-view {
  padding: 24px 32px;
  min-height: 100vh;
  background-color: #f5f7fa;
}

/* ========== 页面头部 ========== */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #1a1a2e;
  letter-spacing: 0.5px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* ========== 查询区域 ========== */
.search-area {
  background-color: #ffffff;
  border-radius: 12px;
  padding: 20px 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.search-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: flex-end;
}

.search-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.search-item label {
  font-size: 13px;
  font-weight: 500;
  color: #5c5c7a;
}

.search-item input,
.search-item select {
  height: 36px;
  padding: 0 12px;
  border: 1px solid #e0e0e8;
  border-radius: 8px;
  font-size: 14px;
  color: #1a1a2e;
  min-width: 160px;
  background-color: #ffffff;
  transition: border-color 0.2s;
}

.search-item input:hover,
.search-item select:hover {
  border-color: #c0c0c8;
}

.search-item input:focus,
.search-item select:focus {
  border-color: #4a7cf7;
  outline: none;
}

.search-actions {
  display: flex;
  gap: 8px;
  margin-left: auto;
}

/* ========== 表格容器 ========== */
.user-table-container {
  background-color: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

/* ========== 表格样式 ========== */
.user-table {
  width: 100%;
  border-collapse: collapse;
}

.user-table th {
  background-color: #fafafc;
  padding: 14px 16px;
  text-align: left;
  font-weight: 600;
  font-size: 14px;
  color: #5c5c7a;
  border-bottom: 1px solid #e8e8f0;
}

.user-table td {
  padding: 14px 16px;
  border-bottom: 1px solid #f0f0f5;
  font-size: 14px;
  color: #1a1a2e;
}

.user-table tbody tr:last-child td {
  border-bottom: none;
}

.user-table tbody tr:hover {
  background-color: #fafafc;
}

.action-col {
  width: 100px;
}

/* ========== 加载与空状态 ========== */
.loading-cell,
.empty-cell {
  text-align: center;
  padding: 48px 16px;
  color: #8a8aa0;
}

.loading-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid #e0e0e8;
  border-top-color: #4a7cf7;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* ========== 状态标签 ========== */
.status-tag {
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}

.status-tag.active {
  background-color: #e8f5e9;
  color: #2e7d32;
}

.status-tag.inactive {
  background-color: #ffebee;
  color: #c62828;
}

/* ========== 操作按钮 ========== */
.btn-icon {
  width: 32px;
  height: 32px;
  padding: 6px;
  border-radius: 6px;
  color: #8a8aa0;
  background-color: transparent;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.btn-icon:hover {
  background-color: #f0f0f5;
  color: #1a1a2e;
}

.btn-danger-icon:hover {
  background-color: #ffebee;
  color: #c62828;
}

/* ========== 分页样式 ========== */
.pagination {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  padding: 16px 24px;
  background-color: #ffffff;
  border-radius: 12px;
  margin-top: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.page-info {
  font-size: 14px;
  color: #5c5c7a;
}

.page-size-select {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-right: 4px;
}

.page-size-select span {
  font-size: 14px;
  color: #5c5c7a;
  white-space: nowrap;
}

.page-size-select select {
  height: 32px;
  padding: 0 8px;
  border: 1px solid #e0e0e8;
  border-radius: 6px;
  font-size: 14px;
  color: #1a1a2e;
  background-color: #ffffff;
  cursor: pointer;
  transition: border-color 0.2s;
  min-width: 64px;
}

.page-size-select select:hover {
  border-color: #c0c0c8;
}

.page-size-select select:focus {
  border-color: #4a7cf7;
  outline: none;
}

.page-jump {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-jump span {
  font-size: 14px;
  color: #5c5c7a;
}

.page-jump input {
  width: 56px;
  height: 32px;
  padding: 0 8px;
  border: 1px solid #e0e0e8;
  border-radius: 6px;
  font-size: 14px;
  text-align: center;
  color: #1a1a2e;
  transition: border-color 0.2s;
}

.page-jump input:hover {
  border-color: #c0c0c8;
}

.page-jump input:focus {
  border-color: #4a7cf7;
  outline: none;
}

/* ========== 弹窗样式 ========== */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(26, 26, 46, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background-color: #ffffff;
  border-radius: 16px;
  width: 90%;
  max-width: 520px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
}

.user-edit-modal {
  max-width: 640px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f5;
}

.modal-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
}

.modal-close {
  width: 36px;
  height: 36px;
  padding: 8px;
  border-radius: 8px;
  color: #8a8aa0;
  background-color: transparent;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-close:hover {
  background-color: #f0f0f5;
  color: #1a1a2e;
}

.modal-body {
  padding: 24px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid #f0f0f5;
}

/* ========== 表单样式 ========== */
.form-grid {
  display: flex;
  gap: 24px;
}

.form-avatar-group {
  flex-shrink: 0;
  width: 160px;
}

.form-fields-grid {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 16px;
}

.form-fields-grid .form-group:nth-last-child(2) {
  margin-bottom: 0;
}

.form-group-full {
  margin-top: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #1a1a2e;
  margin-bottom: 8px;
}

.required {
  color: #c62828;
  margin-left: 2px;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  height: 40px;
  padding: 0 12px;
  border: 1px solid #e0e0e8;
  border-radius: 8px;
  font-size: 14px;
  color: #1a1a2e;
  background-color: #ffffff;
  transition: border-color 0.2s;
}

.form-group textarea {
  height: auto;
  min-height: 80px;
  padding: 12px;
  resize: vertical;
}

.form-group input:hover,
.form-group select:hover,
.form-group textarea:hover {
  border-color: #c0c0c8;
}

.form-group input:disabled {
  background-color: #fafafc;
  color: #8a8aa0;
  cursor: not-allowed;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  border-color: #4a7cf7;
  outline: none;
}

/* ========== 头像上传 ========== */
.avatar-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.avatar-preview {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px dashed #e0e0e8;
  cursor: pointer;
  transition: border-color 0.2s;
  position: relative;
}

.avatar-preview:hover {
  border-color: #4a7cf7;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #8a8aa0;
  font-size: 12px;
  background-color: #fafafc;
}

.btn-text {
  background: none;
  border: none;
  color: #c62828;
  cursor: pointer;
  font-size: 13px;
  padding: 0 4px;
  height: auto;
}

.btn-text:hover {
  text-decoration: underline;
}

/* ========== 按钮全局样式 ========== */
.btn {
  height: 36px;
  padding: 0 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.2s;
  cursor: pointer;
}

.btn-sm {
  height: 32px;
  padding: 0 12px;
  font-size: 13px;
}

.btn-primary {
  background-color: #4a7cf7;
  color: #ffffff;
  border: none;
}

.btn-primary:hover {
  background-color: #3a6ce7;
}

.btn-secondary {
  background-color: #ffffff;
  color: #5c5c7a;
  border: 1px solid #e0e0e8;
}

.btn-secondary:hover {
  background-color: #fafafc;
  border-color: #c0c0c8;
}

.btn-secondary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-danger {
  background-color: #c62828;
  color: #ffffff;
  border: none;
}

.btn-danger:hover {
  background-color: #b71c1c;
}
</style>