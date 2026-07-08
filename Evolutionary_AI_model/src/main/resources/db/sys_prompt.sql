-- 系统默认提示词表
-- 创建时间: 2026-07-06
-- 说明: 存放默认提示词，负责约束智能体规范

-- ==================== 系统默认提示词表 ====================
DROP TABLE IF EXISTS `sys_prompt`;
CREATE TABLE `sys_prompt` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `prompt_name` VARCHAR(100) NOT NULL COMMENT '提示词名称',
    `prompt_code` VARCHAR(50) DEFAULT NULL COMMENT '提示词唯一标识',
    `prompt_description` VARCHAR(500) DEFAULT NULL COMMENT '提示词描述',
    `prompt_type` VARCHAR(20) NOT NULL DEFAULT 'DOCUMENT' COMMENT '提示词类型：DOCUMENT-文档型 TEXT-文本型',
    `document_name` VARCHAR(255) DEFAULT NULL COMMENT '文档名称（仅文档型）',
    `document_path` VARCHAR(500) DEFAULT NULL COMMENT 'MinIO存储路径（仅文档型）',
    `document_type` VARCHAR(20) DEFAULT NULL COMMENT '文档类型（仅文档型，pdf/docx/txt）',
    `document_size` BIGINT DEFAULT NULL COMMENT '文档大小（字节，仅文档型）',
    `document_content` LONGTEXT DEFAULT NULL COMMENT '解析后的文本内容',
    `text_content` LONGTEXT DEFAULT NULL COMMENT '纯文本提示词内容（仅文本型）',
    `is_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用 1-启用',
    `is_default` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否默认提示词：0-否 1-是',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `upload_time` DATETIME DEFAULT NULL COMMENT '上传时间',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除 1-已删除',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_prompt_code` (`prompt_code`),
    KEY `idx_prompt_type` (`prompt_type`),
    KEY `idx_is_enabled` (`is_enabled`),
    KEY `idx_is_default` (`is_default`),
    KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统默认提示词表';