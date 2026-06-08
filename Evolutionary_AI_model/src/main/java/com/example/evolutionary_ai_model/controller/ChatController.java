package com.example.evolutionary_ai_model.controller;

import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.dto.ChatRequestDTO;
import com.example.evolutionary_ai_model.entity.dto.ChatResponseDTO;
import com.example.evolutionary_ai_model.security.LoginUserDetails;
import com.example.evolutionary_ai_model.service.ChatService;
import com.example.evolutionary_ai_model.service.factory.ChatStrategyFactory;
import com.example.evolutionary_ai_model.service.strategy.DynamicChatStrategy;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 用法：AI对话Controller，负责接收前端对话请求并返回AI响应。
 * 调用ChatService处理业务逻辑，支持普通对话和流式对话两种方式。
 * 位于表现层，只负责接收请求、参数校验、调用业务层、返回响应。
 * 支持动态模型配置，当请求中包含configId时使用DynamicChatStrategy。
 * 从认证信息获取用户ID，实现用户模型配置隔离。
 */
@RestController
@RequestMapping("/chat")
public class ChatController {
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final ChatClient chatClient;
    private final ChatService chatService;
    private final ChatStrategyFactory strategyFactory;
    private final DynamicChatStrategy dynamicChatStrategy;

    public ChatController(ChatClient.Builder chatClientBuilder, ChatService chatService, 
                          ChatStrategyFactory strategyFactory, DynamicChatStrategy dynamicChatStrategy) {
        this.chatClient = chatClientBuilder.build();
        this.chatService = chatService;
        this.strategyFactory = strategyFactory;
        this.dynamicChatStrategy = dynamicChatStrategy;
    }

    /**
     * 测试接口 - 简单对话（使用配置文件中的默认模型）
     * 请求地址: GET /chat/test
     * 测试数据: message参数，如 "你好"
     */
    @GetMapping("/test")
    public String test(@RequestParam String message) {
        logger.info("测试对话请求，消息: {}", message);
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    /**
     * 对话接口 - 根据模式进行对话
     * 请求地址: POST /chat/send
     * 测试数据示例:
     * {
     *   "conversationId": "可选，首次对话不传",
     *   "message": "你好，请介绍一下自己",
     *   "mode": "quick" 或 "expert",
     *   "configId": 123456789,  // 可选，指定使用的模型配置ID
     *   "history": [{"role": "user", "content": "历史消息"}]
     * }
     */
    @PostMapping("/send")
    public Result<ChatResponseDTO> send(@AuthenticationPrincipal UserDetails userDetails,
                                        @Valid @RequestBody ChatRequestDTO request) {
        logger.info("对话请求，模式: {}, 消息长度: {}, configId: {}", 
                request.getMode(), request.getMessage().length(), request.getConfigId());

        try {
            // 从认证信息获取用户ID
            Long userId = getUserId(userDetails);
            request.setUserId(userId);
            
            ChatResponseDTO response = chatService.chat(request);
            logger.info("对话成功，消息ID: {}", response.getMessageId());
            return Result.success(response);
        } catch (IllegalArgumentException e) {
            logger.warn("对话参数错误: {}", e.getMessage());
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            logger.error("对话异常", e);
            return Result.fail("AI对话失败，请稍后重试");
        }
    }

    /**
     * 流式对话接口 - 支持实时返回AI回复
     * 请求地址: POST /chat/stream
     * 测试数据示例:
     * {
     *   "conversationId": "可选",
     *   "message": "请详细解释量子计算的基本原理",
     *   "mode": "quick" 或 "expert",
     *   "configId": 123456789,  // 可选，指定使用的模型配置ID
     *   "history": []
     * }
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@AuthenticationPrincipal UserDetails userDetails,
                               @Valid @RequestBody ChatRequestDTO request) {
        logger.info("流式对话请求，模式: {}, 消息长度: {}, configId: {}", 
                request.getMode(), request.getMessage().length(), request.getConfigId());

        try {
            // 从认证信息获取用户ID
            Long userId = getUserId(userDetails);
            request.setUserId(userId);

            // 如果指定了configId，使用动态模型策略
            if (request.getConfigId() != null) {
                logger.info("使用动态模型策略进行流式对话，configId: {}", request.getConfigId());
                return dynamicChatStrategy.streamChat(request);
            }

            // 如果没有configId但有userId，使用用户的默认模型
            if (userId != null) {
                logger.info("使用用户默认模型进行流式对话，userId: {}", userId);
                return dynamicChatStrategy.streamChat(request);
            }

            // 验证模式是否支持（fallback到配置文件中的模型）
            if (!strategyFactory.isSupported(request.getMode())) {
                logger.warn("不支持的对话模式: {}", request.getMode());
                return Flux.just("错误: 不支持的对话模式 " + request.getMode());
            }

            // 获取对应的策略并执行流式对话
            return strategyFactory.getStrategy(request.getMode()).streamChat(request);

        } catch (Exception e) {
            logger.error("流式对话异常", e);
            return Flux.just("错误: AI流式对话失败 - " + e.getMessage());
        }
    }

    /**
     * 获取支持的对话模式列表
     * 请求地址: GET /chat/modes
     */
    @GetMapping("/modes")
    public Result<List<String>> getModes() {
        logger.info("获取支持的对话模式列表");
        List<String> modes = chatService.getSupportedModes();
        return Result.success(modes);
    }

    /**
     * 从UserDetails获取用户ID
     * @param userDetails 用户详情
     * @return 用户ID，如果未登录返回null
     */
    private Long getUserId(UserDetails userDetails) {
        if (userDetails instanceof LoginUserDetails) {
            return ((LoginUserDetails) userDetails).getUserId();
        }
        logger.warn("无法获取用户ID，userDetails类型: {}", userDetails.getClass().getName());
        return null;
    }
}