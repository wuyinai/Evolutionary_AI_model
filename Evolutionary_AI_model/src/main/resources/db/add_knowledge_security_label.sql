CREATE TABLE `knowledge_security_label` (
    `id` BIGINT NOT NULL COMMENT '主键ID（雪花算法）',
    `label_name` VARCHAR(50) NOT NULL COMMENT '标签名称（如：普通、内部、机密、绝密）',
    `label_code` VARCHAR(50) NOT NULL COMMENT '标签编码（如：NORMAL, INTERNAL, SECRET, TOP_SECRET）',
    `label_level` INT NOT NULL COMMENT '密级等级（数值越大密级越高）',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '标签描述',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_label_code` (`label_code`),
    KEY `idx_label_level` (`label_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库密级标签表';
-- 初始化四级密级标签数据
INSERT INTO `knowledge_security_label` (`id`, `label_name`, `label_code`, `label_level`, `description`, `create_by`, `create_time`) VALUES
(1, '普通', 'NORMAL', 1, '普通级别，所有用户可访问', 'admin', NOW()),
(2, '内部', 'INTERNAL', 2, '内部级别，仅部门内部可访问', 'admin', NOW()),
(3, '机密', 'SECRET', 3, '机密级别，仅特定角色可访问', 'admin', NOW()),
(4, '绝密', 'TOP_SECRET', 4, '绝密级别，最高权限角色可访问', 'admin', NOW());
-- 为角色表添加密级标签字段
ALTER TABLE `sys_role`
ADD COLUMN `security_label_id` BIGINT DEFAULT NULL COMMENT '密级标签ID（用户角色可访问的最高密级）' AFTER `role_sort`,
ADD INDEX `idx_security_label_id` (`security_label_id`);
-- 为知识库表添加部门字段
ALTER TABLE `knowledge_base`
ADD COLUMN `dept_id` BIGINT DEFAULT NULL COMMENT '所属部门ID（部门级知识库）' AFTER `user_id`,
ADD INDEX `idx_dept_id` (`dept_id`);
-- 为知识库表添加密级标签字段
ALTER TABLE `knowledge_base`
ADD COLUMN `security_label_id` BIGINT DEFAULT NULL COMMENT '密级标签ID' AFTER `status`,
ADD INDEX `idx_security_label_id` (`security_label_id`);
-- 为文档表添加密级标签字段
ALTER TABLE `knowledge_document`
ADD COLUMN `security_label_id` BIGINT DEFAULT NULL COMMENT '密级标签ID' AFTER `knowledge_base_id`,
ADD INDEX `idx_security_label_id` (`security_label_id`);
-- 为文档块表添加密级标签字段
ALTER TABLE `document_chunk`
ADD COLUMN `security_label_id` BIGINT DEFAULT NULL COMMENT '密级标签ID' AFTER `knowledge_base_id`,
ADD INDEX `idx_security_label_id` (`security_label_id`);