-- AI会话消息表添加模型配置ID字段
-- 版本：V1.0.4
-- 日期：2026-06-25
-- 说明：为ai_conversation_message表添加config_id字段，用于记录每条消息使用的模型配置

-- 添加config_id字段
ALTER TABLE `ai_conversation_message`
ADD COLUMN `config_id` BIGINT DEFAULT NULL COMMENT '模型配置ID，关联ai_model_config.id（记录该消息使用的模型）' AFTER `parent_message_id`;

-- 添加索引以优化查询性能
ALTER TABLE `ai_conversation_message`
ADD INDEX `idx_config_id` (`config_id`) COMMENT '模型配置ID索引';

-- 添加外键约束（可选，根据实际需求决定是否启用）
-- ALTER TABLE `ai_conversation_message`
-- ADD CONSTRAINT `fk_config_id` FOREIGN KEY (`config_id`) REFERENCES `ai_model_config` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;