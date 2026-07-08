-- 系统角色-知识库文档关联表（用于控制角色对知识库文档的访问权限）
CREATE TABLE `sys_role_knowledge_document` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `role_id` BIGINT NOT NULL COMMENT '系统角色ID，关联sys_role.id',
  `knowledge_document_id` BIGINT NOT NULL COMMENT '知识库文档ID，关联knowledge_document.id',
  `permission_type` VARCHAR(20) DEFAULT 'READ' COMMENT '权限类型：READ-只读、EDIT-编辑、DELETE-删除',
  `create_by` VARCHAR(64) COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) COMMENT '更新人',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` INT DEFAULT 0 COMMENT '删除标志：0-未删除 1-已删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_document` (`role_id`, `knowledge_document_id`, `del_flag`) COMMENT '角色-文档唯一索引',
  KEY `idx_role_id` (`role_id`) COMMENT '角色ID索引',
  KEY `idx_document_id` (`knowledge_document_id`) COMMENT '文档ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色-知识库文档权限关联表';

-- 添加索引说明
-- uk_role_document：确保一个角色对一个文档只有一条权限记录（考虑逻辑删除）
-- idx_role_id：快速查询某个角色拥有哪些文档权限
-- idx_document_id：快速查询某个文档被哪些角色访问
