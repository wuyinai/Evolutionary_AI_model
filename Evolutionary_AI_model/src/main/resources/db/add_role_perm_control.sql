-- =============================================
-- 为 sys_role 表添加权限控制开关字段
-- =============================================
-- 作用：控制该角色是否受权限管控
-- perm_control = 1 表示启用权限控制（角色受权限限制）
-- perm_control = 0 表示禁用权限控制（角色不受权限限制，默认拥有全部权限）

ALTER TABLE `sys_role`
    ADD COLUMN `perm_control` TINYINT NOT NULL DEFAULT 1 COMMENT '权限控制开关：0-禁用，1-启用' AFTER `data_scope`;

-- 更新已存在的角色记录，默认启用权限控制
UPDATE `sys_role` SET `perm_control` = 1 WHERE `perm_control` IS NULL;
