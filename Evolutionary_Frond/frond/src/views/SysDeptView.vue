<template>
  <div class="sys-dept-view">
    <div class="page-header">
      <h1 class="page-title">部门管理</h1>
      <div class="header-actions">
        <button class="btn btn-primary" @click="openAddModal">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="12" y1="5" x2="12" y2="19"></line>
            <line x1="5" y1="12" x2="19" y2="12"></line>
          </svg>
          <span>添加部门</span>
        </button>
      </div>
    </div>

    <!-- Main Content Area -->
    <div class="main-content">
      <!-- Left: Department Tree -->
      <div class="dept-tree-panel">
        <div class="panel-header">
          <h3>部门架构</h3>
          <button class="btn btn-secondary btn-sm" @click="loadDeptTree">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"></path>
              <path d="M3 3v5h5"></path>
            </svg>
            <span>刷新</span>
          </button>
        </div>

        <!-- Tree Search -->
        <div class="tree-search">
          <input
            type="text"
            v-model="treeSearchKeyword"
            placeholder="搜索部门名称..."
            @input="handleTreeSearch"
          />
        </div>

        <!-- Tree Content -->
        <div class="tree-content">
          <div v-if="loadingTree" class="loading-cell">
            <div class="loading-spinner"></div>
            <span>加载中...</span>
          </div>
          <div v-else-if="filteredDeptTree.length === 0" class="empty-cell">
            暂无部门数据
          </div>
          <div v-else class="dept-tree">
            <DeptTreeNode
              v-for="node in filteredDeptTree"
              :key="node.id"
              :node="node"
              :selected-id="selectedDeptId"
              :expanded-ids="expandedDeptIds"
              @select="handleSelectDept"
              @toggle-expand="handleToggleExpand"
            />
          </div>
        </div>
      </div>

      <!-- Right: Department Detail & Users -->
      <div class="dept-detail-panel">
        <!-- Search Area -->
        <div class="search-area">
          <div class="search-row">
            <div class="search-item">
              <label>部门名称</label>
              <input type="text" v-model="searchForm.deptName" placeholder="请输入部门名称" />
            </div>
            <div class="search-item">
              <label>部门编码</label>
              <input type="text" v-model="searchForm.deptCode" placeholder="请输入部门编码" />
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

        <!-- Department Detail Section -->
        <div v-if="selectedDept" class="dept-detail-section">
          <div class="section-header">
            <h3>部门信息</h3>
            <div class="section-actions">
              <button class="btn btn-secondary btn-sm" @click="openEditModal(selectedDept)">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                </svg>
                <span>编辑</span>
              </button>
              <button class="btn btn-danger btn-sm" @click="handleDelete(selectedDept.id)">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6"></polyline>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                </svg>
                <span>删除</span>
              </button>
            </div>
          </div>
          <div class="detail-grid">
            <div class="detail-cell">
              <span class="detail-label">部门名称</span>
              <span class="detail-value">{{ selectedDept.deptName }}</span>
            </div>
            <div class="detail-cell">
              <span class="detail-label">部门编码</span>
              <span class="detail-value">{{ selectedDept.deptCode || '-' }}</span>
            </div>
            <div class="detail-cell">
              <span class="detail-label">负责人</span>
              <span class="detail-value">{{ selectedDept.leader || '-' }}</span>
            </div>
            <div class="detail-cell">
              <span class="detail-label">联系电话</span>
              <span class="detail-value">{{ selectedDept.phone || '-' }}</span>
            </div>
            <div class="detail-cell">
              <span class="detail-label">邮箱</span>
              <span class="detail-value">{{ selectedDept.email || '-' }}</span>
            </div>
            <div class="detail-cell">
              <span class="detail-label">排序</span>
              <span class="detail-value">{{ selectedDept.sort }}</span>
            </div>
            <div class="detail-cell">
              <span class="detail-label">状态</span>
              <span class="detail-value">
                <span class="status-tag" :class="selectedDept.status === 1 ? 'active' : 'inactive'">
                  {{ selectedDept.status === 1 ? '正常' : '禁用' }}
                </span>
              </span>
            </div>
            <div class="detail-cell">
              <span class="detail-label">创建时间</span>
              <span class="detail-value">{{ formatTime(selectedDept.createTime) }}</span>
            </div>
            <div class="detail-cell detail-cell-full">
              <span class="detail-label">备注</span>
              <span class="detail-value">{{ selectedDept.remark || '-' }}</span>
            </div>
          </div>
        </div>

        <!-- Users Section -->
        <div v-if="selectedDept" class="users-section">
          <div class="section-header">
            <h3>部门用户</h3>
            <div class="section-actions">
              <button class="btn btn-primary btn-sm" @click="openAddUserModal">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                  <circle cx="8.5" cy="7" r="4"></circle>
                  <line x1="20" y1="8" x2="20" y2="14"></line>
                  <line x1="14" y1="11" x2="26" y2="11"></line>
                </svg>
                <span>添加用户</span>
              </button>
              <button class="btn btn-secondary btn-sm" @click="openAddUserByRoleModal">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                  <circle cx="9" cy="7" r="4"></circle>
                  <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                  <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                </svg>
                <span>按角色添加</span>
              </button>
              <button class="btn btn-danger btn-sm" @click="handleRemoveSelectedUsers" :disabled="selectedUserIds.length === 0">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6"></polyline>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                </svg>
                <span>移除选中</span>
              </button>
            </div>
          </div>

          <!-- Users Table -->
          <div class="users-table-container">
            <table class="users-table">
              <thead>
                <tr>
                  <th class="checkbox-col">
                    <input
                      type="checkbox"
                      :checked="isAllUsersSelected"
                      :indeterminate="isUsersIndeterminate"
                      @change="handleToggleAllUsers"
                    />
                  </th>
                  <th>用户名</th>
                  <th>真实姓名</th>
                  <th>邮箱</th>
                  <th>手机号</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="loadingUsers">
                  <td colspan="7" class="loading-cell">
                    <div class="loading-spinner"></div>
                    <span>加载中...</span>
                  </td>
                </tr>
                <tr v-else-if="deptUsers.length === 0">
                  <td colspan="7" class="empty-cell">暂无部门用户</td>
                </tr>
                <tr v-else v-for="user in deptUsers" :key="user.id">
                  <td class="checkbox-col">
                    <input type="checkbox" :value="user.id" v-model="selectedUserIds" />
                  </td>
                  <td>{{ user.username }}</td>
                  <td>{{ user.realName || '-' }}</td>
                  <td>{{ user.email || '-' }}</td>
                  <td>{{ user.phone || '-' }}</td>
                  <td>
                    <span class="status-tag" :class="user.status === 1 ? 'active' : 'inactive'">
                      {{ user.status === 1 ? '正常' : '禁用' }}
                    </span>
                  </td>
                  <td class="action-col">
                    <button class="btn-icon btn-danger-icon" @click="handleRemoveUser(user.id)" title="移除">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <line x1="18" y1="6" x2="6" y2="18"></line>
                        <line x1="6" y1="6" x2="18" y2="18"></line>
                      </svg>
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Pagination -->
          <div class="pagination">
            <div class="page-size-select">
              <span>每页</span>
              <select v-model.number="userPageSize" @change="handleUserPageSizeChange">
                <option :value="10">10</option>
                <option :value="20">20</option>
                <option :value="50">50</option>
                <option :value="100">100</option>
              </select>
              <span>条</span>
            </div>
            <span class="page-info">共 {{ userTotal }} 条</span>
            <button class="btn btn-secondary" :disabled="userCurrentPage === 1" @click="handleUserPageChange(userCurrentPage - 1)">
              上一页
            </button>
            <span class="page-info">第 {{ userCurrentPage }} / {{ userTotalPages }} 页</span>
            <button class="btn btn-secondary" :disabled="userCurrentPage >= userTotalPages" @click="handleUserPageChange(userCurrentPage + 1)">
              下一页
            </button>
            <div class="page-jump">
              <span>跳至</span>
              <input type="number" v-model.number="userJumpPage" :min="1" :max="userTotalPages" @keyup.enter="handleUserJumpPage" />
              <span>页</span>
              <button class="btn btn-secondary btn-sm" @click="handleUserJumpPage">跳转</button>
            </div>
          </div>
        </div>

        <!-- No Selection Hint -->
        <div v-if="!selectedDept" class="no-selection-hint">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
            <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"></path>
          </svg>
          <p>请在左侧选择一个部门查看详情</p>
        </div>
      </div>
    </div>

    <!-- Add/Edit Department Modal -->
    <div v-if="showDeptModal" class="modal-overlay" @click="closeDeptModal">
      <div class="modal-content dept-modal" @click.stop>
        <div class="modal-header">
          <h2>{{ isEdit ? '编辑部门' : '添加部门' }}</h2>
          <button class="modal-close" @click="closeDeptModal">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="form-grid">
            <div class="form-group form-group-full">
              <label>上级部门</label>
              <select v-model="deptForm.parentId">
                <option value="0">顶级部门</option>
                <option v-for="dept in availableParentDepts" :key="dept.id" :value="dept.id">
                  {{ dept.deptName }}
                </option>
              </select>
            </div>
            <div class="form-group">
              <label>部门名称 <span class="required">*</span></label>
              <input type="text" v-model="deptForm.deptName" placeholder="请输入部门名称" />
            </div>
            <div class="form-group">
              <label>部门编码</label>
              <input type="text" v-model="deptForm.deptCode" placeholder="请输入部门编码" />
            </div>
            <div class="form-group">
              <label>排序</label>
              <input type="number" v-model="deptForm.sort" placeholder="请输入排序值" />
            </div>
            <div class="form-group">
              <label>负责人</label>
              <div class="leader-select-wrapper">
                <input type="text" v-model="deptForm.leader" placeholder="点击选择负责人" readonly @click="openLeaderSelectModal" class="leader-input" />
                <button class="btn btn-secondary btn-sm leader-btn" @click="openLeaderSelectModal">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                    <circle cx="12" cy="7" r="4"></circle>
                  </svg>
                </button>
              </div>
            </div>
            <div class="form-group">
              <label>联系电话</label>
              <input type="text" v-model="deptForm.phone" placeholder="选择负责人后自动填充" readonly class="auto-fill-input" />
            </div>
            <div class="form-group">
              <label>邮箱</label>
              <input type="email" v-model="deptForm.email" placeholder="选择负责人后自动填充" readonly class="auto-fill-input" />
            </div>
            <div class="form-group">
              <label>状态</label>
              <select v-model="deptForm.status">
                <option :value="1">正常</option>
                <option :value="0">禁用</option>
              </select>
            </div>
            <div class="form-group form-group-full">
              <label>备注</label>
              <textarea v-model="deptForm.remark" placeholder="请输入备注"></textarea>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeDeptModal">取消</button>
          <button class="btn btn-primary" @click="handleSaveDept" :disabled="savingDept">
            {{ savingDept ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Add User Modal -->
    <div v-if="showAddUserModal" class="modal-overlay" @click="closeAddUserModal">
      <div class="modal-content user-select-modal" @click.stop>
        <div class="modal-header">
          <h2>添加用户到部门</h2>
          <button class="modal-close" @click="closeAddUserModal">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="user-search">
            <input type="text" v-model="userSearchKeyword" placeholder="搜索用户名或姓名..." @input="handleUserSearch" />
          </div>
          <div class="user-select-list">
            <div v-if="loadingAvailableUsers" class="loading-cell">
              <div class="loading-spinner"></div>
              <span>加载中...</span>
            </div>
            <div v-else-if="availableUsers.length === 0" class="empty-cell">
              暂无可添加用户
            </div>
            <div v-else v-for="user in availableUsers" :key="user.id" class="user-select-item">
              <input type="checkbox" :value="user.id" v-model="selectedAddUserIds" />
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
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeAddUserModal">取消</button>
          <button class="btn btn-primary" @click="handleAddUsers" :disabled="selectedAddUserIds.length === 0">
            添加选中 ({{ selectedAddUserIds.length }})
          </button>
        </div>
      </div>
    </div>

    <!-- Add User by Role Modal -->
    <div v-if="showAddUserByRoleModal" class="modal-overlay" @click="closeAddUserByRoleModal">
      <div class="modal-content role-select-modal" @click.stop>
        <div class="modal-header">
          <h2>按角色添加用户</h2>
          <button class="modal-close" @click="closeAddUserByRoleModal">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <p class="hint-text">选择角色后，该角色下的所有用户将被添加到当前部门。</p>
          <div class="role-select-list">
            <div v-if="loadingRoles" class="loading-cell">
              <div class="loading-spinner"></div>
              <span>加载中...</span>
            </div>
            <div v-else-if="roles.length === 0" class="empty-cell">
              暂无角色数据
            </div>
            <div v-else v-for="role in roles" :key="role.id" class="role-select-item">
              <input type="checkbox" :value="role.id" v-model="selectedRoleIds" />
              <span class="role-name">{{ role.roleName }}</span>
              <span class="role-code">{{ role.roleCode }}</span>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeAddUserByRoleModal">取消</button>
          <button class="btn btn-primary" @click="handleAddUsersByRoles" :disabled="selectedRoleIds.length === 0">
            添加选中角色 ({{ selectedRoleIds.length }})
          </button>
        </div>
      </div>
    </div>

    <!-- Leader Select Modal -->
    <div v-if="showLeaderSelectModal" class="modal-overlay" @click="closeLeaderSelectModal">
      <div class="modal-content leader-select-modal" @click.stop>
        <div class="modal-header">
          <h2>选择负责人</h2>
          <button class="modal-close" @click="closeLeaderSelectModal">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="user-search">
            <input type="text" v-model="leaderSearchKeyword" placeholder="搜索用户名或姓名..." @input="handleLeaderSearch" />
          </div>
          <div class="leader-select-list">
            <div v-if="loadingLeaderUsers" class="loading-cell">
              <div class="loading-spinner"></div>
              <span>加载中...</span>
            </div>
            <div v-else-if="leaderUsers.length === 0" class="empty-cell">
              暂无用户数据
            </div>
            <div v-else v-for="user in leaderUsers" :key="user.id" class="leader-select-item" @click="handleSelectLeader(user)">
              <div class="user-avatar-small">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                  <circle cx="12" cy="7" r="4"></circle>
                </svg>
              </div>
              <div class="user-info-small">
                <span class="user-name">{{ user.realName || user.username }}</span>
                <span class="user-detail">{{ user.phone || '无电话' }} | {{ user.email || '无邮箱' }}</span>
              </div>
            </div>
          </div>
          <div v-if="leaderUserTotal > leaderPageSize" class="mini-pagination">
            <button class="btn btn-secondary btn-sm" :disabled="leaderUserPage === 1" @click="handleLeaderUserPageChange(leaderUserPage - 1)">
              上页
            </button>
            <span class="page-info">{{ leaderUserPage }}/{{ leaderUserPages }}</span>
            <button class="btn btn-secondary btn-sm" :disabled="leaderUserPage >= leaderUserPages" @click="handleLeaderUserPageChange(leaderUserPage + 1)">
              下页
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, defineComponent, h } from 'vue'
import { useToast } from '@/composables/useToast'
import {
  getDeptTree,
  getDeptById,
  addDept,
  updateDept,
  deleteDept,
  getDeptUserIds,
  assignUsersToDept,
  assignUsersByRolesToDept,
  removeUsersFromDept,
  type SysDept,
  type DeptTreeNode
} from '@/utils/sysDeptApi'
import { getUserList, type SysUser } from '@/utils/sysUserApi'
import { getRoleList, type SysRole } from '@/utils/sysRoleApi'

const { showSuccess, showError, showWarning } = useToast()

// Tree Node Component
const DeptTreeNode = defineComponent({
  name: 'DeptTreeNode',
  props: {
    node: { type: Object as () => DeptTreeNode, required: true },
    selectedId: { type: String, default: '' },
    expandedIds: { type: Array as () => string[], default: () => [] },
    depth: { type: Number, default: 0 }
  },
  emits: ['select', 'toggle-expand'],
  setup(props, { emit }) {
    const isSelected = computed(() => props.node.id === props.selectedId)
    const isExpanded = computed(() => props.expandedIds.includes(props.node.id))
    const hasChildren = computed(() => props.node.children && props.node.children.length > 0)

    const handleClick = () => {
      emit('select', props.node)
    }

    const handleToggleExpand = (e: Event) => {
      e.stopPropagation()
      emit('toggle-expand', props.node.id)
    }

    return () => {
      const children = hasChildren.value && isExpanded.value
        ? props.node.children!.map(child =>
          h(DeptTreeNode, {
            key: child.id,
            node: child,
            selectedId: props.selectedId,
            expandedIds: props.expandedIds,
            depth: props.depth + 1,
            onSelect: (n: DeptTreeNode) => emit('select', n),
            onToggleExpand: (id: string) => emit('toggle-expand', id)
          })
        )
        : null

      return h('div', { class: 'tree-node-wrapper' }, [
        h('div', {
          class: ['tree-node-item', { selected: isSelected.value }],
          onClick: handleClick
        }, [
          h('span', {
            class: ['expand-icon', { hidden: !hasChildren.value }],
            onClick: handleToggleExpand
          }, isExpanded.value ? '▼' : '▶'),
          h('span', { class: 'node-icon' }, [
            h('svg', {
              width: 16,
              height: 16,
              viewBox: '0 0 24 24',
              fill: 'none',
              stroke: 'currentColor',
              'stroke-width': 2
            }, [
              h('path', { d: 'M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z' })
            ])
          ]),
          h('span', { class: 'node-name' }, props.node.deptName),
          h('span', { class: ['node-status', props.node.status === 1 ? 'active' : 'inactive'] },
            props.node.status === 1 ? '正常' : '禁用')
        ]),
        hasChildren.value && isExpanded.value
          ? h('div', { class: ['tree-children', `level-${Math.min(props.depth + 1, 3)}`] }, children)
          : null
      ])
    }
  }
})

// Department Tree State
const loadingTree = ref(false)
const deptTree = ref<DeptTreeNode[]>([])
const allDepts = ref<SysDept[]>([])
const selectedDeptId = ref('')
const selectedDept = ref<SysDept | null>(null)
const expandedDeptIds = ref<string[]>([])
const treeSearchKeyword = ref('')

// Department Form State
const showDeptModal = ref(false)
const isEdit = ref(false)
const savingDept = ref(false)
const deptForm = ref({
  id: '',
  parentId: '0',
  deptName: '',
  deptCode: '',
  sort: 0,
  leader: '',
  leaderId: '',
  phone: '',
  email: '',
  status: 1,
  remark: ''
})

// Search Form
const searchForm = ref({
  deptName: '',
  deptCode: '',
  status: ''
})

// Department Users State
const loadingUsers = ref(false)
const deptUsers = ref<SysUser[]>([])
const selectedUserIds = ref<string[]>([])
const userCurrentPage = ref(1)
const userPageSize = ref(10)
const userTotal = ref(0)
const userJumpPage = ref(1)

// Add User Modal State
const showAddUserModal = ref(false)
const loadingAvailableUsers = ref(false)
const availableUsers = ref<SysUser[]>([])
const availableUserPage = ref(1)
const availableUserSize = ref(10)
const availableUserTotal = ref(0)
const selectedAddUserIds = ref<string[]>([])
const userSearchKeyword = ref('')

// Add User by Role Modal State
const showAddUserByRoleModal = ref(false)
const loadingRoles = ref(false)
const roles = ref<SysRole[]>([])
const selectedRoleIds = ref<string[]>([])

// Leader Select Modal State
const showLeaderSelectModal = ref(false)
const loadingLeaderUsers = ref(false)
const leaderUsers = ref<SysUser[]>([])
const leaderUserPage = ref(1)
const leaderPageSize = ref(10)
const leaderUserTotal = ref(0)
const leaderSearchKeyword = ref('')
const selectedLeaderUser = ref<SysUser | null>(null)

// Computed
const userTotalPages = computed(() => Math.ceil(userTotal.value / userPageSize.value) || 1)
const availableUserPages = computed(() => Math.ceil(availableUserTotal.value / availableUserSize.value) || 1)
const leaderUserPages = computed(() => Math.ceil(leaderUserTotal.value / leaderPageSize.value) || 1)

const filteredDeptTree = computed(() => {
  if (!treeSearchKeyword.value) return deptTree.value
  const keyword = treeSearchKeyword.value.toLowerCase()
  return filterTreeNodes(deptTree.value, keyword)
})

const availableParentDepts = computed(() => {
  // Filter out current dept and its children when editing
  if (!isEdit.value) return allDepts.value
  const currentId = deptForm.value.id
  return allDepts.value.filter(d => d.id !== currentId && !d.ancestors?.includes(currentId))
})

const isAllUsersSelected = computed(() => {
  if (deptUsers.value.length === 0) return false
  return selectedUserIds.value.length === deptUsers.value.length
})

const isUsersIndeterminate = computed(() => {
  if (deptUsers.value.length === 0) return false
  return selectedUserIds.value.length > 0 && selectedUserIds.value.length < deptUsers.value.length
})

// Tree Filter Function
function filterTreeNodes(nodes: DeptTreeNode[], keyword: string): DeptTreeNode[] {
  const result: DeptTreeNode[] = []
  for (const node of nodes) {
    const nameMatch = node.deptName.toLowerCase().includes(keyword)
    const codeMatch = node.deptCode?.toLowerCase().includes(keyword)
    if (nameMatch || codeMatch) {
      result.push({ ...node, expanded: true })
    } else if (node.children) {
      const filteredChildren = filterTreeNodes(node.children, keyword)
      if (filteredChildren.length > 0) {
        result.push({ ...node, children: filteredChildren, expanded: true })
      }
    }
  }
  return result
}

// Initialize
onMounted(() => {
  loadDeptTree()
})

// Load Department Tree (supports optional filtering)
const loadDeptTree = async (deptName?: string, deptCode?: string, status?: number) => {
  loadingTree.value = true
  try {
    const response = await getDeptTree(deptName, deptCode, status)
    if (response.code === 200 && response.data) {
      // Backend returns flat list — build tree structure for left panel
      deptTree.value = buildTree(response.data)
      // Expand all nodes by default
      expandedDeptIds.value = collectAllIds(deptTree.value)
      // Store flat list for parent selection dropdown
      allDepts.value = response.data
    }
  } catch (error) {
    console.error('Load department tree failed:', error)
    showError('加载部门树失败')
  } finally {
    loadingTree.value = false
  }
}

// Build tree structure from flat list
function buildTree(flatList: SysDept[]): DeptTreeNode[] {
  const idMap = new Map<string, DeptTreeNode>()
  const roots: DeptTreeNode[] = []

  // Create all nodes
  for (const item of flatList) {
    idMap.set(item.id, { ...item, children: [] })
  }

  // Build parent-child relationships
  for (const item of flatList) {
    const node = idMap.get(item.id)!
    const parentId = item.parentId || '0'
    if (parentId === '0') {
      roots.push(node)
    } else {
      const parent = idMap.get(parentId)
      if (parent) {
        parent.children!.push(node)
      }
    }
  }

  return roots
}

// Collect all IDs from tree
function collectAllIds(nodes: DeptTreeNode[]): string[] {
  const ids: string[] = []
  for (const node of nodes) {
    ids.push(node.id)
    if (node.children) {
      ids.push(...collectAllIds(node.children))
    }
  }
  return ids
}

// Handle Tree Search
const handleTreeSearch = () => {
  // The filteredDeptTree computed handles this
}

// Select Department
const handleSelectDept = async (node: DeptTreeNode) => {
  selectedDeptId.value = node.id
  try {
    const response = await getDeptById(node.id)
    if (response.code === 200 && response.data) {
      selectedDept.value = response.data
      // Load department users
      userCurrentPage.value = 1
      loadDeptUsers()
    }
  } catch (error) {
    console.error('Get department detail failed:', error)
    showError('获取部门详情失败')
  }
}

// Toggle Expand
const handleToggleExpand = (deptId: string) => {
  const index = expandedDeptIds.value.indexOf(deptId)
  if (index >= 0) {
    expandedDeptIds.value.splice(index, 1)
  } else {
    expandedDeptIds.value.push(deptId)
  }
}

// Load Department Users
const loadDeptUsers = async () => {
  if (!selectedDeptId.value) return
  loadingUsers.value = true
  try {
    // Get user IDs in department
    const userIdsResponse = await getDeptUserIds(selectedDeptId.value)
    if (userIdsResponse.code === 200 && userIdsResponse.data) {
      const userIds = userIdsResponse.data
      if (userIds.length === 0) {
        deptUsers.value = []
        userTotal.value = 0
        return
      }
      // Get user details by fetching all users and filtering
      // Note: This could be optimized with a dedicated API
      const allUsersResponse = await getUserList(1, 1000)
      if (allUsersResponse.code === 200 && allUsersResponse.data) {
        const allUsers = allUsersResponse.data.records
        // Filter users that are in this department
        deptUsers.value = allUsers.filter(u => userIds.includes(u.id))
        userTotal.value = deptUsers.value.length
        // Apply pagination
        applyUserPagination()
      }
    }
  } catch (error) {
    console.error('Load department users failed:', error)
    showError('加载部门用户失败')
  } finally {
    loadingUsers.value = false
  }
}

// Apply pagination to users
function applyUserPagination() {
  const start = (userCurrentPage.value - 1) * userPageSize.value
  const end = start + userPageSize.value
  // Note: deptUsers is already filtered, pagination is simulated
  // In real implementation, this should be handled by backend
}

// Handle Search
const handleSearch = () => {
  const deptName = searchForm.value.deptName || undefined
  const deptCode = searchForm.value.deptCode || undefined
  const status = searchForm.value.status !== '' ? Number(searchForm.value.status) : undefined
  loadDeptTree(deptName, deptCode, status)
}

// Handle Reset
const handleReset = () => {
  searchForm.value = {
    deptName: '',
    deptCode: '',
    status: ''
  }
  treeSearchKeyword.value = ''
  loadDeptTree()
}

// Open Add Modal
const openAddModal = () => {
  isEdit.value = false
  deptForm.value = {
    id: '',
    parentId: selectedDeptId.value || '0',
    deptName: '',
    deptCode: '',
    sort: 0,
    leader: '',
    leaderId: '',
    phone: '',
    email: '',
    status: 1,
    remark: ''
  }
  showDeptModal.value = true
}

// Open Edit Modal
const openEditModal = (dept: SysDept) => {
  isEdit.value = true
  deptForm.value = {
    id: dept.id,
    parentId: dept.parentId || '0',
    deptName: dept.deptName,
    deptCode: dept.deptCode || '',
    sort: dept.sort,
    leader: dept.leader || '',
    leaderId: dept.leaderId || '',
    phone: dept.phone || '',
    email: dept.email || '',
    status: dept.status,
    remark: dept.remark || ''
  }
  showDeptModal.value = true
}

// Save Department
const handleSaveDept = async () => {
  if (!deptForm.value.deptName) {
    showWarning('请填写部门名称')
    return
  }

  savingDept.value = true
  try {
    let response
    if (isEdit.value) {
      response = await updateDept({
        id: deptForm.value.id,
        parentId: deptForm.value.parentId,
        deptName: deptForm.value.deptName,
        deptCode: deptForm.value.deptCode,
        sort: deptForm.value.sort,
        leader: deptForm.value.leader,
        leaderId: deptForm.value.leaderId,
        phone: deptForm.value.phone,
        email: deptForm.value.email,
        status: deptForm.value.status,
        remark: deptForm.value.remark
      })
    } else {
      response = await addDept({
        parentId: deptForm.value.parentId,
        deptName: deptForm.value.deptName,
        deptCode: deptForm.value.deptCode,
        sort: deptForm.value.sort,
        leader: deptForm.value.leader,
        leaderId: deptForm.value.leaderId,
        phone: deptForm.value.phone,
        email: deptForm.value.email,
        status: deptForm.value.status,
        remark: deptForm.value.remark
      })
    }

    if (response.code === 200) {
      closeDeptModal()
      loadDeptTree()
      // 如果编辑的是当前选中的部门，刷新详情
      if (isEdit.value && selectedDeptId.value === deptForm.value.id) {
        // 重新加载部门详情
        try {
          const detailResponse = await getDeptById(deptForm.value.id)
          if (detailResponse.code === 200 && detailResponse.data) {
            selectedDept.value = detailResponse.data
          }
        } catch (e) {
          console.error('Refresh dept detail failed:', e)
        }
        // 重新加载部门用户
        userCurrentPage.value = 1
        loadDeptUsers()
      }
      showSuccess(isEdit.value ? '部门修改成功' : '部门添加成功')
    } else {
      showError(response.message || '操作失败')
    }
  } catch (error) {
    console.error('Save department failed:', error)
    showError('保存失败')
  } finally {
    savingDept.value = false
  }
}

// Close Department Modal
const closeDeptModal = () => {
  showDeptModal.value = false
}

// Delete Department
const handleDelete = async (deptId: string) => {
  if (!confirm('确定要删除这个部门吗？删除后将清除该部门下所有用户关联。')) return
  try {
    const response = await deleteDept(deptId)
    if (response.code === 200) {
      if (selectedDeptId.value === deptId) {
        selectedDeptId.value = ''
        selectedDept.value = null
        deptUsers.value = []
      }
      loadDeptTree()
      showSuccess('部门删除成功')
    } else {
      showError(response.message || '删除失败')
    }
  } catch (error) {
    console.error('Delete department failed:', error)
    showError('删除失败')
  }
}

// User Pagination Handlers
const handleUserPageSizeChange = () => {
  userCurrentPage.value = 1
  userJumpPage.value = 1
  loadDeptUsers()
}

const handleUserPageChange = (page: number) => {
  userCurrentPage.value = page
  userJumpPage.value = page
  loadDeptUsers()
}

const handleUserJumpPage = () => {
  if (userJumpPage.value >= 1 && userJumpPage.value <= userTotalPages.value) {
    userCurrentPage.value = userJumpPage.value
    loadDeptUsers()
  }
}

// Toggle All Users Selection
const handleToggleAllUsers = (e: Event) => {
  const checked = (e.target as HTMLInputElement).checked
  if (checked) {
    selectedUserIds.value = deptUsers.value.map(u => u.id)
  } else {
    selectedUserIds.value = []
  }
}

// Remove Single User
const handleRemoveUser = async (userId: string) => {
  if (!selectedDeptId.value) return
  if (!confirm('确定要移除该用户与部门的关联吗？')) return
  try {
    const response = await removeUsersFromDept([userId])
    if (response.code === 200) {
      loadDeptUsers()
      selectedUserIds.value = selectedUserIds.value.filter(id => id !== userId)
      showSuccess('用户移除成功')
    } else {
      showError(response.message || '移除失败')
    }
  } catch (error) {
    console.error('Remove user failed:', error)
    showError('移除失败')
  }
}

// Remove Selected Users
const handleRemoveSelectedUsers = async () => {
  if (!selectedDeptId.value || selectedUserIds.value.length === 0) return
  if (!confirm(`确定要移除选中的 ${selectedUserIds.value.length} 个用户吗？`)) return
  try {
    const response = await removeUsersFromDept(selectedUserIds.value)
    if (response.code === 200) {
      loadDeptUsers()
      selectedUserIds.value = []
      showSuccess('批量移除成功')
    } else {
      showError(response.message || '批量移除失败')
    }
  } catch (error) {
    console.error('Remove users failed:', error)
    showError('批量移除失败')
  }
}

// Open Add User Modal
const openAddUserModal = () => {
  showAddUserModal.value = true
  selectedAddUserIds.value = []
  userSearchKeyword.value = ''
  availableUserPage.value = 1
  loadAvailableUsers()
}

// Load Available Users
const loadAvailableUsers = async () => {
  loadingAvailableUsers.value = true
  try {
    const response = await getUserList(availableUserPage.value, availableUserSize.value)
    if (response.code === 200 && response.data) {
      // Filter out users already in department
      const deptUserIds = deptUsers.value.map(u => u.id)
      availableUsers.value = response.data.records.filter(u => !deptUserIds.includes(u.id))
      availableUserTotal.value = response.data.total - deptUserIds.length
    }
  } catch (error) {
    console.error('Load available users failed:', error)
  } finally {
    loadingAvailableUsers.value = false
  }
}

// Handle User Search
const handleUserSearch = () => {
  availableUserPage.value = 1
  loadAvailableUsers()
}

// Available User Pagination
const handleAvailableUserPageChange = (page: number) => {
  availableUserPage.value = page
  loadAvailableUsers()
}

// Add Users
const handleAddUsers = async () => {
  if (!selectedDeptId.value || selectedAddUserIds.value.length === 0) return
  try {
    const response = await assignUsersToDept(selectedDeptId.value, selectedAddUserIds.value)
    if (response.code === 200) {
      closeAddUserModal()
      loadDeptUsers()
      showSuccess('用户添加成功')
    } else {
      showError(response.message || '添加失败')
    }
  } catch (error) {
    console.error('Add users failed:', error)
    showError('添加失败')
  }
}

// Close Add User Modal
const closeAddUserModal = () => {
  showAddUserModal.value = false
}

// Open Add User by Role Modal
const openAddUserByRoleModal = async () => {
  showAddUserByRoleModal.value = true
  selectedRoleIds.value = []
  await loadRoles()
}

// Load Roles
const loadRoles = async () => {
  loadingRoles.value = true
  try {
    const response = await getRoleList(1, 100)
    if (response.code === 200 && response.data) {
      roles.value = response.data.records
    }
  } catch (error) {
    console.error('Load roles failed:', error)
  } finally {
    loadingRoles.value = false
  }
}

// Add Users by Roles
const handleAddUsersByRoles = async () => {
  if (!selectedDeptId.value || selectedRoleIds.value.length === 0) return
  try {
    const response = await assignUsersByRolesToDept(selectedDeptId.value, selectedRoleIds.value)
    if (response.code === 200) {
      closeAddUserByRoleModal()
      loadDeptUsers()
      showSuccess('用户添加成功')
    } else {
      showError(response.message || '添加失败')
    }
  } catch (error) {
    console.error('Add users by roles failed:', error)
    showError('添加失败')
  }
}

// Close Add User by Role Modal
const closeAddUserByRoleModal = () => {
  showAddUserByRoleModal.value = false
}

// Open Leader Select Modal
const openLeaderSelectModal = async () => {
  showLeaderSelectModal.value = true
  leaderSearchKeyword.value = ''
  leaderUserPage.value = 1
  await loadLeaderUsers()
}

// Load Leader Users
const loadLeaderUsers = async () => {
  loadingLeaderUsers.value = true
  try {
    const response = await getUserList(leaderUserPage.value, leaderPageSize.value)
    if (response.code === 200 && response.data) {
      leaderUsers.value = response.data.records
      leaderUserTotal.value = response.data.total
    }
  } catch (error) {
    console.error('Load leader users failed:', error)
    showError('加载用户列表失败')
  } finally {
    loadingLeaderUsers.value = false
  }
}

// Handle Leader Search
const handleLeaderSearch = () => {
  leaderUserPage.value = 1
  loadLeaderUsers()
}

// Leader User Pagination
const handleLeaderUserPageChange = (page: number) => {
  leaderUserPage.value = page
  loadLeaderUsers()
}

// Select Leader
const handleSelectLeader = (user: SysUser) => {
  selectedLeaderUser.value = user
  // 填充负责人信息
  deptForm.value.leader = user.realName || user.username
  deptForm.value.leaderId = user.id
  deptForm.value.phone = user.phone || ''
  deptForm.value.email = user.email || ''
  closeLeaderSelectModal()
}

// Close Leader Select Modal
const closeLeaderSelectModal = () => {
  showLeaderSelectModal.value = false
}

// Format Time
const formatTime = (time?: string) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
/* ========== Page Layout ========== */
.sys-dept-view {
  padding: 24px 32px;
  min-height: 100vh;
  background-color: #f5f7fa;
}

/* ========== Page Header ========== */
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

/* ========== Main Content Layout ========== */
.main-content {
  display: flex;
  gap: 20px;
  min-height: calc(100vh - 120px);
}

/* ========== Department Tree Panel ========== */
.dept-tree-panel {
  width: 340px;
  background-color: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f5;
}

.panel-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
}

.tree-search {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f5;
}

.tree-search input {
  width: 100%;
  height: 36px;
  padding: 0 12px;
  border: 1px solid #e0e0e8;
  border-radius: 8px;
  font-size: 14px;
  color: #1a1a2e;
  background-color: #ffffff;
  transition: border-color 0.2s;
}

.tree-search input:hover {
  border-color: #c0c0c8;
}

.tree-search input:focus {
  border-color: #4a7cf7;
  outline: none;
}

.tree-content {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;
}

/* ========== Tree Node Styles ========== */
.dept-tree {
  font-size: 14px;
}

.dept-tree :deep(.tree-node-wrapper) {
  margin: 3px 0;
  position: relative;
}

.dept-tree :deep(.tree-node-item) {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
  background-color: #ffffff;
}

.dept-tree :deep(.tree-node-item:hover) {
  background-color: #f5f7fb;
  border-color: #e8e8f0;
  transform: translateX(2px);
}

.dept-tree :deep(.tree-node-item.selected) {
  background: linear-gradient(135deg, #dbeafe 0%, #e0e7ff 100%);
  color: #1d4ed8;
  border: 1px solid #60a5fa;
  box-shadow: 0 2px 8px rgba(96, 165, 250, 0.25);
  font-weight: 600;
}

.dept-tree :deep(.expand-icon) {
  font-size: 10px;
  width: 18px;
  height: 18px;
  line-height: 18px;
  text-align: center;
  color: #6b7280;
  cursor: pointer;
  user-select: none;
  border-radius: 4px;
  transition: all 0.15s;
  background-color: #f3f4f6;
}

.dept-tree :deep(.expand-icon:hover) {
  background-color: #e5e7eb;
  color: #1f2937;
}

.dept-tree :deep(.expand-icon.hidden) {
  visibility: hidden;
}

.dept-tree :deep(.node-icon) {
  color: #6b7280;
  font-size: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 4px;
  background-color: #eef2ff;
}

.dept-tree :deep(.node-icon svg) {
  width: 14px;
  height: 14px;
}

.dept-tree :deep(.node-name) {
  flex: 1;
  font-weight: 500;
  color: #1f2937;
  font-size: 14px;
  letter-spacing: 0.2px;
}

.dept-tree :deep(.node-status) {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 500;
}

.dept-tree :deep(.node-status.active) {
  background-color: #d1fae5;
  color: #047857;
}

.dept-tree :deep(.node-status.inactive) {
  background-color: #fee2e2;
  color: #b91c1c;
}

.dept-tree :deep(.tree-children) {
  padding-left: 22px;
  position: relative;
  margin-left: 12px;
}

/* Vertical line connecting siblings - aligned to center of expand-icon */
.dept-tree :deep(.tree-children::before) {
  content: '';
  position: absolute;
  top: 0;
  bottom: 0;
  left: 12px;
  width: 1px;
  background-color: #d0d0dd;
}

/* Horizontal connector line for each child - the ┌── shape */
.dept-tree :deep(.tree-children > .tree-node-wrapper) {
  position: relative;
}

.dept-tree :deep(.tree-children > .tree-node-wrapper::before) {
  content: '';
  position: absolute;
  left: -10px;
  top: 22px;
  width: 10px;
  height: 1px;
  background-color: #d0d0dd;
}

/* Level background colors - subtle differences */
.dept-tree :deep(.tree-children.level-1 > .tree-node-wrapper > .tree-node-item) {
  background-color: rgba(240, 245, 255, 0.45);
}

.dept-tree :deep(.tree-children.level-2 > .tree-node-wrapper > .tree-node-item) {
  background-color: rgba(228, 238, 250, 0.45);
}

.dept-tree :deep(.tree-children.level-3 > .tree-node-wrapper > .tree-node-item) {
  background-color: rgba(216, 230, 245, 0.45);
}

/* ========== Department Detail Panel ========== */
.dept-detail-panel {
  flex: 1;
  background-color: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
}

/* ========== Search Area ========== */
.search-area {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f5;
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

/* ========== Section Styles ========== */
.dept-detail-section,
.users-section {
  padding: 20px 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
}

.section-actions {
  display: flex;
  gap: 8px;
}

/* ========== Detail Grid (4 columns) ========== */
.detail-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  background-color: #fafafc;
  border-radius: 8px;
  padding: 20px;
}

.detail-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.detail-cell-full {
  grid-column: 1 / -1;
}

.detail-label {
  font-size: 13px;
  color: #8a8aa0;
  font-weight: 500;
}

.detail-value {
  font-size: 14px;
  color: #1a1a2e;
  font-weight: 500;
}


/* ========== Users Table ========== */
.users-section {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.users-table-container {
  flex: 1;
  overflow: auto;
}

.users-table {
  width: 100%;
  border-collapse: collapse;
}

.users-table th {
  background-color: #fafafc;
  padding: 12px 16px;
  text-align: left;
  font-weight: 600;
  font-size: 14px;
  color: #5c5c7a;
  border-bottom: 1px solid #e8e8f0;
}

.users-table td {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f5;
  font-size: 14px;
  color: #1a1a2e;
}

.users-table tbody tr:last-child td {
  border-bottom: none;
}

.users-table tbody tr:hover {
  background-color: #fafafc;
}

.checkbox-col {
  width: 40px;
}

.action-col {
  width: 60px;
}

/* ========== No Selection Hint ========== */
.no-selection-hint {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8a8aa0;
  gap: 16px;
}

.no-selection-hint svg {
  opacity: 0.3;
}

.no-selection-hint p {
  font-size: 14px;
}

/* ========== Loading & Empty States ========== */
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

/* ========== Status Tag ========== */
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

/* ========== Pagination ========== */
.pagination {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #f0f0f5;
}

.page-info {
  font-size: 14px;
  color: #5c5c7a;
}

.page-size-select {
  display: flex;
  align-items: center;
  gap: 8px;
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
}

.page-jump input:hover {
  border-color: #c0c0c8;
}

.page-jump input:focus {
  border-color: #4a7cf7;
  outline: none;
}

/* ========== Modal Styles ========== */
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

.dept-modal {
  max-width: 520px;
}

.user-select-modal,
.role-select-modal {
  max-width: 600px;
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

/* ========== Form Styles ========== */
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.form-grid .form-group {
  margin-bottom: 0;
}

.form-grid .form-group-full {
  grid-column: 1 / -1;
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

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  border-color: #4a7cf7;
  outline: none;
}

/* ========== User/Role Select List ========== */
.user-search {
  margin-bottom: 16px;
}

.user-search input {
  width: 100%;
  height: 36px;
  padding: 0 12px;
  border: 1px solid #e0e0e8;
  border-radius: 8px;
  font-size: 14px;
}

.user-search input:hover {
  border-color: #c0c0c8;
}

.user-search input:focus {
  border-color: #4a7cf7;
  outline: none;
}

.user-select-list,
.role-select-list {
  border: 1px solid #e0e0e8;
  border-radius: 8px;
  max-height: 280px;
  overflow-y: auto;
}

.user-select-item,
.role-select-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f5;
}

.user-select-item:last-child,
.role-select-item:last-child {
  border-bottom: none;
}

.user-select-item:hover,
.role-select-item:hover {
  background-color: #fafafc;
}

.user-name,
.role-name {
  font-weight: 500;
  color: #1a1a2e;
}

.user-email,
.role-code {
  font-size: 12px;
  color: #8a8aa0;
}

.hint-text {
  font-size: 14px;
  color: #5c5c7a;
  margin-bottom: 16px;
}

/* ========== Mini Pagination ========== */
.mini-pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  padding: 12px 0;
}

.mini-pagination .page-info {
  font-size: 12px;
  color: #8a8aa0;
}

/* ========== Button Styles ========== */
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

/* ========== Leader Select Styles ========== */
.leader-select-wrapper {
  display: flex;
  gap: 8px;
}

.leader-input {
  flex: 1;
  cursor: pointer;
  background-color: #fafafc;
}

.leader-input:hover {
  border-color: #c0c0c8;
  background-color: #f0f0f5;
}

.leader-input:focus {
  border-color: #4a7cf7;
  outline: none;
  background-color: #ffffff;
}

.leader-btn {
  width: 40px;
  padding: 0 8px;
}

.auto-fill-input {
  background-color: #fafafc;
  cursor: default;
}

.auto-fill-input:hover {
  border-color: #e0e0e8;
}

.leader-select-modal {
  max-width: 480px;
}

.leader-select-list {
  border: 1px solid #e0e0e8;
  border-radius: 8px;
  max-height: 320px;
  overflow-y: auto;
}

.leader-select-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f5;
  cursor: pointer;
  transition: background-color 0.2s;
}

.leader-select-item:last-child {
  border-bottom: none;
}

.leader-select-item:hover {
  background-color: #e8f4fd;
}

.user-avatar-small {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: #f0f0f5;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8a8aa0;
}

.user-info-small {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-info-small .user-name {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a2e;
}

.user-detail {
  font-size: 12px;
  color: #8a8aa0;
}
</style>
