-- AI模型供应商初始化数据
-- 插入常见的AI服务供应商信息

INSERT INTO `ai_model_provider` (`id`, `provider_code`, `provider_name`, `provider_icon`, `description`, `default_endpoint`, `supports_streaming`, `supports_vision`, `supports_function_call`, `auth_type`, `config_template`, `status`, `sort_order`, `create_time`, `update_time`, `del_flag`) VALUES
(1, 'OPENAI', 'OpenAI', NULL, 'OpenAI官方API，支持GPT系列模型', 'https://api.openai.com', 1, 1, 1, 'API_KEY', '{"models": ["gpt-4o", "gpt-4o-mini", "gpt-4-turbo", "gpt-3.5-turbo"]}', 1, 1, NOW(), NOW(), 0),
(2, 'DEEPSEEK', 'DeepSeek', NULL, 'DeepSeek深度求索，支持DeepSeek系列模型', 'https://api.deepseek.com', 1, 0, 1, 'API_KEY', '{"models": ["deepseek-chat", "deepseek-coder", "deepseek-reasoner"]}', 1, 2, NOW(), NOW(), 0),
(3, 'QWEN', '通义千问', NULL, '阿里云通义千问，支持Qwen系列模型', 'https://dashscope.aliyuncs.com/compatible-mode/v1', 1, 1, 1, 'API_KEY', '{"models": ["qwen-turbo", "qwen-plus", "qwen-max", "qwen-long"]}', 1, 3, NOW(), NOW(), 0),
(4, 'ERNIE', '文心一言', NULL, '百度文心一言，支持ERNIE系列模型', 'https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat', 1, 0, 1, 'API_KEY', '{"models": ["ernie-4.0-8k", "ernie-3.5-8k", "ernie-speed-8k"]}', 1, 4, NOW(), NOW(), 0),
(5, 'CLAUDE', 'Anthropic Claude', NULL, 'Anthropic Claude系列模型', 'https://api.anthropic.com', 1, 1, 1, 'API_KEY', '{"models": ["claude-3-opus", "claude-3-sonnet", "claude-3-haiku"]}', 1, 5, NOW(), NOW(), 0),
(6, 'OLLAMA', 'Ollama本地部署', NULL, 'Ollama本地模型部署服务', 'http://localhost:11434', 1, 0, 0, 'CUSTOM', '{"models": ["llama3", "mistral", "codellama"]}', 1, 6, NOW(), NOW(), 0),
(7, 'MOONSHOT', 'Moonshot Kimi', NULL, 'Moonshot Kimi长文本对话模型', 'https://api.moonshot.cn', 1, 0, 0, 'API_KEY', '{"models": ["moonshot-v1-8k", "moonshot-v1-32k", "moonshot-v1-128k"]}', 1, 7, NOW(), NOW(), 0),
(8, 'ZHIPU', '智谱清言', NULL, '智谱AI GLM系列模型', 'https://open.bigmodel.cn/api/paas/v4', 1, 1, 1, 'API_KEY', '{"models": ["glm-4", "glm-4-flash", "glm-3-turbo"]}', 1, 8, NOW(), NOW(), 0);