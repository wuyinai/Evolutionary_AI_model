# AI模型配置重构方案

## 一、重构背景

当前项目的AI问答功能存在以下问题：
1. **两级配置不清晰**：`ai_model_config` 表既包含连接信息（api_key, api_endpoint），又包含推理参数（temperature, max_tokens），职责不清晰
2. **协议驱动缺失**：代码硬编码使用 OpenAI API，无法根据不同供应商的协议类型动态路由
3. **会话级钉选未实现**：虽然 `ai_conversation` 表有 `config_id` 字段，但代码中没有实现会话级模型钉选逻辑

## 二、重构目标

实现三个核心需求：
1. **两级配置**：Provider 管连接（密钥、URL、协议），ModelConfig 管推理参数（温度、token 上限），一对多关系
2. **协议驱动工厂**：Provider 存协议标识 → ModelProtocol 枚举映射 → ProviderChatModelFactory 路由到对应 ChatModelBuilder → 动态构建 ChatModel 实例
3. **会话级钉选**：用户在聊天界面选择模型后钉选到会话，后续该会话所有消息都用此模型，显式选择绕过能力路由确保"用户选什么用什么"

## 三、重构方案详解

### 3.1 两级配置架构

#### 数据库表结构调整

**新增表：`ai_provider_config`**
- 管理供应商连接配置（密钥、URL、协议类型）
- 一个供应商配置可以关联多个模型配置

**重构表：`ai_model_config`**
- 只管理推理参数（温度、token上限、采样参数等）
- 通过 `provider_config_id` 关联供应商配置

**修改表：`ai_model_provider`**
- 新增 `protocol_type` 字段，标识协议类型

**修改表：`ai_conversation`**
- 新增 `pinned_config_id` 字段，实现会话级模型钉选

#### 实体类对应关系

```
AiModelProvider (供应商基础信息)
    ├── protocol_type: 协议类型枚举
    └── ...

AiProviderConfig (供应商连接配置)
    ├── provider_id: 关联 AiModelProvider
    ├── protocol_type: 协议类型（冗余）
    ├── api_key: API密钥（加密）
    ├── api_endpoint: API端点
    └── ...

AiModelConfig (模型推理参数配置)
    ├── provider_config_id: 关联 AiProviderConfig
    ├── model_name: 模型名称
    ├── temperature: 温度参数
    ├── max_tokens: Token上限
    └── ...

AiConversation (会话)
    ├── pinned_config_id: 钉选的模型配置ID
    └── ...
```

### 3.2 协议驱动工厂

#### 协议枚举设计

```java
public enum ModelProtocol {
    OPENAI("OPENAI", "OpenAI兼容协议", "使用OpenAI API格式"),
    ANTHROPIC("ANTHROPIC", "Anthropic协议", "使用Anthropic Claude API格式"),
    OLLAMA("OLLAMA", "Ollama协议", "使用Ollama本地部署API格式"),
    ERNIE("ERNIE", "文心一言协议", "使用百度文心一言API格式"),
    ZHIPU("ZHIPU", "智谱清言协议", "使用智谱GLM API格式"),
    MOONSHOT("MOONSHOT", "Moonshot协议", "使用Moonshot API格式");
}
```

#### 工厂模式架构

```
ProviderChatModelFactory (工厂)
    ├── builderMap: 协议构建器注册表
    ├── clientCache: ChatClient缓存
    └── createChatClient(): 根据协议路由到对应构建器

ChatModelBuilder (策略接口)
    ├── getSupportedProtocol(): 返回支持的协议
    └── build(): 构建ChatModel实例

OpenAiChatModelBuilder (OpenAI协议构建器)
    ├── 支持: OPENAI协议
    └── 构建: OpenAiChatModel实例

OllamaChatModelBuilder (Ollama协议构建器)
    ├── 支持: OLLAMA协议
    └── 构建: OpenAiChatModel实例（兼容格式）

... 其他协议构建器
```

#### 协议路由流程

```
1. 从 AiProviderConfig 获取 protocol_type
2. ModelProtocol.fromCode() 转换为枚举
3. ProviderChatModelFactory.getBuilder(protocol) 获取构建器
4. ChatModelBuilder.build() 构建对应的 ChatModel
5. ChatClient.builder(chatModel).build() 创建 ChatClient
```

### 3.3 会话级钉选逻辑

#### 模型配置获取优先级

```
优先级1: request.configId (显式指定)
    → 用户明确选择，绕过所有路由逻辑

优先级2: conversation.pinnedConfigId (会话钉选)
    → 用户在聊天界面钉选的模型，确保"用户选什么用什么"

优先级3: user.defaultConfig (用户默认)
    → 用户未指定时的默认配置
```

#### 钉选流程

```
1. 用户在聊天界面选择模型
2. 调用 AiConversationService.pinModelToConversation()
3. 更新 ai_conversation.pinned_config_id
4. 后续该会话所有消息使用钉选的模型配置
```

#### DynamicChatStrategy 重构

```java
private AiModelConfig getModelConfig(ChatRequestDTO request) {
    // 优先级1：显式指定
    if (request.getConfigId() != null) {
        return modelConfigService.getConfigById(request.getConfigId());
    }
    
    // 优先级2：会话钉选
    if (request.getConversationId() != null) {
        Long pinnedConfigId = conversationService.getPinnedModelConfigId(request.getConversationId());
        if (pinnedConfigId != null) {
            return modelConfigService.getConfigById(pinnedConfigId);
        }
    }
    
    // 优先级3：用户默认
    if (request.getUserId() != null) {
        return modelConfigService.getDefaultConfig(request.getUserId());
    }
    
    throw new RuntimeException("未找到可用的模型配置");
}
```

## 四、实施步骤

### 4.1 数据库迁移

执行 `AI_refactor.sql` 文件：
1. 修改 `ai_model_provider` 表，添加 `protocol_type` 字段
2. 创建 `ai_provider_config` 表
3. 迁移现有数据到新表结构
4. 修改 `ai_conversation` 表，添加 `pinned_config_id` 字段

### 4.2 代码更新

1. 创建协议枚举和工厂类
2. 更新实体类、DTO、VO
3. 创建新的 Service 和 Mapper
4. 重构 DynamicChatStrategy
5. 更新 Controller 接口

### 4.3 测试验证

1. 测试两级配置创建流程
2. 测试协议驱动工厂路由
3. 测试会话级模型钉选
4. 测试不同供应商的API调用

## 五、使用指南

### 5.1 配置流程

**步骤1：创建供应商配置**
```java
AiProviderConfig providerConfig = new AiProviderConfig();
providerConfig.setProviderCode("OPENAI");
providerConfig.setProtocolType("OPENAI");
providerConfig.setApiKey("sk-xxx");
providerConfig.setApiEndpoint("https://api.openai.com");
providerConfigService.addConfig(userId, providerConfig);
```

**步骤2：创建模型配置**
```java
AiModelConfig modelConfig = new AiModelConfig();
modelConfig.setProviderConfigId(providerConfig.getId());
modelConfig.setModelName("gpt-4o");
modelConfig.setTemperature(0.7);
modelConfig.setMaxTokens(4096);
modelConfigService.addConfig(userId, modelConfig);
```

### 5.2 会话钉选

**钉选模型到会话**
```java
conversationService.pinModelToConversation(conversationId, userId, modelConfigId);
```

**获取钉选的模型**
```java
Long pinnedConfigId = conversationService.getPinnedModelConfigId(conversationId);
```

### 5.3 对话调用

**显式指定模型**
```json
{
  "conversationId": "xxx",
  "message": "你好",
  "mode": "dynamic",
  "configId": 123456789
}
```

**使用会话钉选模型**
```json
{
  "conversationId": "xxx",
  "message": "你好",
  "mode": "dynamic"
}
```

## 六、架构优势

### 6.1 职责清晰
- Provider配置：管理连接信息，职责单一
- Model配置：管理推理参数，职责单一
- 一对多关系：一个连接配置支持多个模型

### 6.2 协议驱动
- 协议枚举：标准化协议类型定义
- 工厂模式：根据协议动态路由
- 易于扩展：新增协议只需添加构建器

### 6.3 会话钉选
- 用户选择优先：确保"用户选什么用什么"
- 会话级隔离：不同会话可以使用不同模型
- 绕过路由：显式选择不受能力路由影响

## 七、后续优化建议

1. **前端适配**：更新前端页面支持两级配置创建和会话钉选
2. **协议扩展**：添加更多协议构建器（Anthropic、文心一言等）
3. **缓存优化**：优化ChatClient缓存策略，支持配置更新自动刷新
4. **监控统计**：完善调用日志记录，支持协议类型统计
5. **配置管理**：添加配置导入导出功能，支持批量配置

## 八、文件清单

### 新增文件
- `ModelProtocol.java` - 协议枚举
- `AiProviderConfig.java` - 供应商配置实体
- `AiProviderConfigMapper.java` - 供应商配置Mapper
- `AiProviderConfigService.java` - 供应商配置Service接口
- `AiConversationService.java` - 会话Service接口
- `AiConversationServiceImpl.java` - 会话Service实现
- `ChatModelBuilder.java` - ChatModel构建器接口
- `OpenAiChatModelBuilder.java` - OpenAI协议构建器
- `OllamaChatModelBuilder.java` - Ollama协议构建器
- `ProviderChatModelFactory.java` - Provider ChatModel工厂
- `DynamicChatStrategy_refactored.java` - 重构后的动态策略
- `AI_refactor.sql` - 数据库重构SQL

### 修改文件
- `AiModelConfig.java` - 移除连接字段，只保留推理参数
- `AiConversation.java` - 添加钉选字段
- `AiModelProvider.java` - 添加协议类型字段（通过SQL）

## 九、注意事项

1. **数据迁移**：执行SQL前请备份现有数据
2. **API密钥加密**：确保使用AES加密存储
3. **协议兼容性**：不同供应商可能使用相同协议（如DeepSeek使用OpenAI协议）
4. **缓存清理**：配置更新后需清理ChatClient缓存
5. **权限验证**：会话钉选需验证会话归属用户