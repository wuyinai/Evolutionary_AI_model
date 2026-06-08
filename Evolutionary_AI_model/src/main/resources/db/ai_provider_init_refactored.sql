-- AI模型供应商初始化数据（重构后）
-- 添加protocol_type字段，支持协议驱动工厂
-- 插入常见的AI服务供应商信息

INSERT INTO `ai_model_provider` (`id`, `provider_code`, `provider_name`, `provider_icon`, `description`, `default_endpoint`, `supports_streaming`, `supports_vision`, `supports_function_call`, `auth_type`, `protocol_type`, `config_template`, `status`, `sort_order`, `create_time`, `update_time`, `del_flag`, `remark`) VALUES
(1, 'OPENAI', 'OpenAI', NULL, 'OpenAI官方API，支持GPT系列模型，使用OpenAI兼容协议', 'https://api.openai.com/v1', 1, 1, 1, 'API_KEY', 'OPENAI', '{"models": ["gpt-4o", "gpt-4o-mini", "gpt-4-turbo", "gpt-3.5-turbo", "o1-preview", "o1-mini"], "features": ["chat", "vision", "function_call", "streaming"]}', 1, 1, NOW(), NOW(), 0, 'OpenAI官方API，最成熟的AI服务提供商'),
(2, 'DEEPSEEK', 'DeepSeek', NULL, 'DeepSeek深度求索，支持DeepSeek系列模型，使用OpenAI兼容协议', 'https://api.deepseek.com', 1, 0, 1, 'API_KEY', 'OPENAI', '{"models": ["deepseek-chat", "deepseek-coder", "deepseek-reasoner"], "features": ["chat", "function_call", "streaming"]}', 1, 2, NOW(), NOW(), 0, 'DeepSeek深度求索，性价比高，支持推理和编程'),
(3, 'QWEN', '通义千问', NULL, '阿里云通义千问，支持Qwen系列模型，使用OpenAI兼容协议', 'https://dashscope.aliyuncs.com/compatible-mode/v1', 1, 1, 1, 'API_KEY', 'OPENAI', '{"models": ["qwen-turbo", "qwen-plus", "qwen-max", "qwen-max-longcontext", "qwen-long"], "features": ["chat", "vision", "function_call", "streaming"]}', 1, 3, NOW(), NOW(), 0, '阿里云通义千问，支持长文本和多模态'),
(4, 'ERNIE', '文心一言', NULL, '百度文心一言，支持ERNIE系列模型，使用百度专用协议', 'https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat', 1, 0, 1, 'API_KEY', 'ERNIE', '{"models": ["ernie-4.0-8k", "ernie-4.0-turbo-8k", "ernie-3.5-8k", "ernie-3.5-turbo-8k", "ernie-speed-8k", "ernie-speed-128k"], "features": ["chat", "function_call", "streaming"], "auth_config": {"need_access_token": true}}', 1, 4, NOW(), NOW(), 0, '百度文心一言，需要特殊的access token认证方式'),
(5, 'CLAUDE', 'Anthropic Claude', NULL, 'Anthropic Claude系列模型，使用Anthropic专用协议', 'https://api.anthropic.com/v1', 1, 1, 1, 'API_KEY', 'ANTHROPIC', '{"models": ["claude-3-5-sonnet-20241022", "claude-3-opus-20240229", "claude-3-sonnet-20240229", "claude-3-haiku-20240307"], "features": ["chat", "vision", "function_call", "streaming"]}', 1, 5, NOW(), NOW(), 0, 'Anthropic Claude，擅长长文本和复杂推理'),
(6, 'OLLAMA', 'Ollama本地部署', NULL, 'Ollama本地模型部署服务，使用Ollama协议（兼容OpenAI格式）', 'http://localhost:11434', 1, 0, 0, 'CUSTOM', 'OLLAMA', '{"models": ["llama3.1", "llama3.2", "mistral", "codellama", "qwen2.5", "deepseek-coder"], "features": ["chat", "streaming"], "note": "本地部署，无需API密钥"}', 1, 6, NOW(), NOW(), 0, 'Ollama本地部署，完全免费，支持多种开源模型'),
(7, 'MOONSHOT', 'Moonshot Kimi', NULL, 'Moonshot Kimi长文本对话模型，使用Moonshot协议（兼容OpenAI格式）', 'https://api.moonshot.cn/v1', 1, 0, 0, 'API_KEY', 'MOONSHOT', '{"models": ["moonshot-v1-8k", "moonshot-v1-32k", "moonshot-v1-128k"], "features": ["chat", "streaming"], "advantage": "超长文本支持"}', 1, 7, NOW(), NOW(), 0, 'Moonshot Kimi，擅长超长文本处理'),
(8, 'ZHIPU', '智谱清言', NULL, '智谱AI GLM系列模型，使用智谱专用协议', 'https://open.bigmodel.cn/api/paas/v4', 1, 1, 1, 'API_KEY', 'ZHIPU', '{"models": ["glm-4", "glm-4-flash", "glm-4-plus", "glm-3-turbo"], "features": ["chat", "vision", "function_call", "streaming"]}', 1, 8, NOW(), NOW(), 0, '智谱清言，国产大模型，性价比高'),
(9, 'AZURE_OPENAI', 'Azure OpenAI', NULL, '微软Azure OpenAI服务，使用OpenAI兼容协议', 'https://YOUR_RESOURCE.openai.azure.com', 1, 1, 1, 'API_KEY', 'OPENAI', '{"models": ["gpt-4o", "gpt-4-turbo", "gpt-35-turbo"], "features": ["chat", "vision", "function_call", "streaming"], "config_required": ["deployment_name", "api_version"]}', 1, 9, NOW(), NOW(), 0, 'Azure OpenAI，企业级服务，需要deployment配置'),
(10, 'SILICONFLOW', 'SiliconFlow', NULL, 'SiliconFlow算力平台，使用OpenAI兼容协议', 'https://api.siliconflow.cn/v1', 1, 1, 1, 'API_KEY', 'OPENAI', '{"models": ["Qwen/Qwen2.5-7B-Instruct", "Qwen/Qwen2.5-72B-Instruct", "deepseek-ai/DeepSeek-V2.5", "THUDM/glm-4-9b-chat"], "features": ["chat", "streaming"]}', 1, 10, NOW(), NOW(), 0, 'SiliconFlow算力平台，提供多种开源模型API服务');

-- 说明：
-- 1. protocol_type字段标识协议类型，用于驱动ProviderChatModelFactory路由到对应的ChatModelBuilder
-- 2. OPENAI协议：OpenAI、DeepSeek、通义千问、Azure OpenAI、SiliconFlow等使用OpenAI兼容API格式
-- 3. ANTHROPIC协议：Claude使用Anthropic专用API格式
-- 4. OLLAMA协议：Ollama本地部署，使用兼容OpenAI的格式
-- 5. ERNIE协议：百度文心一言，需要特殊的access token认证
-- 6. ZHIPU协议：智谱清言，使用智谱专用API格式
-- 7. MOONSHOT协议：Moonshot Kimi，使用兼容OpenAI的格式
-- 8. config_template字段包含该供应商支持的模型列表和特性信息（JSON格式）
-- 9. remark字段提供供应商的详细说明和使用建议