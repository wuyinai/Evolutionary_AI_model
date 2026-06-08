---
alwaysApply: false
description: 当涉及到SpringAI内容的开发时
---
聚焦于最新版本的API和抽象，帮助Agent避免使用已淘汰的方法。

核心开发规范速查表
规范类别	✅ 推荐做法（1.1.3+）	❌ 避免/淘汰做法	关键说明
核心客户端	使用 ChatClient 流畅API	直接使用底层 ChatModel 进行复杂对话	ChatClient 提供类似 WebClient 的构建器模式，是推荐的统一入口
函数调用	通过 .function() 注册 @Bean 或 Function	在提示词中手工编写JSON Schema	框架自动处理函数声明与调用循环
输出解析	.entity() 映射到 POJO	手动解析 String 响应	保证结构化输出，类型安全
上下文管理	使用 顾问（Advisor） API	手动拼接对话历史	顾问封装了提示增强、历史记录等重复性模式
模型切换	依赖 spring-ai-starter-* 与自动配置	硬编码模型类（如 OpenAiChatModel）	实现跨提供商便携性
流式响应	使用 ChatClient 的 .stream()	阻塞等待完整响应	提升交互响应速度
向量存储	使用统一 VectorStore API及元数据过滤	直接使用各存储专有客户端	保持可移植的ETL管道

开发过程中不可使用已经淘汰的API或方法。请参考最新版本的文档和示例代码。
https://www.spring-doc.cn/spring-ai/1.1.3/index.html


开发过程中，Agent **必须主动识别并应用合适的设计模式**，以达成：
- **解耦模块**：减少模块间直接依赖
- **封装变化**：将容易变化的行为与稳定结构分离
- **职责单一**：每个类仅承担一类职责