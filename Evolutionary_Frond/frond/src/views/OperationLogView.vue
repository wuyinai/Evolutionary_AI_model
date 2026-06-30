<template>
  <div class="operation-log-view">
    <div class="page-header">
      <h1 class="page-title">操作日志管理</h1>
      <div class="header-actions">
        <button class="btn btn-danger" @click="handleClearAll" :disabled="loading || logs.length === 0">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="3 6 5 6 21 6"></polyline>
            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
          </svg>
          <span>清空日志</span>
        </button>
        <button class="btn btn-primary" @click="loadLogs">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="23 4 23 10 17 10"></polyline>
            <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"></path>
          </svg>
          <span>刷新</span>
        </button>
      </div>
    </div>

    <!-- 日志列表 -->
    <div class="log-table-container">
      <table class="log-table">
        <thead>
          <tr>
            <th class="checkbox-col">
              <input type="checkbox" v-model="selectAll" @change="handleSelectAll" />
            </th>
            <th>用户名</th>
            <th>操作描述</th>
            <th>请求URL</th>
            <th>耗时(ms)</th>
            <th>IP地址</th>
            <th>浏览器</th>
            <th>操作系统</th>
            <th>操作状态</th>
            <th>操作时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="11" class="loading-cell">
              <div class="loading-spinner"></div>
              <span>加载中...</span>
            </td>
          </tr>
          <tr v-else-if="logs.length === 0">
            <td colspan="11" class="empty-cell">暂无操作日志</td>
          </tr>
          <tr v-else v-for="log in logs" :key="log.id" :class="{ selected: selectedIds.includes(log.id) }">
            <td class="checkbox-col">
              <input type="checkbox" :value="log.id" v-model="selectedIds" />
            </td>
            <td>{{ log.username }}</td>
            <td>{{ log.operation }}</td>
            <td class="url-col">{{ log.requestUrl }}</td>
            <td>{{ log.requestTime }}</td>
            <td>{{ log.ip }}</td>
            <td>{{ log.browser }}</td>
            <td>{{ log.os }}</td>
            <td>
              <span class="status-tag" :class="log.status === 1 ? 'success' : 'fail'">
                {{ log.status === 1 ? '成功' : '失败' }}
              </span>
            </td>
            <td>{{ formatTime(log.createTime) }}</td>
            <td class="action-col">
              <button class="btn-icon" @click="handleViewDetail(log)" title="查看详情">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                  <circle cx="12" cy="12" r="3"></circle>
                </svg>
              </button>
              <button class="btn-icon btn-danger-icon" @click="handleDelete(log.id)" title="删除">
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

    <!-- 批量操作栏 -->
    <div v-if="selectedIds.length > 0" class="batch-actions">
      <span class="selected-count">已选择 {{ selectedIds.length }} 条记录</span>
      <button class="btn btn-danger" @click="handleBatchDelete">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="3 6 5 6 21 6"></polyline>
          <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
        </svg>
        <span>批量删除</span>
      </button>
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

    <!-- 详情弹窗 -->
    <div v-if="showDetailModal" class="modal-overlay" @click="closeDetailModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>操作日志详情</h2>
          <button class="modal-close" @click="closeDetailModal">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="detail-item">
            <label>用户名：</label>
            <span>{{ currentLog?.username }}</span>
          </div>
          <div class="detail-item">
            <label>操作描述：</label>
            <span>{{ currentLog?.operation }}</span>
          </div>
          <div class="detail-item">
            <label>请求方法：</label>
            <span>{{ currentLog?.method }}</span>
          </div>
          <div class="detail-item">
            <label>请求方式：</label>
            <span>{{ currentLog?.requestMethod }}</span>
          </div>
          <div class="detail-item">
            <label>请求URL：</label>
            <span>{{ currentLog?.requestUrl }}</span>
          </div>
          <div class="detail-item">
            <label>请求参数：</label>
            <pre class="params-box">{{ currentLog?.requestParams || '无' }}</pre>
          </div>
          <div class="detail-item">
            <label>耗时：</label>
            <span>{{ currentLog?.requestTime }} ms</span>
          </div>
          <div class="detail-item">
            <label>IP地址：</label>
            <span>{{ currentLog?.ip }}</span>
          </div>
          <div class="detail-item">
            <label>操作地点：</label>
            <span>{{ currentLog?.location || '未知' }}</span>
          </div>
          <div class="detail-item">
            <label>浏览器：</label>
            <span>{{ currentLog?.browser }}</span>
          </div>
          <div class="detail-item">
            <label>操作系统：</label>
            <span>{{ currentLog?.os }}</span>
          </div>
          <div class="detail-item">
            <label>操作状态：</label>
            <span class="status-tag" :class="currentLog?.status === 1 ? 'success' : 'fail'">
              {{ currentLog?.status === 1 ? '成功' : '失败' }}
            </span>
          </div>
          <div v-if="currentLog?.errorMsg" class="detail-item">
            <label>错误信息：</label>
            <pre class="error-box">{{ currentLog?.errorMsg }}</pre>
          </div>
          <div class="detail-item">
            <label>操作时间：</label>
            <span>{{ formatTime(currentLog?.createTime) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useToast } from '@/composables/useToast'
import {
  getOperationLogList,
  deleteOperationLog,
  batchDeleteOperationLogs,
  clearOperationLogs,
  type OperationLog,
  type PageResponse
} from '@/utils/operationLogApi'

const { showSuccess, showError, showWarning } = useToast()

const loading = ref(false)
const logs = ref<OperationLog[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const jumpPage = ref(1)
const selectedIds = ref<string[]>([])
const selectAll = ref(false)
const showDetailModal = ref(false)
const currentLog = ref<OperationLog | null>(null)

const totalPages = computed(() => Math.ceil(total.value / pageSize.value) || 1)

onMounted(() => {
  loadLogs()
})

const loadLogs = async () => {
  loading.value = true
  try {
    const response = await getOperationLogList(currentPage.value, pageSize.value)
    if (response.code === 200 && response.data) {
      logs.value = response.data.records
      total.value = response.data.total
      selectedIds.value = []
      selectAll.value = false
    }
  } catch (error) {
    console.error('加载操作日志失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSelectAll = () => {
  if (selectAll.value) {
    selectedIds.value = logs.value.map(log => log.id)
  } else {
    selectedIds.value = []
  }
}

const handlePageSizeChange = () => {
  currentPage.value = 1
  jumpPage.value = 1
  loadLogs()
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  jumpPage.value = page
  loadLogs()
}

const handleJumpPage = () => {
  if (jumpPage.value >= 1 && jumpPage.value <= totalPages.value) {
    currentPage.value = jumpPage.value
    loadLogs()
  }
}

const handleViewDetail = (log: OperationLog) => {
  currentLog.value = log
  showDetailModal.value = true
}

const closeDetailModal = () => {
  showDetailModal.value = false
  currentLog.value = null
}

const handleDelete = async (id: string) => {
  if (!confirm('确定要删除这条操作日志吗？')) return
  try {
    const response = await deleteOperationLog(id)
    if (response.code === 200) {
      loadLogs()
    } else {
      showError(response.message || '删除失败')
    }
  } catch (error) {
    console.error('删除操作日志失败:', error)
    showError('删除失败')
  }
}

const handleBatchDelete = async () => {
  if (!confirm(`确定要删除选中的 ${selectedIds.value.length} 条操作日志吗？`)) return
  try {
    const response = await batchDeleteOperationLogs(selectedIds.value)
    if (response.code === 200) {
      loadLogs()
    } else {
      showError(response.message || '批量删除失败')
    }
  } catch (error) {
    console.error('批量删除操作日志失败:', error)
    showError('批量删除失败')
  }
}

const handleClearAll = async () => {
  if (!confirm('确定要清空所有操作日志吗？此操作不可恢复！')) return
  try {
    const response = await clearOperationLogs()
    if (response.code === 200) {
      loadLogs()
    } else {
      showError(response.message || '清空失败')
    }
  } catch (error) {
    console.error('清空操作日志失败:', error)
    showError('清空失败')
  }
}

const formatTime = (time?: string) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
/* ========== 页面布局 ========== */
.operation-log-view {
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

/* ========== 表格容器 ========== */
.log-table-container {
  background-color: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

/* ========== 表格样式 ========== */
.log-table {
  width: 100%;
  border-collapse: collapse;
}

.log-table th {
  background-color: #fafafc;
  padding: 14px 16px;
  text-align: left;
  font-weight: 600;
  font-size: 14px;
  color: #5c5c7a;
  border-bottom: 1px solid #e8e8f0;
}

.log-table td {
  padding: 14px 16px;
  border-bottom: 1px solid #f0f0f5;
  font-size: 14px;
  color: #1a1a2e;
}

.log-table tbody tr:last-child td {
  border-bottom: none;
}

.log-table tbody tr:hover {
  background-color: #fafafc;
}

.log-table tr.selected {
  background-color: #e8f0fe;
}

.checkbox-col {
  width: 48px;
  text-align: center;
}

.url-col {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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

.status-tag.success {
  background-color: #e8f5e9;
  color: #2e7d32;
}

.status-tag.fail {
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

.btn-sm {
  height: 32px;
  padding: 0 12px;
  font-size: 13px;
}

/* ========== 批量操作栏 ========== */
.batch-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background-color: #e8f0fe;
  border-radius: 8px;
  margin-top: 16px;
}

.selected-count {
  font-size: 14px;
  color: #4a7cf7;
  font-weight: 500;
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

/* ========== 详情弹窗 ========== */
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
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
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

.detail-item {
  display: flex;
  margin-bottom: 16px;
}

.detail-item:last-child {
  margin-bottom: 0;
}

.detail-item label {
  width: 120px;
  font-weight: 500;
  color: #5c5c7a;
}

.detail-item span {
  flex: 1;
  color: #1a1a2e;
}

.params-box,
.error-box {
  flex: 1;
  background-color: #fafafc;
  padding: 12px;
  border-radius: 8px;
  font-size: 13px;
  overflow-x: auto;
  white-space: pre-wrap;
  word-break: break-all;
}

.error-box {
  background-color: #ffebee;
  color: #c62828;
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