-- AI角色管理系统数据库迁移脚本
-- 版本：V1.0.5
-- 日期：2026-06-25
-- 说明：创建AI角色管理系统相关数据表，支持角色创建、文档上传和系统提示词自定义

-- 创建AI角色表
CREATE TABLE IF NOT EXISTS `ai_role` (
    `id` BIGINT NOT NULL COMMENT '主键ID（雪花算法）',
    `role_name` VARCHAR(100) NOT NULL COMMENT '角色名称',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色唯一标识',
    `description` TEXT COMMENT '角色描述',
    `system_prompt` TEXT COMMENT '纯文本系统提示词（可选）',
    `system_prompt_template` TEXT COMMENT '系统提示词模板（支持变量替换）',
    `user_id` BIGINT NOT NULL COMMENT '创建者用户ID',
    `is_public` TINYINT DEFAULT 0 COMMENT '是否公开：0-私有 1-公开',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `create_by` VARCHAR(100) COMMENT '创建人',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(100) COMMENT '更新人',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标记(0-未删除 1-已删除)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI角色表';

-- 创建角色文档关联表
CREATE TABLE IF NOT EXISTS `ai_role_document` (
    `id` BIGINT NOT NULL COMMENT '主键ID（雪花算法）',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `document_name` VARCHAR(255) NOT NULL COMMENT '文档名称',
    `document_path` VARCHAR(500) NOT NULL COMMENT 'MinIO存储路径',
    `document_type` VARCHAR(20) NOT NULL COMMENT '文档类型（pdf/docx/txt）',
    `document_size` BIGINT COMMENT '文档大小（字节）',
    `document_content` TEXT COMMENT '解析后的文本内容',
    `upload_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标记(0-未删除 1-已删除)',
    PRIMARY KEY (`id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_document_type` (`document_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色文档关联表';