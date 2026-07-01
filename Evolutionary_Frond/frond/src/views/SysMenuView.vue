<template>
  <div class="sys-menu-view">
    <div class="page-header">
      <h1 class="page-title">菜单管理</h1>
      <div class="header-actions">
        <button v-if="hasPermission('sys:permission:add')" class="btn btn-primary" @click="openAddModal">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="12" y1="5" x2="12" y2="19"></line>
            <line x1="5" y1="12" x2="19" y2="12"></line>
          </svg>
          <span>添加菜单</span>
        </button>
      </div>
    </div>

    <!-- 菜单列表 -->
    <div class="menu-table-container">
      <table class="menu-table">
        <thead>
          <tr>
            <th>菜单名称</th>
            <th>图标</th>
            <th>类型</th>
            <th>权限标识</th>
            <th>路由路径</th>
            <th>排序</th>
            <th>状态</th>
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
          <tr v-else-if="displayList.length === 0">
            <td colspan="8" class="empty-cell">暂无菜单数据</td>
          </tr>
          <tr v-else v-for="item in displayList" :key="item.id" class="tree-row">
            <td>
              <div class="cell-name" :style="{ paddingLeft: (item._level - 1) * 24 + 'px' }">
                <button
                  v-if="item._hasChildren"
                  class="expand-btn"
                  @click="toggleExpand(item)"
                >
                  <svg
                    width="12"
                    height="12"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    :class="{ rotated: !item._collapsed }"
                  >
                    <polyline points="9 18 15 12 9 6"></polyline>
                  </svg>
                </button>
                <span v-else class="expand-placeholder"></span>
                <span class="menu-name">{{ item.permissionName }}</span>
              </div>
            </td>
            <td>{{ item.icon || '-' }}</td>
            <td>
              <span class="type-tag" :class="'type-' + item.permissionType">
                {{ typeLabel(item.permissionType) }}
              </span>
            </td>
            <td>{{ item.permissionCode || '-' }}</td>
            <td>{{ item.path || '-' }}</td>
            <td>{{ item.sort }}</td>
            <td>
              <span class="status-tag" :class="item.status === 1 ? 'active' : 'inactive'">
                {{ item.status === 1 ? '正常' : '禁用' }}
              </span>
            </td>
            <td class="action-col">
              <button v-if="hasPermission('sys:permission:edit')" class="btn-icon" @click="openEditModal(item)" title="编辑">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                </svg>
              </button>
              <button v-if="hasPermission('sys:permission:add')" class="btn-icon" @click="openAddChildModal(item)" title="添加子菜单">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="12" y1="5" x2="12" y2="19"></line>
                  <line x1="5" y1="12" x2="19" y2="12"></line>
                </svg>
              </button>
              <button v-if="hasPermission('sys:permission:delete')" class="btn-icon btn-danger-icon" @click="handleDelete(item.id)" title="删除">
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

    <!-- 添加/编辑菜单弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>{{ isEdit ? '编辑菜单' : (isChild ? '添加子菜单' : '添加菜单') }}</h2>
          <button class="modal-close" @click="closeModal">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>上级菜单</label>
            <select v-model="form.parentId">
              <option value="0">根目录</option>
              <option v-for="p in parentOptions" :key="p.id" :value="p.id">
                {{ p.permissionName }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>菜单名称 <span class="required">*</span></label>
            <input type="text" v-model="form.permissionName" placeholder="请输入菜单名称" />
          </div>
          <div class="form-row">
            <div class="form-group form-half">
              <label>类型 <span class="required">*</span></label>
              <select v-model="form.permissionType">
                <option :value="1">目录</option>
                <option :value="2">菜单</option>
                <option :value="3">按钮</option>
              </select>
            </div>
            <div class="form-group form-half">
              <label>排序</label>
              <input type="number" v-model="form.sort" placeholder="排序值" />
            </div>
          </div>
          <div class="form-group">
            <label>权限标识</label>
            <input type="text" v-model="form.permissionCode" placeholder="如 sys:menu:list（按钮类型必填）" />
          </div>
          <div class="form-row">
            <div class="form-group form-half">
              <label>路由路径</label>
              <input type="text" v-model="form.path" placeholder="如 /system/menu" />
            </div>
            <div class="form-group form-half">
              <label>组件路径</label>
              <input type="text" v-model="form.component" placeholder="如 SysMenuView" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group form-half">
              <label>图标</label>
              <div class="icon-picker-wrap">
                <div class="icon-preview" @click="showIconPicker = true">
                  <feather-icon v-if="form.icon" :type="form.icon" :size="20" />
                  <span v-else class="icon-placeholder">无</span>
                </div>
                <input type="text" v-model="form.icon" placeholder="图标名称" readonly @click="showIconPicker = true" />
                <button v-if="form.icon" class="icon-clear" @click="form.icon = ''">×</button>
              </div>
              <!-- 图标选择弹窗 -->
              <div v-if="showIconPicker" class="icon-picker-dropdown">
                <div class="icon-picker-header">
                  <input
                    class="icon-search"
                    v-model="iconSearch"
                    placeholder="搜索图标..."
                    autofocus
                  />
                </div>
                <div class="icon-picker-grid">
                  <div
                    v-for="name in filteredIcons"
                    :key="name"
                    class="icon-option"
                    :class="{ selected: form.icon === name }"
                    @click="selectIcon(name)"
                    :title="name"
                  >
                    <feather-icon :type="name" :size="18" />
                    <span class="icon-name">{{ name }}</span>
                  </div>
                  <div v-if="filteredIcons.length === 0" class="icon-no-result">无匹配图标</div>
                </div>
                <div class="icon-picker-footer">
                  <button class="btn btn-sm" @click="form.icon = ''; showIconPicker = false">清除</button>
                  <button class="btn btn-sm btn-primary" @click="showIconPicker = false">确定</button>
                </div>
              </div>
            </div>
            <div class="form-group form-half">
              <label>状态</label>
              <select v-model="form.status">
                <option :value="1">正常</option>
                <option :value="0">禁用</option>
              </select>
            </div>
          </div>
          <div class="form-group">
            <label>备注</label>
            <textarea v-model="form.remark" placeholder="请输入备注"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeModal">取消</button>
          <button class="btn btn-primary" @click="handleSave" :disabled="saving">
            {{ saving ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useToast } from '@/composables/useToast'
import { usePermission } from '@/composables/usePermission'
import {
  getPermissionList,
  addPermission,
  updatePermission,
  deletePermission,
  type SysPermission
} from '@/utils/sysPermissionApi'
import FeatherIcon from '@/components/FeatherIcon.vue'

const { showSuccess, showError, showWarning } = useToast()
const { loadPermissions: loadUserPermissions, hasPermission } = usePermission()

// ==================== 类型定义 ====================

interface TreeNode extends SysPermission {
  _level: number
  _hasChildren: boolean
  _collapsed: boolean
  _children: TreeNode[]
}

// ==================== 状态定义 ====================

const loading = ref(false)
const allPermissions = ref<SysPermission[]>([])
const treeRoots = ref<TreeNode[]>([])
const expandedKeys = ref<Set<string>>(new Set())

const showModal = ref(false)
const isEdit = ref(false)
const isChild = ref(false)
const saving = ref(false)
const form = ref({
  id: '',
  parentId: '0',
  permissionName: '',
  permissionCode: '',
  permissionType: 1,
  path: '',
  component: '',
  icon: '',
  sort: 0,
  status: 1,
  visible: 1,
  remark: ''
})

// 常用 Feather 图标列表（按用途分组）
const FEATHER_ICONS = [
  // 导航与布局
  'home', 'grid', 'menu', 'sidebar', 'layers', 'navigation', 'map-pin',
  // 数据与内容
  'database', 'archive', 'file', 'file-text', 'folder', 'bookmark', 'book', 'book-open', 'edit', 'edit-2', 'edit-3',
  // 聊天与通讯
  'message-square', 'message-circle', 'mail', 'send', 'inbox',
  // 用户与团队
  'user', 'users', 'user-check', 'user-plus', 'user-minus', 'user-x',
  // 系统与设置
  'settings', 'sliders', 'tool', 'wrench', 'cpu', 'hard-drive', 'monitor', 'smartphone', 'tablet',
  // AI 与科技
  'activity', 'zap', 'terminal', 'code', 'server', 'cloud', 'globe',
  // 安全与权限
  'shield', 'shield-off', 'lock', 'unlock', 'key', 'eye', 'eye-off',
  // 图表与分析
  'bar-chart', 'bar-chart-2', 'pie-chart', 'trending-up', 'trending-down', 'activity',
  // 文件与文档
  'file', 'file-text', 'file-plus', 'file-minus', 'clipboard', 'check-square',
  // 媒体与设计
  'image', 'camera', 'video', 'music', 'headphones', 'film',
  // 通用
  'star', 'heart', 'flag', 'bell', 'bell-off', 'clock', 'calendar', 'gift', 'award',
  'search', 'plus-circle', 'minus-circle', 'check-circle', 'alert-circle', 'info', 'help-circle',
  'link', 'external-link', 'download', 'upload', 'refresh-cw', 'shuffle', 'repeat',
  'list', 'columns', 'filter', 'sort-asc', 'sort-desc',
  'log-in', 'log-out', 'power', 'trash', 'trash-2', 'plus', 'minus', 'x', 'check',
  // 箭头与方向
  'arrow-left', 'arrow-right', 'arrow-up', 'arrow-down', 'chevron-left', 'chevron-right',
  'chevron-up', 'chevron-down', 'corner-up-left', 'corner-up-right',
  // 其他
  'map', 'compass', 'target', 'crosshair', 'life-buoy', 'anchor', 'coffee', 'feather',
]

// ==================== 计算属性 ====================

/**
 * 扁平化的树形展示列表，只显示展开状态下的可见节点
 */
const displayList = computed(() => {
  const result: TreeNode[] = []
  const traverse = (nodes: TreeNode[]) => {
    for (const node of nodes) {
      result.push(node)
      if (!node._collapsed && node._hasChildren) {
        traverse(node._children)
      }
    }
  }
  traverse(treeRoots.value)
  return result
})

const parentOptions = computed(() => {
  return allPermissions.value.filter(p => p.permissionType === 1 || p.permissionType === 2)
})

// 图标选择器
const showIconPicker = ref(false)
const iconSearch = ref('')
const filteredIcons = computed(() => {
  if (!iconSearch.value) return FEATHER_ICONS
  const q = iconSearch.value.toLowerCase()
  return FEATHER_ICONS.filter(n => n.includes(q))
})
const selectIcon = (name: string) => {
  form.value.icon = name
  showIconPicker.value = false
  iconSearch.value = ''
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadPermissions()
  loadUserPermissions()
})

// ==================== 方法 ====================

const loadPermissions = async () => {
  loading.value = true
  try {
    const response = await getPermissionList()
    if (response.code === 200 && response.data) {
      allPermissions.value = response.data
      buildTree(response.data)
    }
  } catch (error) {
    console.error('加载菜单列表失败:', error)
  } finally {
    loading.value = false
  }
}

const buildTree = (flatList: SysPermission[]) => {
  const childrenMap = new Map<string, SysPermission[]>()
  for (const item of flatList) {
    const parentId = item.parentId || '0'
    if (!childrenMap.has(parentId)) {
      childrenMap.set(parentId, [])
    }
    childrenMap.get(parentId)!.push(item)
  }

  const buildNode = (parentId: string, level: number): TreeNode[] => {
    const children = childrenMap.get(parentId) || []
    return children.map(item => {
      const subChildren = buildNode(item.id, level + 1)
      const hasChildren = subChildren.length > 0
      const key = item.id
      return {
        ...item,
        _level: level,
        _hasChildren: hasChildren,
        _collapsed: !expandedKeys.value.has(key),
        _children: hasChildren ? subChildren : []
      } as TreeNode
    })
  }

  treeRoots.value = buildNode('0', 1)
}

const toggleExpand = (node: TreeNode) => {
  node._collapsed = !node._collapsed
  if (!node._collapsed) {
    expandedKeys.value.add(node.id)
  } else {
    expandedKeys.value.delete(node.id)
  }
}

const typeLabel = (type: number) => {
  const map: Record<number, string> = { 1: '目录', 2: '菜单', 3: '按钮' }
  return map[type] || '未知'
}

const openAddModal = () => {
  isEdit.value = false
  isChild.value = false
  resetForm()
  showModal.value = true
}

const openAddChildModal = (parent: TreeNode) => {
  isEdit.value = false
  isChild.value = true
  resetForm()
  form.value.parentId = parent.id
  form.value.permissionType = parent.permissionType === 1 ? 2 : parent.permissionType
  showModal.value = true
}

const openEditModal = (item: TreeNode) => {
  isEdit.value = true
  isChild.value = false
  form.value = {
    id: item.id,
    parentId: item.parentId || '0',
    permissionName: item.permissionName,
    permissionCode: item.permissionCode || '',
    permissionType: item.permissionType,
    path: item.path || '',
    component: item.component || '',
    icon: item.icon || '',
    sort: item.sort,
    status: item.status,
    visible: item.visible,
    remark: item.remark || ''
  }
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
}

const resetForm = () => {
  form.value = {
    id: '',
    parentId: '0',
    permissionName: '',
    permissionCode: '',
    permissionType: 1,
    path: '',
    component: '',
    icon: '',
    sort: 0,
    status: 1,
    visible: 1,
    remark: ''
  }
}

const handleSave = async () => {
  if (!form.value.permissionName) {
    showWarning('请输入菜单名称')
    return
  }

  saving.value = true
  try {
    let response
    if (isEdit.value) {
      response = await updatePermission({
        id: form.value.id,
        parentId: form.value.parentId !== '0' ? form.value.parentId : '0',
        permissionName: form.value.permissionName,
        permissionCode: form.value.permissionCode || undefined,
        permissionType: form.value.permissionType,
        path: form.value.path || undefined,
        component: form.value.component || undefined,
        icon: form.value.icon || undefined,
        sort: form.value.sort,
        visible: form.value.visible,
        status: form.value.status,
        remark: form.value.remark || undefined
      })
    } else {
      response = await addPermission({
        parentId: form.value.parentId || '0',
        permissionName: form.value.permissionName,
        permissionCode: form.value.permissionCode || undefined,
        permissionType: form.value.permissionType,
        path: form.value.path || undefined,
        component: form.value.component || undefined,
        icon: form.value.icon || undefined,
        sort: form.value.sort,
        visible: form.value.visible,
        status: form.value.status,
        remark: form.value.remark || undefined
      })
    }

    if (response.code === 200) {
      closeModal()
      loadPermissions()
      showSuccess(isEdit.value ? '菜单修改成功' : '菜单添加成功')
    } else {
      showError(response.message || '操作失败')
    }
  } catch (error) {
    console.error('保存菜单失败:', error)
    showError('保存失败')
  } finally {
    saving.value = false
  }
}

const handleDelete = async (id: string) => {
  if (!confirm('确定要删除这个菜单/权限吗？删除后不可恢复。')) return
  try {
    const response = await deletePermission(id)
    if (response.code === 200) {
      loadPermissions()
      showSuccess('菜单删除成功')
    } else {
      showError(response.message || '删除失败')
    }
  } catch (error) {
    console.error('删除菜单失败:', error)
    showError('删除失败')
  }
}


</script>

<style scoped>
.sys-menu-view {
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

.menu-table-container {
  background-color: #ffffff;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.menu-table {
  width: 100%;
  border-collapse: collapse;
}

.menu-table th {
  background-color: var(--color-background-soft);
  padding: var(--spacing-md);
  text-align: left;
  font-weight: 600;
  font-size: 14px;
  color: var(--color-text);
  border-bottom: 1px solid var(--color-border);
}

.menu-table td {
  padding: var(--spacing-md);
  border-bottom: 1px solid var(--color-border);
  font-size: 14px;
  color: var(--color-text);
}

.menu-table tr:hover {
  background-color: var(--color-background-soft);
}

.tree-row td:first-child {
  min-width: 250px;
}

.cell-name {
  display: flex;
  align-items: center;
  gap: 6px;
}

.expand-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: var(--radius-sm);
  color: var(--color-text-secondary);
  cursor: pointer;
  flex-shrink: 0;
}

.expand-btn:hover {
  background-color: var(--color-background-soft);
}

.expand-placeholder {
  display: inline-block;
  width: 20px;
  flex-shrink: 0;
}

.rotated {
  transform: rotate(90deg);
  transition: transform 0.2s;
}

.menu-name {
  font-weight: 500;
}

.action-col {
  width: 120px;
}

.type-tag {
  padding: 4px 8px;
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 500;
}

.type-tag.type-1 {
  background-color: #cce5ff;
  color: #004085;
}

.type-tag.type-2 {
  background-color: #d4edda;
  color: #155724;
}

.type-tag.type-3 {
  background-color: #fff3cd;
  color: #856404;
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
  max-width: 560px;
  width: 90%;
  max-height: 90vh;
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

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-sm);
  padding: var(--spacing-lg);
  border-top: 1px solid var(--color-border);
}

.form-group {
  margin-bottom: var(--spacing-md);
}

.form-row {
  display: flex;
  gap: var(--spacing-md);
}

.form-half {
  flex: 1;
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

/* 按钮样式 */
.btn {
  padding: var(--spacing-sm) var(--spacing-lg);
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease-out;
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.btn-primary {
  background-color: var(--color-primary);
  color: #ffffff;
  border: none;
}

.btn-primary:hover {
  background-color: var(--color-primary-hover);
}

.btn-secondary {
  background-color: #ffffff;
  color: var(--color-text);
  border: 1px solid var(--color-border);
}

.btn-secondary:hover {
  background-color: var(--color-background-soft);
}

/* 图标选择器 */
.icon-picker-wrap {
  display: flex;
  align-items: center;
  gap: 6px;
  position: relative;
}

.icon-preview {
  width: 36px;
  height: 36px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
  color: var(--color-text);
  transition: border-color 0.2s;
}

.icon-preview:hover {
  border-color: var(--color-primary);
}

.icon-placeholder {
  font-size: 12px;
  color: var(--color-text-tertiary);
}

.icon-picker-wrap input {
  flex: 1;
  cursor: pointer;
}

.icon-clear {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: var(--color-text-secondary);
  background: var(--color-background-soft);
  cursor: pointer;
  z-index: 1;
}

.icon-clear:hover {
  background: #e0e0e0;
  color: var(--color-text);
}

/* 图标选择下拉面板 */
.icon-picker-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 4px;
  background: #fff;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
  z-index: 200;
  overflow: hidden;
}

.icon-picker-header {
  padding: 8px;
  border-bottom: 1px solid var(--color-border);
}

.icon-search {
  width: 100%;
  padding: 6px 10px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 13px;
  outline: none;
}

.icon-search:focus {
  border-color: var(--color-primary);
}

.icon-picker-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 4px;
  padding: 8px;
  max-height: 240px;
  overflow-y: auto;
}

.icon-option {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 5px 8px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background 0.15s;
  color: var(--color-text);
  font-size: 12px;
}

.icon-option:hover {
  background: var(--color-primary-light);
  color: var(--color-primary);
}

.icon-option.selected {
  background: var(--color-primary);
  color: #fff;
}

.icon-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 11px;
}

.icon-no-result {
  grid-column: 1 / -1;
  text-align: center;
  padding: 16px;
  color: var(--color-text-tertiary);
  font-size: 13px;
}

.icon-picker-footer {
  display: flex;
  justify-content: flex-end;
  gap: 6px;
  padding: 8px;
  border-top: 1px solid var(--color-border);
}

.btn-sm {
  padding: 4px 12px;
  font-size: 12px;
  border-radius: var(--radius-sm);
}

.form-group {
  position: relative;
}
</style>
