# EvoAI身份统一标识实现方案

## 一、需求概述

实现目标：
1. 不管选择什么AI大模型，都统一回复为"EvoAI"
2. 切换模型不影响上下文，不会导致上下文中断

核心问题：
- 当前系统缺少统一的系统提示词（system prompt）
- AI基于历史消息的上下文判断自己是谁，而不是基于实际配置
- 切换模型后，历史消息中包含前一个模型的痕迹，影响AI的自我认知

## 二、当前状态分析

### 2.1 关键代码现状

**AiConversation实体类** ([AiConversation.java](file:///d:/Project/MyProject/Evolutionary_AI_model/Evolutionary_AI_model/src/main/java/com/example/evolutionary_ai_model/entity/AiConversation.java#L48)):
- 已有 `systemPrompt` 字段，但当前未被使用
- 数据库表中已存在该字段，但默认为null

**DynamicChatStrategy提示词构建** ([DynamicChatStrategy.java](file:///d:/Project/MyProject/Evolutionary_AI_model/Evolutionary_AI_model/src/main/java/com/example/evolutionary_ai_model/service/strategy/DynamicChatStrategy.java#L200-L216)):
- `buildPrompt` 方法只构建用户消息和历史消息
- 没有系统提示词的构建逻辑
- 提示词格式：历史消息 + 当前用户消息

**ChatClient创建和使用** ([ProviderChatModelFactory.java](file:///d:/Project/MyProject/Evolutionary_AI_model/Evolutionary_AI_model/src/main/java/com/example/evolutionary_ai_model/service/factory/ProviderChatModelFactory.java#L92)):
- ChatClient创建时没有设置系统提示词：`ChatClient.builder(chatModel).build()`
- ChatClient使用时没有调用 `.system()` 方法：`chatClient.prompt().user(prompt)`
- 缺少统一的身份标识机制

**模型选择优先级** ([DynamicChatStrategy.java](file:///d:/Project/MyProject/Evolutionary_AI_model/Evolutionary_AI_model/src/main/java/com/example/evolutionary_ai_model/service/strategy/DynamicChatStrategy.java#L224-L253)):
- 三级优先级机制确保每次选择生效（优先级1：指定configId）
- 功能层面正常，但认知层面缺少身份标识

### 2.2 问题根源

1. **缺少系统提示词**：Spring AI的ChatClient需要通过 `.system()` 或默认系统提示词来设置AI身份
2. **历史消息影响**：AI基于历史消息的上下文、风格、语气判断自己是谁
3. **模型切换痕迹**：不同模型有不同的回复风格，切换后历史消息包含前一个模型的特征

## 三、实现方案设计

### 3.1 核心思路

采用**统一的系统提示词机制**，在每次对话开始时明确告知AI身份为"EvoAI"，确保：
- AI的身份认知来自系统提示词，而不是历史消息
- 切换模型不影响AI的自我认知
- 保持历史消息完整性，确保上下文不中断

### 3.2 技术实现路径

**方案选择**：在ChatClient使用时动态设置系统提示词（推荐方案）

优点：
- 灵活性高，可以根据不同场景设置不同的系统提示词
- 不需要修改ChatClient的创建逻辑
- 易于维护和扩展

实现位置：
- 在 `DynamicChatStrategy.streamChat()` 方法中，调用ChatClient时设置系统提示词

## 四、详细实现步骤

### 步骤1：定义统一的系统提示词内容

**创建配置文件**：`application.yml` 中添加系统提示词配置

文件位置：[application.yml](file:///d:/Project/MyProject/Evolutionary_AI_model/Evolutionary_AI_model/src/main/resources/application.yml)

新增配置项：
```yaml
ai:
  system:
    prompt:
      default: "你的名字是EvoAI，是由Evolutionary AI团队开发的智能助手。无论用户询问你的身份或你是什么模型，你都应该统一回复你是EvoAI。你的任务是帮助用户解决问题，提供准确、友好的回答。"
```

### 步骤2：创建系统提示词配置类

**新建配置类**：`AiSystemPromptProperties.java`

文件路径：`src/main/java/com/example/evolutionary_ai_model/config/AiSystemPromptProperties.java`

主要功能：
- 从application.yml读取系统提示词配置
- 提供默认的系统提示词内容
- 支持后续扩展（如不同场景的系统提示词）

关键代码结构：
```java
@ConfigurationProperties(prefix = "ai.system.prompt")
@Component
public class AiSystemPromptProperties {
    private String default; // 默认系统提示词
    // getter/setter
}
```

### 步骤3：修改DynamicChatStrategy注入系统提示词

**修改文件**：[DynamicChatStrategy.java](file:///d:/Project/MyProject/Evolutionary_AI_model/Evolutionary_AI_model/src/main/java/com/example/evolutionary_ai_model/service/strategy/DynamicChatStrategy.java)

修改内容：
1. 注入 `AiSystemPromptProperties` 配置类
2. 在 `streamChat()` 方法中，调用ChatClient时设置系统提示词
3. 调用 `.system()` 方法设置统一的身份标识

具体修改位置：
- 第161-163行：在 `chatClient.prompt().user(prompt)` 之前添加 `.system()` 调用

修改后的代码：
```java
Flux<String> chatFlux = chatClient.prompt()
        .system(systemPromptProperties.getDefault()) // 设置系统提示词
        .user(prompt)
        .stream()
        .content()
        // ... 其他代码不变
```

### 步骤4：验证实现效果

**测试场景**：
1. 第一次选择模型A，询问"你是谁？" → 应回复"EvoAI"
2. 第二次选择模型B，询问"你是谁？" → 应回复"EvoAI"
3. 多轮对话，切换模型后，历史消息完整保留 → 上下文不中断

**验证方法**：
- 通过日志查看系统提示词是否正确传递
- 通过前端测试验证AI回复是否统一为"EvoAI"
- 通过数据库验证历史消息是否完整保存

## 五、关键决策与假设

### 5.1 技术决策

**决策1：系统提示词设置时机**
- 选择：在ChatClient使用时动态设置（`.system()` 方法）
- 原因：灵活性高，易于维护，不修改ChatClient创建逻辑
- 替代方案：在创建ChatClient时设置默认系统提示词（不够灵活）

**决策2：系统提示词配置方式**
- 选择：在application.yml中配置
- 原因：便于修改，支持不同环境配置，易于管理
- 替代方案：硬编码在代码中（不易维护）

**决策3：系统提示词内容**
- 选择：明确的身份标识 + 任务说明
- 内容："你的名字是EvoAI，是由Evolutionary AI团队开发的智能助手..."
- 原因：确保AI不会基于历史消息判断自己是谁

### 5.2 假设与限制

**假设**：
1. Spring AI的ChatClient支持 `.system()` 方法设置系统提示词（基于Spring AI官方文档）
2. 系统提示词优先级高于历史消息上下文（基于AI模型的通用原理）
3. 所有AI模型都支持系统提示词（主流模型都支持）

**限制**：
1. 系统提示词长度有限，需要简洁明了
2. 某些特殊模型可能不支持系统提示词（需要异常处理）
3. 系统提示词不能完全消除历史消息的影响（但可以主导AI的认知）

## 六、影响范围分析

### 6.1 文件修改清单

| 文件路径 | 修改类型 | 修改内容 |
|---------|---------|---------|
| `application.yml` | 新增配置 | 添加 `ai.system.prompt.default` 配置项 |
| `AiSystemPromptProperties.java` | 新建文件 | 系统提示词配置类 |
| `DynamicChatStrategy.java` | 修改代码 | 注入配置类，调用 `.system()` 方法 |

### 6.2 数据库影响

- 无需修改数据库结构（AiConversation表已有systemPrompt字段）
- 不影响现有数据（历史消息完整保留）

### 6.3 功能影响

**正面影响**：
- 统一AI身份认知，提升用户体验
- 切换模型不影响上下文，功能更稳定
- 系统提示词机制为后续功能扩展提供基础（如不同场景的系统提示词）

**潜在风险**：
- 系统提示词可能与某些模型的特性冲突（需要测试验证）
- 用户可能希望知道实际使用的模型（需要权衡透明度和统一性）

## 七、验证与测试计划

### 7.1 功能测试

**测试用例1：首次对话身份验证**
- 操作：选择模型A，发送"你是谁？"
- 预期：AI回复"EvoAI"
- 验证：查看日志中的系统提示词是否正确传递

**测试用例2：切换模型后身份验证**
- 操作：选择模型B，发送"你是谁？"
- 预期：AI回复"EvoAI"（而不是模型A）
- 验证：历史消息完整保留，上下文不中断

**测试用例3：多轮对话上下文验证**
- 操作：选择模型A，对话3轮，切换到模型B，继续对话
- 预期：历史消息完整，AI身份统一为"EvoAI"，上下文连贯
- 验证：数据库中的消息记录完整，AI回复连贯

### 7.2 日志验证

关键日志点：
- 系统提示词注入日志：`logger.info("系统提示词: {}", systemPromptProperties.getDefault())`
- ChatClient调用日志：`logger.info("设置系统提示词后调用ChatClient")`

## 八、实施时间估算

- 配置文件修改：5分钟
- 新建配置类：15分钟
- DynamicChatStrategy修改：20分钟
- 测试验证：30分钟
- 总计：约1.5小时

## 九、后续优化建议

1. **多场景系统提示词**：支持不同场景（如客服、技术支持）的系统提示词配置
2. **用户自定义系统提示词**：允许用户在创建会话时设置自定义系统提示词
3. **系统提示词管理界面**：提供管理界面配置和修改系统提示词
4. **模型身份透明化**：在UI中显示实际使用的模型信息，但AI回复统一为"EvoAI"

---

**方案状态**：已完成设计，待用户确认后执行实施