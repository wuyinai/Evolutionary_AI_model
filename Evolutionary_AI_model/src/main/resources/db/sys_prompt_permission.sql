-- =============================================
-- 系统提示词管理权限初始化脚本
-- 为 SysPromptController 添加权限控制
-- 执行时间：请在现有权限数据基础上执行
-- =============================================

-- =============================================
-- 1. 系统提示词管理菜单（二级菜单）
-- =============================================
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`, `icon`, `sort`, `visible`, `status`, `create_by`, `remark`)
VALUES (290, 200, '系统提示词管理', 'sys:prompt', 2, '/system/prompt', 'SysPromptView', 'file-text', 6, 1, 1, 'admin', '系统提示词管理菜单');

-- =============================================
-- 2. 系统提示词管理按钮权限（三级按钮）
-- =============================================
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`, `icon`, `sort`, `visible`, `status`, `create_by`, `remark`)
VALUES
(291, 290, '提示词列表查询', 'sys:prompt:list', 3, NULL, NULL, NULL, 1, 1, 1, 'admin', '提示词列表查询权限'),
(292, 290, '提示词详情查询', 'sys:prompt:query', 3, NULL, NULL, NULL, 2, 1, 1, 'admin', '提示词详情查询权限'),
(293, 290, '文本型提示词创建', 'sys:prompt:add', 3, NULL, NULL, NULL, 3, 1, 1, 'admin', '文本型提示词创建权限'),
(294, 290, '提示词更新', 'sys:prompt:edit', 3, NULL, NULL, NULL, 4, 1, 1, 'admin', '提示词更新权限'),
(295, 290, '提示词删除', 'sys:prompt:delete', 3, NULL, NULL, NULL, 5, 1, 1, 'admin', '提示词删除权限'),
(296, 290, '文档型提示词上传', 'sys:prompt:upload', 3, NULL, NULL, NULL, 6, 1, 1, 'admin', '文档型提示词上传权限'),
(297, 290, '提示词文档预览', 'sys:prompt:preview', 3, NULL, NULL, NULL, 7, 1, 1, 'admin', '提示词文档预览权限');

-- =============================================
-- 3. 为管理员角色分配新增权限
-- =============================================
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, id FROM `sys_permission` WHERE id BETWEEN 290 AND 297
ON DUPLICATE KEY UPDATE `role_id` = `role_id`;

-- =============================================
-- 说明：
-- 本脚本为 SysPromptController 添加了权限码：
-- 1. sys:prompt:list - 查询提示词列表
-- 2. sys:prompt:query - 查询提示词详情
-- 3. sys:prompt:add - 创建文本型提示词
-- 4. sys:prompt:edit - 更新提示词
-- 5. sys:prompt:delete - 删除提示词
-- 6. sys:prompt:upload - 上传文档型提示词
-- 7. sys:prompt:preview - 获取文档预览URL
-- =============================================