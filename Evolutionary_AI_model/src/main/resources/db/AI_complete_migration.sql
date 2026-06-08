-- ========================================
-- AI模型配置系统重构 - 完整迁移脚本
-- 执行顺序：先执行表结构修改，再执行数据初始化
-- ========================================

-- ========================================
-- 第一部分：表结构修改
-- ========================================

-- 1. 修改ai_model_provider表，添加协议字段
ALTER TABLE `ai_model_provider` 
ADD COLUMN `protocol_type` VARCHAR(20) NOT NULL DEFAULT 'OPENAI' COMMENT '协议类型：OPENAI、ANTHROPIC、OLLAMA、ERNIE、ZHIPU、MOONSHOT' AFTER `auth_type`,
ADD COLUMN `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注说明' AFTER `del_flag`;

-- 添加协议类型索引
ALTER TABLE `ai_model_provider`
ADD KEY `idx_protocol_type` (`protocol_type`);

-- 2. 创建ai_provider_config表（管理连接配置）
DROP TABLE IF EXISTS `ai_provider_config`;
CREATE TABLE `ai_provider_config` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `config_name` VARCHAR(100) NOT NULL COMMENT '配置名称（用户自定义）',
    `user_id` BIGINT NOT NULL COMMENT '用户ID（配置所属用户）',
    `provider_id` BIGINT NOT NULL COMMENT '供应商ID，关联ai_model_provider.id',
    `provider_code` VARCHAR(50) NOT NULL COMMENT '供应商编码（冗余字段，便于查询）',
    `protocol_type` VARCHAR(20) NOT NULL COMMENT '协议类型（冗余字段，便于查询）',
    `api_key` VARCHAR(500) NOT NULL COMMENT 'API密钥（AES加密存储）',
    `api_endpoint` VARCHAR(500) DEFAULT NULL COMMENT 'API端点地址（覆盖默认端点）',
    `extra_config` TEXT DEFAULT NULL COMMENT '扩展配置（JSON格式，如：deploymentName、secretKey、accessToken等）',
    `is_default` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否默认配置：0-否 1-是',
    `timeout_seconds` INT DEFAULT 60 COMMENT '请求超时时间（秒）',
    `max_retries` INT DEFAULT 3 COMMENT '最大重试次数',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除 1-已删除',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_provider_id` (`provider_id`),
    KEY `idx_provider_code` (`provider_code`),
    KEY `idx_protocol_type` (`protocol_type`),
    KEY `idx_status` (`status`),
    KEY `idx_is_default` (`is_default`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI供应商配置表（管理连接信息）';

-- 3. 修改ai_conversation表，添加模型钉选字段
ALTER TABLE `ai_conversation`
ADD COLUMN `pinned_config_id` BIGINT DEFAULT NULL COMMENT '钉选的模型配置ID，关联ai_model_config.id' AFTER `config_id`,
ADD KEY `idx_pinned_config_id` (`pinned_config_id`);

-- 4. 更新ai_chat_log表，添加协议类型字段
ALTER TABLE `ai_chat_log`
ADD COLUMN `protocol_type` VARCHAR(20) DEFAULT NULL COMMENT '协议类型' AFTER `provider_code`,
ADD KEY `idx_protocol_type` (`protocol_type`);

-- ========================================
-- 第二部分：供应商数据初始化
-- ========================================

-- 清空现有供应商数据（如果需要重新初始化）
-- TRUNCATE TABLE `ai_model_provider`;

-- 插入供应商数据（包含protocol_type字段）
INSERT INTO `ai_model_provider` (`id`, `provider_code`, `provider_name`, `provider_icon`, `description`, `default_endpoint`, `supports_streaming`, `supports_vision`, `supports_function_call`, `auth_type`, `protocol_type`, `config_template`, `status`, `sort_order`, `create_time`, `update_time`, `del_flag`, `remark`) VALUES
(1, 'OPENAI', 'OpenAI', NULL, 'OpenAI官方API，支持GPT系列模型，使用OpenAI兼容协议', 'https://api.openai.com/v1', 1, 1, 1, 'API_KEY', 'OPENAI', '{"models": ["gpt-4o", "gpt-4o-mini", "gpt-4-turbo", "gpt-3.5-turbo", "o1-preview", "o1-mini"], "features": ["chat", "vision", "function_call", "streaming"]}', 1, 1, NOW(), NOW(), 0, 'OpenAI官方API，最成熟的AI服务提供商'),
(2, 'DEEPSEEK', 'DeepSeek', NULL, 'DeepSeek深度求索，支持DeepSeek系列模型，使用OpenAI兼容协议', 'https://api.deepseek.com', 1, 0, 1, 'API_KEY', 'OPENAI', '{"models": ["deepseek-chat", "deepseek-coder", "deepseek-reasoner"], "features": ["chat", "function_call", "streaming"]}', 1, 2, NOW(), NOW(), 0, 'DeepSeek深度求索，性价比高，支持推理和编程'),
(3, 'QWEN', '通义千问', NULL, '阿里云通义千问，支持Qwen系列模型，使用OpenAI兼容协议', 'https://dashscope.aliyuncs.com/compatible-mode/v1', 1, 1, 1, 'API_KEY', 'OPENAI', '{"models": ["qwen-turbo", "qwen-plus", "qwen-max", "qwen-max-longcontext", "qwen-long"], "features": ["chat", "vision", "function_call", "streaming"]}', 1, 3, NOW(), NOW(), 0, '阿里云通义千问，支持长文本和多模态'),
(4, 'ERNIE', '文心一言', NULL, '百度文心一言，支持ERNIE系列模型，使用百度专用协议', 'https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat', 1, 0, 1, 'API_KEY', 'ERNIE', '{"models": ["ernie-4.0-8k", "ernie-4.0-turbo-8k", "ernie-3.5-8k", "ernie-3.5-turbo-8k", "ernie-speed-8k", "ernie-speed-128k"], "features": ["chat", "function_call", "streaming"], "auth_config": {"need_access_token": true}}', 1, 4, NOW(), NOW(), 0, '百度文心一言，需要特殊的access token认证方式'),
(5, 'CLAUDE', 'Anthropic Claude', NULL, 'Anthropic Claude系列模型，使用Anthropic专用协议', 'https://api.anthropic.com/v1', 1, 1, 1, 'API_KEY', 'ANTHROPIC', '{"models": ["claude-3-5-sonnet-20241022", "claude-3-opus-20240229", "claude-3-sonnet-20240229", "claude-3-haiku-20240307"], "features": ["chat", "vision", "function_call", "streaming"]}', 1, 5, NOW(), NOW(), 0, 'Anthropic Claude，擅长长文本和复杂推理'),
(6, 'OLLAMA', 'Ollama本地部署', NULL, 'Ollama本地模型部署服务，使用Ollama协议（兼容OpenAI格式）', 'http://localhost:11434', 1, 0, 0, 'CUSTOM', 'OLLAMA', '{"models": ["llama3.1", "llama3.2", "mistral", "codellama", "qwen2.5", "deepseek-coder"], "features": ["chat", "streaming"], "note": "本地部署，无需API密钥"}', 1, 6, NOW(), NOW(), 0, 'Ollama本地部署，完全免费，支持多种开源模型'),
(7, 'MOONSHOT', 'Moonshot Kimi', NULL, 'Moonshot Kimi长文本对话模型，使用Moonshot协议（兼容OpenAI格式）', 'https://api.moonshot.cn/v1', 1, 0, 0, 'API_KEY', 'MOONSHOT', '{"models": ["moonshot-v1-8k", "moonshot-v1-32k", "moonshot-v1-128k"], "features": ["chat", "streaming"], "advantage": "超长文本支持"}', 1, 7, NOW(), NOW(), 0, 'Moonshot Kimi，擅长超长文本处理'),
(8, 'ZHIPU', '智谱清言', NULL, '智谱AI GLM系列模型，使用智谱专用协议', 'https://open.bigmodel.cn/api/paas/v4', 1, 1, 1, 'API_KEY', 'ZHIPU', '{"models": ["glm-4", "glm-4-flash", "glm-4-plus", "glm-3-turbo"], "features": ["chat", "vision", "function_call", "streaming"]}', 1, 8, NOW(), NOW(), 0, '智谱清言，国产大模型，性价比高'),
(9, 'AZURE_OPENAI', 'Azure OpenAI', NULL, '微软Azure OpenAI服务，使用OpenAI兼容协议', 'https://YOUR_RESOURCE.openai.azure.com', 1, 1, 1, 'API_KEY', 'OPENAI', '{"models": ["gpt-4o", "gpt-4-turbo", "gpt-35-turbo"], "features": ["chat", "vision", "function_call", "streaming"], "config_required": ["deployment_name", "api_version"]}', 1, 9, NOW(), NOW(), 0, 'Azure OpenAI，企业级服务，需要deployment配置'),
(10, 'SILICONFLOW', 'SiliconFlow', NULL, 'SiliconFlow算力平台，使用OpenAI兼容协议', 'https://api.siliconflow.cn/v1', 1, 1, 1, 'API_KEY', 'OPENAI', '{"models": ["Qwen/Qwen2.5-7B-Instruct", "Qwen/Qwen2.5-72B-Instruct", "deepseek-ai/DeepSeek-V2.5", "THUDM/glm-4-9b-chat"], "features": ["chat", "streaming"]}', 1, 10, NOW(), NOW(), 0, 'SiliconFlow算力平台，提供多种开源模型API服务')
ON DUPLICATE KEY UPDATE
    `protocol_type` = VALUES(`protocol_type`),
    `remark` = VALUES(`remark`),
    `config_template` = VALUES(`config_template`),
    `update_time` = NOW();

-- ========================================
-- 第三部分：数据迁移（可选）
-- ========================================

-- 注意：以下迁移脚本会修改现有数据，请谨慎执行
-- 建议先备份数据再执行

-- 迁移现有ai_model_config数据到ai_provider_config（可选）
-- 如果您想完全使用新架构，可以执行以下脚本：

-- INSERT INTO `ai_provider_config` (
--     `id`, `config_name`, `user_id`, `provider_id`, `provider_code`, `protocol_type`,
--     `api_key`, `api_endpoint`, `extra_config`, `is_default`, `timeout_seconds`, `max_retries`,
--     `status`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `remark`
-- )
-- SELECT 
--     mc.`id`,
--     CONCAT(mc.`config_name`, '_连接配置') AS `config_name`,
--     mc.`user_id`,
--     mc.`provider_id`,
--     mc.`provider_code`,
--     COALESCE(p.`protocol_type`, 'OPENAI') AS `protocol_type`,
--     mc.`api_key`,
--     mc.`api_endpoint`,
--     mc.`extra_config`,
--     mc.`is_default`,
--     mc.`timeout_seconds`,
--     mc.`max_retries`,
--     mc.`status`,
--     mc.`create_by`,
--     mc.`create_time`,
--     mc.`update_by`,
--     mc.`update_time`,
--     mc.`del_flag`,
--     mc.`remark`
-- FROM `ai_model_config` mc
-- LEFT JOIN `ai_model_provider` p ON mc.`provider_id` = p.`id`
-- WHERE mc.`del_flag` = 0;

-- 更新ai_model_config表，设置provider_config_id关联（可选）
-- UPDATE `ai_model_config` mc
-- SET mc.`provider_config_id` = mc.`id`
-- WHERE mc.`del_flag` = 0;

-- ========================================
-- 第四部分：验证和清理
-- ========================================

-- 验证供应商数据
SELECT 
    `id`, `provider_code`, `provider_name`, `protocol_type`, `default_endpoint`, `status`
FROM `ai_model_provider`
ORDER BY `sort_order`;

-- 验证表结构
SHOW CREATE TABLE `ai_provider_config`;
SHOW CREATE TABLE `ai_model_provider`;
SHOW CREATE TABLE `ai_conversation`;

-- 清理说明：
-- 如果确认迁移成功，可以删除ai_model_config表中的连接相关字段：
-- ALTER TABLE `ai_model_config` 
-- DROP COLUMN `provider_id`,
-- DROP COLUMN `provider_code`,
-- DROP COLUMN `api_key`,
-- DROP COLUMN `api_endpoint`,
-- DROP COLUMN `extra_config`,
-- DROP COLUMN `timeout_seconds`,
-- DROP COLUMN `max_retries`;

-- ========================================
-- 执行说明
-- ========================================

-- 执行顺序：
-- 1. 备份现有数据（重要！）
-- 2. 执行第一部分：表结构修改
-- 3. 执行第二部分：供应商数据初始化
-- 4. 验证数据是否正确
-- 5. 根据需要执行第三部分：数据迁移（可选）
-- 6. 测试功能是否正常
-- 7. 根据需要执行第四部分：清理旧字段（可选）

-- 注意事项：
-- 1. 执行前请备份数据库
-- 2. 建议在测试环境先执行验证
-- 3. 数据迁移部分是可选的，新旧架构可以并存
-- 4. 清理旧字段前请确保所有功能正常