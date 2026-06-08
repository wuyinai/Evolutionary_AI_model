-- AI模型配置表 - 移除旧架构字段
-- 执行时间：2026-06-08
-- 说明：移除ai_model_config表中的旧架构兼容字段，只保留新架构字段

-- 1. 删除旧架构兼容字段
ALTER TABLE `ai_model_config` DROP COLUMN IF EXISTS `provider_id`;
ALTER TABLE `ai_model_config` DROP COLUMN IF EXISTS `provider_code`;
ALTER TABLE `ai_model_config` DROP COLUMN IF EXISTS `api_key`;
ALTER TABLE `ai_model_config` DROP COLUMN IF EXISTS `api_endpoint`;
ALTER TABLE `ai_model_config` DROP COLUMN IF EXISTS `extra_config`;
ALTER TABLE `ai_model_config` DROP COLUMN IF EXISTS `timeout_seconds`;
ALTER TABLE `ai_model_config` DROP COLUMN IF EXISTS `max_retries`;

-- 2. 确保provider_config_id字段存在（如果不存在则添加）
-- 注意：这个字段应该已经存在，如果不存在才执行下面的语句
-- ALTER TABLE `ai_model_config` ADD COLUMN `provider_config_id` BIGINT(20) NULL COMMENT '供应商配置ID，关联ai_provider_config.id' AFTER `user_id`;

-- 3. 为provider_config_id添加索引（如果不存在）
CREATE INDEX IF NOT EXISTS `idx_provider_config_id` ON `ai_model_config` (`provider_config_id`);

-- 4. 添加外键约束（可选，根据实际需求）
-- ALTER TABLE `ai_model_config` ADD CONSTRAINT `fk_model_provider_config` FOREIGN KEY (`provider_config_id`) REFERENCES `ai_provider_config` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

-- 5. 更新表注释
ALTER TABLE `ai_model_config` COMMENT 'AI模型配置表（新架构）- 只包含推理参数，连接信息由关联的供应商配置管理';

-- 6. 验证迁移结果
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

-- 说明：
-- 1. 此脚本移除了所有旧架构兼容字段，确保数据库表结构与代码实体类一致
-- 2. provider_config_id字段必须存在，用于关联供应商配置
-- 3. 执行此脚本前，请确保：
--    - 所有旧架构数据已迁移到新架构（或确认可以删除）
--    - ai_provider_config表已创建并有数据
--    - 已备份重要数据
-- 4. 执行此脚本后，旧架构功能将完全失效，只能使用新架构