<template>
  <div class="sys-approval-view">
    <div class="page-header">
      <h1 class="page-title">审批管理</h1>
      <div class="header-actions">
        <select v-model="selectedType" @change="handleTypeChange" class="filter-select">
          <option value="">全部类型</option>
          <option v-for="type in approvalTypes" :key="type.dictCode" :value="type.dictValue">
            {{ type.dictLabel }}
          </option>
        </select>
        <select v-model="selectedStatus" @change="handleStatusChange" class="filter-select">
          <option value="">全部状态</option>
          <option v-for="status in approvalStatuses" :key="status.dictCode" :value="status.dictValue">
            {{ status.dictLabel }}
          </option>
        </select>
      </div>
    </div>

    <!-- 审批列表 -->
    <div class="approval-table-container">
      <table class="approval-table">
        <thead>
          <tr>
            <th>审批类型</th>
            <th>审批标题</th>
            <th>申请人</th>
            <th>审批人</th>
            <th>审批状态</th>
            <th>申请时间</th>
            <th>审批时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="8" class="loading-cell">
              <div class="loading-spinner"></div>
              <span>加载中...</span>
            </td>
          </tr>
          <tr v-else-if="approvals.length === 0">
            <td colspan="8" class="empty-cell">暂无审批数据</td>
          </tr>
          <tr v-else v-for="approval in approvals" :key="approval.id">
            <td>{{ getApprovalTypeLabel(approval.approvalType) }}</td>
            <td>{{ approval.approvalTitle }}</td>
            <td>{{ approval.applicantName || '-' }}</td>
            <td>{{ approval.approverName || '-' }}</td>
            <td>
              <span class="status-tag" :class="getStatusClass(approval.approvalStatus)">
                {{ getStatusLabel(approval.approvalStatus) }}
              </span>
            </td>
            <td>{{ formatTime(approval.createTime) }}</td>
            <td>{{ approval.approvalTime ? formatTime(approval.approvalTime) : '-' }}</td>
            <td class="action-col">
              <button class="btn-icon" @click="viewApproval(approval)" title="查看详情">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                  <circle cx="12" cy="12" r="3"></circle>
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
      </div>
    </div>

    <!-- 详情弹窗 -->
    <div v-if="showDetailModal" class="modal-overlay" @click="closeDetailModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>审批详情</h2>
          <button class="modal-close" @click="closeDetailModal">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="detail-section">
            <div class="detail-item">
              <label>审批类型：</label>
              <span>{{ getApprovalTypeLabel(currentApproval?.approvalType) }}</span>
            </div>
            <div class="detail-item">
              <label>审批标题：</label>
              <span>{{ currentApproval?.approvalTitle }}</span>
            </div>
            <div class="detail-item">
              <label>申请人：</label>
              <span>{{ currentApproval?.applicantName || '-' }}</span>
            </div>
            <div class="detail-item">
              <label>审批人：</label>
              <span>{{ currentApproval?.approverName || '-' }}</span>
            </div>
            <div class="detail-item">
              <label>审批状态：</label>
              <span class="status-tag" :class="getStatusClass(currentApproval?.approvalStatus)">
                {{ getStatusLabel(currentApproval?.approvalStatus) }}
              </span>
            </div>
            <div class="detail-item">
              <label>申请时间：</label>
              <span>{{ currentApproval?.createTime ? formatTime(currentApproval.createTime) : '-' }}</span>
            </div>
            <div class="detail-item">
              <label>审批时间：</label>
              <span>{{ currentApproval?.approvalTime ? formatTime(currentApproval.approvalTime) : '-' }}</span>
            </div>
            <div class="detail-item">
              <label>审批意见：</label>
              <span>{{ currentApproval?.approvalOpinion || '-' }}</span>
            </div>
            <div class="detail-item" v-if="currentApproval?.approvalContent">
              <label>审批内容：</label>
              <pre class="content-json">{{ formatApprovalContent(currentApproval?.approvalContent) }}</pre>
            </div>
            <div class="detail-item">
              <label>备注：</label>
              <span>{{ currentApproval?.remark || '-' }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { hasPermission } from '@/utils/permission'
import {
  getApprovalList,
  getApprovalListByType,
  getApprovalListByStatus,
  getApprovalTypes,
  getApprovalStatuses,
  type SysApproval,
  type SysDict,
  type PageResponse
} from '@/utils/sysApprovalApi'

const approvals = ref<SysApproval[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const jumpPage = ref<number | null>(null)

const approvalTypes = ref<SysDict[]>([])
const approvalStatuses = ref<SysDict[]>([])
const selectedType = ref('')
const selectedStatus = ref('')

const showDetailModal = ref(false)
const currentApproval = ref<SysApproval | null>(null)

const totalPages = computed(() => Math.ceil(total.value / pageSize.value) || 1)

onMounted(() => {
  loadApprovals()
  loadApprovalTypes()
  loadApprovalStatuses()
})

const loadApprovals = async () => {
  loading.value = true
  try {
    let response
    if (selectedType.value && selectedStatus.value) {
      // 同时有类型和状态筛选时，优先按类型筛选
      response = await getApprovalListByType(selectedType.value, currentPage.value, pageSize.value)
    } else if (selectedType.value) {
      response = await getApprovalListByType(selectedType.value, currentPage.value, pageSize.value)
    } else if (selectedStatus.value) {
      const statusValue = parseInt(selectedStatus.value)
      response = await getApprovalListByStatus(statusValue, currentPage.value, pageSize.value)
    } else {
      response = await getApprovalList(currentPage.value, pageSize.value)
    }

    if (response.code === 200 && response.data) {
      approvals.value = response.data.records
      total.value = response.data.total
    }
  } catch (error) {
    console.error('加载审批列表失败:', error)
  } finally {
    loading.value = false
  }
}

const loadApprovalTypes = async () => {
  try {
    const response = await getApprovalTypes()
    if (response.code === 200 && response.data) {
      approvalTypes.value = response.data
    }
  } catch (error) {
    console.error('加载审批类型失败:', error)
  }
}

const loadApprovalStatuses = async () => {
  try {
    const response = await getApprovalStatuses()
    if (response.code === 200 && response.data) {
      approvalStatuses.value = response.data
    }
  } catch (error) {
    console.error('加载审批状态失败:', error)
  }
}

const handleTypeChange = () => {
  currentPage.value = 1
  loadApprovals()
}

const handleStatusChange = () => {
  currentPage.value = 1
  loadApprovals()
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  loadApprovals()
}

const handlePageSizeChange = () => {
  currentPage.value = 1
  loadApprovals()
}

const handleJumpPage = () => {
  if (jumpPage.value && jumpPage.value >= 1 && jumpPage.value <= totalPages.value) {
    currentPage.value = jumpPage.value
    loadApprovals()
    jumpPage.value = null
  }
}

const getApprovalTypeLabel = (type?: string): string => {
  if (!type) return '-'
  const dict = approvalTypes.value.find(t => t.dictValue === type)
  return dict ? dict.dictLabel : type
}

const getStatusLabel = (status?: number): string => {
  if (!status) return '-'
  const dict = approvalStatuses.value.find(s => s.dictValue === String(status))
  return dict ? dict.dictLabel : String(status)
}

const getStatusClass = (status?: number): string => {
  if (!status) return 'status-unknown'
  switch (status) {
    case 0:
      return 'status-pending'
    case 1:
      return 'status-approved'
    case 2:
      return 'status-rejected'
    default:
      return 'status-unknown'
  }
}

const formatTime = (time?: string): string => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const formatApprovalContent = (content?: string): string => {
  if (!content) return ''
  try {
    const parsed = JSON.parse(content)
    return JSON.stringify(parsed, null, 2)
  } catch {
    return content
  }
}

const viewApproval = (approval: SysApproval) => {
  currentApproval.value = approval
  showDetailModal.value = true
}

const closeDetailModal = () => {
  showDetailModal.value = false
  currentApproval.value = null
}
</script>

<style scoped>
.sys-approval-view {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.filter-select {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: white;
  font-size: 14px;
  min-width: 120px;
}

.approval-table-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.approval-table {
  width: 100%;
  border-collapse: collapse;
}

.approval-table thead {
  background: #f8f9fa;
}

.approval-table th {
  padding: 12px 16px;
  text-align: left;
  font-weight: 600;
  color: #606266;
  border-bottom: 1px solid #e4e7ed;
  font-size: 14px;
}

.approval-table td {
  padding: 12px 16px;
  color: #606266;
  border-bottom: 1px solid #ebeef5;
  font-size: 14px;
}

.approval-table tbody tr:hover {
  background: #f5f7fa;
}

.loading-cell,
.empty-cell {
  text-align: center;
  padding: 40px 16px;
  color: #909399;
}

.loading-spinner {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid #e4e7ed;
  border-top-color: #409eff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-right: 8px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.status-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-pending {
  background: #e6f7ff;
  color: #1890ff;
}

.status-approved {
  background: #f6ffed;
  color: #52c41a;
}

.status-rejected {
  background: #fff2f0;
  color: #ff4d4f;
}

.status-unknown {
  background: #f5f5f5;
  color: #595959;
}

.action-col {
  display: flex;
  gap: 8px;
  justify-content: flex-start;
}

.btn-icon {
  background: transparent;
  border: none;
  padding: 4px;
  cursor: pointer;
  color: #606266;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.3s;
}

.btn-icon:hover {
  background: #ecf5ff;
  color: #409eff;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 20px;
  background: white;
  border-top: 1px solid #e4e7ed;
}

.page-size-select {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-size-select select {
  padding: 4px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.page-info {
  color: #606266;
  font-size: 14px;
}

.btn {
  padding: 8px 16px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-secondary {
  background: white;
  color: #606266;
}

.btn-secondary:not(:disabled):hover {
  background: #ecf5ff;
  color: #409eff;
  border-color: #409eff;
}

.page-jump {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-jump input {
  width: 50px;
  padding: 4px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  text-align: center;
}

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
  border-radius: 8px;
  max-width: 600px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #e4e7ed;
}

.modal-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.modal-close {
  background: transparent;
  border: none;
  cursor: pointer;
  color: #909399;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-close:hover {
  color: #606266;
}

.modal-body {
  padding: 24px;
}

.detail-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-item label {
  font-weight: 600;
  color: #606266;
  font-size: 14px;
}

.detail-item span {
  color: #2c3e50;
  font-size: 14px;
}

.content-json {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  font-size: 12px;
  overflow-x: auto;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>