-- =============================================
-- 新增密级标签查询权限（用于角色管理弹窗）
-- =============================================
-- 1. 在角色管理菜单下新增密级标签查询按钮权限
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`, `icon`, `sort`, `visible`, `status`, `create_by`, `remark`)
VALUES (150, 101, '密级标签查询', 'sys:security-label:list', 3, NULL, NULL, NULL, 5, 1, 1, 'admin', '密级标签查询权限（用于角色管理选择密级）');

-- 2. 为管理员角色分配新权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, 150 FROM DUAL
ON DUPLICATE KEY UPDATE `role_id` = `role_id`;
