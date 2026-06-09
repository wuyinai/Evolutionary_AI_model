-- ========================================
-- 数据库迁移脚本：添加缺失字段
-- 版本：V1.0.1
-- 日期：2026-06-09
-- 说明：为ai_conversation表添加pinned_config_id字段，支持会话级模型钉选功能
-- ========================================

-- 1. 为ai_conversation表添加pinned_config_id字段
ALTER TABLE `ai_conversation` 
ADD COLUMN `pinned_config_id` BIGINT(20) NULL COMMENT '钉选的模型配置ID，关联ai_model_config.id（用户在聊天界面选择的模型）' AFTER `config_id`;

-- 2. 添加索引（可选，提升查询性能）
ALTER TABLE `ai_conversation` 
ADD INDEX `idx_pinned_config_id` (`pinned_config_id`);

-- ========================================
-- 执行说明：
-- 1. 请在MySQL客户端中执行此脚本
-- 2. 执行前请备份数据库
-- 3. 执行后请验证字段是否添加成功
-- ========================================

-- 验证SQL（执行后运行此查询验证）
-- SELECT * FROM ai_conversation LIMIT 1;
-- SHOW COLUMNS FROM ai_conversation LIKE 'pinned_config_id';