-- 用户技能表
CREATE TABLE IF NOT EXISTS user_skills (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID（雪花算法）',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(255) NOT NULL COMMENT 'skill唯一标识',
    display_name VARCHAR(255) COMMENT '友好显示名',
    description TEXT NOT NULL COMMENT '技能描述',
    version VARCHAR(50) COMMENT '版本号',
    author VARCHAR(255) COMMENT '作者',
    path VARCHAR(500) NOT NULL COMMENT 'MinIO存储路径',
    enabled BOOLEAN DEFAULT true COMMENT '是否启用',
    metadata JSON COMMENT '元数据信息',
    create_by VARCHAR(100) COMMENT '创建人',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(100) COMMENT '更新人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标记(0-未删除 1-已删除)',
    UNIQUE KEY uk_user_name (user_id, name) COMMENT '同一用户skill名唯一',
    KEY idx_user_id (user_id) COMMENT '用户ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户技能表';