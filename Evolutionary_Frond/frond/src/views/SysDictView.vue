<template>
  <div class="sys-dict-view">
    <div class="page-header">
      <h1 class="page-title">字典管理</h1>
      <div class="header-actions">
        <input
          v-model="searchDictType"
          placeholder="搜索字典类型"
          class="search-input"
          @keyup.enter="handleSearch"
        />
        <input
          v-model="searchDictName"
          placeholder="搜索字典名称"
          class="search-input"
          @keyup.enter="handleSearch"
        />
        <button class="btn btn-primary" @click="handleSearch">搜索</button>
        <button class="btn btn-secondary" @click="handleReset">重置</button>
        <button class="btn btn-primary" @click="openAddTypeModal">添加字典</button>
      </div>
    </div>

    <!-- 字典类型列表 -->
    <div class="dict-table-container">
      <table class="dict-table">
        <thead>
          <tr>
            <th style="width: 30%">字典类型</th>
            <th style="width: 55%">字典名称</th>
            <th style="width: 15%">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="3" class="loading-cell">
              <div class="loading-spinner"></div>
              <span>加载中...</span>
            </td>
          </tr>
          <tr v-else-if="dictTypes.length === 0">
            <td colspan="3" class="empty-cell">暂无字典数据</td>
          </tr>
          <tr v-else v-for="dict in dictTypes" :key="dict.dictType">
            <td>
              <button class="dict-type-link" @click="openDictItemsModal(dict.dictType)">
                {{ dict.dictType }}
              </button>
            </td>
            <td>{{ dict.dictName || '-' }}</td>
            <td class="action-col">
              <button class="btn-icon" @click="openEditTypeModal(dict)" title="修改">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                </svg>
              </button>
              <button class="btn-icon danger" @click="handleDeleteType(dict.dictType)" title="删除">
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
      </div>
    </div>

    <!-- 添加/修改字典类型弹窗 -->
    <div v-if="showTypeModal" class="modal-overlay" @click="closeTypeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ typeModalTitle }}</h3>
          <button class="modal-close" @click="closeTypeModal">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>字典类型：</label>
            <input
              v-model="typeForm.dictType"
              :disabled="isEditType"
              placeholder="请输入字典类型"
              class="form-input"
            />
            <span v-if="isEditType" class="hint-text">（不可修改）</span>
          </div>
          <div class="form-group">
            <label>字典名称：</label>
            <input v-model="typeForm.dictName" placeholder="请输入字典名称" class="form-input" />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeTypeModal">取消</button>
          <button class="btn btn-primary" @click="handleSubmitType" :disabled="submitting">
            {{ submitting ? '提交中...' : '确定' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 字典项详情弹窗 -->
    <div v-if="showItemsModal" class="modal-overlay" @click="closeItemsModal">
      <div class="modal-content items-modal" @click.stop>
        <div class="modal-header">
          <h3>字典项列表 - {{ currentDictType }}</h3>
          <button class="modal-close" @click="closeItemsModal">×</button>
        </div>
        <div class="modal-body">
          <div class="items-header">
            <button class="btn btn-primary" @click="openAddItemModal">添加字典项</button>
          </div>
          <table class="items-table">
            <thead>
              <tr>
                <th style="width: 25%">字典编码</th>
                <th style="width: 30%">字典标签</th>
                <th style="width: 30%">字典值</th>
                <th style="width: 15%">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loadingItems">
                <td colspan="4" class="loading-cell">
                  <div class="loading-spinner"></div>
                  <span>加载中...</span>
                </td>
              </tr>
              <tr v-else-if="dictItems.length === 0">
                <td colspan="4" class="empty-cell">暂无字典项数据</td>
              </tr>
              <tr v-else v-for="item in dictItems" :key="item.id">
                <td>{{ item.dictCode }}</td>
                <td>{{ item.dictLabel }}</td>
                <td>{{ item.dictValue || '-' }}</td>
                <td class="action-col">
                  <button class="btn-icon" @click="openEditItemModal(item)" title="修改">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                      <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                    </svg>
                  </button>
                  <button class="btn-icon danger" @click="handleDeleteItem(item.id)" title="删除">
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
      </div>
    </div>

    <!-- 添加/修改字典项弹窗 -->
    <div v-if="showItemModal" class="modal-overlay" @click="closeItemModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ itemModalTitle }}</h3>
          <button class="modal-close" @click="closeItemModal">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>字典编码：</label>
            <input
              v-model="itemForm.dictCode"
              :disabled="isEditItem"
              placeholder="请输入字典编码"
              class="form-input"
            />
            <span v-if="isEditItem" class="hint-text">（不可修改）</span>
          </div>
          <div class="form-group">
            <label>字典标签：</label>
            <input v-model="itemForm.dictLabel" placeholder="请输入字典标签" class="form-input" />
          </div>
          <div class="form-group">
            <label>字典值：</label>
            <input v-model="itemForm.dictValue" placeholder="请输入字典值" class="form-input" />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeItemModal">取消</button>
          <button class="btn btn-primary" @click="handleSubmitItem" :disabled="submitting">
            {{ submitting ? '提交中...' : '确定' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import {
  getDictTypesList,
  getDictItemsByType,
  addDictType,
  updateDictType,
  deleteDictType,
  addDictItem,
  updateDictItem,
  deleteDictItem,
  type SysDict,
} from '@/utils/sysDictApi'
import { useToast } from '@/composables/useToast'

const toast = useToast()

// 字典类型列表
const dictTypes = ref<SysDict[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

// 搜索
const searchDictType = ref('')
const searchDictName = ref('')
const jumpPage = ref(1)

// 字典项列表
const dictItems = ref<SysDict[]>([])
const loadingItems = ref(false)
const currentDictType = ref('')
const showItemsModal = ref(false)

// 字典类型弹窗
const showTypeModal = ref(false)
const isEditType = ref(false)
const typeModalTitle = ref('')
const typeForm = ref<SysDict>({
  id: '',
  dictType: '',
  dictName: '',
  dictCode: '',
  dictLabel: '',
  sort: 0,
  status: 1,
})
const submitting = ref(false)

// 字典项弹窗
const showItemModal = ref(false)
const isEditItem = ref(false)
const itemModalTitle = ref('')
const itemForm = ref<SysDict>({
  id: '',
  dictType: '',
  dictName: '',
  dictCode: '',
  dictLabel: '',
  dictValue: '',
  sort: 0,
  status: 1,
})

// 加载字典类型列表
const loadDictTypes = async () => {
  loading.value = true
  try {
    const response = await getDictTypesList(
      searchDictType.value,
      searchDictName.value,
      currentPage.value,
      pageSize.value
    )
    if (response.code === 200) {
      dictTypes.value = response.data.records
      total.value = response.data.total
    } else {
      toast.showError(response.message || '加载字典类型列表失败')
    }
  } catch (error: any) {
    toast.showError(error.message || '加载字典类型列表失败')
  } finally {
    loading.value = false
  }
}

// 加载字典项列表
const loadDictItems = async (dictType: string) => {
  loadingItems.value = true
  try {
    const response = await getDictItemsByType(dictType)
    if (response.code === 200) {
      dictItems.value = response.data
      // 更新字典名称
      const dictTypeInfo = dictTypes.value.find(d => d.dictType === dictType)
      if (dictTypeInfo) {
        dictItems.value.forEach(item => {
          item.dictName = dictTypeInfo.dictName
        })
      }
    } else {
      toast.showError(response.message || '加载字典项列表失败')
    }
  } catch (error: any) {
    toast.showError(error.message || '加载字典项列表失败')
  } finally {
    loadingItems.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadDictTypes()
}

// 重置
const handleReset = () => {
  searchDictType.value = ''
  searchDictName.value = ''
  currentPage.value = 1
  loadDictTypes()
}

// 分页
const handlePageChange = (page: number) => {
  currentPage.value = page
  loadDictTypes()
}

const handlePageSizeChange = () => {
  currentPage.value = 1
  loadDictTypes()
}

const handleJumpPage = () => {
  if (jumpPage.value >= 1 && jumpPage.value <= totalPages.value) {
    currentPage.value = jumpPage.value
    loadDictTypes()
  }
}

// 打开字典项弹窗
const openDictItemsModal = (dictType: string) => {
  currentDictType.value = dictType
  showItemsModal.value = true
  loadDictItems(dictType)
}

const closeItemsModal = () => {
  showItemsModal.value = false
  currentDictType.value = ''
  dictItems.value = []
}

// 打开添加字典类型弹窗
const openAddTypeModal = () => {
  isEditType.value = false
  typeModalTitle.value = '添加字典类型'
  typeForm.value = {
    id: '',
    dictType: '',
    dictName: '',
    dictCode: '',
    dictLabel: '',
    sort: 0,
    status: 1,
  }
  showTypeModal.value = true
}

// 打开修改字典类型弹窗
const openEditTypeModal = (dict: SysDict) => {
  isEditType.value = true
  typeModalTitle.value = '修改字典类型'
  typeForm.value = {
    ...dict,
  }
  showTypeModal.value = true
}

const closeTypeModal = () => {
  showTypeModal.value = false
  isEditType.value = false
}

// 提交字典类型
const handleSubmitType = async () => {
  if (!typeForm.value.dictType || !typeForm.value.dictName) {
    toast.showWarning('字典类型和字典名称不能为空')
    return
  }

  submitting.value = true
  try {
    let response
    if (isEditType.value) {
      response = await updateDictType(typeForm.value)
    } else {
      response = await addDictType(typeForm.value)
    }

    if (response.code === 200) {
      toast.showSuccess(isEditType.value ? '修改字典类型成功' : '添加字典类型成功')
      closeTypeModal()
      loadDictTypes()
    } else {
      toast.showError(response.message || '操作失败')
    }
  } catch (error: any) {
    toast.showError(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

// 删除字典类型
const handleDeleteType = async (dictType: string) => {
  if (!confirm(`确定要删除字典类型 ${dictType} 及其所有字典项吗？`)) {
    return
  }

  try {
    const response = await deleteDictType(dictType)
    if (response.code === 200) {
      toast.showSuccess('删除字典类型成功')
      loadDictTypes()
    } else {
      toast.showError(response.message || '删除失败')
    }
  } catch (error: any) {
    toast.showError(error.message || '删除失败')
  }
}

// 打开添加字典项弹窗
const openAddItemModal = () => {
  isEditItem.value = false
  itemModalTitle.value = '添加字典项'
  const dictTypeInfo = dictTypes.value.find(d => d.dictType === currentDictType.value)
  itemForm.value = {
    id: '',
    dictType: currentDictType.value,
    dictName: dictTypeInfo?.dictName || '',
    dictCode: '',
    dictLabel: '',
    dictValue: '',
    sort: 0,
    status: 1,
  }
  showItemModal.value = true
}

// 打开修改字典项弹窗
const openEditItemModal = (item: SysDict) => {
  isEditItem.value = true
  itemModalTitle.value = '修改字典项'
  itemForm.value = {
    ...item,
  }
  showItemModal.value = true
}

const closeItemModal = () => {
  showItemModal.value = false
  isEditItem.value = false
}

// 提交字典项
const handleSubmitItem = async () => {
  if (!itemForm.value.dictCode || !itemForm.value.dictLabel) {
    toast.showWarning('字典编码和字典标签不能为空')
    return
  }

  submitting.value = true
  try {
    let response
    if (isEditItem.value) {
      response = await updateDictItem(itemForm.value)
    } else {
      response = await addDictItem(itemForm.value)
    }

    if (response.code === 200) {
      toast.showSuccess(isEditItem.value ? '修改字典项成功' : '添加字典项成功')
      closeItemModal()
      loadDictItems(currentDictType.value)
    } else {
      toast.showError(response.message || '操作失败')
    }
  } catch (error: any) {
    toast.showError(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

// 删除字典项
const handleDeleteItem = async (dictId: string) => {
  if (!confirm('确定要删除该字典项吗？')) {
    return
  }

  try {
    const response = await deleteDictItem(dictId)
    if (response.code === 200) {
      toast.showSuccess('删除字典项成功')
      loadDictItems(currentDictType.value)
    } else {
      toast.showError(response.message || '删除失败')
    }
  } catch (error: any) {
    toast.showError(error.message || '删除失败')
  }
}

onMounted(() => {
  loadDictTypes()
})
</script>

<style scoped>
.sys-dict-view {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
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
  gap: 10px;
}

.search-input {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  width: 200px;
}

.search-input:focus {
  outline: none;
  border-color: #409eff;
}

.dict-table-container {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.dict-table {
  width: 100%;
  border-collapse: collapse;
}

.dict-table th {
  background: #f0f2f5;
  padding: 12px 16px;
  text-align: left;
  font-weight: 600;
  color: #2c3e50;
  border-bottom: 1px solid #e8e8e8;
}

.dict-table td {
  padding: 12px 16px;
  border-bottom: 1px solid #e8e8e8;
  color: #5a5a5a;
}

.dict-table tr:hover {
  background: #f5f7fa;
}

.dict-type-link {
  background: none;
  border: none;
  color: #409eff;
  cursor: pointer;
  font-size: 14px;
  padding: 0;
  text-decoration: underline;
}

.dict-type-link:hover {
  color: #66b1ff;
}

.action-col {
  display: flex;
  gap: 8px;
}

.btn-icon {
  background: white;
  border: 1px solid #dcdfe6;
  padding: 6px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-icon:hover {
  background: #f5f7fa;
  border-color: #409eff;
  color: #409eff;
}

.btn-icon.danger:hover {
  border-color: #f56c6c;
  color: #f56c6c;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: white;
  border-radius: 8px;
  margin-top: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.page-size-select {
  display: flex;
  align-items: center;
  gap: 4px;
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

.page-jump {
  display: flex;
  align-items: center;
  gap: 4px;
}

.page-jump input {
  width: 50px;
  padding: 4px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.btn {
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  border: 1px solid #dcdfe6;
}

.btn-primary {
  background: #409eff;
  color: white;
  border-color: #409eff;
}

.btn-primary:hover {
  background: #66b1ff;
  border-color: #66b1ff;
}

.btn-secondary {
  background: white;
  color: #606266;
}

.btn-secondary:hover {
  background: #f5f7fa;
}

.btn-secondary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.loading-cell,
.empty-cell {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.loading-spinner {
  width: 30px;
  height: 30px;
  margin: 0 auto 10px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #409eff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 500px;
  max-width: 90%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.items-modal {
  width: 800px;
  max-width: 90%;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e8e8e8;
}

.modal-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #909399;
}

.modal-close:hover {
  color: #2c3e50;
}

.modal-body {
  padding: 20px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 20px;
  border-top: 1px solid #e8e8e8;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #2c3e50;
}

.form-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
}

.form-input:focus {
  outline: none;
  border-color: #409eff;
}

.form-input:disabled {
  background: #f5f7fa;
  cursor: not-allowed;
}

.hint-text {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}

.items-header {
  margin-bottom: 16px;
}

.items-table {
  width: 100%;
  border-collapse: collapse;
}

.items-table th {
  background: #f0f2f5;
  padding: 12px 16px;
  text-align: left;
  font-weight: 600;
  color: #2c3e50;
  border-bottom: 1px solid #e8e8e8;
}

.items-table td {
  padding: 12px 16px;
  border-bottom: 1px solid #e8e8e8;
  color: #5a5a5a;
}

.items-table tr:hover {
  background: #f5f7fa;
}
</style>
