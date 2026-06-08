# AI模型配置迁移指南

## 一、当前状态说明

为了确保现有功能正常运行，AiModelConfig 实体类目前保留了两种架构的字段：

### 新架构字段（推荐使用）
- `providerConfigId` - 供应商配置ID，关联 ai_provider_config.id
- 其他推理参数字段（temperature、maxTokens等）

### 兼容字段（过渡期保留，未来将移除）
- `providerId` - 供应商ID（旧架构）
- `providerCode` - 供应商编码（旧架构）
- `apiKey` - API密钥（旧架构）
- `apiEndpoint` - API端点（旧架构）
- `extraConfig` - 扩展配置（旧架构）
- `timeoutSeconds` - 超时时间（旧架构）
- `maxRetries` - 重试次数（旧架构）

## 二、迁移步骤

### 步骤1：执行数据库迁移

执行 `AI_refactor.sql` 文件，创建新的表结构：

```bash
mysql -u username -p database_name < AI_refactor.sql
```

这将：
1. 创建 `ai_provider_config` 表（管理连接信息）
2. 重构 `ai_model_config` 表（只管理推理参数）
3. 迁移现有数据到新表结构
4. 添加协议类型和会话钉选字段

### 步骤2：更新配置创建流程

**旧流程（单级配置）**
```java
// 直接创建模型配置，包含连接信息
AiModelConfig config = new AiModelConfig();
config.setProviderCode("OPENAI");
config.setApiKey("sk-xxx");
config.setApiEndpoint("https://api.openai.com");
config.setModelName("gpt-4o");
config.setTemperature(0.7);
modelConfigService.addConfig(userId, dto);
```

**新流程（两级配置）**
```java
// 步骤1：创建供应商配置（管理连接信息）
AiProviderConfig providerConfig = new AiProviderConfig();
providerConfig.setConfigName("我的OpenAI配置");
providerConfig.setProviderCode("OPENAI");
providerConfig.setProtocolType("OPENAI");
providerConfig.setApiKey("sk-xxx");
providerConfig.setApiEndpoint("https://api.openai.com");
Long providerConfigId = providerConfigService.addConfig(userId, providerConfig);

// 步骤2：创建模型配置（管理推理参数）
AiModelConfig modelConfig = new AiModelConfig();
modelConfig.setProviderConfigId(providerConfigId); // 关联供应商配置
modelConfig.setModelName("gpt-4o");
modelConfig.setTemperature(0.7);
modelConfig.setMaxTokens(4096);
modelConfigService.addConfig(userId, modelConfig);
```

### 步骤3：更新对话调用流程

**旧流程**
```java
// 直接使用模型配置中的连接信息
AiModelConfig config = modelConfigService.getConfigById(configId);
// config.getApiKey() 和 config.getApiEndpoint() 可用
```

**新流程**
```java
// 通过模型配置获取关联的供应商配置
AiModelConfig modelConfig = modelConfigService.getConfigById(configId);
AiProviderConfig providerConfig = providerConfigService.getConfigById(modelConfig.getProviderConfigId());
// providerConfig.getApiKey() 和 providerConfig.getApiEndpoint() 可用
```

### 步骤4：实现会话钉选

```java
// 用户在聊天界面选择模型后钉选到会话
conversationService.pinModelToConversation(conversationId, userId, modelConfigId);

// 后续对话会自动使用钉选的模型
// 无需每次指定 configId
```

## 三、兼容性说明

### 当前阶段（过渡期）

**特点：**
- AiModelConfig 同时包含新旧两种字段
- 现有代码可以继续使用旧字段（providerId、apiKey等）
- 新代码建议使用新字段（providerConfigId）

**兼容逻辑：**
```java
// DynamicChatStrategy 中的兼容处理
private AiProviderConfig getProviderConfig(AiModelConfig modelConfig) {
    // 新架构：通过 providerConfigId 获取
    if (modelConfig.getProviderConfigId() != null) {
        return providerConfigService.getConfigById(modelConfig.getProviderConfigId());
    }
    
    // 旧架构兼容：直接从 modelConfig 获取连接信息
    // 将旧字段映射到 AiProviderConfig
    AiProviderConfig providerConfig = new AiProviderConfig();
    providerConfig.setApiKey(modelConfig.getApiKey());
    providerConfig.setApiEndpoint(modelConfig.getApiEndpoint());
    providerConfig.setProtocolType(ModelProtocol.fromProviderCode(modelConfig.getProviderCode()).getCode());
    return providerConfig;
}
```

### 未来阶段（完全迁移）

**时间点：** 所有用户完成数据迁移后

**操作：**
1. 移除 AiModelConfig 中的兼容字段
2. 更新所有使用旧字段的代码
3. 删除旧字段对应的数据库列

## 四、迁移建议

### 对于新用户

**建议：** 直接使用新的两级配置架构

**优势：**
- 职责清晰，易于管理
- 支持一个连接配置关联多个模型
- 支持协议驱动和会话钉选
- 更好的扩展性

### 对于现有用户

**建议：** 分阶段迁移

**阶段1：** 继续使用现有配置（兼容字段可用）
**阶段2：** 创建新的供应商配置
**阶段3：** 将模型配置关联到供应商配置
**阶段4：** 测试新架构功能
**阶段5：** 删除旧的配置数据

## 五、API接口变化

### 新增接口

**供应商配置管理**
```
POST   /ai/provider-config/add       - 添加供应商配置
GET    /ai/provider-config/list      - 获取供应商配置列表
PUT    /ai/provider-config/update    - 更新供应商配置
DELETE /ai/provider-config/delete/{id} - 删除供应商配置
PUT    /ai/provider-config/set-default/{id} - 设置默认配置
POST   /ai/provider-config/test/{id} - 测试连接
```

**会话管理**
```
POST   /ai/conversation/create       - 创建会话
GET    /ai/conversation/{id}         - 获取会话详情
PUT    /ai/conversation/pin-model    - 钉选模型到会话
PUT    /ai/conversation/update-title - 更新会话标题
```

### 保持兼容的接口

**模型配置管理**
```
POST   /ai/config/add                - 添加模型配置（兼容旧参数）
GET    /ai/config/list               - 获取模型配置列表
PUT    /ai/config/update             - 更新模型配置（兼容旧参数）
DELETE /ai/config/delete/{id}        - 删除模型配置
PUT    /ai/config/set-default/{id}   - 设置默认模型
POST   /ai/config/test/{id}          - 测试模型连接
```

## 六、常见问题

### Q1: 现有配置还能用吗？

**A:** 可以。AiModelConfig 保留了兼容字段，现有配置可以正常使用。

### Q2: 如何创建新的配置？

**A:** 建议使用两级配置流程：
1. 先创建供应商配置（管理连接）
2. 再创建模型配置（管理推理参数）

### Q3: 如何实现会话钉选？

**A:** 使用 `AiConversationController.pinModel()` 接口钉选模型到会话。

### Q4: 协议驱动有什么好处？

**A:** 支持多种AI协议（OpenAI、Ollama、Anthropic等），易于扩展新供应商。

### Q5: 什么时候需要迁移？

**A:** 建议尽快迁移，以便使用新功能（会话钉选、协议驱动等）。

## 七、技术支持

如有迁移问题，请参考：
- [AI模型配置重构方案.md](file:///d:/Project/MyProject/Evolutionary_AI_model/AI模型配置重构方案.md)
- 查看代码注释中的"兼容字段"标记
- 联系技术支持团队