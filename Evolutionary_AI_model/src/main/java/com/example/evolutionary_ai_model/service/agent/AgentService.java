package com.example.evolutionary_ai_model.service.agent;

import com.example.evolutionary_ai_model.entity.dto.AgentRequestDTO;
import com.example.evolutionary_ai_model.entity.vo.AgentResultVO;
import reactor.core.publisher.Flux;

/**
 * 用法：Agent服务接口，定义Agent任务执行的核心业务操作。
 * 位于业务逻辑层，负责协调工具注册、模型调用和任务执行。
 * 支持流式执行和同步执行两种模式，实现ReAct循环（思考→行动→观察）。
 */
public interface AgentService {

    /**
     * 流式执行Agent任务，实时返回执行过程和结果
     * @param request Agent任务请求
     * @return 流式响应内容（Flux<String>），包含思考过程、工具调用和最终答案
     */
    Flux<String> executeTask(AgentRequestDTO request);

    /**
     * 同步执行Agent任务，返回完整的执行结果
     * @param request Agent任务请求
     * @return Agent任务结果VO，包含最终答案和工具执行日志
     */
    AgentResultVO executeTaskSync(AgentRequestDTO request);

    /**
     * 获取可用的工具列表
     * @return 工具名称列表
     */
    java.util.List<String> getAvailableTools();
}