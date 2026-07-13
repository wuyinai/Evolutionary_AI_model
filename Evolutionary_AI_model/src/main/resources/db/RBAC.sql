-- =============================================
-- 1. 用户表
-- =============================================
CREATE TABLE `sys_user` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                            `username` VARCHAR(50) NOT NULL COMMENT '用户名',
                            `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
                            `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
                            `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
                            `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
                            `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
                            `gender` TINYINT DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
                            `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
                            `dept_id` BIGINT DEFAULT NULL COMMENT '部门ID',
                            `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
                            `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
                            `password_reset_time` DATETIME DEFAULT NULL COMMENT '密码重置时间',
                            `account_expire_time` DATETIME DEFAULT NULL COMMENT '账号过期时间',
                            `credentials_expire_time` DATETIME DEFAULT NULL COMMENT '密码过期时间',
                            `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
                            `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_by` VARCHAR(50) DEFAULT NULL COMMENT '修改人',
                            `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                            `del_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
                            `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_username` (`username`),
                            KEY `idx_dept_id` (`dept_id`),
                            KEY `idx_status` (`status`),
                            KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- 用户部门关联迁移到 sys_user_dept 表（多对多），移除旧字段
ALTER TABLE `sys_user` DROP COLUMN `dept_id`,
DROP INDEX `idx_dept_id`;

-- =============================================
-- 2. 角色表
-- =============================================
CREATE TABLE `sys_role` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
                            `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
                            `role_code` VARCHAR(100) NOT NULL COMMENT '角色编码',
                            `role_sort` INT DEFAULT 0 COMMENT '显示顺序',
                            `data_scope` TINYINT DEFAULT 1 COMMENT '数据范围：1-全部数据，2-本部门数据，3-本部门及以下数据，4-仅本人数据，5-自定义',
                            `perm_control` TINYINT NOT NULL DEFAULT 1 COMMENT '权限控制开关：0-禁用，1-启用',
                            `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
                            `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
                            `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_by` VARCHAR(50) DEFAULT NULL COMMENT '修改人',
                            `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                            `del_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
                            `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_role_code` (`role_code`),
                            KEY `idx_role_sort` (`role_sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统角色表';

-- =============================================
-- 3. 权限表（菜单和按钮权限）
-- =============================================
CREATE TABLE `sys_permission` (
                                  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
                                  `parent_id` BIGINT DEFAULT 0 COMMENT '父级权限ID',
                                  `permission_name` VARCHAR(50) NOT NULL COMMENT '权限名称',
                                  `permission_code` VARCHAR(100) DEFAULT NULL COMMENT '权限编码（如：sys:user:add）',
                                  `permission_type` TINYINT NOT NULL COMMENT '权限类型：1-目录，2-菜单，3-按钮',
                                  `path` VARCHAR(200) DEFAULT NULL COMMENT '路由地址',
                                  `component` VARCHAR(255) DEFAULT NULL COMMENT '组件路径',
                                  `icon` VARCHAR(100) DEFAULT NULL COMMENT '图标',
                                  `sort` INT DEFAULT 0 COMMENT '显示顺序',
                                  `visible` TINYINT DEFAULT 1 COMMENT '是否可见：0-隐藏，1-显示',
                                  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
                                  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
                                  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '修改人',
                                  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                  `del_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
                                  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
                                  PRIMARY KEY (`id`),
                                  KEY `idx_parent_id` (`parent_id`),
                                  KEY `idx_permission_code` (`permission_code`),
                                  KEY `idx_permission_type` (`permission_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统权限表';

-- =============================================
-- 4. 用户角色关联表
-- =============================================
CREATE TABLE `sys_user_role` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `user_id` BIGINT NOT NULL COMMENT '用户ID',
                                 `role_id` BIGINT NOT NULL COMMENT '角色ID',
                                 `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
                                 KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- =============================================
-- 5. 角色权限关联表
-- =============================================
CREATE TABLE `sys_role_permission` (
                                       `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                       `role_id` BIGINT NOT NULL COMMENT '角色ID',
                                       `permission_id` BIGINT NOT NULL COMMENT '权限ID',
                                       `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       PRIMARY KEY (`id`),
                                       UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
                                       KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- =============================================
-- 6. 部门表
-- =============================================
CREATE TABLE `sys_dept` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '部门ID',
                            `parent_id` BIGINT DEFAULT 0 COMMENT '父部门ID',
                            `ancestors` VARCHAR(500) DEFAULT '' COMMENT '祖级列表',
                            `dept_name` VARCHAR(50) NOT NULL COMMENT '部门名称',
                            `dept_code` VARCHAR(100) DEFAULT NULL COMMENT '部门编码',
                            `sort` INT DEFAULT 0 COMMENT '显示顺序',
                            `leader` VARCHAR(50) DEFAULT NULL COMMENT '负责人',
                            `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
                            `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
                            `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
                            `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
                            `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_by` VARCHAR(50) DEFAULT NULL COMMENT '修改人',
                            `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                            `del_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
                            `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
                            PRIMARY KEY (`id`),
                            KEY `idx_parent_id` (`parent_id`),
                            KEY `idx_dept_code` (`dept_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统部门表';

-- =============================================
-- 7. 知识库与部门关联表
-- =============================================
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

-- =============================================
-- 8. 操作日志表
-- =============================================
CREATE TABLE `sys_operation_log` (
                                     `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
                                     `user_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
                                     `username` VARCHAR(50) DEFAULT NULL COMMENT '操作人用户名',
                                     `operation` VARCHAR(100) DEFAULT NULL COMMENT '操作描述',
                                     `method` VARCHAR(255) DEFAULT NULL COMMENT '请求方法',
                                     `request_method` VARCHAR(10) DEFAULT NULL COMMENT '请求方式（GET/POST/PUT/DELETE）',
                                     `request_url` VARCHAR(255) DEFAULT NULL COMMENT '请求URL',
                                     `request_params` TEXT DEFAULT NULL COMMENT '请求参数',
                                     `request_time` BIGINT DEFAULT NULL COMMENT '请求耗时（毫秒）',
                                     `ip` VARCHAR(50) DEFAULT NULL COMMENT '操作IP',
                                     `location` VARCHAR(100) DEFAULT NULL COMMENT '操作地点',
                                     `browser` VARCHAR(100) DEFAULT NULL COMMENT '浏览器',
                                     `os` VARCHAR(100) DEFAULT NULL COMMENT '操作系统',
                                     `status` TINYINT DEFAULT 1 COMMENT '操作状态：0-失败，1-成功',
                                     `error_msg` TEXT DEFAULT NULL COMMENT '错误信息',
                                     `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     PRIMARY KEY (`id`),
                                     KEY `idx_user_id` (`user_id`),
                                     KEY `idx_create_time` (`create_time`),
                                     KEY `idx_operation` (`operation`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- =============================================
-- 9. 用户部门关联表
-- =============================================
CREATE TABLE `sys_user_dept` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `user_id` BIGINT NOT NULL COMMENT '用户ID',
                                 `dept_id` BIGINT NOT NULL COMMENT '部门ID',
                                 `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
                                 `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_user_dept` (`user_id`, `dept_id`),
                                 KEY `idx_user_id` (`user_id`),
                                 KEY `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户部门关联表';

-- =============================================
-- 10. 登录日志表
-- =============================================
CREATE TABLE `sys_login_log` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
                                 `username` VARCHAR(50) NOT NULL COMMENT '登录用户名',
                                 `login_time` DATETIME NOT NULL COMMENT '登录时间',
                                 `ip` VARCHAR(50) DEFAULT NULL COMMENT '登录IP',
                                 `location` VARCHAR(100) DEFAULT NULL COMMENT '登录地点',
                                 `browser` VARCHAR(100) DEFAULT NULL COMMENT '浏览器',
                                 `os` VARCHAR(100) DEFAULT NULL COMMENT '操作系统',
                                 `status` TINYINT NOT NULL COMMENT '登录状态：0-失败，1-成功',
                                 `message` VARCHAR(255) DEFAULT NULL COMMENT '提示消息',
                                 `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 PRIMARY KEY (`id`),
                                 KEY `idx_username` (`username`),
                                 KEY `idx_login_time` (`login_time`),
                                 KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';