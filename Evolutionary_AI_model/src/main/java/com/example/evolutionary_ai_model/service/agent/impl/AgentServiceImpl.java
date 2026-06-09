package com.example.evolutionary_ai_model.service.agent.impl;

import cn.hutool.core.util.IdUtil;
import com.example.evolutionary_ai_model.entity.AiModelConfig;
import com.example.evolutionary_ai_model.entity.AiProviderConfig;
import com.example.evolutionary_ai_model.entity.dto.AgentRequestDTO;
import com.example.evolutionary_ai_model.entity.enums.ModelProtocol;
import com.example.evolutionary_ai_model.entity.vo.AgentResultVO;
import com.example.evolutionary_ai_model.service.AiModelConfigService;
import com.example.evolutionary_ai_model.service.AiProviderConfigService;
import com.example.evolutionary_ai_model.service.agent.AgentService;
import com.example.evolutionary_ai_model.service.agent.tool.Tool;
import com.example.evolutionary_ai_model.service.agent.tool.ToolRegistry;
import com.example.evolutionary_ai_model.service.factory.ChatModelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * 用法：Agent服务实现类，负责处理Agent任务的核心业务逻辑。
 * 使用ToolRegistry管理工具，使用ChatModelBuilder创建ChatModel。
 * 位于业务逻辑层，实现ReAct循环（思考→行动→观察），支持流式和同步执行。
 * 利用Spring AI的ChatClient.function()能力实现工具自动调用。
 */
@Service
public class AgentServiceImpl implements AgentService {

    private static final Logger logger = LoggerFactory.getLogger(AgentServiceImpl.class);

    // 工具注册中心
    @Autowired
    private ToolRegistry toolRegistry;

    // 模型配置服务
    @Autowired
    private AiModelConfigService modelConfigService;

    // 供应商配置服务
    @Autowired
    private AiProviderConfigService providerConfigService;

    // ChatModel构建器列表（所有协议的构建器）
    @Autowired
    private List<ChatModelBuilder> chatModelBuilders;

    // ChatModel构建器注册表（协议 -> 构建器）
    private final Map<ModelProtocol, ChatModelBuilder> builderMap = new HashMap<>();

    /**
     * 初始化方法，注册所有ChatModel构建器
     */
    @Autowired
    public void initBuilders() {
        for (ChatModelBuilder builder : chatModelBuilders) {
            ModelProtocol protocol = builder.getSupportedProtocol();
            builderMap.put(protocol, builder);
            logger.info("注册Agent ChatModel构建器: {}", protocol.getName());
        }
    }

    @Override
    public Flux<String> executeTask(AgentRequestDTO request) {
        logger.info("开始流式执行Agent任务，任务描述: {}", request.getTask());

        try {
            // 获取模型配置和供应商配置
            AiModelConfig modelConfig = getModelConfig(request);
            AiProviderConfig providerConfig = getProviderConfig(modelConfig);

            // 创建ChatClient
            ChatClient chatClient = createAgentChatClient(providerConfig, modelConfig, request);

            // 构建Agent提示词
            String agentPrompt = buildAgentPrompt(request);

            // 记录开始时间
            LocalDateTime startTime = LocalDateTime.now();
            long startMs = System.currentTimeMillis();

            // 使用AtomicReference收集响应内容
            AtomicReference<StringBuilder> responseBuilder = new AtomicReference<>(new StringBuilder());

            // 流式执行Agent任务
            return chatClient.prompt()
                    .user(agentPrompt)
                    .stream()
                    .content()
                    // 收集每个响应片段
                    .doOnNext(chunk -> {
                        responseBuilder.get().append(chunk);
                        logger.debug("收到Agent响应片段，长度: {}", chunk.length());
                    })
                    // 流式响应完成后，记录日志
                    .doOnComplete(() -> {
                        String responseContent = responseBuilder.get().toString();
                        long endMs = System.currentTimeMillis();
                        long totalTimeMs = endMs - startMs;

                        logger.info("Agent任务流式执行完成，总长度: {}, 耗时: {}ms", 
                                responseContent.length(), totalTimeMs);
                    })
                    // 流式响应出错时，返回错误消息流（避免传播到GlobalExceptionHandler）
                    .onErrorResume(error -> {
                        logger.error("Agent任务流式执行失败", error);
                        return Flux.just("错误: Agent任务执行失败 - " + error.getMessage());
                    });

        } catch (Exception e) {
            logger.error("Agent任务流式执行异常", e);
            // 返回错误消息流，而不是Flux.error()，避免传播到GlobalExceptionHandler
            return Flux.just("错误: Agent任务执行失败 - " + e.getMessage());
        }
    }

    @Override
    public AgentResultVO executeTaskSync(AgentRequestDTO request) {
        logger.info("开始同步执行Agent任务，任务描述: {}", request.getTask());

        // 创建结果对象
        AgentResultVO result = new AgentResultVO();
        result.setTaskId(IdUtil.fastSimpleUUID());
        result.setStartTime(LocalDateTime.now());
        result.setToolLogs(new ArrayList<>());

        long startMs = System.currentTimeMillis();

        try {
            // 获取模型配置和供应商配置
            AiModelConfig modelConfig = getModelConfig(request);
            AiProviderConfig providerConfig = getProviderConfig(modelConfig);

            // 创建ChatClient
            ChatClient chatClient = createAgentChatClient(providerConfig, modelConfig, request);

            // 构建Agent提示词
            String agentPrompt = buildAgentPrompt(request);

            // 执行Agent任务（同步）
            String response = chatClient.prompt()
                    .user(agentPrompt)
                    .call()
                    .content();

            // 设置结果
            result.setStatus("SUCCESS");
            result.setFinalAnswer(response);
            result.setTotalSteps(1); // Spring AI自动处理工具调用循环

            long endMs = System.currentTimeMillis();
            result.setTotalTimeMs(endMs - startMs);
            result.setEndTime(LocalDateTime.now());

            logger.info("Agent任务同步执行完成，耗时: {}ms", result.getTotalTimeMs());

            return result;

        } catch (Exception e) {
            logger.error("Agent任务同步执行失败", e);

            // 设置错误结果
            result.setStatus("FAILED");
            result.setErrorMessage("Agent任务执行失败: " + e.getMessage());

            long endMs = System.currentTimeMillis();
            result.setTotalTimeMs(endMs - startMs);
            result.setEndTime(LocalDateTime.now());

            return result;
        }
    }

    @Override
    public List<String> getAvailableTools() {
        return toolRegistry.getAllToolNames();
    }

    /**
     * 获取模型配置
     * @param request Agent任务请求
     * @return 模型配置实体
     */
    private AiModelConfig getModelConfig(AgentRequestDTO request) {
        // 如果指定了configId，使用指定的配置
        if (request.getConfigId() != null) {
            logger.info("使用指定模型配置，配置ID: {}", request.getConfigId());
            return modelConfigService.getConfigById(request.getConfigId());
        }

        // 如果有userId，获取用户的默认模型配置
        if (request.getUserId() != null) {
            logger.info("获取用户默认模型配置，用户ID: {}", request.getUserId());
            AiModelConfig defaultConfig = modelConfigService.getDefaultConfig(request.getUserId());
            if (defaultConfig != null) {
                logger.info("找到用户默认模型配置，配置ID: {}", defaultConfig.getId());
                return defaultConfig;
            }
        }

        // 既没有configId也没有userId，无法获取模型配置
        logger.warn("未指定模型配置ID且无法获取用户默认模型");
        throw new RuntimeException("未找到可用的模型配置，请添加模型配置或指定configId");
    }

    /**
     * 根据模型配置获取关联的供应商配置
     * @param modelConfig 模型配置
     * @return 供应商配置实体
     */
    private AiProviderConfig getProviderConfig(AiModelConfig modelConfig) {
        if (modelConfig.getProviderConfigId() == null) {
            logger.warn("模型配置缺少供应商配置关联，配置ID: {}", modelConfig.getId());
            throw new RuntimeException("模型配置未关联供应商配置，请先创建供应商配置");
        }

        logger.info("通过providerConfigId获取供应商配置，配置ID: {}", modelConfig.getProviderConfigId());
        AiProviderConfig providerConfig = providerConfigService.getConfigById(modelConfig.getProviderConfigId());

        if (providerConfig == null) {
            logger.warn("供应商配置不存在，配置ID: {}", modelConfig.getProviderConfigId());
            throw new RuntimeException("供应商配置不存在");
        }

        return providerConfig;
    }

    /**
     * 创建Agent专用的ChatClient（注册工具函数）
     * @param providerConfig 供应商配置
     * @param modelConfig 模型配置
     * @param request Agent任务请求
     * @return ChatClient实例
     */
    private ChatClient createAgentChatClient(AiProviderConfig providerConfig, 
            AiModelConfig modelConfig, AgentRequestDTO request) {
        logger.info("创建Agent ChatClient，模型配置ID: {}", modelConfig.getId());

        // 使用ChatModelBuilder创建ChatModel
        ChatModel chatModel = createChatModel(providerConfig, modelConfig);

        // 获取可用工具列表
        List<Tool> availableTools = getAvailableTools(request);

        // 创建ChatClient.Builder并注册工具函数
        ChatClient.Builder builder = ChatClient.builder(chatModel);

        // 注册工具函数（Spring AI 1.0.6的方式）
        for (Tool tool : availableTools) {
            registerToolFunction(builder, tool);
            logger.info("注册工具函数: {}", tool.getName());
        }

        // 构建ChatClient
        return builder.build();
    }

    /**
     * 创建ChatModel实例
     * @param providerConfig 供应商配置
     * @param modelConfig 模型配置
     * @return ChatModel实例
     */
    private ChatModel createChatModel(AiProviderConfig providerConfig, AiModelConfig modelConfig) {
        // 获取协议类型
        ModelProtocol protocol = ModelProtocol.fromCode(providerConfig.getProtocolType());
        if (protocol == null) {
            logger.warn("未知的协议类型: {}, 使用默认OpenAI协议", providerConfig.getProtocolType());
            protocol = ModelProtocol.OPENAI;
        }

        // 获取对应的构建器
        ChatModelBuilder builder = builderMap.get(protocol);
        if (builder == null) {
            logger.warn("未找到协议构建器: {}, 使用默认OpenAI构建器", protocol.getName());
            builder = builderMap.get(ModelProtocol.OPENAI);
            if (builder == null) {
                throw new RuntimeException("未找到可用的协议构建器");
            }
        }

        // 构建ChatModel
        return builder.build(
                providerConfig.getApiKey(), // API密钥（已解密）
                providerConfig.getApiEndpoint(), // API端点
                modelConfig.getModelName(), // 模型名称
                modelConfig.getTemperature() != null ? modelConfig.getTemperature().doubleValue() : 0.7,
                modelConfig.getMaxTokens(),
                providerConfig.getExtraConfig()
        );
    }

    /**
     * 注册工具函数到ChatClient.Builder
     * @param builder ChatClient.Builder
     * @param tool 工具实例
     */
    private void registerToolFunction(ChatClient.Builder builder, Tool tool) {
        // 使用Spring AI 1.0.6的正确API：FunctionToolCallback
        // 定义工具请求类（接收JSON参数）
        record ToolRequest(String input) {}

        // 创建Function<ToolRequest, String>实例
        Function<ToolRequest, String> toolFunction = request -> {
            logger.info("执行工具: {}, 输入: {}", tool.getName(), request.input());
            // 将输入转换为Map格式
            Map<String, Object> params = new HashMap<>();
            params.put("input", request.input());
            return tool.execute(params);
        };

        // 创建FunctionToolCallback（使用ToolRequest作为inputType）
        FunctionToolCallback<ToolRequest, String> toolCallback = FunctionToolCallback.builder(tool.getName(), toolFunction)
                .description(tool.getDescription())
                .inputType(ToolRequest.class)
                .build();

        // 使用.defaultToolCallbacks()方法注册ToolCallback实例（正确的API）
        builder.defaultToolCallbacks(toolCallback);
        logger.debug("注册工具函数: {}", tool.getName());
    }

    /**
     * 获取可用工具列表（根据请求参数过滤）
     * @param request Agent任务请求
     * @return 可用工具列表
     */
    private List<Tool> getAvailableTools(AgentRequestDTO request) {
        // 如果请求中指定了可用工具列表，只使用指定的工具
        if (request.getAvailableTools() != null && !request.getAvailableTools().isEmpty()) {
            List<Tool> tools = new ArrayList<>();
            for (String toolName : request.getAvailableTools()) {
                Tool tool = toolRegistry.getTool(toolName);
                if (tool != null) {
                    tools.add(tool);
                } else {
                    logger.warn("请求的工具不存在: {}", toolName);
                }
            }
            return tools;
        }

        // 否则使用所有已注册工具
        return toolRegistry.getAllTools();
    }

    /**
     * 构建Agent提示词（包含工具描述和任务描述）
     * @param request Agent任务请求
     * @return Agent提示词
     */
    private String buildAgentPrompt(AgentRequestDTO request) {
        StringBuilder prompt = new StringBuilder();

        // 添加Agent系统提示
        prompt.append("你是一个智能Agent，可以使用以下工具来完成任务：\n\n");

        // 添加工具描述
        prompt.append(toolRegistry.getToolDescriptions());
        prompt.append("\n");

        // 添加ReAct循环说明
        prompt.append("请按照以下步骤完成任务：\n");
        prompt.append("1. 思考：分析任务，确定需要使用哪些工具\n");
        prompt.append("2. 行动：调用合适的工具\n");
        prompt.append("3. 观察：分析工具返回的结果\n");
        prompt.append("4. 重复上述步骤直到完成任务\n\n");

        // 添加任务描述
        prompt.append("任务：").append(request.getTask()).append("\n\n");

        // 添加执行限制
        if (request.getMaxSteps() != null) {
            prompt.append("注意：最多执行").append(request.getMaxSteps()).append("步。\n");
        }

        prompt.append("请开始执行任务，并在每一步说明你的思考过程。");

        return prompt.toString();
    }
}