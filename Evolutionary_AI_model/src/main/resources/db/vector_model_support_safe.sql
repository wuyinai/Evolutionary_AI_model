-- ========================================
-- 向量模型配置支持 - 数据库迁移脚本（安全版本）
-- 版本: V1.0.3
-- 日期: 2026-06-12
-- 说明: 为ai_model_config表添加模型类型和向量相关字段
-- 注意: 此脚本可以安全重复执行
-- ========================================

-- 1. 检查并添加模型类型字段（如果不存在）
SET @dbname = DATABASE();
SET @tablename = 'ai_model_config';
SET @columnname = 'model_type';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname
    AND TABLE_NAME = @tablename
    AND COLUMN_NAME = @columnname
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE `', @tablename, '` ADD COLUMN `', @columnname, '` VARCHAR(20) DEFAULT ''CHAT'' COMMENT ''模型类型：CHAT-对话模型 EMBEDDING-向量模型'' AFTER `model_alias`')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 2. 检查并添加向量维度字段（如果不存在）
SET @columnname = 'vector_dimensions';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname
    AND TABLE_NAME = @tablename
    AND COLUMN_NAME = @columnname
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE `', @tablename, '` ADD COLUMN `', @columnname, '` INT DEFAULT NULL COMMENT ''向量维度（仅向量模型使用）'' AFTER `model_type`')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 3. 检查并添加相似度阈值字段（如果不存在）
SET @columnname = 'similarity_threshold';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname
    AND TABLE_NAME = @tablename
    AND COLUMN_NAME = @columnname
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE `', @tablename, '` ADD COLUMN `', @columnname, '` DECIMAL(5,2) DEFAULT NULL COMMENT ''相似度阈值（仅向量模型使用，0.00-1.00）'' AFTER `vector_dimensions`')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 4. 检查并添加模型类型索引（如果不存在）
SET @indexname = 'idx_model_type';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = @dbname
    AND TABLE_NAME = @tablename
    AND INDEX_NAME = @indexname
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE `', @tablename, '` ADD INDEX `', @indexname, '` (`model_type`)')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 5. 更新现有数据的模型类型为CHAT（如果字段为空）
UPDATE `ai_model_config` 
SET `model_type` = 'CHAT' 
WHERE `model_type` IS NULL OR `model_type` = '';

-- ========================================
-- 说明：
-- 1. 此脚本可以安全重复执行，不会重复添加字段
-- 2. model_type字段用于区分模型类型，默认值为CHAT（对话模型）
-- 3. vector_dimensions字段仅在model_type为EMBEDDING时使用，存储向量维度
-- 4. similarity_threshold字段仅在model_type为EMBEDDING时使用，存储相似度阈值
-- 5. 添加了model_type字段的索引，便于按模型类型查询
-- ========================================
