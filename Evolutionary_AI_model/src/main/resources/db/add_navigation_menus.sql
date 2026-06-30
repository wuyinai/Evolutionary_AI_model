-- =============================================
-- 将左侧导航栏所有菜单添加到 sys_permission 表
-- 这是为了实现角色的菜单级权限控制：
-- 角色勾选哪些菜单，左侧导航栏就显示哪些菜单
-- =============================================

-- 1. 插入所有顶级导航菜单（parent_id = 0, type = 2 菜单类型）
INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`, `sort`, `visible`, `status`, `create_by`, `remark`)
VALUES 
(10, 0, 'AI对话', NULL, 2, '/chat', 'ChatView', 1, 1, 1, 'admin', 'AI对话菜单'),
(11, 0, '知识库管理', NULL, 2, '/knowledge-base', 'KnowledgeBaseManagement', 2, 1, 1, 'admin', '知识库管理菜单'),
(12, 0, '文档管理', NULL, 2, '/knowledge-document', 'KnowledgeDocumentManagement', 3, 1, 1, 'admin', '文档管理菜单'),
(13, 0, 'Skills仓库', NULL, 2, '/skills', 'SkillManagement', 4, 1, 1, 'admin', 'Skills仓库菜单'),
(14, 0, '角色专家', NULL, 2, '/role-expert', 'RoleExpertView', 5, 1, 1, 'admin', '角色专家菜单'),
(15, 0, '供应商配置', NULL, 2, '/provider-config', 'ProviderConfigManagement', 6, 1, 1, 'admin', '供应商配置菜单'),
(16, 0, '模型配置', NULL, 2, '/model-config', 'ModelConfigManagement', 7, 1, 1, 'admin', '模型配置菜单'),
(17, 0, 'Agent助手', NULL, 2, '/agent', 'AgentView', 8, 1, 1, 'admin', 'Agent助手菜单'),
(18, 0, 'AI热点监控', NULL, 2, '/ai-hotspot-monitor', NULL, 9, 1, 1, 'admin', 'AI热点监控菜单'),
(19, 0, '预留4', NULL, 2, '/reserved-4', NULL, 10, 1, 1, 'admin', '预留菜单');

-- 2. 为管理员角色(role_id=1)分配所有菜单权限（包括原有的100-144和新加的10-19）
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, id FROM `sys_permission` WHERE (id BETWEEN 10 AND 19) OR (id BETWEEN 100 AND 144);

-- =============================================
-- 验证
-- =============================================
SELECT '---- 所有菜单/权限 ----' AS '';
SELECT id, permission_name, permission_type, path, component, sort FROM sys_permission ORDER BY sort, id;

SELECT '---- 管理员角色权限数 ----' AS '';
SELECT COUNT(*) AS perm_count FROM sys_role_permission WHERE role_id = 1;

SELECT '---- 管理员角色所有权限ID ----' AS '';
SELECT permission_id FROM sys_role_permission WHERE role_id = 1 ORDER BY permission_id;
