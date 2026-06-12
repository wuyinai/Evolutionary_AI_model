-- ========================================
-- 向量模型配置支持 - 数据库迁移脚本
-- 版本: V1.0.3
-- 日期: 2026-06-12
-- 说明: 为ai_model_config表添加模型类型和向量相关字段
-- ========================================

-- 1. 添加模型类型字段
ALTER TABLE `ai_model_config` 
ADD COLUMN `model_type` VARCHAR(20) DEFAULT 'CHAT' COMMENT '模型类型：CHAT-对话模型 EMBEDDING-向量模型' AFTER `model_alias`;

-- 2. 添加向量维度字段
ALTER TABLE `ai_model_config` 
ADD COLUMN `vector_dimensions` INT DEFAULT NULL COMMENT '向量维度（仅向量模型使用）' AFTER `model_type`;

-- 3. 添加相似度阈值字段
ALTER TABLE `ai_model_config` 
ADD COLUMN `similarity_threshold` DECIMAL(5,2) DEFAULT NULL COMMENT '相似度阈值（仅向量模型使用，0.00-1.00）' AFTER `vector_dimensions`;

-- 4. 为模型类型字段添加索引
ALTER TABLE `ai_model_config` 
ADD INDEX `idx_model_type` (`model_type`);

-- 5. 更新现有数据的模型类型为CHAT（如果字段为空）
UPDATE `ai_model_config` 
SET `model_type` = 'CHAT' 
WHERE `model_type` IS NULL OR `model_type` = '';

-- ========================================
-- 说明：
-- 1. model_type字段用于区分模型类型，默认值为CHAT（对话模型）
-- 2. vector_dimensions字段仅在model_type为EMBEDDING时使用，存储向量维度
-- 3. similarity_threshold字段仅在model_type为EMBEDDING时使用，存储相似度阈值
-- 4. 添加了model_type字段的索引，便于按模型类型查询
-- ========================================
