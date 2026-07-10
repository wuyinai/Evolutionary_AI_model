-- =============================================
-- 为字典表添加字典名称字段
-- =============================================
ALTER TABLE `sys_dict` ADD COLUMN `dict_name` VARCHAR(100) DEFAULT NULL COMMENT '字典名称（中文名称）' AFTER `dict_type`;

-- =============================================
-- 更新已有数据的字典名称
-- =============================================
UPDATE `sys_dict` SET `dict_name` = '审批类型' WHERE `dict_type` = 'approval_type';
UPDATE `sys_dict` SET `dict_name` = '审批状态' WHERE `dict_type` = 'approval_status';