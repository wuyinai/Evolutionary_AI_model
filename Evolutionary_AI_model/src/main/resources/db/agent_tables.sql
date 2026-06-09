-- Agent功能数据库表结构
-- 创建时间: 2026-06-09
-- 说明: 支持Agent工具管理、任务执行和日志记录

-- ==================== 1. Agent工具表 ====================
DROP TABLE IF EXISTS `ai_agent_tool`;
CREATE TABLE `ai_agent_tool` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `tool_name` VARCHAR(50) NOT NULL COMMENT '工具名称（唯一标识）',
    `tool_description` VARCHAR(500) NOT NULL COMMENT '工具描述（用于AI模型理解工具用途）',
    `tool_schema` TEXT DEFAULT NULL COMMENT '工具参数Schema（JSON格式）',
    `tool_class` VARCHAR(255) NOT NULL COMMENT '工具实现类全限定名',
    `tool_category` VARCHAR(50) DEFAULT NULL COMMENT '工具分类（如：计算、查询、搜索等）',
    `tool_version` VARCHAR(20) DEFAULT '1.0.0' COMMENT '工具版本号',
    `is_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用 1-启用',
    `is_system` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否系统工具：0-否 1-是',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除 1-已删除',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tool_name` (`tool_name`),
    KEY `idx_tool_category` (`tool_category`),
    KEY `idx_is_enabled` (`is_enabled`),
    KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent工具表';

-- ==================== 2. Agent任务表 ====================
DROP TABLE IF EXISTS `ai_agent_task`;
CREATE TABLE `ai_agent_task` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `task_id` VARCHAR(64) NOT NULL COMMENT '任务ID（唯一标识）',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
    `config_id` BIGINT NOT NULL COMMENT '模型配置ID，关联ai_model_config.id',
    `task_description` TEXT NOT NULL COMMENT '任务描述（用户希望Agent完成的目标）',
    `task_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '任务状态：PENDING-待执行、RUNNING-执行中、SUCCESS-成功、FAILED-失败、TIMEOUT-超时',
    `final_answer` LONGTEXT DEFAULT NULL COMMENT '最终答案（Agent的最终输出）',
    `total_steps` INT DEFAULT NULL COMMENT '总执行步数',
    `total_time_ms` BIGINT DEFAULT NULL COMMENT '总耗时（毫秒）',
    `available_tools` TEXT DEFAULT NULL COMMENT '可用工具列表（JSON数组格式）',
    `max_steps` INT DEFAULT 10 COMMENT '最大执行步数',
    `enable_detailed_log` TINYINT(1) DEFAULT 0 COMMENT '是否启用详细日志：0-否 1-是',
    `start_time` DATETIME DEFAULT NULL COMMENT '任务开始时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '任务结束时间',
    `error_message` TEXT DEFAULT NULL COMMENT '错误信息（如果失败）',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除 1-已删除',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_id` (`task_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_config_id` (`config_id`),
    KEY `idx_task_status` (`task_status`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent任务表';

-- ==================== 3. Agent执行日志表 ====================
DROP TABLE IF EXISTS `ai_agent_execution_log`;
CREATE TABLE `ai_agent_execution_log` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `task_id` VARCHAR(64) NOT NULL COMMENT '任务ID，关联ai_agent_task.task_id',
    `step_number` INT NOT NULL COMMENT '执行步骤序号',
    `tool_name` VARCHAR(50) NOT NULL COMMENT '工具名称',
    `tool_description` VARCHAR(500) DEFAULT NULL COMMENT '工具描述',
    `parameters` TEXT DEFAULT NULL COMMENT '执行参数（JSON格式）',
    `result` LONGTEXT DEFAULT NULL COMMENT '执行结果',
    `execution_status` VARCHAR(20) NOT NULL COMMENT '执行状态：SUCCESS-成功、FAILED-失败',
    `execution_time_ms` BIGINT DEFAULT NULL COMMENT '执行耗时（毫秒）',
    `execution_time` DATETIME DEFAULT NULL COMMENT '执行时间',
    `error_message` TEXT DEFAULT NULL COMMENT '错误信息（如果失败）',
    `thinking_process` TEXT DEFAULT NULL COMMENT '思考过程（Agent的推理过程）',
    `observation` TEXT DEFAULT NULL COMMENT '观察结果（Agent对工具返回结果的分析）',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除 1-已删除',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_tool_name` (`tool_name`),
    KEY `idx_step_number` (`step_number`),
    KEY `idx_execution_status` (`execution_status`),
    KEY `idx_execution_time` (`execution_time`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent执行日志表';

-- ==================== 4. 初始化工具数据 ====================
-- 插入基础工具数据（系统工具）
INSERT INTO `ai_agent_tool` (`id`, `tool_name`, `tool_description`, `tool_schema`, `tool_class`, `tool_category`, `tool_version`, `is_enabled`, `is_system`, `sort_order`, `remark`) VALUES
(1, 'calculator', '计算器工具，支持基本数学运算（加、减、乘、除）。输入表达式，返回计算结果。',
 '{"type":"object","properties":{"expression":{"type":"string","description":"数学表达式，如: 2+3, 10*5, 100/4"}},"required":["expression"]}',
 'com.example.evolutionary_ai_model.service.agent.tool.CalculatorTool', '计算', '1.0.0', 1, 1, 1, '系统内置计算器工具'),
(2, 'search', '搜索工具，根据关键词搜索相关信息。输入查询关键词，返回搜索结果摘要。',
 '{"type":"object","properties":{"query":{"type":"string","description":"搜索关键词或查询语句"}},"required":["query"]}',
 'com.example.evolutionary_ai_model.service.agent.tool.SearchTool', '查询', '1.0.0', 1, 1, 2, '系统内置搜索工具（模拟实现）'),
(3, 'weather', '天气查询工具，查询指定城市的天气信息。输入城市名称，返回天气状况。',
 '{"type":"object","properties":{"city":{"type":"string","description":"城市名称，如: 北京、上海、广州"}},"required":["city"]}',
 'com.example.evolutionary_ai_model.service.agent.tool.WeatherTool', '查询', '1.0.0', 1, 1, 3, '系统内置天气查询工具（模拟实现）'),
(4, 'time', '时间查询工具，查询当前时间和日期信息。可选输入格式类型，返回对应格式的时间。',
 '{"type":"object","properties":{"format":{"type":"string","description":"时间格式类型：default-默认格式，date-仅日期，time-仅时间，full-完整格式","enum":["default","date","time","full"]}},"required":[]}',
 'com.example.evolutionary_ai_model.service.agent.tool.TimeTool', '查询', '1.0.0', 1, 1, 4, '系统内置时间查询工具');

-- ==================== 5. 创建索引优化 ====================
-- 为提高查询性能，创建复合索引
ALTER TABLE `ai_agent_task` ADD INDEX `idx_user_status_time` (`user_id`, `task_status`, `start_time`);
ALTER TABLE `ai_agent_execution_log` ADD INDEX `idx_task_step` (`task_id`, `step_number`);