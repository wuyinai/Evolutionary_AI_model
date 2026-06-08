-- AI模型配置重构SQL - 两级配置架构
-- Provider管理连接信息（密钥、URL、协议），ModelConfig管理推理参数（温度、token上限）

-- ========================================
-- 1. 修改ai_model_provider表，添加协议字段
-- ========================================
ALTER TABLE `ai_model_provider` 
ADD COLUMN `protocol_type` VARCHAR(20) NOT NULL DEFAULT 'OPENAI' COMMENT '协议类型：OPENAI、ANTHROPIC、OLLAMA、ERNIE、ZHIPU、MOONSHOT' AFTER `auth_type`;

-- 更新现有供应商的协议类型
UPDATE `ai_model_provider` SET `protocol_type` = 'OPENAI' WHERE `provider_code` IN ('OPENAI', 'DEEPSEEK', 'QWEN', 'AZURE_OPENAI');
UPDATE `ai_model_provider` SET `protocol_type` = 'ANTHROPIC' WHERE `provider_code` = 'CLAUDE';
UPDATE `ai_model_provider` SET `protocol_type` = 'OLLAMA' WHERE `provider_code` = 'OLLAMA';
UPDATE `ai_model_provider` SET `protocol_type` = 'ERNIE' WHERE `provider_code` = 'ERNIE';
UPDATE `ai_model_provider` SET `protocol_type` = 'ZHIPU' WHERE `provider_code` = 'ZHIPU';
UPDATE `ai_model_provider` SET `protocol_type` = 'MOONSHOT' WHERE `provider_code` = 'MOONSHOT';

-- ========================================
-- 2. 创建ai_provider_config表（管理连接配置）
-- ========================================
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
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
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

-- ========================================
-- 3. 重构ai_model_config表（只管理推理参数）
-- ========================================
-- 先备份原有数据到新表结构
-- 步骤1：将原ai_model_config中的连接配置数据迁移到ai_provider_config
INSERT INTO `ai_provider_config` (
    `id`, `config_name`, `user_id`, `provider_id`, `provider_code`, `protocol_type`,
    `api_key`, `api_endpoint`, `extra_config`, `is_default`, `timeout_seconds`, `max_retries`,
    `status`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `remark`
)
SELECT 
    `id`, 
    CONCAT(`config_name`, '_连接配置') AS `config_name`,
    `user_id`, 
    `provider_id`, 
    `provider_code`,
    p.`protocol_type`,
    `api_key`, 
    `api_endpoint`, 
    `extra_config`, 
    `is_default`, 
    `timeout_seconds`, 
    `max_retries`,
    `status`, 
    `create_by`, 
    `create_time`, 
    `update_by`, 
    `update_time`, 
    `del_flag`, 
    `remark`
FROM `ai_model_config` mc
LEFT JOIN `ai_model_provider` p ON mc.`provider_id` = p.`id`;

-- 步骤2：修改ai_model_config表结构，移除连接相关字段，只保留推理参数
-- 创建临时表保存推理参数数据
DROP TABLE IF EXISTS `ai_model_config_temp`;
CREATE TABLE `ai_model_config_temp` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `config_name` VARCHAR(100) NOT NULL COMMENT '配置名称（用户自定义）',
    `user_id` BIGINT NOT NULL COMMENT '用户ID（配置所属用户）',
    `provider_config_id` BIGINT NOT NULL COMMENT '供应商配置ID，关联ai_provider_config.id',
    `model_name` VARCHAR(100) NOT NULL COMMENT '模型名称（如：gpt-4o、qwen-turbo等）',
    `model_alias` VARCHAR(100) DEFAULT NULL COMMENT '模型别名（用户自定义显示名称）',
    `temperature` DECIMAL(3,2) DEFAULT 0.70 COMMENT '温度参数（0.00-2.00），控制输出随机性',
    `max_tokens` INT DEFAULT 4096 COMMENT '最大输出Token数',
    `top_p` DECIMAL(3,2) DEFAULT 1.00 COMMENT 'Top-P采样参数',
    `frequency_penalty` DECIMAL(3,2) DEFAULT 0.00 COMMENT '频率惩罚参数',
    `presence_penalty` DECIMAL(3,2) DEFAULT 0.00 COMMENT '存在惩罚参数',
    `is_streaming_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用流式输出：0-否 1-是',
    `is_default` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否默认模型：0-否 1-是',
    `daily_quota` INT DEFAULT NULL COMMENT '每日调用限额（次数），NULL表示无限制',
    `monthly_quota` INT DEFAULT NULL COMMENT '每月调用限额（次数），NULL表示无限制',
    `token_quota` BIGINT DEFAULT NULL COMMENT 'Token总量限额，NULL表示无限制',
    `used_count` BIGINT NOT NULL DEFAULT 0 COMMENT '累计调用次数',
    `used_tokens` BIGINT NOT NULL DEFAULT 0 COMMENT '累计使用Token数',
    `last_used_time` DATETIME DEFAULT NULL COMMENT '最后使用时间',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除 1-已删除',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_provider_config_id` (`provider_config_id`),
    KEY `idx_model_name` (`model_name`),
    KEY `idx_status` (`status`),
    KEY `idx_is_default` (`is_default`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI模型配置表（管理推理参数）';


-- 删除原表并重命名临时表
DROP TABLE `ai_model_config`;
RENAME TABLE `ai_model_config_temp` TO `ai_model_config`;

-- ========================================
-- 4. 修改ai_conversation表，添加模型钉选字段
-- ========================================
ALTER TABLE `ai_conversation`
ADD COLUMN `pinned_config_id` BIGINT DEFAULT NULL COMMENT '钉选的模型配置ID，关联ai_model_config.id' AFTER `config_id`,
ADD KEY `idx_pinned_config_id` (`pinned_config_id`);

-- ========================================
-- 5. 更新ai_chat_log表，添加协议类型字段
-- ========================================
ALTER TABLE `ai_chat_log`
ADD COLUMN `protocol_type` VARCHAR(20) DEFAULT NULL COMMENT '协议类型' AFTER `provider_code`,
ADD KEY `idx_protocol_type` (`protocol_type`);