-- =============================================
-- 初始化角色管理权限数据
-- =============================================

-- 1. 插入角色管理菜单权限（父级菜单）
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`, `icon`, `sort`, `visible`, `status`, `create_by`, `remark`)
VALUES (100, 0, '系统管理', NULL, 1, '/system', NULL, 'setting', 1, 1, 1, 'admin', '系统管理目录');

-- 2. 插入角色管理菜单权限
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`, `icon`, `sort`, `visible`, `status`, `create_by`, `remark`)
VALUES (101, 100, '角色管理', 'sys:role', 2, '/system/role', 'SysRoleView', 'user-group', 1, 1, 1, 'admin', '角色管理菜单');

-- 3. 插入角色管理按钮权限
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`, `icon`, `sort`, `visible`, `status`, `create_by`, `remark`)
VALUES 
(102, 101, '角色查询', 'sys:role:list', 3, NULL, NULL, NULL, 1, 1, 1, 'admin', '角色查询权限'),
(103, 101, '角色添加', 'sys:role:add', 3, NULL, NULL, NULL, 2, 1, 1, 'admin', '角色添加权限'),
(104, 101, '角色编辑', 'sys:role:edit', 3, NULL, NULL, NULL, 3, 1, 1, 'admin', '角色编辑权限'),
(105, 101, '角色删除', 'sys:role:delete', 3, NULL, NULL, NULL, 4, 1, 1, 'admin', '角色删除权限');

-- 4. 插入操作日志菜单权限
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`, `icon`, `sort`, `visible`, `status`, `create_by`, `remark`)
VALUES (110, 100, '操作日志', 'sys:log', 2, '/system/operation-log', 'OperationLogView', 'file-text', 2, 1, 1, 'admin', '操作日志菜单');

-- 5. 插入操作日志按钮权限
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`, `icon`, `sort`, `visible`, `status`, `create_by`, `remark`)
VALUES 
(111, 110, '日志查询', 'sys:log:list', 3, NULL, NULL, NULL, 1, 1, 1, 'admin', '日志查询权限'),
(112, 110, '日志删除', 'sys:log:delete', 3, NULL, NULL, NULL, 2, 1, 1, 'admin', '日志删除权限'),
(113, 110, '日志清空', 'sys:log:clear', 3, NULL, NULL, NULL, 3, 1, 1, 'admin', '日志清空权限');

-- 6. 插入用户管理按钮权限（用于角色分配用户功能）
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`, `icon`, `sort`, `visible`, `status`, `create_by`, `remark`)
VALUES 
(120, 100, '用户查询', 'sys:user:list', 3, NULL, NULL, NULL, 1, 1, 1, 'admin', '用户查询权限（用于角色分配用户）');

-- 7. 插入部门管理按钮权限（用于角色分配用户功能）
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`, `icon`, `sort`, `visible`, `status`, `create_by`, `remark`)
VALUES 
(130, 100, '部门查询', 'sys:dept:list', 3, NULL, NULL, NULL, 1, 1, 1, 'admin', '部门查询权限（用于角色分配用户）');

-- =============================================
-- 8. 创建管理员角色（如果不存在）
-- =============================================
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `role_sort`, `data_scope`, `status`, `create_by`, `remark`)
SELECT 1, '超级管理员', 'admin', 1, 1, 1, 'admin', '超级管理员角色'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_role` WHERE `role_code` = 'admin');

-- =============================================
-- 9. 为管理员角色分配权限
-- =============================================
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, id FROM `sys_permission` WHERE id BETWEEN 100 AND 130
ON DUPLICATE KEY UPDATE `role_id` = `role_id`;

-- =============================================
-- 10. 将用户ID为1的用户分配到管理员角色（如果用户存在）
-- =============================================
INSERT INTO `sys_user_role` (`user_id`, `role_id`)
SELECT 1, 1 FROM DUAL
WHERE EXISTS (SELECT 1 FROM `sys_user` WHERE `id` = 1)
ON DUPLICATE KEY UPDATE `user_id` = `user_id`;

-- =============================================
-- 说明：
-- 执行此脚本后，用户ID为1的用户将拥有管理员角色，具有角色管理所有权限。
-- 如果您的用户ID不是1，请修改第10部分的SQL，将user_id改为您的实际用户ID。
-- 例如：INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (您的用户ID, 1);
-- =============================================