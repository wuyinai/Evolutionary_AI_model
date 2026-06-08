-- ========================================
-- AI模型配置表 - 最终迁移脚本
-- 执行时间：2026-06-08
-- 说明：将ai_model_config表从旧架构迁移到新架构
-- ========================================

-- 重要提示：
-- 1. 执行此脚本前，请确保ai_provider_config表已创建
-- 2. 建议先备份数据，再执行此脚本
-- 3. MySQL版本兼容：不支持IF EXISTS语法，请根据实际情况选择执行

-- ========================================
-- 第一步：检查当前表结构
-- ========================================

-- 查看当前表结构，确认需要添加/删除的字段
SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_COMMENT
FROM 
    INFORMATION_SCHEMA.COLUMNS 
WHERE 
    TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ai_model_config'
ORDER BY 
    ORDINAL_POSITION;

-- ========================================
-- 第二步：添加新架构字段（如果不存在）
-- ========================================

-- 添加provider_config_id字段
-- 注意：如果字段已存在，会报错，请跳过此语句
ALTER TABLE `ai_model_config` 
ADD COLUMN `provider_config_id` BIGINT(20) DEFAULT NULL COMMENT '供应商配置ID，关联ai_provider_config.id' AFTER `user_id`;

-- 为provider_config_id添加索引
-- 注意：如果索引已存在，会报错，请跳过此语句
ALTER TABLE `ai_model_config`
ADD INDEX `idx_provider_config_id` (`provider_config_id`);

-- ========================================
-- 第三步：删除旧架构兼容字段（如果存在）
-- ========================================

-- 删除旧架构字段
-- 注意：如果字段不存在，会报错，请跳过对应的语句

-- 删除provider_id字段
ALTER TABLE `ai_model_config` DROP COLUMN `provider_id`;

-- 删除provider_code字段
ALTER TABLE `ai_model_config` DROP COLUMN `provider_code`;

-- 删除api_key字段
ALTER TABLE `ai_model_config` DROP COLUMN `api_key`;

-- 删除api_endpoint字段
ALTER TABLE `ai_model_config` DROP COLUMN `api_endpoint`;

-- 删除extra_config字段
ALTER TABLE `ai_model_config` DROP COLUMN `extra_config`;

-- 删除timeout_seconds字段
ALTER TABLE `ai_model_config` DROP COLUMN `timeout_seconds`;

-- 删除max_retries字段
ALTER TABLE `ai_model_config` DROP COLUMN `max_retries`;

-- ========================================
-- 第四步：更新表注释
-- ========================================

ALTER TABLE `ai_model_config` COMMENT 'AI模型配置表（新架构）- 只包含推理参数，连接信息由关联的供应商配置管理';

-- ========================================
-- 第五步：验证迁移结果
-- ========================================

-- 查看迁移后的表结构
SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_COMMENT
FROM 
    INFORMATION_SCHEMA.COLUMNS 
WHERE 
    TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ai_model_config'
ORDER BY 
    ORDINAL_POSITION;

-- ========================================
-- 执行说明
-- ========================================

-- 1. 先执行第一步，查看当前表结构
-- 2. 根据第一步的结果，判断需要执行哪些语句：
--    - 如果provider_config_id字段不存在，执行第二步的添加语句
--    - 如果provider_config_id索引不存在，执行第二步的添加索引语句
--    - 如果旧架构字段存在，执行第三步对应的删除语句
-- 3. 如果执行某条语句时报错"字段/索引已存在"或"字段不存在"，说明该字段/索引已经处理过，跳过该语句继续执行其他语句
-- 4. 执行完成后，查看第五步的结果，确认表结构正确

-- ========================================
-- 新架构字段说明
-- ========================================

-- 1. provider_config_id：关联供应商配置，管理连接信息
-- 2. 其他推理参数字段：temperature、max_tokens、top_p等
-- 3. 已删除的旧架构字段：
--    - provider_id、provider_code：供应商关联（改用provider_config_id）
--    - api_key、api_endpoint：连接信息（改由供应商配置管理）
--    - extra_config、timeout_seconds、max_retries：扩展配置（改由供应商配置管理）
-- 4. 执行此脚本后：
--    - 所有模型配置必须关联供应商配置（provider_config_id）
--    - 连接信息由供应商配置管理，模型配置只管理推理参数
--    - 旧架构功能完全失效，只能使用新架构