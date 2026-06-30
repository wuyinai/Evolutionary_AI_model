<template>
  <div class="sys-role-view">
    <div class="page-header">
      <h1 class="page-title">角色管理</h1>
      <div class="header-actions">
        <button class="btn btn-primary" @click="openAddModal">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="12" y1="5" x2="12" y2="19"></line>
            <line x1="5" y1="12" x2="19" y2="12"></line>
          </svg>
          <span>添加角色</span>
        </button>
      </div>
    </div>

    <!-- 角色列表 -->
    <div class="role-table-container">
      <table class="role-table">
        <thead>
          <tr>
            <th>角色名称</th>
            <th>角色编码</th>
            <th>排序</th>
            <th>状态</th>
            <th>备注</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="7" class="loading-cell">
              <div class="loading-spinner"></div>
              <span>加载中...</span>
            </td>
          </tr>
          <tr v-else-if="roles.length === 0">
            <td colspan="7" class="empty-cell">暂无角色数据</td>
          </tr>
          <tr v-else v-for="role in roles" :key="role.id">
            <td>{{ role.roleName }}</td>
            <td>{{ role.roleCode }}</td>
            <td>{{ role.roleSort }}</td>
            <td>
              <span class="status-tag" :class="role.status === 1 ? 'active' : 'inactive'">
                {{ role.status === 1 ? '正常' : '禁用' }}
              </span>
            </td>
            <td>{{ role.remark || '-' }}</td>
            <td>{{ formatTime(role.createTime) }}</td>
            <td class="action-col">
              <button class="btn-icon" @click="openEditModal(role)" title="编辑">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                </svg>
              </button>
              <button class="btn-icon" @click="openUserModal(role)" title="分配用户">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                  <circle cx="9" cy="7" r="4"></circle>
                  <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                  <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                </svg>
              </button>
              <button class="btn-icon btn-danger-icon" @click="handleDelete(role.id)" title="删除">
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

    <!-- 添加/编辑角色弹窗 -->
    <div v-if="showRoleModal" class="modal-overlay" @click="closeRoleModal">
      <div class="modal-content role-edit-modal" @click.stop>
        <div class="modal-header">
          <h2>{{ isEdit ? '编辑角色' : '添加角色' }}</h2>
          <button class="modal-close" @click="closeRoleModal">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <!-- 基本信息 -->
          <div class="form-group">
            <label>角色名称 <span class="required">*</span></label>
            <input type="text" v-model="roleForm.roleName" placeholder="请输入角色名称" />
          </div>
          <div class="form-group">
            <label>角色编码 <span class="required">*</span></label>
            <input type="text" v-model="roleForm.roleCode" placeholder="请输入角色编码" />
          </div>
          <div class="form-group">
            <label>排序</label>
            <input type="number" v-model="roleForm.roleSort" placeholder="请输入排序值" />
          </div>
          <div class="form-group">
            <label>状态</label>
            <select v-model="roleForm.status">
              <option :value="1">正常</option>
              <option :value="0">禁用</option>
            </select>
          </div>
          <div class="form-group">
            <label>备注</label>
            <textarea v-model="roleForm.remark" placeholder="请输入备注"></textarea>
          </div>

          <!-- 权限分配 -->
          <div v-if="isEdit && permissionTree.length > 0" class="permission-section">
            <label class="section-label">菜单权限</label>
            <div class="permission-tree">
              <div v-for="node in permissionTree" :key="node.id" class="tree-node">
                <div class="tree-node-label" @click="toggleNodeExpand(node)">
                  <span class="expand-icon">{{ node.expanded ? '▼' : '▶' }}</span>
                  <input
                    type="checkbox"
                    :checked="isNodeChecked(node)"
                    :indeterminate="isNodeIndeterminate(node)"
                    @change.stop="toggleNodeCheck(node, $event)"
                  />
                  <span class="node-name">{{ node.permissionName }}</span>
                  <span v-if="node.permissionCode" class="node-code">{{ node.permissionCode }}</span>
                </div>
                <div v-if="node.expanded && node.children && node.children.length > 0" class="tree-children" :style="{ paddingLeft: '24px' }">
                  <div v-for="child in node.children" :key="child.id" class="tree-node">
                    <div class="tree-node-label" @click="toggleNodeExpand(child)">
                      <span v-if="child.children && child.children.length > 0" class="expand-icon">{{ child.expanded ? '▼' : '▶' }}</span>
                      <span v-else class="expand-icon" style="visibility:hidden">▶</span>
                      <input
                        type="checkbox"
                        :checked="isNodeChecked(child)"
                        :indeterminate="isNodeIndeterminate(child)"
                        @change.stop="toggleNodeCheck(child, $event)"
                      />
                      <span class="node-name">{{ child.permissionName }}</span>
                      <span v-if="child.permissionCode" class="node-code">{{ child.permissionCode }}</span>
                    </div>
                    <div v-if="child.expanded && child.children && child.children.length > 0" class="tree-children" :style="{ paddingLeft: '24px' }">
                      <div v-for="grandchild in child.children" :key="grandchild.id" class="tree-node">
                        <div class="tree-node-label">
                          <span class="expand-icon" style="visibility:hidden">▶</span>
                          <input
                            type="checkbox"
                            :checked="selectedPermissionIds.has(grandchild.id)"
                            @change="toggleLeafCheck(grandchild.id)"
                          />
                          <span class="node-name">{{ grandchild.permissionName }}</span>
                          <span v-if="grandchild.permissionCode" class="node-code">{{ grandchild.permissionCode }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeRoleModal">取消</button>
          <button class="btn btn-primary" @click="handleSaveRole" :disabled="savingRole">
            {{ savingRole ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 用户分配弹窗 -->
    <div v-if="showUserModal" class="modal-overlay" @click="closeUserModal">
      <div class="modal-content user-modal" @click.stop>
        <div class="modal-header">
          <h2>分配用户 - {{ currentRole?.roleName }}</h2>
          <button class="modal-close" @click="closeUserModal">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="user-section">
            <div class="section-header">
              <h3>已分配用户</h3>
              <button class="btn btn-danger btn-sm" @click="handleRemoveSelected" :disabled="selectedRemoveIds.length === 0">
                移除选中
              </button>
            </div>
            <div class="user-list">
              <div v-if="loadingUsers" class="loading-cell">
                <div class="loading-spinner"></div>
              </div>
              <div v-else-if="assignedUsers.length === 0" class="empty-cell">暂无已分配用户</div>
              <div v-else v-for="user in assignedUsers" :key="user.id" class="user-item">
                <input type="checkbox" :value="user.id" v-model="selectedRemoveIds" />
                <span class="user-name">{{ user.realName || user.username }}</span>
                <span class="user-email">{{ user.email || '-' }}</span>
                <button class="btn-icon btn-danger-icon btn-sm" @click="handleRemoveUser(user.id)" title="移除">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <line x1="18" y1="6" x2="6" y2="18"></line>
                    <line x1="6" y1="6" x2="18" y2="18"></line>
                  </svg>
                </button>
              </div>
            </div>
          </div>

          <div class="user-section">
            <div class="section-header">
              <h3>可添加用户</h3>
              <div class="filter-area">
                <select v-model="selectedDeptId" @change="handleDeptChange" class="dept-select">
                  <option value="">全部部门</option>
                  <option v-for="dept in depts" :key="dept.id" :value="dept.id">
                    {{ dept.deptName }}
                  </option>
                </select>
                <button class="btn btn-primary btn-sm" @click="handleAddSelected" :disabled="selectedAddIds.length === 0">
                  添加选中
                </button>
              </div>
            </div>
            <div class="user-list">
              <div v-if="loadingAvailableUsers" class="loading-cell">
                <div class="loading-spinner"></div>
              </div>
              <div v-else-if="availableUsers.length === 0" class="empty-cell">暂无可添加用户</div>
              <div v-else v-for="user in availableUsers" :key="user.id" class="user-item">
                <input type="checkbox" :value="user.id" v-model="selectedAddIds" />
                <span class="user-name">{{ user.realName || user.username }}</span>
                <span class="user-email">{{ user.email || '-' }}</span>
              </div>
            </div>
            <div v-if="availableUserTotal > 0" class="mini-pagination">
              <button class="btn btn-secondary btn-sm" :disabled="availableUserPage === 1" @click="handleAvailableUserPageChange(availableUserPage - 1)">
                上页
              </button>
              <span class="page-info">{{ availableUserPage }}/{{ availableUserPages }}</span>
              <button class="btn btn-secondary btn-sm" :disabled="availableUserPage >= availableUserPages" @click="handleAvailableUserPageChange(availableUserPage + 1)">
                下页
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import {
  getRoleList,
  addRole,
  updateRole,
  deleteRole,
  getUsersByRoleId,
  assignUsersToRole,
  removeUserFromRole,
  removeUsersFromRole,
  type SysRole,
  type SysUser
} from '@/utils/sysRoleApi'
import { getUserList, type PageResponse } from '@/utils/sysUserApi'
import { getDeptList, type SysDept } from '@/utils/sysDeptApi'
import {
  getPermissionList,
  getRolePermissionIds,
  updateRolePermissions,
  type SysPermission
} from '@/utils/sysPermissionApi'

// 带展开状态和子节点的权限树节点
interface PermissionTreeNode extends SysPermission {
  children?: PermissionTreeNode[]
  expanded?: boolean
}

const loading = ref(false)
const roles = ref<SysRole[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const showRoleModal = ref(false)
const isEdit = ref(false)
const savingRole = ref(false)
const roleForm = ref({
  id: '',
  roleName: '',
  roleCode: '',
  roleSort: 0,
  status: 1,
  remark: ''
})

const showUserModal = ref(false)
const currentRole = ref<SysRole | null>(null)
const loadingUsers = ref(false)
const assignedUsers = ref<SysUser[]>([])

// 可添加用户相关状态
const depts = ref<SysDept[]>([])
const selectedDeptId = ref<string>('')
const availableUsers = ref<SysUser[]>([])
const availableUserTotal = ref(0)
const availableUserPage = ref(1)
const availableUserSize = ref(5)
const loadingAvailableUsers = ref(false)

const selectedRemoveIds = ref<string[]>([])
const selectedAddIds = ref<string[]>([])

// 权限树相关
const allPermissions = ref<SysPermission[]>([])
const permissionTree = ref<PermissionTreeNode[]>([])
const selectedPermissionIds = ref<Set<string>>(new Set())
const loadingPermissions = ref(false)

const totalPages = computed(() => Math.ceil(total.value / pageSize.value) || 1)
const availableUserPages = computed(() => Math.ceil(availableUserTotal.value / availableUserSize.value) || 1)

onMounted(() => {
  loadRoles()
})

const loadRoles = async () => {
  loading.value = true
  try {
    const response = await getRoleList(currentPage.value, pageSize.value)
    if (response.code === 200 && response.data) {
      roles.value = response.data.records
      total.value = response.data.total
    }
  } catch (error) {
    console.error('加载角色列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  loadRoles()
}

const openAddModal = () => {
  isEdit.value = false
  roleForm.value = {
    id: '',
    roleName: '',
    roleCode: '',
    roleSort: 0,
    status: 1,
    remark: ''
  }
  showRoleModal.value = true
}

const openEditModal = async (role: SysRole) => {
  isEdit.value = true
  roleForm.value = {
    id: role.id,
    roleName: role.roleName,
    roleCode: role.roleCode,
    roleSort: role.roleSort,
    status: role.status,
    remark: role.remark || ''
  }
  showRoleModal.value = true
  // 加载权限树和已分配的权限ID
  await loadAllPermissions()
  await loadRolePermissionIds(role.id)
}

// 加载所有权限并构建树
const loadAllPermissions = async () => {
  loadingPermissions.value = true
  try {
    const response = await getPermissionList()
    if (response.code === 200 && response.data) {
      allPermissions.value = response.data
      buildPermissionTree()
    }
  } catch (error) {
    console.error('加载权限列表失败:', error)
  } finally {
    loadingPermissions.value = false
  }
}

// 构建权限树
function buildPermissionTree() {
  const map = new Map<string, PermissionTreeNode>()
  const roots: PermissionTreeNode[] = []

  allPermissions.value.forEach(p => {
    map.set(p.id, { ...p, children: [], expanded: true })
  })

  allPermissions.value.forEach(p => {
    const node = map.get(p.id)!
    if (p.parentId === '0' || !map.has(p.parentId)) {
      roots.push(node)
    } else {
      const parent = map.get(p.parentId)
      if (parent) {
        if (!parent.children) parent.children = []
        parent.children.push(node)
      }
    }
  })

  // 按 sort 排序
  const sortNodes = (nodes: PermissionTreeNode[]) => {
    nodes.sort((a, b) => (a.sort || 0) - (b.sort || 0))
    nodes.forEach(n => {
      if (n.children && n.children.length > 0) sortNodes(n.children)
    })
  }
  sortNodes(roots)

  permissionTree.value = roots
}

// 加载角色已分配的权限ID
const loadRolePermissionIds = async (roleId: string) => {
  try {
    const response = await getRolePermissionIds(roleId)
    if (response.code === 200 && response.data) {
      selectedPermissionIds.value = new Set(response.data)
    }
  } catch (error) {
    console.error('加载角色权限失败:', error)
  }
}

// 判断节点是否被选中（所有子节点都被选中）
function isNodeChecked(node: PermissionTreeNode): boolean {
  if (!node.children || node.children.length === 0) {
    return selectedPermissionIds.value.has(node.id)
  }
  // 所有直接子节点都被选中
  return node.children.every(child => isNodeChecked(child))
}

// 判断节点是否为半选状态（部分子节点被选中）
function isNodeIndeterminate(node: PermissionTreeNode): boolean {
  if (!node.children || node.children.length === 0) return false
  const checkedCount = node.children.filter(child => isNodeChecked(child) || isNodeIndeterminate(child)).length
  return checkedCount > 0 && checkedCount < node.children.length
}

// 切换节点展开/折叠
function toggleNodeExpand(node: PermissionTreeNode) {
  node.expanded = !node.expanded
}

// 切换节点选中状态（级联子节点）
function toggleNodeCheck(node: PermissionTreeNode, event: Event) {
  const checked = (event.target as HTMLInputElement).checked
  const ids = collectAllIds(node)
  ids.forEach(id => {
    if (checked) {
      selectedPermissionIds.value.add(id)
    } else {
      selectedPermissionIds.value.delete(id)
    }
  })
  // 触发响应式更新
  selectedPermissionIds.value = new Set(selectedPermissionIds.value)
}

// 切换叶子节点选中状态
function toggleLeafCheck(id: string) {
  if (selectedPermissionIds.value.has(id)) {
    selectedPermissionIds.value.delete(id)
  } else {
    selectedPermissionIds.value.add(id)
  }
  selectedPermissionIds.value = new Set(selectedPermissionIds.value)
}

// 收集节点及其所有子节点的ID
function collectAllIds(node: PermissionTreeNode): string[] {
  const ids = [node.id]
  if (node.children) {
    node.children.forEach(child => {
      ids.push(...collectAllIds(child))
    })
  }
  return ids
}

const handleSaveRole = async () => {
  if (!roleForm.value.roleName || !roleForm.value.roleCode) {
    alert('请填写角色名称和角色编码')
    return
  }

  savingRole.value = true
  try {
    let response
    if (isEdit.value) {
      response = await updateRole({
        id: roleForm.value.id,
        roleName: roleForm.value.roleName,
        roleCode: roleForm.value.roleCode,
        roleSort: roleForm.value.roleSort,
        status: roleForm.value.status,
        remark: roleForm.value.remark
      })
    } else {
      response = await addRole({
        roleName: roleForm.value.roleName,
        roleCode: roleForm.value.roleCode,
        roleSort: roleForm.value.roleSort,
        status: roleForm.value.status,
        remark: roleForm.value.remark
      })
    }

    if (response.code === 200) {
      // 如果是编辑模式，保存权限分配
      if (isEdit.value && roleForm.value.id) {
        const permissionIds = Array.from(selectedPermissionIds.value)
        await updateRolePermissions(roleForm.value.id, permissionIds)
      }
      closeRoleModal()
      loadRoles()
    } else {
      alert(response.message || '操作失败')
    }
  } catch (error) {
    console.error('保存角色失败:', error)
    alert('保存失败')
  } finally {
    savingRole.value = false
  }
}

const closeRoleModal = () => {
  showRoleModal.value = false
  // 清理权限树状态
  setTimeout(() => {
    permissionTree.value = []
    selectedPermissionIds.value = new Set()
  }, 300)
}

const handleDelete = async (roleId: string) => {
  if (!confirm('确定要删除这个角色吗？删除后将清除该角色下所有用户关联。')) return
  try {
    const response = await deleteRole(roleId)
    if (response.code === 200) {
      loadRoles()
    } else {
      alert(response.message || '删除失败')
    }
  } catch (error) {
    console.error('删除角色失败:', error)
    alert('删除失败')
  }
}

const openUserModal = async (role: SysRole) => {
  currentRole.value = role
  showUserModal.value = true
  selectedRemoveIds.value = []
  selectedAddIds.value = []
  selectedDeptId.value = ''
  availableUserPage.value = 1
  
  // 加载部门列表和已分配用户
  await loadDepts()
  await loadAssignedUsers(role.id)
  await loadAvailableUsers()
}

const closeUserModal = () => {
  showUserModal.value = false
  currentRole.value = null
  assignedUsers.value = []
  availableUsers.value = []
  depts.value = []
}

const loadDepts = async () => {
  try {
    const response = await getDeptList()
    if (response.code === 200 && response.data) {
      depts.value = response.data
    }
  } catch (error) {
    console.error('加载部门列表失败:', error)
  }
}

const loadAssignedUsers = async (roleId: string) => {
  loadingUsers.value = true
  try {
    const response = await getUsersByRoleId(roleId)
    if (response.code === 200 && response.data) {
      assignedUsers.value = response.data
    }
  } catch (error) {
    console.error('加载角色用户失败:', error)
  } finally {
    loadingUsers.value = false
  }
}

const loadAvailableUsers = async () => {
  loadingAvailableUsers.value = true
  try {
    const response = await getUserList(
      availableUserPage.value,
      availableUserSize.value,
      selectedDeptId.value || undefined
    )
    if (response.code === 200 && response.data) {
      // 排除已分配的用户
      const assignedIds = assignedUsers.value.map(u => u.id)
      availableUsers.value = response.data.records.filter(u => !assignedIds.includes(u.id))
      // 重新计算总数（需要考虑排除已分配用户的影响）
      const totalFiltered = response.data.total - assignedIds.length
      availableUserTotal.value = totalFiltered > 0 ? totalFiltered : 0
    }
  } catch (error) {
    console.error('加载可添加用户失败:', error)
  } finally {
    loadingAvailableUsers.value = false
  }
}

const handleDeptChange = () => {
  availableUserPage.value = 1
  loadAvailableUsers()
}

const handleAvailableUserPageChange = (page: number) => {
  availableUserPage.value = page
  loadAvailableUsers()
}

const handleRemoveUser = async (userId: string) => {
  if (!currentRole.value) return
  try {
    const response = await removeUserFromRole(currentRole.value.id, userId)
    if (response.code === 200) {
      await loadAssignedUsers(currentRole.value.id)
      selectedRemoveIds.value = selectedRemoveIds.value.filter(id => id !== userId)
    } else {
      alert(response.message || '移除失败')
    }
  } catch (error) {
    console.error('移除用户失败:', error)
    alert('移除失败')
  }
}

const handleRemoveSelected = async () => {
  if (!currentRole.value || selectedRemoveIds.value.length === 0) return
  if (!confirm(`确定要移除选中的 ${selectedRemoveIds.value.length} 个用户吗？`)) return
  try {
    const response = await removeUsersFromRole(currentRole.value.id, selectedRemoveIds.value)
    if (response.code === 200) {
      await loadAssignedUsers(currentRole.value.id)
      selectedRemoveIds.value = []
    } else {
      alert(response.message || '批量移除失败')
    }
  } catch (error) {
    console.error('批量移除用户失败:', error)
    alert('批量移除失败')
  }
}

const handleAddSelected = async () => {
  if (!currentRole.value || selectedAddIds.value.length === 0) return
  try {
    const response = await assignUsersToRole(currentRole.value.id, selectedAddIds.value)
    if (response.code === 200) {
      await loadAssignedUsers(currentRole.value.id)
      await loadAvailableUsers()
      selectedAddIds.value = []
    } else {
      alert(response.message || '添加失败')
    }
  } catch (error) {
    console.error('添加用户失败:', error)
    alert('添加失败')
  }
}

const formatTime = (time?: string) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.sys-role-view {
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

.role-table-container {
  background-color: #ffffff;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.role-table {
  width: 100%;
  border-collapse: collapse;
}

.role-table th {
  background-color: var(--color-background-soft);
  padding: var(--spacing-md);
  text-align: left;
  font-weight: 600;
  font-size: 14px;
  color: var(--color-text);
  border-bottom: 1px solid var(--color-border);
}

.role-table td {
  padding: var(--spacing-md);
  border-bottom: 1px solid var(--color-border);
  font-size: 14px;
  color: var(--color-text);
}

.role-table tr:hover {
  background-color: var(--color-background-soft);
}

.action-col {
  width: 100px;
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

.btn-sm {
  padding: var(--spacing-xs) var(--spacing-sm);
  font-size: 12px;
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

.user-modal {
  max-width: 700px;
}

.role-edit-modal {
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

/* 用户分配区域 */
.user-section {
  margin-bottom: var(--spacing-lg);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);
}

.section-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text);
}

.user-list {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  max-height: 200px;
  overflow-y: auto;
}

.user-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  border-bottom: 1px solid var(--color-border);
}

.user-item:last-child {
  border-bottom: none;
}

.user-item:hover {
  background-color: var(--color-background-soft);
}

.user-name {
  font-weight: 500;
  color: var(--color-text);
}

.user-email {
  color: var(--color-text-secondary);
  font-size: 12px;
}

.filter-area {
  display: flex;
  gap: var(--spacing-sm);
  align-items: center;
}

.dept-select {
  padding: var(--spacing-xs) var(--spacing-sm);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 12px;
  min-width: 120px;
}

.mini-pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm);
  border-top: 1px solid var(--color-border);
}

.mini-pagination .page-info {
  font-size: 12px;
  color: var(--color-text-secondary);
}

/* 权限树样式 */
.permission-section {
  margin-top: var(--spacing-lg);
  border-top: 1px solid var(--color-border);
  padding-top: var(--spacing-md);
}

.section-label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: var(--spacing-sm);
}

.permission-tree {
  max-height: 350px;
  overflow-y: auto;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--spacing-sm);
  background-color: var(--color-background-soft);
}

.tree-node {
  margin: 2px 0;
}

.tree-node-label {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 6px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: 13px;
}

.tree-node-label:hover {
  background-color: #e8f0fe;
}

.expand-icon {
  font-size: 10px;
  width: 14px;
  text-align: center;
  color: var(--color-text-secondary);
}

.tree-node-label input[type="checkbox"] {
  width: 14px;
  height: 14px;
  cursor: pointer;
}

.node-name {
  font-weight: 500;
  color: var(--color-text);
}

.node-code {
  font-size: 11px;
  color: var(--color-text-tertiary);
  margin-left: 4px;
}

.tree-children {
  border-left: 1px dashed var(--color-border);
  margin-left: 7px;
}
</style>