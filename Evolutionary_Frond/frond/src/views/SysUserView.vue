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
      <button class="btn btn-secondary" :disabled="currentPage === 1" @click="handlePageChange(currentPage - 1)">
        上一页
      </button>
      <span class="page-info">第 {{ currentPage }} 页 / 共 {{ totalPages }} 页（共 {{ total }} 条）</span>
      <button class="btn btn-secondary" :disabled="currentPage >= totalPages" @click="handlePageChange(currentPage + 1)">
        下一页
      </button>
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
          <div class="form-group">
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
import {
  getUserList,
  getUserById,
  addUser,
  updateUser,
  deleteUser,
  type SysUser,
  type UserAddData,
  type UserUpdateData
} from '@/utils/sysUserApi'

const loading = ref(false)
const users = ref<SysUser[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const showUserModal = ref(false)
const isEdit = ref(false)
const savingUser = ref(false)
const userForm = ref({
  id: '',
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  gender: 0,
  status: 1,
  remark: ''
})

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

const handlePageChange = (page: number) => {
  currentPage.value = page
  loadUsers()
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
    gender: user.gender || 0,
    status: user.status,
    remark: user.remark || ''
  }
  showUserModal.value = true
}

const handleSaveUser = async () => {
  if (!userForm.value.username) {
    alert('请填写用户名')
    return
  }
  if (!isEdit.value && !userForm.value.password) {
    alert('请填写密码')
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
        gender: userForm.value.gender,
        status: userForm.value.status,
        remark: userForm.value.remark
      }
      response = await addUser(addData)
    }

    if (response.code === 200) {
      closeUserModal()
      loadUsers()
    } else {
      alert(response.message || '操作失败')
    }
  } catch (error) {
    console.error('保存用户失败:', error)
    alert('保存失败')
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
    } else {
      alert(response.message || '删除失败')
    }
  } catch (error) {
    console.error('删除用户失败:', error)
    alert('删除失败')
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
.sys-user-view {
  padding: var(--spacing-xl);
  min-height: 100vh;
  background-color: var(--color-background);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text);
}

.search-area {
  background-color: #ffffff;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-lg);
}

.search-row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-md);
  align-items: flex-end;
}

.search-item {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.search-item label {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
}

.search-item input,
.search-item select {
  padding: var(--spacing-sm) var(--spacing-md);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 14px;
  min-width: 150px;
}

.search-item input:focus,
.search-item select:focus {
  border-color: var(--color-primary);
  outline: none;
}

.search-actions {
  display: flex;
  gap: var(--spacing-sm);
  margin-left: auto;
}

.user-table-container {
  background-color: #ffffff;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.user-table {
  width: 100%;
  border-collapse: collapse;
}

.user-table th {
  background-color: var(--color-background-soft);
  padding: var(--spacing-md);
  text-align: left;
  font-weight: 600;
  font-size: 14px;
  color: var(--color-text);
  border-bottom: 1px solid var(--color-border);
}

.user-table td {
  padding: var(--spacing-md);
  border-bottom: 1px solid var(--color-border);
  font-size: 14px;
  color: var(--color-text);
}

.user-table tr:hover {
  background-color: var(--color-background-soft);
}

.action-col {
  width: 80px;
}

.loading-cell,
.empty-cell {
  text-align: center;
  padding: var(--spacing-xl);
  color: var(--color-text-secondary);
}

.loading-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
}

.loading-spinner {
  width: 20px;
  height: 20px;
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

.status-tag {
  padding: 4px 8px;
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 500;
}

.status-tag.active {
  background-color: #d4edda;
  color: #155724;
}

.status-tag.inactive {
  background-color: #f8d7da;
  color: #721c24;
}

.btn-icon {
  padding: var(--spacing-xs);
  border-radius: var(--radius-sm);
  color: var(--color-text-secondary);
  transition: all 0.2s ease-out;
}

.btn-icon:hover {
  background-color: var(--color-background-soft);
  color: var(--color-text);
}

.btn-danger-icon:hover {
  background-color: #f8d7da;
  color: #721c24;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: var(--spacing-md);
  margin-top: var(--spacing-xl);
}

.page-info {
  font-size: 14px;
  color: var(--color-text-secondary);
}

/* 弹窗样式 */
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
  max-width: 500px;
  width: 90%;
}

.user-edit-modal {
  max-width: 560px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-lg);
  border-bottom: 1px solid var(--color-border);
}

.modal-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text);
}

.modal-close {
  padding: var(--spacing-sm);
  border-radius: var(--radius-sm);
  color: var(--color-text-secondary);
}

.modal-close:hover {
  background-color: var(--color-background-soft);
}

.modal-body {
  padding: var(--spacing-lg);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-sm);
  padding: var(--spacing-lg);
  border-top: 1px solid var(--color-border);
}

/* 表单样式 */
.form-group {
  margin-bottom: var(--spacing-md);
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
  margin-bottom: var(--spacing-xs);
}

.required {
  color: #dc3545;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: var(--spacing-sm) var(--spacing-md);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 14px;
}

.form-group input:disabled {
  background-color: var(--color-background-soft);
  color: var(--color-text-secondary);
  cursor: not-allowed;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  border-color: var(--color-primary);
  outline: none;
}

.form-group textarea {
  min-height: 80px;
  resize: vertical;
}
</style>