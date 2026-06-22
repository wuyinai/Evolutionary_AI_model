-- 知识库表
CREATE TABLE IF NOT EXISTS `knowledge_base` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '知识库名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '知识库描述',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `embedding_model_id` BIGINT DEFAULT NULL COMMENT '默认向量模型配置ID',
    `document_count` INT DEFAULT 0 COMMENT '文档数量',
    `chunk_count` INT DEFAULT 0 COMMENT '总分块数量',
    `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '知识库状态：ACTIVE-活跃 INACTIVE-停用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标记（0-未删除 1-已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_embedding_model_id` (`embedding_model_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库表';

-- 为knowledge_document表添加知识库ID字段
ALTER TABLE `knowledge_document` 
ADD COLUMN `knowledge_base_id` BIGINT DEFAULT NULL COMMENT '知识库ID' AFTER `user_id`,
ADD INDEX `idx_knowledge_base_id` (`knowledge_base_id`);

-- 为document_chunk表添加知识库ID字段（便于按知识库查询）
ALTER TABLE `document_chunk`
ADD COLUMN `knowledge_base_id` BIGINT DEFAULT NULL COMMENT '知识库ID' AFTER `document_id`,
ADD INDEX `idx_knowledge_base_id` (`knowledge_base_id`);