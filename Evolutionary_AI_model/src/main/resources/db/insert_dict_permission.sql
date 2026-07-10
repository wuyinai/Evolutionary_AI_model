-- =============================================
-- 字典管理权限数据插入SQL
-- =============================================

-- 字典列表查询权限
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
    2075485573151580162,              -- parent_id: 父级权限ID（字典管理菜单的ID）
    '字典列表查询',                    -- permission_name: 权限名称
    'sys:dict:list',                  -- permission_code: 权限编码
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
    '字典列表查询权限，包含分页查询、按类型/名称筛选、详情查看、字典项查询等操作' -- remark: 备注
);

-- 字典添加权限
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
    2075485573151580162,              -- parent_id: 父级权限ID（字典管理菜单的ID）
    '字典添加',                        -- permission_name: 权限名称
    'sys:dict:add',                   -- permission_code: 权限编码
    3,                                 -- permission_type: 权限类型（3-按钮）
    NULL,                              -- path: 路由地址（按钮权限为空）
    NULL,                              -- component: 组件路径（按钮权限为空）
    NULL,                              -- icon: 图标（按钮权限为空）
    2,                                 -- sort: 显示顺序
    1,                                 -- visible: 是否可见（1-显示）
    1,                                 -- status: 状态（1-启用）
    'system',                          -- create_by: 创建人
    NOW(),                             -- create_time: 创建时间
    NULL,                              -- update_by: 修改人
    NULL,                              -- update_time: 修改时间
    0,                                 -- del_flag: 删除标记（0-未删除）
    '字典添加权限，包含添加字典类型和添加字典项操作' -- remark: 备注
);

-- 字典修改权限
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
    2075485573151580162,              -- parent_id: 父级权限ID（字典管理菜单的ID）
    '字典修改',                        -- permission_name: 权限名称
    'sys:dict:edit',                  -- permission_code: 权限编码
    3,                                 -- permission_type: 权限类型（3-按钮）
    NULL,                              -- path: 路由地址（按钮权限为空）
    NULL,                              -- component: 组件路径（按钮权限为空）
    NULL,                              -- icon: 图标（按钮权限为空）
    3,                                 -- sort: 显示顺序
    1,                                 -- visible: 是否可见（1-显示）
    1,                                 -- status: 状态（1-启用）
    'system',                          -- create_by: 创建人
    NOW(),                             -- create_time: 创建时间
    NULL,                              -- update_by: 修改人
    NULL,                              -- update_time: 修改时间
    0,                                 -- del_flag: 删除标记（0-未删除）
    '字典修改权限，包含修改字典类型和修改字典项操作' -- remark: 备注
);

-- 字典删除权限
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
    2075485573151580162,              -- parent_id: 父级权限ID（字典管理菜单的ID）
    '字典删除',                        -- permission_name: 权限名称
    'sys:dict:delete',                -- permission_code: 权限编码
    3,                                 -- permission_type: 权限类型（3-按钮）
    NULL,                              -- path: 路由地址（按钮权限为空）
    NULL,                              -- component: 组件路径（按钮权限为空）
    NULL,                              -- icon: 图标（按钮权限为空）
    4,                                 -- sort: 显示顺序
    1,                                 -- visible: 是否可见（1-显示）
    1,                                 -- status: 状态（1-启用）
    'system',                          -- create_by: 创建人
    NOW(),                             -- create_time: 创建时间
    NULL,                              -- update_by: 修改人
    NULL,                              -- update_time: 修改时间
    0,                                 -- del_flag: 删除标记（0-未删除）
    '字典删除权限，包含删除字典类型和删除字典项操作' -- remark: 备注
);