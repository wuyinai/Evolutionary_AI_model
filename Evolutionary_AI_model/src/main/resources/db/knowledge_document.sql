-- 知识库文档表
CREATE TABLE IF NOT EXISTS `knowledge_document` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `document_name` VARCHAR(255) NOT NULL COMMENT '文档名称',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `file_type` VARCHAR(20) NOT NULL COMMENT '文件类型（pdf/docx/txt）',
    `file_size` BIGINT NOT NULL COMMENT '文件大小（字节）',
    `storage_path` VARCHAR(500) NOT NULL COMMENT 'MinIO存储路径',
    `embedding_model_id` BIGINT DEFAULT NULL COMMENT '向量模型配置ID',
    `security_label_id` BIGINT DEFAULT NULL COMMENT '密级标签ID',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '文档状态：PENDING-待处理 PROCESSING-处理中 COMPLETED-已完成 FAILED-失败',
    `chunk_count` INT DEFAULT 0 COMMENT '分块数量',
    `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标记（0-未删除 1-已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_embedding_model_id` (`embedding_model_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档表';

-- 文档分块表
CREATE TABLE IF NOT EXISTS `document_chunk` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `document_id` BIGINT NOT NULL COMMENT '文档ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `chunk_index` INT NOT NULL COMMENT '分块序号',
    `content` TEXT NOT NULL COMMENT '分块内容',
    `vector_id` VARCHAR(100) DEFAULT NULL COMMENT '向量ID（在向量数据库中的ID）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标记（0-未删除 1-已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_document_id` (`document_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_vector_id` (`vector_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档分块表';
