-- =============================================
-- 知识库与部门关联表
-- =============================================
-- 作用：将知识库与部门进行多对多关联，一个知识库可被多个部门访问
-- 一个部门可拥有多个知识库

CREATE TABLE IF NOT EXISTS `knowledge_base_dept` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `knowledge_base_id` BIGINT NOT NULL COMMENT '知识库ID',
    `dept_id` BIGINT NOT NULL COMMENT '部门ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_kb_dept` (`knowledge_base_id`, `dept_id`),
    KEY `idx_knowledge_base_id` (`knowledge_base_id`),
    KEY `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库与部门关联表';
