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
      <button class="btn btn-secondary" :disabled="currentPage === 1" @click="handlePageChange(currentPage - 1)">
        上一页
      </button>
      <span class="page-info">第 {{ currentPage }} 页 / 共 {{ totalPages }} 页（共 {{ total }} 条）</span>
      <button class="btn btn-secondary" :disabled="currentPage >= totalPages" @click="handlePageChange(currentPage + 1)">
        下一页
      </button>
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
import {
  getOperationLogList,
  deleteOperationLog,
  batchDeleteOperationLogs,
  clearOperationLogs,
  type OperationLog,
  type PageResponse
} from '@/utils/operationLogApi'

const loading = ref(false)
const logs = ref<OperationLog[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
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

const handlePageChange = (page: number) => {
  currentPage.value = page
  loadLogs()
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
      alert(response.message || '删除失败')
    }
  } catch (error) {
    console.error('删除操作日志失败:', error)
    alert('删除失败')
  }
}

const handleBatchDelete = async () => {
  if (!confirm(`确定要删除选中的 ${selectedIds.value.length} 条操作日志吗？`)) return
  try {
    const response = await batchDeleteOperationLogs(selectedIds.value)
    if (response.code === 200) {
      loadLogs()
    } else {
      alert(response.message || '批量删除失败')
    }
  } catch (error) {
    console.error('批量删除操作日志失败:', error)
    alert('批量删除失败')
  }
}

const handleClearAll = async () => {
  if (!confirm('确定要清空所有操作日志吗？此操作不可恢复！')) return
  try {
    const response = await clearOperationLogs()
    if (response.code === 200) {
      loadLogs()
    } else {
      alert(response.message || '清空失败')
    }
  } catch (error) {
    console.error('清空操作日志失败:', error)
    alert('清空失败')
  }
}

const formatTime = (time?: string) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.operation-log-view {
  padding: var(--spacing-xl);
  min-height: 100vh;
  background-color: var(--color-background);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-xl);
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text);
}

.header-actions {
  display: flex;
  gap: var(--spacing-sm);
}

.log-table-container {
  background-color: #ffffff;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.log-table {
  width: 100%;
  border-collapse: collapse;
}

.log-table th {
  background-color: var(--color-background-soft);
  padding: var(--spacing-md);
  text-align: left;
  font-weight: 600;
  font-size: 14px;
  color: var(--color-text);
  border-bottom: 1px solid var(--color-border);
}

.log-table td {
  padding: var(--spacing-md);
  border-bottom: 1px solid var(--color-border);
  font-size: 14px;
  color: var(--color-text);
}

.log-table tr:hover {
  background-color: var(--color-background-soft);
}

.log-table tr.selected {
  background-color: var(--color-primary-light);
}

.checkbox-col {
  width: 40px;
  text-align: center;
}

.url-col {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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

.status-tag.success {
  background-color: #d4edda;
  color: #155724;
}

.status-tag.fail {
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

.batch-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-md);
  background-color: var(--color-primary-light);
  border-radius: var(--radius-md);
  margin-top: var(--spacing-md);
}

.selected-count {
  font-size: 14px;
  color: var(--color-primary);
  font-weight: 500;
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

/* 详情弹窗 */
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
  max-width: 600px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
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

.detail-item {
  display: flex;
  margin-bottom: var(--spacing-md);
}

.detail-item label {
  width: 120px;
  font-weight: 500;
  color: var(--color-text-secondary);
}

.detail-item span {
  flex: 1;
  color: var(--color-text);
}

.params-box,
.error-box {
  flex: 1;
  background-color: var(--color-background-soft);
  padding: var(--spacing-sm);
  border-radius: var(--radius-sm);
  font-size: 12px;
  overflow-x: auto;
  white-space: pre-wrap;
  word-break: break-all;
}

.error-box {
  background-color: #f8d7da;
  color: #721c24;
}
</style>