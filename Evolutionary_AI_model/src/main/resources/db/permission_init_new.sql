-- =============================================
-- 新增权限码初始化脚本
-- 为未添加权限码的接口添加权限控制
-- 执行时间：请在现有权限数据基础上执行
-- =============================================

-- =============================================
-- 1. AI中心父级菜单（一级菜单）
-- =============================================
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`, `icon`, `sort`, `visible`, `status`, `create_by`, `remark`)
VALUES (200, 0, 'AI中心', NULL, 1, '/ai', NULL, 'robot', 2, 1, 1, 'admin', 'AI中心目录');

-- =============================================
-- 2. 技能管理菜单及按钮权限
-- =============================================
-- 技能管理菜单（二级菜单）
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`, `icon`, `sort`, `visible`, `status`, `create_by`, `remark`)
VALUES (210, 200, '技能管理', 'skill', 2, '/ai/skill', 'SkillView', 'tool', 1, 1, 1, 'admin', '技能管理菜单');

-- 技能管理按钮权限（三级按钮）
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`, `icon`, `sort`, `visible`, `status`, `create_by`, `remark`)
VALUES 
(211, 13, '技能查询', 'skill:list', 3, NULL, NULL, NULL, 1, 1, 1, 'admin', '技能查询权限'),
(212, 13, '技能上传', 'skill:upload', 3, NULL, NULL, NULL, 2, 1, 1, 'admin', '技能上传权限'),
(213, 13, '技能编辑', 'skill:edit', 3, NULL, NULL, NULL, 3, 1, 1, 'admin', '技能编辑权限'),
(214, 13, '技能删除', 'skill:delete', 3, NULL, NULL, NULL, 4, 1, 1, 'admin', '技能删除权限');

-- =============================================
-- 3. 知识库管理菜单及按钮权限
-- =============================================
-- 知识库管理父级菜单（二级菜单）
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`, `icon`, `sort`, `visible`, `status`, `create_by`, `remark`)
VALUES (220, 200, '知识库管理', 'knowledge', 2, '/ai/knowledge', NULL, 'book', 2, 1, 1, 'admin', '知识库管理目录');

-- 知识库管理菜单（三级菜单）
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`, `icon`, `sort`, `visible`, `status`, `create_by`, `remark`)
VALUES (221, 220, '知识库', 'knowledge:base', 2, '/ai/knowledge/base', 'KnowledgeBaseView', 'database', 1, 1, 1, 'admin', '知识库菜单');

-- 知识库按钮权限
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`, `icon`, `sort`, `visible`, `status`, `create_by`, `remark`)
VALUES 
(222, 11, '知识库查询', 'knowledge:base:list', 3, NULL, NULL, NULL, 1, 1, 1, 'admin', '知识库查询权限'),
(223, 11, '知识库添加', 'knowledge:base:add', 3, NULL, NULL, NULL, 2, 1, 1, 'admin', '知识库添加权限'),
(224, 11, '知识库编辑', 'knowledge:base:edit', 3, NULL, NULL, NULL, 3, 1, 1, 'admin', '知识库编辑权限'),
(225, 11, '知识库删除', 'knowledge:base:delete', 3, NULL, NULL, NULL, 4, 1, 1, 'admin', '知识库删除权限');

-- 知识库文档管理菜单（三级菜单）
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`, `icon`, `sort`, `visible`, `status`, `create_by`, `remark`)
VALUES (226, 220, '文档管理', 'knowledge:document', 2, '/ai/knowledge/document', 'KnowledgeDocumentView', 'file', 2, 1, 1, 'admin', '知识库文档菜单');

-- 知识库文档按钮权限
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`, `icon`, `sort`, `visible`, `status`, `create_by`, `remark`)
VALUES 
(227, 12, '文档查询', 'knowledge:document:list', 3, NULL, NULL, NULL, 1, 1, 1, 'admin', '文档查询权限'),
(228, 12, '文档上传', 'knowledge:document:upload', 3, NULL, NULL, NULL, 2, 1, 1, 'admin', '文档上传权限'),
(229, 12, '文档编辑', 'knowledge:document:edit', 3, NULL, NULL, NULL, 3, 1, 1, 'admin', '文档编辑权限'),
(230, 12, '文档删除', 'knowledge:document:delete', 3, NULL, NULL, NULL, 4, 1, 1, 'admin', '文档删除权限');

-- =============================================
-- 4. AI角色管理菜单及按钮权限
-- =============================================
-- AI角色管理菜单（二级菜单）
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`, `icon`, `sort`, `visible`, `status`, `create_by`, `remark`)
VALUES (240, 200, 'AI角色管理', 'ai:role', 2, '/ai/role', 'AiRoleView', 'user', 3, 1, 1, 'admin', 'AI角色管理菜单');

-- AI角色按钮权限
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`, `icon`, `sort`, `visible`, `status`, `create_by`, `remark`)
VALUES 
(241, 14, 'AI角色查询', 'ai:role:list', 3, NULL, NULL, NULL, 1, 1, 1, 'admin', 'AI角色查询权限'),
(242, 14, 'AI角色添加', 'ai:role:add', 3, NULL, NULL, NULL, 2, 1, 1, 'admin', 'AI角色添加权限'),
(243, 14, 'AI角色编辑', 'ai:role:edit', 3, NULL, NULL, NULL, 3, 1, 1, 'admin', 'AI角色编辑权限'),
(244, 14, 'AI角色删除', 'ai:role:delete', 3, NULL, NULL, NULL, 4, 1, 1, 'admin', 'AI角色删除权限');

-- AI角色文档管理菜单（三级菜单）
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`, `icon`, `sort`, `visible`, `status`, `create_by`, `remark`)
VALUES (245, 240, '角色文档管理', 'ai:role:document', 2, NULL, NULL, 'file-text', 5, 1, 1, 'admin', 'AI角色文档管理菜单');

-- AI角色文档按钮权限
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`, `icon`, `sort`, `visible`, `status`, `create_by`, `remark`)
VALUES 
(246, 14, '角色文档查询', 'ai:role:document:list', 3, NULL, NULL, NULL, 1, 1, 1, 'admin', '角色文档查询权限'),
(247, 14, '角色文档上传', 'ai:role:document:add', 3, NULL, NULL, NULL, 2, 1, 1, 'admin', '角色文档上传权限'),
(248, 14, '角色文档删除', 'ai:role:document:delete', 3, NULL, NULL, NULL, 3, 1, 1, 'admin', '角色文档删除权限');

-- =============================================
-- 5. AI配置管理菜单及按钮权限
-- =============================================
-- AI配置管理父级菜单（二级菜单）
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`, `icon`, `sort`, `visible`, `status`, `create_by`, `remark`)
VALUES (250, 200, 'AI配置管理', 'ai:config', 2, '/ai/config', NULL, 'setting', 4, 1, 1, 'admin', 'AI配置管理目录');

-- AI供应商配置菜单（三级菜单）
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`, `icon`, `sort`, `visible`, `status`, `create_by`, `remark`)
VALUES (251, 250, '供应商配置', 'ai:provider', 2, '/ai/config/provider', 'AiProviderConfigView', 'cloud-server', 1, 1, 1, 'admin', 'AI供应商配置菜单');

-- AI供应商配置按钮权限
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`, `icon`, `sort`, `visible`, `status`, `create_by`, `remark`)
VALUES 
(252, 15, '供应商配置查询', 'ai:provider:list', 3, NULL, NULL, NULL, 1, 1, 1, 'admin', '供应商配置查询权限'),
(253, 15, '供应商配置添加', 'ai:provider:add', 3, NULL, NULL, NULL, 2, 1, 1, 'admin', '供应商配置添加权限'),
(254, 15, '供应商配置编辑', 'ai:provider:edit', 3, NULL, NULL, NULL, 3, 1, 1, 'admin', '供应商配置编辑权限'),
(255, 15, '供应商配置删除', 'ai:provider:delete', 3, NULL, NULL, NULL, 4, 1, 1, 'admin', '供应商配置删除权限'),
(256, 15, '供应商连接测试', 'ai:provider:test', 3, NULL, NULL, NULL, 5, 1, 1, 'admin', '供应商连接测试权限');

-- AI模型配置菜单（三级菜单）
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`, `icon`, `sort`, `visible`, `status`, `create_by`, `remark`)
VALUES (260, 250, '模型配置', 'ai:model', 2, '/ai/config/model', 'AiModelConfigView', 'experiment', 2, 1, 1, 'admin', 'AI模型配置菜单');

-- AI模型配置按钮权限
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`, `icon`, `sort`, `visible`, `status`, `create_by`, `remark`)
VALUES 
(261, 16, '模型配置查询', 'ai:config:list', 3, NULL, NULL, NULL, 1, 1, 1, 'admin', '模型配置查询权限'),
(262, 16, '模型配置添加', 'ai:config:add', 3, NULL, NULL, NULL, 2, 1, 1, 'admin', '模型配置添加权限'),
(263, 16, '模型配置编辑', 'ai:config:edit', 3, NULL, NULL, NULL, 3, 1, 1, 'admin', '模型配置编辑权限'),
(264, 16, '模型配置删除', 'ai:config:delete', 3, NULL, NULL, NULL, 4, 1, 1, 'admin', '模型配置删除权限'),
(265, 16, '模型连接测试', 'ai:config:test', 3, NULL, NULL, NULL, 5, 1, 1, 'admin', '模型连接测试权限');

-- =============================================
-- 6. AI对话管理菜单及按钮权限
-- =============================================
-- AI对话管理菜单（二级菜单）
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`, `icon`, `sort`, `visible`, `status`, `create_by`, `remark`)
VALUES (270, 200, 'AI对话', 'chat', 2, '/ai/chat', 'ChatView', 'message', 5, 1, 1, 'admin', 'AI对话菜单');

-- AI对话按钮权限
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`, `icon`, `sort`, `visible`, `status`, `create_by`, `remark`)
VALUES 
(271, 10, '对话查询', 'chat:list', 3, NULL, NULL, NULL, 1, 1, 1, 'admin', '对话查询权限'),
(272, 10, '流式对话', 'chat:stream', 3, NULL, NULL, NULL, 2, 1, 1, 'admin', '流式对话权限'),
(273, 10, '对话删除', 'chat:delete', 3, NULL, NULL, NULL, 3, 1, 1, 'admin', '对话删除权限');

-- AI Agent管理菜单（三级菜单）
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`, `icon`, `sort`, `visible`, `status`, `create_by`, `remark`)
VALUES (274, 270, 'Agent管理', 'chat:agent', 2, NULL, NULL, 'robot', 4, 1, 1, 'admin', 'AI Agent管理菜单');

-- AI Agent按钮权限
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`, `icon`, `sort`, `visible`, `status`, `create_by`, `remark`)
VALUES 
(275, 17, 'Agent工具查询', 'chat:agent:list', 3, NULL, NULL, NULL, 1, 1, 1, 'admin', 'Agent工具查询权限'),
(276, 17, 'Agent任务执行', 'chat:agent:execute', 3, NULL, NULL, NULL, 2, 1, 1, 'admin', 'Agent任务执行权限');

-- =============================================
-- 7. 文件上传权限
-- =============================================
-- 文件上传菜单（二级菜单，放在系统管理下）
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`, `icon`, `sort`, `visible`, `status`, `create_by`, `remark`)
VALUES (280, 100, '文件管理', 'upload', 2, '/system/upload', 'FileUploadView', 'upload', 5, 1, 1, 'admin', '文件管理菜单');

-- 文件上传按钮权限
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`, `icon`, `sort`, `visible`, `status`, `create_by`, `remark`)
VALUES 
(281, 2071872986719526914, '头像上传', 'upload:avatar', 3, NULL, NULL, NULL, 1, 1, 1, 'admin', '头像上传权限');

-- =============================================
-- 8. 为管理员角色分配新增权限
-- =============================================
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, id FROM `sys_permission` WHERE id BETWEEN 200 AND 281
ON DUPLICATE KEY UPDATE `role_id` = `role_id`;

-- =============================================
-- 说明：
-- 本脚本为以下控制器添加了权限码：
-- 1. UserSkillController - 技能管理（skill:list/upload/edit/delete）
-- 2. KnowledgeDocumentController - 知识库文档管理（knowledge:document:list/upload/edit/delete）
-- 3. KnowledgeBaseController - 知识库管理（knowledge:base:list/add/edit/delete）
-- 4. AiRoleController - AI角色管理（ai:role:list/add/edit/delete + ai:role:document:list/add/delete）
-- 5. AiProviderConfigController - AI供应商配置（ai:provider:list/add/edit/delete/test）
-- 6. AiModelConfigController - AI模型配置（ai:config:list/add/edit/delete/test）
-- 7. AiModelProviderController - AI模型供应商（ai:provider:list，共用供应商配置权限）
-- 8. ChatController - AI对话（chat:list/stream/delete + chat:agent:list/execute）
-- 9. FileUploadController - 文件上传（upload:avatar）
-- =============================================