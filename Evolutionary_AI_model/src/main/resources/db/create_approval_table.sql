-- =============================================
-- 审批表
-- =============================================
CREATE TABLE `sys_approval` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '审批ID',
    `approval_type` VARCHAR(50) NOT NULL COMMENT '审批类型：role_create-角色创建审批，role_user_auth-角色用户授权审批，dept_user_change-部门用户变动审批',
    `approval_title` VARCHAR(200) NOT NULL COMMENT '审批标题',
    `approval_content` TEXT DEFAULT NULL COMMENT '审批内容（JSON格式存储详细内容）',
    `applicant_id` BIGINT NOT NULL COMMENT '申请人ID',
    `applicant_name` VARCHAR(50) DEFAULT NULL COMMENT '申请人姓名',
    `approver_id` BIGINT DEFAULT NULL COMMENT '审批人ID',
    `approver_name` VARCHAR(50) DEFAULT NULL COMMENT '审批人姓名',
    `approval_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审批状态：0-待审批，1-已通过，2-已拒绝',
    `approval_time` DATETIME DEFAULT NULL COMMENT '审批时间',
    `approval_opinion` VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '修改人',
    `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `del_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_applicant_id` (`applicant_id`),
    KEY `idx_approver_id` (`approver_id`),
    KEY `idx_approval_type` (`approval_type`),
    KEY `idx_approval_status` (`approval_status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批表';

-- =============================================
-- 字典表
-- =============================================
CREATE TABLE `sys_dict` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '字典ID',
    `dict_type` VARCHAR(100) NOT NULL COMMENT '字典类型',
    `dict_name` VARCHAR(100) DEFAULT NULL COMMENT '字典名称（中文名称）',
    `dict_code` VARCHAR(100) NOT NULL COMMENT '字典编码',
    `dict_label` VARCHAR(200) NOT NULL COMMENT '字典标签',
    `dict_value` VARCHAR(200) DEFAULT NULL COMMENT '字典值',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '修改人',
    `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `del_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dict_type_code` (`dict_type`, `dict_code`),
    KEY `idx_dict_type` (`dict_type`),
    KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典表';

-- =============================================
-- 初始化审批类型字典数据
-- =============================================
INSERT INTO `sys_dict` (`dict_type`, `dict_name`, `dict_code`, `dict_label`, `dict_value`, `sort`, `status`, `create_by`, `remark`) VALUES
('approval_type', '审批类型', 'role_create', '角色创建审批', 'role_create', 1, 1, 'system', '角色创建审批'),
('approval_type', '审批类型', 'role_user_auth', '角色用户授权审批', 'role_user_auth', 2, 1, 'system', '角色用户授权审批'),
('approval_type', '审批类型', 'dept_user_change', '部门用户变动审批', 'dept_user_change', 3, 1, 'system', '部门用户变动审批');

-- =============================================
-- 初始化审批状态字典数据
-- =============================================
INSERT INTO `sys_dict` (`dict_type`, `dict_name`, `dict_code`, `dict_label`, `dict_value`, `sort`, `status`, `create_by`, `remark`) VALUES
('approval_status', '审批状态', 'pending', '待审批', '0', 1, 1, 'system', '待审批'),
('approval_status', '审批状态', 'approved', '已通过', '1', 2, 1, 'system', '已通过'),
('approval_status', '审批状态', 'rejected', '已拒绝', '2', 3, 1, 'system', '已拒绝');

INSERT INTO `sys_permission` (
    `parent_id`,
    `permission_name`,
    `permission_code`,
    `permission_type`,
    `path`,
    `component`,
    `icon`,
    `sort`,
    `visible`,
    `status`,
    `create_by`,
    `create_time`,
    `update_by`,
    `update_time`,
    `del_flag`,
    `remark`
) VALUES (
             2075480414988062722,              -- parent_id: 父级权限ID（审批管理菜单的ID）
             '审批列表查询',                    -- permission_name: 权限名称
             'sys:approval:list',              -- permission_code: 权限编码
             3,                                 -- permission_type: 权限类型（3-按钮）
             NULL,                              -- path: 路由地址（按钮权限为空）
             NULL,                              -- component: 组件路径（按钮权限为空）
             NULL,                              -- icon: 图标（按钮权限为空）
             1,                                 -- sort: 显示顺序
             1,                                 -- visible: 是否可见（1-显示）
             1,                                 -- status: 状态（1-启用）
             'system',                          -- create_by: 创建人
             NOW(),                             -- create_time: 创建时间
             NULL,                              -- update_by: 修改人
             NULL,                              -- update_time: 修改时间
             0,                                 -- del_flag: 删除标记（0-未删除）
             '审批列表查询权限，包含分页查询、按类型/状态/申请人筛选、详情查看、字典查询等操作' -- remark: 备注
         );