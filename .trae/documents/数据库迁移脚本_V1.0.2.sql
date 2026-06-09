-- ========================================
-- 数据库迁移脚本：添加逻辑删除字段
-- 版本：V1.0.2
-- 日期：2026-06-09
-- 说明：为ai_conversation_message表添加del_flag字段，支持逻辑删除功能
-- ========================================

-- 1. 为ai_conversation_message表添加del_flag字段
ALTER TABLE `ai_conversation_message` 
ADD COLUMN `del_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除 1-已删除' AFTER `create_time`;

-- 2. 添加索引（可选，提升查询性能）
ALTER TABLE `ai_conversation_message` 
ADD INDEX `idx_del_flag` (`del_flag`);

-- ========================================
-- 执行说明：
-- 1. 请在MySQL客户端中执行此脚本
-- 2. 执行前请备份数据库
-- 3. 执行后请验证字段是否添加成功
-- ========================================

-- 验证SQL（执行后运行此查询验证）
-- SHOW COLUMNS FROM ai_conversation_message LIKE 'del_flag';