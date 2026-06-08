# AI供应商数据对比表

## 一、供应商数据更新说明

### 新增字段
- **protocol_type** - 协议类型字段，标识供应商使用的API协议格式
- **remark** - 备注字段，提供供应商的详细说明和使用建议

### 协议类型映射

| 供应商编码 | 供应商名称 | 协议类型 | 说明 |
|-----------|-----------|---------|------|
| OPENAI | OpenAI | OPENAI | OpenAI官方API，使用标准OpenAI协议 |
| DEEPSEEK | DeepSeek | OPENAI | 使用OpenAI兼容协议，API格式与OpenAI相同 |
| QWEN | 通义千问 | OPENAI | 使用OpenAI兼容协议，通过compatible-mode接口 |
| ERNIE | 文心一言 | ERNIE | 使用百度专用协议，需要access token认证 |
| CLAUDE | Anthropic Claude | ANTHROPIC | 使用Anthropic专用协议，API格式不同 |
| OLLAMA | Ollama本地部署 | OLLAMA | 使用Ollama协议（兼容OpenAI格式），本地部署 |
| MOONSHOT | Moonshot Kimi | MOONSHOT | 使用Moonshot协议（兼容OpenAI格式） |
| ZHIPU | 智谱清言 | ZHIPU | 使用智谱专用协议，API格式不同 |
| AZURE_OPENAI | Azure OpenAI | OPENAI | 使用OpenAI兼容协议，需要deployment配置 |
| SILICONFLOW | SiliconFlow | OPENAI | 使用OpenAI兼容协议，提供开源模型API |

## 二、供应商详细数据

### 供应商列表（重构后）

#### 1. OpenAI
```json
{
  "id": 1,
  "provider_code": "OPENAI",
  "provider_name": "OpenAI",
  "protocol_type": "OPENAI",
  "default_endpoint": "https://api.openai.com/v1",
  "models": ["gpt-4o", "gpt-4o-mini", "gpt-4-turbo", "gpt-3.5-turbo", "o1-preview", "o1-mini"],
  "features": ["chat", "vision", "function_call", "streaming"],
  "remark": "OpenAI官方API，最成熟的AI服务提供商"
}
```

#### 2. DeepSeek
```json
{
  "id": 2,
  "provider_code": "DEEPSEEK",
  "provider_name": "DeepSeek",
  "protocol_type": "OPENAI",
  "default_endpoint": "https://api.deepseek.com",
  "models": ["deepseek-chat", "deepseek-coder", "deepseek-reasoner"],
  "features": ["chat", "function_call", "streaming"],
  "remark": "DeepSeek深度求索，性价比高，支持推理和编程"
}
```

#### 3. 通义千问
```json
{
  "id": 3,
  "provider_code": "QWEN",
  "provider_name": "通义千问",
  "protocol_type": "OPENAI",
  "default_endpoint": "https://dashscope.aliyuncs.com/compatible-mode/v1",
  "models": ["qwen-turbo", "qwen-plus", "qwen-max", "qwen-max-longcontext", "qwen-long"],
  "features": ["chat", "vision", "function_call", "streaming"],
  "remark": "阿里云通义千问，支持长文本和多模态"
}
```

#### 4. 文心一言
```json
{
  "id": 4,
  "provider_code": "ERNIE",
  "provider_name": "文心一言",
  "protocol_type": "ERNIE",
  "default_endpoint": "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat",
  "models": ["ernie-4.0-8k", "ernie-4.0-turbo-8k", "ernie-3.5-8k", "ernie-3.5-turbo-8k", "ernie-speed-8k", "ernie-speed-128k"],
  "features": ["chat", "function_call", "streaming"],
  "auth_config": {"need_access_token": true},
  "remark": "百度文心一言，需要特殊的access token认证方式"
}
```

#### 5. Anthropic Claude
```json
{
  "id": 5,
  "provider_code": "CLAUDE",
  "provider_name": "Anthropic Claude",
  "protocol_type": "ANTHROPIC",
  "default_endpoint": "https://api.anthropic.com/v1",
  "models": ["claude-3-5-sonnet-20241022", "claude-3-opus-20240229", "claude-3-sonnet-20240229", "claude-3-haiku-20240307"],
  "features": ["chat", "vision", "function_call", "streaming"],
  "remark": "Anthropic Claude，擅长长文本和复杂推理"
}
```

#### 6. Ollama本地部署
```json
{
  "id": 6,
  "provider_code": "OLLAMA",
  "provider_name": "Ollama本地部署",
  "protocol_type": "OLLAMA",
  "default_endpoint": "http://localhost:11434",
  "models": ["llama3.1", "llama3.2", "mistral", "codellama", "qwen2.5", "deepseek-coder"],
  "features": ["chat", "streaming"],
  "note": "本地部署，无需API密钥",
  "remark": "Ollama本地部署，完全免费，支持多种开源模型"
}
```

#### 7. Moonshot Kimi
```json
{
  "id": 7,
  "provider_code": "MOONSHOT",
  "provider_name": "Moonshot Kimi",
  "protocol_type": "MOONSHOT",
  "default_endpoint": "https://api.moonshot.cn/v1",
  "models": ["moonshot-v1-8k", "moonshot-v1-32k", "moonshot-v1-128k"],
  "features": ["chat", "streaming"],
  "advantage": "超长文本支持",
  "remark": "Moonshot Kimi，擅长超长文本处理"
}
```

#### 8. 智谱清言
```json
{
  "id": 8,
  "provider_code": "ZHIPU",
  "provider_name": "智谱清言",
  "protocol_type": "ZHIPU",
  "default_endpoint": "https://open.bigmodel.cn/api/paas/v4",
  "models": ["glm-4", "glm-4-flash", "glm-4-plus", "glm-3-turbo"],
  "features": ["chat", "vision", "function_call", "streaming"],
  "remark": "智谱清言，国产大模型，性价比高"
}
```

#### 9. Azure OpenAI
```json
{
  "id": 9,
  "provider_code": "AZURE_OPENAI",
  "provider_name": "Azure OpenAI",
  "protocol_type": "OPENAI",
  "default_endpoint": "https://YOUR_RESOURCE.openai.azure.com",
  "models": ["gpt-4o", "gpt-4-turbo", "gpt-35-turbo"],
  "features": ["chat", "vision", "function_call", "streaming"],
  "config_required": ["deployment_name", "api_version"],
  "remark": "Azure OpenAI，企业级服务，需要deployment配置"
}
```

#### 10. SiliconFlow（新增）
```json
{
  "id": 10,
  "provider_code": "SILICONFLOW",
  "provider_name": "SiliconFlow",
  "protocol_type": "OPENAI",
  "default_endpoint": "https://api.siliconflow.cn/v1",
  "models": ["Qwen/Qwen2.5-7B-Instruct", "Qwen/Qwen2.5-72B-Instruct", "deepseek-ai/DeepSeek-V2.5", "THUDM/glm-4-9b-chat"],
  "features": ["chat", "streaming"],
  "remark": "SiliconFlow算力平台，提供多种开源模型API服务"
}
```

## 三、协议驱动说明

### 协议类型分类

**OpenAI兼容协议（OPENAI）**
- OpenAI、DeepSeek、通义千问、Azure OpenAI、SiliconFlow
- 使用标准OpenAI API格式
- 接口路径：`/v1/chat/completions`
- 认证方式：API Key

**Anthropic协议（ANTHROPIC）**
- Claude系列模型
- 使用Anthropic专用API格式
- 接口路径：`/v1/messages`
- 认证方式：API Key

**Ollama协议（OLLAMA）**
- 本地部署服务
- 兼容OpenAI格式，但端点不同
- 接口路径：`/api/chat` 或 `/api/generate`
- 认证方式：无需API Key

**百度文心协议（ERNIE）**
- 百度文心一言
- 使用百度专用API格式
- 需要先获取access token
- 认证方式：API Key + Access Token

**智谱协议（ZHIPU）**
- 智谱清言
- 使用智谱专用API格式
- 认证方式：API Key

**Moonshot协议（MOONSHOT）**
- Moonshot Kimi
- 兼容OpenAI格式
- 认证方式：API Key

### 协议路由逻辑

```
用户选择供应商 → 获取protocol_type → ProviderChatModelFactory路由
→ 对应的ChatModelBuilder → 构建ChatModel实例 → 创建ChatClient
```

## 四、使用示例

### 创建供应商配置（OpenAI）

```java
AiProviderConfig providerConfig = new AiProviderConfig();
providerConfig.setConfigName("我的OpenAI配置");
providerConfig.setProviderCode("OPENAI");
providerConfig.setProtocolType("OPENAI"); // 自动推断，也可以手动指定
providerConfig.setApiKey("sk-xxxxxxxxxxxxx");
providerConfig.setApiEndpoint("https://api.openai.com/v1"); // 可选，默认使用供应商默认端点
Long providerConfigId = providerConfigService.addConfig(userId, providerConfig);
```

### 创建模型配置（GPT-4o）

```java
AiModelConfig modelConfig = new AiModelConfig();
modelConfig.setProviderConfigId(providerConfigId); // 关联供应商配置
modelConfig.setModelName("gpt-4o");
modelConfig.setTemperature(0.7);
modelConfig.setMaxTokens(4096);
modelConfigService.addConfig(userId, modelConfig);
```

### 协议自动推断

```java
// 系统会根据provider_code自动推断protocol_type
ModelProtocol protocol = ModelProtocol.fromProviderCode("DEEPSEEK");
// 返回：OPENAI（因为DeepSeek使用OpenAI兼容协议）
```

## 五、迁移建议

### 对于现有用户

1. **无需立即迁移** - 新旧架构可以并存
2. **逐步迁移** - 建议先创建新的供应商配置
3. **测试验证** - 测试新架构功能是否正常
4. **完全迁移** - 确认无误后删除旧配置

### 对于新用户

1. **直接使用新架构** - 创建两级配置
2. **利用协议驱动** - 自动路由到对应的协议构建器
3. **使用会话钉选** - 实现会话级模型选择

## 六、SQL文件说明

### 已创建的SQL文件

1. **AI_complete_migration.sql** - 完整迁移脚本
   - 包含表结构修改
   - 包含供应商数据初始化
   - 包含数据迁移（可选）
   - 包含验证和清理

2. **ai_provider_init_refactored.sql** - 供应商初始化数据
   - 包含protocol_type字段
   - 包含完整的供应商信息
   - 包含模型列表和特性

### 执行顺序

```bash
# 1. 备份数据库
mysqldump -u username -p database_name > backup.sql

# 2. 执行完整迁移脚本
mysql -u username -p database_name < AI_complete_migration.sql

# 3. 验证数据
mysql -u username -p database_name -e "SELECT id, provider_code, protocol_type FROM ai_model_provider ORDER BY sort_order;"
```

## 七、注意事项

1. **协议类型必须正确** - 影响ChatModel构建
2. **API端点格式** - 不同协议的端点格式可能不同
3. **认证方式** - 文心一言需要特殊的access token
4. **本地部署** - Ollama无需API密钥
5. **企业服务** - Azure OpenAI需要deployment配置

## 八、技术支持

如有问题，请参考：
- [AI模型配置重构方案.md](file:///d:/Project/MyProject/Evolutionary_AI_model/AI模型配置重构方案.md)
- [AI模型配置迁移指南.md](file:///d:/Project/MyProject/Evolutionary_AI_model/AI模型配置迁移指南.md)
- [AI_complete_migration.sql](file:///d:/Project/MyProject/Evolutionary_AI_model/Evolutionary_AI_model/src/main/resources/db/AI_complete_migration.sql)