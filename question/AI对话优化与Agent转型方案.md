# AI对话优化与Agent转型方案

## 一、现状分析

### 已具备的基础
- ✅ 基础的 Agent 实现（ReAct 循环，4 个工具：calculator、weather、search、time）
- ✅ 独立的 `AgentView.vue` 和 `ChatView.vue`
- ✅ LangGraph4j 依赖已引入（`langgraph4j-core`、`langgraph4j-spring-ai`、`langgraph4j-studio-springboot`）**但未使用**
- ✅ MCP 客户端依赖已引入（`spring-ai-starter-mcp-client`）**但未使用**
- ✅ 完整的模型配置/供应商配置体系（工厂模式 + 策略模式）
- ✅ 完整的 RAG 知识库检索能力
- ❌ Chat 和 Agent 是两条独立路径，完全割裂

### 核心问题
1. **Chat 和 Agent 割裂**——用户在聊天中用不到工具，切换到 Agent 页面又丢失对话上下文
2. **工具全是模拟的**——weather/search 返回假数据，没有实际价值
3. **没有正式的工作流引擎**——当前 Agent 只是简单 ReAct，没有复杂 DAG 编排能力
4. **缺乏对话记忆管理**——Chat 的历史消息管理能力较弱，Agent 完全没有会话管理
5. **前端体验分离**——用户无法在聊天中"自然触发"工具调用

---

## 二、整体方案

### 核心目标
将 Chat 和 Agent 统一为一条路径，聊天中自然地支持工具调用，并在后台使用 LangGraph4j 进行有状态的工作流编排。

### 架构对比

```
当前架构（割裂）                          目标架构（统一）

┌──────────┐     ┌───────────┐          ┌─────────────────────────────────┐
│ ChatView │────▶│DynamicChat│          │         UnifiedChatView         │
│ (对话)   │     │ Strategy  │          │  (对话 + 工具调用一体化展现)     │
└──────────┘     └───────────┘          └──────────┬────────────────────────┘
                                                    │
┌──────────┐     ┌───────────┐                     ▼
│ AgentView│────▶│AgentService│          ┌─────────────────────────────────┐
│ (工具)   │     │(ReAct循环) │          │     AgentChatStrategy           │
└──────────┘     └───────────┘          │(智能路由: 普通对话→Agent→工具)   │
                                         └──────────┬────────────────────────┘
                                                     │
                                        ┌────────────┼────────────┐
                                        ▼            ▼            ▼
                                  ┌─────────┐ ┌─────────┐ ┌─────────┐
                                  │ 普通对话 │ │ 调用工具 │ │ 复杂任务│
                                  │(无工具)  │ │(ReAct)  │ │(LangGraph│
                                  └─────────┘ └─────────┘ └─────────┘
```

---

## 三、阶段一：AI对话优化

### 3.1 消息窗口管理
- **现状**：每次请求只传 `history`，无截断策略
- **方案**：在 `DynamicChatStrategy` 中增加滑动窗口策略，保留最近 N 轮对话
- **涉及文件**：`DynamicChatStrategy.java`、`ChatRequestDTO.java`

### 3.2 系统提示词增强
- **现状**：`AiRole` 支持简单的系统提示词模板
- **方案**：扩展系统提示词构建逻辑，支持动态变量注入（用户名称、当前时间、知识库上下文等）
- **涉及文件**：`AiRoleService.java`、`DynamicChatStrategy.java`

### 3.3 流式响应稳定性
- **现状**：SSE 流在大量 token 下可能不稳
- **方案**：增加心跳机制（每 N 秒发送 keepalive），超时重试
- **涉及文件**：`ChatController.java`

### 3.4 Token 使用优化
- **现状**：`ChatRequestDTO` 中没有 `maxTokens` 字段
- **方案**：在 DTO 中增加 `maxTokens` 字段，透传到 `ChatClient`
- **涉及文件**：`ChatRequestDTO.java`、`DynamicChatStrategy.java`

### 3.5 对话标题智能生成
- **现状**：使用用户第一条消息的前 50 字符作为标题
- **方案**：AI 异步生成对话标题（调用轻量模型总结）
- **涉及文件**：`AiConversationService.java`

---

## 四、阶段二：Chat → Agent 统一（核心转型）

### 4.1 新增 AgentChatStrategy（统一入口）

在 `DynamicChatStrategy` 基础上扩展，增加**智能路由**能力：

```
ChatRequestDTO
    │
    ▼
AgentChatStrategy.streamChat()
    │
    ├─ 判断是否需要工具调用能力
    │   ├─ 用户消息包含"计算/查询/搜索/天气"等关键词 → 启用 Agent 模式
    │   └─ 普通对话 → 走原有 Chat 路径
    │
    ├─ Agent 模式
    │   ├─ 携带历史消息上下文
    │   ├─ 注册可用工具到 ChatClient
    │   ├─ 流式返回（思考过程 + 工具调用 + 最终答案）
    │   └─ 保存完整的 Agent 执行日志
    │
    └─ 普通模式
        └─ 保持现有 DynamicChatStrategy 逻辑
```

**涉及文件**：
- 新建 `AgentChatStrategy.java`（继承/组合 `DynamicChatStrategy`）
- 修改 `ChatController.java`（统一入口）
- 修改 `ChatServiceImpl.java`（委托到新策略）
- 修改 `ChatRequestDTO.java`（增加 `agentMode`、`enabledTools` 字段）

### 4.2 引入 LangGraph4j 替代手写 ReAct

LangGraph4j 依赖已存在（`pom.xml` 中已有），用于构建**有状态、可编排**的 Agent 工作流：

```java
// LangGraph4j Agent 工作流示例
@Bean
public Graph<AgentState> buildAgentGraph() {
    Graph<AgentState> graph = new Graph<>();
    
    // 节点：思考节点（调用LLM决定下一步）
    graph.addNode("reason", agentNode.callLlm());
    
    // 节点：工具执行节点
    graph.addNode("act", agentNode.executeTool());
    
    // 条件边：LLM决定是继续还是结束
    graph.addConditionalEdge(
        "reason",
        state -> state.hasToolCall() ? "act" : "end"
    );
    
    // 循环边：工具执行完回到思考
    graph.addEdge("act", "reason");
    
    return graph.compile();
}
```

**与手写 ReAct 对比：**

| 对比项 | 当前 ReAct（手写） | LangGraph4j |
|--------|-------------------|-------------|
| 状态管理 | 无 | 有状态图 `AgentState` |
| 循环控制 | 靠 `maxSteps` 参数 | 条件边 + 循环边 |
| 并行工具 | 不支持 | 支持 |
| 可视化 | 无 | LangGraph4j Studio |
| 持久化 | 无 | PostgreSQL saver |
| 错误恢复 | 弱 | 支持重试/回退 |

**涉及文件**：
- 新建 `langgraph4j/` 包（`node/`, `state/`, `graph/`）
- 修改 `AgentServiceImpl.java`（接入 LangGraph4j 工作流）
- 配置 `LangGraph4j Studio`（可视化调试）

### 4.3 真实工具替换和新增

```
现有模拟工具 → 替换/增强为真实工具

calculator  → 保留（已是真实数学运算）
weather     → 替换为「和风天气 / OpenWeatherMap API」
search      → 替换为「SerpAPI / 百度搜索 API」
time        → 保留（已是真实时间）

新增真实工具：
├─ database_query    → 自然语言查数据库（NL2SQL）
├─ knowledge_retrieve → 知识库 RAG 检索（复用现有 RagService）
├─ code_executor     → Python / JS 代码沙箱执行
├─ file_reader       → 读取文件内容（复用现有文档解析能力）
├─ web_fetch         → 抓取网页内容
└─ mcp_tools         → 通过 MCP 协议接入外部工具
```

**涉及文件**：
- 新建 `DatabaseQueryTool.java`、`CodeExecutorTool.java`、`WebFetchTool.java`
- 修改 `WeatherTool.java`、`SearchTool.java`（接入真实 API）
- 修改 `Tool.java` 接口（增强 Schema 定义，支持复杂参数）

### 4.4 Agent 执行日志与消息持久化

- **现状**：Agent 执行结果不保存到数据库，无历史追溯
- **方案**：
  - 扩展 `AiConversationMessage` 表，增加 `tool_calls` JSON 字段
  - Agent 执行的每一步（思考、行动、观察）都作为消息保存
  - 前端可回溯查看完整的 Agent 推理链

**涉及文件**：
- 修改 `AiConversationMessage.java`（增加 toolCalls 字段）
- 新建数据库迁移脚本
- 修改 `AgentServiceImpl.java`（保存执行日志）

---

## 五、阶段三：MCP 协议接入（外部工具生态）

利用已引入的 `spring-ai-starter-mcp-client`：

```
┌────────────────────────────────────────┐
│     AgentChatStrategy                  │
│         │                              │
│         ▼                              │
│  ┌──────────────────┐                  │
│  │   MCP Client     │                  │
│  └────────┬─────────┘                  │
│           │                            │
└───────────┼────────────────────────────┘
            │
     ┌──────┴──────┐
     │  MCP Server  │ ─── 文件系统操作
     │  (Stdio/SSE) │ ─── 数据库查询
     └──────┬──────┘ ─── 外部 API 调用
            │
      ┌─────┴─────┐
      │  External   │
      │  Services   │
      └───────────┘
```

**涉及文件**：
- 新建 `MCPClientConfig.java`（MCP 客户端配置）
- 新建 `MCPTool.java`（MCP 工具适配器）
- 修改 `application.yml`（MCP Server 地址配置）

---

## 六、阶段四：前端统一

### 6.1 ChatView 增强

在现有 `ChatView.vue` 中**内嵌 Agent 能力**，不再走独立 Agent 页面：

```
ChatView.vue（统一视图）
├── 左侧：会话列表（现有）
├── 中间：消息区域（现有）
│   ├── 普通消息气泡（现有）
│   └── 工具调用气泡（新增）
│       ├── 思考过程折叠展示
│       ├── 工具调用卡片（名称、参数、结果）
│       └── 执行步骤时间线
└── 底部：输入区域（现有）
    └── 工具选择开关（新增，可启用/禁用 Agent 模式）
```

### 6.2 新增组件

| 组件 | 说明 |
|------|------|
| `ToolCallBubble.vue` | 工具调用气泡，展示思考→行动→观察的完整过程 |
| `ToolSelector.vue` | 工具选择器，可切换启用哪些工具 |
| `AgentStatusBar.vue` | Agent 执行状态指示器（思考中/执行中/完成） |

### 6.3 conversation store 扩展

```typescript
// conversation store 扩展 Agent 相关状态
interface Conversation {
  // ...现有字段
  agentMode?: boolean          // 是否启用 Agent 模式
  enabledTools?: string[]      // 启用的工具列表
  agentSteps?: AgentStep[]     // Agent 执行步骤记录
}
```

---

## 七、实施路线图

```
阶段一：AI对话优化（预估 3-5 天）
├── 1.1 消息窗口管理策略
├── 1.2 系统提示词增强
├── 1.3 流式响应稳定性
├── 1.4 Token 优化
└── 1.5 标题智能生成

阶段二：Chat→Agent 统一（预估 5-7 天）
├── 2.1 AgentChatStrategy 统一入口
├── 2.2 LangGraph4j 工作流集成
├── 2.3 真实工具替换和新增
└── 2.4 Agent 执行日志与消息持久化

阶段三：MCP 协议接入（预估 2-3 天）
├── 3.1 MCP Client 配置与集成
└── 3.2 标准 MCP 工具实现

阶段四：前端统一（预估 3-5 天）
├── 4.1 ChatView 增强（工具调用气泡）
├── 4.2 ToolSelector 组件
├── 4.3 AgentStatusBar 组件
└── 4.4 store 扩展与 API 适配
```

### 推荐实施顺序

1. **阶段二先行**（核心转型）——先做 `AgentChatStrategy` 统一入口 + LangGraph4j 工作流
2. **阶段四并行**（前端统一）——与后端同步开发
3. **阶段一穿插**（对话优化）——在统一架构上做优化
4. **阶段三收尾**（MCP 接入）——最后扩展工具生态

---

## 八、涉及的文件清单

### 后端新增/修改文件

| 类别 | 文件路径 | 操作 |
|------|----------|------|
| 策略 | `service/strategy/AgentChatStrategy.java` | 新增 |
| 策略 | `service/strategy/DynamicChatStrategy.java` | 修改 |
| 服务 | `service/agent/impl/AgentServiceImpl.java` | 修改 |
| 服务 | `service/impl/ChatServiceImpl.java` | 修改 |
| 控制器 | `controller/ChatController.java` | 修改 |
| DTO | `entity/dto/ChatRequestDTO.java` | 修改 |
| 实体 | `entity/AiConversationMessage.java` | 修改 |
| 工具 | `service/agent/tool/WeatherTool.java` | 修改 |
| 工具 | `service/agent/tool/SearchTool.java` | 修改 |
| 工具 | `service/agent/tool/DatabaseQueryTool.java` | 新增 |
| 工具 | `service/agent/tool/CodeExecutorTool.java` | 新增 |
| 工具 | `service/agent/tool/WebFetchTool.java` | 新增 |
| 工具 | `service/agent/tool/Tool.java` | 修改 |
| LangGraph | `service/agent/langgraph4j/state/AgentState.java` | 新增 |
| LangGraph | `service/agent/langgraph4j/node/ReasonNode.java` | 新增 |
| LangGraph | `service/agent/langgraph4j/node/ActionNode.java` | 新增 |
| LangGraph | `service/agent/langgraph4j/graph/AgentGraph.java` | 新增 |
| MCP | `config/MCPClientConfig.java` | 新增 |
| MCP | `service/agent/tool/MCPTool.java` | 新增 |

### 前端新增/修改文件

| 类别 | 文件路径 | 操作 |
|------|----------|------|
| 视图 | `views/ChatView.vue` | 修改 |
| 视图 | `views/AgentView.vue` | 废弃或合并 |
| 组件 | `components/chat/ToolCallBubble.vue` | 新增 |
| 组件 | `components/chat/ToolSelector.vue` | 新增 |
| 组件 | `components/chat/AgentStatusBar.vue` | 新增 |
| Store | `stores/conversation.ts` | 修改 |
| Store | `stores/agent.ts` | 合并到 conversation |
| API | `utils/agent.ts` | 修改 |
| 类型 | `types/agent.ts` | 修改 |
| 类型 | `types/conversation.ts` | 修改 |
