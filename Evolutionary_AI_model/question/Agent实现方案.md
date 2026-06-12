# Agent实现方案

## 1. 项目背景

### 1.1 当前系统状态
- 基础框架：Spring Boot 3.5.14 + Spring AI 1.0.6
- 核心功能：可配置模型的AI问答系统
- 架构模式：工厂模式（ProviderChatModelFactory）+ 策略模式（DynamicChatStrategy）
- 前端技术：Vue 3 + TypeScript + Pinia

### 1.2 Agent目标
将简单的AI问答系统升级为具备工具调用能力的Agent系统，实现：
- **Reasoning（推理）**：AI模型分析用户需求，决定是否需要调用工具
- **Acting（执行）**：自动选择并执行合适的工具
- **Observation（观察）**：根据工具执行结果调整后续行为

## 2. 技术架构设计

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────┐
│                     前端层 (Vue 3)                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  AgentView   │  │  AgentStore  │  │  Agent API   │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                          ↓ HTTP/SSE
┌─────────────────────────────────────────────────────────┐
│                     后端层 (Spring Boot)                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ ChatController│  │ AgentService │  │ ToolRegistry │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│         ↓                  ↓                  ↓         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │DynamicStrategy│  │ChatClient   │  │ Tool Layer   │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│         ↓                  ↓                  ↓         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ModelProvider │  │ Spring AI    │  │CalculatorTool│  │
│  │  Factory     │  │   Core       │  │ WeatherTool  │  │
│  │              │  │              │  │ SearchTool   │  │
│  │              │  │              │  │ TimeTool     │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                   数据层 (MySQL + Redis)                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ agent_task   │  │agent_message │  │  二级缓存    │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
```

### 2.2 核心组件

#### 2.2.1 工具层（Tool Layer）
**职责**：封装具体功能实现，提供标准化接口

**核心接口**：
```java
public interface Tool {
    String getName();                    // 工具名称
    String getDescription();             // 工具描述
    String execute(Map<String, Object> params);  // 执行方法
    String getSchema();                  // JSON Schema定义
}
```

**已实现工具**：
- `CalculatorTool`：计算器工具，支持基本数学运算
- `WeatherTool`：天气查询工具，模拟天气查询
- `SearchTool`：搜索工具，模拟信息检索
- `TimeTool`：时间查询工具，查询当前时间

#### 2.2.2 工具注册中心（ToolRegistry）
**职责**：统一管理所有工具实例

**核心功能**：
- 自动注入所有`Tool`实现类
- 提供工具注册、查询、列表功能
- 支持动态工具扩展

**实现代码**：
```java
@Component
public class ToolRegistry {
    private final Map<String, Tool> toolMap = new HashMap<>();

    public ToolRegistry(List<Tool> tools) {
        for (Tool tool : tools) {
            registerTool(tool);
        }
    }

    public void registerTool(Tool tool) {
        toolMap.put(tool.getName(), tool);
    }

    public Tool getTool(String name) {
        return toolMap.get(name);
    }

    public List<String> getAvailableTools() {
        return new ArrayList<>(toolMap.keySet());
    }
}
```

#### 2.2.3 Agent核心服务（AgentServiceImpl）
**职责**：实现ReAct循环，协调工具调用

**核心流程**：
1. **接收任务**：从前端接收用户任务和工具选择
2. **构建Agent**：创建ChatClient并注册选定的工具
3. **执行ReAct循环**：
   - **Reasoning**：AI分析任务，决定是否调用工具
   - **Acting**：自动调用工具并传递参数
   - **Observation**：观察工具执行结果，继续推理
4. **返回结果**：流式返回最终结果

**关键代码**：
```java
@Override
public Flux<String> executeTask(AgentRequestDTO request) {
    // 1. 获取模型配置
    ModelConfig modelConfig = getValidModelConfig(request.getModelId());

    // 2. 创建ChatModel
    ChatModel chatModel = providerChatModelFactory.createChatModel(modelConfig);

    // 3. 构建ChatClient并注册工具
    ChatClient.Builder builder = ChatClient.builder(chatModel);
    List<Tool> selectedTools = getSelectedTools(request.getTools());
    for (Tool tool : selectedTools) {
        registerToolFunction(builder, tool);
    }
    ChatClient chatClient = builder.build();

    // 4. 构建Agent提示词
    String agentPrompt = buildAgentPrompt(request.getTask(), selectedTools);

    // 5. 流式执行Agent任务
    return chatClient.prompt()
            .user(agentPrompt)
            .stream()
            .content()
            .onErrorResume(error -> {
                logger.error("Agent任务流式执行失败", error);
                return Flux.just("错误: Agent任务执行失败 - " + error.getMessage());
            });
}
```

**工具注册机制**：
```java
private void registerToolFunction(ChatClient.Builder builder, Tool tool) {
    // 定义工具请求类（接收JSON参数）
    record ToolRequest(String input) {}

    // 创建Function<ToolRequest, String>实例
    Function<ToolRequest, String> toolFunction = request -> {
        Map<String, Object> params = new HashMap<>();
        params.put("input", request.input());
        return tool.execute(params);
    };

    // 创建FunctionToolCallback
    FunctionToolCallback<ToolRequest, String> toolCallback =
        FunctionToolCallback.builder(tool.getName(), toolFunction)
            .description(tool.getDescription())
            .inputType(ToolRequest.class)
            .build();

    // 注册到ChatClient.Builder
    builder.defaultToolCallbacks(toolCallback);
}
```

#### 2.2.4 Controller集成（ChatController）
**职责**：提供HTTP接口，处理前后端交互

**新增接口**：
```java
// 流式执行Agent任务（SSE）
@PostMapping(value = "/agent/task", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> executeAgentTask(@AuthenticationPrincipal UserDetails userDetails,
                                     @Valid @RequestBody AgentRequestDTO request) {
    Long userId = getUserId(userDetails);
    request.setUserId(userId);
    return agentService.executeTask(request);
}

// 同步执行Agent任务
@PostMapping("/agent/task/sync")
public Result<AgentResultVO> executeAgentTaskSync(@AuthenticationPrincipal UserDetails userDetails,
                                                   @Valid @RequestBody AgentRequestDTO request) {
    Long userId = getUserId(userDetails);
    request.setUserId(userId);
    return Result.success(agentService.executeTaskSync(request));
}

// 获取可用工具列表
@GetMapping("/agent/tools")
public Result<List<String>> getAvailableTools() {
    List<String> tools = agentService.getAvailableTools();
    return Result.success(tools);
}
```

### 2.3 数据传输对象

#### AgentRequestDTO
```java
public class AgentRequestDTO {
    private Long userId;              // 用户ID
    private String task;              // 任务描述
    private Long modelId;            // 模型ID
    private List<String> tools;       // 选定工具列表
    private Map<String, Object> params;  // 额外参数
}
```

#### AgentResultVO
```java
public class AgentResultVO {
    private String taskId;           // 任务ID
    private String result;           // 执行结果
    private List<String> toolCalls;   // 工具调用记录
    private Long executionTimeMs;    // 执行耗时
}
```

## 3. 工具系统设计

### 3.1 工具接口规范

所有工具必须实现`Tool`接口，并提供：
1. **名称和描述**：供AI模型理解工具用途
2. **执行方法**：接收Map参数，返回String结果
3. **JSON Schema**：定义参数格式，供AI模型生成正确的调用参数

### 3.2 工具实现示例

#### CalculatorTool
```java
@Component
public class CalculatorTool implements Tool {
    @Override
    public String getName() {
        return "calculator";
    }

    @Override
    public String getDescription() {
        return "计算器工具，支持基本数学运算（加、减、乘、除）。输入表达式，返回计算结果。";
    }

    @Override
    public String execute(Map<String, Object> params) {
        // 支持expression和input两种参数名
        String expression = (String) params.get("expression");
        if (expression == null) {
            expression = (String) params.get("input");
        }

        // 解析并计算表达式
        double result = evaluateExpression(expression);
        return String.format("计算结果: %s = %.2f", expression, result);
    }

    @Override
    public String getSchema() {
        return """
            {
              "type": "object",
              "properties": {
                "expression": {
                  "type": "string",
                  "description": "数学表达式，如: 2+3, 10*5, 100/4"
                }
              },
              "required": ["expression"]
            }
            """;
    }
}
```

### 3.3 工具参数兼容性

为确保AI模型能正确调用工具，所有工具实现都支持`input`参数名作为后备：

```java
String param = (String) params.get("originalParamName");
if (param == null) {
    param = (String) params.get("input"); // 支持input参数名
}
```

这样无论AI模型传递`{"expression": "1+2"}`还是`{"input": "1+2"}`，工具都能正确执行。

## 4. 前端集成

### 4.1 Agent页面（AgentView.vue）

**核心功能**：
- 任务输入框
- 工具选择器（多选）
- 模型选择器
- 流式结果展示
- 执行历史记录

**关键代码**：
```vue
<template>
  <div class="agent-container">
    <!-- 任务输入 -->
    <a-textarea
      v-model:value="agentStore.taskInput"
      placeholder="请输入任务描述..."
      :rows="4"
    />

    <!-- 工具选择 -->
    <div class="tools-grid">
      <div
        v-for="tool in agentStore.availableTools"
        :key="tool.name"
        class="tool-card"
        :class="{ selected: agentStore.selectedTools.includes(tool.name) }"
        @click.stop="agentStore.toggleTool(tool.name)"
      >
        <span class="tool-name">{{ tool.name }}</span>
        <p class="tool-description">{{ tool.description }}</p>
      </div>
    </div>

    <!-- 执行按钮 -->
    <a-button type="primary" @click="executeTask" :loading="loading">
      执行任务
    </a-button>

    <!-- 结果展示 -->
    <div class="result-container">
      <pre>{{ agentStore.executionResult }}</pre>
    </div>
  </div>
</template>
```

### 4.2 状态管理（agent.ts）

**Pinia Store**：
```typescript
export const useAgentStore = defineStore('agent', {
  state: () => ({
    taskInput: '',
    availableTools: [] as ToolInfo[],
    selectedTools: [] as string[],
    selectedModelId: null as number | null,
    executionResult: '',
    executionHistory: [] as ExecutionRecord[],
  }),

  actions: {
    async loadAvailableTools() {
      const tools = await getAvailableTools();
      this.availableTools = tools;
    },

    toggleTool(toolName: string) {
      const index = this.selectedTools.indexOf(toolName);
      if (index > -1) {
        this.selectedTools.splice(index, 1);
      } else {
        this.selectedTools.push(toolName);
      }
    },

    async executeTask() {
      const result = await executeAgentTask({
        task: this.taskInput,
        modelId: this.selectedModelId,
        tools: this.selectedTools,
      });
      this.executionResult = result;
    },
  },
});
```

### 4.3 API调用（agent.ts）

**流式请求**：
```typescript
export const executeAgentTask = async (request: AgentRequest): Promise<string> => {
  const token = getToken()
  const response = await fetch(`${API_BASE_URL}/chat/agent/task`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(request),
  })

  const reader = response.body?.getReader()
  const decoder = new TextDecoder()
  let result = ''

  while (reader) {
    const { done, value } = await reader.read()
    if (done) break
    result += decoder.decode(value, { stream: true })
  }

  return result
}
```

## 5. 数据库设计

### 5.1 agent_task表
```sql
CREATE TABLE `agent_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `task_description` text NOT NULL COMMENT '任务描述',
  `model_id` bigint NOT NULL COMMENT '使用的模型ID',
  `selected_tools` json COMMENT '选定的工具列表',
  `status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '任务状态',
  `result` text COMMENT '执行结果',
  `execution_time_ms` bigint COMMENT '执行耗时（毫秒）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent任务表';
```

### 5.2 agent_message表
```sql
CREATE TABLE `agent_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `role` varchar(20) NOT NULL COMMENT '角色（user/assistant/tool）',
  `content` text NOT NULL COMMENT '消息内容',
  `tool_name` varchar(50) COMMENT '工具名称（如果是工具消息）',
  `tool_input` json COMMENT '工具输入参数',
  `tool_output` text COMMENT '工具输出结果',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent消息记录表';
```

## 6. Spring AI 1.0.6关键技术点

### 6.1 Tool Calling API

**正确的工具注册方式**：
```java
// 1. 定义请求类
record ToolRequest(String input) {}

// 2. 创建Function
Function<ToolRequest, String> toolFunction = request -> {
    return tool.execute(Map.of("input", request.input()));
};

// 3. 创建FunctionToolCallback
FunctionToolCallback<ToolRequest, String> toolCallback =
    FunctionToolCallback.builder(toolName, toolFunction)
        .description(toolDescription)
        .inputType(ToolRequest.class)
        .build();

// 4. 注册到ChatClient.Builder
builder.defaultToolCallbacks(toolCallback);
```

**注意事项**：
- 使用`FunctionToolCallback`而不是`@Tool`注解
- 使用`defaultToolCallbacks()`而不是`defaultTools()`
- 定义明确的请求类（record）来接收JSON参数
- AI模型会自动生成JSON格式的参数

### 6.2 流式响应处理

**SSE流式返回**：
```java
@PostMapping(value = "/agent/task", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> executeAgentTask(@RequestBody AgentRequestDTO request) {
    return agentService.executeTask(request);
}
```

**异常处理**：
```java
return chatClient.prompt()
        .user(agentPrompt)
        .stream()
        .content()
        .onErrorResume(error -> {
            logger.error("Agent任务执行失败", error);
            return Flux.just("错误: " + error.getMessage());
        });
```

**重要**：在Flux流内部使用`onErrorResume()`处理异常，避免异常传播到`GlobalExceptionHandler`导致无法转换为SSE格式。

## 7. 使用示例

### 7.1 计算器工具调用

**用户输入**：
```
请帮我计算 123 + 456
```

**AI推理过程**：
1. 分析任务：需要执行数学计算
2. 选择工具：calculator
3. 生成参数：`{"input": "123+456"}`
4. 调用工具：CalculatorTool.execute({"input": "123+456"})
5. 获取结果：`计算结果: 123+456 = 579.00`
6. 返回答案：`123 + 456 的结果是 579`

### 7.2 天气查询工具调用

**用户输入**：
```
西安今天天气怎么样？
```

**AI推理过程**：
1. 分析任务：需要查询天气信息
2. 选择工具：weather
3. 生成参数：`{"input": "西安"}`
4. 调用工具：WeatherTool.execute({"input": "西安"})
5. 获取结果：
```
天气查询结果（模拟）：
城市: 西安
查询时间: 2026-06-09 17:45:15
天气状况: 晴朗
温度: 25°C
湿度: 60%
风速: 3级
```
6. 返回答案：西安今天天气晴朗，温度25°C...

### 7.3 多工具协作

**用户输入**：
```
帮我查询北京天气，并计算100除以5
```

**AI推理过程**：
1. 分析任务：需要查询天气 + 执行计算
2. 选择工具：weather + calculator
3. 依次调用：
   - WeatherTool.execute({"input": "北京"})
   - CalculatorTool.execute({"input": "100/5"})
4. 组合结果并返回

## 8. 后续优化方向

### 8.1 第二阶段：多Agent协作

**目标**：实现多个Agent协作完成复杂任务

**架构升级**：
```
┌─────────────────────────────────────────┐
│          Orchestrator Agent             │
│  (任务分解、协调、结果整合)              │
└─────────────────────────────────────────┘
         ↓                ↓                ↓
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│ Researcher   │  │  Calculator  │  │   Reporter   │
│   Agent      │  │    Agent     │  │    Agent     │
└──────────────┘  └──────────────┘  └──────────────┘
```

**实现要点**：
- 设计Agent间通信协议
- 实现任务队列和调度器
- 添加Agent状态管理
- 实现结果聚合机制

### 8.2 第三阶段：智能规划与执行

**目标**：实现自主规划和反思能力

**核心能力**：
- **任务分解**：将复杂任务拆解为子任务
- **动态规划**：根据执行结果调整计划
- **自我反思**：评估执行效果，优化策略
- **知识积累**：从历史任务中学习

**技术方案**：
- 引入Planning Agent
- 实现ReAct + Reflection循环
- 添加记忆系统（向量数据库）
- 实现经验回放机制

### 8.3 性能优化

**缓存优化**：
- 工具执行结果缓存
- 模型响应缓存
- 常见任务模板缓存

**并发优化**：
- 工具并行执行
- 异步任务队列
- 流式响应优化

### 8.4 安全增强

**权限控制**：
- 工具调用权限管理
- 敏感操作审批机制
- 用户操作审计日志

**输入验证**：
- 任务输入安全检查
- 工具参数验证
- SQL注入防护

## 9. 技术栈总结

### 9.1 后端技术栈
- **框架**：Spring Boot 3.5.14
- **AI集成**：Spring AI 1.0.6
- **ORM**：MyBatis Plus 3.5.9
- **缓存**：Redis + Caffeine（二级缓存）
- **认证**：JWT
- **响应式**：Spring WebFlux

### 9.2 前端技术栈
- **框架**：Vue 3 + TypeScript
- **UI组件**：Ant Design Vue
- **状态管理**：Pinia
- **HTTP客户端**：Axios
- **构建工具**：Vite

### 9.3 核心依赖
```xml
<!-- Spring AI -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
    <version>1.0.6</version>
</dependency>

<!-- Spring WebFlux -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>

<!-- MyBatis Plus -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
    <version>3.5.9</version>
</dependency>
```

## 10. 总结

本方案成功将简单的AI问答系统升级为具备工具调用能力的Agent系统，实现了：

✅ **工具层**：4个基础工具（计算器、天气、搜索、时间）
✅ **工具注册中心**：统一管理工具，支持动态扩展
✅ **Agent核心服务**：实现ReAct循环，自动调用工具
✅ **前端集成**：完整的Agent页面，支持工具选择和流式展示
✅ **数据库设计**：任务表和消息记录表
✅ **Spring AI 1.0.6适配**：正确使用FunctionToolCallback API

**核心价值**：
1. **自动化**：AI模型自动决定何时、如何调用工具
2. **可扩展**：新增工具只需实现Tool接口并注册
3. **流式响应**：实时展示AI推理和工具调用过程
4. **用户友好**：前端界面直观，操作简单

**下一步**：
- 测试各种复杂场景
- 优化工具执行性能
- 实现第二阶段多Agent协作
- 添加更多实用工具